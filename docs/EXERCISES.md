# Exercises — WHOOP Android Engineer II 인터뷰 카테고리 매핑

이 프로젝트의 모든 ⭐ TODO 는 `whoop-android-interview` 스킬의 인터뷰 카테고리에 1:1로 매핑되도록 설계되어 있습니다. 각 exercise 를 풀고 나면, 해당 카테고리 면접 질문이 들어왔을 때 **실제 코딩 경험을 근거로 답할 수 있어야** 합니다.

## 진행 순서 (권장)

1. Exercise 1 → ESP32 가 보이게 한다
2. Exercise 2 → 연결 + notification 수신
3. Exercise 3, 4 → MVVM 흐름 완성
4. Exercise 5 → DI 로 리팩터링
5. Exercise 6 → ViewModel unit test 작성
6. Stretch goals (아래) → 시니어 레벨 깊이

---

## Exercise 1 — `BleScanner.kt`

**카테고리**: BLE / Coroutines / Flow

**할 일**
- ScanFilter 로 우리 service UUID 만 발견
- `SCAN_MODE_LOW_LATENCY` 적용
- 같은 address 중복 emit 방지
- adapter null/disabled 시 의미 있는 예외

**관련 인터뷰 질문**
- "BLE scan 결과를 중복 없이 collect 하는 로직을 작성하세요." (라이브 코딩)
- "왜 cold flow 가 BLE scan 에 적합한가?" (개념)
- "여러 화면에서 동일한 scan 결과를 공유하려면?" → `shareIn` 답변

**검증**: ESP32 켠 채 Scan 누르면 `WHOOP-Practice` 카드가 **딱 한 번** 표시.

---

## Exercise 2 — `BleGattClient.kt`

**카테고리**: BLE / Connectivity (WHOOP 핵심)

**할 일**
- 2-0: `connect()` 에서 `connectGatt(autoConnect=false)` 호출 + 상태를 `Connecting` 으로
- 2-1: `onConnectionStateChange(STATE_CONNECTED)` 시 `discoverServices()`
- 2-2: `onServicesDiscovered` 에서 service/characteristic 획득 + `enableNotifications` 호출 + 상태 `Connected`
- 2-3: CCCD 에 `ENABLE_NOTIFICATION_VALUE` write (API 33 분기 처리)

**관련 인터뷰 질문**
- "`BluetoothGattCallback` 을 구현하고 연결 상태를 `StateFlow` 로 노출하세요." (라이브 코딩)
- "Notification 과 Indication 의 차이? CCCD 는 어떻게 설정?" (개념)
- "GATT 연결이 끊어졌을 때 재연결 로직 설계는?" → status=133 트랩, exponential backoff

**검증**: Connect 누르면 `Connected` 표시 + 1초마다 `tick=N` notification 라벨 갱신.

---

## Exercise 3 — `BleRepository.kt`

**카테고리**: Architecture (MVVM / Repository)

**할 일**
- 3-1: `scanDevices()` 에서 scanner 결과 그대로 노출
- 3-2: `connect(address)` 에서 `adapter.getRemoteDevice(address)` 후 `gattClient.connect()`

**관련 인터뷰 질문**
- "Repository 패턴을 쓰는 이유와 BLE 프로젝트에서 어떻게 적용?"
- "Android SDK 클래스를 ViewModel 에서 직접 쓰면 어떤 문제가?"
- "여러 BLE 기기 동시 연결을 어떻게 확장?" (system design follow-up)

---

## Exercise 4 — `BleViewModel.kt`

**카테고리**: Coroutines / MVVM

**할 일**
- 4-1: 이전 scanJob cancel → `viewModelScope.launch { repo.scanDevices().collect { ... } }` → `withTimeoutOrNull(SCAN_PERIOD_MS)` 로 자동 stop → distinct address 누적
- 4-2: `connect()` 에서 isScanning=false 후 repo 호출

**관련 인터뷰 질문**
- "회전 후에도 스캔이 끊기지 않는 이유?"
- "`viewModelScope` vs `lifecycleScope`?"
- "`combine()` 으로 여러 StateFlow 를 합쳐 UiState 만드는 코드?"

**Bonus**: `combine(scanFlow, connectionState) { ... }` 으로 한번에 합쳐보기.

---

## Exercise 5 — `MainActivity.kt` 의 ViewModelFactory

**카테고리**: Architecture / DI

**할 일**
- Hilt(또는 Koin) 도입 → `@HiltAndroidApp` Application 추가, `BleRepository` 를 `@Singleton` 으로 제공
- `MainActivity` 의 inline factory 제거, `by viewModels()` 만 남기기
- `@HiltViewModel` + `@Inject` 로 BleViewModel 갈아끼우기

**관련 인터뷰 질문**
- "DI 컨테이너 없이 만든 코드의 단점?"
- "Hilt scope (`@Singleton`, `@ActivityRetainedScoped`, `@ViewModelScoped`) 의 차이?"

---

## Exercise 6 — `BleViewModelTest.kt`

**카테고리**: Testing

**할 일**
- 6-1: `repo.scanDevices()` 가 fake flow 반환하도록 mock → `vm.startScan()` 후 `ui.value.devices.size == 2` 검증
- 6-2: `connectionState` MutableStateFlow 직접 변경 후 `vm.ui.value.connection` 동기화 검증

**관련 인터뷰 질문**
- "BLE 연결 로직을 unit test 하려면 어떤 전략을 쓰나요?"
- "`UnconfinedTestDispatcher` 를 언제 쓰나?"
- "Flow test 에 Turbine 을 쓰는 이유?"

**Bonus**: Turbine 의 `flow.test { awaitItem() }` 으로 emission 순서 검증.

---

## Stretch Goals (시니어 레벨)

다 풀고 나서 한 단계 더 — JD 의 "Create and own systems that aid in analyzing connectivity health of our members" 를 의식한 확장.

### S1. GATT operation queue
연속 read/write 호출이 silent fail 하는 문제를 큐로 직렬화. coroutines `Channel` 또는 `Mutex` 활용.

> 관련 면접 질문: "BLE throughput 을 최적화하기 위해 고려할 요소들?"

### S2. Exponential backoff reconnect
disconnect 감지 시 1s → 2s → 4s → … → max 30s 로 재연결. cancel signal 로 중단 가능하게.

> 관련 면접 질문: "GATT 가 끊어졌을 때 재연결 로직을 설계하시겠어요?"

### S3. Connectivity Health metrics
`StateFlow<HealthMetrics>` — 평균 RSSI, scan-to-connect latency p50/p95, disconnect 빈도, OTA 진행률.
이걸 Firebase Analytics / Datadog 등에 보낼 인터페이스 설계.

> 관련 면접 질문: "BLE 기기의 펌웨어 업데이트 기능을 Android 앱에서 어떻게 설계하겠어요?"

### S4. Multi-device manager
한번에 여러 ESP32 와 연결되도록 `Map<DeviceAddress, BleGattClient>` 보관 + per-device StateFlow.

> 관련 면접 질문: "여러 BLE 기기를 동시에 연결 관리하는 아키텍처를 그려보세요."

### S5. UI test — Compose
`createComposeRule()` 로 BleScreen 의 connect 버튼 클릭 → ViewModel 호출 검증.

### S6. Capability Registry — 모델 / 펌웨어별 feature 분기 ⭐ (WHOOP 특화)

> 관련 면접 질문 — 시스템 디자인:
> - "WHOOP 4.0 / 5.0 / MG 가 동시에 존재하는데 같은 앱으로 어떻게 지원?"
> - "옛 펌웨어 버그를 앱에서 우회해야 한다면 어디에 분기?"
> - "새 모델 출시 시 앱 업데이트 없이 기능을 켤 수 있게 하려면?"

WHOOP 같은 회사는 **Service UUID 는 제품군 공통**, **Characteristic 은 모델별로 일부만 존재**하는 구조를 씁니다. 그래서 앱은 연결 직후:

1. **Device Information Service** (`0x180A`) 의 Model Number / Firmware Revision 을 read
2. `discoverServices()` 결과를 보고 어떤 characteristic 이 존재하는지 확인
3. 두 정보를 합쳐 `DeviceCapabilities` 객체 생성
4. 모든 ViewModel/UseCase 는 `capabilities.supports(Feature.X)` 로 분기

**할 일** (`ble/CapabilityResolver.kt` 의 TODO 참조)
- S6-1: `BleConstants` 의 `DeviceInformationService` 객체에 정의된 표준 UUID(0x180A, 0x2A24, 0x2A26) 로 read 호출
- S6-2: `CapabilityResolver.resolve()` 가 `BluetoothGatt` + Model Number + Firmware Version 을 받아 `DeviceCapabilities` 반환
- S6-3: `BleConnectionState.Connected` 에 `capabilities: DeviceCapabilities` 필드를 추가, ViewModel UiState 까지 propagate
- S6-4: 임시 Feature 분기 — `HEART_RATE_CHAR` 가 있으면 `Feature.HEART_RATE` add, 없으면 skip
- S6-5 (Stretch): 펌웨어 SemVer 비교로 "1.4.0 미만은 OTA_V2 미지원" 같은 분기 추가

**검증**: ESP32 펌웨어를 두 개 만들어 (model="ESP-V1" / "ESP-V2") 어느 쪽에 연결됐는지에 따라 `capabilities.model` 이 다르게 잡히는지 확인.

**왜 이게 면접 무게를 키우나**
- JD 의 "Create and own systems that aid in analyzing connectivity health" 와 정확히 매칭
- 시스템 디자인 follow-up 에서 capability registry 패턴을 **자기 코드** 로 설명할 수 있게 됨
- "하드코딩 vs 런타임 discovery" 의 trade-off 를 실제로 경험

---

## 막힐 때 스스로 묻는 면접 질문

각 TODO 풀 때 옆에 이 질문을 두고 풀이를 말로 해보세요.
실전 인터뷰에서 채점 포인트는 "정답" 이 아니라 **사고 과정의 명료함** 입니다.

1. 이 자료구조가 thread-safe 한가? (BLE callback 은 binder thread)
2. 권한이 회수되면 어떻게 graceful 하게 fail 하지?
3. 사용자가 화면 회전하면 어떻게 되지?
4. unit test 로 검증하려면 어디를 mock 해야 하지?
5. 시니어 한 명이 코드 리뷰 한다면 가장 먼저 깔 곳은 어디일까?
