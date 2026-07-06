package com.yiqiu.readingquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.yiqiu.readingquiz.ui.app.ReadingAppShell
import com.yiqiu.readingquiz.ui.theme.CafeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            CafeTheme {
                ReadingAppShell()
            }
        }
    }
}