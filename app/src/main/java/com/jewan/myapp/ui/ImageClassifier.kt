package com.jewan.myapp.ui

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

/**
 * TensorFlow Lite 기반 이미지 분류기
 * - MobileNet 모델 사용
 * - 입력 크기: 224x224x3
 */
class ImageClassifier(private val context: Context) {

    // TensorFlow Lite 인터프리터 초기화
    private val tflite: Interpreter by lazy {
        val model = FileUtil.loadMappedFile(context, "ml/mobilenet_imagenet.tflite")
        val options = Interpreter.Options().apply { setNumThreads(2) } // 병렬 스레드 2개
        val interpreter = Interpreter(model, options)

        // 🧩 모델의 입출력 텐서 정보 출력
        val inputTensor = interpreter.getInputTensor(0)
        val outputTensor = interpreter.getOutputTensor(0)

        println(
            """
            🟢 Input Tensor
                name:  ${inputTensor.name()}
                shape: ${inputTensor.shape().contentToString()}
                type:  ${inputTensor.dataType()}
            🔵 Output Tensor
                name:  ${outputTensor.name()}
                shape: ${outputTensor.shape().contentToString()}
                type:  ${outputTensor.dataType()}
            """.trimIndent()
        )

        interpreter
    }

    // 🏷️ 라벨 파일 로드 (assets/label/label.txt)
    private val labels: List<String> by lazy {
        FileUtil.loadLabels(context, "label/label.txt")
    }

    /**
     * 이미지를 분류하여 (클래스 인덱스, 신뢰도) 반환
     */
    fun classify(bitmap: Bitmap): Pair<String, Float> {
        // 1️⃣ 이미지 전처리
        val inputImage = loadImage(bitmap)

        // 2️⃣ 출력 버퍼 자동 생성
        val outputShape = tflite.getOutputTensor(0).shape()
        val outputType = tflite.getOutputTensor(0).dataType()
        val outputBuffer = TensorBuffer.createFixedSize(outputShape, outputType)

        // 3️⃣ 추론 실행
        tflite.run(inputImage.buffer, outputBuffer.buffer.rewind())

        // 4️⃣ TensorLabel로 라벨 매핑
        val tensorLabel = TensorLabel(labels, outputBuffer)
        val labeledProbability = tensorLabel.mapWithFloatValue // Map<String, Float>

        // 5️⃣ 신뢰도 기준으로 정렬
        val topResult = labeledProbability.maxByOrNull { it.value }
        val label = topResult?.key ?: "Unknown"
        val confidence = topResult?.value ?: 0f

        return Pair(label, confidence)
    }

    /**
     * 이미지 전처리 수행 (리사이즈 + 정규화)
     */
    private fun loadImage(bitmap: Bitmap): TensorImage {
        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)

        // 전처리 파이프라인 구성
        val imageProcessor = ImageProcessor.Builder()
            // 입력 크기를 모델 크기에 맞게 조정
            // .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR)) 양선형 보간법
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR)) // 최근접 보간법
            // 정규화: (x - mean) / std
            // MobileNet의 경우 일반적으로 [0,1] 스케일을 사용하거나 mean=127.5, std=127.5 사용
            .add(NormalizeOp(0f, 255f))
            .build()

        tensorImage = imageProcessor.process(tensorImage)
        return tensorImage
    }

    fun close() {
        tflite.close()
    }

}
