package com.pwfb.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pwfb.R

/* ───────── 당일 전략 ───────── */

/** 당일 전략 흐름의 단계 */
private enum class StrategyStep { CHOOSE, ASK_FULL, ASK_DRY, ASK_FLAT, CONTINUE_DEHYDRATION, RESULT }

/**
 * 당일 전략 바텀시트: FULL/DRY 선택 → 예/아니오 질문 → 결과
 */
@Composable
fun StrategySheetContent() {
    var step by rememberSaveable { mutableStateOf(StrategyStep.CHOOSE) }
    var isFull by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var isDry by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var isDehydration by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var resultRes by rememberSaveable { mutableIntStateOf(R.string.stabilize_condition) }

    fun showResult(res: Int) {
        resultRes = res
        step = StrategyStep.RESULT
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.the_day_strategy_2),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.the_day_strategy_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))

        Box(modifier = Modifier.heightIn(min = 200.dp)) {
            when (step) {
                StrategyStep.CHOOSE -> ChooseStep(
                    onFull = { step = StrategyStep.ASK_FULL },
                    onDry = { step = StrategyStep.ASK_DRY }
                )

                StrategyStep.ASK_FULL -> QuestionCard(
                    question = stringResource(R.string.am_i_full),
                    onYes = {
                        isFull = true
                        if (isDry == true) showResult(R.string.stabilize_condition) else step = StrategyStep.ASK_DRY
                    },
                    onNo = {
                        isFull = false
                        if (isDry == true) showResult(R.string.stabilize_condition) else step = StrategyStep.ASK_DRY
                    }
                )

                StrategyStep.ASK_DRY -> QuestionCard(
                    question = stringResource(R.string.am_i_dry),
                    onYes = {
                        isDry = true
                        if (isFull != null && isDehydration == null) {
                            showResult(R.string.stabilize_condition)
                        } else {
                            step = StrategyStep.ASK_FULL
                        }
                    },
                    onNo = {
                        isDry = false
                        step = StrategyStep.CONTINUE_DEHYDRATION
                    }
                )

                StrategyStep.ASK_FLAT -> QuestionCard(
                    question = stringResource(R.string.am_i_flat),
                    onYes = { showResult(R.string.consider_flat) },
                    onNo = { showResult(R.string.stabilize_condition) }
                )

                StrategyStep.CONTINUE_DEHYDRATION -> PwfbCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.continue_dehydration_strategy),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton(
                        text = stringResource(R.string.continue_upper),
                        enabled = true,
                        trailingIconRes = null,
                        onClick = {
                            isDehydration = true
                            step = StrategyStep.ASK_DRY
                        }
                    )
                }

                StrategyStep.RESULT -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(resultRes),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ChooseStep(onFull: () -> Unit, onDry: () -> Unit) {
    Column {
        Eyebrow(text = stringResource(R.string.choose_upper))
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            ChoiceCard(
                emoji = stringResource(R.string.emoji_full),
                label = stringResource(R.string.full_upper),
                container = MaterialTheme.colorScheme.primaryContainer,
                content = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = onFull,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            ChoiceCard(
                emoji = stringResource(R.string.emoji_dry),
                label = stringResource(R.string.dry_upper),
                container = MaterialTheme.colorScheme.tertiaryContainer,
                content = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onDry,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ChoiceCard(
    emoji: String,
    label: String,
    container: Color,
    content: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(140.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 32.sp)
            Spacer(Modifier.height(6.dp))
            Text(text = label, style = MaterialTheme.typography.titleLarge, color = content)
        }
    }
}

@Composable
private fun QuestionCard(question: String, onYes: () -> Unit, onNo: () -> Unit) {
    PwfbCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = question,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(16.dp))
        Row {
            Button(
                onClick = onYes,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) { Text(stringResource(R.string.yes_upper), style = MaterialTheme.typography.titleMedium) }
            Spacer(Modifier.width(12.dp))
            OutlinedButton(
                onClick = onNo,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) { Text(stringResource(R.string.no_upper), style = MaterialTheme.typography.titleMedium) }
        }
    }
}

/* ───────── 앱 정보 ───────── */

@Composable
fun InfoSheetContent() {
    val context = LocalContext.current
    val versionName = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull() ?: "-"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_app),
            contentDescription = stringResource(R.string.iv_app),
            modifier = Modifier.size(96.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.app_name_upper),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.app_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.app_version_format, versionName),
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.font_credit),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
