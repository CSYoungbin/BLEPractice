package com.youngdevsbin.whoopblepractice.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * BLE 권한 체크 헬퍼.
 *
 * [면접 단골 — Permissions]
 * Q: "Android 12+ 에서 BLE 권한 처리는?"
 * A: 1) BLUETOOTH_SCAN: 스캔용. neverForLocation flag 붙이면 location 권한 면제.
 *    2) BLUETOOTH_CONNECT: 연결/페어링용.
 *    3) Android 11 이하: ACCESS_FINE_LOCATION 추가로 필요 (BLE 비콘 ↔ 위치 추적 우려).
 *
 * Q: "권한 없이 connectGatt 호출하면?"
 * A: SecurityException 발생 → try/catch 또는 사전 체크 필수.
 */
object PermissionsHelper {

    fun requiredPermissions(): Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

    fun hasAll(context: Context): Boolean = requiredPermissions().all { p ->
        ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
    }
}
