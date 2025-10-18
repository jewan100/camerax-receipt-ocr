package com.jewan.myapp.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jewan.myapp.ui.main.MainScreen
import com.jewan.myapp.ui.camera.CameraPreviewScreen
import com.jewan.myapp.ui.preview.ImagePreviewScreen
import com.jewan.myapp.viewmodel.ImageViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {

    val viewModel: ImageViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "main" // ✅ 변경됨
    ) {
        // ✅ 메인 선택 화면
        composable("main") {
            MainScreen(navController)
        }

        // 카메라 화면
        composable("camera") {
            CameraPreviewScreen(navController, viewModel)
        }

        // 드로잉 화면
        composable("draw") {
            // DrawCanvasScreen()
        }

        // 촬영한 사진 미리보기 화면
        composable("preview") {
            ImagePreviewScreen(navController, viewModel)
        }
    }
}