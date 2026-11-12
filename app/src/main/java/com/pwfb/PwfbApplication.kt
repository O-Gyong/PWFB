package com.pwfb

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PwfbApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 다크모드 비활성화 (앱 전체에 한 번만 적용)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
