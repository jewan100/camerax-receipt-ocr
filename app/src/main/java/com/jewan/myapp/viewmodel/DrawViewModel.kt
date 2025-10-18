package com.jewan.myapp.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import androidx.core.graphics.scale
import androidx.core.graphics.get

class DrawViewModel(application: Application) : AndroidViewModel(application) {

    private val tflite: Interpreter by lazy {
        val model = FileUtil.loadMappedFile(application, "ml/keras_model_cnn.tflite")
        Interpreter(model, Interpreter.Options().apply { setNumThreads(2) })
    }

    fun classify(bitmap: Bitmap): Pair<Int, Float> {
        // 1️⃣ 입력 리사이즈 (28x28)
        // 보간법 : 최근접
        val resized = bitmap.scale(28, 28, false)

        // 2️⃣ (1,28,28) float buffer 생성
        val inputShape = tflite.getInputTensor(0).shape() // [1,28,28]
        val inputBuffer =
            ByteBuffer.allocateDirect(28 * 28 * 4).order(ByteOrder.nativeOrder())

        // 3️⃣ Bitmap → grayscale float (0~1 정규화)
        for (y in 0 until 28) {
            for (x in 0 until 28) {
                val pixel = resized[x, y]
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                val gray = (0.299 * r + 0.587 * g + 0.114 * b).toFloat() / 255f
                inputBuffer.putFloat(gray)
            }
        }

        // 4️⃣ 출력 버퍼 준비
        val outputBuffer =
            TensorBuffer.createFixedSize(intArrayOf(1, 10), DataType.FLOAT32)

        // 5️⃣ 모델 추론 실행 (직접 ByteBuffer 전달)
        tflite.run(inputBuffer.rewind(), outputBuffer.buffer.rewind())

        // 6️⃣ 결과 처리
        val probs = outputBuffer.floatArray
        val maxIdx = probs.indices.maxByOrNull { probs[it] } ?: -1
        val conf = probs[maxIdx]

        return Pair(maxIdx, conf)
    }

    override fun onCleared() {
        super.onCleared()
        tflite.close()
    }
}
