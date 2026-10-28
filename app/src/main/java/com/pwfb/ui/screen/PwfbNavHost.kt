package com.pwfb.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pwfb.R
import kotlinx.coroutines.launch
import java.io.File

/** 화면 경로 */
object ScreenName {
    const val SCREEN_NAME = "name"
    const val SCREEN_WEIGHT = "weight"
    const val SCREEN_DAY = "day"
    const val SCREEN_HOME = "home"
}

/**
 * 앱 전체 내비게이션. 최초 실행이면 온보딩(이름 → 체중 → D-Day)부터, 아니면 바로 메인.
 * 온보딩이 끝나면 온보딩 화면들을 백스택에서 걷어내 뒤로 가기로 돌아갈 수 없게 한다.
 */
@Composable
fun PwfbNavHost(isFirstInit: Boolean, onUploadLog: (File?) -> Unit) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val saveFailedMessage = stringResource(R.string.save_failed)
    val onSaveFailed: () -> Unit = { scope.launch { snackbarHostState.showSnackbar(saveFailedMessage) } }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = if (isFirstInit) ScreenName.SCREEN_NAME else ScreenName.SCREEN_HOME
        ) {
            composable(ScreenName.SCREEN_NAME) {
                NameScreen(
                    onNext = { navController.navigate(ScreenName.SCREEN_WEIGHT) },
                    onSaveFailed = onSaveFailed
                )
            }
            composable(ScreenName.SCREEN_WEIGHT) {
                WeightScreen(
                    onNext = { navController.navigate(ScreenName.SCREEN_DAY) },
                    onSaveFailed = onSaveFailed
                )
            }
            composable(ScreenName.SCREEN_DAY) {
                DdayScreen(
                    onDone = {
                        navController.navigate(ScreenName.SCREEN_HOME) {
                            popUpTo(ScreenName.SCREEN_NAME) { inclusive = true }
                        }
                    },
                    onSaveFailed = onSaveFailed
                )
            }
            composable(ScreenName.SCREEN_HOME) {
                MainScreen(onUploadLog = onUploadLog)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}
