package com.personal.thesystem

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.personal.thesystem.data.SystemRepository
import com.personal.thesystem.notifications.ReminderScheduler
import com.personal.thesystem.ui.SystemApp
import com.personal.thesystem.ui.theme.TheSystemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        ReminderScheduler.createChannel(this)
        setContent {
            TheSystemTheme {
                val repository = remember { SystemRepository(applicationContext) }
                SystemApp(repository)
            }
        }
    }
}
