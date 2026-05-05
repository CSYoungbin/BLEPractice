package com.youngdevsbin.whoopblepractice.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.youngdevsbin.whoopblepractice.domain.BleDevice
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * BLE 스캔을 callback → Flow 로 변환하는 클래스.
 *
 * [핵심 학습 포인트 — Coroutines/Flow]
 * - callbackFlow 는 콜백 기반 API 를 cold Flow 로 wrapping 할 때 사용한다.
 * - awaitClose { ... } 안에서 반드시 리소스(스캐너) cleanup 해야 leak 안 남.
 * - cold Flow 라 collect 시점에 scan 이 시작되고, cancel 시점에 stop 됨.
 *
 * [면접 단골]
 * Q: "왜 cold flow 가 BLE scan 에 적합한가?"
 * A: 구독자가 있을 때만 스캔 시작 → 배터리 절약. unsubscribe 자동 cleanup.
 *
 * Q: "여러 곳에서 동일한 scan 결과를 공유하려면?"
 * A: shareIn(SharingStarted.WhileSubscribed(), replay = ...) 로 hot 으로 변환.
 */
class BleScanner(context: Context) {

    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter

    /**
     * BLE scan 결과를 cold Flow 로 emit.
     *
     * @param filterServiceUuid null 이면 모든 advertising 수신, 지정 시 해당 service 만.
     *
     * TODO[Exercise 1 — BLE/Coroutines]:
     *   1) ScanFilter 를 사용해 BleConstants.SERVICE_UUID 만 스캔하도록 필터링.
     *      힌트: ScanFilter.Builder().setServiceUuid(ParcelUuid(uuid)).build()
     *   2) ScanSettings 를 LOW_LATENCY 로 설정해서 발견 속도 ↑
     *   3) 동일 device address 가 여러 번 emit 되지 않도록 distinct 처리.
     *      (Flow operator 또는 set 을 사용)
     *   4) BluetoothAdapter null 이거나 disabled 일 때 IllegalStateException 발생시키기.
     *
     * 정답 검증: ESP32 를 켠 채로 scan 했을 때 같은 기기가 한 번만 emit 되어야 한다.
     */
    @SuppressLint("MissingPermission")
    fun scan(filterServiceUuid: java.util.UUID? = BleConstants.SERVICE_UUID, scanMode: Int = ScanSettings.SCAN_MODE_LOW_LATENCY): Flow<BleDevice> = callbackFlow {
        val scanner = adapter?.takeIf { it.isEnabled }?.bluetoothLeScanner
            ?: error("BluetoothAdapter is null or BLE scanner unavailable")

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.toDomainOrNull() ?: return
                trySend(device)
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                results.forEach { res -> res.toDomainOrNull()?.let { trySend(it) } }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e(TAG, "Scan failed: $errorCode")
                close(IllegalStateException("BLE scan failed: $errorCode"))
            }
        }

        // TODO[Exercise 1-1]: filterServiceUuid 가 null 이 아닐 때 ScanFilter 적용
        val filters: List<ScanFilter> = filterServiceUuid?.let {
            listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(it)).build())
        } ?: emptyList()

        // TODO[Exercise 1-2]: ScanSettings.SCAN_MODE_LOW_LATENCY 로 변경
        val settings = ScanSettings.Builder()
            .setScanMode(scanMode)
            .build()

        Log.d(TAG, "Starting BLE scan...")
        scanner.startScan(filters, settings, callback)

        awaitClose {
            // ⚠️ 반드시 stopScan! 안 하면 백그라운드에서 계속 스캔하며 배터리 소모
            Log.d(TAG, "Stopping BLE scan...")
            try {
                scanner.stopScan(callback)
            } catch (e: SecurityException) {
                // Android 12+ BLUETOOTH_SCAN 권한이 도중에 회수된 케이스
                Log.w(TAG, "stopScan SecurityException", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun ScanResult.toDomainOrNull(): BleDevice? {
        val addr = device?.address ?: return null
        return BleDevice(
            name = device?.name ?: scanRecord?.deviceName,
            address = addr,
            rssi = rssi,
            isConnectable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                isConnectable
            } else {
                true  // API 26 미만: 일단 true 로 가정 (대부분 connectable)
            },
        )
    }

    companion object {
        private const val TAG = "BleScanner"
    }
}
