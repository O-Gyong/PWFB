package com.pwfb.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pwfb.R

/** Pretendard Std (SIL Open Font License 1.1, licenses/Pretendard-OFL.txt) */
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold)
)

private fun TextStyle.pretendard(weight: FontWeight, letterSpacingSp: Float? = null): TextStyle = copy(
    fontFamily = Pretendard,
    fontWeight = weight,
    letterSpacing = letterSpacingSp?.sp ?: letterSpacing
)

/**
 * Material 3 type scale 전체를 Pretendard로. Display·Headline 700, Title·Label 600, Body 400.
 */
val Typography: Typography = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.pretendard(FontWeight.Bold, -1.5f),
        displayMedium = base.displayMedium.pretendard(FontWeight.Bold, -1.2f),
        displaySmall = base.displaySmall.pretendard(FontWeight.Bold, -0.7f),
        headlineLarge = base.headlineLarge.pretendard(FontWeight.Bold, -0.6f),
        headlineMedium = base.headlineMedium.pretendard(FontWeight.Bold, -0.5f),
        headlineSmall = base.headlineSmall.pretendard(FontWeight.Bold, -0.2f),
        titleLarge = base.titleLarge.pretendard(FontWeight.SemiBold),
        titleMedium = base.titleMedium.pretendard(FontWeight.SemiBold),
        titleSmall = base.titleSmall.pretendard(FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.pretendard(FontWeight.Normal),
        bodyMedium = base.bodyMedium.pretendard(FontWeight.Normal),
        bodySmall = base.bodySmall.pretendard(FontWeight.Normal),
        labelLarge = base.labelLarge.pretendard(FontWeight.SemiBold),
        labelMedium = base.labelMedium.pretendard(FontWeight.SemiBold),
        labelSmall = base.labelSmall.pretendard(FontWeight.SemiBold)
    )
}
