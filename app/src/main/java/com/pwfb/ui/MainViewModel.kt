package com.pwfb.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pwfb.domain.entity.PrefKey
import com.pwfb.domain.entity.PwfbResultEntity
import com.pwfb.domain.usecase.PrefUseCase
import com.pwfb.domain.usecase.file.CreateLogFileUseCase
import com.pwfb.domain.usecase.file.GetLogFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * 설정 저장 결과. 저장된 값 자체는 [MainViewModel.state]로 관찰하고,
 * 저장 성공/실패 같은 일회성 신호는 이 이벤트로 전달한다.
 */
data class SaveResult(val key: PrefKey, val success: Boolean)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefUseCase: PrefUseCase,
    private val createLogFileUseCase: CreateLogFileUseCase,
    private val getLogFileUseCase: GetLogFileUseCase
): ViewModel() {

    // 항목별 저장 값. 구독 중일 때만 DataStore를 collect하며, 저장하면 새 값이 자동으로 반영된다.
    private val states: Map<PrefKey, StateFlow<String>> = PrefKey.values().associateWith { key ->
        prefUseCase.observe(key).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )
    }

    private val _saveResult = MutableSharedFlow<SaveResult>(extraBufferCapacity = 8)
    val saveResult: SharedFlow<SaveResult> = _saveResult.asSharedFlow()

    // 업로드할 로그 파일. 일회성 이벤트이므로 화면이 재생성되어도 다시 전달되지 않는다. 파일이 없으면 null.
    private val _logUpload = MutableSharedFlow<File?>(extraBufferCapacity = 1)
    val logUpload: SharedFlow<File?> = _logUpload.asSharedFlow()

    fun requestLogUpload() {
        viewModelScope.launch {
            _logUpload.emit(withContext(Dispatchers.IO) { getLogFileUseCase() })
        }
    }

    /**
     * 저장된 값. 아직 읽기 전에는 빈 문자열("")이 방출된다.
     */
    fun state(key: PrefKey): StateFlow<String> = states.getValue(key)

    fun save(key: PrefKey, value: String) {
        viewModelScope.launch {
            val result = prefUseCase.set(key, value)
            val success = result is PwfbResultEntity.Success
            if (!success) {
                createLogFileUseCase("[${key.name} 설정 실패]: $result")
            }
            _saveResult.emit(SaveResult(key, success))
        }
    }
}
