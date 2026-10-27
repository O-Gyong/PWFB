package com.pwfb.ui

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.graphics.ColorUtils
import com.pwfb.R
import com.pwfb.common.NutritionObject

/**
 * 영양소별 화면 표현 (이모지 · 이름 · 포인트 색 · 단위). 홈 목록과 영양 탭 카드가 같이 쓴다.
 */
data class NutrientUi(
    val emoji: String,
    @StringRes val nameRes: Int,
    @ColorRes val colorRes: Int,
    @StringRes val unitRes: Int
) {
    companion object {
        fun of(type: String): NutrientUi = when (type) {
            NutritionObject.CARBOHYDRATE -> NutrientUi("🌾", R.string.nutrient_carbohydrate, R.color.nutrient_carbohydrate, R.string.unit_g)
            NutritionObject.PROTEIN -> NutrientUi("🥩", R.string.nutrient_protein, R.color.nutrient_protein, R.string.unit_g)
            NutritionObject.FAT -> NutrientUi("🥜", R.string.nutrient_fat, R.color.nutrient_fat, R.string.unit_g)
            NutritionObject.WATER -> NutrientUi("💧", R.string.nutrient_water, R.color.nutrient_water, R.string.unit_ml)
            NutritionObject.SODIUM -> NutrientUi("🧂", R.string.nutrient_sodium, R.color.nutrient_sodium, R.string.unit_mg)
            NutritionObject.POTASSIUM -> NutrientUi("🍌", R.string.nutrient_potassium, R.color.nutrient_potassium, R.string.unit_mg)
            NutritionObject.CREATINE -> NutrientUi("🔥", R.string.nutrient_creatine, R.color.nutrient_creatine, R.string.unit_g)
            else -> NutrientUi("🥦", R.string.nutrient_dietary_fiber, R.color.nutrient_dietary_fiber, R.string.unit_g)
        }
    }
}

/**
 * 이모지 아바타: 포인트 색을 연하게 깐 원 위에 이모지를 올린다
 */
fun TextView.showNutrientAvatar(ui: NutrientUi) {
    text = ui.emoji
    val tint = ColorUtils.setAlphaComponent(context.getColor(ui.colorRes), 0x3D)
    backgroundTintList = ColorStateList.valueOf(tint)
}
