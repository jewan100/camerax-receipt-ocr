package com.jewan.myapp

import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class ReceiptAnalyzer(private val context: Context) : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
    }
}