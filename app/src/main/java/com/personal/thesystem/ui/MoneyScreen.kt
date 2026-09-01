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
internal fun MoneyScreen(repository: SystemRepository) {
    val today = LocalDate.now()
    val period = SystemLogic.moneyPeriodFor(today)
    val snapshot = SystemLogic.moneySnapshot(
        today,
        repository.moneyTransactions,
        repository.moneyReceivedPeriods,
        repository.moneyCommitments,
        repository.settings.moneyTransferRubles,
        repository.settings.moneyReservePerTransferRubles,
    )
    val periodTransactions = repository.moneyTransactions.filter {
        !it.date.isBefore(period.start) && !it.date.isAfter(period.end)
    }
    val previousPeriod = SystemLogic.previousMoneyPeriod(today)
    val previousHasData = !previousPeriod.start.isBefore(SystemLogic.MONEY_START_DATE) &&
        (previousPeriod.start in repository.moneyReceivedPeriods || repository.moneyTransactions.any {
            !it.date.isBefore(previousPeriod.start) && !it.date.isAfter(previousPeriod.end)
        })
    var addingExpense by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<MoneyTransaction?>(null) }
    var addingCommitment by remember { mutableStateOf(false) }
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
                    Text("СТАРТ 30 АВГУСТА", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.6.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("${formatMoney(repository.settings.moneyTransferRubles)} на полмесяца", color = Paper, style = MaterialTheme.typography.headlineMedium)
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
                        Text("Подтверди получение ${formatMoney(repository.settings.moneyTransferRubles)}", color = Paper, style = MaterialTheme.typography.titleLarge)
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
                                Text("${formatMoney(repository.settings.moneyTransferRubles)} ПОЛУЧЕНЫ", color = Paper, style = MaterialTheme.typography.labelLarge)
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
            repository.moneyTransactions.firstOrNull()?.let { recent ->
                item {
                    HseActionButton("ПОВТОРИТЬ · ${recent.category.label.uppercase(Ru)} · ${formatMoney(recent.amountRubles)}") {
                        repository.addMoneyExpense(recent.amountRubles, recent.category, recent.planned)
                    }
                }
            }
            item { MoneyPeriodSummary(snapshot) }
            item {
                PremiumCard(color = SurfaceSoft) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("ОБЯЗАТЕЛЬНЫЕ ТРАТЫ", color = Muted, style = MaterialTheme.typography.labelMedium)
                            Text(formatMoney(snapshot.mandatoryRemaining), color = Paper, style = MaterialTheme.typography.titleLarge)
                        }
                        Text(
                            "+ ДОБАВИТЬ",
                            color = Acid,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.clickable { addingCommitment = true }.padding(10.dp),
                        )
                    }
                    repository.moneyCommitments.forEach { commitment ->
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = Hairline)
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(commitment.title, color = Paper, style = MaterialTheme.typography.titleMedium)
                                Text(commitment.category.label, color = Muted, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(formatMoney(commitment.amountRubles), color = Paper, style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.width(8.dp))
                            Text("×", color = Danger, fontSize = 22.sp, modifier = Modifier.clickable { repository.deleteMoneyCommitment(commitment.id) }.padding(8.dp))
                        }
                    }
                }
            }
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
                        periodTransactions.sortedWith(compareByDescending<MoneyTransaction> { it.date }.thenByDescending { it.id }).forEachIndexed { index, transaction ->
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
                                onEdit = { editingExpense = transaction },
                            )
                            if (index != periodTransactions.lastIndex) {
                                HorizontalDivider(color = Hairline, modifier = Modifier.padding(vertical = 12.dp))
                            }
                        }
                    }
                }
            }
            if (previousHasData) {
                item {
                    MoneyReportCard(
                        SystemLogic.moneyReport(
                            previousPeriod,
                            repository.moneyTransactions,
                            repository.moneyReceivedPeriods,
                            repository.settings.moneyTransferRubles,
                        )
                    )
                }
            }
        }
    }

    if (addingExpense) {
        AddMoneyExpenseSheet(
            existing = null,
            onDismiss = { addingExpense = false },
            onAdd = { amount, category, planned ->
                repository.addMoneyExpense(amount, category, planned)
                addingExpense = false
            },
        )
    }
    editingExpense?.let { transaction ->
        AddMoneyExpenseSheet(
            existing = transaction,
            onDismiss = { editingExpense = null },
            onAdd = { amount, category, planned ->
                repository.updateMoneyExpense(transaction.copy(amountRubles = amount, category = category, planned = planned))
                editingExpense = null
            },
        )
    }
    if (addingCommitment) {
        AddMoneyCommitmentSheet(
            onDismiss = { addingCommitment = false },
            onAdd = { title, amount, category ->
                repository.addMoneyCommitment(title, amount, category)
                addingCommitment = false
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
    val totalAvailable = snapshot.balance + snapshot.spent
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
        if (snapshot.mandatoryRemaining > 0L) {
            Spacer(Modifier.height(9.dp))
            Text("Уже отложено на обязательные траты: ${formatMoney(snapshot.mandatoryRemaining)}", color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(15.dp))
        Text(moneyForecastText(snapshot), color = statusColor, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MoneyHeroMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Muted, style = MaterialTheme.typography.labelMedium, fontSize = 12.sp)
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
        Text(label, color = Muted, style = MaterialTheme.typography.labelMedium, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun MoneyTransactionRow(
    transaction: MoneyTransaction,
    today: LocalDate,
    deleteArmed: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onEdit).padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
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
    existing: MoneyTransaction?,
    onDismiss: () -> Unit,
    onAdd: (Long, MoneyCategory, Boolean) -> Unit,
) {
    var amount by remember(existing?.id) { mutableStateOf(existing?.amountRubles?.toString().orEmpty()) }
    var category by remember(existing?.id) { mutableStateOf(existing?.category ?: MoneyCategory.GROCERIES) }
    var planned by remember(existing?.id) { mutableStateOf(existing?.planned ?: true) }
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
            Text(if (existing == null) "НОВЫЙ РАСХОД" else "ИЗМЕНИТЬ РАСХОД", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.6.sp)
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

@Composable
private fun AddMoneyCommitmentSheet(
    onDismiss: () -> Unit,
    onAdd: (String, Long, MoneyCategory) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MoneyCategory.GROCERIES) }
    val amountValue = amount.toLongOrNull()?.takeIf { it in 1L..1_000_000L }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceRaised,
        contentColor = Paper,
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("ОБЯЗАТЕЛЬНАЯ ТРАТА", color = Acid, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.5.sp)
            AssignmentTextField(title, { title = it }, "Например, транспорт")
            BasicTextField(
                value = amount,
                onValueChange = { amount = it.filter(Char::isDigit).take(7) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.headlineMedium.copy(color = Paper),
                cursorBrush = SolidColor(Acid),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceSoft).padding(16.dp),
                decorationBox = { inner ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) {
                            if (amount.isBlank()) Text("Сумма", color = Muted, style = MaterialTheme.typography.bodyLarge)
                            inner()
                        }
                        Text("₽", color = Acid, style = MaterialTheme.typography.titleLarge)
                    }
                },
            )
            MoneyCategory.entries.chunked(2).forEach { rowCategories ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    rowCategories.forEach { option ->
                        val selected = category == option
                        Box(
                            Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(13.dp))
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
            HseActionButton("СОХРАНИТЬ", enabled = title.isNotBlank() && amountValue != null) {
                amountValue?.let { onAdd(title, it, category) }
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

internal fun formatMoney(value: Long): String = "${NumberFormat.getIntegerInstance(Ru).format(value)} ₽"

private fun dayWord(value: Long): String = when {
    value % 100 in 11L..14L -> "дней"
    value % 10 == 1L -> "день"
    value % 10 in 2L..4L -> "дня"
    else -> "дней"
}
