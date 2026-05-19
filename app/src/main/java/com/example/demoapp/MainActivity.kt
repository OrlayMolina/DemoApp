package com.example.demoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.demoapp.core.navigation.AppNavigation
import com.example.demoapp.core.notifications.NotificationPushObserver
import com.example.demoapp.ui.theme.DemoAppTheme
import com.example.demoapp.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var notificationPushObserver: NotificationPushObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tocar la referencia para que Hilt cree el observer y arranque su listener.
        notificationPushObserver.toString()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val darkModeEnabled by themeViewModel.darkModeEnabled.collectAsStateWithLifecycle()

            DemoAppTheme(darkTheme = darkModeEnabled) {
                AppNavigation()
            }
        }
    }
}