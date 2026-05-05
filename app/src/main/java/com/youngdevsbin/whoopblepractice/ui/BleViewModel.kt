package com.youngdevsbin.whoopblepractice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youngdevsbin.whoopblepractice.ble.BleConstants
import com.youngdevsbin.whoopblepractice.ble.BleRepository
import com.youngdevsbin.whoopblepractice.domain.BleConnectionState
import com.youngdevsbin.whoopblepractice.domain.BleDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * MVVM 의 ViewModel — UI 상태 보유 & 비즈니스 로직.
 *
 * [핵심]
 * - View(Compose) 는 절대 BleRepository 를 직접 호출하지 않는다 → 단방향 dataflow.
 * - viewModelScope 는 Activity 회전(configChange) 살아남고, ViewModel 이 cleared 될 때 cancel 됨.
 *
 * [면접 단골]
 * Q: "회전 후에도 BLE 스캔이 끊기지 않는 이유?"
 * A: ViewModel 이 ActivityLifecycle 의 새 인스턴스에 attach 되지만 객체 자체는 같음.
 *    viewModelScope 도 살아있어서 launched coroutine 이 그대로 진행.
 *
 * Q: "viewModelScope 와 lifecycleScope 의 차이?"
 * A: viewModelScope = ViewModel 수명. configChange 안 죽음.
 *    lifecycleScope = Activity/Fragment 수명. 회전 시 cancel.
 */
@HiltViewModel
class BleViewModel @Inject constructor(
    private val repo: BleRepository,
) : ViewModel() {


    data class UiState(
        val devices: List<BleDevice> = emptyList(),
        val isScanning: Boolean = false,
        val connection: BleConnectionState = BleConnectionState.Idle,
        val lastNotification: String? = null,
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    /** repo.connectionState 를 UiState 안으로 합성. */
    init {
        viewModelScope.launch {
            repo.connectionState.collect { state ->
                _ui.update { it.copy(connection = state) }
            }
        }
        viewModelScope.launch {
            repo.notifications.collect { bytes ->
                val text = bytes.toString(Charsets.UTF_8)
                _ui.update { it.copy(lastNotification = text) }
            }
        }
    }

    private var scanJob: Job? = null

    /**
     * TODO[Exercise 4-1 — Coroutines/Flow]:
     *   1) 이전 scanJob 이 있으면 cancel.
     *   2) viewModelScope.launch 로 repo.scanDevices() 를 collect.
     *   3) 발견된 device 를 _ui.devices 에 distinct 로 추가 (address 기준 중복 제거).
     *   4) BleConstants.SCAN_PERIOD_MS 후 자동 stop (withTimeoutOrNull 활용).
     *
     *  Q: "왜 distinctUntilChanged 로 안 되나?"
     *  A: 같은 device 라도 RSSI 가 매번 달라서 emission 자체는 다 다름.
     *     "본 적 있는 address 인지" 를 직접 set 으로 추적해야 함.
     */
    fun startScan() {
        // TODO: 구현
        scanJob?.cancel()
        val deviceMap = mutableMapOf<String, BleDevice>()
        _ui.update { it.copy(isScanning = true, devices = emptyList()) }

        scanJob = viewModelScope.launch {
            withTimeoutOrNull(BleConstants.SCAN_PERIOD_MS) {
                repo.scanDevices()
                    .collect {
                        bleDevice -> deviceMap[bleDevice.address] = bleDevice
                        _ui.update { it.copy(devices = deviceMap.values.toList())
                    }
                }
            }

            _ui.update { it.copy(isScanning = false) }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        _ui.update { it.copy(isScanning = false) }
    }

    /**
     * TODO[Exercise 4-2]:
     *   1) repo.connect(address) 호출
     *   2) 연결 시 isScanning=false 로 자동 전환 (스캔과 connect 동시 진행 X)
     */
    fun connect(address: String) {
        // TODO: 구현
        _ui.update { it.copy(isScanning = false) }
        scanJob?.cancel()
        scanJob = null
        try {
            repo.connect(address)
        } catch (e: Exception) {
            _ui.update { it.copy(connection = BleConnectionState.Failed(e.message ?: "Connect Failed")) }
        }
    }

    fun disconnect() {
        repo.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        // viewModelScope 자체는 자동으로 cancel 되지만,
        // GATT close 같은 native resource 는 명시적으로 정리해야 안전.
        repo.disconnect()
    }
}
