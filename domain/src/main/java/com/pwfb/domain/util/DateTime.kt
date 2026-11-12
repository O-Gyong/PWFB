package com.pwfb.domain.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 로그 파일명·헤더에 쓰는 현재 날짜(yyyy-MM-dd)
 */
fun getCurrentToday(): String {
    return SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    ).format(Calendar.getInstance(Locale.getDefault()).time)
}

/**
 * 로그 헤더에 쓰는 현재 시각(HH:mm:ss)
 */
fun getCurrentTime(): String {
    return SimpleDateFormat(
        "HH:mm:ss",
        Locale.getDefault()
    ).format(Calendar.getInstance(Locale.getDefault()).time)
}
