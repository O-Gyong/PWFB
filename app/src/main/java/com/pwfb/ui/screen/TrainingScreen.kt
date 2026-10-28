package com.pwfb.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pwfb.R
import com.pwfb.domain.entity.PrefKey
import com.pwfb.ui.model.PeakWeekText
import com.pwfb.ui.viewmodel.MainViewModel
import com.pwfb.util.calcDDay

/**
 * 트레이닝 탭: 오늘의 트레이닝 안내와 나의 운동 계획 기록
 */
@Composable
fun TrainingScreen(viewModel: MainViewModel, contentPadding: PaddingValues) {
    val dDayText by viewModel.state(PrefKey.D_DAY).collectAsStateWithLifecycle()
    val storedPlan by viewModel.state(PrefKey.TRAINING_PROGRAM).collectAsStateWithLifecycle()
    val dDay = calcDDay(dDayText)

    TabColumn(contentPadding = contentPadding) {
        DdayHeaderCard(dDay = dDay, dateText = dDayText)

        Spacer(Modifier.height(12.dp))
        PwfbCard(
            modifier = Modifier.fillMaxWidth(),
            hero = true,
            contentPadding = PaddingValues(24.dp)
        ) {
            Eyebrow(text = stringResource(R.string.home_training_title), color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(
                text = dDay?.let { PeakWeekText.training(it).text() } ?: "",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(24.dp))
        SectionTitle(text = stringResource(R.string.training_plan_title))
        Spacer(Modifier.height(12.dp))
        TrainingPlanCard(
            stored = storedPlan,
            onSave = { viewModel.save(PrefKey.TRAINING_PROGRAM, it) }
        )
    }
}

/**
 * 운동 계획 입력. 여러 줄이라 완료 키가 없으므로 포커스 이탈·화면 이탈 시 저장한다.
 */
@Composable
private fun TrainingPlanCard(stored: String, onSave: (String) -> Unit) {
    var text by rememberSaveable(stored) { mutableStateOf(stored) }
    val latestText by rememberUpdatedState(text)
    val latestStored by rememberUpdatedState(stored)

    DisposableEffect(Unit) {
        onDispose { if (latestText != latestStored) onSave(latestText) }
    }

    PwfbCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(
            text = stringResource(R.string.training_plan_tip),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (!it.isFocused && text != stored) onSave(text) },
            label = { Text(stringResource(R.string.write_training_plan)) },
            minLines = 8,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
    }
}
