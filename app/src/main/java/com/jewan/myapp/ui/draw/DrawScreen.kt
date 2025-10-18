package com.jewan.myapp.ui.draw

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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun DrawScreen() {
    // ✏️ Path는 상태로 관리 (변경 시 Canvas 다시 그림)
    var path by remember { mutableStateOf(Path()) }

    // 🎯 인식 결과 텍스트
    var resultText by remember { mutableStateOf("결과 없음") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEEE)), // 상위 배경은 밝게
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // ✅ BoxWithConstraints: 실제 너비(maxWidth) 기반으로 정사각형 크기 결정
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val canvasSize = maxWidth // ✅ 정사각형 한 변의 길이 (1:1 비율)

            Box(
                modifier = Modifier
                    .size(canvasSize)
                    .background(Color.Black) // 검은색 = 실제 드로잉 가능 영역
                    .border(2.dp, Color.DarkGray), // 시각적 확인용 테두리
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds() // ✅ 경계 밖 드로잉 차단
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    path.moveTo(offset.x, offset.y)
                                },
                                onDrag = { change, _ ->
                                    // Path 객체를 새로 만들어 Compose가 감지하도록 함
                                    val newPath = Path().apply {
                                        addPath(path)
                                        lineTo(change.position.x, change.position.y)
                                    }
                                    path = newPath
                                }
                            )
                        }
                ) {
                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(width = 100f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ⚙️ 하단 버튼 영역
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEEEEEE))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = {
                    resultText = "5, 93%" // TODO: 나중에 TFLite 모델 연결
                }) {
                    Text("CLASSIFY")
                }

                Button(onClick = {
                    path = Path() // 초기화
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
