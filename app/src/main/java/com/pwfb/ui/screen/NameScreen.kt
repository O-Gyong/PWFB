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
import com.pwfb.ui.viewmodel.NameViewModel

/**
 * 온보딩 1단계: 이름
 */
@Composable
fun NameScreen(
    onNext: () -> Unit,
    onSaveFailed: () -> Unit,
    viewModel: NameViewModel = hiltViewModel()
) {
    LaunchedEffect(viewModel) {
        viewModel.saved.collect { success -> if (success) onNext() else onSaveFailed() }
    }

    NameContent(onSubmit = viewModel::setName)
}

@Composable
private fun NameContent(onSubmit: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    val canProceed = name.isNotBlank()

    OnboardingStep(
        step = 1,
        title = stringResource(R.string.welcome),
        subtitle = stringResource(R.string.welcome_subtitle),
        buttonText = stringResource(R.string.next),
        buttonEnabled = canProceed,
        onButtonClick = { onSubmit(name) }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.input_name)) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (canProceed) onSubmit(name) })
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121110)
@Composable
private fun NamePreview() {
    PwfbTheme { NameContent(onSubmit = {}) }
}
