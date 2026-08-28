package com.personal.thesystem

import android.graphics.Color
import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.personal.thesystem.data.SystemRepository
import com.personal.thesystem.notifications.ReminderScheduler
import com.personal.thesystem.ui.SystemApp
import com.personal.thesystem.ui.theme.TheSystemTheme

class MainActivity : ComponentActivity() {
    private lateinit var repository: SystemRepository
    private var destination by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        repository = SystemRepository(applicationContext)
        destination = intent.getStringExtra(EXTRA_DESTINATION)
        ReminderScheduler.scheduleAll(this, repository.settings)
        setContent {
            TheSystemTheme {
                SystemApp(remember { repository }, destination) { destination = null }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::repository.isInitialized) {
            repository.reload()
            ReminderScheduler.scheduleAll(this, repository.settings)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        destination = intent.getStringExtra(EXTRA_DESTINATION)
    }

    companion object {
        const val EXTRA_DESTINATION = "destination"
    }

}
