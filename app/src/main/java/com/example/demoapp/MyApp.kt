package com.example.demoapp

import android.app.Application
import com.example.demoapp.core.utils.EmbeddingSeeder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application() {

    @Inject lateinit var embeddingSeeder: EmbeddingSeeder

    override fun onCreate() {
        super.onCreate()
        embeddingSeeder.start()
    }
}
