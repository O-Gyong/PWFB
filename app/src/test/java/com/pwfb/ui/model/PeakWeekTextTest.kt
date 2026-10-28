package com.pwfb.ui.model

import com.pwfb.R
import com.pwfb.common.NutritionObject
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 일자별 전략·트레이닝·영양 가이드 문구가 올바른 날에 바뀌는지
 */
class PeakWeekTextTest {

    @Test
    fun strategy_changesPerDay() {
        assertEquals(R.string.re_feed_day, PeakWeekText.strategy(-7).resId)
        assertEquals(R.string.low_carbohydrate_day, PeakWeekText.strategy(-4).resId)
        assertEquals(R.string.high_carbohydrate_day, PeakWeekText.strategy(-2).resId)
        assertEquals(R.string.correction_day, PeakWeekText.strategy(-1).resId)
        assertEquals(R.string.man_of_the_hour, PeakWeekText.strategy(0).resId)
        assertEquals(R.string.gonna_be_alright, PeakWeekText.strategy(-18).resId)
        assertEquals(R.string.gonna_be_alright, PeakWeekText.strategy(3).resId)
    }

    @Test
    fun training_outsidePeakWeek_isUsual() {
        assertEquals(R.string.nutrition_usually_intake, PeakWeekText.training(-7).resId)
        assertEquals(R.string.training_day_6, PeakWeekText.training(-6).resId)
        assertEquals(R.string.training_day_1, PeakWeekText.training(-1).resId)
        assertEquals(R.string.nutrition_usually_intake, PeakWeekText.homeTraining(0).resId)
    }

    @Test
    fun nutritionGuide_sodiumAndCreatine_followLoadingSchedule() {
        assertEquals(R.string.nutrition_sodium_day_3, NutritionGuide.of(NutritionObject.SODIUM, -3).resId)
        assertEquals(R.string.nutrition_sodium_day_1, NutritionGuide.of(NutritionObject.SODIUM, -1).resId)
        assertEquals(R.string.nutrition_creatine_day_7654, NutritionGuide.of(NutritionObject.CREATINE, -7).resId)
        assertEquals(R.string.nutrition_creatine_day_321, NutritionGuide.of(NutritionObject.CREATINE, -2).resId)
    }

    @Test
    fun nutritionGuide_outsidePeakWeek_isUsualWithEmoji() {
        val guide = NutritionGuide.of(NutritionObject.WATER, -10)
        assertEquals(R.string.nutrition_usually_intake_2, guide.resId)
        assertEquals(listOf<Any>("💧"), guide.args)
    }
}
