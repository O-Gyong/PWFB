package com.pwfb.ui.model

import com.pwfb.R

/**
 * 피크위크 일자별 전략·트레이닝 문구. 홈과 트레이닝 탭이 쓴다.
 */
object PeakWeekText {
    /** 홈 히어로 카드의 오늘 전략 */
    fun strategy(dDay: Int): IntakeText = when (dDay) {
        -7 -> IntakeText(R.string.re_feed_day)
        -6, -5, -4 -> IntakeText(R.string.low_carbohydrate_day)
        -3, -2 -> IntakeText(R.string.high_carbohydrate_day)
        -1 -> IntakeText(R.string.correction_day)
        0 -> IntakeText(R.string.man_of_the_hour)
        else -> IntakeText(R.string.gonna_be_alright)
    }

    /** 홈의 오늘 트레이닝 요약 */
    fun homeTraining(dDay: Int): IntakeText = when (dDay) {
        -6 -> IntakeText(R.string.home_training_day_6)
        -5, -4 -> IntakeText(R.string.home_training_day_54)
        -3 -> IntakeText(R.string.home_training_day_3)
        -2 -> IntakeText(R.string.home_training_day_2)
        -1 -> IntakeText(R.string.home_training_day_1)
        else -> IntakeText(R.string.nutrition_usually_intake, listOf("💪"))
    }

    /** 트레이닝 탭의 오늘 트레이닝 안내 */
    fun training(dDay: Int): IntakeText = when (dDay) {
        -6 -> IntakeText(R.string.training_day_6)
        -5, -4 -> IntakeText(R.string.training_day_54)
        -3 -> IntakeText(R.string.training_day_3)
        -2 -> IntakeText(R.string.training_day_2)
        -1 -> IntakeText(R.string.training_day_1)
        else -> IntakeText(R.string.nutrition_usually_intake, listOf("🏋"))
    }
}
