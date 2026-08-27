@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.personal.thesystem.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.personal.thesystem.R
import com.personal.thesystem.data.HSE_ROUTE_TIME
import com.personal.thesystem.data.HseCalendarEvent
import com.personal.thesystem.data.HseCalendarReader
import com.personal.thesystem.data.SystemRepository
import com.personal.thesystem.data.TransitOption
import com.personal.thesystem.data.TransitRoutesState
import com.personal.thesystem.data.YandexTransitController
import com.personal.thesystem.data.plannedMorningDate
import com.personal.thesystem.model.DailyRecord
import com.personal.thesystem.model.DailyTask
import com.personal.thesystem.model.DecisionStatus
import com.personal.thesystem.model.DietViolationReason
import com.personal.thesystem.model.AssignmentPriority
import com.personal.thesystem.model.ExperimentFeedback
import com.personal.thesystem.model.LightPlanState
import com.personal.thesystem.model.MoneyCategory
import com.personal.thesystem.model.MoneyPeriod
import com.personal.thesystem.model.MoneyReport
import com.personal.thesystem.model.MoneySnapshot
import com.personal.thesystem.model.MoneyStatus
import com.personal.thesystem.model.MoneyTransaction
import com.personal.thesystem.model.SystemLogic
import com.personal.thesystem.model.SystemSettings
import com.personal.thesystem.model.StudyAssignment
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
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private val Ru = Locale("ru")
private val DayFormatter = DateTimeFormatter.ofPattern("d MMMM", Ru)

private enum class AppTab(val label: String) {
    TODAY("Сегодня"),
    HISTORY("История"),
    MONEY("Деньги"),
    STATS("Анализ"),
    SETTINGS("Настройки"),
}

private fun AppTab.label(hseMode: Boolean): String = if (!hseMode) label else when (this) {
    AppTab.TODAY -> "День"
    AppTab.HISTORY -> "Задания"
    AppTab.MONEY -> "Деньги"
    AppTab.STATS -> "Итоги"
    AppTab.SETTINGS -> "Настройки"
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
    var showPersonalSystem by remember { mutableStateOf(false) }
    var showDisciplineHistory by remember { mutableStateOf(false) }
    var pendingReason by remember { mutableStateOf<Pair<LocalDate, DecisionKind>?>(null) }
    var emojiAfterReason by remember { mutableStateOf(false) }
    var confettiBurst by remember { mutableIntStateOf(0) }
    var celebrationKind by remember { mutableStateOf(DecisionKind.MORNING) }
    var emojiBurst by remember { mutableIntStateOf(0) }
    var exactAlarmsAllowed by remember { mutableStateOf(ReminderScheduler.canScheduleExactly(context)) }
    var calendarAccessAllowed by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        )
    }
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    val launchDate = remember { LocalDate.now() }
    val previousDate = remember(launchDate) { launchDate.minusDays(1) }
    var showPreviousDayReminder by remember {
        mutableStateOf(repository.shouldShowPreviousDayReminder(launchDate))
    }
    val modeTransitionProgress = remember { Animatable(1f) }
    var modeTransitionTarget by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            currentTime = LocalTime.now()
        }
    }
    val hseMode = repository.settings.hseModeEnabled
    val targetBackground = when {
        !currentTime.isBefore(repository.settings.digitalCutoff) -> NightInk
        hseMode -> Color(0xFF08121C)
        currentTime.hour < 12 -> MorningInk
        else -> Ink
    }
    val appBackground by animateColorAsState(targetBackground, tween(900), label = "dayBackground")

    fun applySettings(transform: (SystemSettings) -> SystemSettings) {
        repository.updateSettings(transform)
        ReminderScheduler.scheduleAll(context, repository.settings)
    }

    fun toggleHseModeAnimated() {
        if (modeTransitionTarget != null) return
        val target = !repository.settings.hseModeEnabled
        scope.launch {
            modeTransitionProgress.snapTo(0f)
            modeTransitionTarget = target
            applySettings { it.copy(hseModeEnabled = target) }
            modeTransitionProgress.animateTo(
                1f,
                tween(durationMillis = 900, easing = FastOutSlowInEasing),
            )
            modeTransitionTarget = null
        }
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

    val calendarLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> calendarAccessAllowed = granted }

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
                    onSelect = {
                        selectedTab = it
                        showPersonalSystem = false
                        showDisciplineHistory = false
                    },
                    background = appBackground,
                    hseMode = hseMode,
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
                    AppTab.TODAY -> if (hseMode && !showPersonalSystem) HseDashboardScreen(
                        repository = repository,
                        currentTime = currentTime,
                        calendarAccessAllowed = calendarAccessAllowed,
                        onRequestCalendarAccess = { calendarLauncher.launch(Manifest.permission.READ_CALENDAR) },
                        onToggleHseMode = ::toggleHseModeAnimated,
                        onOpenSystem = { showPersonalSystem = true },
                        onOpenSettings = { selectedTab = AppTab.SETTINGS },
                    ) else TodayScreen(
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
                        onToggleHseMode = ::toggleHseModeAnimated,
                        onBack = if (hseMode) ({ showPersonalSystem = false }) else null,
                    )
                    AppTab.HISTORY -> if (hseMode) AssignmentsScreen(repository) else HistoryScreen(
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
                    AppTab.MONEY -> MoneyScreen(repository)
                    AppTab.STATS -> if (hseMode && showDisciplineHistory) HistoryScreen(
                        repository = repository,
                        onNo = { date, kind ->
                            when (kind) {
                                DecisionKind.MORNING -> { repository.setMorning(date, DecisionStatus.NO); emojiBurst += 1 }
                                DecisionKind.LIGHT -> { repository.setLight(date, DecisionStatus.NO); emojiBurst += 1 }
                                DecisionKind.WATER -> { repository.setWater(date, DecisionStatus.NO); emojiBurst += 1 }
                                else -> { emojiAfterReason = true; pendingReason = date to kind }
                            }
                        },
                        onCelebrate = { kind -> celebrationKind = kind; confettiBurst += 1 },
                        onExit = { showDisciplineHistory = false },
                    ) else StatsScreen(
                        repository = repository,
                        hseMode = hseMode,
                        onOpenHistory = { showDisciplineHistory = true },
                    )
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
        if (showPreviousDayReminder) {
            PreviousDayReminderScreen(
                repository = repository,
                date = previousDate,
                onYes = { task ->
                    val kind = when (task) {
                        DailyTask.MORNING -> { repository.setMorning(previousDate, DecisionStatus.YES); DecisionKind.MORNING }
                        DailyTask.LIGHT -> { repository.setLight(previousDate, DecisionStatus.YES); DecisionKind.LIGHT }
                        DailyTask.DIET -> { repository.setDiet(previousDate, DecisionStatus.YES); DecisionKind.DIET }
                        DailyTask.WATER -> { repository.setWater(previousDate, DecisionStatus.YES); DecisionKind.WATER }
                        DailyTask.SLEEP -> { repository.setSleep(previousDate, DecisionStatus.YES); DecisionKind.SLEEP }
                    }
                    celebrationKind = kind
                    confettiBurst += 1
                },
                onNo = { task ->
                    when (task) {
                        DailyTask.MORNING -> { repository.setMorning(previousDate, DecisionStatus.NO); emojiBurst += 1 }
                        DailyTask.LIGHT -> { repository.setLight(previousDate, DecisionStatus.NO); emojiBurst += 1 }
                        DailyTask.WATER -> { repository.setWater(previousDate, DecisionStatus.NO); emojiBurst += 1 }
                        DailyTask.DIET -> { emojiAfterReason = true; pendingReason = previousDate to DecisionKind.DIET }
                        DailyTask.SLEEP -> { emojiAfterReason = true; pendingReason = previousDate to DecisionKind.SLEEP }
                    }
                },
                onDone = {
                    repository.markPreviousDayReminderShown(launchDate)
                    showPreviousDayReminder = false
                },
                modifier = Modifier.fillMaxSize().zIndex(19f),
            )
        }
        modeTransitionTarget?.let { targetHseMode ->
            ModeTransitionOverlay(
                progress = modeTransitionProgress.value,
                targetHseMode = targetHseMode,
                modifier = Modifier.fillMaxSize().zIndex(40f),
            )
        }
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
private fun SystemBottomBar(selected: AppTab, onSelect: (AppTab) -> Unit, background: Color, hseMode: Boolean) {
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
                    tab.label(hseMode),
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
            AppTab.MONEY -> {
                drawRoundRect(
                    color,
                    topLeft = androidx.compose.ui.geometry.Offset(3.dp.toPx(), 5.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(16.dp.toPx(), 13.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
                    style = stroke,
                )
                drawLine(color, androidx.compose.ui.geometry.Offset(3.dp.toPx(), 9.dp.toPx()), androidx.compose.ui.geometry.Offset(19.dp.toPx(), 9.dp.toPx()), stroke.width)
                drawCircle(color, 1.5.dp.toPx(), androidx.compose.ui.geometry.Offset(15.5.dp.toPx(), 13.5.dp.toPx()))
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
    onToggleHseMode: () -> Unit,
    onBack: (() -> Unit)? = null,
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
                if (onBack == null) {
                    ScreenHeader("The System", today.format(DayFormatter).replaceFirstChar { it.uppercase() })
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircleTextButton("‹", "Назад в режим ВШЭ", onBack)
                        Spacer(Modifier.width(13.dp))
                        ScreenHeader("Личная система", "Сегодня")
                    }
                }
                SystemMark(answered / 5f, onToggleHseMode)
            }
        }

        item { DailyStatement(answered, record) }

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
private fun SystemMark(progress: Float, onToggleHseMode: () -> Unit) {
    val animated by animateFloatAsState(progress, spring(stiffness = Spring.StiffnessLow), label = "systemMark")
    Box(
        Modifier
            .size(54.dp)
            .clip(CircleShape)
            .clickable(onClick = onToggleHseMode)
            .semantics {
                role = Role.Button
                contentDescription = "Включить или выключить режим ВШЭ"
            },
        contentAlignment = Alignment.Center,
    ) {
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
private fun ModeTransitionOverlay(
    progress: Float,
    targetHseMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val interaction = remember { MutableInteractionSource() }
    val eased = FastOutSlowInEasing.transform(progress)
    val endRadius = if (targetHseMode) 21.dp else 27.dp

    BoxWithConstraints(
        modifier.clickable(
            interactionSource = interaction,
            indication = null,
            onClick = {},
        )
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val endRadiusPx = with(density) { endRadius.toPx() }
        val centerX = widthPx - with(density) { (20.dp + endRadius).toPx() }
        val centerY = WindowInsets.statusBars.getTop(density).toFloat() +
            with(density) { (18.dp + endRadius).toPx() }
        val startRadius = maxOf(
            hypot(centerX, centerY),
            hypot(widthPx - centerX, centerY),
            hypot(centerX, heightPx - centerY),
            hypot(widthPx - centerX, heightPx - centerY),
        )
        val radius = startRadius + (endRadiusPx - startRadius) * eased

        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFFE5E7EB),
                radius = radius,
                center = androidx.compose.ui.geometry.Offset(centerX, centerY),
            )
        }
        Box(
            Modifier
                .offset {
                    IntOffset(
                        (centerX - endRadiusPx).roundToInt(),
                        (centerY - endRadiusPx).roundToInt(),
                    )
                }
                .size(endRadius * 2)
                .graphicsLayer {
                    alpha = ((eased - .62f) / .38f).coerceIn(0f, 1f)
                    scaleX = .82f + alpha * .18f
                    scaleY = scaleX
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (targetHseMode) "В" else "S",
                color = Color(0xFF11151A),
                fontWeight = FontWeight.Black,
                fontSize = if (targetHseMode) 20.sp else 19.sp,
            )
        }
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
private fun HseDashboardScreen(
    repository: SystemRepository,
    currentTime: LocalTime,
    calendarAccessAllowed: Boolean,
    onRequestCalendarAccess: () -> Unit,
    onToggleHseMode: () -> Unit,
    onOpenSystem: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val settings = repository.settings
    val today = LocalDate.now()
    val calendarEvents = remember(calendarAccessAllowed, today, currentTime.hour, currentTime.minute) {
        if (calendarAccessAllowed) HseCalendarReader.eventsFor(context, today) else emptyList()
    }
    val nextLesson = calendarEvents.firstOrNull { !it.end.isBefore(currentTime) }
    val record = repository.recordFor(today)
    val statuses = listOf(record.morning, record.light, record.diet, record.water, record.sleep)
    val completed = statuses.count { it == DecisionStatus.YES }
    val evening = !currentTime.isBefore(settings.digitalCutoff)
    val transitController = remember { YandexTransitController() }
    val transitState = transitController.state
    val savedTransitPlan = repository.hseTransitPlan
    val transitDate = plannedMorningDate(today, currentTime, savedTransitPlan?.targetDate)

    LaunchedEffect(
        settings.hseHomeAddress,
        settings.hseUniversityAddress,
        transitDate,
    ) {
        if (settings.hseHomeAddress.isNotBlank()) {
            val saved = repository.hseTransitPlan
            if (
                saved?.targetDate == transitDate &&
                saved.homeAddress == settings.hseHomeAddress &&
                saved.universityAddress == settings.hseUniversityAddress
            ) {
                transitController.showSaved(saved)
            } else {
                transitController.refresh(
                    settings.hseHomeAddress,
                    settings.hseUniversityAddress,
                    transitDate,
                    repository::saveHseTransitPlan,
                )
            }
        }
    }

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 30.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                ScreenHeader("Режим ВШЭ", today.format(DayFormatter).replaceFirstChar { it.uppercase() }, "Учёба, дорога и личная система.")
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF142A3B))
                        .clickable(onClick = onToggleHseMode)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Выключить режим ВШЭ"
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("В", color = Acid, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        if (evening) {
            item {
                PremiumCard(color = Color(0xFF10151B), border = Acid.copy(alpha = .22f)) {
                    Text("УЧЕБНЫЙ ДЕНЬ ЗАВЕРШЁН", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.4.sp)
                    Spacer(Modifier.height(9.dp))
                    Text("Сейчас приоритет — сон.", color = Paper, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(5.dp))
                    Text("Расписание и дорога приглушены до утра. Личная система остаётся доступна.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            PremiumCard(
                color = Color(0xFF10202D).copy(alpha = if (evening) .55f else 1f),
                border = Acid.copy(alpha = if (evening) .12f else .32f),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("СЛЕДУЮЩАЯ ПАРА", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.4.sp)
                    Text(nextLesson?.start?.let(SystemLogic::formatTime) ?: "—", color = Paper, style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(15.dp))
                when {
                    !calendarAccessAllowed -> {
                        Text("Подключи календарь ВШЭ", color = Paper, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(6.dp))
                        Text("The System прочитает только события календаря Android. Пароль ВШЭ приложению не нужен.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        HseActionButton("ПОДКЛЮЧИТЬ КАЛЕНДАРЬ", onClick = onRequestCalendarAccess)
                    }
                    nextLesson == null -> {
                        Text(
                            if (calendarEvents.isEmpty()) "На сегодня занятий нет" else "На сегодня занятий больше нет",
                            color = Paper,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Spacer(Modifier.height(6.dp))
                        if (calendarEvents.isEmpty()) {
                            Text("Если расписание уже опубликовано, экспортируй его в календарь из HSE App X.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    else -> {
                        Text(nextLesson.title, color = Paper, style = MaterialTheme.typography.headlineMedium)
                        if (nextLesson.location.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(nextLesson.location, color = Muted, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
        item {
            PremiumCard(color = Color(0xFF0E1B25).copy(alpha = if (evening) .55f else 1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Text("АВТОБУС В ВШЭ", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.4.sp)
                    Text(
                        "${if (transitDate == today) "СЕГОДНЯ" else "ЗАВТРА"} · ${SystemLogic.formatTime(HSE_ROUTE_TIME)}",
                        color = Paper,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Spacer(Modifier.height(15.dp))
                if (settings.hseHomeAddress.isBlank()) {
                    Text("Укажи домашний адрес — он сохранится только на этом устройстве.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    HseActionButton("УКАЗАТЬ АДРЕС", onClick = onOpenSettings)
                } else {
                    when (val routeState = transitState) {
                        TransitRoutesState.Idle,
                        TransitRoutesState.Loading -> {
                            Text("Собираю понятный маршрут на 08:30…", color = Paper, style = MaterialTheme.typography.bodyMedium)
                        }
                        is TransitRoutesState.Ready -> {
                            TransitOptionRow(routeState.plan.route)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Яндекс Карты · план сохранён на ${routeState.plan.targetDate.format(DayFormatter)}",
                                color = Muted,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 12.sp,
                            )
                        }
                        is TransitRoutesState.Failed -> {
                            Text(routeState.message, color = Danger, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "ОБНОВИТЬ",
                                color = Acid,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.clickable {
                                    transitController.refresh(
                                        settings.hseHomeAddress,
                                        settings.hseUniversityAddress,
                                        transitDate,
                                        repository::saveHseTransitPlan,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
        item {
            PremiumCard(color = Color(0xFF0D1822).copy(alpha = if (evening) .55f else 1f)) {
                Text("РАСПИСАНИЕ ДНЯ", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.4.sp)
                Spacer(Modifier.height(13.dp))
                when {
                    !calendarAccessAllowed -> Text("Дай доступ к календарю в карточке выше.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    calendarEvents.isEmpty() -> Text("В календаре нет занятий на этот день.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    else -> calendarEvents.forEachIndexed { index, event ->
                        HseLessonRow(event)
                        if (index != calendarEvents.lastIndex) Spacer(Modifier.height(14.dp))
                    }
                }
            }
        }
        item {
            PremiumCard(
                modifier = Modifier.clickable(onClick = onOpenSystem),
                color = SurfaceRaised,
                border = if (completed < 5) Acid.copy(alpha = .34f) else Hairline,
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("ЛИЧНАЯ СИСТЕМА", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.4.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("$completed из 5 выполнено", color = Paper, style = MaterialTheme.typography.titleLarge)
                    }
                    Text("→", color = Acid, style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    statuses.forEach { status ->
                        Box(
                            Modifier.weight(1f).height(5.dp).background(
                                when (status) {
                                    DecisionStatus.YES -> Acid
                                    DecisionStatus.NO -> Danger
                                    null -> Hairline
                                }, CircleShape
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HseLessonRow(event: HseCalendarEvent) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.width(70.dp)) {
            Text(SystemLogic.formatTime(event.start), color = Paper, style = MaterialTheme.typography.titleLarge)
            Text(SystemLogic.formatTime(event.end), color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        Box(Modifier.size(7.dp).background(Acid, CircleShape))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(event.title, color = Paper, style = MaterialTheme.typography.titleMedium)
            if (event.location.isNotBlank()) {
                Text(event.location, color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun TransitOptionRow(option: TransitOption) {
    Column(Modifier.fillMaxWidth()) {
        val busTitle = if (option.lines.startsWith("автобус", ignoreCase = true)) {
            option.lines
        } else {
            "Автобус ${option.lines}"
        }
        Text(busTitle, color = Paper, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(13.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("БУДЕТ НА ОСТАНОВКЕ", color = Muted, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(3.dp))
                Text(option.busArrivalTime, color = Acid, style = MaterialTheme.typography.titleLarge)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("ВСЯ ДОРОГА", color = Muted, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(3.dp))
                Text("${option.totalMinutes} мин", color = Paper, style = MaterialTheme.typography.titleLarge)
            }
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Hairline)
        Spacer(Modifier.height(14.dp))
        TransitStopRow("СЕСТЬ", option.boardingStop)
        Spacer(Modifier.height(11.dp))
        Text("↓", color = Acid, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 4.dp))
        Spacer(Modifier.height(11.dp))
        TransitStopRow("ВЫЙТИ", option.exitStop)
    }
}

@Composable
private fun TransitStopRow(label: String, stop: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(label, color = Acid, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(64.dp))
        Text(stop, color = Paper, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun HseActionButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(15.dp))
            .background(if (enabled) Acid else Hairline).clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (enabled) Ink else Muted, style = MaterialTheme.typography.labelLarge)
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
private fun MoneyScreen(repository: SystemRepository) {
    val today = LocalDate.now()
    val period = SystemLogic.moneyPeriodFor(today)
    val snapshot = SystemLogic.moneySnapshot(today, repository.moneyTransactions, repository.moneyReceivedPeriods)
    val periodTransactions = repository.moneyTransactions.filter {
        !it.date.isBefore(period.start) && !it.date.isAfter(period.end)
    }
    val previousPeriod = SystemLogic.previousMoneyPeriod(today)
    val previousHasData = !previousPeriod.start.isBefore(SystemLogic.MONEY_START_DATE) &&
        (previousPeriod.start in repository.moneyReceivedPeriods || repository.moneyTransactions.any {
            !it.date.isBefore(previousPeriod.start) && !it.date.isAfter(previousPeriod.end)
        })
    var addingExpense by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<Long?>(null) }
    var revokeTransferArmed by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeader("Money", "Финансы", "До следующего перевода — без сюрпризов.")
        }
        if (today.isBefore(SystemLogic.MONEY_START_DATE)) {
            item {
                PremiumCard(color = Acid.copy(alpha = .07f), border = Acid.copy(alpha = .30f)) {
                    Text("СТАРТ 1 СЕНТЯБРЯ", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.6.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("20 000 ₽ на полмесяца", color = Paper, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(7.dp))
                    val days = ChronoUnit.DAYS.between(today, SystemLogic.MONEY_START_DATE)
                    Text("До первого периода — $days ${dayWord(days)}. После перевода нажми «Перевод получен».", color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            item { MoneyHero(snapshot, today) }
            item {
                if (!snapshot.transferReceived) {
                    PremiumCard(color = Acid.copy(alpha = .06f), border = Acid.copy(alpha = .28f)) {
                        Text("ПЕРЕВОД", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
                        Spacer(Modifier.height(9.dp))
                        Text("Подтверди получение 20 000 ₽", color = Paper, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(5.dp))
                        Text("Сумма появится в бюджете только после фактического перевода.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))
                        HseActionButton("ПЕРЕВОД ПОЛУЧЕН") {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            repository.confirmMoneyTransfer(period.start)
                        }
                    }
                } else {
                    PremiumCard(color = SurfaceSoft) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(34.dp).background(Acid, CircleShape), contentAlignment = Alignment.Center) {
                                Text("✓", color = Ink, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("20 000 ₽ ПОЛУЧЕНЫ", color = Paper, style = MaterialTheme.typography.labelLarge)
                                Text(moneyPeriodLabel(period), color = Muted, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                if (revokeTransferArmed) "ОТМЕНИТЬ?" else "ИСПРАВИТЬ",
                                color = if (revokeTransferArmed) Danger else Muted,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable {
                                    if (revokeTransferArmed) {
                                        repository.revokeMoneyTransfer(period.start)
                                        revokeTransferArmed = false
                                    } else {
                                        revokeTransferArmed = true
                                    }
                                }.padding(8.dp),
                            )
                        }
                    }
                }
            }
            item {
                HseActionButton(
                    label = "+ ДОБАВИТЬ РАСХОД",
                    enabled = snapshot.transferReceived || snapshot.carryIn > 0L,
                ) { addingExpense = true }
            }
            item { MoneyPeriodSummary(snapshot) }
            item {
                SectionLabel("ОПЕРАЦИИ", if (periodTransactions.isEmpty()) "ПОКА ПУСТО" else "${periodTransactions.size}")
            }
            if (periodTransactions.isEmpty()) {
                item {
                    PremiumCard(color = SurfaceRaised) {
                        Text("Расходов пока нет", color = Paper, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(5.dp))
                        Text("Добавляй их сразу после оплаты — так прогноз останется точным.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                item {
                    PremiumCard(color = SurfaceRaised) {
                        periodTransactions.sortedByDescending { it.id }.take(15).forEachIndexed { index, transaction ->
                            MoneyTransactionRow(
                                transaction = transaction,
                                today = today,
                                deleteArmed = deleteCandidate == transaction.id,
                                onDelete = {
                                    if (deleteCandidate == transaction.id) {
                                        repository.deleteMoneyExpense(transaction.id)
                                        deleteCandidate = null
                                    } else {
                                        deleteCandidate = transaction.id
                                    }
                                },
                            )
                            if (index != periodTransactions.take(15).lastIndex) {
                                HorizontalDivider(color = Hairline, modifier = Modifier.padding(vertical = 12.dp))
                            }
                        }
                    }
                }
            }
            if (previousHasData) {
                item {
                    MoneyReportCard(
                        SystemLogic.moneyReport(previousPeriod, repository.moneyTransactions, repository.moneyReceivedPeriods)
                    )
                }
            }
        }
    }

    if (addingExpense) {
        AddMoneyExpenseSheet(
            onDismiss = { addingExpense = false },
            onAdd = { amount, category, planned ->
                repository.addMoneyExpense(amount, category, planned)
                addingExpense = false
            },
        )
    }
}

@Composable
private fun MoneyHero(snapshot: MoneySnapshot, today: LocalDate) {
    val statusColor = when (snapshot.status) {
        MoneyStatus.WAITING -> Muted
        MoneyStatus.CALM -> Acid
        MoneyStatus.WATCH -> Amber
        MoneyStatus.SAVE -> Danger
    }
    val statusLabel = when (snapshot.status) {
        MoneyStatus.WAITING -> "ОЖИДАЕТ ПЕРЕВОДА"
        MoneyStatus.CALM -> "СПОКОЙНЫЙ ТЕМП"
        MoneyStatus.WATCH -> "ВНИМАНИЕ К ТЕМПУ"
        MoneyStatus.SAVE -> "РЕЖИМ СОХРАНЕНИЯ"
    }
    val totalAvailable = snapshot.carryIn + if (snapshot.transferReceived) SystemLogic.MONEY_TRANSFER_RUBLES else 0L
    val balanceProgress = if (totalAvailable <= 0L) 0f else (snapshot.balance.toFloat() / totalAvailable).coerceIn(0f, 1f)
    val daysRemaining = ChronoUnit.DAYS.between(today.coerceIn(snapshot.period.start, snapshot.period.end), snapshot.period.end) + 1L
    PremiumCard(color = statusColor.copy(alpha = .07f), border = statusColor.copy(alpha = .42f)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(statusLabel, color = statusColor, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
            Text("$daysRemaining ${dayWord(daysRemaining)}", color = Muted, style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(17.dp))
        Text("ОСТАЛОСЬ", color = Muted, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(3.dp))
        Text(formatMoney(snapshot.balance), color = Paper, style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(15.dp))
        Box(Modifier.fillMaxWidth().height(7.dp).background(Hairline, CircleShape)) {
            Box(Modifier.fillMaxWidth(balanceProgress).fillMaxHeight().background(statusColor, CircleShape))
        }
        Spacer(Modifier.height(17.dp))
        Row(Modifier.fillMaxWidth()) {
            MoneyHeroMetric("МОЖНО В ДЕНЬ", formatMoney(snapshot.safePerDay), Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(42.dp).background(Hairline))
            MoneyHeroMetric("РЕЗЕРВ", formatMoney(snapshot.reserveTarget), Modifier.weight(1f))
        }
        Spacer(Modifier.height(15.dp))
        Text(moneyForecastText(snapshot), color = statusColor, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MoneyHeroMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Muted, style = MaterialTheme.typography.labelMedium, fontSize = 9.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = Paper, style = MaterialTheme.typography.titleMedium, maxLines = 1)
    }
}

@Composable
private fun MoneyPeriodSummary(snapshot: MoneySnapshot) {
    PremiumCard(color = SurfaceSoft) {
        Text("ТЕКУЩИЙ ПЕРИОД", color = Muted, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(13.dp))
        Row(Modifier.fillMaxWidth()) {
            MoneySmallMetric("ПОТРАЧЕНО", snapshot.spent, Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(48.dp).background(Hairline))
            MoneySmallMetric("СПОНТАННО", snapshot.unplannedSpent, Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(48.dp).background(Hairline))
            MoneySmallMetric("ДОСТУПНО", snapshot.safeToSpend.coerceAtLeast(0L), Modifier.weight(1f))
        }
        snapshot.topCategory?.let {
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Hairline)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Больше всего", color = Muted, style = MaterialTheme.typography.bodyMedium)
                Text(it.label, color = Paper, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun MoneySmallMetric(label: String, value: Long, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(formatMoney(value), color = Paper, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(label, color = Muted, style = MaterialTheme.typography.labelMedium, fontSize = 8.sp, maxLines = 1)
    }
}

@Composable
private fun MoneyTransactionRow(
    transaction: MoneyTransaction,
    today: LocalDate,
    deleteArmed: Boolean,
    onDelete: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).background(if (transaction.planned) Acid.copy(alpha = .12f) else Danger.copy(alpha = .11f), CircleShape), contentAlignment = Alignment.Center) {
            Text(if (transaction.planned) "П" else "С", color = if (transaction.planned) Acid else Danger, style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(transaction.category.label, color = Paper, style = MaterialTheme.typography.titleMedium)
            Text(
                if (transaction.date == today) "Сегодня" else transaction.date.format(DateTimeFormatter.ofPattern("d MMMM", Ru)),
                color = Muted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("−${formatMoney(transaction.amountRubles)}", color = Paper, style = MaterialTheme.typography.titleMedium)
            Text(
                if (deleteArmed) "УДАЛИТЬ?" else "×",
                color = if (deleteArmed) Danger else Muted,
                fontSize = if (deleteArmed) 9.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onDelete).padding(horizontal = 7.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun MoneyReportCard(report: MoneyReport) {
    PremiumCard(color = Acid.copy(alpha = .05f), border = Acid.copy(alpha = .25f)) {
        Text("ОТЧЁТ · ${moneyPeriodLabel(report.period).uppercase(Ru)}", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.3.sp)
        Spacer(Modifier.height(12.dp))
        Text(if (report.endingBalance >= 0L) "Деньги дожили до перевода" else "Период закрыт с дефицитом", color = Paper, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        MoneyReportRow("Получено", report.income)
        MoneyReportRow("Потрачено", report.spent)
        MoneyReportRow("Перенесено", report.endingBalance)
        MoneyReportRow("Спонтанные расходы", report.unplannedSpent)
        report.topCategory?.let {
            Spacer(Modifier.height(8.dp))
            Text("Главная категория — ${it.label.lowercase(Ru)}.", color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun MoneyReportRow(label: String, amount: Long) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted, style = MaterialTheme.typography.bodyMedium)
        Text(formatMoney(amount), color = Paper, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun AddMoneyExpenseSheet(
    onDismiss: () -> Unit,
    onAdd: (Long, MoneyCategory, Boolean) -> Unit,
) {
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MoneyCategory.GROCERIES) }
    var planned by remember { mutableStateOf(true) }
    val amountValue = amount.toLongOrNull()?.takeIf { it in 1L..1_000_000L }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceRaised,
        contentColor = Paper,
        dragHandle = { Box(Modifier.padding(top = 11.dp).size(42.dp, 4.dp).background(Hairline, CircleShape)) },
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("НОВЫЙ РАСХОД", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.6.sp)
            BasicTextField(
                value = amount,
                onValueChange = { amount = it.filter(Char::isDigit).take(7) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.displayMedium.copy(color = Paper),
                cursorBrush = SolidColor(Acid),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceSoft).padding(16.dp),
                decorationBox = { inner ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) {
                            if (amount.isBlank()) Text("0", color = Muted, style = MaterialTheme.typography.displayMedium)
                            inner()
                        }
                        Text("₽", color = Acid, style = MaterialTheme.typography.headlineLarge)
                    }
                },
            )
            Text("КАТЕГОРИЯ", color = Muted, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.3.sp)
            MoneyCategory.entries.chunked(2).forEach { rowCategories ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    rowCategories.forEach { option ->
                        val selected = category == option
                        Box(
                            Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(13.dp))
                                .background(if (selected) Acid else SurfaceSoft)
                                .border(1.dp, if (selected) Acid else Hairline, RoundedCornerShape(13.dp))
                                .clickable { category = option },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(option.label, color = if (selected) Ink else Paper, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                        }
                    }
                    if (rowCategories.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            Text("ТИП РАСХОДА", color = Muted, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.3.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                listOf(true to "ПО ПЛАНУ", false to "СПОНТАННО").forEach { (value, label) ->
                    val selected = planned == value
                    Box(
                        Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(13.dp))
                            .background(if (selected) Acid else SurfaceSoft)
                            .clickable { planned = value },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(label, color = if (selected) Ink else Muted, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            HseActionButton("СОХРАНИТЬ РАСХОД", enabled = amountValue != null) {
                amountValue?.let { onAdd(it, category, planned) }
            }
        }
    }
}

private fun moneyForecastText(snapshot: MoneySnapshot): String = when {
    !snapshot.transferReceived -> "Подтверди перевод — после этого появится точный дневной темп."
    snapshot.balance < snapshot.reserveTarget -> "Резерв уже затронут. Сократи необязательные расходы до следующего перевода."
    snapshot.spent == 0L -> "Темп сформируется после первого расхода."
    snapshot.projectedSafeEnd != null && !snapshot.projectedSafeEnd.isAfter(snapshot.period.end) ->
        "Безопасная часть бюджета закончится ${snapshot.projectedSafeEnd.format(DateTimeFormatter.ofPattern("d MMMM", Ru))}."
    else -> "При текущем темпе денег хватает до следующего перевода."
}

private fun moneyPeriodLabel(period: MoneyPeriod): String =
    "${period.start.dayOfMonth}–${period.end.format(DateTimeFormatter.ofPattern("d MMMM", Ru))}"

private fun formatMoney(value: Long): String = "${NumberFormat.getIntegerInstance(Ru).format(value)} ₽"

private fun dayWord(value: Long): String = when {
    value % 100 in 11L..14L -> "дней"
    value % 10 == 1L -> "день"
    value % 10 in 2L..4L -> "дня"
    else -> "дней"
}

@Composable
private fun AssignmentsScreen(repository: SystemRepository) {
    var adding by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val active = repository.assignments.filterNot { it.completed }
    val completed = repository.assignments.filter { it.completed }

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 30.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                ScreenHeader("Режим ВШЭ", "Задания", "Дедлайны вместо календаря дисциплины.")
                CircleTextButton("+", "Добавить задание", onClick = { adding = true })
            }
        }
        if (active.isEmpty()) {
            item {
                PremiumCard(color = Color(0xFF0F1B25), border = Acid.copy(alpha = .24f)) {
                    Text("СПИСОК ЧИСТ", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.4.sp)
                    Spacer(Modifier.height(10.dp))
                    Text("Заданий пока нет", color = Paper, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(5.dp))
                    Text("Добавь первое — оно останется только на этом устройстве.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(14.dp))
                    HseActionButton("ДОБАВИТЬ ЗАДАНИЕ") { adding = true }
                }
            }
        } else {
            val overdue = active.filter { it.dueDate.isBefore(today) }
            val todayItems = active.filter { it.dueDate == today }
            val week = active.filter { it.dueDate.isAfter(today) && !it.dueDate.isAfter(today.plusDays(7)) }
            val later = active.filter { it.dueDate.isAfter(today.plusDays(7)) }
            listOf(
                "ПРОСРОЧЕНО" to overdue,
                "СЕГОДНЯ" to todayItems,
                "БЛИЖАЙШИЕ 7 ДНЕЙ" to week,
                "ПОЗЖЕ" to later,
            ).forEach { (label, group) ->
                if (group.isNotEmpty()) {
                    item { SectionLabel(label, "${group.size}") }
                    items(group, key = { it.id }) { assignment ->
                        AssignmentCard(
                            assignment = assignment,
                            today = today,
                            onToggle = { repository.toggleAssignment(assignment.id) },
                            onDelete = { repository.deleteAssignment(assignment.id) },
                        )
                    }
                }
            }
        }
        if (completed.isNotEmpty()) {
            item { SectionLabel("ВЫПОЛНЕНО", "${completed.size}") }
            items(completed, key = { it.id }) { assignment ->
                AssignmentCard(
                    assignment = assignment,
                    today = today,
                    onToggle = { repository.toggleAssignment(assignment.id) },
                    onDelete = { repository.deleteAssignment(assignment.id) },
                )
            }
        }
    }

    if (adding) {
        AddAssignmentSheet(
            onDismiss = { adding = false },
            onAdd = { title, subject, date, priority ->
                repository.addAssignment(title, subject, date, priority)
                adding = false
            },
        )
    }
}

@Composable
private fun AssignmentCard(
    assignment: StudyAssignment,
    today: LocalDate,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    PremiumCard(
        modifier = Modifier.clickable {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onToggle()
        },
        color = if (assignment.completed) SurfaceRaised.copy(alpha = .55f) else SurfaceRaised,
        border = when {
            assignment.completed -> Hairline
            assignment.dueDate.isBefore(today) -> Danger.copy(alpha = .45f)
            assignment.priority == AssignmentPriority.IMPORTANT -> Acid.copy(alpha = .36f)
            else -> Hairline
        },
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(23.dp).border(1.5.dp, if (assignment.completed) Acid else Muted, CircleShape)
                    .background(if (assignment.completed) Acid else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (assignment.completed) Text("✓", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    assignment.title,
                    color = if (assignment.completed) Muted else Paper,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (assignment.subject.isNotBlank()) Text(assignment.subject, color = Muted, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(5.dp))
                Text(
                    assignmentDueLabel(assignment.dueDate, today),
                    color = if (assignment.dueDate.isBefore(today) && !assignment.completed) Danger else Acid,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Text(
                "×",
                color = Muted,
                fontSize = 23.sp,
                modifier = Modifier.clip(CircleShape).clickable(onClick = onDelete).padding(8.dp),
            )
        }
    }
}

private fun assignmentDueLabel(date: LocalDate, today: LocalDate): String = when (date) {
    today -> "СЕГОДНЯ"
    today.plusDays(1) -> "ЗАВТРА"
    else -> date.format(DateTimeFormatter.ofPattern("d MMMM", Ru)).uppercase(Ru)
}

@Composable
private fun AddAssignmentSheet(
    onDismiss: () -> Unit,
    onAdd: (String, String, LocalDate, AssignmentPriority) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(LocalDate.now()) }
    var priority by remember { mutableStateOf(AssignmentPriority.NORMAL) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceRaised,
        contentColor = Paper,
        dragHandle = { Box(Modifier.padding(top = 11.dp).size(42.dp, 4.dp).background(Hairline, CircleShape)) },
    ) {
        Column(Modifier.padding(20.dp).padding(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("НОВОЕ ЗАДАНИЕ", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.6.sp)
            AssignmentTextField(title, { title = it.take(100) }, "Что нужно сделать")
            AssignmentTextField(subject, { subject = it.take(60) }, "Предмет — необязательно")
            PremiumCard(color = SurfaceSoft) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Дедлайн", color = Paper, style = MaterialTheme.typography.titleMedium)
                        Text(assignmentDueLabel(dueDate, LocalDate.now()), color = Acid, style = MaterialTheme.typography.labelMedium)
                    }
                    MiniButton("−", "На день раньше") { dueDate = dueDate.minusDays(1).coerceAtLeast(LocalDate.now()) }
                    Spacer(Modifier.width(8.dp))
                    MiniButton("+", "На день позже") { dueDate = dueDate.plusDays(1) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                AssignmentPriority.entries.forEach { option ->
                    Box(
                        Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(13.dp))
                            .background(if (priority == option) Acid else SurfaceSoft)
                            .clickable { priority = option },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(option.label, color = if (priority == option) Ink else Muted, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            HseActionButton("ДОБАВИТЬ") { onAdd(title, subject, dueDate, priority) }
        }
    }
}

@Composable
private fun AssignmentTextField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Paper),
        cursorBrush = SolidColor(Acid),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(SurfaceSoft)
            .padding(horizontal = 15.dp, vertical = 15.dp),
        decorationBox = { inner ->
            Box {
                if (value.isBlank()) Text(placeholder, color = Muted, style = MaterialTheme.typography.bodyLarge)
                inner()
            }
        },
    )
}

@Composable
private fun HistoryScreen(
    repository: SystemRepository,
    onNo: (LocalDate, DecisionKind) -> Unit,
    onCelebrate: (DecisionKind) -> Unit,
    onExit: (() -> Unit)? = null,
) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val today = LocalDate.now()

    selectedDate?.let { date ->
        HistoryDayScreen(
            date = date,
            record = repository.recordFor(date),
            admissionTarget = SystemLogic.admissionFor(
                date,
                repository.settings.admissionStart,
                repository.records.values,
            ).target,
            lightPlan = SystemLogic.lightPlanFor(date, repository.settings.lightStart),
            onBack = { selectedDate = null },
            onSleepYes = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).sleep, DecisionStatus.YES) == null) repository.clearSleep(date)
                else {
                    repository.setSleep(date, DecisionStatus.YES)
                    onCelebrate(DecisionKind.SLEEP)
                }
            },
            onSleepNo = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).sleep, DecisionStatus.NO) == null) repository.clearSleep(date)
                else onNo(date, DecisionKind.SLEEP)
            },
            onMorningYes = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).morning, DecisionStatus.YES) == null) repository.clearMorning(date)
                else {
                    repository.setMorning(date, DecisionStatus.YES)
                    onCelebrate(DecisionKind.MORNING)
                }
            },
            onMorningNo = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).morning, DecisionStatus.NO) == null) repository.clearMorning(date)
                else onNo(date, DecisionKind.MORNING)
            },
            onLightYes = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).light, DecisionStatus.YES) == null) repository.clearLight(date)
                else {
                    repository.setLight(date, DecisionStatus.YES)
                    onCelebrate(DecisionKind.LIGHT)
                }
            },
            onLightNo = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).light, DecisionStatus.NO) == null) repository.clearLight(date)
                else onNo(date, DecisionKind.LIGHT)
            },
            onDietYes = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).diet, DecisionStatus.YES) == null) repository.clearDiet(date)
                else {
                    repository.setDiet(date, DecisionStatus.YES)
                    onCelebrate(DecisionKind.DIET)
                }
            },
            onDietNo = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).diet, DecisionStatus.NO) == null) repository.clearDiet(date)
                else onNo(date, DecisionKind.DIET)
            },
            onWaterYes = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).water, DecisionStatus.YES) == null) repository.clearWater(date)
                else {
                    repository.setWater(date, DecisionStatus.YES)
                    onCelebrate(DecisionKind.WATER)
                }
            },
            onWaterNo = {
                if (SystemLogic.toggledDecision(repository.recordFor(date).water, DecisionStatus.NO) == null) repository.clearWater(date)
                else onNo(date, DecisionKind.WATER)
            },
        )
        return
    }

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            if (onExit == null) ScreenHeader("История", "Календарь", "История и карта повторяющихся действий.")
            else Row(verticalAlignment = Alignment.CenterVertically) {
                CircleTextButton("‹", "Назад к итогам", onExit)
                Spacer(Modifier.width(14.dp))
                ScreenHeader("Итоги", "История", "Календарь личной системы.")
            }
        }
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
private fun HistoryDayScreen(
    date: LocalDate,
    record: DailyRecord,
    admissionTarget: Int,
    lightPlan: LightPlanState,
    onBack: () -> Unit,
    onSleepYes: () -> Unit,
    onSleepNo: () -> Unit,
    onMorningYes: () -> Unit,
    onMorningNo: () -> Unit,
    onLightYes: () -> Unit,
    onLightNo: () -> Unit,
    onDietYes: () -> Unit,
    onDietNo: () -> Unit,
    onWaterYes: () -> Unit,
    onWaterNo: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                CircleTextButton("‹", "Назад к календарю", onBack)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(date.format(DayFormatter).uppercase(Ru), color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
                    Text("Отметить день", color = Paper, style = MaterialTheme.typography.headlineLarge)
                }
            }
        }
        item { HistoryDecisionRow("УТРО", "$admissionTarget ${pushupWord(admissionTarget)} подряд", record.morning, onMorningYes, onMorningNo) }
        item { HistoryDecisionRow("СВЕТ", "День ${lightPlan.day}: ${lightPlan.task}", record.light, onLightYes, onLightNo) }
        item { HistoryDecisionRow("ПИТАНИЕ", "Без сладкого и без чипсов", record.diet, onDietYes, onDietNo) }
        item { HistoryDecisionRow("ВОДА", "2,5 литра за день", record.water, onWaterYes, onWaterNo) }
        item { HistoryDecisionRow("СОН", "Отбой и кровать вовремя", record.sleep, onSleepYes, onSleepNo) }
        item {
            Text(
                "Повторное нажатие на выбранный ответ отменяет отметку.",
                color = Muted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            )
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
private fun PreviousDayReminderScreen(
    repository: SystemRepository,
    date: LocalDate,
    onYes: (DailyTask) -> Unit,
    onNo: (DailyTask) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val record = repository.recordFor(date)
    val missing = SystemLogic.missingTasks(record)
    val admission = SystemLogic.admissionFor(date, repository.settings.admissionStart, repository.records.values)
    val lightPlan = SystemLogic.lightPlanFor(date, repository.settings.lightStart)

    LaunchedEffect(missing.isEmpty()) {
        if (missing.isEmpty()) onDone()
    }

    LazyColumn(
        modifier.background(Ink).statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeader(
                "Вчерашний день",
                "Остались пустые карточки",
                "Отметь только факты, которые не успел зафиксировать.",
            )
        }
        missing.forEach { task ->
            item(task.name) {
                val detail = when (task) {
                    DailyTask.MORNING -> "${admission.target} ${pushupWord(admission.target)} подряд"
                    DailyTask.LIGHT -> "День ${lightPlan.day}: ${lightPlan.task}"
                    DailyTask.DIET -> "Без сладкого и без чипсов"
                    DailyTask.WATER -> "2,5 литра за день"
                    DailyTask.SLEEP -> "Отбой и кровать вовремя"
                }
                HistoryDecisionRow(task.title, detail, null, { onYes(task) }, { onNo(task) })
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            HseActionButton("ПРОДОЛЖИТЬ БЕЗ ОТМЕТОК", onClick = onDone)
        }
    }
}

@Composable
private fun StatsScreen(
    repository: SystemRepository,
    hseMode: Boolean = false,
    onOpenHistory: (() -> Unit)? = null,
) {
    val today = LocalDate.now()
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
        item {
            ScreenHeader(
                if (hseMode) "Режим ВШЭ" else "Анализ",
                if (hseMode) "Итоги" else "30 дней",
                "Факты, данные и статистика.",
            )
        }
        if (hseMode && onOpenHistory != null) {
            item {
                PremiumCard(
                    modifier = Modifier.clickable(onClick = onOpenHistory),
                    color = Color(0xFF0F1B25),
                    border = Acid.copy(alpha = .28f),
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("ИСТОРИЯ ДИСЦИПЛИНЫ", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.4.sp)
                            Spacer(Modifier.height(7.dp))
                            Text("Открыть календарь", color = Paper, style = MaterialTheme.typography.titleLarge)
                            Text("Все пять личных задач по дням", color = Muted, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("→", color = Acid, style = MaterialTheme.typography.headlineMedium)
                    }
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
                Text("Один срыв не обнуляет прогресс. Система ищет повторяющуюся причину и возвращает к следующей задаче.", color = Muted, style = MaterialTheme.typography.bodyMedium)
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
                        null -> "На этой неделе ещё нет отметок."
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
                Text("ЗАДАЧА СЛЕДУЮЩЕЙ НЕДЕЛИ", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.4.sp)
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
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val compact = maxWidth < 110.dp
            val ringSize = if (compact) 62.dp else 112.dp
            Column(
                Modifier.fillMaxWidth().height(if (compact) 92.dp else 142.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(Modifier.size(ringSize), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize().padding(if (compact) 2.dp else 4.dp)) {
                        val stroke = if (compact) 4.dp.toPx() else 8.dp.toPx()
                        drawArc(Hairline, -215f, 250f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                        drawArc(Acid, -215f, 250f * animated, false, style = Stroke(stroke, cap = StrokeCap.Round))
                    }
                    Text(
                        value?.let { "$it%" } ?: "—",
                        color = Paper,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (compact) 16.sp else 26.sp,
                        lineHeight = if (compact) 18.sp else 30.sp,
                        maxLines = 1,
                    )
                }
                Spacer(Modifier.height(if (compact) 4.dp else 7.dp))
                Text(
                    label,
                    color = Muted,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = if (compact) 8.sp else 10.sp,
                    maxLines = 1,
                    softWrap = false,
                )
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
    val context = LocalContext.current
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
                    "Всё для студента!",
                    settings.hseModeEnabled,
                ) { enabled -> onUpdate { it.copy(hseModeEnabled = enabled) } }
                if (settings.hseModeEnabled) {
                    SettingsDivider()
                    TimeSettingRow("Первая пара", "Единое время для будних дней", settings.hseFirstClassTime) { delta ->
                        onUpdate { it.copy(hseFirstClassTime = it.hseFirstClassTime.plusMinutes(delta.toLong())) }
                    }
                    SettingsDivider()
                    AddressSettingRow(
                        title = "Домашний адрес",
                        detail = "Хранится только на этом устройстве",
                        value = settings.hseHomeAddress,
                        placeholder = "Улица, дом",
                    ) { address -> onUpdate { it.copy(hseHomeAddress = address.take(120)) } }
                    SettingsDivider()
                    Column {
                        Text("Корпус ВШЭ", color = Paper, style = MaterialTheme.typography.titleMedium)
                        Text(settings.hseUniversityAddress, color = Acid, style = MaterialTheme.typography.bodyMedium)
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Все отметки и настройки хранятся только на этом устройстве. Интернет используется для маршрута ВШЭ, а расписание читается из календаря Android только после разрешения.",
                    color = Muted,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                )
                if (settings.hseModeEnabled) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Условия использования сервиса Яндекс Карты",
                        color = Acid,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://yandex.ru/legal/maps_termsofuse")))
                            }
                        },
                    )
                }
            }
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
private fun AddressSettingRow(
    title: String,
    detail: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    Column {
        Text(title, color = Paper, style = MaterialTheme.typography.titleMedium)
        Text(detail, color = Muted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Paper),
            cursorBrush = SolidColor(Acid),
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(SurfaceSoft)
                .padding(horizontal = 13.dp, vertical = 12.dp),
            decorationBox = { inner ->
                Box {
                    if (value.isBlank()) Text(placeholder, color = Muted, style = MaterialTheme.typography.bodyLarge)
                    inner()
                }
            },
        )
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
