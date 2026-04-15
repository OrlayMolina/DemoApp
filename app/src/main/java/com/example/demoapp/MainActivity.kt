package com.example.demoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.demoapp.core.navigation.AppNavigation
import com.example.demoapp.ui.theme.DemoAppTheme
import com.example.demoapp.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val darkModeEnabled by themeViewModel.darkModeEnabled.collectAsStateWithLifecycle()

            DemoAppTheme(darkTheme = darkModeEnabled) {
                AppNavigation()
            }
        }
    }
}