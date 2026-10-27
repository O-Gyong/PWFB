package com.pwfb.ui.home

import com.pwfb.R
import com.pwfb.common.NutritionObject
import org.junit.Assert.assertEquals
import org.junit.Test

class NutritionIntakeTest {
    private val kg = 70.0

    @Test
    fun carbohydrate_scalesWithWeightPerDay() {
        assertEquals(IntakeText(R.string.home_carbohydrate_day_654), NutritionIntake.of(NutritionObject.CARBOHYDRATE, -5, kg))
        assertEquals(IntakeText(R.string.home_carbohydrate_day_32, listOf(700)), NutritionIntake.of(NutritionObject.CARBOHYDRATE, -3, kg))
        assertEquals(IntakeText(R.string.home_carbohydrate_day_32, listOf(466)), NutritionIntake.of(NutritionObject.CARBOHYDRATE, -2, kg))
        assertEquals(IntakeText(R.string.home_carbohydrate_day_1, listOf(70, 210)), NutritionIntake.of(NutritionObject.CARBOHYDRATE, -1, kg))
        assertEquals(IntakeText(R.string.home_carbohydrate_day_7), NutritionIntake.of(NutritionObject.CARBOHYDRATE, -7, kg))
        assertEquals(IntakeText(R.string.home_carbohydrate_day_7), NutritionIntake.of(NutritionObject.CARBOHYDRATE, 0, kg))
    }

    @Test
    fun protein_and_fat_useWeightMultipliers() {
        assertEquals(IntakeText(R.string.home_protein_day_654, listOf(168, 210)), NutritionIntake.of(NutritionObject.PROTEIN, -4, kg))
        assertEquals(IntakeText(R.string.home_protein_day_32, listOf(112)), NutritionIntake.of(NutritionObject.PROTEIN, -2, kg))
        assertEquals(IntakeText(R.string.home_protein_day_1, listOf(210)), NutritionIntake.of(NutritionObject.PROTEIN, -1, kg))
        assertEquals(IntakeText(R.string.home_fat_day_7654, listOf(49, 84)), NutritionIntake.of(NutritionObject.FAT, -7, kg))
        assertEquals(IntakeText(R.string.home_fat_day_32, listOf(35)), NutritionIntake.of(NutritionObject.FAT, -3, kg))
        assertEquals(IntakeText(R.string.home_fat_day_1, listOf(49, 70)), NutritionIntake.of(NutritionObject.FAT, -1, kg))
    }

    @Test
    fun outsidePeakWeek_showsUsualIntakeWithEmoji() {
        assertEquals(IntakeText(R.string.nutrition_usually_intake, listOf("🥩")), NutritionIntake.of(NutritionObject.PROTEIN, -7, kg))
        assertEquals(IntakeText(R.string.nutrition_usually_intake, listOf("💧")), NutritionIntake.of(NutritionObject.WATER, 3, kg))
        assertEquals(IntakeText(R.string.nutrition_usually_intake, listOf("🍌")), NutritionIntake.of(NutritionObject.POTASSIUM, -2, kg))
    }

    @Test
    fun fixedTextItems_dependOnlyOnDay() {
        assertEquals(IntakeText(R.string.home_water_day_3), NutritionIntake.of(NutritionObject.WATER, -3, kg))
        assertEquals(IntakeText(R.string.home_water_day_2), NutritionIntake.of(NutritionObject.WATER, -2, kg))
        assertEquals(IntakeText(R.string.home_water_day_1), NutritionIntake.of(NutritionObject.WATER, -1, kg))
        assertEquals(IntakeText(R.string.home_sodium_day_2), NutritionIntake.of(NutritionObject.SODIUM, -2, kg))
        assertEquals(IntakeText(R.string.home_potassium_day_3), NutritionIntake.of(NutritionObject.POTASSIUM, -3, kg))
        assertEquals(IntakeText(R.string.home_creatine_day_321), NutritionIntake.of(NutritionObject.CREATINE, -1, kg))
        assertEquals(IntakeText(R.string.home_dietary_fiber_day_654), NutritionIntake.of(NutritionObject.DIETARY_FIBER, -6, kg))
    }

    @Test
    fun unknownType_fallsBackToUsualIntake() {
        assertEquals(IntakeText(R.string.nutrition_usually_intake), NutritionIntake.of("unknown", -3, kg))
    }
}
