# Android Engineer II — BLE Practice

Connectivity 팀의 Android Engineer II JD 를 기반으로 만든 **BLE 학습 프로젝트**.
실제 ESP32 보드를 peripheral 로 두고 Android 앱이 central 로 동작하도록 구성했습니다.

## 학습 목표

JD에서 요구하는 핵심 역량을 직접 손으로 구현하면서 익힙니다.

- **BLE**: Scan → Connect → Service Discovery → CCCD enable → Notification 수신
- **Kotlin / Coroutines / Flow**: `callbackFlow`, `StateFlow`, `SharedFlow`, `viewModelScope`
- **Architecture**: MVVM, Repository, sealed class 로 표현하는 UiState
- **Lifecycle**: 회전 시 BLE 세션 유지, `onCleared` 에서 GATT close
- **Testing**: MockK + Turbine + `UnconfinedTestDispatcher` 로 ViewModel 검증
- **Permissions**: Android 12+ `BLUETOOTH_SCAN/CONNECT`, 11 이하 `ACCESS_FINE_LOCATION`

## 프로젝트 구조

```
app/src/main/java/com/youngdevsbin/whoopblepractice/
├── MainActivity.kt              # Compose entry, permission 요청
├── domain/
│   ├── BleConnectionState.kt    # sealed class — 상태 표현
│   └── BleDevice.kt             # 도메인 모델 (Android SDK 비의존)
├── ble/
│   ├── BleConstants.kt          # ESP32 와 공유하는 UUID
│   ├── BleScanner.kt            # callbackFlow 로 scan 결과 emit  ⭐ TODO Exercise 1
│   ├── BleGattClient.kt         # BluetoothGattCallback 구현      ⭐ TODO Exercise 2
│   └── BleRepository.kt         # 단일 진입점                      ⭐ TODO Exercise 3
└── ui/
    ├── BleViewModel.kt          # MVVM ViewModel                  ⭐ TODO Exercise 4
    ├── BleScreen.kt             # Stateless Compose UI
    └── PermissionsHelper.kt     # SDK 별 권한 매핑

app/src/test/java/.../BleViewModelTest.kt  ⭐ TODO Exercise 6

esp32/esp32_ble_server.ino       # Arduino sketch (peripheral)
docs/EXERCISES.md                # 인터뷰 카테고리 ↔ TODO 매핑
```

각 ⭐ TODO 는 직접 구현해야 할 자리이며, 해당 파일 안의 주석에 힌트와 인터뷰 단골 질문이 들어있습니다.

## 빠른 시작 — ESP32 ↔ Android 테스트 가이드

### 1) ESP32 펌웨어 굽기

가지고 계신 **AITRIP ESP32 ESP-WROOM-32 (Type-C, 30 pins)** 보드 기준입니다.

1. **Arduino IDE 설치** (1.8.x 또는 2.x).
2. **ESP32 보드 매니저 추가**
   - `File → Preferences → Additional Boards Manager URLs`
     ```
     https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
     ```
   - `Tools → Board → Boards Manager` → "esp32" 검색 → **esp32 by Espressif** 설치 (3.x 권장)
3. **드라이버**: 이 보드는 CP2102 USB-to-UART 칩이라 macOS / 최신 Windows 는 드라이버 자동 인식. 안 되면 [Silicon Labs CP210x driver](https://www.silabs.com/developers/usb-to-uart-bridge-vcp-drivers) 설치.
4. **보드 선택**
   - `Tools → Board → ESP32 Arduino → ESP32 Dev Module`
   - `Tools → Port → /dev/cu.usbserial-XXXX` (macOS) 또는 `COMx` (Windows)
5. `esp32/esp32_ble_server.ino` 를 Arduino IDE 에서 열고 **Upload**.
   - 업로드가 안 된다면 보드의 **BOOT 버튼을 누른 채로** 업로드 시작 → "Connecting…" 점이 나오면 손 떼기.
6. `Tools → Serial Monitor` 를 115200 baud 로 열면
   ```
   Booting WHOOP-Practice BLE peripheral
   Advertising started: WHOOP-Practice
   ```
   가 보여야 정상.

### 2) 손쉬운 동작 확인 (앱 구현 전)

앱 구현이 끝나기 전에도 ESP32 가 정상 동작하는지는 **nRF Connect for Mobile** (Nordic) 으로 검증할 수 있습니다.

1. Play Store 에서 **nRF Connect** 설치
2. Scan 시작 → `WHOOP-Practice` 라는 이름의 기기 발견
3. CONNECT → SERVICES 탭에서 `4fafc201-…` service 안의 `beb5483e-…` characteristic 노출 확인
4. 해당 characteristic 옆 ↓↓ 모양 (Enable notifications) 누르면 1초 단위로 `tick=N` 값이 들어옴

이 과정이 되면 ESP32 측은 완성. Android 앱은 똑같은 동작을 재현하면 됩니다.

### 3) Android 앱 빌드 & 실행

1. Android Studio 에서 이 프로젝트 열기 (Hedgehog 이상 권장).
2. **실제 안드로이드 단말** 연결 (BLE 는 에뮬레이터로 못 한다 — 단말 필수).
3. `app` Run.
4. 첫 실행 시 BLE 권한 요청 허용.
5. **Scan** 버튼 → ESP32 가 발견되면 카드로 노출.
6. **Connect** → 상태가 `Connecting → Connected` 로 진행되면 GATT + CCCD 까지 OK.
7. 1초마다 "Last notification: tick=N" 이 갱신되는지 확인.

### 4) 디버그 팁

- `adb logcat | grep -E "BleScanner|BleGattClient"` 로 핵심 로그 확인.
- 연결이 자꾸 status=133 으로 실패한다면:
  - 이전 연결의 `gatt.close()` 가 안 불렸을 가능성. `cleanup()` 호출 보장.
  - 단말 BT off → on 토글 후 재시도.
- ESP32 가 advertising 안 보이면 보드 RST 버튼 눌러 재부팅.

## 인터뷰 연습용 활용

`docs/EXERCISES.md` 에 각 TODO 가 어떤 인터뷰 카테고리(BLE / Coroutines / Architecture / Lifecycle / Testing / System Design)에 매핑되는지 정리되어 있습니다. 코드에 손대기 전에 한번 훑어보세요.
