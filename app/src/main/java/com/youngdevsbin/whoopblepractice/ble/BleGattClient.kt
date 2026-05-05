package com.youngdevsbin.whoopblepractice.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.util.Log
import com.youngdevsbin.whoopblepractice.domain.BleConnectionState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 단일 BLE peripheral 과의 GATT 세션을 캡슐화.
 *
 * [핵심 학습 포인트]
 * - BluetoothGatt 는 한 번에 1개 operation 만 처리한다 (Android 한계).
 *   즉 read 직후 write 를 호출하면 두 번째가 silently fail 할 수 있어,
 *   연속 GATT 요청은 큐잉(operation queue)을 직접 만드는 게 베스트.
 *
 * - 상태 노출은 StateFlow (현재값 필요), 일회성 byte payload 는 SharedFlow.
 *   Q: "왜 둘을 같이 쓰는가?"
 *   A: StateFlow = "지금 연결됐냐?" 같은 latest value 가 의미 있는 데이터.
 *      SharedFlow = "방금 들어온 알림 페이로드" 처럼 stream of events.
 *
 * - onConnectionStateChange 의 status 는 BluetoothGatt.GATT_SUCCESS(0) 인지 먼저 확인.
 *   status 가 0 이 아닌데 newState=CONNECTED 인 경우는 ❌ 비정상으로 다뤄야 함.
 *
 * [면접 단골]
 * Q: "GATT 가 끊어졌을 때 재연결은?"
 * A: 1) onConnectionStateChange 에서 newState=DISCONNECTED 감지
 *    2) gatt.close() 로 리소스 해제 (close 안 하면 다음 연결 시 status=133 발생)
 *    3) 백오프(예: exponential delay)로 connectGatt 재시도
 *    4) 일정 횟수 실패 시 사용자에게 알림 또는 포기
 */
class BleGattClient(private val context: Context) {

    private var gatt: BluetoothGatt? = null

    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Idle)
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    /** Characteristic notification 페이로드 stream (one-shot events) */
    private val _notifications = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    val notifications: SharedFlow<ByteArray> = _notifications.asSharedFlow()

    private val callback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            Log.d(TAG, "onConnectionStateChange status=$status newState=$newState")
            val deviceAddr = g.device.address

            if (status != BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = BleConnectionState.Failed(
                    message = "GATT error",
                    statusCode = status
                )
                cleanup()
                return
            }

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    // TODO[Exercise 2-1]: discoverServices() 호출
                    // 힌트: 연결 직후 service discovery 안 하면 characteristic UUID 로 접근 불가
                    val started = g.discoverServices()
                    if(!started) {
                        _connectionState.value = BleConnectionState.Failed(
                            message = "Service discovery failed to start"
                        )
                        cleanup()
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _connectionState.value = BleConnectionState.Disconnected(
                        deviceAddress = deviceAddr,
                        reason = "GATT disconnected"
                    )
                    cleanup()
                }
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            Log.d(TAG, "onServicesDiscovered status=$status")
            // TODO[Exercise 2-2]:
            //   1) status == GATT_SUCCESS 검증
            //   2) g.getService(BleConstants.SERVICE_UUID) 가져오고
            //      getCharacteristic(BleConstants.CHARACTERISTIC_UUID) 로 char 획득
            //   3) enableNotifications(g, char) 호출
            //   4) _connectionState.value = Connected(g.device.address)

            if (status != BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = BleConnectionState.Failed(
                    message = "Service discovery failed",
                    statusCode = status
                )
                cleanup()
                return
            }
            val service = g.getService(BleConstants.SERVICE_UUID)
            if (service == null) {
                _connectionState.value = BleConnectionState.Failed(
                    message = "Service discovery failed to start"
                )
                cleanup()
                return
            }
            val char = service.getCharacteristic(BleConstants.CHARACTERISTIC_UUID)
            if (char == null) {
                _connectionState.value = BleConnectionState.Failed(
                    message = "char not found"
                )
                cleanup()
                return
            }

            enableNotifications(g, char)
            _connectionState.value = BleConnectionState.Connected(g.device.address)
        }

        @Deprecated("API 33 미만용")
        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            // API 33 미만 — value 를 바로 사용
            @Suppress("DEPRECATION")
            characteristic.value?.let { _notifications.tryEmit(it) }
        }

        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            // API 33+ — value 가 인자로 옴 (race-free)
            _notifications.tryEmit(value)
        }

        override fun onDescriptorWrite(
            g: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            Log.d(TAG, "Descriptor write status=$status (CCCD)")
        }
    }

    /**
     * 연결 시작.
     *
     * TODO[Exercise 2-0]:
     *   1) _connectionState 를 Connecting(device.address) 로 업데이트
     *   2) device.connectGatt(context, autoConnect=false, callback, TRANSPORT_LE) 호출
     *   3) 반환된 BluetoothGatt 를 this.gatt 에 저장
     *
     *  Q: autoConnect=true 와 false 의 차이?
     *  A: false = 즉시 연결 시도, 빠르지만 범위 밖이면 실패.
     *     true  = 백그라운드 reconnect, 느리지만 자동 재연결. 보통 첫 연결은 false 추천.
     */
    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        // TODO: 위 주석대로 구현
        _connectionState.value = BleConnectionState.Connecting(device.address)
        this.gatt = device.connectGatt(context, false, callback, TRANSPORT_LE)
    }

    /**
     * Notification 활성화 — CCCD 에 ENABLE_NOTIFICATION_VALUE write.
     *
     * TODO[Exercise 2-3]:
     *   1) gatt.setCharacteristicNotification(char, true) 호출 (local 등록)
     *   2) char.getDescriptor(BleConstants.CCCD_UUID) 가져오기
     *   3) Android 13(API 33) 이상이면 gatt.writeDescriptor(desc, ENABLE_NOTIFICATION_VALUE) (신규 API)
     *      미만이면 desc.value = ENABLE_NOTIFICATION_VALUE 후 gatt.writeDescriptor(desc) (deprecated)
     *
     *  Q: "왜 setCharacteristicNotification 만으론 안 되나?"
     *  A: 그건 Android local 의 callback 등록일 뿐. peripheral 은 CCCD write 받을 때까지
     *     실제 notify 를 안 보낸다.
     */
    @SuppressLint("MissingPermission")
    private fun enableNotifications(
        g: BluetoothGatt,
        char: BluetoothGattCharacteristic
    ) {
        // TODO: 위 주석대로 구현
        g.setCharacteristicNotification(char, true)
        val descriptor = char.getDescriptor(BleConstants.CCCD_UUID)
        if (descriptor == null) {
            _connectionState.value = BleConnectionState.Failed(
                message = "CCCD descriptor not found"
            )
            cleanup()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            g.writeDescriptor(descriptor, ENABLE_NOTIFICATION_VALUE)
        } else {
            descriptor.value = ENABLE_NOTIFICATION_VALUE
            g.writeDescriptor(descriptor)
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        gatt?.disconnect()
        // close 는 onConnectionStateChange(DISCONNECTED) 콜백 후 cleanup() 에서 처리
    }

    @SuppressLint("MissingPermission")
    private fun cleanup() {
        gatt?.close()
        gatt = null
    }

    companion object {
        private const val TAG = "BleGattClient"
    }
}
