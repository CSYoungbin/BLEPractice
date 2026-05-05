package com.youngdevsbin.whoopblepractice.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import com.youngdevsbin.whoopblepractice.domain.BleConnectionState
import com.youngdevsbin.whoopblepractice.domain.BleDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * BLE 관련 모든 동작의 단일 진입점.
 *
 * [Architecture — MVVM/Repository]
 * - ViewModel 은 BleRepository 만 알고, BleScanner / BleGattClient 의 존재를 모른다.
 * - Repository 가 Android SDK(Bluetooth*) 를 캡슐화 → ViewModel 은 unit testable.
 *
 * [면접 단골]
 * Q: "Repository 패턴의 의의는?"
 * A: 1) data source 변경(local DB, remote API, BLE 등) 을 ViewModel 에서 격리
 *    2) 동일 데이터를 여러 ViewModel 이 공유할 때 single source of truth
 *    3) test 시 fake repository 로 쉽게 swap
 */
class BleRepository(
    context: Context,
    private val scanner: BleScanner = BleScanner(context),
    private val gattClient: BleGattClient = BleGattClient(context),
) {

    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter = bluetoothManager.adapter

    val connectionState: StateFlow<BleConnectionState> = gattClient.connectionState
    val notifications: SharedFlow<ByteArray> = gattClient.notifications

    /**
     * 스캔 결과 stream.
     *
     * TODO[Exercise 3-1 — Architecture]:
     *   1) scanner.scan() 결과를 그대로 노출.
     *   2) 추가 가공이 필요하면 여기서 .map / .filter 로 적용 (e.g. RSSI 필터).
     *
     *  Q: "왜 ViewModel 이 BleScanner 를 직접 안 쓰고 Repository 거치나?"
     *  A: 도메인 변환, 캐싱, 다중 source 합성 등 데이터 정제 로직의 자리이기 때문.
     */
    fun scanDevices(): Flow<BleDevice> = scanner.scan()

    /**
     * MAC 주소로 GATT 연결.
     *
     * TODO[Exercise 3-2]:
     *   1) adapter.getRemoteDevice(address) 로 BluetoothDevice 가져오기
     *   2) gattClient.connect(device) 호출
     *   3) 잘못된 address 형식이면 IllegalArgumentException
     */
    @SuppressLint("MissingPermission")
    fun connect(address: String) {
        if (adapter == null || !adapter.isEnabled) {
            error("Bluetooth adapter unavailable")
        }

        // TODO: 구현
        val device = adapter.getRemoteDevice(address)
            ?: throw IllegalArgumentException("Invalid address: $address")
        gattClient.connect(device = device)
    }

    fun disconnect() {
        gattClient.disconnect()
    }
}
