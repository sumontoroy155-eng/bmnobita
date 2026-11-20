package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.model.AppThemeAccent

val BmShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun BMGamingProTheme(
    accent: AppThemeAccent = AppThemeAccent.CRIMSON_RED,
    content: @Composable () -> Unit
) {
    val (primary, secondary, container) = when (accent) {
        AppThemeAccent.CRIMSON_RED -> Triple(CrimsonPrimary, CrimsonBright, CrimsonDark)
        AppThemeAccent.BLOOD_RUBY -> Triple(RubyPrimary, RubyBright, RubyDark)
        AppThemeAccent.CYBER_EMBER -> Triple(EmberPrimary, EmberBright, EmberDark)
    }

    val colorScheme = darkColorScheme(
        primary = primary,
        onPrimary = PureWhite,
        primaryContainer = container,
        onPrimaryContainer = PureWhite,
        secondary = secondary,
        onSecondary = PureWhite,
        secondaryContainer = TacticalSurfaceElevated,
        onSecondaryContainer = SilverText,
        tertiary = CyanHud,
        onTertiary = ObsidianBlack,
        background = ObsidianBlack,
        onBackground = PureWhite,
        surface = DeepCarbon,
        onSurface = PureWhite,
        surfaceVariant = TacticalSurface,
        onSurfaceVariant = SilverText,
        outline = TacticalBorder,
        outlineVariant = TacticalSurfaceElevated,
        error = secondary,
        onError = PureWhite
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BmTypography,
        shapes = BmShapes,
        content = content
    )
}
