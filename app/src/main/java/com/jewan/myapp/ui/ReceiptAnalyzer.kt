package com.jewan.myapp.ui

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.graphics.scale
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class ReceiptAnalyzer(private val context: Context) : ImageAnalysis.Analyzer {

    /**
     * TensorFlow Lite Interpreter 초기화
     * - tflite 모델 파일을 assets/ml/receipt_detector.tflite 에서 읽어옴
     * - 2개의 스레드로 병렬 추론 수행
     */
    private val tflite: Interpreter by lazy {
        // 모델 파일을 메모리에 매핑 (파일을 전부 로드하지 않고 빠르게 접근 가능)
        val model = FileUtil.loadMappedFile(context, "ml/receipt_detector.tflite")

        // TFLite 옵션 설정: 스레드 개수 2개로 병렬 처리
        val options = Interpreter.Options().apply { setNumThreads(2) }

        // Interpreter 생성 (모델과 옵션 전달)
        Interpreter(model, options)
    }

    private var lastAnalyzedTime = 0L // 최근 프레임 분석 시각을 기록
    private val analyzeInterval = 500L // 0.5초마다 분석을 수행

    /**
     * CameraX에서 매 프레임마다 호출되는 analyze() 메서드
     * - ImageProxy 객체를 입력받아, Bitmap으로 변환 → TFLite 모델 추론 → 결과 로그 출력
     */
    override fun analyze(image: ImageProxy) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastAnalyzedTime < analyzeInterval) {
            image.close()
            return
        }
        lastAnalyzedTime = currentTime

        try {

            // ImageProxy → Bitmap 변환
            val bitmap = image.toBitmap()

            // 모델 입력 크기로 리사이즈 (224x224)
            val resized = bitmap.scale(224, 224)

            // TensorImage로 래핑 (TFLite 지원 형식)
            val tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(resized)

            // 출력 버퍼 준비
            val output = TensorBuffer.createFixedSize(intArrayOf(1, 1), DataType.FLOAT32)

            /**
             * 모델 추론 실행
             * - 입력: tensorImage.buffer
             * - 출력: output.buffer (rewind()로 포인터를 처음으로 이동)
             */
            tflite.run(tensorImage.buffer, output.buffer.rewind())

            // 결과 해석
            val probability = output.floatArray[0]
            val isReceipt = probability > 0.5f

            Log.d("ReceiptAnalyzer", "📷 확률: $probability → ${if (isReceipt) "✅ 영수증" else "❌ 비영수증"}")

        } catch (e: Exception) {
            Log.e("ReceiptAnalyzer", "분석 중 오류 발생: ${e.message}")
        } finally {
            // ImageProxy는 사용 후 반드시 close() 호출해야 함
            image.close()
        }
    }
}