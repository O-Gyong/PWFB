package com.pwfb.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import com.pwfb.ui.MainActivity
import com.pwfb.ui.setting.NameActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity: AppCompatActivity() {
    companion object {
        private const val UPDATE_REQUEST_CODE = 100
    }

    private val viewModel: SplashViewModel by viewModels()
    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 최초 진입 여부를 읽을 때까지 스플래시 유지
        splashScreen.setKeepOnScreenCondition { viewModel.firstInitObserve.value == null }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.firstInitObserve.filterNotNull().collect { checkAppUpdate(it) }
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
            .addOnFailureListener {
                Toast.makeText(this, R.string.app_update_failed, Toast.LENGTH_SHORT).show()
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

    private fun intentLogin(intentType: Boolean) {
        val intent = if(intentType) {
            Intent(applicationContext, NameActivity::class.java)
        } else {
            Intent(applicationContext, MainActivity::class.java)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        finish()
    }
}
