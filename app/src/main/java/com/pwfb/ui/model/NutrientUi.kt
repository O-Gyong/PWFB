package com.pwfb.ui.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.pwfb.R
import com.pwfb.common.NutritionObject
import com.pwfb.theme.NutrientCarbohydrate
import com.pwfb.theme.NutrientCreatine
import com.pwfb.theme.NutrientDietaryFiber
import com.pwfb.theme.NutrientFat
import com.pwfb.theme.NutrientPotassium
import com.pwfb.theme.NutrientProtein
import com.pwfb.theme.NutrientSodium
import com.pwfb.theme.NutrientWater

/**
 * 영양소별 화면 표현 (이모지 · 이름 · 포인트 색 · 단위). 홈 목록과 영양 탭 카드가 같이 쓴다.
 */
data class NutrientUi(
    val emoji: String,
    @StringRes val nameRes: Int,
    val color: Color,
    @StringRes val unitRes: Int
) {
    companion object {
        fun of(type: String): NutrientUi = when (type) {
            NutritionObject.CARBOHYDRATE -> NutrientUi("🌾", R.string.nutrient_carbohydrate, NutrientCarbohydrate, R.string.unit_g)
            NutritionObject.PROTEIN -> NutrientUi("🥩", R.string.nutrient_protein, NutrientProtein, R.string.unit_g)
            NutritionObject.FAT -> NutrientUi("🥜", R.string.nutrient_fat, NutrientFat, R.string.unit_g)
            NutritionObject.WATER -> NutrientUi("💧", R.string.nutrient_water, NutrientWater, R.string.unit_ml)
            NutritionObject.SODIUM -> NutrientUi("🧂", R.string.nutrient_sodium, NutrientSodium, R.string.unit_mg)
            NutritionObject.POTASSIUM -> NutrientUi("🍌", R.string.nutrient_potassium, NutrientPotassium, R.string.unit_mg)
            NutritionObject.CREATINE -> NutrientUi("🔥", R.string.nutrient_creatine, NutrientCreatine, R.string.unit_g)
            else -> NutrientUi("🥦", R.string.nutrient_dietary_fiber, NutrientDietaryFiber, R.string.unit_g)
        }
    }
}
