package com.pwfb.util

import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.pwfb.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private const val TAG_DATE_PICKER = "pwfb_date_picker"
private const val TAG_TIME_PICKER = "pwfb_time_picker"

/** MaterialDatePicker는 날짜를 UTC 자정 millis로 주고받는다 */
private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

/**
 * 대회 날짜 선택 다이얼로그. 오늘 이전 날짜는 고를 수 없다.
 * @param initial 처음 선택돼 있을 날짜. null이거나 오늘 이전이면 오늘
 */
fun FragmentManager.showPwfbDatePicker(initial: LocalDate?, onPicked: (LocalDate) -> Unit) {
    if (findFragmentByTag(TAG_DATE_PICKER) != null) return

    val today = LocalDate.now()
    val selection = (initial?.takeIf { it >= today } ?: today).toUtcMillis()
    val constraints = CalendarConstraints.Builder()
        .setValidator(DateValidatorPointForward.from(today.toUtcMillis()))
        .setOpenAt(selection)
        .build()

    val picker = MaterialDatePicker.Builder.datePicker()
        .setTitleText(R.string.competition_date)
        .setSelection(selection)
        .setCalendarConstraints(constraints)
        .setPositiveButtonText(R.string.confirm)
        .setNegativeButtonText(R.string.cancel)
        .build()
    picker.addOnPositiveButtonClickListener { onPicked(it.toLocalDate()) }
    picker.show(this, TAG_DATE_PICKER)
}

/**
 * 대회 시간 선택 다이얼로그 (12시간제 시계)
 */
fun FragmentManager.showPwfbTimePicker(hour: Int, minute: Int, onPicked: (hour: Int, minute: Int) -> Unit) {
    if (findFragmentByTag(TAG_TIME_PICKER) != null) return

    val picker = MaterialTimePicker.Builder()
        .setTitleText(R.string.time)
        .setTimeFormat(TimeFormat.CLOCK_12H)
        .setHour(hour)
        .setMinute(minute)
        .setPositiveButtonText(R.string.confirm)
        .setNegativeButtonText(R.string.cancel)
        .build()
    picker.addOnPositiveButtonClickListener { onPicked(picker.hour, picker.minute) }
    picker.show(this, TAG_TIME_PICKER)
}

/**
 * 화면 회전 등으로 다시 만들어진 피커에는 리스너가 없으므로 화면 생성 시 다시 연결한다
 */
@Suppress("UNCHECKED_CAST")
fun FragmentManager.reattachPwfbPickers(
    onDatePicked: (LocalDate) -> Unit,
    onTimePicked: (hour: Int, minute: Int) -> Unit
) {
    (findFragmentByTag(TAG_DATE_PICKER) as? MaterialDatePicker<Long>)?.let { picker ->
        picker.addOnPositiveButtonClickListener { onDatePicked(it.toLocalDate()) }
    }
    (findFragmentByTag(TAG_TIME_PICKER) as? MaterialTimePicker)?.let { picker ->
        picker.addOnPositiveButtonClickListener { onTimePicked(picker.hour, picker.minute) }
    }
}
