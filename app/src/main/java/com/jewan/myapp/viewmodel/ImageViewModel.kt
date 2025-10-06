package com.jewan.myapp.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ImageViewModel : ViewModel() {

    val capturedBitmap = mutableStateOf<Bitmap?>(null)

    fun setBitmap(bitmap: Bitmap) {
        capturedBitmap.value = bitmap
    }

    fun clearBitmap() {
        capturedBitmap.value = null
    }
}