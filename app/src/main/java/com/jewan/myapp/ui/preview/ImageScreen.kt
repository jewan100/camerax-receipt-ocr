package com.jewan.myapp.ui.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jewan.myapp.viewmodel.ImageViewModel

@Composable
fun ImagePreviewScreen(
    navController: NavController,
    viewModel: ImageViewModel
) {
    val capturedBitmap = viewModel.capturedBitmap

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        capturedBitmap.value?.let { bitmap ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            )
            {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .height(400.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Button(onClick = { /* Info 기능 */ }) { Text("Info") }
                    Button(onClick = { /* Classify 기능 */ }) { Text("Classify") }
                    Button(onClick = { /* OCR 기능 */ }) { Text("OCR") }
                }
            }
        } ?: Text("이미지를 불러올 수 없습니다.", modifier = Modifier.align(Alignment.Center))
    }
}