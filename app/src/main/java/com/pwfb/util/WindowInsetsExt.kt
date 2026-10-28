package com.pwfb.util

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

/**
 * 모든 화면을 edge-to-edge로 통일 (Android 15부터는 어차피 강제 적용되므로 그 이하 버전도 같게 맞춤)
 * 앱이 항상 다크 테마라 상태바·내비게이션 바 아이콘은 밝은 색으로 고정.
 * 인셋 처리는 Compose 쪽(Scaffold, windowInsetsPadding)에서 한다.
 * setContent 전에 호출
 */
fun ComponentActivity.enablePwfbEdgeToEdge() {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
    )
}
