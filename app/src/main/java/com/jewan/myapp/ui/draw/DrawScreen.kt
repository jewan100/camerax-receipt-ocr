package com.jewan.myapp.ui.draw

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jewan.myapp.viewmodel.DrawViewModel

@Composable
fun DrawScreen() {
    val viewModel: DrawViewModel = viewModel()

    var path by remember { mutableStateOf(Path()) }
    var resultText by remember { mutableStateOf("결과 없음") }

    // 🖼️ 사용자가 그린 내용을 Bitmap으로 저장
    var latestBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEEE)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val canvasSize = maxWidth

            Box(
                modifier = Modifier
                    .size(canvasSize)
                    .background(Color.Black)
                    .border(2.dp, Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    path.moveTo(offset.x, offset.y)
                                },
                                onDrag = { change, _ ->
                                    val newPath = Path().apply {
                                        addPath(path)
                                        lineTo(change.position.x, change.position.y)
                                    }
                                    path = newPath
                                }
                            )
                        }
                ) {
                    // Path를 그리기
                    drawPath(path = path, color = Color.White, style = Stroke(width = 60f))

                    // Canvas 내용을 Bitmap으로 변환 (최신 상태 저장)
                    val bitmap = createBitmap(size.width.toInt(), size.height.toInt())
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.BLACK)
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 60f
                        isAntiAlias = true
                    }
                    canvas.drawPath(path.asAndroidPath(), paint)
                    latestBitmap = bitmap
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ⚙️ 하단 버튼
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEEEEEE))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = {
                    latestBitmap?.let {
                        val (digit, confidence) = viewModel.classify(it)
                        resultText = "예측: $digit (정확도 ${(confidence * 100).toInt()}%)"
                    } ?: run {
                        resultText = "그림이 없습니다."
                    }
                }) {
                    Text("CLASSIFY")
                }

                Button(onClick = {
                    path = Path()
                    resultText = "결과 없음"
                }) {
                    Text("CLEAR")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = resultText, color = Color.Black)
        }
    }
}
