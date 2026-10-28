package com.pwfb.ui.model

import com.pwfb.R
import com.pwfb.common.NutritionObject

/**
 * 영양 탭의 일자별 섭취 전략 문구. 문자열 리소스와 포맷 인자만 계산하므로 단위 테스트가 가능하다.
 * (홈의 권장 섭취량 [NutritionIntake]와 달리 체중에 의존하지 않는 설명 문구)
 */
object NutritionGuide {
    private val USUAL = R.string.nutrition_usually_intake_2

    /**
     * @param type [NutritionObject]의 영양 항목 키
     * @param dDay 대회일 기준 일수 (전날 = -1). 피크위크(-7~-1) 밖이면 "평소대로"
     */
    fun of(type: String, dDay: Int): IntakeText = when (type) {
        NutritionObject.CARBOHYDRATE -> when (dDay) {
            -7 -> IntakeText(R.string.nutrition_carbohydrate_day_7)
            -6, -5, -4 -> IntakeText(R.string.nutrition_carbohydrate_day_654)
            -3 -> IntakeText(R.string.nutrition_carbohydrate_day_3)
            -2 -> IntakeText(R.string.nutrition_carbohydrate_day_2)
            -1 -> IntakeText(R.string.nutrition_carbohydrate_day_1)
            else -> IntakeText(USUAL, listOf("🌾"))
        }

        NutritionObject.PROTEIN -> when (dDay) {
            -7 -> IntakeText(R.string.nutrition_usually_intake, listOf("🥩"))
            -6, -5, -4 -> IntakeText(R.string.nutrition_protein_day_654)
            -3, -2 -> IntakeText(R.string.nutrition_protein_day_32)
            -1 -> IntakeText(R.string.nutrition_protein_day_1)
            else -> IntakeText(USUAL, listOf("🥩"))
        }

        NutritionObject.FAT -> when (dDay) {
            -7, -6, -5, -4 -> IntakeText(R.string.nutrition_fat_day_7654)
            -3, -2 -> IntakeText(R.string.nutrition_fat_day_32)
            -1 -> IntakeText(R.string.nutrition_fat_day_1)
            else -> IntakeText(USUAL, listOf("🥜"))
        }

        NutritionObject.WATER -> when (dDay) {
            -3 -> IntakeText(R.string.nutrition_water_day_3)
            -2 -> IntakeText(R.string.nutrition_water_day_2)
            -1 -> IntakeText(R.string.nutrition_water_day_1)
            else -> IntakeText(USUAL, listOf("💧"))
        }

        NutritionObject.SODIUM -> when (dDay) {
            -3 -> IntakeText(R.string.nutrition_sodium_day_3)
            -2 -> IntakeText(R.string.nutrition_sodium_day_2)
            -1 -> IntakeText(R.string.nutrition_sodium_day_1)
            else -> IntakeText(USUAL, listOf("🧂"))
        }

        NutritionObject.POTASSIUM -> when (dDay) {
            -3 -> IntakeText(R.string.nutrition_potassium_day_3)
            else -> IntakeText(USUAL, listOf("🍌"))
        }

        NutritionObject.CREATINE -> when (dDay) {
            -7, -6, -5, -4 -> IntakeText(R.string.nutrition_creatine_day_7654)
            -3, -2, -1 -> IntakeText(R.string.nutrition_creatine_day_321)
            else -> IntakeText(USUAL, listOf("🔥"))
        }

        NutritionObject.DIETARY_FIBER -> when (dDay) {
            -6, -5, -4 -> IntakeText(R.string.nutrition_dietary_fiber_day_654)
            -3, -2, -1 -> IntakeText(R.string.nutrition_dietary_fiber_day_321)
            else -> IntakeText(USUAL, listOf("🥦"))
        }

        else -> IntakeText(USUAL)
    }
}
