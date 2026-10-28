package com.pwfb.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pwfb.R
import com.pwfb.theme.PwfbTheme
import com.pwfb.ui.viewmodel.WeightViewModel

/**
 * 온보딩 2단계: 공복 체중
 */
@Composable
fun WeightScreen(
    onNext: () -> Unit,
    onSaveFailed: () -> Unit,
    viewModel: WeightViewModel = hiltViewModel()
) {
    LaunchedEffect(viewModel) {
        viewModel.saved.collect { success -> if (success) onNext() else onSaveFailed() }
    }

    WeightContent(onSubmit = viewModel::setWeight)
}

@Composable
private fun WeightContent(onSubmit: (String) -> Unit) {
    var weight by rememberSaveable { mutableStateOf("") }
    val isValid = WeightViewModel.isValidWeight(weight)
    val showError = weight.isNotBlank() && !isValid

    OnboardingStep(
        step = 2,
        title = stringResource(R.string.kr_empty_stomach_weight),
        subtitle = stringResource(R.string.weight_subtitle),
        buttonText = stringResource(R.string.next),
        buttonEnabled = isValid,
        onButtonClick = { onSubmit(weight) }
    ) {
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.input_weight)) },
            suffix = { Text(stringResource(R.string.unit_kg)) },
            singleLine = true,
            isError = showError,
            supportingText = if (showError) {
                { Text(stringResource(R.string.weight_invalid)) }
            } else {
                null
            },
            shape = MaterialTheme.shapes.medium,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (isValid) onSubmit(weight) })
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121110)
@Composable
private fun WeightPreview() {
    PwfbTheme { WeightContent(onSubmit = {}) }
}
