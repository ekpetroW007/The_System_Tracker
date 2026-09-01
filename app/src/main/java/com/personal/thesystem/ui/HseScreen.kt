@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.personal.thesystem.ui

import android.Manifest
import android.animation.ValueAnimator
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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

@Composable
internal fun HseDashboardScreen(
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
    val calendarSources by produceState<List<HseCalendarSource>>(
        initialValue = emptyList(),
        calendarAccessAllowed,
    ) {
        value = if (calendarAccessAllowed) withContext(Dispatchers.IO) { HseCalendarReader.calendars(context) } else emptyList()
    }
    val calendarEvents by produceState<List<HseCalendarEvent>>(
        initialValue = emptyList(),
        calendarAccessAllowed,
        today,
        settings.hseCalendarId,
    ) {
        value = if (calendarAccessAllowed) withContext(Dispatchers.IO) {
            HseCalendarReader.eventsFor(context, today, settings.hseCalendarId)
        } else emptyList()
    }
    val nextLesson = calendarEvents.firstOrNull { !it.end.isBefore(currentTime) }
    val record = repository.recordFor(today)
    val activeTasks = SystemLogic.activeTasks(today, settings)
    val statuses = activeTasks.map { SystemLogic.statusFor(record, it) }
    val completed = statuses.count { it == DecisionStatus.YES }
    val evening = !currentTime.isBefore(settings.digitalCutoff)
    val mapKitReady = remember { MapKitRuntime.start(context) }
    DisposableEffect(Unit) { onDispose { MapKitRuntime.stop() } }
    val transitController = remember(mapKitReady) { if (mapKitReady) YandexTransitController() else null }
    val transitState = transitController?.state ?: TransitRoutesState.Failed("Ключ Яндекс Карт не настроен для этой сборки")
    val savedTransitPlan = repository.hseTransitPlan
    val transitDate = plannedMorningDate(today, currentTime, savedTransitPlan?.targetDate)
    val transitEvents by produceState<List<HseCalendarEvent>>(
        initialValue = emptyList(),
        calendarAccessAllowed,
        transitDate,
        settings.hseCalendarId,
    ) {
        value = if (calendarAccessAllowed) withContext(Dispatchers.IO) {
            HseCalendarReader.eventsFor(context, transitDate, settings.hseCalendarId)
        } else emptyList()
    }
    val targetLesson = transitEvents.firstOrNull()
    val routeTargetTime = targetLesson?.start?.minusMinutes(10) ?: HSE_ROUTE_TIME

    LaunchedEffect(
        settings.hseHomeAddress,
        settings.hseUniversityAddress,
        transitDate,
        routeTargetTime,
    ) {
        if (settings.hseHomeAddress.isNotBlank()) {
            val saved = repository.hseTransitPlan
            if (
                saved?.targetDate == transitDate &&
                saved.targetTime == routeTargetTime &&
                saved.homeAddress == settings.hseHomeAddress &&
                saved.universityAddress == settings.hseUniversityAddress
            ) {
                transitController?.showSaved(saved)
            } else {
                transitController?.refresh(
                    settings.hseHomeAddress,
                    settings.hseUniversityAddress,
                    transitDate,
                    routeTargetTime,
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
                        .background(Color(0xFF111112))
                        .clickable(onClick = onToggleHseMode)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Выключить режим ВШЭ"
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("В", color = HseGold, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        if (evening) {
            item {
                PremiumCard(color = Color(0xFF0B0B0C), border = HseGold.copy(alpha = .26f)) {
                    Text("УЧЕБНЫЙ ДЕНЬ ЗАВЕРШЁН", color = HseGold, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.4.sp)
                    Spacer(Modifier.height(9.dp))
                    Text("Сейчас приоритет — сон.", color = Paper, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(5.dp))
                    Text("Расписание и дорога приглушены до утра. Личная система остаётся доступна.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (calendarAccessAllowed && settings.hseCalendarId == null) {
            item {
                PremiumCard(color = Color(0xFF111112), border = HseGold.copy(alpha = .42f)) {
                    Text("ВЫБЕРИ КАЛЕНДАРЬ ВШЭ", color = HseGold, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.4.sp)
                    Spacer(Modifier.height(7.dp))
                    Text("Личные события больше не подставляются вместо пар.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    if (calendarSources.isEmpty()) {
                        Text("Доступных календарей пока нет.", color = Paper, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        calendarSources.forEach { source ->
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SurfaceSoft)
                                    .clickable {
                                        repository.updateSettings {
                                            it.copy(hseCalendarId = source.id, hseCalendarName = source.name)
                                        }
                                    }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Text(source.name, color = Paper, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
        item {
            PremiumCard(
                color = Color(0xFF111112).copy(alpha = if (evening) .55f else 1f),
                border = HseGold.copy(alpha = if (evening) .14f else .34f),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("СЛЕДУЮЩАЯ ПАРА", color = HseGold, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.4.sp)
                    Text(nextLesson?.start?.let(SystemLogic::formatTime) ?: "—", color = Paper, style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(15.dp))
                when {
                    !calendarAccessAllowed -> {
                        Text("Подключи календарь ВШЭ", color = Paper, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(6.dp))
                        Text("The System прочитает только события календаря Android. Пароль ВШЭ приложению не нужен.", color = Muted, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        HseActionButton("ПОДКЛЮЧИТЬ КАЛЕНДАРЬ", color = HseGold, onClick = onRequestCalendarAccess)
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
            PremiumCard(color = Color(0xFF0D0D0E).copy(alpha = if (evening) .55f else 1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Text("АВТОБУС В ВШЭ", color = HseGold, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.4.sp)
                    Text(
                        "${if (transitDate == today) "СЕГОДНЯ" else "ЗАВТРА"} · ${SystemLogic.formatTime(routeTargetTime)}",
                        color = Paper,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Spacer(Modifier.height(15.dp))
                if (settings.hseHomeAddress.isBlank()) {
                    HseActionButton("УКАЗАТЬ АДРЕС", color = HseGold, onClick = onOpenSettings)
                } else {
                    when (val routeState = transitState) {
                        TransitRoutesState.Idle,
                        TransitRoutesState.Loading -> {
                    Text("Собираю маршрут к ${SystemLogic.formatTime(routeTargetTime)}…", color = Paper, style = MaterialTheme.typography.bodyMedium)
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
                                color = HseGold,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.clickable {
                                    transitController?.refresh(
                                        settings.hseHomeAddress,
                                        settings.hseUniversityAddress,
                                        transitDate,
                                        routeTargetTime,
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
            PremiumCard(color = Color(0xFF101011).copy(alpha = if (evening) .55f else 1f)) {
                Text("РАСПИСАНИЕ ДНЯ", color = HseGold, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.4.sp)
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
                color = Color(0xFF121213),
                border = if (completed < activeTasks.size) HseGold.copy(alpha = .36f) else Hairline,
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("ЛИЧНАЯ СИСТЕМА", color = HseGold, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.4.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("$completed из ${activeTasks.size} выполнено", color = Paper, style = MaterialTheme.typography.titleLarge)
                    }
                    Text("→", color = HseGold, style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    statuses.forEach { status ->
                        Box(
                            Modifier.weight(1f).height(5.dp).background(
                                when (status) {
                                    DecisionStatus.YES -> HseGold
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
        Box(Modifier.size(7.dp).background(HseGold, CircleShape))
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
        if (option.leaveHomeTime.isNotBlank()) {
            Spacer(Modifier.height(5.dp))
            Text("Выйти из дома в ${option.leaveHomeTime}", color = HseGold, style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(13.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("БУДЕТ НА ОСТАНОВКЕ", color = Muted, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(3.dp))
                Text(option.busArrivalTime, color = HseGold, style = MaterialTheme.typography.titleLarge)
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
        Text("${option.walkToStopMeters} м пешком от дома", color = Muted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 64.dp, top = 3.dp))
        Spacer(Modifier.height(11.dp))
        Text("↓", color = HseGold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 4.dp))
        Spacer(Modifier.height(11.dp))
        TransitStopRow("ВЫЙТИ", option.exitStop)
        Text("${option.walkToUniversityMeters} м пешком до ВШЭ", color = Muted, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 64.dp, top = 3.dp))
    }
}

@Composable
private fun TransitStopRow(label: String, stop: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(label, color = HseGold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(64.dp))
        Text(stop, color = Paper, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
internal fun HseActionButton(label: String, enabled: Boolean = true, color: Color = Acid, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(15.dp))
            .background(if (enabled) color else Hairline).clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (enabled) Ink else Muted, style = MaterialTheme.typography.labelLarge)
    }
}
