package com.pwfb.ui.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pwfb.R
import com.pwfb.theme.PwfbTheme
import com.pwfb.ui.viewmodel.DdayViewModel
import com.pwfb.util.formatDatePref
import com.pwfb.util.formatTimeDisplay
import java.time.LocalDate
import java.time.LocalTime

/**
 * 온보딩 3단계: 대회 날짜와 시간. 저장되면 온보딩이 끝나고 메인으로 간다.
 */
@Composable
fun DdayScreen(
    onDone: () -> Unit,
    onSaveFailed: () -> Unit,
    viewModel: DdayViewModel = hiltViewModel()
) {
    LaunchedEffect(viewModel) {
        viewModel.completed.collect { success -> if (success) onDone() else onSaveFailed() }
    }

    DdayContent(onSubmit = viewModel::setDDay)
}

@Composable
private fun DdayContent(onSubmit: (date: LocalDate, hour: Int, minute: Int) -> Unit) {
    // 날짜는 고르기 전까지 비어 있고, 시간은 현재 시각이 기본값
    var dateEpochDay by rememberSaveable { mutableStateOf<Long?>(null) }
    val now = LocalTime.now()
    var hour by rememberSaveable { mutableIntStateOf(now.hour) }
    var minute by rememberSaveable { mutableIntStateOf(now.minute) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    val date = dateEpochDay?.let(LocalDate::ofEpochDay)

    OnboardingStep(
        step = 3,
        title = stringResource(R.string.d_day),
        subtitle = stringResource(R.string.d_day_subtitle),
        buttonText = stringResource(R.string.go),
        buttonEnabled = date != null,
        onButtonClick = { date?.let { onSubmit(it, hour, minute) } }
    ) {
        PwfbCard(modifier = Modifier.fillMaxWidth()) {
            Eyebrow(text = stringResource(R.string.competition_date))
            Spacer(Modifier.height(8.dp))
            TonalButton(
                text = date?.let(::formatDatePref) ?: stringResource(R.string.select_date),
                iconRes = R.drawable.ic_calendar,
                onClick = { showDatePicker = true }
            )
            Spacer(Modifier.height(20.dp))
            Eyebrow(text = stringResource(R.string.time))
            Spacer(Modifier.height(8.dp))
            TonalButton(
                text = formatTimeDisplay(hour, minute),
                iconRes = R.drawable.ic_schedule,
                onClick = { showTimePicker = true }
            )
        }
    }

    if (showDatePicker) {
        PwfbDatePickerDialog(
            initial = date,
            onDismiss = { showDatePicker = false },
            onConfirm = { picked ->
                dateEpochDay = picked.toEpochDay()
                showDatePicker = false
            }
        )
    }

    if (showTimePicker) {
        PwfbTimePickerDialog(
            hour = hour,
            minute = minute,
            onDismiss = { showTimePicker = false },
            onConfirm = { h, m ->
                hour = h
                minute = m
                showTimePicker = false
            }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121110)
@Composable
private fun DdayPreview() {
    PwfbTheme { DdayContent(onSubmit = { _, _, _ -> }) }
}
