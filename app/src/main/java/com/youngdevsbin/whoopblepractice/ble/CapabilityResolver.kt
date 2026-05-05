package com.youngdevsbin.whoopblepractice.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.youngdevsbin.whoopblepractice.domain.DeviceCapabilities
import com.youngdevsbin.whoopblepractice.domain.Feature
import com.youngdevsbin.whoopblepractice.domain.SemVer
import com.youngdevsbin.whoopblepractice.domain.WhoopModel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 연결된 BluetoothGatt 에서 Device Information Service 를 read 해서
 * DeviceCapabilities 를 뽑아내는 역할.
 *
 * [Stretch Goal S6 — Capability Registry]
 *
 * [면접 단골 — 시스템 디자인]
 * Q: "WHOOP 4.0 / 5.0 / MG 가 동시에 존재하는데 같은 앱으로 어떻게 지원?"
 * A: 1) Service UUID 는 제품군 공통.
 *    2) 연결 직후 Device Information Service 에서 Model Number / Firmware Revision read.
 *    3) discoverServices() 결과의 characteristic 존재 여부로 capability 판단.
 *    4) UseCase/ViewModel 은 capabilities.supports(Feature.X) 로 분기.
 *
 * Q: "그냥 model name 으로 if 분기하면 안 되나?"
 * A: 새 모델 출시 시마다 앱 코드를 고쳐 deploy 해야 함.
 *    capability 기반은 펌웨어가 새 characteristic 만 노출하면 자동 인식 가능 → forward compatible.
 */
class CapabilityResolver {

    /**
     * TODO[S6-2]:
     *   1) DeviceInformationService.MODEL_NUMBER 를 read → String
     *   2) DeviceInformationService.FIRMWARE_REVISION 을 read → String → SemVer.parse
     *   3) gatt 의 service 목록에서 FeatureCharacteristics 의 각 UUID 가 있는지 확인
     *      → Feature.HEART_RATE / SKIN_TEMPERATURE / ECG 추가
     *   4) DeviceCapabilities(model, firmware, supports) 반환
     *
     *   힌트: BluetoothGatt.readCharacteristic() 은 콜백 기반이라 suspend 로 감싸야 한다.
     *         아래의 readCharacteristic suspending helper 를 참고.
     */
    @SuppressLint("MissingPermission")
    suspend fun resolve(gatt: BluetoothGatt): DeviceCapabilities {
        // TODO: 위 주석대로 구현. 일단은 EMPTY 반환.
        return DeviceCapabilities.EMPTY
    }

    /**
     * TODO[S6-1]: BluetoothGattCallback.onCharacteristicRead 와 연동되는 suspend helper.
     *
     * 구현 힌트:
     *  - suspendCancellableCoroutine 사용
     *  - 외부에서 BluetoothGattCallback 의 onCharacteristicRead 콜백을 받아
     *    continuation.resume(value) 호출하도록 wiring 필요
     *  - 또는 더 깔끔하게: BleGattClient 에 read queue 를 두고 suspend 로 expose
     *
     *  ⚠️ BluetoothGatt 는 한 번에 1개 GATT operation 만 처리한다.
     *     read 를 연속 호출하면 두 번째가 silent fail. mutex 또는 channel 로 직렬화 필요 (S1 GATT op queue 와 합쳐서 풀면 좋음).
     */
    @SuppressLint("MissingPermission")
    private suspend fun readString(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
    ): String? = suspendCancellableCoroutine { cont ->
        // TODO: 구현
        cont.resume(null)
    }

    /**
     * TODO[S6-5 — Stretch]: 펌웨어 버전 분기.
     *   - firmwareVersion >= SemVer(1,4,0) 이면 Feature.OTA_V2 add
     *   - 그 미만이면 OTA_V2 없음
     */
    private fun firmwareGatedFeatures(firmware: SemVer): Set<Feature> {
        // TODO: 구현
        return emptySet()
    }

    /**
     * 단순 helper — capability 기반 if 분기 예시를 위해 ViewModel/UseCase 에서 호출.
     *
     * 사용 예:
     *   if (capabilities.supports(Feature.ECG)) {
     *       // ECG 카드 표시
     *   }
     */
    fun debugSummary(capabilities: DeviceCapabilities): String =
        "${capabilities.model.displayName} fw=${capabilities.firmwareVersion} " +
            "supports=${capabilities.supports.joinToString()}"
}
