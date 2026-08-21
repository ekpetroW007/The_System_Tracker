@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.personal.thesystem.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.personal.thesystem.R
import com.personal.thesystem.data.SystemRepository
import com.personal.thesystem.model.DailyRecord
import com.personal.thesystem.model.DailyTask
import com.personal.thesystem.model.DecisionStatus
import com.personal.thesystem.model.DietViolationReason
import com.personal.thesystem.model.ExperimentFeedback
import com.personal.thesystem.model.LightPlanState
import com.personal.thesystem.model.SystemLogic
import com.personal.thesystem.model.SystemSettings
import com.personal.thesystem.model.ViolationReason
import com.personal.thesystem.model.WeeklyExperiment
import com.personal.thesystem.model.WeeklyReport
import com.personal.thesystem.notifications.ReminderScheduler
import com.personal.thesystem.ui.theme.Acid
import com.personal.thesystem.ui.theme.Amber
import com.personal.thesystem.ui.theme.Danger
import com.personal.thesystem.ui.theme.Hairline
import com.personal.thesystem.ui.theme.Ink
import com.personal.thesystem.ui.theme.Muted
import com.personal.thesystem.ui.theme.MorningInk
import com.personal.thesystem.ui.theme.NightInk
import com.personal.thesystem.ui.theme.Paper
import com.personal.thesystem.ui.theme.SurfaceRaised
import com.personal.thesystem.ui.theme.SurfaceSoft
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private val Ru = Locale("ru")
private val DayFormatter = DateTimeFormatter.ofPattern("d MMMM", Ru)

private enum class AppTab(val label: String) {
    TODAY("Сегодня"),
    HISTORY("История"),
    STATS("Анализ"),
    SETTINGS("Настройки"),
}

private enum class DecisionKind { SLEEP, DIET, WATER, LIGHT, MORNING }

private data class ConfettiParticle(
    val x: Float,
    val startY: Float,
    val delay: Float,
    val sway: Float,
    val phase: Float,
    val rotation: Float,
    val colorIndex: Int,
    val shape: Int,
    val scale: Float,
    val drift: Float,
    val spin: Float,
    val speed: Float,
)

private data class EmojiParticle(
    val x: Float,
    val startY: Float,
    val delay: Float,
    val sway: Float,
    val phase: Float,
    val rotation: Float,
    val scale: Float,
)

private data class SuccessEmojiParticle(
    val x: Float,
    val startY: Float,
    val delay: Float,
    val sway: Float,
    val phase: Float,
    val rotation: Float,
    val scale: Float,
    val symbolIndex: Int,
)

@Composable
fun SystemApp(repository: SystemRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(AppTab.TODAY) }
    var pendingReason by remember { mutableStateOf<Pair<LocalDate, DecisionKind>?>(null) }
    var emojiAfterReason by remember { mutableStateOf(false) }
    var confettiBurst by remember { mutableIntStateOf(0) }
    var celebrationKind by remember { mutableStateOf(DecisionKind.MORNING) }
    var emojiBurst by remember { mutableIntStateOf(0) }
    var exactAlarmsAllowed by remember { mutableStateOf(ReminderScheduler.canScheduleExactly(context)) }
    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            currentTime = LocalTime.now()
        }
    }
    val targetBackground = when {
        !currentTime.isBefore(repository.settings.digitalCutoff) -> NightInk
        currentTime.hour < 12 -> MorningInk
        else -> Ink
    }
    val appBackground by animateColorAsState(targetBackground, tween(900), label = "dayBackground")

    fun applySettings(transform: (SystemSettings) -> SystemSettings) {
        repository.updateSettings(transform)
        ReminderScheduler.scheduleAll(context, repository.settings)
    }

    val exactAlarmLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        exactAlarmsAllowed = ReminderScheduler.canScheduleExactly(context)
        ReminderScheduler.scheduleAll(context, repository.settings)
    }

    fun requestExactAlarmAccess() {
        if (ReminderScheduler.canScheduleExactly(context)) {
            exactAlarmsAllowed = true
            ReminderScheduler.scheduleAll(context, repository.settings)
        } else {
            exactAlarmLauncher.launch(ReminderScheduler.exactAlarmPermissionIntent(context))
        }
    }

    fun activateNotifications(granted: Boolean) {
        applySettings { it.copy(notificationsEnabled = granted) }
        if (granted) requestExactAlarmAccess()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> activateNotifications(granted) }

    fun enableNotifications() {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        else activateNotifications(true)
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = appBackground,
            bottomBar = {
                SystemBottomBar(
                    selected = selectedTab,
                    onSelect = { selectedTab = it },
                    background = appBackground,
                )
            }
        ) { insets ->
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                    (fadeIn() + slideInHorizontally { it / 9 * direction }) togetherWith
                        (fadeOut() + slideOutHorizontally { -it / 9 * direction })
                },
                label = "screenTransition",
                modifier = Modifier.padding(bottom = insets.calculateBottomPadding()),
            ) { tab ->
                when (tab) {
                    AppTab.TODAY -> TodayScreen(
                        repository = repository,
                        currentTime = currentTime,
                        onNo = { kind ->
                            when (kind) {
                                DecisionKind.MORNING -> {
                                    repository.setMorning(LocalDate.now(), DecisionStatus.NO)
                                    emojiBurst += 1
                                }
                                DecisionKind.LIGHT -> {
                                    repository.setLight(LocalDate.now(), DecisionStatus.NO)
                                    emojiBurst += 1
                                }
                                DecisionKind.WATER -> {
                                    repository.setWater(LocalDate.now(), DecisionStatus.NO)
                                    emojiBurst += 1
                                }
                                else -> {
                                    emojiAfterReason = true
                                    pendingReason = LocalDate.now() to kind
                                }
                            }
                        },
                        onCelebrate = { kind ->
                            celebrationKind = kind
                            confettiBurst += 1
                        },
                        onEnableNotifications = ::enableNotifications,
                        exactAlarmsAllowed = exactAlarmsAllowed,
                    )
                    AppTab.HISTORY -> HistoryScreen(
                        repository = repository,
                        onNo = { date, kind ->
                            when (kind) {
                                DecisionKind.MORNING -> {
                                    repository.setMorning(date, DecisionStatus.NO)
                                    emojiBurst += 1
                                }
                                DecisionKind.LIGHT -> {
                                    repository.setLight(date, DecisionStatus.NO)
                                    emojiBurst += 1
                                }
                                DecisionKind.WATER -> {
                                    repository.setWater(date, DecisionStatus.NO)
                                    emojiBurst += 1
                                }
                                else -> {
                                    emojiAfterReason = true
                                    pendingReason = date to kind
                                }
                            }
                        },
                        onCelebrate = { kind ->
                            celebrationKind = kind
                            confettiBurst += 1
                        },
                    )
                    AppTab.STATS -> StatsScreen(repository)
                    AppTab.SETTINGS -> SettingsScreen(
                        settings = repository.settings,
                        records = repository.records.values,
                        onUpdate = ::applySettings,
                        onEnableNotifications = ::enableNotifications,
                        exactAlarmsAllowed = exactAlarmsAllowed,
                    )
                }
            }
        }
        if (celebrationKind == DecisionKind.SLEEP) {
            ConfettiBurst(confettiBurst, Modifier.fillMaxSize().zIndex(20f))
        } else {
            SuccessEmojiBurst(confettiBurst, celebrationKind, Modifier.fillMaxSize().zIndex(21f))
        }
        EmojiRain(emojiBurst, Modifier.fillMaxSize().zIndex(22f))
    }

    pendingReason?.let { (date, kind) ->
        ReasonSheet(
            kind = kind,
            onDismiss = {
                emojiAfterReason = false
                pendingReason = null
            },
            onReason = { reason ->
                when (kind) {
                    DecisionKind.SLEEP -> repository.setSleep(date, DecisionStatus.NO, ViolationReason.fromId(reason))
                    DecisionKind.DIET -> repository.setDiet(date, DecisionStatus.NO, DietViolationReason.fromId(reason))
                    DecisionKind.LIGHT -> repository.setLight(date, DecisionStatus.NO)
                    DecisionKind.WATER -> repository.setWater(date, DecisionStatus.NO)
                    DecisionKind.MORNING -> repository.setMorning(date, DecisionStatus.NO)
                }
                val shouldLaunchEmoji = emojiAfterReason
                emojiAfterReason = false
                pendingReason = null
                if (shouldLaunchEmoji) {
                    scope.launch {
                        delay(220)
                        emojiBurst += 1
                    }
                }
            }
        )
    }
}

@Composable
private fun ConfettiBurst(burstId: Int, modifier: Modifier = Modifier) {
    val animation = remember { Animatable(1f) }
    val particles = remember(burstId) {
        if (burstId == 0) emptyList() else {
            val random = Random(burstId * 7919)
            List(156) { index ->
                val wave = index % 3
                ConfettiParticle(
                    x = random.nextFloat(),
                    startY = -0.22f + random.nextFloat() * 0.16f,
                    delay = wave * 0.17f + random.nextFloat() * 0.14f,
                    sway = 0.015f + random.nextFloat() * 0.075f,
                    phase = random.nextFloat() * 6.283f,
                    rotation = random.nextFloat() * 540f - 270f,
                    colorIndex = index % 8,
                    shape = index % 5,
                    scale = 0.65f + random.nextFloat() * 0.9f,
                    drift = random.nextFloat() * 0.22f - 0.11f,
                    spin = (480f + random.nextFloat() * 1080f) * if (index % 2 == 0) 1f else -1f,
                    speed = 0.9f + random.nextFloat() * 0.28f,
                )
            }
        }
    }

    LaunchedEffect(burstId) {
        if (burstId > 0) {
            animation.snapTo(0f)
            animation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 3300, easing = LinearEasing),
            )
        }
    }

    val progress = animation.value
    if (burstId == 0 || progress >= 1f) return
    val palette = listOf(
        Acid,
        Paper,
        Color(0xFF9EB6C2),
        Color(0xFF78838D),
        Danger,
        Color(0xFFB8A7FF),
        Color(0xFF7BDFF2),
        Color(0xFFFF8FAB),
    )

    Canvas(modifier) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height * 0.45f)
        val firstPulse = (progress / 0.28f).coerceIn(0f, 1f)
        if (firstPulse < 1f) {
            drawCircle(
                Acid.copy(alpha = (1f - firstPulse) * 0.20f),
                radius = size.minDimension * (0.08f + firstPulse * 0.56f),
                center = center,
            )
            drawCircle(
                Paper.copy(alpha = (1f - firstPulse) * 0.72f),
                radius = size.minDimension * (0.05f + firstPulse * 0.42f),
                center = center,
                style = Stroke(2.dp.toPx()),
            )
        }

        val secondPulse = ((progress - 0.13f) / 0.30f).coerceIn(0f, 1f)
        if (secondPulse > 0f && secondPulse < 1f) {
            drawCircle(
                Color(0xFF7BDFF2).copy(alpha = (1f - secondPulse) * 0.42f),
                radius = size.minDimension * (0.12f + secondPulse * 0.50f),
                center = center,
                style = Stroke(1.5.dp.toPx()),
            )
        }

        particles.forEach { particle ->
            val localProgress = ((progress - particle.delay) / (1f - particle.delay)).coerceIn(0f, 1f)
            if (localProgress <= 0f || localProgress >= 1f) return@forEach

            val pieceWidth = 6.dp.toPx() * particle.scale
            val pieceHeight = 14.dp.toPx() * particle.scale
            val alpha = if (localProgress < 0.78f) 1f else (1f - localProgress) / 0.22f
            val x = particle.x * size.width +
                sin(localProgress * 11f + particle.phase) * particle.sway * size.width +
                particle.drift * localProgress * size.width
            val startY = particle.startY * size.height
            val y = startY + localProgress * particle.speed * (size.height - startY + pieceHeight)
            val color = palette[particle.colorIndex].copy(alpha = alpha.coerceIn(0f, 1f))
            val pivot = androidx.compose.ui.geometry.Offset(x, y)

            rotate(degrees = particle.rotation + localProgress * particle.spin, pivot = pivot) {
                when (particle.shape) {
                    0 -> {
                        drawRoundRect(
                            color = color,
                            topLeft = androidx.compose.ui.geometry.Offset(x - pieceWidth / 2f, y - pieceHeight / 2f),
                            size = androidx.compose.ui.geometry.Size(pieceWidth, pieceHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(pieceWidth / 3f),
                        )
                    }
                    1 -> drawCircle(color, radius = pieceWidth * 0.58f, center = pivot)
                    2 -> drawLine(
                        color = color,
                        start = androidx.compose.ui.geometry.Offset(x, y - pieceHeight / 2f),
                        end = androidx.compose.ui.geometry.Offset(x, y + pieceHeight / 2f),
                        strokeWidth = pieceWidth * 0.72f,
                        cap = StrokeCap.Round,
                    )
                    3 -> {
                        drawLine(color, androidx.compose.ui.geometry.Offset(x - pieceWidth, y), androidx.compose.ui.geometry.Offset(x + pieceWidth, y), pieceWidth * 0.38f, StrokeCap.Round)
                        drawLine(color, androidx.compose.ui.geometry.Offset(x, y - pieceWidth), androidx.compose.ui.geometry.Offset(x, y + pieceWidth), pieceWidth * 0.38f, StrokeCap.Round)
                    }
                    else -> drawCircle(
                        color = color,
                        radius = pieceWidth * 0.72f,
                        center = pivot,
                        style = Stroke(pieceWidth * 0.28f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessEmojiBurst(burstId: Int, kind: DecisionKind, modifier: Modifier = Modifier) {
    val animation = remember { Animatable(1f) }
    val symbols = remember(kind) {
        when (kind) {
            DecisionKind.MORNING -> listOf("💪")
            DecisionKind.LIGHT -> listOf("📷")
            DecisionKind.DIET -> listOf("😏", "😁")
            DecisionKind.WATER -> listOf("🧊", "💧")
            DecisionKind.SLEEP -> listOf("❤️", "😏")
        }
    }
    val particles = remember(burstId, kind) {
        if (burstId == 0) emptyList() else {
            val random = Random(burstId * 65537 + kind.ordinal * 257)
            val count = when (kind) {
                DecisionKind.LIGHT -> 26
                DecisionKind.WATER, DecisionKind.SLEEP -> 42
                else -> 34
            }
            List(count) { index ->
                SuccessEmojiParticle(
                    x = random.nextFloat(),
                    startY = random.nextFloat(),
                    delay = random.nextFloat() * 0.38f,
                    sway = 0.025f + random.nextFloat() * 0.07f,
                    phase = random.nextFloat() * 6.283f,
                    rotation = random.nextFloat() * 50f - 25f,
                    scale = 0.72f + random.nextFloat() * 0.85f,
                    symbolIndex = index % symbols.size,
                )
            }
        }
    }
    val paint = remember {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = android.graphics.Paint.Align.CENTER
            color = android.graphics.Color.WHITE
        }
    }

    LaunchedEffect(burstId) {
        if (burstId > 0) {
            animation.snapTo(0f)
            animation.animateTo(1f, tween(durationMillis = 3300, easing = LinearEasing))
        }
    }

    val progress = animation.value
    if (burstId == 0 || progress >= 1f) return

    Canvas(modifier) {
        if (kind == DecisionKind.LIGHT) {
            val flash = listOf(0.18f, 0.40f, 0.62f, 0.82f).maxOf { moment ->
                (1f - abs(progress - moment) / 0.035f).coerceIn(0f, 1f)
            }
            if (flash > 0f) drawRect(Color.White.copy(alpha = flash * 0.58f))
        }

        particles.forEach { particle ->
            val localProgress = ((progress - particle.delay) / (1f - particle.delay)).coerceIn(0f, 1f)
            if (localProgress <= 0f || localProgress >= 1f) return@forEach

            val entrance = (localProgress / 0.12f).coerceIn(0f, 1f)
            val exit = ((1f - localProgress) / 0.18f).coerceIn(0f, 1f)
            val alpha = entrance * exit
            val x = particle.x * size.width +
                sin(localProgress * 9f + particle.phase) * particle.sway * size.width +
                if (kind == DecisionKind.SLEEP) localProgress * size.width * 0.12f else 0f
            val y = when (kind) {
                DecisionKind.MORNING, DecisionKind.DIET ->
                    size.height * (1.08f - particle.startY * 0.28f - localProgress * 1.16f)
                DecisionKind.LIGHT ->
                    size.height * (0.12f + particle.startY * 0.76f) + sin(localProgress * 10f + particle.phase) * 12.dp.toPx()
                DecisionKind.WATER ->
                    size.height * (-0.18f + particle.startY * 0.22f + localProgress * 1.28f)
                DecisionKind.SLEEP ->
                    size.height * (0.05f + particle.startY * 0.76f + localProgress * 0.24f)
            }
            val pulse = if (kind == DecisionKind.LIGHT) {
                0.86f + sin(localProgress * 24f + particle.phase) * 0.14f
            } else {
                1f
            }
            val textSize = 38.dp.toPx() * particle.scale * entrance * pulse
            val nativeCanvas = drawContext.canvas.nativeCanvas

            if (kind == DecisionKind.LIGHT) {
                val cameraFlash = (1f - abs((localProgress * 3f % 1f) - 0.18f) / 0.055f).coerceIn(0f, 1f)
                if (cameraFlash > 0f) {
                    drawCircle(
                        Color.White.copy(alpha = cameraFlash * alpha * 0.72f),
                        radius = textSize * 0.75f,
                        center = androidx.compose.ui.geometry.Offset(x, y),
                    )
                }
            }

            paint.textSize = textSize
            paint.alpha = (alpha * 255f).roundToInt().coerceIn(0, 255)
            nativeCanvas.save()
            nativeCanvas.rotate(particle.rotation + sin(localProgress * 8f + particle.phase) * 12f, x, y)
            nativeCanvas.drawText(symbols[particle.symbolIndex], x, y + textSize * 0.34f, paint)
            nativeCanvas.restore()
        }
    }
}

@Composable
private fun EmojiRain(burstId: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val emoji = remember { ImageBitmap.imageResource(context.resources, R.drawable.facepalm_emoji) }
    val animation = remember { Animatable(1f) }
    val particles = remember(burstId) {
        if (burstId == 0) emptyList() else {
            val random = Random(burstId * 104729)
            List(46) {
                EmojiParticle(
                    x = random.nextFloat(),
                    startY = -0.24f + random.nextFloat() * 0.18f,
                    delay = random.nextFloat() * 0.34f,
                    sway = 0.02f + random.nextFloat() * 0.055f,
                    phase = random.nextFloat() * 6.283f,
                    rotation = random.nextFloat() * 80f - 40f,
                    scale = 0.7f + random.nextFloat() * 0.65f,
                )
            }
        }
    }

    LaunchedEffect(burstId) {
        if (burstId > 0) {
            animation.snapTo(0f)
            animation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1700, easing = LinearEasing),
            )
        }
    }

    val progress = animation.value
    if (burstId == 0 || progress >= 1f) return

    Canvas(modifier) {
        val baseSize = 48.dp.toPx()
        particles.forEach { particle ->
            val localProgress = ((progress - particle.delay) / (1f - particle.delay)).coerceIn(0f, 1f)
            if (localProgress <= 0f || localProgress >= 1f) return@forEach

            val spriteSize = baseSize * particle.scale
            val alpha = if (localProgress < 0.86f) 1f else (1f - localProgress) / 0.14f
            val x = particle.x * size.width +
                sin(localProgress * 8f + particle.phase) * particle.sway * size.width
            val startY = particle.startY * size.height
            val y = startY + localProgress * (size.height - startY + spriteSize)

            rotate(
                degrees = particle.rotation + sin(localProgress * 7f + particle.phase) * 16f,
                pivot = androidx.compose.ui.geometry.Offset(x, y),
            ) {
                drawImage(
                    image = emoji,
                    dstOffset = androidx.compose.ui.unit.IntOffset(
                        (x - spriteSize / 2f).roundToInt(),
                        (y - spriteSize / 2f).roundToInt(),
                    ),
                    dstSize = androidx.compose.ui.unit.IntSize(
                        spriteSize.roundToInt(),
                        spriteSize.roundToInt(),
                    ),
                    alpha = alpha.coerceIn(0f, 1f),
                )
            }
        }
    }
}

@Composable
private fun SystemBottomBar(selected: AppTab, onSelect: (AppTab) -> Unit, background: Color) {
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background.copy(alpha = 0.98f))
            .border(1.dp, Hairline.copy(alpha = 0.65f))
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        AppTab.entries.forEach { tab ->
            val active = tab == selected
            val tint by animateColorAsState(if (active) Acid else Muted, label = "navTint")
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelect(tab)
                    }
                    .padding(vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                NavGlyph(tab, tint)
                Spacer(Modifier.height(4.dp))
                Text(
                    tab.label,
                    color = tint,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 9.sp,
                    letterSpacing = 0.2.sp,
                )
            }
        }
    }
}

@Composable
private fun NavGlyph(tab: AppTab, color: Color) {
    Canvas(Modifier.size(22.dp).semantics { contentDescription = tab.label }) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        when (tab) {
            AppTab.TODAY -> {
                drawCircle(color, radius = 8.dp.toPx(), style = stroke)
                drawLine(color, center.copy(x = center.x - 3.dp.toPx()), center.copy(x = center.x - 0.5.dp.toPx(), y = center.y + 3.dp.toPx()), stroke.width, StrokeCap.Round)
                drawLine(color, center.copy(x = center.x - 0.5.dp.toPx(), y = center.y + 3.dp.toPx()), center.copy(x = center.x + 4.dp.toPx(), y = center.y - 3.dp.toPx()), stroke.width, StrokeCap.Round)
            }
            AppTab.HISTORY -> {
                drawRoundRect(color, topLeft = androidx.compose.ui.geometry.Offset(3.dp.toPx(), 4.dp.toPx()), size = androidx.compose.ui.geometry.Size(16.dp.toPx(), 15.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()), style = stroke)
                drawLine(color, androidx.compose.ui.geometry.Offset(3.dp.toPx(), 8.dp.toPx()), androidx.compose.ui.geometry.Offset(19.dp.toPx(), 8.dp.toPx()), stroke.width)
                listOf(8f, 12f, 16f).forEach { x -> drawCircle(color, 0.8.dp.toPx(), androidx.compose.ui.geometry.Offset(x.dp.toPx(), 12.dp.toPx())) }
            }
            AppTab.STATS -> {
                drawLine(color, androidx.compose.ui.geometry.Offset(4.dp.toPx(), 18.dp.toPx()), androidx.compose.ui.geometry.Offset(4.dp.toPx(), 12.dp.toPx()), 3.dp.toPx(), StrokeCap.Round)
                drawLine(color, androidx.compose.ui.geometry.Offset(11.dp.toPx(), 18.dp.toPx()), androidx.compose.ui.geometry.Offset(11.dp.toPx(), 7.dp.toPx()), 3.dp.toPx(), StrokeCap.Round)
                drawLine(color, androidx.compose.ui.geometry.Offset(18.dp.toPx(), 18.dp.toPx()), androidx.compose.ui.geometry.Offset(18.dp.toPx(), 3.dp.toPx()), 3.dp.toPx(), StrokeCap.Round)
            }
            AppTab.SETTINGS -> {
                listOf(6f, 11f, 16f).forEachIndexed { index, y ->
                    drawLine(color, androidx.compose.ui.geometry.Offset(3.dp.toPx(), y.dp.toPx()), androidx.compose.ui.geometry.Offset(19.dp.toPx(), y.dp.toPx()), stroke.width, StrokeCap.Round)
                    val x = listOf(8f, 15f, 10f)[index]
                    drawCircle(Ink, 2.5.dp.toPx(), androidx.compose.ui.geometry.Offset(x.dp.toPx(), y.dp.toPx()))
                    drawCircle(color, 2.5.dp.toPx(), androidx.compose.ui.geometry.Offset(x.dp.toPx(), y.dp.toPx()), style = stroke)
                }
            }
        }
    }
}

@Composable
private fun ScreenHeader(eyebrow: String, title: String, detail: String? = null) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).background(Acid, CircleShape))
            Spacer(Modifier.width(9.dp))
            Text(
                eyebrow.uppercase(Ru),
                color = Acid,
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 2.sp,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(title, color = Paper, style = MaterialTheme.typography.headlineLarge)
        if (detail != null) {
            Spacer(Modifier.height(6.dp))
            Text(detail, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TodayScreen(
    repository: SystemRepository,
    currentTime: LocalTime,
    onNo: (DecisionKind) -> Unit,
    onCelebrate: (DecisionKind) -> Unit,
    onEnableNotifications: () -> Unit,
    exactAlarmsAllowed: Boolean,
) {
    val today = LocalDate.now()
    val record = repository.recordFor(today)
    val settings = repository.settings
    var expandedCompleted by remember(today) { mutableStateOf(emptySet<DailyTask>()) }
    val admission = SystemLogic.admissionFor(today, settings.admissionStart, repository.records.values)
    val lightPlan = SystemLogic.lightPlanFor(today, settings.lightStart)
    val experiment = if (SystemLogic.experimentAvailableOn(today)) {
        SystemLogic.weeklyExperiment(today, repository.records.values, repository.experimentFeedback)
    } else {
        null
    }
    val answered = listOf(record.morning, record.light, record.diet, record.water, record.sleep).count { it != null }
    val currentTask = SystemLogic.currentTask(record, currentTime, settings.digitalCutoff)
    val recoveryTask = SystemLogic.recoveryTask(record, currentTime, settings.digitalCutoff)

    fun toggleCompleted(task: DailyTask) {
        expandedCompleted = if (task in expandedCompleted) expandedCompleted - task else expandedCompleted + task
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                ScreenHeader("The System", today.format(DayFormatter).replaceFirstChar { it.uppercase() })
                SystemMark(answered / 5f)
            }
        }

        item { DailyStatement(answered, record) }

        if (settings.hseModeEnabled && today.dayOfWeek in DayOfWeek.MONDAY..DayOfWeek.FRIDAY) {
            item {
                HseModeCard(
                    settings = settings,
                    currentTime = currentTime,
                    task = record.studyTask,
                    onTaskChange = { repository.setStudyTask(today, it) },
                )
            }
        }

        recoveryTask?.let { task ->
            item { RecoveryCard(task) }
        }

        experiment?.let { weeklyExperiment ->
            item {
                WeeklyExperimentCard(
                    experiment = weeklyExperiment,
                    feedback = repository.experimentFeedback[weeklyExperiment.weekStart],
                    canReview = today.dayOfWeek == DayOfWeek.SATURDAY || today.dayOfWeek == DayOfWeek.SUNDAY,
                    onFeedback = { repository.setExperimentFeedback(weeklyExperiment.weekStart, it) },
                )
            }
        }

        if (!settings.notificationsEnabled || !exactAlarmsAllowed) {
            item {
                ReminderPermissionCard(
                    title = if (settings.notificationsEnabled) "Разрешить точное время" else "Включить напоминания",
                    detail = if (settings.notificationsEnabled) {
                        "Чтобы Android присылал их вовремя, даже когда приложение закрыто"
                    } else {
                        "Отбой, подготовка ко сну и утренний триггер"
                    },
                    onEnable = onEnableNotifications,
                )
            }
        }

        item {
            SectionLabel("УТРО", if (admission.completed) "РЕЖИМ 20" else "УРОВЕНЬ ${admission.level}/${admission.totalLevels}")
            Spacer(Modifier.height(8.dp))
            DecisionCard(
                title = "УТРО",
                number = "01",
                description = if (admission.completed) {
                    "Сразу после пробуждения — 20 отжиманий."
                } else {
                    "Сразу после пробуждения — ${admission.target} ${pushupWord(admission.target)}."
                },
                status = record.morning,
                reasonLabel = record.morningReason?.label,
                onYes = {
                    if (SystemLogic.toggledDecision(record.morning, DecisionStatus.YES) == null) {
                        repository.clearMorning(today)
                    } else {
                        expandedCompleted -= DailyTask.MORNING
                        repository.setMorning(today, DecisionStatus.YES)
                        onCelebrate(DecisionKind.MORNING)
                    }
                },
                onNo = {
                    if (SystemLogic.toggledDecision(record.morning, DecisionStatus.NO) == null) repository.clearMorning(today)
                    else onNo(DecisionKind.MORNING)
                },
                collapsed = record.morning == DecisionStatus.YES && DailyTask.MORNING !in expandedCompleted,
                current = currentTask == DailyTask.MORNING,
                onToggleCollapsed = { toggleCompleted(DailyTask.MORNING) },
                footer = {
                    AdmissionProgress(admission.target, admission.level, admission.totalLevels, admission.completed)
                },
            )
        }

        item {
            SectionLabel("СВЕТ", if (lightPlan.completed) "РЕЖИМ ЗАКРЕПЛЁН" else "ДЕНЬ ${lightPlan.day}/${lightPlan.totalDays}")
            Spacer(Modifier.height(8.dp))
            DecisionCard(
                title = "ПРИВЫКАНИЕ К СВЕТУ",
                number = "02",
                description = lightPlan.task,
                status = record.light,
                reasonLabel = null,
                onYes = {
                    if (SystemLogic.toggledDecision(record.light, DecisionStatus.YES) == null) {
                        repository.clearLight(today)
                    } else {
                        expandedCompleted -= DailyTask.LIGHT
                        repository.setLight(today, DecisionStatus.YES)
                        onCelebrate(DecisionKind.LIGHT)
                    }
                },
                onNo = {
                    if (SystemLogic.toggledDecision(record.light, DecisionStatus.NO) == null) repository.clearLight(today)
                    else onNo(DecisionKind.LIGHT)
                },
                collapsed = record.light == DecisionStatus.YES && DailyTask.LIGHT !in expandedCompleted,
                current = currentTask == DailyTask.LIGHT,
                onToggleCollapsed = { toggleCompleted(DailyTask.LIGHT) },
                footer = { LightPlanProgress(lightPlan) },
            )
        }

        item {
            SectionLabel("ДЕНЬ", "БЕЗ СЛАДКОГО И БЕЗ ЧИПСОВ")
            Spacer(Modifier.height(8.dp))
            DecisionCard(
                title = "I'm on a diet",
                number = "03",
                description = "День без сладкого и без чипсов. Газировку можно.",
                status = record.diet,
                reasonLabel = record.dietReason?.label,
                onYes = {
                    if (SystemLogic.toggledDecision(record.diet, DecisionStatus.YES) == null) {
                        repository.clearDiet(today)
                    } else {
                        expandedCompleted -= DailyTask.DIET
                        repository.setDiet(today, DecisionStatus.YES)
                        onCelebrate(DecisionKind.DIET)
                    }
                },
                onNo = {
                    if (SystemLogic.toggledDecision(record.diet, DecisionStatus.NO) == null) repository.clearDiet(today)
                    else onNo(DecisionKind.DIET)
                },
                collapsed = record.diet == DecisionStatus.YES && DailyTask.DIET !in expandedCompleted,
                current = currentTask == DailyTask.DIET,
                onToggleCollapsed = { toggleCompleted(DailyTask.DIET) },
            )
        }

        item {
            SectionLabel("ВОДА", "2,5 ЛИТРА")
            Spacer(Modifier.height(8.dp))
            WaterCard(
                quarters = SystemLogic.waterQuarters(record) ?: 0,
                onMinus = { repository.adjustWater(today, -1) },
                onPlus = {
                    if (repository.adjustWater(today, 1)) {
                        expandedCompleted -= DailyTask.WATER
                        onCelebrate(DecisionKind.WATER)
                    }
                },
                collapsed = record.water == DecisionStatus.YES && DailyTask.WATER !in expandedCompleted,
                current = currentTask == DailyTask.WATER,
                onToggleCollapsed = { toggleCompleted(DailyTask.WATER) },
            )
        }

        item {
            SectionLabel("ВЕЧЕР", "22:45 → 23:30")
            Spacer(Modifier.height(8.dp))
            DecisionCard(
                title = "СОН",
                number = "05",
                description = "Цифровой отбой в ${SystemLogic.formatTime(settings.digitalCutoff)}. В кровати в ${SystemLogic.formatTime(settings.bedTime)}.",
                status = record.sleep,
                reasonLabel = record.sleepReason?.label,
                onYes = {
                    if (SystemLogic.toggledDecision(record.sleep, DecisionStatus.YES) == null) {
                        repository.clearSleep(today)
                    } else {
                        expandedCompleted -= DailyTask.SLEEP
                        repository.setSleep(today, DecisionStatus.YES)
                        onCelebrate(DecisionKind.SLEEP)
                    }
                },
                onNo = {
                    if (SystemLogic.toggledDecision(record.sleep, DecisionStatus.NO) == null) repository.clearSleep(today)
                    else onNo(DecisionKind.SLEEP)
                },
                collapsed = record.sleep == DecisionStatus.YES && DailyTask.SLEEP !in expandedCompleted,
                current = currentTask == DailyTask.SLEEP,
                onToggleCollapsed = { toggleCompleted(DailyTask.SLEEP) },
            )
        }

        item { TonightProtocol(settings) }
    }
}

@Composable
private fun SystemMark(progress: Float) {
    val animated by animateFloatAsState(progress, spring(stiffness = Spring.StiffnessLow), label = "systemMark")
    Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Hairline, style = Stroke(3.dp.toPx()))
            drawArc(
                Acid,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                style = Stroke(3.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        Text("S", color = if (progress > 0f) Acid else Muted, fontWeight = FontWeight.Black, fontSize = 19.sp)
    }
}

@Composable
private fun DailyStatement(answered: Int, record: DailyRecord) {
    val failures = listOf(record.morning, record.light, record.diet, record.water, record.sleep).count { it == DecisionStatus.NO }
    val label = when {
        answered == 0 -> "ПЯТЬ ЗАДАЧ -\nОДИН ДЕНЬ"
        answered == 1 -> "ПЕРВАЯ ЧАСТЬ\nГОТОВА"
        answered == 2 -> "ДВЕ ЧАСТИ\nГОТОВЫ"
        answered == 3 -> "ПОЛДЕЛА\nСДЕЛАНО"
        answered == 4 -> "ПОЧТИ\nГОТОВО"
        failures == 0 -> "ДЕНЬ\nЗАКРЫТ."
        else -> "НАРУШЕНИЯ\nУСТАНОВЛЕНЫ"
    }
    val supporting = SystemLogic.contextualPhrase(record)
    Column(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)) {
        Text(label, color = Paper, style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(12.dp))
        Text(supporting, color = Muted, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth(0.9f))
    }
}

@Composable
private fun RecoveryCard(task: DailyTask) {
    PremiumCard(color = SurfaceSoft, border = Acid.copy(alpha = .28f)) {
        Text("РЕЖИМ ВОССТАНОВЛЕНИЯ", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        Text("День не потерян.", color = Paper, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(5.dp))
        Text("Следующее действие — ${task.recoveryAction.replaceFirstChar { it.lowercase() }}", color = Muted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun HseModeCard(
    settings: SystemSettings,
    currentTime: LocalTime,
    task: String,
    onTaskChange: (String) -> Unit,
) {
    val leaveTime = SystemLogic.hseLeaveTime(settings)
    PremiumCard(color = Acid.copy(alpha = .075f), border = Acid.copy(alpha = .30f)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Text("РЕЖИМ ВШЭ", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(7.dp))
                Text(SystemLogic.hseCountdown(currentTime, leaveTime), color = Paper, style = MaterialTheme.typography.titleLarge)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("ПАРА ${SystemLogic.formatTime(settings.hseFirstClassTime)}", color = Paper, style = MaterialTheme.typography.labelMedium)
                Text("ВЫХОД ${SystemLogic.formatTime(leaveTime)}", color = Muted, style = MaterialTheme.typography.labelMedium)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("ГЛАВНАЯ УЧЕБНАЯ ЗАДАЧА", color = Muted, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.1.sp)
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = task,
            onValueChange = onTaskChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Paper),
            cursorBrush = SolidColor(Acid),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceSoft)
                .padding(horizontal = 14.dp, vertical = 13.dp),
            decorationBox = { inner ->
                Box {
                    if (task.isBlank()) Text("Например: закрыть лабораторную", color = Muted, style = MaterialTheme.typography.bodyLarge)
                    inner()
                }
            },
        )
    }
}

@Composable
private fun WeeklyExperimentCard(
    experiment: WeeklyExperiment,
    feedback: ExperimentFeedback?,
    canReview: Boolean,
    onFeedback: (ExperimentFeedback) -> Unit,
) {
    PremiumCard(color = Acid.copy(alpha = .08f), border = Acid.copy(alpha = .28f)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("ЭКСПЕРИМЕНТ НЕДЕЛИ", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
            Text(if (experiment.continued) "ПРОДОЛЖЕНИЕ" else "7 ДНЕЙ", color = Muted, style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(13.dp))
        Text(experiment.focus.title, color = Paper, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(experiment.focus.action, color = Muted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        Text(experiment.basis, color = Paper.copy(alpha = .72f), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Hairline)
        Spacer(Modifier.height(14.dp))

        if (feedback != null) {
            Text("ИТОГ: ${feedback.label.uppercase(Ru)}", color = Acid, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.sp)
        } else if (canReview) {
            Text("ИТОГ НЕДЕЛИ", color = Muted, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(9.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                ExperimentFeedback.entries.forEach { option ->
                    val label = when (option) {
                        ExperimentFeedback.HELPED -> "ПОМОГЛО"
                        ExperimentFeedback.DID_NOT_HELP -> "НЕ ПОМОГЛО"
                        ExperimentFeedback.CONTINUE -> "ПРОДОЛЖИТЬ"
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Hairline, RoundedCornerShape(12.dp))
                            .clickable(role = Role.Button) { onFeedback(option) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, color = Paper, style = MaterialTheme.typography.labelMedium, fontSize = 9.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            Text("Итог можно отметить в выходные.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ReminderPermissionCard(title: String, detail: String, onEnable: () -> Unit) {
    PremiumCard(
        modifier = Modifier.clickable(onClick = onEnable),
        color = Acid.copy(alpha = 0.09f),
        border = Acid.copy(alpha = 0.28f),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(Acid, CircleShape), contentAlignment = Alignment.Center) {
                Text("!", color = Ink, fontWeight = FontWeight.Black, fontSize = 19.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Paper, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(3.dp))
                Text(detail, color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
            Text("→", color = Acid, fontSize = 23.sp)
        }
    }
}

@Composable
private fun SectionLabel(left: String, right: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(left, color = Muted, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.8.sp)
        Text(right, color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 0.6.sp)
    }
}

@Composable
private fun DecisionCard(
    title: String,
    number: String,
    description: String,
    status: DecisionStatus?,
    reasonLabel: String?,
    onYes: () -> Unit,
    onNo: () -> Unit,
    collapsed: Boolean = false,
    current: Boolean = false,
    onToggleCollapsed: () -> Unit = {},
    footer: (@Composable () -> Unit)? = null,
) {
    val background by animateColorAsState(
        when (status) {
            DecisionStatus.YES -> Acid.copy(alpha = 0.10f)
            DecisionStatus.NO -> Danger.copy(alpha = 0.08f)
            null -> SurfaceRaised
        },
        label = "decisionColor",
    )
    AnimatedContent(targetState = collapsed, label = "collapse$number") { isCollapsed ->
        if (isCollapsed) {
            PremiumCard(
                color = Acid.copy(alpha = .08f),
                border = Acid.copy(alpha = .34f),
                modifier = Modifier.clickable(onClick = onToggleCollapsed),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(number, color = Acid, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(14.dp))
                    Text(title, color = Paper, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text("✓", color = Acid, fontWeight = FontWeight.Black, fontSize = 19.sp)
                }
            }
            return@AnimatedContent
        }
        PremiumCard(
            color = if (current && status == null) Acid.copy(alpha = .06f) else background,
            border = when {
                current && status == null -> Acid.copy(alpha = .78f)
                status == DecisionStatus.YES -> Acid.copy(alpha = .38f)
                else -> Hairline
            },
            modifier = Modifier.animateContentSize(),
        ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(number, color = if (status == null) Muted else Acid, style = MaterialTheme.typography.labelMedium)
                    if (current && status == null) {
                        Spacer(Modifier.width(9.dp))
                        Text("СЕЙЧАС", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.2.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(title, color = Paper, style = MaterialTheme.typography.headlineMedium, letterSpacing = 1.sp)
                Spacer(Modifier.height(7.dp))
                Text(description, color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
            if (status != null) {
                Box(
                    Modifier
                        .size(34.dp)
                        .background(if (status == DecisionStatus.YES) Acid else Danger, CircleShape)
                        .clickable(enabled = status == DecisionStatus.YES, onClick = onToggleCollapsed),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (status == DecisionStatus.YES) "✓" else "×", color = Ink, fontWeight = FontWeight.Black, fontSize = 19.sp)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            DecisionButton("ДА", status == DecisionStatus.YES, true, Modifier.weight(1f), onYes)
            DecisionButton("НЕТ", status == DecisionStatus.NO, false, Modifier.weight(1f), onNo)
        }
        AnimatedVisibility(reasonLabel != null, enter = fadeIn(), exit = fadeOut()) {
            if (reasonLabel != null) {
                Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("ПРИЧИНА", color = Muted, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(8.dp))
                    Text(reasonLabel, color = Danger, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        if (footer != null) {
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = Hairline)
            Spacer(Modifier.height(16.dp))
            footer()
        }
        }
    }
}

@Composable
private fun WaterCard(
    quarters: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    collapsed: Boolean = false,
    current: Boolean = false,
    onToggleCollapsed: () -> Unit = {},
) {
    val completed = quarters >= SystemLogic.WATER_GOAL_QUARTERS
    val progress by animateFloatAsState(
        (quarters / SystemLogic.WATER_GOAL_QUARTERS.toFloat()).coerceIn(0f, 1f),
        spring(stiffness = Spring.StiffnessLow),
        label = "waterProgress",
    )
    val background by animateColorAsState(
        if (completed) Acid.copy(alpha = .10f) else SurfaceRaised,
        label = "waterColor",
    )
    AnimatedContent(targetState = collapsed, label = "collapseWater") { isCollapsed ->
        if (isCollapsed) {
            PremiumCard(
                color = Acid.copy(alpha = .08f),
                border = Acid.copy(alpha = .34f),
                modifier = Modifier.clickable(onClick = onToggleCollapsed),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("04", color = Acid, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(14.dp))
                    Text("Watering · ${SystemLogic.formatWaterLiters(quarters)} л", color = Paper, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text("✓", color = Acid, fontWeight = FontWeight.Black, fontSize = 19.sp)
                }
            }
            return@AnimatedContent
        }
        PremiumCard(
            color = if (current && !completed) Acid.copy(alpha = .06f) else background,
            border = when {
                current && !completed -> Acid.copy(alpha = .78f)
                completed -> Acid.copy(alpha = .38f)
                else -> Hairline
            },
            modifier = Modifier.animateContentSize(),
        ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("04", color = if (quarters == 0) Muted else Acid, style = MaterialTheme.typography.labelMedium)
                    if (current && !completed) {
                        Spacer(Modifier.width(9.dp))
                        Text("СЕЙЧАС", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.2.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("Watering", color = Paper, style = MaterialTheme.typography.headlineMedium, letterSpacing = 1.sp)
                Spacer(Modifier.height(7.dp))
                Text("Выпить за день 2,5 литра воды.", color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
            if (completed) {
                Box(
                    Modifier.size(34.dp).background(Acid, CircleShape).clickable(onClick = onToggleCollapsed),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✓", color = Ink, fontWeight = FontWeight.Black, fontSize = 19.sp)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            WaterAdjustButton("−", "Убавить 0,25 литра", quarters > 0, onClick = onMinus)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${SystemLogic.formatWaterLiters(quarters)} л",
                    color = if (completed) Acid else Paper,
                    style = MaterialTheme.typography.headlineLarge,
                )
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(7.dp).background(Hairline, CircleShape)) {
                    Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(Acid, CircleShape))
                }
                Spacer(Modifier.height(7.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("0", color = Muted, style = MaterialTheme.typography.labelMedium)
                    Text("2,5 л", color = if (completed) Acid else Muted, style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.width(14.dp))
            WaterAdjustButton(
                "+",
                "Добавить 0,25 литра",
                quarters < SystemLogic.WATER_MAX_QUARTERS,
                strong = quarters == SystemLogic.WATER_GOAL_QUARTERS - 1,
                onClick = onPlus,
            )
        }
        }
    }
}

@Composable
private fun WaterAdjustButton(
    text: String,
    description: String,
    enabled: Boolean,
    strong: Boolean = false,
    onClick: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    Box(
        Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(if (enabled) SurfaceSoft else SurfaceSoft.copy(alpha = .35f))
            .border(1.dp, if (enabled) Acid.copy(alpha = .45f) else Hairline, CircleShape)
            .semantics { role = Role.Button; contentDescription = description }
            .clickable(enabled = enabled) {
                haptics.performHapticFeedback(if (strong) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (enabled) Paper else Muted.copy(alpha = .35f), fontSize = 28.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DecisionButton(
    text: String,
    selected: Boolean,
    positive: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.965f else 1f, spring(stiffness = Spring.StiffnessMedium), label = "press")
    val selectedColor = if (positive) Acid else Danger
    Box(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .height(48.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (selected) selectedColor else SurfaceSoft)
            .border(1.dp, if (selected) selectedColor else Hairline, RoundedCornerShape(15.dp))
            .semantics { role = Role.Button; contentDescription = text }
            .clickable(interactionSource = interaction, indication = null) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (selected) Ink else Paper, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.2.sp)
    }
}

@Composable
private fun AdmissionProgress(target: Int, level: Int, total: Int, completed: Boolean) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column {
                Text("УРОВЕНЬ", color = Muted, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Text("$level", color = Acid, style = MaterialTheme.typography.displayMedium)
            }
            Text("$target ПОДРЯД", color = Paper, style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(5.dp).background(Hairline, CircleShape)) {
            val fraction = if (completed) 1f else level / total.toFloat()
            val animated by animateFloatAsState(fraction, spring(stiffness = Spring.StiffnessLow), label = "admission")
            Box(Modifier.fillMaxWidth(animated).fillMaxHeight().background(Acid, CircleShape))
        }
    }
}

@Composable
private fun LightPlanProgress(plan: LightPlanState) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("ПЛАН ПРИВЫКАНИЯ", color = Muted, style = MaterialTheme.typography.labelMedium)
            Text("${plan.day}/${plan.totalDays}", color = Paper, style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(5.dp).background(Hairline, CircleShape)) {
            val fraction = if (plan.completed) 1f else plan.day / plan.totalDays.toFloat()
            Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(Acid, CircleShape))
        }
        Spacer(Modifier.height(12.dp))
        Text("Держи дискомфорт на уровне 3–5/10. Если он выше — повтори предыдущий этап.", color = Muted, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun pushupWord(count: Int): String = when (count % 10) {
    1 -> "отжимание"
    2, 3, 4 -> "отжимания"
    else -> "отжиманий"
}

@Composable
private fun TonightProtocol(settings: SystemSettings) {
    PremiumCard(color = SurfaceRaised, border = Hairline) {
        Text("ПРОТОКОЛ ВЕЧЕРА", color = Paper, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.4.sp)
        Spacer(Modifier.height(16.dp))
        TimelineItem(SystemLogic.formatTime(settings.dietTime), "Отметить питание", false)
        TimelineItem(SystemLogic.formatTime(settings.digitalCutoff.minusMinutes(15)), "Заверши текущее", false)
        TimelineItem(SystemLogic.formatTime(settings.digitalCutoff), "Цифровой отбой", false)
        TimelineItem(SystemLogic.formatTime(settings.bedTime.minusMinutes(30)), "Подготовка ко сну", false)
        TimelineItem(SystemLogic.formatTime(settings.bedTime), "В кровати", true)
    }
}

@Composable
private fun TimelineItem(time: String, text: String, last: Boolean) {
    Row(Modifier.height(if (last) 32.dp else 48.dp)) {
        Text(time, color = Acid, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(52.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(8.dp).background(Acid, CircleShape))
            if (!last) Box(Modifier.width(1.dp).weight(1f).background(Hairline))
        }
        Spacer(Modifier.width(12.dp))
        Text(text, color = Paper, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ReasonSheet(kind: DecisionKind, onDismiss: () -> Unit, onReason: (String) -> Unit) {
    val haptics = LocalHapticFeedback.current
    val reasons = when (kind) {
        DecisionKind.SLEEP -> ViolationReason.entries.map { it.id to it.label }
        DecisionKind.DIET -> DietViolationReason.entries.map { it.id to it.label }
        DecisionKind.MORNING, DecisionKind.LIGHT, DecisionKind.WATER -> emptyList()
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceRaised,
        contentColor = Paper,
        dragHandle = { Box(Modifier.padding(top = 11.dp).size(42.dp, 4.dp).background(Hairline, CircleShape)) },
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 36.dp)) {
            Text("ЗАФИКСИРОВАТЬ ПРИЧИНУ", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(10.dp))
            Text(
                if (kind == DecisionKind.DIET) "Что помешало питанию?" else "Что сорвало вечер?",
                color = Paper,
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(Modifier.height(8.dp))
            Text("Без оправданий и самооценки — только факт для анализа.", color = Muted, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(22.dp))
            reasons.forEach { (id, label) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 9.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceSoft)
                        .border(1.dp, Hairline, RoundedCornerShape(16.dp))
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onReason(id)
                        }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(8.dp).background(Danger, CircleShape))
                    Spacer(Modifier.width(13.dp))
                    Text(label, color = Paper, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    Text("→", color = Muted, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun PremiumCard(
    color: Color = SurfaceRaised,
    border: Color = Hairline,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(color)
            .border(1.dp, border, RoundedCornerShape(24.dp))
            .padding(18.dp),
        content = content,
    )
}

@Composable
private fun HistoryScreen(
    repository: SystemRepository,
    onNo: (LocalDate, DecisionKind) -> Unit,
    onCelebrate: (DecisionKind) -> Unit,
) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val today = LocalDate.now()

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { ScreenHeader("История", "Календарь", "История и карта повторяющихся решений.") }
        item {
            CalendarCard(
                month = month,
                records = repository.records,
                today = today,
                onPrevious = { month = month.minusMonths(1) },
                onNext = { if (month < YearMonth.now()) month = month.plusMonths(1) },
                onSelect = { selectedDate = it },
            )
        }
        item { CalendarLegend() }
        item {
            val monthRecords = repository.records.values.filter { YearMonth.from(it.date) == month }
            MonthSummary(monthRecords)
        }
    }

    selectedDate?.let { date ->
        HistoryEditorSheet(
            date = date,
            record = repository.recordFor(date),
            admissionTarget = SystemLogic.admissionFor(
                date,
                repository.settings.admissionStart,
                repository.records.values,
            ).target,
            lightPlan = SystemLogic.lightPlanFor(date, repository.settings.lightStart),
            onDismiss = { selectedDate = null },
            onSleepYes = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).sleep, DecisionStatus.YES) == null) repository.clearSleep(date)
                else {
                    repository.setSleep(date, DecisionStatus.YES)
                    selectedDate = null
                    onCelebrate(DecisionKind.SLEEP)
                }
            },
            onSleepNo = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).sleep, DecisionStatus.NO) == null) repository.clearSleep(date)
                else {
                    selectedDate = null
                    onNo(date, DecisionKind.SLEEP)
                }
            },
            onMorningYes = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).morning, DecisionStatus.YES) == null) repository.clearMorning(date)
                else {
                    repository.setMorning(date, DecisionStatus.YES)
                    selectedDate = null
                    onCelebrate(DecisionKind.MORNING)
                }
            },
            onMorningNo = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).morning, DecisionStatus.NO) == null) repository.clearMorning(date)
                else {
                    selectedDate = null
                    onNo(date, DecisionKind.MORNING)
                }
            },
            onLightYes = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).light, DecisionStatus.YES) == null) repository.clearLight(date)
                else {
                    repository.setLight(date, DecisionStatus.YES)
                    selectedDate = null
                    onCelebrate(DecisionKind.LIGHT)
                }
            },
            onLightNo = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).light, DecisionStatus.NO) == null) repository.clearLight(date)
                else {
                    selectedDate = null
                    onNo(date, DecisionKind.LIGHT)
                }
            },
            onDietYes = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).diet, DecisionStatus.YES) == null) repository.clearDiet(date)
                else {
                    repository.setDiet(date, DecisionStatus.YES)
                    selectedDate = null
                    onCelebrate(DecisionKind.DIET)
                }
            },
            onDietNo = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).diet, DecisionStatus.NO) == null) repository.clearDiet(date)
                else {
                    selectedDate = null
                    onNo(date, DecisionKind.DIET)
                }
            },
            onWaterMinus = { repository.adjustWater(date, -1) },
            onWaterPlus = {
                if (repository.adjustWater(date, 1)) {
                    selectedDate = null
                    onCelebrate(DecisionKind.WATER)
                }
            },
        )
    }
}

@Composable
private fun CalendarCard(
    month: YearMonth,
    records: Map<LocalDate, DailyRecord>,
    today: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelect: (LocalDate) -> Unit,
) {
    PremiumCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            CircleTextButton("‹", "Предыдущий месяц", onPrevious)
            Text(
                month.month.getDisplayName(TextStyle.FULL, Ru).replaceFirstChar { it.uppercase() } + "  ${month.year}",
                color = Paper,
                style = MaterialTheme.typography.titleLarge,
            )
            CircleTextButton("›", "Следующий месяц", onNext, enabled = month < YearMonth.now())
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth()) {
            listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС").forEach { day ->
                Text(day, color = Muted, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(8.dp))
        val firstOffset = month.atDay(1).dayOfWeek.value - 1
        var day = 1 - firstOffset
        repeat(6) {
            Row(Modifier.fillMaxWidth()) {
                repeat(7) {
                    if (day in 1..month.lengthOfMonth()) {
                        val date = month.atDay(day)
                        CalendarDay(date, records[date], date == today, date <= today, Modifier.weight(1f)) { onSelect(date) }
                    } else {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    }
                    day++
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    record: DailyRecord?,
    isToday: Boolean,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val good = listOf(record?.morning, record?.light, record?.diet, record?.water, record?.sleep).count { it == DecisionStatus.YES }
    val bad = listOf(record?.morning, record?.light, record?.diet, record?.water, record?.sleep).count { it == DecisionStatus.NO }
    Box(
        modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(if (isToday) Modifier.border(1.dp, Acid, RoundedCornerShape(12.dp)) else Modifier)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(date.dayOfMonth.toString(), color = if (enabled) Paper else Muted.copy(alpha = .35f), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                if (record == null || (record.sleep == null && record.morning == null && record.light == null && record.diet == null && record.water == null)) {
                    Box(Modifier.size(3.dp).background(Hairline, CircleShape))
                } else {
                    repeat(good) { Box(Modifier.size(4.dp).background(Acid, CircleShape)) }
                    repeat(bad) { Box(Modifier.size(4.dp).background(Danger, CircleShape)) }
                }
            }
        }
    }
}

@Composable
private fun CircleTextButton(text: String, description: String, onClick: () -> Unit, enabled: Boolean = true) {
    Box(
        Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(SurfaceSoft)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (enabled) Paper else Muted.copy(alpha = .25f), fontSize = 24.sp)
    }
}

@Composable
private fun CalendarLegend() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        LegendDot(Acid, "соблюдено")
        Spacer(Modifier.width(20.dp))
        LegendDot(Danger, "нарушено")
        Spacer(Modifier.width(20.dp))
        LegendDot(Hairline, "нет отметки")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(label, color = Muted, style = MaterialTheme.typography.bodyMedium, fontSize = 11.sp)
    }
}

@Composable
private fun MonthSummary(records: List<DailyRecord>) {
    val sleep = SystemLogic.compliance(records) { it.sleep }
    val morning = SystemLogic.compliance(records) { it.morning }
    val light = SystemLogic.compliance(records) { it.light }
    val diet = SystemLogic.compliance(records) { it.diet }
    val water = SystemLogic.compliance(records) { it.water }
    PremiumCard(color = SurfaceSoft) {
        Text("ИТОГ МЕСЯЦА", color = Muted, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth()) {
            CompactMetric("УТРО", morning, Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(50.dp).background(Hairline))
            CompactMetric("СВЕТ", light, Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(50.dp).background(Hairline))
            CompactMetric("ПИТАНИЕ", diet, Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(50.dp).background(Hairline))
            CompactMetric("ВОДА", water, Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(50.dp).background(Hairline))
            CompactMetric("СОН", sleep, Modifier.weight(1f))
        }
    }
}

@Composable
private fun CompactMetric(label: String, value: Int?, modifier: Modifier, suffix: String = "%") {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value?.let { "$it$suffix" } ?: "—",
            color = Paper,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            lineHeight = 23.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(label, color = Muted, style = MaterialTheme.typography.labelMedium, fontSize = 10.sp, maxLines = 1)
    }
}

@Composable
private fun HistoryEditorSheet(
    date: LocalDate,
    record: DailyRecord,
    admissionTarget: Int,
    lightPlan: LightPlanState,
    onDismiss: () -> Unit,
    onSleepYes: () -> Unit,
    onSleepNo: () -> Unit,
    onMorningYes: () -> Unit,
    onMorningNo: () -> Unit,
    onLightYes: () -> Unit,
    onLightNo: () -> Unit,
    onDietYes: () -> Unit,
    onDietNo: () -> Unit,
    onWaterMinus: () -> Unit,
    onWaterPlus: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = SurfaceRaised, contentColor = Paper) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 34.dp)) {
            Text(date.format(DayFormatter).uppercase(Ru), color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(7.dp))
            Text("Редактировать день", color = Paper, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(22.dp))
            HistoryDecisionRow("УТРО", "$admissionTarget ${pushupWord(admissionTarget)} подряд", record.morning, onMorningYes, onMorningNo)
            Spacer(Modifier.height(12.dp))
            HistoryDecisionRow("СВЕТ", "День ${lightPlan.day}: ${lightPlan.task}", record.light, onLightYes, onLightNo)
            Spacer(Modifier.height(12.dp))
            HistoryDecisionRow("ПИТАНИЕ", "Без сладкого и без чипсов", record.diet, onDietYes, onDietNo)
            Spacer(Modifier.height(12.dp))
            WaterCard(SystemLogic.waterQuarters(record) ?: 0, onWaterMinus, onWaterPlus)
            Spacer(Modifier.height(12.dp))
            HistoryDecisionRow("СОН", "Отбой и кровать вовремя", record.sleep, onSleepYes, onSleepNo)
        }
    }
}

@Composable
private fun HistoryDecisionRow(
    title: String,
    detail: String,
    status: DecisionStatus?,
    onYes: () -> Unit,
    onNo: () -> Unit,
) {
    PremiumCard(color = SurfaceSoft) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Paper, style = MaterialTheme.typography.titleLarge)
                Text(detail, color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                when (status) { DecisionStatus.YES -> "ДА"; DecisionStatus.NO -> "НЕТ"; null -> "—" },
                color = when (status) { DecisionStatus.YES -> Acid; DecisionStatus.NO -> Danger; null -> Muted },
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            DecisionButton("ДА", status == DecisionStatus.YES, true, Modifier.weight(1f), onYes)
            DecisionButton("НЕТ", status == DecisionStatus.NO, false, Modifier.weight(1f), onNo)
        }
    }
}

@Composable
private fun StatsScreen(repository: SystemRepository) {
    var showWeeklyReport by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    if (showWeeklyReport) {
        WeeklyReportScreen(
            report = SystemLogic.weeklyReport(today, repository.records.values, repository.experimentFeedback),
            onBack = { showWeeklyReport = false },
        )
        return
    }
    val recentRecords = repository.records.values.filter {
        !it.date.isBefore(today.minusDays(29)) && !it.date.isAfter(today)
    }
    val sleep = SystemLogic.compliance(recentRecords) { it.sleep }
    val morning = SystemLogic.compliance(recentRecords) { it.morning }
    val light = SystemLogic.compliance(recentRecords) { it.light }
    val diet = SystemLogic.compliance(recentRecords) { it.diet }
    val water = SystemLogic.compliance(recentRecords) { it.water }
    val correlations = SystemLogic.correlationAnalysis(repository.records.values)
    val sleepReasons = ViolationReason.entries.associate { reason -> reason.label to
        recentRecords.sumOf { record ->
            if (record.sleep == DecisionStatus.NO && record.sleepReason == reason) 1 else 0
        }
    }
    val dietReasons = DietViolationReason.entries.associate { reason -> reason.label to
        recentRecords.count { it.diet == DecisionStatus.NO && it.dietReason == reason }
    }

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { ScreenHeader("Анализ", "30 дней", "Факты, данные и статистика.") }
        item {
            PremiumCard(
                modifier = Modifier.clickable { showWeeklyReport = true },
                color = Acid.copy(alpha = .09f),
                border = Acid.copy(alpha = .28f),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("НЕДЕЛЬНЫЙ ОТЧЁТ", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
                        Spacer(Modifier.height(7.dp))
                        Text("Управленческий бриф", color = Paper, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(4.dp))
                        Text("Результат, закономерности и одно решение на следующую неделю.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("→", color = Acid, style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricRing("УТРО", morning, Modifier.weight(1f))
                    MetricRing("СВЕТ", light, Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricRing("ПИТАНИЕ", diet, Modifier.weight(1f))
                    MetricRing("ВОДА", water, Modifier.weight(1f))
                    MetricRing("СОН", sleep, Modifier.weight(1f))
                }
            }
        }
        item { LastSevenDays(repository.records, today) }
        item { CorrelationAnalysisCard(correlations) }
        item { ReasonAnalysis("ПРИЧИНЫ НАРУШЕНИЯ СНА", sleepReasons) }
        item { ReasonAnalysis("ПРИЧИНЫ НАРУШЕНИЯ ПИТАНИЯ", dietReasons) }
        item {
            PremiumCard(color = Acid.copy(alpha = .09f), border = Acid.copy(alpha = .25f)) {
                Text("ПРИНЦИП", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.6.sp)
                Spacer(Modifier.height(10.dp))
                Text("Процент важнее серии", color = Paper, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(5.dp))
                Text("Один срыв не обнуляет прогресс. Система ищет повторяющуюся причину и возвращает к следующему решению.", color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun WeeklyReportScreen(report: WeeklyReport, onBack: () -> Unit) {
    val period = "${report.weekStart.format(DateTimeFormatter.ofPattern("d MMM", Ru))} — ${report.weekEnd.format(DateTimeFormatter.ofPattern("d MMM", Ru))}"
    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CircleTextButton("‹", "Назад", onBack)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("НЕДЕЛЬНЫЙ ОТЧЁТ", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.6.sp)
                    Text(period.uppercase(Ru), color = Muted, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        item {
            PremiumCard(color = SurfaceSoft, border = Acid.copy(alpha = .22f)) {
                Text("ВЫПОЛНЕНИЕ", color = Muted, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(8.dp))
                Text(report.overall?.let { "$it%" } ?: "—", color = Paper, style = MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    when (report.overall) {
                        null -> "Неделя ещё не содержит решений."
                        in 85..100 -> "Стандарт работает устойчиво."
                        in 65..84 -> "Основа есть. Нужна точечная корректировка."
                        else -> "Сейчас важнее восстановить управляемость, а не идеальность."
                    },
                    color = Muted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth()) {
                    report.metrics.forEachIndexed { index, metric ->
                        CompactMetric(metric.label, metric.value, Modifier.weight(1f))
                        if (index < report.metrics.lastIndex) Box(Modifier.width(1.dp).height(50.dp).background(Hairline))
                    }
                }
            }
        }
        item {
            PremiumCard {
                Text("КАРТИНА НЕДЕЛИ", color = Paper, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(18.dp))
                ReportFact("Сильная сторона", report.strongest ?: "Недостаточно данных", Acid)
                Spacer(Modifier.height(14.dp))
                ReportFact("Зона внимания", report.weakest ?: "Недостаточно данных", Amber)
                Spacer(Modifier.height(14.dp))
                ReportFact("Частая причина", report.topReason ?: "Причины не зафиксированы", Danger)
            }
        }
        item {
            PremiumCard {
                Text("НАБЛЮДЕНИЕ", color = Paper, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    report.insight ?: "Для надёжной взаимосвязи нужно не меньше ${SystemLogic.MIN_CORRELATION_DAYS} сопоставимых дней.",
                    color = Muted,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        item {
            PremiumCard(color = Acid.copy(alpha = .09f), border = Acid.copy(alpha = .28f)) {
                Text("РЕШЕНИЕ СЛЕДУЮЩЕЙ НЕДЕЛИ", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.4.sp)
                Spacer(Modifier.height(10.dp))
                Text(report.experiment.focus.title, color = Paper, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(7.dp))
                Text(report.experiment.focus.action, color = Muted, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(14.dp))
                Text(report.experiment.basis, color = Muted, style = MaterialTheme.typography.labelMedium)
                report.experimentFeedback?.let { feedback ->
                    Spacer(Modifier.height(10.dp))
                    Text("ИТОГ: ${feedback.label.uppercase(Ru)}", color = Acid, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun ReportFact(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(11.dp))
        Column {
            Text(label.uppercase(Ru), color = Muted, style = MaterialTheme.typography.labelMedium)
            Text(value, color = Paper, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun CorrelationAnalysisCard(analysis: com.personal.thesystem.model.CorrelationAnalysis) {
    PremiumCard {
        Text("ВЗАИМОСВЯЗИ", color = Paper, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.2.sp)
        Spacer(Modifier.height(5.dp))
        Text("Система сравнивает дни, но не выдаёт совпадение за причину.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(18.dp))

        if (analysis.insights.isEmpty()) {
            val remaining = (analysis.requiredDays - analysis.comparableDays).coerceAtLeast(0)
            Text(
                if (remaining > 0) "Нужно ещё $remaining сопоставимых дней." else "Нужны примеры как «ДА», так и «НЕТ», чтобы сравнение было честным.",
                color = Paper,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(7.dp).background(Hairline, CircleShape)) {
                Box(
                    Modifier
                        .fillMaxWidth((analysis.comparableDays / analysis.requiredDays.toFloat()).coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(Acid, CircleShape)
                )
            }
            Spacer(Modifier.height(7.dp))
            Text("${analysis.comparableDays.coerceAtMost(analysis.requiredDays)}/${analysis.requiredDays} дней", color = Muted, style = MaterialTheme.typography.labelMedium)
        } else {
            analysis.insights.forEachIndexed { index, insight ->
                if (index > 0) {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Hairline)
                    Spacer(Modifier.height(16.dp))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(insight.title, color = Acid, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.sp)
                    Text("${insight.sampleSize} ДНЕЙ", color = Muted, style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.height(9.dp))
                Text("${insight.yesLabel}: ${insight.yesRate}%", color = Paper, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Text("${insight.noLabel}: ${insight.noRate}%", color = Muted, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(7.dp))
                Text("Разница: ${abs(insight.yesRate - insight.noRate)} п. п.", color = Muted, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun MetricRing(label: String, value: Int?, modifier: Modifier) {
    val target = (value ?: 0) / 100f
    val animated by animateFloatAsState(target, spring(stiffness = Spring.StiffnessLow), label = "metric")
    PremiumCard(modifier = modifier) {
        BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(1.2f), contentAlignment = Alignment.Center) {
            val compact = maxWidth < 110.dp
            if (compact) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Box(Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.fillMaxSize().padding(2.dp)) {
                            val stroke = 4.dp.toPx()
                            drawArc(Hairline, -215f, 250f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                            drawArc(Acid, -215f, 250f * animated, false, style = Stroke(stroke, cap = StrokeCap.Round))
                        }
                        Text(
                            value?.let { "$it%" } ?: "—",
                            color = Paper,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            lineHeight = 18.sp,
                            maxLines = 1,
                        )
                    }
                    Text(label, color = Muted, style = MaterialTheme.typography.labelMedium, fontSize = 9.sp, maxLines = 1)
                }
            } else {
                Canvas(Modifier.fillMaxSize().padding(12.dp)) {
                    val stroke = 8.dp.toPx()
                    drawArc(Hairline, -215f, 250f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                    drawArc(Acid, -215f, 250f * animated, false, style = Stroke(stroke, cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value?.let { "$it%" } ?: "—", color = Paper, style = MaterialTheme.typography.headlineMedium, maxLines = 1)
                    Text(label, color = Muted, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun LastSevenDays(records: Map<LocalDate, DailyRecord>, today: LocalDate) {
    PremiumCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("ПОСЛЕДНИЕ 7 ДНЕЙ", color = Paper, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.2.sp)
            Text("5 ЗАДАЧ", color = Muted, style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth().height(116.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            (6 downTo 0).map { today.minusDays(it.toLong()) }.forEach { date ->
                val record = records[date]
                val answered = listOf(record?.morning, record?.light, record?.diet, record?.water, record?.sleep).count { it != null }
                val yes = listOf(record?.morning, record?.light, record?.diet, record?.water, record?.sleep).count { it == DecisionStatus.YES }
                val fraction = if (answered == 0) 0.04f else yes / 5f
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.width(30.dp)) {
                    Box(
                        Modifier
                            .width(9.dp)
                            .height((74 * max(.06f, fraction)).dp)
                            .background(if (answered == 0) Hairline else if (yes == 5) Acid else if (yes > 0) Amber else Danger, CircleShape)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(date.dayOfWeek.getDisplayName(TextStyle.SHORT, Ru).take(2).uppercase(Ru), color = if (date == today) Paper else Muted, style = MaterialTheme.typography.labelMedium, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun ReasonAnalysis(title: String, reasons: Map<String, Int>) {
    val maxCount = max(1, reasons.values.maxOrNull() ?: 0)
    val total = reasons.values.sum()
    PremiumCard {
        Text(title, color = Paper, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.2.sp)
        Spacer(Modifier.height(5.dp))
        Text(if (total == 0) "Пока нарушений с указанной причиной нет." else "$total зафиксированных причин за 30 дней", color = Muted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(20.dp))
        reasons.forEach { (label, count) ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(label, color = if (count > 0) Paper else Muted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(118.dp))
                Box(Modifier.weight(1f).height(7.dp).background(Hairline, CircleShape)) {
                    val fraction = count / maxCount.toFloat()
                    Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(if (count > 0) Danger else Hairline, CircleShape))
                }
                Text(count.toString(), color = if (count > 0) Paper else Muted, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.End, modifier = Modifier.width(30.dp))
            }
            Spacer(Modifier.height(13.dp))
        }
    }
}

@Composable
private fun SettingsScreen(
    settings: SystemSettings,
    records: Collection<DailyRecord>,
    onUpdate: ((SystemSettings) -> SystemSettings) -> Unit,
    onEnableNotifications: () -> Unit,
    exactAlarmsAllowed: Boolean,
) {
    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { ScreenHeader("Настройки", "Режим", "График уведомлений и напоминаний.") }
        item {
            SettingsGroup("РАСПИСАНИЕ") {
                TimeSettingRow("Цифровой отбой", "Телефон и ноутбук выключены", settings.digitalCutoff) { delta ->
                    onUpdate { it.copy(digitalCutoff = it.digitalCutoff.plusMinutes(delta.toLong())) }
                }
                SettingsDivider()
                TimeSettingRow("В кровати", "Свет выключен", settings.bedTime) { delta ->
                    onUpdate { it.copy(bedTime = it.bedTime.plusMinutes(delta.toLong())) }
                }
                SettingsDivider()
                TimeSettingRow("Утренний триггер", "Сразу после пробуждения", settings.morningTime) { delta ->
                    onUpdate { it.copy(morningTime = it.morningTime.plusMinutes(delta.toLong())) }
                }
                SettingsDivider()
                TimeSettingRow("Питание", "Напомнить отметить день", settings.dietTime) { delta ->
                    onUpdate { it.copy(dietTime = it.dietTime.plusMinutes(delta.toLong())) }
                }
            }
        }
        item {
            SettingsGroup("РЕЖИМ ВШЭ") {
                ToggleSettingRow(
                    "Учебный режим",
                    "По будням: первая пара, выход и главная задача",
                    settings.hseModeEnabled,
                ) { enabled -> onUpdate { it.copy(hseModeEnabled = enabled) } }
                if (settings.hseModeEnabled) {
                    SettingsDivider()
                    TimeSettingRow("Первая пара", "Единое время для будних дней", settings.hseFirstClassTime) { delta ->
                        onUpdate { it.copy(hseFirstClassTime = it.hseFirstClassTime.plusMinutes(delta.toLong())) }
                    }
                    SettingsDivider()
                    NumberSettingRow("Дорога", "Запас от дома до ВШЭ", settings.hseCommuteMinutes, "мин") { delta ->
                        onUpdate { it.copy(hseCommuteMinutes = (it.hseCommuteMinutes + delta).coerceIn(10, 180)) }
                    }
                }
            }
        }
        item {
            SettingsGroup("НАПОМИНАНИЯ") {
                ToggleSettingRow(
                    "Локальные напоминания",
                    when {
                        !settings.notificationsEnabled -> "Нужно разрешение Android"
                        !exactAlarmsAllowed -> "Разреши Android присылать их точно вовремя"
                        else -> "Приходят точно по расписанию"
                    },
                    settings.notificationsEnabled && exactAlarmsAllowed,
                ) { enabled ->
                    if (enabled) onEnableNotifications() else onUpdate { it.copy(notificationsEnabled = false) }
                }
                SettingsDivider()
                ToggleSettingRow("Предупреждение", "За 15 минут до отбоя", settings.warningEnabled) { value -> onUpdate { it.copy(warningEnabled = value) } }
                SettingsDivider()
                ToggleSettingRow("Цифровой отбой", SystemLogic.formatTime(settings.digitalCutoff), settings.cutoffEnabled) { value -> onUpdate { it.copy(cutoffEnabled = value) } }
                SettingsDivider()
                ToggleSettingRow("Подготовка ко сну", "За 30 минут до кровати", settings.preparationEnabled) { value -> onUpdate { it.copy(preparationEnabled = value) } }
                SettingsDivider()
                ToggleSettingRow("В кровати", SystemLogic.formatTime(settings.bedTime), settings.bedEnabled) { value -> onUpdate { it.copy(bedEnabled = value) } }
                SettingsDivider()
                ToggleSettingRow("Утренние отжимания", SystemLogic.formatTime(settings.morningTime), settings.morningEnabled) { value -> onUpdate { it.copy(morningEnabled = value) } }
                SettingsDivider()
                ToggleSettingRow("Питание", SystemLogic.formatTime(settings.dietTime), settings.dietEnabled) { value -> onUpdate { it.copy(dietEnabled = value) } }
            }
        }
        item {
            val admission = SystemLogic.admissionFor(LocalDate.now(), settings.admissionStart, records)
            SettingsGroup("РЕЖИМ ДОПУСКА") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text(if (admission.completed) "Стандарт активен" else "Уровень ${admission.level} из ${admission.totalLevels}", color = Paper, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(4.dp))
                        Text("10 → 12 → 14 → 16 → 18 → 20", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("${admission.target}", color = Acid, style = MaterialTheme.typography.displayMedium)
                }
                Spacer(Modifier.height(14.dp))
                Text("Уровень повышается только после отметки «УТРО — ДА». После успешного 14-го уровня приложение закрепляет правило: 20 отжиманий подряд каждое утро.", color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
        }
        item {
            Text("Все отметки и настройки хранятся только на этом устройстве. Регистрация и интернет не нужны.", color = Muted, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
        }
    }
}

@Composable
private fun SettingsGroup(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(label, color = Muted, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.6.sp, modifier = Modifier.padding(start = 4.dp, bottom = 9.dp))
        PremiumCard(content = content)
    }
}

@Composable
private fun TimeSettingRow(title: String, detail: String, value: LocalTime, onAdjust: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Paper, style = MaterialTheme.typography.titleMedium)
            Text(detail, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        MiniButton("−", "Уменьшить время") { onAdjust(-5) }
        Text(SystemLogic.formatTime(value), color = Acid, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.width(72.dp))
        MiniButton("+", "Увеличить время") { onAdjust(5) }
    }
}

@Composable
private fun NumberSettingRow(title: String, detail: String, value: Int, suffix: String, onAdjust: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Paper, style = MaterialTheme.typography.titleMedium)
            Text(detail, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        MiniButton("−", "Уменьшить") { onAdjust(-5) }
        Text("$value $suffix", color = Acid, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.width(72.dp))
        MiniButton("+", "Увеличить") { onAdjust(5) }
    }
}

@Composable
private fun MiniButton(text: String, description: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(SurfaceSoft)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Paper, fontSize = 20.sp)
    }
}

@Composable
private fun ToggleSettingRow(title: String, detail: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Paper, style = MaterialTheme.typography.titleMedium)
            Text(detail, color = Muted, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Ink,
                checkedTrackColor = Acid,
                uncheckedThumbColor = Muted,
                uncheckedTrackColor = SurfaceSoft,
                uncheckedBorderColor = Hairline,
            ),
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = Hairline, modifier = Modifier.padding(vertical = 15.dp))
}
