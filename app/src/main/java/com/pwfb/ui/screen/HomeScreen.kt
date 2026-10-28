package com.pwfb.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pwfb.R
import com.pwfb.common.NutritionObject
import com.pwfb.domain.entity.PrefKey
import com.pwfb.ui.model.NutrientUi
import com.pwfb.ui.model.NutritionIntake
import com.pwfb.ui.model.PeakWeekText
import com.pwfb.ui.viewmodel.MainViewModel
import com.pwfb.util.calcDDay

private const val PROGRESS_SEGMENTS = 8 // D-7 ~ D-DAY

/**
 * 홈 탭: D-Day 히어로 카드, 공복 체중 · 오늘 트레이닝, 오늘의 영양 가이드
 */
@Composable
fun HomeScreen(viewModel: MainViewModel, contentPadding: PaddingValues) {
    val weightText by viewModel.state(PrefKey.WEIGHT).collectAsStateWithLifecycle()
    val dDayText by viewModel.state(PrefKey.D_DAY).collectAsStateWithLifecycle()

    // 아직 읽기 전(빈 문자열)이면 값 없음으로 둔다
    val kg = weightText.toDoubleOrNull()
    val dDay = calcDDay(dDayText)

    TabColumn(contentPadding = contentPadding) {
        HeroCard(dDay = dDay, dateText = dDayText)

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            PwfbCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Eyebrow(text = stringResource(R.string.kr_empty_stomach_weight))
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (kg != null) weightText + stringResource(R.string.kg) else "",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.empty_stomach_weight_tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            PwfbCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Eyebrow(text = stringResource(R.string.home_training_title))
                Spacer(Modifier.height(8.dp))
                Text(
                    text = dDay?.let { PeakWeekText.homeTraining(it).text() } ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        SectionTitle(text = stringResource(R.string.home_nutrition_title))
        Spacer(Modifier.height(12.dp))
        PwfbCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            if (kg != null && dDay != null) {
                NutritionObject.nutritionEntityLists.forEachIndexed { index, entity ->
                    NutritionRow(
                        ui = NutrientUi.of(entity.type),
                        explain = stringResource(entity.explainRes),
                        intake = NutritionIntake.of(entity.type, dDay, kg).text(),
                        showDivider = index != NutritionObject.nutritionEntityLists.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCard(dDay: Int?, dateText: String) {
    PwfbCard(
        modifier = Modifier.fillMaxWidth(),
        hero = true,
        contentPadding = PaddingValues(24.dp)
    ) {
        Eyebrow(text = stringResource(R.string.d_day), color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(
            text = dDay?.let { "D$it" } ?: "",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = dateText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))
        ProgressStrip(dDay = dDay)
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.progress_start),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.d_day),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))
        Eyebrow(text = stringResource(R.string.peak_week_strategy))
        Spacer(Modifier.height(8.dp))
        Text(
            text = dDay?.let { PeakWeekText.strategy(it).text() } ?: "",
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * D-7 → D-DAY 진행 스트립. i번째 칸은 D-(7-i)를 뜻하며, 오늘이 그 날 이후면 채운다.
 */
@Composable
private fun ProgressStrip(dDay: Int?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(PROGRESS_SEGMENTS) { index ->
            val segmentDay = index - (PROGRESS_SEGMENTS - 1)
            val reached = dDay != null && dDay >= segmentDay
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

@Composable
private fun NutritionRow(ui: NutrientUi, explain: String, intake: String, showDivider: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        NutrientAvatar(ui = ui)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = explain,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = intake,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (showDivider) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}
