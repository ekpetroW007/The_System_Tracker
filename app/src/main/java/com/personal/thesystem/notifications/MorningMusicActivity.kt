package com.personal.thesystem.notifications

import android.Manifest
import android.app.ActivityOptions
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.personal.thesystem.R
import com.personal.thesystem.data.SystemRepository

internal const val CUTOFF_MUSIC_VOLUME_PERCENT = 50
internal const val CUTOFF_MUSIC_ARTIST = "FRIENDLY THUG 52 NGG"
internal const val CUTOFF_MUSIC_TITLE = "Sladki Snov Rapper 2"

internal fun mediaVolumeFor(maxVolume: Int, percent: Int): Int =
    (maxVolume.coerceAtLeast(0) * percent.coerceIn(0, 100) + 50) / 100

internal fun yandexAutoPlayUrl(url: String): String = when {
    url.contains(Regex("[?&]play=true(?:&|$)")) -> url
    '?' in url -> "$url&play=true"
    else -> "$url?play=true"
}

private fun dispatchMediaKey(context: Context, keyCode: Int) {
    val audio = context.getSystemService(AudioManager::class.java)
    val now = SystemClock.uptimeMillis()
    audio.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0))
    audio.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0))
}

internal object YandexMusicLauncher {
    private const val YANDEX_MUSIC_PACKAGE = "ru.yandex.music"

    fun launch(context: Context, url: String): Boolean {
        val finalUrl = yandexAutoPlayUrl(url)
        val launchIntent = viewIntent(finalUrl)
        if (launchIntent.resolveActivity(context.packageManager) == null) {
            ReminderScheduler.recordMusicDiagnostic(context, "Яндекс Музыка не установлена")
            return false
        }

        val pendingIntent = activityPendingIntent(context, launchIntent, finalUrl.hashCode() and Int.MAX_VALUE)
        val launched = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                pendingIntent.send(context, 0, null, null, null, null, senderOptions())
            } else {
                pendingIntent.send()
            }
        }.onFailure {
            ReminderScheduler.recordMusicDiagnostic(context, "Ошибка открытия Яндекс Музыки: ${it.javaClass.simpleName}")
        }.isSuccess

        if (launched) {
            runCatching {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, MusicKickService::class.java).putExtra(MusicKickService.EXTRA_URL, finalUrl),
                )
            }.onFailure {
                ReminderScheduler.recordMusicDiagnostic(context, "Плейлист открыт, команда PLAY не запущена")
            }
        }
        return launched
    }

    fun retryPendingIntent(context: Context, url: String): PendingIntent =
        activityPendingIntent(context, viewIntent(yandexAutoPlayUrl(url)), (url.hashCode() + 31) and Int.MAX_VALUE)

    private fun viewIntent(url: String): Intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        setPackage(YANDEX_MUSIC_PACKAGE)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    private fun activityPendingIntent(context: Context, intent: Intent, requestCode: Int): PendingIntent {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            PendingIntent.getActivity(context, requestCode, intent, flags, creatorOptions())
        } else {
            PendingIntent.getActivity(context, requestCode, intent, flags)
        }
    }

    @Suppress("DEPRECATION")
    private fun backgroundStartMode(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_ALWAYS
        } else {
            ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
        }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun creatorOptions(): Bundle = ActivityOptions.makeBasic().apply {
        pendingIntentCreatorBackgroundActivityStartMode = backgroundStartMode()
    }.toBundle()

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun senderOptions(): Bundle = ActivityOptions.makeBasic().apply {
        pendingIntentBackgroundActivityStartMode = backgroundStartMode()
    }.toBundle()
}

class MusicAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val isCutoff = intent?.action == ACTION_PLAY_CUTOFF ||
            intent?.getStringExtra(LEGACY_EXTRA_MODE) == LEGACY_MODE_CUTOFF
        if (!isCutoff) return

        val settings = SystemRepository(context).settings
        if (!settings.cutoffEnabled) {
            ReminderScheduler.cancelCutoffMusic(context)
            return
        }
        ReminderScheduler.scheduleCutoffMusic(context, settings.digitalCutoff)
        CutoffMusicService.start(context)
    }

    companion object {
        const val ACTION_PLAY_CUTOFF = "com.personal.thesystem.action.PLAY_CUTOFF"
        private const val LEGACY_EXTRA_MODE = "music_mode"
        private const val LEGACY_MODE_CUTOFF = "cutoff"
    }
}

class CutoffMusicService : Service() {
    private var player: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopPlayback()
            return START_NOT_STICKY
        }

        ReminderScheduler.createChannel(this)
        val stopIntent = PendingIntent.getService(
            this,
            STOP_REQUEST_CODE,
            Intent(this, CutoffMusicService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, ReminderScheduler.CHANNEL_MUSIC_ENGINE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Цифровой отбой")
            .setContentText("$CUTOFF_MUSIC_ARTIST · $CUTOFF_MUSIC_TITLE")
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setOngoing(true)
            .setSilent(true)
            .addAction(R.drawable.ic_notification, "ВЫКЛЮЧИТЬ", stopIntent)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        runCatching { startTrack() }.onFailure {
            ReminderScheduler.recordMusicDiagnostic(this, "Ошибка локального трека: ${it.javaClass.simpleName}")
            stopPlayback()
        }
        return START_NOT_STICKY
    }

    private fun startTrack() {
        player?.release()
        player = null

        val audio = getSystemService(AudioManager::class.java)
        audio.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            mediaVolumeFor(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC), CUTOFF_MUSIC_VOLUME_PERCENT),
            0,
        )
        dispatchMediaKey(this, KeyEvent.KEYCODE_MEDIA_PAUSE)

        val nextPlayer = MediaPlayer()
        player = nextPlayer
        try {
            resources.openRawResourceFd(R.raw.sladki_snov_rapper_2).use { descriptor ->
                nextPlayer.apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build(),
                    )
                    setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                    setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                    setOnCompletionListener { stopPlayback() }
                    setOnErrorListener { _, what, extra ->
                        ReminderScheduler.recordMusicDiagnostic(this@CutoffMusicService, "Ошибка аудио $what/$extra")
                        stopPlayback()
                        true
                    }
                    prepare()
                    start()
                }
            }
        } catch (error: Exception) {
            nextPlayer.release()
            if (player === nextPlayer) player = null
            throw error
        }
        ReminderScheduler.recordMusicDiagnostic(this, "Локальный $CUTOFF_MUSIC_TITLE запущен")
    }

    private fun stopPlayback() {
        player?.release()
        player = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val ACTION_STOP = "com.personal.thesystem.action.STOP_CUTOFF"
        private const val NOTIFICATION_ID = 420
        private const val STOP_REQUEST_CODE = 421

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, CutoffMusicService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, CutoffMusicService::class.java))
        }
    }
}

class MusicKickService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ReminderScheduler.createChannel(this)
        val notification = NotificationCompat.Builder(this, ReminderScheduler.CHANNEL_MUSIC_ENGINE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Запускаю плейлист")
            .setOngoing(true)
            .setSilent(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(418, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE)
        } else {
            startForeground(418, notification)
        }

        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TheSystem:music-start")
            .apply { acquire(8_000L) }

        val url = intent?.getStringExtra(EXTRA_URL).orEmpty()
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed(::dispatchPlay, 1_200L)
        handler.postDelayed(::dispatchPlay, 2_800L)
        handler.postDelayed({ finishCheck(url) }, 5_000L)
        return START_NOT_STICKY
    }

    private fun dispatchPlay() {
        dispatchMediaKey(this, KeyEvent.KEYCODE_MEDIA_PLAY)
    }

    private fun finishCheck(url: String) {
        val active = getSystemService(AudioManager::class.java).isMusicActive
        ReminderScheduler.recordMusicDiagnostic(
            this,
            if (active) "Воспроизведение подтверждено" else "Плейлист открыт, но аудиопоток не появился",
        )
        if (!active) showFailureNotification(url)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun showFailureNotification(url: String) {
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted || url.isBlank()) return
        val notification = NotificationCompat.Builder(this, ReminderScheduler.CHANNEL_MUSIC)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Плейлист не начал воспроизведение")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(YandexMusicLauncher.retryPendingIntent(this, url))
            .build()
        NotificationManagerCompat.from(this).notify(419, notification)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_URL = "music_url"
    }
}
