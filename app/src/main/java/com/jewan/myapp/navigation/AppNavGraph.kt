package com.jewan.myapp.navigation

import androidx.compose.runtime.Composable

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jewan.myapp.ui.camera.CameraPreviewScreen
import com.jewan.myapp.ui.preview.ImagePreviewScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "camera"
    ) {
        // 카메라 화면
        composable("camera") {
            CameraPreviewScreen(navController)
        }

        // 촬영한 사진 미리보기 화면
        composable("preview") {
            ImagePreviewScreen(navController)
        }
    }
}