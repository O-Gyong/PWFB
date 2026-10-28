package com.pwfb.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pwfb.domain.entity.PrefKey
import com.pwfb.domain.entity.PwfbResultEntity
import com.pwfb.domain.usecase.PrefUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 온보딩 1단계: 이름 저장
 */
@HiltViewModel
class NameViewModel @Inject constructor(
    private val prefUseCase: PrefUseCase
) : ViewModel() {

    // 저장 결과(성공 여부). 화면 전환 같은 일회성 신호라 이벤트로 전달한다
    private val _saved = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val saved: SharedFlow<Boolean> = _saved.asSharedFlow()

    fun setName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            _saved.emit(prefUseCase.set(PrefKey.NAME, trimmed) is PwfbResultEntity.Success)
        }
    }
}
