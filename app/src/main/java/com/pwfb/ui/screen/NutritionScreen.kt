package com.pwfb.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pwfb.R
import com.pwfb.common.NutritionObject
import com.pwfb.domain.entity.PrefKey
import com.pwfb.ui.model.NutrientUi
import com.pwfb.ui.model.NutritionGuide
import com.pwfb.ui.viewmodel.MainViewModel
import com.pwfb.util.calcDDay

/** 영양 항목 키 → 저장 키 */
private fun prefKeyOf(type: String): PrefKey = when (type) {
    NutritionObject.CARBOHYDRATE -> PrefKey.CARBOHYDRATE
    NutritionObject.PROTEIN -> PrefKey.PROTEIN
    NutritionObject.FAT -> PrefKey.FAT
    NutritionObject.WATER -> PrefKey.WATER
    NutritionObject.SODIUM -> PrefKey.SODIUM
    NutritionObject.POTASSIUM -> PrefKey.POTASSIUM
    NutritionObject.CREATINE -> PrefKey.CREATINE
    else -> PrefKey.DIETARY_FIBER
}

/**
 * 영양 탭: 일자별 영양 전략과 실제 섭취량 기록
 */
@Composable
fun NutritionScreen(viewModel: MainViewModel, contentPadding: PaddingValues) {
    val dDayText by viewModel.state(PrefKey.D_DAY).collectAsStateWithLifecycle()
    val dDay = calcDDay(dDayText)

    TabColumn(contentPadding = contentPadding) {
        DdayHeaderCard(dDay = dDay, dateText = dDayText)

        Spacer(Modifier.height(24.dp))
        SectionTitle(text = stringResource(R.string.nutrition_section_title))

        NutritionObject.nutritionEntityLists.forEach { entity ->
            val prefKey = prefKeyOf(entity.type)
            val stored by viewModel.state(prefKey).collectAsStateWithLifecycle()
            key(entity.type) {
                Spacer(Modifier.height(12.dp))
                NutrientInputCard(
                    ui = NutrientUi.of(entity.type),
                    guide = dDay?.let { NutritionGuide.of(entity.type, it).text() } ?: "",
                    stored = stored,
                    onSave = { viewModel.save(prefKey, it) }
                )
            }
        }
    }
}

/**
 * 영양소 카드: 아이콘 + 이름 + 오늘 가이드 + 섭취 기록 입력.
 * 입력값은 완료 키, 포커스 이탈, 화면 이탈 시 저장한다.
 */
@Composable
private fun NutrientInputCard(
    ui: NutrientUi,
    guide: String,
    stored: String,
    onSave: (String) -> Unit
) {
    // 저장된 값이 바뀌면(처음 읽힘, 저장 완료) 입력칸도 그 값으로 맞춘다
    var text by rememberSaveable(stored) { mutableStateOf(stored) }
    val latestText by rememberUpdatedState(text)
    val latestStored by rememberUpdatedState(stored)
    val focusManager = LocalFocusManager.current

    DisposableEffect(Unit) {
        onDispose { if (latestText != latestStored) onSave(latestText) }
    }

    PwfbCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NutrientAvatar(ui = ui, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(ui.nameRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = guide,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (!it.isFocused && text != stored) onSave(text) },
            label = { Text(stringResource(R.string.hint_nutrition_intake)) },
            suffix = { Text(stringResource(ui.unitRes)) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onSave(text)
                focusManager.clearFocus()
            })
        )
    }
}
