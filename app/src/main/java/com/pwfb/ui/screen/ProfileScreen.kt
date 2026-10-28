package com.pwfb.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pwfb.R
import com.pwfb.domain.entity.PrefKey
import com.pwfb.ui.viewmodel.DdayViewModel
import com.pwfb.ui.viewmodel.MainViewModel
import com.pwfb.util.calcDDay
import com.pwfb.util.formatDatePref
import com.pwfb.util.formatTimeDisplay
import com.pwfb.util.parseDatePref
import com.pwfb.util.parseTimePref
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt

/**
 * 프로필 탭: 이름·이상 체중, 목표(공복 체중 · 대회 D-Day) 수정, 당일 전략 · 앱 정보 · 로그 전송
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel, contentPadding: PaddingValues) {
    val name by viewModel.state(PrefKey.NAME).collectAsStateWithLifecycle()
    val storedWeightText by viewModel.state(PrefKey.WEIGHT).collectAsStateWithLifecycle()
    val dDayText by viewModel.state(PrefKey.D_DAY).collectAsStateWithLifecycle()

    val storedWeight = storedWeightText.toDoubleOrNull()
    val storedDate = parseDatePref(dDayText)
    val storedTime = parseTimePref(dDayText)

    // 화면에서 고른(아직 저장 전일 수 있는) 값. 저장된 값이 바뀌면 그 값으로 다시 맞춘다
    var weight by rememberSaveable(storedWeightText) { mutableDoubleStateOf(storedWeight ?: 0.0) }
    var dateEpochDay by rememberSaveable(dDayText) { mutableStateOf(storedDate?.toEpochDay()) }
    var hour by rememberSaveable(dDayText) { mutableIntStateOf(storedTime?.first ?: LocalTime.now().hour) }
    var minute by rememberSaveable(dDayText) { mutableIntStateOf(storedTime?.second ?: LocalTime.now().minute) }
    var timeChanged by rememberSaveable(dDayText) { mutableStateOf(false) }

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var showStrategy by rememberSaveable { mutableStateOf(false) }
    var showInfo by rememberSaveable { mutableStateOf(false) }

    val date = dateEpochDay?.let(LocalDate::ofEpochDay)
    val dDay = date?.let { calcDDay(formatDatePref(it)) }
    val canModify = (storedWeight != null && weight != storedWeight) ||
        (date != null && date != storedDate) || timeChanged

    TabColumn(contentPadding = contentPadding) {
        // ① 프로필
        PwfbCard(modifier = Modifier.fillMaxWidth(), hero = true) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.trim().firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    if (storedWeight != null) {
                        Text(
                            text = stringResource(
                                R.string.profile_weight,
                                (storedWeight * 0.99).toString(),
                                storedWeight.toString()
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // ② 목표 수정
        Spacer(Modifier.height(24.dp))
        SectionTitle(text = stringResource(R.string.profile_goal))
        Spacer(Modifier.height(12.dp))
        PwfbCard(modifier = Modifier.fillMaxWidth()) {
            Eyebrow(text = stringResource(R.string.kr_empty_stomach_weight))
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(onClick = { weight = stepWeight(weight, -1) }) {
                    Icon(painterResource(R.drawable.ic_remove), contentDescription = stringResource(R.string.iv_minus_description))
                }
                Text(
                    text = weight.toString() + stringResource(R.string.kg),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                FilledTonalIconButton(onClick = { weight = stepWeight(weight, +1) }) {
                    Icon(painterResource(R.drawable.ic_add), contentDescription = stringResource(R.string.iv_plus_description))
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 20.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(text = stringResource(R.string.profile_d_day), modifier = Modifier.weight(1f))
                Text(
                    text = dDay?.let { "D$it" } ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(12.dp))
            TonalButton(
                text = date?.let(::formatDatePref) ?: stringResource(R.string.select_date),
                iconRes = R.drawable.ic_calendar,
                onClick = { showDatePicker = true }
            )
            Spacer(Modifier.height(8.dp))
            TonalButton(
                text = formatTimeDisplay(hour, minute),
                iconRes = R.drawable.ic_schedule,
                onClick = { showTimePicker = true }
            )
            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                text = stringResource(R.string.modify),
                enabled = canModify,
                trailingIconRes = null,
                onClick = {
                    date?.let { viewModel.save(PrefKey.D_DAY, DdayViewModel.toDDayText(it, hour, minute)) }
                    viewModel.save(PrefKey.WEIGHT, weight.toString())
                }
            )
        }

        // ③ 더보기 메뉴
        Spacer(Modifier.height(24.dp))
        PwfbCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            MenuRow(iconRes = R.drawable.ic_bolt, text = stringResource(R.string.the_day_strategy_1)) { showStrategy = true }
            MenuDivider()
            MenuRow(iconRes = R.drawable.ic_info, text = stringResource(R.string.pwfb_information)) { showInfo = true }
            MenuDivider()
            MenuRow(iconRes = R.drawable.ic_upload, text = stringResource(R.string.log_send)) { viewModel.requestLogUpload() }
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
                timeChanged = true
                showTimePicker = false
            }
        )
    }

    if (showStrategy) {
        ModalBottomSheet(
            onDismissRequest = { showStrategy = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            StrategySheetContent()
        }
    }

    if (showInfo) {
        ModalBottomSheet(
            onDismissRequest = { showInfo = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            InfoSheetContent()
        }
    }
}

/** 체중을 0.1kg 단위로 올리거나 내린다 (부동소수점 오차 방지) */
private fun stepWeight(weight: Double, direction: Int): Double =
    ((weight * 10).roundToInt() + direction).coerceAtLeast(0) / 10.0

@Composable
private fun MenuRow(iconRes: Int, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 60.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
