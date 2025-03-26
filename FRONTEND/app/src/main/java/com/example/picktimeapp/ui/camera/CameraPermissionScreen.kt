package com.example.picktimeapp.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun CameraPermissionScreen(
    onPermissionGranted: @Composable () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission = isGranted
            if (!isGranted) showRationale = true
        }
    )

    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                hasPermission = true
            }
            else -> {
                launcher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    when {
        hasPermission -> onPermissionGranted()
        showRationale -> Text("카메라 권한이 필요합니다. 설정 > 권한에서 허용해주세요.")
    }
}
