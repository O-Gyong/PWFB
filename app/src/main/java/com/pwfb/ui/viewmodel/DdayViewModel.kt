package com.pwfb.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pwfb.domain.entity.PrefKey
import com.pwfb.domain.entity.PwfbResultEntity
import com.pwfb.domain.usecase.PrefUseCase
import com.pwfb.util.formatDatePref
import com.pwfb.util.formatTimeStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * 온보딩 3단계: 대회 D-Day 저장 + 최초 진입 완료 처리
 */
@HiltViewModel
class DdayViewModel @Inject constructor(
    private val prefUseCase: PrefUseCase
) : ViewModel() {

    // D-Day 저장과 최초 진입 완료 처리가 모두 성공했는지
    private val _completed = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val completed: SharedFlow<Boolean> = _completed.asSharedFlow()

    fun setDDay(date: LocalDate, hour: Int, minute: Int) {
        viewModelScope.launch {
            val saved = prefUseCase.set(PrefKey.D_DAY, toDDayText(date, hour, minute))
            if (saved !is PwfbResultEntity.Success) {
                _completed.emit(false)
                return@launch
            }
            _completed.emit(prefUseCase.setFirstInit() is PwfbResultEntity.Success)
        }
    }

    companion object {
        /** 저장 형식: "yyyy.MM.dd(요일) HH:mm⏳" */
        fun toDDayText(date: LocalDate, hour: Int, minute: Int): String =
            "${formatDatePref(date)} ${formatTimeStore(hour, minute)}"
    }
}
