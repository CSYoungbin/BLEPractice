package com.youngdevsbin.whoopblepractice.domain

/**
 * 기기의 모델/펌웨어/기능을 한 곳에 모아둔 capability 모델.
 *
 * [WHOOP 관점]
 * - 같은 앱이 4.0 / 5.0 / MG 를 모두 지원해야 함.
 * - "이 기기는 ECG 를 지원하느냐?" 를 ViewModel 이 물어볼 때
 *   곳곳에서 if (model == "MG") 분기하는 게 아니라
 *   capabilities.supports(Feature.ECG) 한 줄로 끝낸다.
 *
 * [면접 단골]
 * Q: "feature flag 와 capability registry 의 차이?"
 * A: feature flag 는 서버에서 켜고 끄는 것. capability 는 기기 자체의 능력.
 *    둘은 보통 같이 쓰임 — capability 가 있어도 flag 로 추가 게이팅.
 */
data class DeviceCapabilities(
    val model: WhoopModel,
    val firmwareVersion: SemVer,
    val supports: Set<Feature>,
) {
    fun supports(feature: Feature): Boolean = feature in supports

    companion object {
        val EMPTY = DeviceCapabilities(
            model = WhoopModel.Unknown,
            firmwareVersion = SemVer(0, 0, 0),
            supports = emptySet(),
        )
    }
}

/**
 * 모델 enum — Device Information Service 의 Model Number 문자열을 파싱해 얻음.
 */
sealed class WhoopModel(val displayName: String) {
    data object Whoop40 : WhoopModel("WHOOP 4.0")
    data object Whoop50 : WhoopModel("WHOOP 5.0")
    data object WhoopMG : WhoopModel("WHOOP MG")

    /** 학습용: ESP32 가 advertising 할 때 가짜로 자기 자신을 뭐라고 부를지 */
    data class EspMock(val variant: String) : WhoopModel("ESP-Mock-$variant")

    data object Unknown : WhoopModel("Unknown")

    companion object {
        fun fromModelNumber(raw: String?): WhoopModel = when {
            raw == null -> Unknown
            raw.contains("4.0") -> Whoop40
            raw.contains("5.0") -> Whoop50
            raw.contains("MG", ignoreCase = true) -> WhoopMG
            raw.startsWith("ESP", ignoreCase = true) -> EspMock(raw.removePrefix("ESP-").ifBlank { "0" })
            else -> Unknown
        }
    }
}

/**
 * 기기가 지원할 수 있는 feature 의 enum.
 *
 * 새 모델/펌웨어 가 새 feature 를 추가할 때 여기를 늘리고
 * CapabilityResolver 에서 분기 추가만 하면 됨 → ViewModel 코드 손 안 대도 됨.
 */
enum class Feature {
    HEART_RATE,
    SKIN_TEMPERATURE,
    ECG,
    OTA_V2,
}

/** 펌웨어 버전 비교용 간단 SemVer. */
data class SemVer(val major: Int, val minor: Int, val patch: Int) : Comparable<SemVer> {
    override fun compareTo(other: SemVer): Int = compareValuesBy(
        this, other, SemVer::major, SemVer::minor, SemVer::patch
    )

    companion object {
        /** "1.4.207" -> SemVer(1, 4, 207). 잘못된 입력은 0.0.0 으로. */
        fun parse(raw: String?): SemVer {
            if (raw == null) return SemVer(0, 0, 0)
            val parts = raw.split(".").mapNotNull { it.toIntOrNull() }
            return SemVer(
                major = parts.getOrNull(0) ?: 0,
                minor = parts.getOrNull(1) ?: 0,
                patch = parts.getOrNull(2) ?: 0,
            )
        }
    }
}
