package com.jewan.myapp

import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

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