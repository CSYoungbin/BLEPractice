/*
 * WHOOP BLE Practice — ESP32 BLE Peripheral (Server)
 * ----------------------------------------------------
 * Board: AITRIP ESP32 ESP-WROOM-32 (Type-C, 30 pins)
 *
 * 동작:
 *   1) BLE advertising 시작 ("WHOOP-Practice")
 *   2) Android 앱이 GATT 연결 + service discovery 수행
 *   3) Android 가 CCCD 에 ENABLE_NOTIFICATION 쓰면
 *      이 sketch 가 매 1초마다 카운터 값을 notify 로 전송
 *   4) Android 앱의 "Last notification" 라벨에 carrier 가 쌓이는지 확인
 *
 * UUID 는 BleConstants.kt 와 정확히 일치해야 함.
 *
 * ---------- Arduino IDE 설정 ----------
 * 1) File → Preferences → Additional Boards Manager URLs:
 *    https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json
 * 2) Tools → Board → Boards Manager → "esp32" 검색 → "esp32 by Espressif" 설치
 * 3) Tools → Board → ESP32 Arduino → "ESP32 Dev Module"
 * 4) Tools → Port → /dev/cu.usbserial-XXXX (macOS) / COMx (Windows)
 *    (Type-C 보드는 CP2102 칩이라 드라이버 자동 인식 보통 OK)
 * 5) 라이브러리는 ESP32 코어에 BLE 가 포함돼 있으므로 추가 설치 불필요
 * 6) Sketch → Upload (보드의 BOOT 버튼 누르면서 업로드해야 하는 경우 있음)
 * 7) Tools → Serial Monitor 115200 baud 로 디버그 로그 확인
 */

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define DEVICE_NAME         "WHOOP-Practice"

BLECharacteristic* pCharacteristic = nullptr;
bool deviceConnected = false;
uint32_t counter = 0;

class ServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer* pServer) override {
    deviceConnected = true;
    Serial.println(">> Central connected");
  }
  void onDisconnect(BLEServer* pServer) override {
    deviceConnected = false;
    Serial.println(">> Central disconnected — restarting advertising");
    // 끊긴 직후 다시 광고 시작 (안 그러면 재연결 불가)
    pServer->getAdvertising()->start();
  }
};

void setup() {
  Serial.begin(115200);
  delay(500);
  Serial.println("Booting WHOOP-Practice BLE peripheral");

  BLEDevice::init(DEVICE_NAME);
  BLEServer* pServer = BLEDevice::createServer();
  pServer->setCallbacks(new ServerCallbacks());

  BLEService* pService = pServer->createService(SERVICE_UUID);

  pCharacteristic = pService->createCharacteristic(
      CHARACTERISTIC_UUID,
      BLECharacteristic::PROPERTY_READ |
      BLECharacteristic::PROPERTY_WRITE |
      BLECharacteristic::PROPERTY_NOTIFY
  );

  // CCCD descriptor 추가 — 안 하면 Android 가 notify 활성화 못 함!
  pCharacteristic->addDescriptor(new BLE2902());
  pCharacteristic->setValue("hello-from-esp32");

  pService->start();

  BLEAdvertising* pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  // iPhone 호환 권장값
  pAdvertising->setMinPreferred(0x06);
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();

  Serial.println("Advertising started: WHOOP-Practice");
}

void loop() {
  if (deviceConnected) {
    counter++;
    char buf[32];
    snprintf(buf, sizeof(buf), "tick=%lu", (unsigned long) counter);
    pCharacteristic->setValue((uint8_t*) buf, strlen(buf));
    pCharacteristic->notify();
    Serial.print("notify: ");
    Serial.println(buf);
  }
  delay(1000);
}
