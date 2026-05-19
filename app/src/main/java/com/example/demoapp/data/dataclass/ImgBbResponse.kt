package com.example.demoapp.data.dataclass

import com.google.gson.annotations.SerializedName

data class ImgBbResponse(
    val data: ImgBbData,
    val success: Boolean,
    val status: Int
)

data class ImgBbData(
    val id: String,
    val title: String,
    val url: String, // Esta es la URL directa de la imagen (.jpg/.png)
    @SerializedName("display_url")
    val displayUrl: String
)

