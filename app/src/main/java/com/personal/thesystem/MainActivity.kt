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
import com.yandex.mapkit.MapKitFactory

class MainActivity : ComponentActivity() {
    private lateinit var repository: SystemRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        repository = SystemRepository(applicationContext)
        ReminderScheduler.scheduleAll(this, repository.settings)
        setContent {
            TheSystemTheme {
                SystemApp(remember { repository })
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

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}
