package com.jewan.myapp.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ImageViewModel : ViewModel() {
    val capturedBitmap = mutableStateOf<Bitmap?>(null)
    val imageUri = mutableStateOf<Uri?>(null)

    fun setBitmap(bitmap: Bitmap?) {
        capturedBitmap.value = bitmap
    }

    fun setImageUri(uri: Uri?) {
        imageUri.value = uri
    }

    fun clear() {
        capturedBitmap.value = null
        imageUri.value = null
    }
}
