package com.pwfb.util

import android.graphics.Color
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * 모든 화면을 edge-to-edge로 통일 (Android 15부터는 어차피 강제 적용되므로 그 이하 버전도 같게 맞춤)
 * 앱이 항상 다크 테마라 상태바·내비게이션 바 아이콘은 밝은 색으로 고정
 * setContentView 전에 호출
 */
fun ComponentActivity.enablePwfbEdgeToEdge() {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
    )
}

/**
 * 화면 루트 View에 시스템 바(상태바·내비게이션 바·컷아웃)와 키보드 영역만큼 패딩을 준다
 * - 상단: 제목이 상태바에 가려지지 않도록
 * - 하단: Next 버튼 등이 내비게이션 바에 가려지지 않도록, 키보드가 올라오면 그 위로 밀어 올리도록
 *   (edge-to-edge에서는 windowSoftInputMode=adjustResize가 창을 줄여 주지 않으므로 직접 처리)
 * 여기서 인셋을 소비하므로 자식 View가 다시 패딩을 넣지 않는다
 */
fun View.applySystemBarInsetsAsPadding() {
    // 레이아웃에 지정한 패딩은 유지하고 그 위에 인셋만큼 더한다
    val base = Insets.of(paddingLeft, paddingTop, paddingRight, paddingBottom)
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        view.updatePadding(
            left = base.left + bars.left,
            top = base.top + bars.top,
            right = base.right + bars.right,
            bottom = base.bottom + maxOf(bars.bottom, ime.bottom)
        )
        WindowInsetsCompat.CONSUMED
    }
}

/**
 * 메인 화면용. 루트는 상단·좌우 시스템 바와 키보드만큼만 패딩을 주고,
 * 하단 바([bottomBar]: 광고 + 탭)는 내비게이션 바 뒤까지 배경을 깔고 그만큼 안쪽 패딩을 준다.
 * 키보드가 올라오면 하단 바는 키보드 위로 올라가므로 안쪽 패딩을 없앤다.
 */
fun View.applyMainInsets(bottomBar: View) {
    val base = Insets.of(paddingLeft, paddingTop, paddingRight, paddingBottom)
    val barBase = bottomBar.paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        val imeVisible = ime.bottom > 0
        view.updatePadding(
            left = base.left + bars.left,
            top = base.top + bars.top,
            right = base.right + bars.right,
            bottom = base.bottom + if (imeVisible) ime.bottom else 0
        )
        bottomBar.updatePadding(bottom = barBase + if (imeVisible) 0 else bars.bottom)
        WindowInsetsCompat.CONSUMED
    }
}
