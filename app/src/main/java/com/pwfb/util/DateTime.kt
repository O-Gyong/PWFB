package com.pwfb.util

import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

fun getCurrentToday(): String {
    return SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    ).format(Calendar.getInstance(Locale.getDefault()).time)
}

fun getCurrentTime(): String {
    return SimpleDateFormat(
        "HH:mm:ss",
        Locale.getDefault()
    ).format(Calendar.getInstance(Locale.getDefault()).time)
}

internal val DDAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu.MM.dd")

/**
 * 저장된 D-Day 문자열("yyyy.MM.dd(요일) HH:mm⏳")의 앞 10자리 날짜를 기준으로
 * 오늘과의 달력상 일수 차이를 반환한다. D-Day 전은 음수, 당일은 0, 이후는 양수.
 * 형식이 올바르지 않으면 null.
 */
fun calcDDay(dDayText: String, today: LocalDate = LocalDate.now()): Int? {
    if (dDayText.length < 10) return null
    return try {
        val target = LocalDate.parse(dDayText.substring(0, 10), DDAY_DATE_FORMATTER)
        ChronoUnit.DAYS.between(target, today).toInt()
    } catch (e: DateTimeParseException) {
        null
    }
}

fun getWeekKor(year: Int, month: Int, day: Int): String {
    return when (LocalDate.of(year, month, day).dayOfWeek) {
        DayOfWeek.MONDAY -> "(월)"
        DayOfWeek.TUESDAY -> "(화)"
        DayOfWeek.WEDNESDAY -> "(수)"
        DayOfWeek.THURSDAY -> "(목)"
        DayOfWeek.FRIDAY -> "(금)"
        DayOfWeek.SATURDAY -> "(토)"
        DayOfWeek.SUNDAY -> "(일)"
    }
}

/**
 * 화면 표시용 시간. 예) 14:05 -> "오후 02:05", 0:00 -> "오전 12:00", 12:00 -> "오후 12:00"
 */
fun formatTimeDisplay(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "오전" else "오후"
    val hour12 = if (hour % 12 == 0) 12 else hour % 12
    return "$amPm ${hour12.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

/**
 * 저장용 시간(24시간제). 예) 14:05 -> "14:05⏳"
 */
fun formatTimeStore(hour: Int, minute: Int): String {
    return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}⏳"
}

/**
 * 저장된 "HH:mm" 문자열을 (시, 분)으로 변환. 형식이 올바르지 않으면 null.
 */
fun parseStoredTime(time: String): Pair<Int, Int>? {
    if (time.length < 5 || time[2] != ':') return null
    val hour = time.substring(0, 2).toIntOrNull() ?: return null
    val minute = time.substring(3, 5).toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return hour to minute
}
