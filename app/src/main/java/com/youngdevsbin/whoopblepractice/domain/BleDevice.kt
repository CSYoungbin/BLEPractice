package com.youngdevsbin.whoopblepractice.domain

/**
 * Scan에서 발견된 BLE 기기를 표현하는 도메인 모델.
 *
 * [의도]
 * - Android의 BluetoothDevice / ScanResult 를 직접 UI까지 끌고 가지 않고
 *   도메인 모델로 매핑하는 클린 아키텍처 패턴.
 * - ViewModel/UI는 안드로이드 SDK에 의존하지 않게 됨 → testability ↑
 */
data class BleDevice(
    val name: String?,
    val address: String,
    val rssi: Int,
    val isConnectable: Boolean,
)
