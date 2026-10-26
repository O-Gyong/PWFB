package com.pwfb.ui.setting

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pwfb.domain.entity.PrefKey
import com.pwfb.domain.entity.PwfbResultEntity
import com.pwfb.domain.usecase.PrefUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DayViewModel @Inject constructor(
    private val prefUseCase: PrefUseCase
): ViewModel() {

    private val _dDayObserve: MutableLiveData<PwfbResultEntity> = MutableLiveData()
    val dDayObserve = _dDayObserve

    private val _firstInitObserve: MutableLiveData<PwfbResultEntity> = MutableLiveData()
    val firstInitObserve = _firstInitObserve

    fun setDDay(dDay: String) {
        viewModelScope.launch {
            _dDayObserve.value = prefUseCase.set(PrefKey.D_DAY, dDay)
        }
    }

    fun setFirstInit() {
        viewModelScope.launch {
            _firstInitObserve.value = prefUseCase.setFirstInit()
        }
    }
}
