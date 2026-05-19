package com.example.demoapp.data

import android.content.Context
import android.net.Uri
import com.example.demoapp.data.dataclass.ImgBbApiService
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ImageRepository {

    // Leer la API key desde BuildConfig (configurada en app/build.gradle.kts => IMGBB_API_KEY)
    private val apiKey: String = com.example.demoapp.BuildConfig.IMGBB_API_KEY

    suspend fun uploadImageToRemote(imageFile: File): String? {
        return withContext(Dispatchers.IO) {
            try {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                val response = RetrofitClient.apiService.uploadImage(apiKey, body)

                return@withContext if (response.isSuccessful && response.body() != null) {
                    val url = response.body()?.data?.url
                    Log.d("ImageRepository", "Imagen subida a ImgBB: $url")
                    url
                } else {
                    Log.w("ImageRepository", "Fallo al subir imagen, codigo: ${response.code()}")
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // Conveniencia: subir directamente desde un Uri (ej: seleccionado en Compose)
    suspend fun uploadImageFromUri(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val file = uriToFile(context, uri)
                uploadImageToRemote(file)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File.createTempFile("upload", "jpg", context.cacheDir)
        inputStream.use { input ->
            FileOutputStream(file).use { output ->
                input?.copyTo(output)
            }
        }
        return file
    }
}

// Cliente Retrofit interno para conectar la API
object RetrofitClient {
    private const val BASE_URL = "https://api.imgbb.com/"

    val apiService: ImgBbApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(ImgBbApiService::class.java)
    }
}