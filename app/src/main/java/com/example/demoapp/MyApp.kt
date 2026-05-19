package com.example.demoapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.demoapp.core.notifications.NotificationChannelIds
import com.example.demoapp.core.utils.EmbeddingSeeder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application() {

    @Inject lateinit var embeddingSeeder: EmbeddingSeeder

    override fun onCreate() {
        super.onCreate()
        embeddingSeeder.start()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            NotificationChannelIds.DEFAULT,
            getString(R.string.fcm_channel_default_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.fcm_channel_default_description)
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}
