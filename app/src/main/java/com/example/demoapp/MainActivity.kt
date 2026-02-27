package com.example.demoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.demoapp.features.home.HomeScreen
import com.example.demoapp.features.login.LoginScreen
import com.example.demoapp.features.recovery.PasswordRecoveryScreen
import com.example.demoapp.features.register.RegisterScreen
import com.example.demoapp.features.reset.PasswordResetScreen
import com.example.demoapp.ui.theme.DemoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DemoAppTheme {
                PasswordRecoveryScreen()
            }
        }
    }
}