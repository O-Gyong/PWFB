package com.pwfb.ui.home

import androidx.annotation.StringRes
import com.pwfb.R
import com.pwfb.common.NutritionObject

/**
 * 홈 화면 영양 섭취량 문구. 문자열 리소스와 포맷 인자만 계산하므로 단위 테스트가 가능하다.
 */
data class IntakeText(@StringRes val resId: Int, val args: List<Any> = emptyList())

object NutritionIntake {
    private val USUAL = R.string.nutrition_usually_intake

    /**
     * @param type [NutritionObject]의 영양 항목 키
     * @param dDay 대회일 기준 일수 (전날 = -1)
     * @param kg 공복 체중
     */
    fun of(type: String, dDay: Int, kg: Double): IntakeText = when (type) {
        NutritionObject.CARBOHYDRATE -> when (dDay) {
            -6, -5, -4 -> IntakeText(R.string.home_carbohydrate_day_654)
            -3 -> IntakeText(R.string.home_carbohydrate_day_32, listOf((kg * 10).toInt()))
            -2 -> IntakeText(R.string.home_carbohydrate_day_32, listOf((kg * 2 * 10 / 3).toInt()))
            -1 -> IntakeText(R.string.home_carbohydrate_day_1, listOf(kg.toInt(), (kg * 3).toInt()))
            else -> IntakeText(R.string.home_carbohydrate_day_7)
        }

        NutritionObject.PROTEIN -> when (dDay) {
            -6, -5, -4 -> IntakeText(R.string.home_protein_day_654, listOf((kg * 2.4).toInt(), (kg * 3).toInt()))
            -3, -2 -> IntakeText(R.string.home_protein_day_32, listOf((kg * 1.6).toInt()))
            -1 -> IntakeText(R.string.home_protein_day_1, listOf((kg * 3).toInt()))
            else -> IntakeText(USUAL, listOf("🥩"))
        }

        NutritionObject.FAT -> when (dDay) {
            -7, -6, -5, -4 -> IntakeText(R.string.home_fat_day_7654, listOf((kg * 0.7).toInt(), (kg * 1.2).toInt()))
            -3, -2 -> IntakeText(R.string.home_fat_day_32, listOf((kg * 0.5).toInt()))
            -1 -> IntakeText(R.string.home_fat_day_1, listOf((kg * 0.7).toInt(), (kg * 1.0).toInt()))
            else -> IntakeText(USUAL, listOf("🥜"))
        }

        NutritionObject.WATER -> when (dDay) {
            -3 -> IntakeText(R.string.home_water_day_3)
            -2 -> IntakeText(R.string.home_water_day_2)
            -1 -> IntakeText(R.string.home_water_day_1)
            else -> IntakeText(USUAL, listOf("💧"))
        }

        NutritionObject.SODIUM -> when (dDay) {
            -3 -> IntakeText(R.string.home_sodium_day_3)
            -2 -> IntakeText(R.string.home_sodium_day_2)
            -1 -> IntakeText(R.string.home_sodium_day_1)
            else -> IntakeText(USUAL, listOf("🧂"))
        }

        NutritionObject.POTASSIUM -> when (dDay) {
            -3 -> IntakeText(R.string.home_potassium_day_3)
            else -> IntakeText(USUAL, listOf("🍌"))
        }

        NutritionObject.CREATINE -> when (dDay) {
            -7, -6, -5, -4 -> IntakeText(R.string.home_creatine_day_7654)
            -3, -2, -1 -> IntakeText(R.string.home_creatine_day_321)
            else -> IntakeText(USUAL, listOf("🔥"))
        }

        NutritionObject.DIETARY_FIBER -> when (dDay) {
            -6, -5, -4 -> IntakeText(R.string.home_dietary_fiber_day_654)
            -3, -2, -1 -> IntakeText(R.string.home_dietary_fiber_day_321)
            else -> IntakeText(USUAL, listOf("🥦"))
        }

        else -> IntakeText(USUAL)
    }
}
