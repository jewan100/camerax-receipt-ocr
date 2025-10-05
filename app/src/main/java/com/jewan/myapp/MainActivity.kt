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

@Composable
fun CameraPreviewScreen() {

    val context = LocalContext.current

    val imageAnalyzer = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also {
            it.setAnalyzer(Executors.newSingleThreadExecutor(), ReceiptAnalyzer(context))
        }

    // AndroidView: 기존 Android View(PreviewView)를 Compose 환경에서 사용하도록 감싸는 래퍼
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx -> // AndroidView가 실제 View를 생성할 때 실행되는 블록
            val previewView = PreviewView(ctx) // CameraX의 프리뷰를 띄우기 위한 뷰 생성

            // 카메라 프로바이더 가져오기 (CameraX 핵심 엔트리 포인트)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            // getInstance()는 비동기이므로 리스너 등록이 필요
            cameraProviderFuture.addListener({
                // future가 완료되면 CameraProvider 객체를 가져옴
                val cameraProvider = cameraProviderFuture.get()

                // Preview UseCase 생성 (카메라 영상을 화면에 표시)
                val preview = Preview.Builder().build().also {
                    // PreviewView의 SurfaceProvider를 연결해 실제 영상이 화면에 표시되도록 함
                    // it은 .also { ... } 블록 안에서 그 객체 자신(여기서는 Preview) 을 가리키는 축약 변수
                    it.surfaceProvider = previewView.surfaceProvider
                }

                // 어떤 카메라를 사용할지 선택 (후면 카메라)
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    // 기존에 바인딩된 UseCase가 있다면 모두 해제
                    cameraProvider.unbindAll()

                    // 카메라를 Lifecycle에 바인딩 (Activity 생명주기와 동기화)
                    cameraProvider.bindToLifecycle(
                        ctx as ComponentActivity,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx)) // 메인 스레드에서 리스너 실행
            // AndroidView에서 반환할 실제 뷰 (PreviewView)
            previewView
        }) { }
}