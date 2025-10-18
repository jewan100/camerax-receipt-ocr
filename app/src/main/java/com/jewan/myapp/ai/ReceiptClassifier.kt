package com.jewan.myapp.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.core.graphics.scale
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

/**
 *  ReceiptClassifier
 * - 실시간 Analyzer 대신, 사용자가 촬영한 1장의 이미지를 분류하는 클래스
 * - 반환값: Boolean (true = 영수증 / false = 비영수증)
 */
class ReceiptClassifier(private val context: Context) {

    private val tflite: Interpreter by lazy {
        val model = FileUtil.loadMappedFile(context, "ml/receipt_detector.tflite")
        val options = Interpreter.Options().apply { setNumThreads(2) }
        Interpreter(model, options) // 여기서 생성된 Interpreter 객체가 반환(return)됨
        /**
         * return 키워드는 없음
         * 하지만 lazy 블록의 마지막 줄이 return 값이 됨
         * 즉, tflite에 Interpreter(model, options) 객체가 저장되는 것
         */
    }

    /**
     *  Bitmap 이미지 한 장을 영수증 분류
     * @param bitmap 입력 이미지
     * @param rotationDegrees 회전 각도 (CameraX 촬영 시 imageInfo.rotationDegrees)
     * @return Float 확률값과 Boolean 분류 결과
     */
    fun isReceipt(bitmap: Bitmap, rotationDegrees: Int = 0): Pair<Float, Boolean> {
        return try {
            // 회전 보정
            val rotated = rotateBitmap(bitmap, rotationDegrees)

            // 모델 입력 크기로 리사이즈 (224x224)
            val resized = rotated.scale(224, 224)

            // TensorImage로 변환
            val tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(resized)

            // 출력 버퍼 준비
            val output = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)

            // 추론 실행
            tflite.run(tensorImage.buffer, output.buffer.rewind())

            // 결과 해석
            val probability = output.floatArray[0]
            val isReceipt = probability > 0.5f

            Log.d(
                "ReceiptClassifier",
                "📷 확률: $probability → ${if (isReceipt) "✅ 영수증" else "❌ 비영수증"}"
            )

            Pair(probability, isReceipt)

        } catch (e: Exception) {
            Log.e("ReceiptClassifier", "분석 중 오류 발생: ${e.message}")
            Pair(0f, false)
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return bitmap
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}