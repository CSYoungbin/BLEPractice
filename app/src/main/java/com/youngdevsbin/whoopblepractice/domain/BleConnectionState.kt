package com.youngdevsbin.whoopblepractice.domain

/**
 * BLE GATT 연결 상태를 표현하는 sealed class.
 *
 * [면접 단골 — Architecture]
 * Q: "왜 enum이 아닌 sealed class를 쓰나?"
 * A: 각 상태가 서로 다른 데이터를 가질 수 있고(예: Failed는 에러 메시지),
 *    when 분기에서 컴파일 타임에 exhaustive check 가능.
 *
 * Q: "MVVM에서 이걸 ViewModel이 StateFlow로 노출하면?"
 * A: UI는 collect만 하고 분기 로직은 ViewModel에 캡슐화 가능.
 */
sealed class BleConnectionState {
    /** 초기 상태 — 아직 연결 시도 전 */
    data object Idle : BleConnectionState()

    /** 스캔 중 */
    data object Scanning : BleConnectionState()

    /** GATT 연결 시도 중 */
    data class Connecting(val deviceAddress: String) : BleConnectionState()

    /**
     * GATT 연결 완료 + Service Discovery 완료.
     *
     * TODO[S6-3]: capabilities: DeviceCapabilities 필드를 추가해서
     *   CapabilityResolver.resolve() 결과를 여기로 흘려보내면
     *   UI 가 곧바로 "ECG 지원 여부" 등을 분기할 수 있게 된다.
     *   기본값은 DeviceCapabilities.EMPTY 로 두면 backward compatible.
     */
    data class Connected(val deviceAddress: String) : BleConnectionState()

    /** 연결 끊김 — 재연결 로직 필요 */
    data class Disconnected(val deviceAddress: String?, val reason: String) : BleConnectionState()

    /** 연결 실패 (status code, 권한 부족 등) */
    data class Failed(val message: String, val statusCode: Int? = null) : BleConnectionState()
}
