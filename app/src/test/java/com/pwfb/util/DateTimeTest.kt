package com.pwfb.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DateTimeTest {
    private val today = LocalDate.of(2026, 10, 26)

    @Test
    fun calcDDay_beforeTarget_isNegative() {
        // 3일 뒤 -> -3 (시각과 무관)
        assertEquals(-3, calcDDay("2026.10.29(목) 14:00⏳", today))
        assertEquals(-1, calcDDay("2026.10.27(화) 00:00⏳", today))
    }

    @Test
    fun calcDDay_onTarget_isZero() {
        assertEquals(0, calcDDay("2026.10.26(월) 23:59⏳", today))
    }

    @Test
    fun calcDDay_afterTarget_isPositive() {
        assertEquals(2, calcDDay("2026.10.24(토) 09:00⏳", today))
    }

    @Test
    fun calcDDay_dateOnly_works() {
        assertEquals(-7, calcDDay("2026.11.02(월)", today))
    }

    @Test
    fun calcDDay_invalid_returnsNull() {
        assertNull(calcDDay("", today))
        assertNull(calcDDay("WeightNull!!", today))
        assertNull(calcDDay("2026.13.40(월)", today))
    }

    @Test
    fun formatTimeDisplay_coversBoundaries() {
        assertEquals("오전 12:00", formatTimeDisplay(0, 0))
        assertEquals("오전 09:05", formatTimeDisplay(9, 5))
        assertEquals("오후 12:00", formatTimeDisplay(12, 0))
        assertEquals("오후 02:05", formatTimeDisplay(14, 5))
        assertEquals("오후 11:59", formatTimeDisplay(23, 59))
    }

    @Test
    fun formatTimeStore_is24HourPadded() {
        assertEquals("00:00⏳", formatTimeStore(0, 0))
        assertEquals("14:05⏳", formatTimeStore(14, 5))
    }

    @Test
    fun parseStoredTime_roundTrip() {
        assertEquals(20 to 30, parseStoredTime("20:30⏳".substring(0, 5)))
        assertNull(parseStoredTime("24:00"))
        assertNull(parseStoredTime("ab:cd"))
        assertNull(parseStoredTime("9:05"))
    }

    @Test
    fun getWeekKor_returnsKoreanWeekday() {
        assertEquals("(월)", getWeekKor(2026, 10, 26))
        assertEquals("(일)", getWeekKor(2026, 11, 1))
    }
}
