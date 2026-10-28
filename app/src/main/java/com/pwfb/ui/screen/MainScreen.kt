package com.pwfb.ui.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pwfb.R
import com.pwfb.ui.viewmodel.MainViewModel
import java.io.File

/** 하단 탭 */
enum class MainTab(@StringRes val titleRes: Int, @DrawableRes val iconRes: Int) {
    HOME(R.string.home, R.drawable.ic_home),
    NUTRITION(R.string.nutrition, R.drawable.ic_nutrition),
    TRAINING(R.string.training, R.drawable.ic_training),
    PROFILE(R.string.profile, R.drawable.ic_profile)
}

/**
 * 메인 화면: 제목 · 탭 내용 · 광고 배너 · 하단 내비게이션.
 * 네 탭이 하나의 [MainViewModel]을 공유한다.
 *
 * @param onUploadLog 프로필의 "로그 전송"에서 보낼 파일 (null이면 보낼 로그 없음)
 */
@Composable
fun MainScreen(
    onUploadLog: (File?) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    var tab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val saveFailedMessage = stringResource(R.string.save_failed)

    // 저장 실패 안내
    LaunchedEffect(viewModel) {
        viewModel.saveResult.collect { if (!it.success) snackbarHostState.showSnackbar(saveFailedMessage) }
    }
    LaunchedEffect(viewModel) {
        viewModel.logUpload.collect { onUploadLog(it) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            Text(
                text = stringResource(tab.titleRes),
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        bottomBar = {
            // 광고 배너와 하단 내비게이션을 한 덩어리로 묶어 같은 배경색을 준다
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                AdBanner(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                )
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    MainTab.entries.forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = {
                                // 탭을 옮기면 입력 포커스를 빼서 키보드를 내리고 입력값을 저장시킨다
                                focusManager.clearFocus()
                                tab = item
                            },
                            icon = { Icon(painterResource(item.iconRes), contentDescription = null) },
                            label = { Text(stringResource(item.titleRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                // 입력칸 바깥을 누르면 키보드를 내린다 (포커스가 빠지면서 입력값이 저장된다)
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
        ) {
            when (tab) {
                MainTab.HOME -> HomeScreen(viewModel = viewModel, contentPadding = innerPadding)
                MainTab.NUTRITION -> NutritionScreen(viewModel = viewModel, contentPadding = innerPadding)
                MainTab.TRAINING -> TrainingScreen(viewModel = viewModel, contentPadding = innerPadding)
                MainTab.PROFILE -> ProfileScreen(viewModel = viewModel, contentPadding = innerPadding)
            }
        }
    }
}
