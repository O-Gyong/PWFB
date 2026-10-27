package com.pwfb.util

import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter
import com.pwfb.R
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * D-Day 선택 캘린더 공통 설정 (DayActivity, ProfileFragment에서 같이 사용)
 * - 헤더: "yyyy년 MM월", 요일: 한글
 * - 오늘 이전 날짜 비활성화, 오늘 강조
 * - 날짜를 고르면 선택 표시를 갱신하고 [onDateSelected]를 호출
 */
fun MaterialCalendarView.setupPwfbCalendar(onDateSelected: (CalendarDay) -> Unit) {
    setHeaderTextAppearance(R.style.CalendarWidgetHeader)
    setWeekDayFormatter(ArrayWeekDayFormatter(resources.getTextArray(R.array.calendar_week)))
    setTitleFormatter { day -> context.getString(R.string.calendar_title, day.year, day.month) }

    val dayDisableDecorator = DayDisableDecorator(hashSetOf(), CalendarDay.today(), context.getColor(R.color.c_949292))
    val todayDecorator = TodayDecorator(context.getColor(R.color.c_caab3f))
    addDecorators(dayDisableDecorator, todayDecorator)

    setOnDateChangedListener { _, date, _ ->
        addDecorators(
            ClearDecorator(context.getColor(R.color.white), date),
            dayDisableDecorator, todayDecorator,
            SelectDecorator(context.getColor(R.color.c_caab3f), date)
        )
        onDateSelected(date)
    }
}

/**
 * 저장된 D-Day 날짜를 캘린더에 선택 표시
 */
fun MaterialCalendarView.showSelectedPwfbDate(date: CalendarDay) {
    selectedDate = date
    addDecorator(SelectDecorator(context.getColor(R.color.c_caab3f), date))
}

/**
 * 저장용 날짜 문자열. 예) 2026.10.27(화)
 */
fun formatDatePref(date: CalendarDay): String =
    String.format(Locale.ROOT, "%04d.%02d.%02d%s", date.year, date.month, date.day, getWeekKor(date.year, date.month, date.day))

/**
 * 저장된 D-Day 문자열("yyyy.MM.dd...")의 날짜 부분을 캘린더 날짜로 변환. 형식이 올바르지 않으면 null.
 */
fun parseDatePref(dDayText: String): CalendarDay? {
    if (dDayText.length < 10) return null
    return try {
        val date = LocalDate.parse(dDayText.substring(0, 10), DDAY_DATE_FORMATTER)
        CalendarDay.from(date.year, date.monthValue, date.dayOfMonth)
    } catch (e: DateTimeParseException) {
        null
    }
}
