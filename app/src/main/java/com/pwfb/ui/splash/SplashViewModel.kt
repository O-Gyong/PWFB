package com.pwfb.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pwfb.domain.usecase.PrefUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val prefUseCase: PrefUseCase
): ViewModel() {
    // 최초 진입 여부. 읽기 전에는 null이며, 그동안 스플래시를 유지한다.
    private val _firstInitObserve = MutableStateFlow<Boolean?>(null)
    val firstInitObserve = _firstInitObserve.asStateFlow()

    init {
        viewModelScope.launch {
            _firstInitObserve.value = prefUseCase.observeFirstInit().first()
        }
    }
}
