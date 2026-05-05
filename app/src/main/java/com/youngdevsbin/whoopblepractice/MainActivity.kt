package com.youngdevsbin.whoopblepractice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.youngdevsbin.whoopblepractice.ui.BleScreen
import com.youngdevsbin.whoopblepractice.ui.BleViewModel
import com.youngdevsbin.whoopblepractice.ui.PermissionsHelper
import com.youngdevsbin.whoopblepractice.ui.theme.WhoopBLEPracticeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * 간단한 ViewModelFactory.
     *
     * TODO[Exercise 5 — DI]:
     *   1) Hilt 또는 Koin 으로 교체.
     *   2) BleRepository 를 @Singleton 으로 제공해서 process 전체에서 공유.
     */
    private val viewModel: BleViewModel by viewModels()

    /** Multi-permission launcher (Android 12+ BLUETOOTH_SCAN/CONNECT, 11 이하 LOCATION) */
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* 결과 처리는 UI 에서 다시 hasAll() 확인 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!PermissionsHelper.hasAll(this)) {
            permissionLauncher.launch(PermissionsHelper.requiredPermissions())
        }

        setContent {
            WhoopBLEPracticeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val state by viewModel.ui.collectAsStateWithLifecycle()
                    BleScreen(
                        state = state,
                        onStartScan = viewModel::startScan,
                        onStopScan = viewModel::stopScan,
                        onConnect = viewModel::connect,
                        onDisconnect = viewModel::disconnect,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
