@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.personal.thesystem.ui

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import com.personal.thesystem.data.MapKitRuntime
import com.personal.thesystem.data.TransitOption
import com.personal.thesystem.data.TransitRoutesState
import com.personal.thesystem.data.YandexTransitController
import com.personal.thesystem.data.plannedMorningDate
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
import com.personal.thesystem.notifications.CutoffMusicService
import com.personal.thesystem.notifications.ReminderReceiver
import com.personal.thesystem.ui.theme.Acid
import com.personal.thesystem.ui.theme.AcidDim
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
internal fun SettingsScreen(
    settings: SystemSettings,
    onUpdate: ((SystemSettings) -> SystemSettings) -> Unit,
    onEnableNotifications: () -> Unit,
    onRequestExactAlarmAccess: () -> Unit,
    exactAlarmsAllowed: Boolean,
) {
    val context = LocalContext.current
    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { ScreenHeader("Настройки", "Режим") }
        item {
            SettingsGroup("РАСПИСАНИЕ") {
                TimeSettingRow("Цифровой отбой", settings.digitalCutoff) { delta ->
                    onUpdate { it.copy(digitalCutoff = it.digitalCutoff.plusMinutes(delta.toLong())) }
                }
                SettingsDivider()
                TimeSettingRow("В кровати", settings.bedTime) { delta ->
                    onUpdate { it.copy(bedTime = it.bedTime.plusMinutes(delta.toLong())) }
                }
                SettingsDivider()
                TimeSettingRow("Утренний триггер", settings.morningTime) { delta ->
                    onUpdate { it.copy(morningTime = it.morningTime.plusMinutes(delta.toLong())) }
                }
                SettingsDivider()
                TimeSettingRow("Питание", settings.dietTime) { delta ->
                    onUpdate { it.copy(dietTime = it.dietTime.plusMinutes(delta.toLong())) }
                }
            }
        }
        item {
            SettingsGroup("РЕЖИМ ВШЭ") {
                ToggleSettingRow(
                    "Учебный режим",
                    "",
                    settings.hseModeEnabled,
                ) { enabled -> onUpdate { it.copy(hseModeEnabled = enabled) } }
                if (settings.hseModeEnabled) {
                    SettingsDivider()
                    AddressSettingRow(
                        title = "Домашний адрес",
                        value = settings.hseHomeAddress,
                        placeholder = "Улица, дом",
                    ) { address -> onUpdate { it.copy(hseHomeAddress = address.take(120)) } }
                    SettingsDivider()
                    Column {
                        Text("Корпус ВШЭ", color = Paper, style = MaterialTheme.typography.titleMedium)
                        Text(settings.hseUniversityAddress, color = Acid, style = MaterialTheme.typography.bodyMedium)
                    }
                    SettingsDivider()
                    Column {
                        Text("Календарь занятий", color = Paper, style = MaterialTheme.typography.titleMedium)
                        Text(settings.hseCalendarName.ifBlank { "Выбери на главной странице режима ВШЭ" }, color = Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item {
            SettingsGroup("MONEY") {
                MoneySettingRow("Перевод на полмесяца", settings.moneyTransferRubles, 500L) { value ->
                    onUpdate { it.copy(moneyTransferRubles = value.coerceIn(1_000L, 1_000_000L)) }
                }
                SettingsDivider()
                MoneySettingRow("Защищённый резерв", settings.moneyReservePerTransferRubles, 500L) { value ->
                    onUpdate { it.copy(moneyReservePerTransferRubles = value.coerceIn(0L, settings.moneyTransferRubles)) }
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
                        else -> ""
                    },
                    settings.notificationsEnabled && exactAlarmsAllowed,
                ) { enabled ->
                    if (enabled) onEnableNotifications() else onUpdate { it.copy(notificationsEnabled = false) }
                }
                SettingsDivider()
                ToggleSettingRow("Предупреждение", SystemLogic.formatTime(settings.digitalCutoff.minusMinutes(15)), settings.warningEnabled) { value -> onUpdate { it.copy(warningEnabled = value) } }
                SettingsDivider()
                ToggleSettingRow(
                    "Цифровой отбой",
                    ReminderScheduler.cutoffMusicTimes(settings.digitalCutoff).joinToString(" · ", transform = SystemLogic::formatTime),
                    settings.cutoffEnabled,
                ) { value ->
                    onUpdate { it.copy(cutoffEnabled = value) }
                    if (value && !exactAlarmsAllowed) onRequestExactAlarmAccess()
                }
                SettingsDivider()
                ToggleSettingRow("Подготовка ко сну", SystemLogic.formatTime(settings.bedTime.minusMinutes(30)), settings.preparationEnabled) { value -> onUpdate { it.copy(preparationEnabled = value) } }
                SettingsDivider()
                ToggleSettingRow("В кровати", SystemLogic.formatTime(settings.bedTime), settings.bedEnabled) { value -> onUpdate { it.copy(bedEnabled = value) } }
                SettingsDivider()
                ToggleSettingRow("Утренние отжимания", SystemLogic.formatTime(settings.morningTime), settings.morningEnabled) { value -> onUpdate { it.copy(morningEnabled = value) } }
                SettingsDivider()
                ToggleSettingRow("Питание", SystemLogic.formatTime(settings.dietTime), settings.dietEnabled) { value -> onUpdate { it.copy(dietEnabled = value) } }
                SettingsDivider()
                HseActionButton("ПРОВЕРИТЬ УВЕДОМЛЕНИЕ") { ReminderReceiver.showTest(context) }
                Spacer(Modifier.height(9.dp))
                HseActionButton("ПРОВЕРИТЬ ОТБОЙ СЕЙЧАС") {
                    CutoffMusicService.start(context)
                }
                Spacer(Modifier.height(9.dp))
                HseActionButton("ВЫКЛЮЧИТЬ МУЗЫКУ ОТБОЯ") { CutoffMusicService.stop(context) }
            }
        }
        if (settings.hseModeEnabled) item {
            Text(
                "Условия Яндекс Карт",
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

@Composable
private fun SettingsGroup(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(label, color = Muted, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.6.sp, modifier = Modifier.padding(start = 4.dp, bottom = 9.dp))
        PremiumCard(content = content)
    }
}

@Composable
private fun TimeSettingRow(title: String, value: LocalTime, onAdjust: (Int) -> Unit) {
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Paper, style = MaterialTheme.typography.titleMedium)
        }
        MiniButton("−", "Уменьшить время") { onAdjust(-5) }
        Text(
            SystemLogic.formatTime(value),
            color = Acid,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(72.dp).clickable {
                android.app.TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val selected = LocalTime.of(hour, minute)
                        onAdjust(java.time.Duration.between(value, selected).toMinutes().toInt())
                    },
                    value.hour,
                    value.minute,
                    true,
                ).show()
            }.padding(vertical = 12.dp),
        )
        MiniButton("+", "Увеличить время") { onAdjust(5) }
    }
}

@Composable
private fun AddressSettingRow(
    title: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    var draft by remember(value) { mutableStateOf(value) }
    Column {
        Text(title, color = Paper, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))
        BasicTextField(
            value = draft,
            onValueChange = { input -> draft = input.replaceFirstChar { it.uppercase() } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Paper),
            cursorBrush = SolidColor(Acid),
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(SurfaceSoft)
                .padding(horizontal = 13.dp, vertical = 12.dp),
            decorationBox = { inner ->
                Box {
                    if (draft.isBlank()) Text(placeholder, color = Muted, style = MaterialTheme.typography.bodyLarge)
                    inner()
                }
            },
        )
        if (draft != value) {
            Spacer(Modifier.height(9.dp))
            HseActionButton("СОХРАНИТЬ АДРЕС") { onValueChange(draft) }
        }
    }
}

@Composable
internal fun MiniButton(text: String, description: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
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
private fun MoneySettingRow(title: String, value: Long, step: Long, onChange: (Long) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Paper, style = MaterialTheme.typography.titleMedium)
            Text(formatMoney(value), color = Acid, style = MaterialTheme.typography.bodyLarge)
        }
        MiniButton("−", "Уменьшить $title") { onChange((value - step).coerceAtLeast(0L)) }
        Spacer(Modifier.width(8.dp))
        MiniButton("+", "Увеличить $title") { onChange(value + step) }
    }
}

@Composable
private fun ToggleSettingRow(title: String, detail: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Paper, style = MaterialTheme.typography.titleMedium)
            if (detail.isNotBlank()) Text(detail, color = Muted, style = MaterialTheme.typography.bodyMedium)
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
