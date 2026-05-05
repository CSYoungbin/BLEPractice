package com.youngdevsbin.whoopblepractice

import com.youngdevsbin.whoopblepractice.ble.BleRepository
import com.youngdevsbin.whoopblepractice.domain.BleConnectionState
import com.youngdevsbin.whoopblepractice.domain.BleDevice
import com.youngdevsbin.whoopblepractice.ui.BleViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * BleViewModel unit test scaffold.
 *
 * [면접 단골 — Testing]
 * Q: "TestCoroutineDispatcher 와 UnconfinedTestDispatcher 의 차이는?"
 * A: - StandardTestDispatcher: 명시적으로 advanceUntilIdle 해야 작업 진행됨. 정확한 timing test.
 *    - UnconfinedTestDispatcher: 즉시 실행. 빠른 setup test 에 적합. flow 검증에 자주 씀.
 *
 * Q: "ViewModel test 시 Main dispatcher 어떻게?"
 * A: viewModelScope 가 Main dispatcher 사용. setMain(testDispatcher) 로 swap 후
 *    @After 에서 resetMain() 호출.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BleViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: BleRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk(relaxed = true)
        every { repo.connectionState } returns MutableStateFlow(BleConnectionState.Idle)
        every { repo.notifications } returns MutableSharedFlow()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * TODO[Exercise 6-1 — Testing]:
     *   1) repo.scanDevices() 가 BleDevice("a","AA:01",...), BleDevice("b","AA:02",...) 를
     *      방출하는 fake flow 를 반환하도록 mock.
     *   2) viewModel.startScan() 호출 후 viewModel.ui.value.devices 가 2개 인지 검증.
     *
     *  힌트: Turbine 의 .test { } 또는 직접 .value 검증.
     */
    @Test
    fun `startScan emits discovered devices into UiState`() = runTest {
        every { repo.scanDevices() } returns flowOf(
            BleDevice("Esp1", "AA:BB:CC:00:00:01", -50, true),
            BleDevice("Esp2", "AA:BB:CC:00:00:02", -60, true),
        )

        val vm = BleViewModel(repo)

        vm.startScan()

        assertEquals(2, vm.ui.value.devices.size)
    }

    /**
     * TODO[Exercise 6-2]:
     *   - repo.connectionState 를 직접 Connected 로 emit 시켰을 때
     *     vm.ui.value.connection 도 Connected 가 되는지 검증.
     */
    @Test
    fun `connectionState propagates to UiState`() = runTest {
        val flow = MutableStateFlow<BleConnectionState>(BleConnectionState.Idle)
        every { repo.connectionState } returns flow

        val vm = BleViewModel(repo)
        flow.value = BleConnectionState.Connected("AA:BB:CC:00:00:01")

        assertEquals(BleConnectionState.Connected("AA:BB:CC:00:00:01"), vm.ui.value.connection)
    }
}
