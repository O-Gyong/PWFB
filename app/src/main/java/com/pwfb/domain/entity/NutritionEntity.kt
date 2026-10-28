package com.pwfb.domain.entity

import androidx.annotation.StringRes

data class NutritionEntity(
    val type: String,
    @StringRes val explainRes: Int
)
