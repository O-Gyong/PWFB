package com.pwfb.common

import com.pwfb.R
import com.pwfb.domain.entity.NutritionEntity

object NutritionObject {
    const val CARBOHYDRATE = "carbohydrate"
    const val PROTEIN = "protein"
    const val FAT = "fat"
    const val WATER = "water"
    const val SODIUM = "sodium"
    const val POTASSIUM = "potassium"
    const val CREATINE = "creatine"
    const val DIETARY_FIBER = "dietaryFiber"

    val nutritionEntityLists = listOf(
        NutritionEntity(CARBOHYDRATE, R.string.nutrition_explain_carbohydrate),
        NutritionEntity(PROTEIN, R.string.nutrition_explain_protein),
        NutritionEntity(FAT, R.string.nutrition_explain_fat),
        NutritionEntity(WATER, R.string.nutrition_explain_water),
        NutritionEntity(SODIUM, R.string.nutrition_explain_sodium),
        NutritionEntity(POTASSIUM, R.string.nutrition_explain_potassium),
        NutritionEntity(CREATINE, R.string.nutrition_explain_creatine),
        NutritionEntity(DIETARY_FIBER, R.string.nutrition_explain_dietary_fiber)
    )
}
