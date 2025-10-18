package com.jewan.myapp.ui.gallery

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jewan.myapp.viewmodel.ImageViewModel

@Composable
fun GalleryScreen(
    navController: NavController,
    viewModel: ImageViewModel
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // 갤러리 열기 런처
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            selectedImageUri = uri
            uri?.let {
                viewModel.setImageUri(it)
                navController.navigate("preview") // 선택 후 미리보기로 이동
            }
        }
    }

    // 갤러리 열기 버튼
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = "image/*"
                }
                galleryLauncher.launch(intent)
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("앨범에서 이미지 선택하기")
        }
    }
}
