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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
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
import com.personal.thesystem.model.DecisionStatus
import com.personal.thesystem.model.SystemLogic
import com.personal.thesystem.model.SystemSettings
import com.personal.thesystem.model.ViolationReason
import com.personal.thesystem.notifications.ReminderScheduler
import com.personal.thesystem.ui.theme.Acid
import com.personal.thesystem.ui.theme.Amber
import com.personal.thesystem.ui.theme.Danger
import com.personal.thesystem.ui.theme.Hairline
import com.personal.thesystem.ui.theme.Ink
import com.personal.thesystem.ui.theme.Muted
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

private enum class DecisionKind { SLEEP, MORNING }

private data class ConfettiParticle(
    val x: Float,
    val startY: Float,
    val delay: Float,
    val sway: Float,
    val phase: Float,
    val rotation: Float,
    val colorIndex: Int,
    val round: Boolean,
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

@Composable
fun SystemApp(repository: SystemRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(AppTab.TODAY) }
    var pendingReason by remember { mutableStateOf<Pair<LocalDate, DecisionKind>?>(null) }
    var emojiAfterReason by remember { mutableStateOf(false) }
    var confettiBurst by remember { mutableIntStateOf(0) }
    var emojiBurst by remember { mutableIntStateOf(0) }

    fun applySettings(transform: (SystemSettings) -> SystemSettings) {
        repository.updateSettings(transform)
        ReminderScheduler.scheduleAll(context, repository.settings)
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        applySettings { it.copy(notificationsEnabled = granted) }
    }

    fun enableNotifications() {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        else applySettings { it.copy(notificationsEnabled = true) }
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Ink,
            bottomBar = {
                SystemBottomBar(
                    selected = selectedTab,
                    onSelect = { selectedTab = it },
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
                        onNo = { kind ->
                            emojiAfterReason = true
                            pendingReason = LocalDate.now() to kind
                        },
                        onCelebrate = { confettiBurst += 1 },
                        onEnableNotifications = ::enableNotifications,
                    )
                    AppTab.HISTORY -> HistoryScreen(
                        repository = repository,
                        onNo = { date, kind ->
                            emojiAfterReason = false
                            pendingReason = date to kind
                        },
                    )
                    AppTab.STATS -> StatsScreen(repository)
                    AppTab.SETTINGS -> SettingsScreen(
                        settings = repository.settings,
                        onUpdate = ::applySettings,
                        onEnableNotifications = ::enableNotifications,
                    )
                }
            }
        }
        ConfettiBurst(confettiBurst, Modifier.fillMaxSize().zIndex(20f))
        EmojiRain(emojiBurst, Modifier.fillMaxSize().zIndex(21f))
    }

    pendingReason?.let { (date, kind) ->
        ReasonSheet(
            kind = kind,
            onDismiss = {
                emojiAfterReason = false
                pendingReason = null
            },
            onReason = { reason ->
                if (kind == DecisionKind.SLEEP) repository.setSleep(date, DecisionStatus.NO, reason)
                else repository.setMorning(date, DecisionStatus.NO, reason)
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
            List(88) { index ->
                ConfettiParticle(
                    x = random.nextFloat(),
                    startY = -0.18f + random.nextFloat() * 0.13f,
                    delay = random.nextFloat() * 0.32f,
                    sway = 0.025f + random.nextFloat() * 0.07f,
                    phase = random.nextFloat() * 6.283f,
                    rotation = random.nextFloat() * 540f - 270f,
                    colorIndex = index % 5,
                    round = index % 6 == 0,
                )
            }
        }
    }

    LaunchedEffect(burstId) {
        if (burstId > 0) {
            animation.snapTo(0f)
            animation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1900, easing = LinearEasing),
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
    )

    Canvas(modifier) {
        val pieceWidth = 6.dp.toPx()
        val pieceHeight = 13.dp.toPx()
        particles.forEach { particle ->
            val localProgress = ((progress - particle.delay) / (1f - particle.delay)).coerceIn(0f, 1f)
            if (localProgress <= 0f || localProgress >= 1f) return@forEach

            val alpha = if (localProgress < 0.82f) 1f else (1f - localProgress) / 0.18f
            val x = particle.x * size.width +
                sin(localProgress * 10f + particle.phase) * particle.sway * size.width
            val startY = particle.startY * size.height
            val y = startY + localProgress * (size.height - startY + pieceHeight)
            val color = palette[particle.colorIndex].copy(alpha = alpha.coerceIn(0f, 1f))

            if (particle.round) {
                drawCircle(color, radius = pieceWidth * 0.55f, center = androidx.compose.ui.geometry.Offset(x, y))
            } else {
                rotate(
                    degrees = particle.rotation + localProgress * 720f,
                    pivot = androidx.compose.ui.geometry.Offset(x, y),
                ) {
                    drawRoundRect(
                        color = color,
                        topLeft = androidx.compose.ui.geometry.Offset(x - pieceWidth / 2f, y - pieceHeight / 2f),
                        size = androidx.compose.ui.geometry.Size(pieceWidth, pieceHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(pieceWidth / 3f),
                    )
                }
            }
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
private fun SystemBottomBar(selected: AppTab, onSelect: (AppTab) -> Unit) {
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Ink.copy(alpha = 0.98f))
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
    onNo: (DecisionKind) -> Unit,
    onCelebrate: () -> Unit,
    onEnableNotifications: () -> Unit,
) {
    val today = LocalDate.now()
    val record = repository.recordFor(today)
    val settings = repository.settings
    val admission = SystemLogic.admissionFor(today, settings.admissionStart)
    val answered = listOf(record.sleep, record.morning).count { it != null }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                ScreenHeader("The System", today.format(DayFormatter).replaceFirstChar { it.uppercase() })
                SystemMark(answered / 2f)
            }
        }

        item { DailyStatement(answered, record) }

        if (!settings.notificationsEnabled) {
            item { ReminderPermissionCard(onEnableNotifications) }
        }

        item {
            SectionLabel("ВЕЧЕР", "22:45 → 23:30")
            Spacer(Modifier.height(8.dp))
            DecisionCard(
                title = "СОН",
                number = "01",
                description = "Цифровой отбой в ${SystemLogic.formatTime(settings.digitalCutoff)}. В кровати в ${SystemLogic.formatTime(settings.bedTime)}.",
                status = record.sleep,
                reason = record.sleepReason,
                onYes = {
                    repository.setSleep(today, DecisionStatus.YES)
                    onCelebrate()
                },
                onNo = { onNo(DecisionKind.SLEEP) },
            )
        }

        item {
            SectionLabel("УТРО", if (admission.completed) "РЕЖИМ 20" else "ДОПУСК ${admission.day}/${admission.totalDays}")
            Spacer(Modifier.height(8.dp))
            DecisionCard(
                title = "УТРО",
                number = "02",
                description = if (admission.completed) {
                    "Сразу после пробуждения — 20 отжиманий подряд."
                } else {
                    "Сегодня ${admission.target} ${pushupWord(admission.target)} подряд. Сразу после пробуждения."
                },
                status = record.morning,
                reason = record.morningReason,
                onYes = {
                    repository.setMorning(today, DecisionStatus.YES)
                    onCelebrate()
                },
                onNo = { onNo(DecisionKind.MORNING) },
                footer = {
                    AdmissionProgress(admission.target, admission.day, admission.totalDays, admission.completed)
                },
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
    val failures = listOf(record.sleep, record.morning).count { it == DecisionStatus.NO }
    val label = when {
        answered == 0 -> "ДВА РЕШЕНИЯ.\nБЕЗ ПЕРЕГОВОРОВ."
        answered < 2 -> "ОДНО РЕШЕНИЕ\nОСТАЛОСЬ."
        failures == 0 -> "ДЕНЬ\nЗАКРЫТ."
        else -> "НАРУШЕНИЯ\nУСТАНОВЛЕНЫ"
    }
    val supporting = when {
        answered == 0 -> "Отметь факты. Система не требует идеальности — только честности."
        answered < 2 -> "Первый факт записан. Закрой вторую часть дня."
        failures == 0 -> "Оба правила соблюдены. Никаких очков и серий — только выполненный стандарт."
        else -> "Нарушение не обнуляет систему. Причина записана, следующий выбор остаётся твоим."
    }
    Column(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)) {
        Text(label, color = Paper, style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(12.dp))
        Text(supporting, color = Muted, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth(0.9f))
    }
}

@Composable
private fun ReminderPermissionCard(onEnable: () -> Unit) {
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
                Text("Включить напоминания", color = Paper, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(3.dp))
                Text("Отбой, подготовка ко сну и утренний триггер", color = Muted, style = MaterialTheme.typography.bodyMedium)
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
    reason: ViolationReason?,
    onYes: () -> Unit,
    onNo: () -> Unit,
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
    PremiumCard(background, if (status == DecisionStatus.YES) Acid.copy(alpha = 0.38f) else Hairline) {
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
                    Modifier.size(34.dp).background(if (status == DecisionStatus.YES) Acid else Danger, CircleShape),
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
        AnimatedVisibility(reason != null, enter = fadeIn(), exit = fadeOut()) {
            if (reason != null) {
                Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("ПРИЧИНА", color = Muted, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(8.dp))
                    Text(reason.label, color = Danger, style = MaterialTheme.typography.labelLarge)
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
private fun AdmissionProgress(target: Int, day: Int, total: Int, completed: Boolean) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column {
                Text(if (completed) "СТАНДАРТ" else "ТЕКУЩИЙ ДОПУСК", color = Muted, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Text("$target", color = Acid, style = MaterialTheme.typography.displayMedium)
            }
            Text(if (completed) "20 ПОДРЯД" else "ДЕНЬ $day ИЗ $total", color = Paper, style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(5.dp).background(Hairline, CircleShape)) {
            val fraction = if (completed) 1f else day / total.toFloat()
            val animated by animateFloatAsState(fraction, spring(stiffness = Spring.StiffnessLow), label = "admission")
            Box(Modifier.fillMaxWidth(animated).fillMaxHeight().background(Acid, CircleShape))
        }
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
private fun ReasonSheet(kind: DecisionKind, onDismiss: () -> Unit, onReason: (ViolationReason) -> Unit) {
    val haptics = LocalHapticFeedback.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceRaised,
        contentColor = Paper,
        dragHandle = { Box(Modifier.padding(top = 11.dp).size(42.dp, 4.dp).background(Hairline, CircleShape)) },
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 36.dp)) {
            Text("ЗАФИКСИРОВАТЬ ПРИЧИНУ", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(10.dp))
            Text(if (kind == DecisionKind.SLEEP) "Что сорвало вечер?" else "Что остановило утром?", color = Paper, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text("Без оправданий и самооценки — только факт для анализа.", color = Muted, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(22.dp))
            ViolationReason.entries.forEach { reason ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 9.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceSoft)
                        .border(1.dp, Hairline, RoundedCornerShape(16.dp))
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onReason(reason)
                        }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(8.dp).background(Danger, CircleShape))
                    Spacer(Modifier.width(13.dp))
                    Text(reason.label, color = Paper, style = MaterialTheme.typography.titleMedium)
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
private fun HistoryScreen(repository: SystemRepository, onNo: (LocalDate, DecisionKind) -> Unit) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    val today = LocalDate.now()

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { ScreenHeader("История", "Календарь", "Не серия. Карта фактов и повторяющихся решений.") }
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
            admissionTarget = SystemLogic.admissionFor(date, repository.settings.admissionStart).target,
            onDismiss = { selectedDate = null },
            onSleepYes = { repository.setSleep(date, DecisionStatus.YES) },
            onSleepNo = { selectedDate = null; onNo(date, DecisionKind.SLEEP) },
            onMorningYes = { repository.setMorning(date, DecisionStatus.YES) },
            onMorningNo = { selectedDate = null; onNo(date, DecisionKind.MORNING) },
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
    val good = listOf(record?.sleep, record?.morning).count { it == DecisionStatus.YES }
    val bad = listOf(record?.sleep, record?.morning).count { it == DecisionStatus.NO }
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
                if (record == null || (record.sleep == null && record.morning == null)) {
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
    PremiumCard(color = SurfaceSoft) {
        Text("ИТОГ МЕСЯЦА", color = Muted, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth()) {
            CompactMetric("СОН", sleep, Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(50.dp).background(Hairline))
            CompactMetric("УТРО", morning, Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(50.dp).background(Hairline))
            CompactMetric("ДНЕЙ", records.size, Modifier.weight(1f), suffix = "")
        }
    }
}

@Composable
private fun CompactMetric(label: String, value: Int?, modifier: Modifier, suffix: String = "%") {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value?.let { "$it$suffix" } ?: "—", color = Paper, style = MaterialTheme.typography.headlineMedium)
        Text(label, color = Muted, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun HistoryEditorSheet(
    date: LocalDate,
    record: DailyRecord,
    admissionTarget: Int,
    onDismiss: () -> Unit,
    onSleepYes: () -> Unit,
    onSleepNo: () -> Unit,
    onMorningYes: () -> Unit,
    onMorningNo: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = SurfaceRaised, contentColor = Paper) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 34.dp)) {
            Text(date.format(DayFormatter).uppercase(Ru), color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(7.dp))
            Text("Редактировать день", color = Paper, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(22.dp))
            HistoryDecisionRow("СОН", "Отбой и кровать вовремя", record.sleep, onSleepYes, onSleepNo)
            Spacer(Modifier.height(12.dp))
            HistoryDecisionRow("УТРО", "$admissionTarget ${pushupWord(admissionTarget)} подряд", record.morning, onMorningYes, onMorningNo)
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
    val today = LocalDate.now()
    val recentRecords = repository.records.values.filter {
        !it.date.isBefore(today.minusDays(29)) && !it.date.isAfter(today)
    }
    val sleep = SystemLogic.compliance(recentRecords) { it.sleep }
    val morning = SystemLogic.compliance(recentRecords) { it.morning }
    val reasons = ViolationReason.entries.associateWith { reason ->
        recentRecords.sumOf { record ->
            (if (record.sleep == DecisionStatus.NO && record.sleepReason == reason) 1 else 0) +
                (if (record.morning == DecisionStatus.NO && record.morningReason == reason) 1 else 0)
        }
    }

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { ScreenHeader("Анализ", "30 дней", "Не оценка характера. Только данные, которые помогают изменить среду.") }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricRing("СОН", sleep, Modifier.weight(1f))
                MetricRing("УТРО", morning, Modifier.weight(1f))
            }
        }
        item { LastSevenDays(repository.records, today) }
        item { ReasonAnalysis(reasons) }
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
private fun MetricRing(label: String, value: Int?, modifier: Modifier) {
    val target = (value ?: 0) / 100f
    val animated by animateFloatAsState(target, spring(stiffness = Spring.StiffnessLow), label = "metric")
    PremiumCard(modifier = modifier) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.2f), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().padding(12.dp)) {
                val stroke = 8.dp.toPx()
                drawArc(Hairline, -215f, 250f, false, style = Stroke(stroke, cap = StrokeCap.Round))
                drawArc(Acid, -215f, 250f * animated, false, style = Stroke(stroke, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(value?.let { "$it%" } ?: "—", color = Paper, style = MaterialTheme.typography.headlineLarge)
                Text(label, color = Muted, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun LastSevenDays(records: Map<LocalDate, DailyRecord>, today: LocalDate) {
    PremiumCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("ПОСЛЕДНИЕ 7 ДНЕЙ", color = Paper, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.2.sp)
            Text("СОН + УТРО", color = Muted, style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth().height(116.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            (6 downTo 0).map { today.minusDays(it.toLong()) }.forEach { date ->
                val record = records[date]
                val answered = listOf(record?.sleep, record?.morning).count { it != null }
                val yes = listOf(record?.sleep, record?.morning).count { it == DecisionStatus.YES }
                val fraction = if (answered == 0) 0.04f else yes / 2f
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.width(30.dp)) {
                    Box(
                        Modifier
                            .width(9.dp)
                            .height((74 * max(.06f, fraction)).dp)
                            .background(if (answered == 0) Hairline else if (yes == 2) Acid else if (yes == 1) Amber else Danger, CircleShape)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(date.dayOfWeek.getDisplayName(TextStyle.SHORT, Ru).take(2).uppercase(Ru), color = if (date == today) Paper else Muted, style = MaterialTheme.typography.labelMedium, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun ReasonAnalysis(reasons: Map<ViolationReason, Int>) {
    val maxCount = max(1, reasons.values.maxOrNull() ?: 0)
    val total = reasons.values.sum()
    PremiumCard {
        Text("ПРИЧИНЫ НАРУШЕНИЙ", color = Paper, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.2.sp)
        Spacer(Modifier.height(5.dp))
        Text(if (total == 0) "Пока нарушений с указанной причиной нет." else "$total зафиксированных причин за 30 дней", color = Muted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(20.dp))
        ViolationReason.entries.forEach { reason ->
            val count = reasons[reason] ?: 0
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(reason.label, color = if (count > 0) Paper else Muted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(76.dp))
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
    onUpdate: ((SystemSettings) -> SystemSettings) -> Unit,
    onEnableNotifications: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { ScreenHeader("Настройки", "Режим", "Время меняется здесь один раз — вечером решения уже не пересматриваются.") }
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
            }
        }
        item {
            SettingsGroup("НАПОМИНАНИЯ") {
                ToggleSettingRow(
                    "Локальные напоминания",
                    if (settings.notificationsEnabled) "Активны на этом устройстве" else "Нужно разрешение Android",
                    settings.notificationsEnabled,
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
            }
        }
        item {
            val admission = SystemLogic.admissionFor(LocalDate.now(), settings.admissionStart)
            SettingsGroup("РЕЖИМ ДОПУСКА") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text(if (admission.completed) "Стандарт активен" else "День ${admission.day} из ${admission.totalDays}", color = Paper, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(4.dp))
                        Text("10 → 12 → 14 → 16 → 18 → 20", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("${admission.target}", color = Acid, style = MaterialTheme.typography.displayMedium)
                }
                Spacer(Modifier.height(14.dp))
                Text("После 14-го дня приложение автоматически закрепляет правило: 20 отжиманий подряд каждое утро.", color = Muted, style = MaterialTheme.typography.bodyMedium)
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
