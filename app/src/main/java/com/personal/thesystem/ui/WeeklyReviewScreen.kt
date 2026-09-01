package com.personal.thesystem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.thesystem.data.SystemRepository
import com.personal.thesystem.model.WeeklyReview
import com.personal.thesystem.ui.theme.Acid
import com.personal.thesystem.ui.theme.HseGold
import com.personal.thesystem.ui.theme.Ink
import com.personal.thesystem.ui.theme.Muted
import com.personal.thesystem.ui.theme.Paper
import com.personal.thesystem.ui.theme.SurfaceSoft

@Composable
internal fun WeeklyReviewScreen(
    repository: SystemRepository,
    onDone: () -> Unit,
) {
    val dueDate = repository.weeklyReviewDueDate
    val review = (if (dueDate != null) repository.weeklyReviewDraft else repository.weeklyReview)
        ?: WeeklyReview(dueDate ?: java.time.LocalDate.now())
    val accent = if (repository.settings.hseModeEnabled) HseGold else Acid

    LazyColumn(
        Modifier.fillMaxSize().background(Ink).statusBarsPadding(),
        contentPadding = PaddingValues(20.dp, 22.dp, 20.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Text("ПОДВЕДЕМ ИТОГИ!", color = accent, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(10.dp))
            Text("Следующая неделя", color = Paper, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(5.dp))
            Text(review.date.format(DayFormatter), color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        item {
            WeeklyReviewField("Что сдаётся на следующей неделе?", review.nextWeekDeadlines, accent) {
                repository.updateWeeklyReview { current -> current.copy(nextWeekDeadlines = it.take(800)) }
            }
        }
        item {
            WeeklyReviewField("Какие проверочные работы будут на следующей неделе?", review.tests, accent) {
                repository.updateWeeklyReview { current -> current.copy(tests = it.take(800)) }
            }
        }
        item {
            WeeklyReviewField("По какому предмету у меня самая слабая позиция?", review.weakestSubject, accent) {
                repository.updateWeeklyReview { current -> current.copy(weakestSubject = it.take(800)) }
            }
        }
        item {
            WeeklyReviewField("Что надо начать заранее?", review.startEarly, accent) {
                repository.updateWeeklyReview { current -> current.copy(startEarly = it.take(800)) }
            }
        }
        item { HseActionButton("ГОТОВО", color = accent, onClick = onDone) }
    }
}

@Composable
private fun WeeklyReviewField(
    question: String,
    answer: String,
    accent: androidx.compose.ui.graphics.Color,
    onAnswerChange: (String) -> Unit,
) {
    PremiumCard {
        Text(question, color = Paper, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))
        BasicTextField(
            value = answer,
            onValueChange = onAnswerChange,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Paper),
            cursorBrush = SolidColor(accent),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 82.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(SurfaceSoft)
                .padding(15.dp),
            decorationBox = { inner ->
                Box {
                    if (answer.isBlank()) Text("Ответ", color = Muted, style = MaterialTheme.typography.bodyLarge)
                    inner()
                }
            },
        )
    }
}
