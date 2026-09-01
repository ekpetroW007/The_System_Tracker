@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.personal.thesystem.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.thesystem.notifications.YandexMusicLauncher
import com.personal.thesystem.ui.theme.Acid
import com.personal.thesystem.ui.theme.Danger
import com.personal.thesystem.ui.theme.Hairline
import com.personal.thesystem.ui.theme.Ink
import com.personal.thesystem.ui.theme.Muted
import com.personal.thesystem.ui.theme.Paper
import com.personal.thesystem.ui.theme.SurfaceRaised
import com.personal.thesystem.ui.theme.SurfaceSoft
import java.util.Locale
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sqrt

internal const val RUN_PLAYLIST_URL =
    "https://music.yandex.ru/playlists/2bff5ff7-5188-28d8-ad8c-c228d34d12b8?utm_medium=copy_link&ref_id=48c7d654-a965-4e8d-afae-8feef5c4c41b"
internal const val RUN_SPEED_KMH = 5.0
internal const val RUN_WEIGHT_KG = 93.0
private const val RUN_MET = 3.8

internal data class RunStats(
    val elapsedSeconds: Long,
    val countedSeconds: Long,
    val distanceKm: Double,
    val calories: Double,
)

internal fun calculateRunStats(elapsedSeconds: Long): RunStats {
    val safeElapsed = elapsedSeconds.coerceAtLeast(0L)
    val countedSeconds = safeElapsed
    val countedHours = countedSeconds / 3_600.0
    return RunStats(
        elapsedSeconds = safeElapsed,
        countedSeconds = countedSeconds,
        distanceKm = RUN_SPEED_KMH * countedHours,
        calories = RUN_MET * RUN_WEIGHT_KG * countedHours,
    )
}

internal fun formatRunTime(seconds: Long): String {
    val safeSeconds = seconds.coerceAtLeast(0L)
    val hours = safeSeconds / 3_600L
    val minutes = safeSeconds / 60L % 60L
    val remainder = safeSeconds % 60L
    return if (hours > 0L) {
        String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, remainder)
    } else {
        String.format(Locale.ROOT, "%02d:%02d", minutes, remainder)
    }
}

internal fun launchRunPlaylist(context: Context) {
    YandexMusicLauncher.launch(
        context = context,
        url = RUN_PLAYLIST_URL,
    )
}

internal class ShakeTrigger {
    private var firstPeakAt = 0L
    private var peakCount = 0
    private var lastTriggerAt: Long? = null

    fun register(gForce: Float, now: Long): Boolean {
        if (gForce < 2.7f) return false
        if (peakCount == 0 || now - firstPeakAt > 700L) {
            firstPeakAt = now
            peakCount = 1
            return false
        }
        peakCount = 0
        if (lastTriggerAt?.let { now - it < 2_000L } == true) return false
        lastTriggerAt = now
        return true
    }
}

@Composable
internal fun RunShakeEffect(enabled: Boolean, onShake: () -> Unit) {
    val context = LocalContext.current
    val currentOnShake by rememberUpdatedState(onShake)
    DisposableEffect(context, enabled) {
        if (!enabled) {
            onDispose {}
        } else {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (accelerometer == null) {
                onDispose {}
            } else {
                val trigger = ShakeTrigger()
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]
                        val gForce = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
                        if (trigger.register(gForce, SystemClock.elapsedRealtime())) currentOnShake()
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
                }
                sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
                onDispose { sensorManager.unregisterListener(listener) }
            }
        }
    }
}

@Composable
internal fun RunPromptSheet(onYes: () -> Unit, onNo: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onNo, containerColor = SurfaceRaised) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp).padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Бежим сегодня?", color = Paper, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "Запущу секундомер и плейлист «БЕГ». После открытия Яндекс Музыки вернись кнопкой «Назад» — секундомер уже будет идти.",
                color = Muted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RunChoiceButton("НЕТ", primary = false, modifier = Modifier.weight(1f), onClick = onNo)
                RunChoiceButton("ДА", primary = true, modifier = Modifier.weight(1f), onClick = onYes)
            }
        }
    }
}

@Composable
private fun RunChoiceButton(label: String, primary: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (primary) Acid else SurfaceSoft)
            .border(1.dp, if (primary) Acid else Hairline, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (primary) Ink else Paper, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
internal fun RunningScreen(
    startedAtRealtime: Long,
    motionAllowed: Boolean,
    onFinish: (Long) -> Unit,
) {
    var elapsedSeconds by remember(startedAtRealtime) {
        mutableLongStateOf(((SystemClock.elapsedRealtime() - startedAtRealtime) / 1_000L).coerceAtLeast(0L))
    }
    LaunchedEffect(startedAtRealtime) {
        while (true) {
            elapsedSeconds = ((SystemClock.elapsedRealtime() - startedAtRealtime) / 1_000L).coerceAtLeast(0L)
            delay(250L)
        }
    }
    BackHandler(enabled = true) {}
    val stats = remember(elapsedSeconds) { calculateRunStats(elapsedSeconds) }

    LazyColumn(
        Modifier.fillMaxSize().background(Ink).statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScreenHeader("Режим БЕГ", "Пробежка", "Скорость расчёта · 5 км/ч") }
        item {
            PremiumCard(color = SurfaceSoft, border = Acid.copy(alpha = .34f)) {
                Text("СЕКУНДОМЕР", color = Muted, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(7.dp))
                Text(formatRunTime(elapsedSeconds), color = Paper, style = MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(14.dp))
                RunnerAnimation(motionAllowed)
            }
        }
        item {
            RunStatsCard(stats.distanceKm, stats.calories)
        }
        item {
            HseActionButton("ЗАВЕРШИТЬ ПРОБЕЖКУ", color = Danger) {
                onFinish(((SystemClock.elapsedRealtime() - startedAtRealtime) / 1_000L).coerceAtLeast(0L))
            }
        }
    }
}

@Composable
private fun RunnerAnimation(motionAllowed: Boolean) {
    val transition = rememberInfiniteTransition(label = "runner")
    val travel = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2_600, easing = LinearEasing), RepeatMode.Restart),
        label = "runnerTravel",
    ).value
    val bob = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(260, easing = LinearEasing), RepeatMode.Reverse),
        label = "runnerBob",
    ).value
    BoxWithConstraints(
        Modifier.fillMaxWidth().height(112.dp).clip(RoundedCornerShape(18.dp)).background(Ink),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val y = size.height - 18.dp.toPx()
            drawLine(Hairline, start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
            drawLine(Acid.copy(alpha = .65f), start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(size.width * if (motionAllowed) travel else .5f, y), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
        }
        val x = (maxWidth - 66.dp).coerceAtLeast(0.dp) * if (motionAllowed) travel else .5f
        val y = if (motionAllowed) 7.dp * bob else 3.dp
        Text("🏃", fontSize = 54.sp, modifier = Modifier.offset(x = x, y = y + 19.dp))
    }
}

@Composable
private fun RunStatsCard(distanceKm: Double, calories: Double) {
    PremiumCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RunMetric("ДИСТАНЦИЯ", formatRunDistance(distanceKm), Modifier.weight(1f))
            RunMetric("КАЛОРИИ", "≈ ${calories.roundToInt()} ккал", Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = Hairline)
        Spacer(Modifier.height(12.dp))
        Text("Оценка по скорости 5 км/ч и весу 93 кг; это не измерение GPS или датчика пульса.", color = Muted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun RunMetric(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, color = Muted, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.1.sp)
        Spacer(Modifier.height(5.dp))
        Text(value, color = Paper, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
internal fun RunSummaryScreen(elapsedSeconds: Long, motionAllowed: Boolean, onDone: () -> Unit) {
    BackHandler(onBack = onDone)
    val reveal = remember(elapsedSeconds) { Animatable(if (motionAllowed) 0f else 1f) }
    LaunchedEffect(elapsedSeconds, motionAllowed) {
        if (motionAllowed) reveal.animateTo(1f, tween(1_300)) else reveal.snapTo(1f)
    }
    val stats = remember(elapsedSeconds) { calculateRunStats(elapsedSeconds) }
    val progress = reveal.value

    LazyColumn(
        Modifier.fillMaxSize().background(Ink).statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScreenHeader("Режим БЕГ", "Пробежка завершена", "Хорошая работа. Теперь восстановление.") }
        item {
            PremiumCard(color = Acid.copy(alpha = .075f), border = Acid.copy(alpha = .4f)) {
                RunFinishAnimation(progress)
                Spacer(Modifier.height(12.dp))
                Text("ИТОГ", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(14.dp))
                RunSummaryLine("Время", formatRunTime((elapsedSeconds * progress).roundToLong()))
                Spacer(Modifier.height(12.dp))
                RunSummaryLine("Расстояние", formatRunDistance(stats.distanceKm * progress))
                Spacer(Modifier.height(12.dp))
                RunSummaryLine("Сожжено", "≈ ${(stats.calories * progress).roundToInt()} ккал")
            }
        }
        item { HseActionButton("НА ГЛАВНЫЙ ЭКРАН", onClick = onDone) }
    }
}

@Composable
private fun RunFinishAnimation(progress: Float) {
    BoxWithConstraints(Modifier.fillMaxWidth().height(90.dp)) {
        val x = (maxWidth - 64.dp).coerceAtLeast(0.dp) * progress
        Text("🏃", fontSize = 52.sp, modifier = Modifier.offset(x = x))
        Text(
            "✓",
            color = Acid,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.CenterEnd).graphicsLayer { alpha = ((progress - .78f) / .22f).coerceIn(0f, 1f) },
        )
    }
}

@Composable
private fun RunSummaryLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Muted, style = MaterialTheme.typography.bodyLarge)
        Text(value, color = Paper, style = MaterialTheme.typography.titleLarge)
    }
}

private fun formatRunDistance(distanceKm: Double): String =
    String.format(Locale.forLanguageTag("ru"), "%.2f км", distanceKm)
