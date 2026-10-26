package com.pwfb.ui.splash

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AnticipateInterpolator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.pwfb.R
import com.pwfb.base.BaseActivity
import com.pwfb.ui.MainActivity
import com.pwfb.ui.setting.NameActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity: BaseActivity() {
    companion object {
        private const val UPDATE_REQUEST_CODE = 100
    }

    private val viewModel: SplashViewModel by viewModels()
    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(PWFB, "SplashActivity onCreate")

        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 다크모드 비활성화
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        /**
         * 스플래시 탐지
         */
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    return if (!viewModel.isLoading.value) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        false
                    }
                }
            }
        )

        /**
         * 스플래시 끝나고 애니메이션 적용
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                ObjectAnimator.ofFloat(
                    splashScreenView,
                    View.TRANSLATION_Y,
                    0f,
                    -splashScreenView.height.toFloat()
                ).run {
                    interpolator = AnticipateInterpolator()
                    duration = 0L
                    doOnEnd { splashScreenView.remove() }
                    start()
                    addListener(object : Animator.AnimatorListener{
                        override fun onAnimationStart(p0: Animator) {
                        }
                        override fun onAnimationEnd(p0: Animator) {
                            viewModel.getFirstInit()
                        }
                        override fun onAnimationCancel(p0: Animator) {
                        }
                        override fun onAnimationRepeat(p0: Animator) {
                        }
                    })
                }
            }
        } else {
            viewModel.getFirstInit()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.firstInitObserve.collect {
                    if(it != null) {
                        checkAppUpdate(it)
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun checkAppUpdate(intentType: Boolean) {
        // PlayStore에서 설치된 경우 설치 경로가 "com.android.vending"으로 됨
        val isRegisteredPlayStore = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11 기준
            val info = packageManager.getInstallSourceInfo(packageName)
            info.installingPackageName == "com.android.vending"
        } else {
            packageManager.getInstallerPackageName(packageName) == "com.android.vending"
        }

        // PlayStore에 등록된 앱 아닌 경우 앱 체크 안 하도록 처리
        // 로컬 빌드는 앱 등록이 무조건 안 된 것으로 체크하여 addOnFailureListener를 타게 됨
        if (!isRegisteredPlayStore) {
            intentLogin(intentType)
            return
        }

        appUpdateManager.appUpdateInfo
            // PlayStore 통신 성공
            .addOnSuccessListener { appUpdateInfo ->
                if ((appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE))
                    || appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) { // 즉시 업데이트 가능한 경우
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        this@SplashActivity,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                        UPDATE_REQUEST_CODE
                    )
                } else {
                    intentLogin(intentType)
                }
            }
            // PlayStore 통신 실패 (Ex- PlayStore에 앱 등록이 안 된 경우)
            .addOnFailureListener { e ->
                Toast.makeText(this, "앱 업데이트 실패", Toast.LENGTH_SHORT).show()
                intentLogin(intentType)
            }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE && resultCode != RESULT_OK) {
            finish() // 업데이트 강제: 취소하거나 실패하면 앱을 종료
        }
    }

    fun intentLogin(intentType: Boolean) {
        val intent = if(intentType) {
            Intent(applicationContext, NameActivity::class.java)
        }
        else {
            Intent(applicationContext, MainActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        finish()

    }
}