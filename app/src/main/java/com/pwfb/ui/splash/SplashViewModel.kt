package com.pwfb.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pwfb.domain.usecase.PrefUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val prefUseCase: PrefUseCase
): ViewModel() {
    private var _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _firstInitObserve = MutableStateFlow<Boolean?>(null) // 초기값 null
    val firstInitObserve = _firstInitObserve.asStateFlow()

    init {
        viewModelScope.launch {
            delay(0L)
            _isLoading.value = false
        }
    }

     fun getFirstInit() {
        viewModelScope.launch {
            _firstInitObserve.value = prefUseCase.observeFirstInit().first()
        }
    }
}