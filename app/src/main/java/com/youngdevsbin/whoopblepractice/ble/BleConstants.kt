package com.youngdevsbin.whoopblepractice.ble

import java.util.UUID

/**
 * ESP32 BLE Server 와 동일한 UUID 를 사용해야 함.
 *
 * 이 값들은 esp32/esp32_ble_server.ino 의 SERVICE_UUID / CHARACTERISTIC_UUID 와 1:1로 매칭됨.
 * 변경 시 양쪽을 같이 변경할 것.
 *
 * [면접 단골 — BLE]
 * Q: "Service / Characteristic / Descriptor 가 뭔가요?"
 * A: - Service: 관련 기능의 묶음 (e.g. Heart Rate Service)
 *    - Characteristic: 실제 데이터 read/write/notify 단위
 *    - Descriptor: characteristic 의 메타데이터 (CCCD = notify 활성화 플래그)
 *
 * Q: "CCCD 가 뭔가요?"
 * A: Client Characteristic Configuration Descriptor.
 *    UUID: 00002902-0000-1000-8000-00805f9b34fb (BLE 표준 SIG 값).
 *    여기에 ENABLE_NOTIFICATION_VALUE / ENABLE_INDICATION_VALUE 를 write 해야
 *    실제로 peripheral 이 notify 를 보내기 시작한다.
 */
object BleConstants {

    // ESP32 예제에서 흔히 쓰이는 random UUID — 양쪽이 같으면 됨
    val SERVICE_UUID: UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    val CHARACTERISTIC_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")

    // BLE SIG 표준 — 모든 peripheral 의 CCCD 에서 동일
    val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    // ESP32 advertising 시 노출하는 device name (스캔 필터링에 사용)
    const val DEVICE_NAME_PREFIX = "WHOOP-Practice"

    const val SCAN_PERIOD_MS = 10_000L
    const val CONNECT_TIMEOUT_MS = 15_000L

    /**
     * BLE SIG 표준 — Device Information Service.
     *
     * 모든 BLE 기기가 이 service 를 지원하는 건 아니지만,
     * 제대로 만든 consumer 디바이스(WHOOP, Apple Watch, 헤드폰 등) 는 거의 다 노출함.
     * Stretch Goal S6 (capability registry) 에서 사용.
     */
    object DeviceInformationService {
        // 16-bit standard UUID 를 128-bit 로 확장한 형태 (BLE Base UUID + assigned number)
        val SERVICE: UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")

        /** Manufacturer Name String — UTF-8 string */
        val MANUFACTURER_NAME: UUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb")

        /** Model Number String — e.g. "WHOOP 4.0" / "ESP-V1" */
        val MODEL_NUMBER: UUID = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb")

        /** Firmware Revision String — e.g. "1.4.207" */
        val FIRMWARE_REVISION: UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb")

        /** Hardware Revision String — e.g. "B" */
        val HARDWARE_REVISION: UUID = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb")
    }

    /**
     * 모델별로 일부만 존재하는 characteristic — capability 분기에 쓰임.
     * 학습용으로는 ESP32 sketch 에서 일부만 노출하도록 만들어볼 것.
     */
    object FeatureCharacteristics {
        // 4.0+ 모든 모델 공통
        val HEART_RATE: UUID = UUID.fromString("4d484f57-0000-1000-8000-000000002001")

        // 5.0+ 부터 존재
        val SKIN_TEMPERATURE: UUID = UUID.fromString("4d484f57-0000-1000-8000-000000002010")

        // MG 전용
        val ECG: UUID = UUID.fromString("4d484f57-0000-1000-8000-000000002020")
    }
}
