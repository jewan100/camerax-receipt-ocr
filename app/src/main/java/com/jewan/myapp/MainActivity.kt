package com.jewan.myapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

// MainActivity: 앱 실행 시 가장 먼저 실행되는 Activity (Jetpack Compose 기반)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Activity가 처음 생성될 때 호출되는 메서드
        super.onCreate(savedInstanceState)

        // 카메라 권한 요청을 위한 런처 등록
        val permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { grant ->
                // 사용자가 권한을 허용(grant=true)했는지 거부(false)했는지 콜백으로 전달
                setContent {
                    if (grant) {
                        // 권한이 허용된 경우 → 카메라 프리뷰 화면 표시
                        CameraPreviewScreen()
                    } else {
                        // 권한이 거부된 경우 → 안내 문구 표시
                        Text("카메라 권한이 필요합니다.")
                    }
                }
            }

        // 현재 카메라 권한 상태를 확인
        if (ContextCompat.checkSelfPermission(
                this, // 현재 Activity의 Context
                Manifest.permission.CAMERA  // 요청할 권한: CAMERA
            ) == PackageManager.PERMISSION_GRANTED // 이미 허용된 상태인지 검사
        ) {
            // 권한이 이미 허용되어 있으면 바로 프리뷰 표시
            setContent { CameraPreviewScreen() }
        } else {
            // 권한이 없으면 런처를 통해 사용자에게 권한 요청
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}