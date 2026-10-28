package com.pwfb.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.pwfb.R
import com.pwfb.ui.model.IntakeText
import com.pwfb.ui.model.NutrientUi
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/* ───────── 문자열 ───────── */

/** 리소스 ID + 포맷 인자를 문자열로 */
@Composable
fun IntakeText.text(): String = stringResource(resId, *args.toTypedArray())

/* ───────── 텍스트 ───────── */

/** 섹션 머리말(작은 대문자 라벨) */
@Composable
fun Eyebrow(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
        color = color
    )
}

/** 카드 묶음 위의 섹션 제목 */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(start = 4.dp),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

/* ───────── 컨테이너 ───────── */

/**
 * 기본 카드: 평평한 톤 컨테이너, 모서리 24dp. [hero]는 한 단계 밝은 배경과 28dp 모서리
 */
@Composable
fun PwfbCard(
    modifier: Modifier = Modifier,
    hero: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = if (hero) MaterialTheme.shapes.extraLarge else MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (hero) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/** 탭 화면 공통 스크롤 컬럼 (좌우 20dp, 아래 24dp 여백) */
@Composable
fun TabColumn(
    contentPadding: PaddingValues,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 24.dp),
        content = content
    )
}

/* ───────── 버튼 ───────── */

/** 56dp 알약형 주요 버튼 (끝에 화살표) */
@Composable
fun PrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes trailingIconRes: Int? = R.drawable.ic_arrow_forward
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        if (trailingIconRes != null) Spacer(Modifier.width(24.dp))
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        if (trailingIconRes != null) {
            Icon(
                painter = painterResource(trailingIconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/** 아이콘 + 글자가 가운데 오는 톤 버튼 (날짜·시간 선택 등) */
@Composable
fun TonalButton(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = MaterialTheme.shapes.medium,
        contentPadding = ButtonDefaults.ContentPadding
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/* ───────── 영양소 ───────── */

/** 이모지 아바타: 포인트 색을 연하게 깐 원 위에 이모지 */
@Composable
fun NutrientAvatar(ui: NutrientUi, size: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(ui.color.copy(alpha = 0.24f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = ui.emoji, fontSize = (size.value * 0.45f).sp)
    }
}

/* ───────── D-Day ───────── */

/** 영양·트레이닝 탭 상단의 간단한 D-Day 요약 카드 */
@Composable
fun DdayHeaderCard(dDay: Int?, dateText: String) {
    PwfbCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = dDay?.let { "D$it" } ?: "",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            VerticalDivider(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .height(28.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Column {
                Eyebrow(text = stringResource(R.string.d_day))
                Spacer(Modifier.height(2.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/* ───────── 온보딩 틀 ───────── */

const val ONBOARDING_STEPS = 3

/**
 * 온보딩 공통 틀: 진행바 · STEP 라벨 · 제목 · 설명 · 내용 · 하단 주요 버튼
 */
@Composable
fun OnboardingStep(
    step: Int,
    title: String,
    subtitle: String,
    buttonText: String,
    buttonEnabled: Boolean,
    onButtonClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { step / ONBOARDING_STEPS.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp)),
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(12.dp))
            Eyebrow(text = stringResource(R.string.onboarding_step, step, ONBOARDING_STEPS))
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))
            content()
        }

        PrimaryButton(
            text = buttonText,
            enabled = buttonEnabled,
            onClick = onButtonClick,
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

/* ───────── 날짜 · 시간 선택 ───────── */

/** DatePicker는 날짜를 UTC 자정 millis로 주고받는다 */
private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

/**
 * 대회 날짜 선택. 오늘 이전은 고를 수 없다.
 * @param initial 처음 선택돼 있을 날짜. null이거나 오늘 이전이면 오늘
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PwfbDatePickerDialog(
    initial: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val todayUtc = today.toUtcMillis()
    val state = rememberDatePickerState(
        initialSelectedDateMillis = (initial?.takeIf { it >= today } ?: today).toUtcMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= todayUtc
            override fun isSelectableYear(year: Int): Boolean = year >= today.year
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { state.selectedDateMillis?.let { onConfirm(it.toLocalDate()) } },
                enabled = state.selectedDateMillis != null
            ) { Text(stringResource(R.string.confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    ) {
        DatePicker(
            state = state,
            title = {
                Text(
                    text = stringResource(R.string.competition_date),
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                )
            }
        )
    }
}

/**
 * 대회 시간 선택 (12시간제 시계)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PwfbTimePickerDialog(
    hour: Int,
    minute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = false)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.time),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                TimePicker(state = state)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                    TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            }
        }
    }
}

/* ───────── 광고 ───────── */

/** AdMob 배너 (하단 내비게이션 위) */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val adUnitId = stringResource(R.string.admob_key)
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
