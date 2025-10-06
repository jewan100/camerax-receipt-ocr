package com.jewan.myapp

import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun CameraPreviewScreen() {

    // 현재 컴포저블이 실행 중인 Context를 가져옴
    // → CameraProvider, PreviewView 생성 등에 필요
    val context = LocalContext.current

    // Box 레이아웃: 카메라 프리뷰와 버튼을 겹쳐서 배치하기 위해 사용
    Box(
        modifier = Modifier.fillMaxSize() // Box 전체를 화면 크기로 확장
    ) {
        // AndroidView: 기존 안드로이드 View를 Compose 환경에서 표시하기 위한 래퍼
        AndroidView(
            modifier = Modifier.fillMaxSize(), // 프리뷰를 화면 전체에 표시
            factory = { ctx -> // AndroidView가 실제 PreviewView를 생성할 때 실행되는 블록
                val previewView = PreviewView(ctx) // CameraX의 실제 카메라 미리보기를 표시할 View 생성

                // CameraProvider 가져오기 (CameraX의 핵심 클래스)
                // → UseCase(Preview, ImageCapture 등)를 실제 카메라 하드웨어에 바인딩하는 역할
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                // getInstance()는 비동기로 작동하므로, 완료 리스너를 등록해야 함
                cameraProviderFuture.addListener({
                    // future가 완료되면 CameraProvider 객체를 가져옴
                    val cameraProvider = cameraProviderFuture.get()

                    // Preview UseCase 생성 (카메라 화면을 미리보기로 표시하기 위한 역할)
                    val preview = Preview.Builder().build().also {
                        // PreviewView의 SurfaceProvider를 연결해 실제 영상이 화면에 나타나도록 설정
                        // → Preview가 이 Surface를 통해 카메라 영상 데이터를 PreviewView에 렌더링함
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    // 어떤 카메라를 사용할지 선택 (후면 카메라)
                    // → 전면 카메라를 쓰려면 CameraSelector.DEFAULT_FRONT_CAMERA
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        // 이미 바인딩된 UseCase가 있다면 모두 해제 (중복 방지)
                        cameraProvider.unbindAll()

                        // 카메라를 Lifecycle에 바인딩
                        // - 첫 번째 인자: 현재 액티비티의 Lifecycle
                        // - 두 번째 인자: 어떤 카메라를 쓸지 (후면)
                        // - 세 번째 인자: 어떤 UseCase를 바인딩할지 (Preview)
                        // => 이렇게 하면 Activity의 생명주기에 맞춰 카메라가 자동으로 on/off됨
                        cameraProvider.bindToLifecycle(
                            ctx as ComponentActivity,
                            cameraSelector,
                            preview
                        )
                    } catch (e: Exception) {
                        // 카메라 바인딩 중 에러 발생 시 로그 출력
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(ctx)) // 메인 스레드에서 리스너 실행

                // factory 블록의 마지막에 반환된 View가 AndroidView로 표시됨
                // 여기서는 CameraX의 PreviewView를 화면에 표시
                previewView
            }
        )

        // 플로팅 액션 버튼 (화면 하단 중앙에 위치)
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomCenter) // Box 기준으로 하단 중앙에 정렬
                .padding(bottom = 32.dp),      // 화면 하단과의 간격 설정
            containerColor = Color(0xFF3DDC84), // 안드로이드 공식 그린 색상 (#3DDC84)
            contentColor = Color.White,         // 아이콘 색상 (흰색)
            onClick = { /*TODO: 촬영 버튼 클릭 시 실행될 로직*/ }
        ) {
            // 플로팅 버튼 내부의 아이콘
            Icon(
                imageVector = Icons.Default.AddCircle, // 기본 Material 아이콘 중 'AddCircle' 사용
                contentDescription = "촬영 버튼"       // 접근성(스크린리더)을 위한 설명
            )
        }
    }
}
