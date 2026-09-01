@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.personal.thesystem.ui

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.semantics.selected
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
import com.personal.thesystem.data.HseCalendarSource
import com.personal.thesystem.data.SystemRepository
import com.personal.thesystem.data.MapKitRuntime
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
import com.personal.thesystem.model.MoneyCommitment
import com.personal.thesystem.model.MoneyPeriod
import com.personal.thesystem.model.MoneyReport
import com.personal.thesystem.model.MoneySnapshot
import com.personal.thesystem.model.MoneyStatus
import com.personal.thesystem.model.MoneyTransaction
import com.personal.thesystem.model.SystemLogic
import com.personal.thesystem.model.SystemSettings
import com.personal.thesystem.model.StudyAssignment
import com.personal.thesystem.model.SleepViolationPart
import com.personal.thesystem.model.ViolationReason
import com.personal.thesystem.model.WeeklyExperiment
import com.personal.thesystem.notifications.ReminderScheduler
import com.personal.thesystem.notifications.ReminderReceiver
import com.personal.thesystem.ui.theme.Acid
import com.personal.thesystem.ui.theme.AcidDim
import com.personal.thesystem.ui.theme.Amber
import com.personal.thesystem.ui.theme.Danger
import com.personal.thesystem.ui.theme.Hairline
import com.personal.thesystem.ui.theme.HseGold
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

internal val Ru = Locale.forLanguageTag("ru")
internal val DayFormatter = DateTimeFormatter.ofPattern("d MMMM", Ru)

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
fun SystemApp(
    repository: SystemRepository,
    initialDestination: String? = null,
    onDestinationHandled: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.TODAY) }
    var showPersonalSystem by rememberSaveable { mutableStateOf(false) }
    var showDisciplineHistory by rememberSaveable { mutableStateOf(false) }
    var showWeeklyReview by rememberSaveable {
        mutableStateOf(repository.weeklyReviewDueDate != null || initialDestination == "weekly_review")
    }
    var showRunPrompt by rememberSaveable { mutableStateOf(false) }
    var runStartedAtRealtime by rememberSaveable { mutableStateOf<Long?>(null) }
    var runSummarySeconds by rememberSaveable { mutableStateOf<Long?>(null) }
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
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    val previousDate = remember(currentDate) { currentDate.minusDays(1) }
    var showPreviousDayReminder by remember {
        mutableStateOf(repository.shouldShowPreviousDayReminder(currentDate))
    }
    val modeTransitionProgress = remember { Animatable(1f) }
    var modeTransitionTarget by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(initialDestination) {
        when (initialDestination) {
            "history" -> selectedTab = AppTab.HISTORY
            "money" -> selectedTab = AppTab.MONEY
            "stats" -> selectedTab = AppTab.STATS
            "settings" -> selectedTab = AppTab.SETTINGS
            "weekly_review" -> showWeeklyReview = true
            "hse" -> {
                if (!repository.settings.hseModeEnabled) repository.updateSettings { it.copy(hseModeEnabled = true) }
                selectedTab = AppTab.TODAY
                showPersonalSystem = false
            }
            "today", "water", "diet", "morning", "sleep" -> {
                selectedTab = AppTab.TODAY
                showPersonalSystem = repository.settings.hseModeEnabled
            }
        }
        if (initialDestination != null) onDestinationHandled()
    }

    LaunchedEffect(repository.weeklyReviewDueDate) {
        if (repository.weeklyReviewDueDate != null) showWeeklyReview = true
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            currentTime = LocalTime.now()
            currentDate = LocalDate.now()
        }
    }
    LaunchedEffect(currentDate) {
        repository.reload()
        showPreviousDayReminder = repository.shouldShowPreviousDayReminder(currentDate)
    }
    val hseMode = repository.settings.hseModeEnabled
    val targetBackground = when {
        !currentTime.isBefore(repository.settings.digitalCutoff) -> NightInk
        hseMode -> Color(0xFF050506)
        currentTime.hour < 12 -> MorningInk
        else -> Ink
    }
    val appBackground by animateColorAsState(targetBackground, tween(900), label = "dayBackground")
    val motionAllowed = !repository.settings.reduceMotion && ValueAnimator.areAnimatorsEnabled()

    fun applySettings(transform: (SystemSettings) -> SystemSettings) {
        val before = repository.settings
        repository.updateSettings(transform)
        if (ReminderScheduler.scheduleRelevantChanged(before, repository.settings)) {
            ReminderScheduler.scheduleAll(context, repository.settings)
        }
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
                tween(durationMillis = if (motionAllowed) 900 else 120, easing = FastOutSlowInEasing),
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

    if (showWeeklyReview) {
        WeeklyReviewScreen(repository) {
            repository.completeWeeklyReview()
            showWeeklyReview = false
        }
        return
    }

    RunShakeEffect(
        enabled = runStartedAtRealtime == null &&
            runSummarySeconds == null &&
            !showRunPrompt &&
            pendingReason == null &&
            !showPreviousDayReminder &&
            modeTransitionTarget == null,
        onShake = { showRunPrompt = true },
    )

    runStartedAtRealtime?.let { startedAt ->
        RunningScreen(
            startedAtRealtime = startedAt,
            motionAllowed = motionAllowed,
            onFinish = { elapsedSeconds ->
                runStartedAtRealtime = null
                runSummarySeconds = elapsedSeconds
            },
        )
        return
    }
    runSummarySeconds?.let { elapsedSeconds ->
        RunSummaryScreen(
            elapsedSeconds = elapsedSeconds,
            motionAllowed = motionAllowed,
            onDone = { runSummarySeconds = null },
        )
        return
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
                        onOpenWeeklyReview = { showWeeklyReview = true },
                    )
                    AppTab.SETTINGS -> SettingsScreen(
                        settings = repository.settings,
                        onUpdate = ::applySettings,
                        onEnableNotifications = ::enableNotifications,
                        onRequestExactAlarmAccess = ::requestExactAlarmAccess,
                        exactAlarmsAllowed = exactAlarmsAllowed,
                    )
                }
            }
        }
        if (!motionAllowed) Unit else if (celebrationKind == DecisionKind.SLEEP) {
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
                        DailyTask.MORNING -> {
                            repository.setMorning(previousDate, DecisionStatus.YES)
                            DecisionKind.MORNING
                        }
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
                    repository.markPreviousDayReminderShown(currentDate)
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
            onReason = { reason, sleepPart ->
                when (kind) {
                    DecisionKind.SLEEP -> repository.setSleep(
                        date,
                        DecisionStatus.NO,
                        ViolationReason.fromId(reason),
                        sleepPart,
                    )
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
    if (showRunPrompt) {
        RunPromptSheet(
            onYes = {
                showRunPrompt = false
                runSummarySeconds = null
                runStartedAtRealtime = SystemClock.elapsedRealtime()
                launchRunPlaylist(context)
            },
            onNo = { showRunPrompt = false },
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
                    .semantics {
                        role = Role.Tab
                        this.selected = active
                        contentDescription = tab.label(hseMode)
                    }
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
                    fontSize = 11.sp,
                    letterSpacing = 0.2.sp,
                )
            }
        }
    }
}

@Composable
private fun NavGlyph(tab: AppTab, color: Color) {
    Canvas(Modifier.size(22.dp)) {
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
internal fun ScreenHeader(eyebrow: String, title: String, detail: String? = null) {
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
    val lightPlan = SystemLogic.lightPlanFor(today, settings.lightStart)
    val activeTasks = SystemLogic.activeTasks(today, settings)
    val experiment = if (SystemLogic.experimentAvailableOn(today)) {
        SystemLogic.weeklyExperiment(today, repository.records.values, repository.experimentFeedback)
    } else {
        null
    }
    val answered = activeTasks.count { SystemLogic.statusFor(record, it) != null }
    val currentTask = SystemLogic.currentTask(record, currentTime, settings.digitalCutoff, activeTasks)
    val recoveryTask = SystemLogic.recoveryTask(record, currentTime, settings.digitalCutoff, activeTasks)

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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    ModeToggleButton(settings.hseModeEnabled, onToggleHseMode)
                    SystemMark(answered, activeTasks.size)
                }
            }
        }

        item { DailyStatement(answered, activeTasks.size, record) }

        recoveryTask?.let { task ->
            item { RecoveryCard(task) }
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
            SectionLabel("УТРО", "20 ОТЖИМАНИЙ")
            Spacer(Modifier.height(8.dp))
            DecisionCard(
                title = "УТРО",
                number = "01",
                description = "Сразу после пробуждения — 20 отжиманий.",
                status = record.morning,
                reasonLabel = record.morningReason?.label,
                onYes = {
                    if (SystemLogic.toggledDecision(record.morning, DecisionStatus.YES) == null) {
                        repository.clearMorning(today)
                    } else {
                        repository.setMorning(today, DecisionStatus.YES)
                        expandedCompleted -= DailyTask.MORNING
                        onCelebrate(DecisionKind.MORNING)
                    }
                },
                onNo = {
                    if (SystemLogic.toggledDecision(record.morning, DecisionStatus.NO) == null) repository.clearMorning(today)
                    else onNo(DecisionKind.MORNING)
                },
                collapsed = record.morning != null && DailyTask.MORNING !in expandedCompleted,
                current = currentTask == DailyTask.MORNING,
                onToggleCollapsed = { toggleCompleted(DailyTask.MORNING) },
            )
        }

        if (lightPlan.completed && today == settings.lightStart.plusDays(SystemLogic.LIGHT_PLAN_DAYS.toLong())) item {
            PremiumCard(color = Acid.copy(alpha = .07f), border = Acid.copy(alpha = .55f)) {
                Text("ПЛАН СВЕТА ПРОЙДЕН", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(8.dp))
                Text("30 дней завершены", color = Paper, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(5.dp))
                Text("Свет больше не входит в ежедневные задачи и статистику новых дней.", color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (!lightPlan.completed) item {
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
                collapsed = record.light != null && DailyTask.LIGHT !in expandedCompleted,
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
                collapsed = record.diet != null && DailyTask.DIET !in expandedCompleted,
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
                reasonLabel = listOfNotNull(record.sleepViolationPart?.label, record.sleepReason?.label)
                    .takeIf { it.isNotEmpty() }
                    ?.joinToString(" · "),
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
                collapsed = record.sleep != null && DailyTask.SLEEP !in expandedCompleted,
                current = currentTask == DailyTask.SLEEP,
                onToggleCollapsed = { toggleCompleted(DailyTask.SLEEP) },
            )
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

        if (currentTime.hour >= 20) item { TonightProtocol(settings) }
    }
}

@Composable
private fun SystemMark(answered: Int, total: Int) {
    val progress = if (total == 0) 1f else answered / total.toFloat()
    val animated by animateFloatAsState(progress, spring(stiffness = Spring.StiffnessLow), label = "systemMark")
    Box(
        Modifier
            .size(54.dp)
            .clip(CircleShape)
            .semantics {
                contentDescription = "Отмечено $answered из $total задач"
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
        Text("$answered/$total", color = if (progress > 0f) Acid else Muted, fontWeight = FontWeight.Black, fontSize = 14.sp)
    }
}

@Composable
private fun ModeToggleButton(enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) Acid.copy(alpha = .14f) else SurfaceSoft)
            .border(1.dp, if (enabled) Acid.copy(alpha = .55f) else Hairline, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = if (enabled) "Выключить режим ВШЭ" else "Включить режим ВШЭ" }
            .padding(horizontal = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(if (enabled) "ВШЭ · ВКЛ" else "ВШЭ", color = if (enabled) Acid else Paper, style = MaterialTheme.typography.labelMedium)
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
private fun DailyStatement(answered: Int, total: Int, record: DailyRecord) {
    val failures = listOf(record.morning, record.light, record.diet, record.water, record.sleep).count { it == DecisionStatus.NO }
    val label = when {
        answered == 0 -> "${if (total == 5) "ПЯТЬ ЗАДАЧ" else "ЧЕТЫРЕ ЗАДАЧИ"} -\nОДИН ДЕНЬ"
        answered == total && failures == 0 -> "ДЕНЬ\nЗАКРЫТ."
        answered == total -> "НАРУШЕНИЯ\nУСТАНОВЛЕНЫ"
        answered == total - 1 -> "ПОЧТИ\nГОТОВО"
        answered == 1 -> "ПЕРВАЯ ЧАСТЬ\nГОТОВА"
        answered == 2 -> "ДВЕ ЧАСТИ\nГОТОВЫ"
        else -> "ПОЛДЕЛА\nСДЕЛАНО"
    }
    val supporting = if (total == 5) SystemLogic.contextualPhrase(record) else when {
        answered == total && failures == 0 -> "Все четыре текущие задачи выполнены. Дело сделано. Супер!"
        answered == total -> "Все текущие задачи отмечены. Картина дня готова."
        else -> "Отмечено $answered из $total. Главное — честность."
    }
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
                        Text(label, color = Paper, style = MaterialTheme.typography.labelMedium, fontSize = 11.sp, textAlign = TextAlign.Center)
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
internal fun SectionLabel(left: String, right: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(left, color = Muted, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.8.sp)
        Text(right, color = Muted, style = MaterialTheme.typography.labelMedium, letterSpacing = 0.6.sp)
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
    showDecisionButtons: Boolean = true,
    footer: (@Composable () -> Unit)? = null,
) {
    val background by animateColorAsState(
        when (status) {
            DecisionStatus.YES -> AcidDim.copy(alpha = 0.10f)
            DecisionStatus.NO -> Danger.copy(alpha = 0.08f)
            null -> SurfaceRaised
        },
        label = "decisionColor",
    )
    AnimatedContent(targetState = collapsed, label = "collapse$number") { isCollapsed ->
        if (isCollapsed) {
            val statusColor = if (status == DecisionStatus.NO) Danger else AcidDim
            PremiumCard(
                color = statusColor.copy(alpha = .08f),
                border = statusColor.copy(alpha = .34f),
                modifier = Modifier.clickable(onClick = onToggleCollapsed),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(number, color = statusColor, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(14.dp))
                    Text(title, color = Paper, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(if (status == DecisionStatus.NO) "×" else "✓", color = statusColor, fontWeight = FontWeight.Black, fontSize = 19.sp)
                }
            }
            return@AnimatedContent
        }
        PremiumCard(
            color = if (current && status == null) Acid.copy(alpha = .06f) else background,
            border = when {
                current && status == null -> Acid.copy(alpha = .78f)
                status == DecisionStatus.YES -> AcidDim.copy(alpha = .38f)
                else -> Hairline
            },
            modifier = Modifier.animateContentSize(),
        ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(number, color = if (status == null) Muted else Acid, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(12.dp))
                Text(title, color = Paper, style = MaterialTheme.typography.headlineMedium, letterSpacing = 1.sp)
                Spacer(Modifier.height(7.dp))
                Text(description, color = Muted, style = MaterialTheme.typography.bodyMedium)
            }
            if (status != null) {
                Box(
                    Modifier
                        .size(48.dp)
                        .background(if (status == DecisionStatus.YES) Acid else Danger, CircleShape)
                        .clickable(onClick = onToggleCollapsed),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (status == DecisionStatus.YES) "✓" else "×", color = Ink, fontWeight = FontWeight.Black, fontSize = 19.sp)
                }
            }
        }
        if (showDecisionButtons) {
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                DecisionButton("ДА", "$title: Да", status == DecisionStatus.YES, true, Modifier.weight(1f), onYes)
                DecisionButton("НЕТ", "$title: Нет", status == DecisionStatus.NO, false, Modifier.weight(1f), onNo)
            }
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
                Text("04", color = if (quarters == 0) Muted else Acid, style = MaterialTheme.typography.labelMedium)
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
    description: String,
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
            .semantics { role = Role.Button; contentDescription = description }
            .clickable(interactionSource = interaction, indication = null) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (selected) Ink else Paper, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.2.sp)
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
private fun ReasonSheet(
    kind: DecisionKind,
    onDismiss: () -> Unit,
    onReason: (String, SleepViolationPart?) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    var sleepPart by remember(kind) { mutableStateOf<SleepViolationPart?>(null) }
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
            if (kind == DecisionKind.SLEEP) {
                Text("ЧТО НАРУШЕНО", color = Muted, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(10.dp))
                SleepViolationPart.entries.forEach { part ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 9.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (sleepPart == part) Acid.copy(alpha = .12f) else SurfaceSoft)
                            .border(1.dp, if (sleepPart == part) Acid.copy(alpha = .55f) else Hairline, RoundedCornerShape(16.dp))
                            .clickable { sleepPart = part }
                            .padding(horizontal = 16.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(part.label, color = Paper, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.weight(1f))
                        if (sleepPart == part) Text("✓", color = Acid, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("ПОЧЕМУ", color = Muted, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(10.dp))
            }
            val canChooseReason = kind != DecisionKind.SLEEP || sleepPart != null
            reasons.forEach { (id, label) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 9.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceSoft)
                        .border(1.dp, Hairline, RoundedCornerShape(16.dp))
                        .clickable(enabled = canChooseReason) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onReason(id, sleepPart)
                        }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(8.dp).background(Danger, CircleShape))
                    Spacer(Modifier.width(13.dp))
                    Text(label, color = if (canChooseReason) Paper else Muted, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    Text("→", color = Muted, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
internal fun PremiumCard(
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
private fun AssignmentsScreen(repository: SystemRepository) {
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<StudyAssignment?>(null) }
    var deleteCandidate by remember { mutableStateOf<Long?>(null) }
    var showCompleted by remember { mutableStateOf(false) }
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
                            onEdit = { editing = assignment },
                            deleteArmed = deleteCandidate == assignment.id,
                            onDelete = {
                                if (deleteCandidate == assignment.id) {
                                    repository.deleteAssignment(assignment.id)
                                    deleteCandidate = null
                                } else deleteCandidate = assignment.id
                            },
                        )
                    }
                }
            }
        }
        if (completed.isNotEmpty()) {
            item {
                PremiumCard(modifier = Modifier.clickable { showCompleted = !showCompleted }, color = SurfaceRaised.copy(alpha = .6f)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ВЫПОЛНЕНО", color = Muted, style = MaterialTheme.typography.labelLarge)
                        Text("${completed.size} ${if (showCompleted) "↑" else "↓"}", color = Acid, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            if (showCompleted) items(completed, key = { it.id }) { assignment ->
                AssignmentCard(
                    assignment = assignment,
                    today = today,
                    onToggle = { repository.toggleAssignment(assignment.id) },
                    onEdit = { editing = assignment },
                    deleteArmed = deleteCandidate == assignment.id,
                    onDelete = {
                        if (deleteCandidate == assignment.id) {
                            repository.deleteAssignment(assignment.id)
                            deleteCandidate = null
                        } else deleteCandidate = assignment.id
                    },
                )
            }
        }
    }

    if (adding) {
        AddAssignmentSheet(
            existing = null,
            onDismiss = { adding = false },
            onAdd = { title, subject, date, priority ->
                repository.addAssignment(title, subject, date, priority)
                adding = false
            },
        )
    }
    editing?.let { assignment ->
        AddAssignmentSheet(
            existing = assignment,
            onDismiss = { editing = null },
            onAdd = { title, subject, date, priority ->
                repository.updateAssignment(
                    assignment.copy(title = title, subject = subject, dueDate = date, priority = priority)
                )
                editing = null
            },
        )
    }
}

@Composable
private fun AssignmentCard(
    assignment: StudyAssignment,
    today: LocalDate,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    deleteArmed: Boolean,
    onDelete: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    PremiumCard(
        modifier = Modifier.clickable(onClick = onEdit),
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
                Modifier.size(48.dp).clip(CircleShape).clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggle()
                }.border(1.5.dp, if (assignment.completed) Acid else Muted, CircleShape)
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
                if (deleteArmed) "УДАЛИТЬ?" else "×",
                color = if (deleteArmed) Danger else Muted,
                fontSize = if (deleteArmed) 10.sp else 23.sp,
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
    existing: StudyAssignment?,
    onDismiss: () -> Unit,
    onAdd: (String, String, LocalDate, AssignmentPriority) -> Unit,
) {
    val context = LocalContext.current
    var title by remember(existing?.id) { mutableStateOf(existing?.title.orEmpty()) }
    var subject by remember(existing?.id) { mutableStateOf(existing?.subject.orEmpty()) }
    var dueDate by remember(existing?.id) { mutableStateOf(existing?.dueDate ?: LocalDate.now()) }
    var priority by remember(existing?.id) { mutableStateOf(existing?.priority ?: AssignmentPriority.NORMAL) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceRaised,
        contentColor = Paper,
        dragHandle = { Box(Modifier.padding(top = 11.dp).size(42.dp, 4.dp).background(Hairline, CircleShape)) },
    ) {
        Column(Modifier.padding(20.dp).padding(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(if (existing == null) "НОВОЕ ЗАДАНИЕ" else "ИЗМЕНИТЬ ЗАДАНИЕ", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.6.sp)
            AssignmentTextField(title, { title = it.take(100) }, "Что нужно сделать")
            AssignmentTextField(subject, { subject = it.take(60) }, "Предмет — необязательно")
            PremiumCard(
                color = SurfaceSoft,
                modifier = Modifier.clickable {
                    android.app.DatePickerDialog(
                        context,
                        { _, year, month, day -> dueDate = LocalDate.of(year, month + 1, day) },
                        dueDate.year,
                        dueDate.monthValue - 1,
                        dueDate.dayOfMonth,
                    ).show()
                },
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Дедлайн · нажми, чтобы выбрать", color = Paper, style = MaterialTheme.typography.titleMedium)
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
                        Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(13.dp))
                            .background(if (priority == option) Acid else SurfaceSoft)
                            .clickable { priority = option },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(option.label, color = if (priority == option) Ink else Muted, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            HseActionButton(if (existing == null) "ДОБАВИТЬ" else "СОХРАНИТЬ", enabled = title.isNotBlank()) {
                onAdd(title, subject, dueDate, priority)
            }
        }
    }
}

@Composable
internal fun AssignmentTextField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    BasicTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.replaceFirstChar { it.uppercase() }) },
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
    val statuses = listOf(record?.morning, record?.light, record?.diet, record?.water, record?.sleep)
    val statusDescription = listOf("Утро", "Свет", "Питание", "Вода", "Сон")
        .zip(statuses)
        .joinToString { (label, status) ->
            "$label: ${when (status) { DecisionStatus.YES -> "соблюдено"; DecisionStatus.NO -> "нарушено"; null -> "нет отметки" }}"
        }
    Box(
        modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(if (isToday) Modifier.border(1.dp, Acid, RoundedCornerShape(12.dp)) else Modifier)
            .semantics {
                contentDescription = "${date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Ru))}. $statusDescription"
                role = Role.Button
            }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(date.dayOfMonth.toString(), color = if (enabled) Paper else Muted.copy(alpha = .35f), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                statuses.forEach { status ->
                    Box(
                        Modifier.size(4.dp).background(
                            when (status) {
                                DecisionStatus.YES -> Acid
                                DecisionStatus.NO -> Danger
                                null -> Hairline
                            },
                            CircleShape,
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleTextButton(text: String, description: String, onClick: () -> Unit, enabled: Boolean = true) {
    Box(
        Modifier
            .size(48.dp)
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
    val eligible = records.size
    val metrics = listOf(
        "УТРО" to SystemLogic.complianceStat(records, eligible) { it.morning },
        "СВЕТ" to SystemLogic.complianceStat(records, eligible) { it.light },
        "ПИТАНИЕ" to SystemLogic.complianceStat(records, eligible) { it.diet },
        "ВОДА" to SystemLogic.complianceStat(records, eligible) { it.water },
        "СОН" to SystemLogic.complianceStat(records, eligible) { it.sleep },
    )
    PremiumCard(color = SurfaceSoft) {
        Text("ИТОГ МЕСЯЦА", color = Muted, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            metrics.take(2).forEach { (label, stat) ->
                CompactMetric(label, stat.value, Modifier.weight(1f), coverage = "${stat.answered}/${stat.eligible}")
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            metrics.drop(2).forEach { (label, stat) ->
                CompactMetric(label, stat.value, Modifier.weight(1f), coverage = "${stat.answered}/${stat.eligible}")
            }
        }
    }
}

@Composable
private fun CompactMetric(
    label: String,
    value: Int?,
    modifier: Modifier,
    suffix: String = "%",
    coverage: String? = null,
) {
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
        Text(label, color = Muted, style = MaterialTheme.typography.labelMedium, fontSize = 12.sp, maxLines = 1)
        coverage?.let { Text(it, color = Muted, fontSize = 11.sp) }
    }
}

@Composable
private fun HistoryDayScreen(
    date: LocalDate,
    record: DailyRecord,
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
        item { HistoryDecisionRow("УТРО", "20 отжиманий", record.morning, onMorningYes, onMorningNo) }
        if (!lightPlan.completed) item {
            HistoryDecisionRow("СВЕТ", "День ${lightPlan.day}: ${lightPlan.task}", record.light, onLightYes, onLightNo)
        }
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
            DecisionButton("ДА", "$title: Да", status == DecisionStatus.YES, true, Modifier.weight(1f), onYes)
            DecisionButton("НЕТ", "$title: Нет", status == DecisionStatus.NO, false, Modifier.weight(1f), onNo)
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
    val missing = SystemLogic.missingTasks(record, SystemLogic.activeTasks(date, repository.settings))
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
                    DailyTask.MORNING -> "20 отжиманий"
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
    onOpenWeeklyReview: () -> Unit,
) {
    val today = LocalDate.now()
    val recentRecords = repository.records.values.filter {
        !it.date.isBefore(today.minusDays(29)) && !it.date.isAfter(today)
    }
    fun eligibleDays(start: LocalDate, end: LocalDate = today): Int {
        val windowStart = maxOf(today.minusDays(29), start)
        val windowEnd = minOf(today, end)
        return if (windowEnd.isBefore(windowStart)) 0 else (ChronoUnit.DAYS.between(windowStart, windowEnd) + 1).toInt()
    }
    val standardEligible = eligibleDays(repository.settings.admissionStart)
    val lightEligible = eligibleDays(
        repository.settings.lightStart,
        repository.settings.lightStart.plusDays(SystemLogic.LIGHT_PLAN_DAYS.toLong() - 1L),
    )
    val sleep = SystemLogic.complianceStat(recentRecords, standardEligible) { it.sleep }
    val morning = SystemLogic.complianceStat(recentRecords, standardEligible) { it.morning }
    val light = SystemLogic.complianceStat(recentRecords, lightEligible) { it.light }
    val diet = SystemLogic.complianceStat(recentRecords, standardEligible) { it.diet }
    val water = SystemLogic.complianceStat(recentRecords, standardEligible) { it.water }
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
        item {
            HseActionButton(
                label = "ВЫШКА: ОБЗОР НЕДЕЛИ",
                color = if (hseMode) HseGold else Acid,
                onClick = onOpenWeeklyReview,
            )
        }
        if (hseMode && onOpenHistory != null) {
            item {
                PremiumCard(
                    modifier = Modifier.clickable(onClick = onOpenHistory),
                    color = Color(0xFF111112),
                    border = HseGold.copy(alpha = .34f),
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("ИСТОРИЯ ДИСЦИПЛИНЫ", color = HseGold, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.4.sp)
                            Spacer(Modifier.height(7.dp))
                            Text("Открыть календарь", color = Paper, style = MaterialTheme.typography.titleLarge)
                            Text("Все пять личных задач по дням", color = Muted, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("→", color = HseGold, style = MaterialTheme.typography.headlineMedium)
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
        item { LastSevenDays(repository.records, today, repository.settings) }
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
private fun MetricRing(label: String, stat: com.personal.thesystem.model.ComplianceStat, modifier: Modifier) {
    val target = (stat.value ?: 0) / 100f
    val animated by animateFloatAsState(target, spring(stiffness = Spring.StiffnessLow), label = "metric")
    PremiumCard(modifier = modifier) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val compact = maxWidth < 110.dp
            val ringSize = if (compact) 62.dp else 112.dp
            Column(
                Modifier.fillMaxWidth().height(if (compact) 108.dp else 154.dp),
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
                        stat.value?.let { "$it%" } ?: "—",
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
                    fontSize = if (compact) 11.sp else 12.sp,
                    maxLines = 1,
                    softWrap = false,
                )
                Spacer(Modifier.height(3.dp))
                Text("${stat.answered}/${stat.eligible}", color = Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun LastSevenDays(records: Map<LocalDate, DailyRecord>, today: LocalDate, settings: SystemSettings) {
    PremiumCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("ПОСЛЕДНИЕ 7 ДНЕЙ", color = Paper, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.2.sp)
            Text("ЧЕСТНЫЕ ОТМЕТКИ", color = Muted, style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth().height(116.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            (6 downTo 0).map { today.minusDays(it.toLong()) }.forEach { date ->
                val record = records[date]
                val active = SystemLogic.activeTasks(date, settings)
                val answered = active.count { task -> record?.let { SystemLogic.statusFor(it, task) } != null }
                val yes = active.count { task -> record?.let { SystemLogic.statusFor(it, task) } == DecisionStatus.YES }
                val fraction = if (answered == 0) 0.04f else yes / active.size.toFloat()
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.width(30.dp)) {
                    Box(
                        Modifier
                            .width(9.dp)
                            .height((74 * max(.06f, fraction)).dp)
                            .background(if (answered == 0) Hairline else if (yes == active.size) Acid else if (yes > 0) Amber else Danger, CircleShape)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(date.dayOfWeek.getDisplayName(TextStyle.SHORT, Ru).take(2).uppercase(Ru), color = if (date == today) Paper else Muted, style = MaterialTheme.typography.labelMedium, fontSize = 11.sp)
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
