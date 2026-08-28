package com.personal.thesystem.notifications

import android.app.SearchManager
import android.Manifest
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.personal.thesystem.R
import com.personal.thesystem.data.SystemRepository
import com.personal.thesystem.model.SystemLogic
import java.time.LocalDate

class MorningMusicActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings = SystemRepository(this).settings
        val manualTest = intent.getBooleanExtra("manual_test", false)
        if (!settings.morningMusicEnabled && !manualTest) {
            ReminderScheduler.cancelMusic(this)
            finish()
            return
        }
        if (!manualTest) ReminderScheduler.scheduleMusic(this)

        val artist = SystemLogic.morningArtistFor(LocalDate.now().dayOfWeek)
        val playIntent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
            setPackage(YANDEX_MUSIC_PACKAGE)
            putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)
            putExtra(MediaStore.EXTRA_MEDIA_ARTIST, artist)
            putExtra(SearchManager.QUERY, artist)
        }
        val destination = if (playIntent.resolveActivity(packageManager) != null) {
            playIntent
        } else {
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://music.yandex.ru/search?text=${Uri.encode(artist)}"),
            )
        }

        runCatching { startActivity(destination) }
            .onSuccess { ReminderScheduler.markMusicLaunch(this, LocalDate.now()) }
        finish()
    }

    private companion object {
        const val YANDEX_MUSIC_PACKAGE = "ru.yandex.music"
    }
}

class MorningMusicWatchdogReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val settings = SystemRepository(context).settings
        if (!settings.morningMusicEnabled) {
            ReminderScheduler.cancelMusic(context)
            return
        }

        if (ReminderScheduler.wasMusicLaunched(context, LocalDate.now())) {
            ReminderScheduler.scheduleMusic(context)
            return
        }

        ReminderScheduler.scheduleMusic(context)
        ReminderScheduler.createChannel(context)
        val artist = SystemLogic.morningArtistFor(LocalDate.now().dayOfWeek)
        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_MUSIC)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Музыка на утро · $artist")
            .setContentText("Если музыка ещё не играет, нажми — открою исполнителя дня.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Если музыка ещё не играет, нажми — открою исполнителя дня."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(ReminderScheduler.musicPendingIntent(context))
            .build()
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (permissionGranted) NotificationManagerCompat.from(context).notify(408, notification)
    }
}
