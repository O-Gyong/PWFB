package com.pwfb.ui

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.storage.StorageReference
import com.pwfb.R
import com.pwfb.theme.PwfbTheme
import com.pwfb.ui.screen.PwfbNavHost
import com.pwfb.util.enablePwfbEdgeToEdge
import com.pwfb.util.getCurrentToday
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

/**
 * 앱의 유일한 콘텐츠 Activity. 화면은 전부 Compose([PwfbNavHost])로 그린다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var logStorageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        enablePwfbEdgeToEdge()
        super.onCreate(savedInstanceState)

        val isFirstInit = intent.getBooleanExtra(EXTRA_FIRST_INIT, false)
        setContent {
            PwfbTheme {
                PwfbNavHost(isFirstInit = isFirstInit, onUploadLog = ::uploadLog)
            }
        }
    }

    /**
     * 오늘 로그 파일을 Firebase Storage로 전송한다. file이 null이면 전송할 로그가 없는 것.
     * 같은 날 여러 사용자가 올려도 덮어쓰지 않도록 앱 설치 ID로 파일명을 구분한다.
     */
    private fun uploadLog(file: File?) {
        if (file == null) {
            showToast(R.string.log_empty)
            return
        }

        showToast(R.string.log_sending)
        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            val name = if (task.isSuccessful) task.result else "unknown_${System.currentTimeMillis()}"
            try {
                logStorageReference
                    .child("${getCurrentToday()}/$name.txt")
                    .putFile(Uri.fromFile(file))
                    .addOnSuccessListener { showToast(R.string.log_send_success) }
                    .addOnFailureListener { showToast(R.string.log_send_fail) }
            } catch (e: Exception) {
                showToast(R.string.log_send_fail)
            }
        }
    }

    /**
     * 전송 결과처럼 화면이 사라진 뒤에 호출될 수 있는 콜백에서도 안전하게 토스트를 띄운다.
     */
    private fun showToast(@StringRes messageRes: Int) {
        Toast.makeText(applicationContext, messageRes, Toast.LENGTH_SHORT).show()
    }

    companion object {
        /** 스플래시가 넘겨주는 최초 실행 여부 (true면 온보딩부터 시작) */
        const val EXTRA_FIRST_INIT = "isFirstInit"
    }
}
