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
 * 온보딩 2단계: 공복 체중 저장
 */
@HiltViewModel
class WeightViewModel @Inject constructor(
    private val prefUseCase: PrefUseCase
) : ViewModel() {

    private val _saved = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val saved: SharedFlow<Boolean> = _saved.asSharedFlow()

    fun setWeight(weight: String) {
        if (!isValidWeight(weight)) return
        viewModelScope.launch {
            _saved.emit(prefUseCase.set(PrefKey.WEIGHT, weight.trim()) is PwfbResultEntity.Success)
        }
    }

    companion object {
        /** 0보다 큰 숫자만 체중으로 인정 */
        fun isValidWeight(text: String): Boolean = (text.trim().toDoubleOrNull() ?: 0.0) > 0.0
    }
}
