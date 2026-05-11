package com.example.demoapp.core.utils

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ImageLabeler(private val context: Context) {

    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
    )

    suspend fun label(imageUri: Uri): List<String> = suspendCancellableCoroutine { cont ->
        try {
            val image = InputImage.fromFilePath(context, imageUri)
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    cont.resume(labels.take(5).map { it.text })
                }
                .addOnFailureListener {
                    cont.resume(emptyList())
                }
        } catch (e: Exception) {
            cont.resume(emptyList())
        }
    }
}
