package com.example.forgelauncher.ui.glass

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.forgelauncher.data.preferences.GlassSettings
import com.example.forgelauncher.data.preferences.GlassStyleMode
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur

/**
 * The rendering seam for glass effects. A Refraction implementation can be added later without
 * changing every dock, search, clock, and icon call site.
 */
sealed interface GlassOpticalEffect {
    data object BackdropBlur : GlassOpticalEffect
}

private val LocalGlassSettings = staticCompositionLocalOf { GlassSettings() }
private val LocalGlassHazeState = staticCompositionLocalOf<HazeState?> { null }

@Composable
fun ProvideGlassEnvironment(
    settings: GlassSettings,
    hazeState: HazeState,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalGlassSettings provides settings.sanitized(),
        LocalGlassHazeState provides hazeState,
        content = content,
    )
}

/**
 * Reusable Liquid-Glass-inspired surface.
 *
 * Android 13+ uses Haze for a captured backdrop blur. Android 12 and below intentionally use the
 * lightweight tint/border/shadow fallback because those versions need more expensive workarounds.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    enabled: Boolean = true,
    cornerRadius: Dp? = null,
    shadowElevation: Dp = 12.dp,
    blurStrengthMultiplier: Float = 1f,
    opticalEffect: GlassOpticalEffect = GlassOpticalEffect.BackdropBlur,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    val settings = LocalGlassSettings.current
    val hazeState = LocalGlassHazeState.current
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val palette = remember(settings, darkTheme) { resolvePalette(settings, darkTheme) }
    val radius = cornerRadius ?: settings.cornerRadiusDp.dp
    val shape: Shape = if (radius == 0.dp) RectangleShape else RoundedCornerShape(radius)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.975f else 1f,
        animationSpec = spring(stiffness = 700f, dampingRatio = 0.72f),
        label = "glass press scale",
    )
    val pressLight by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = spring(stiffness = 650f, dampingRatio = 0.82f),
        label = "glass press light",
    )
    val canBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && hazeState != null
    val safeBlurRadius = (settings.blurRadiusDp * blurStrengthMultiplier)
        .coerceIn(0f, 40f)
        .dp

    val blurStyle = remember(palette, safeBlurRadius, canBlur) {
        HazeBlurStyle {
            blurEnabled(canBlur)
            blurRadius(safeBlurRadius)
            noiseFactor(0.025f)
            backgroundColor(palette.blurBackground)
            colorEffects(
                listOf(HazeColorEffect.tint(palette.tint.copy(alpha = palette.tintAlpha))),
            )
            fallbackColorEffect(
                HazeColorEffect.tint(palette.tint.copy(alpha = palette.fallbackAlpha)),
            )
            alpha(palette.effectAlpha)
        }
    }

    val effectModifier = when (opticalEffect) {
        GlassOpticalEffect.BackdropBlur -> {
            if (canBlur) {
                Modifier.hazeBlur(
                    input = HazeInput.Backdrop(requireNotNull(hazeState)),
                    style = blurStyle,
                    performanceMode = HazePerformanceMode.Performance,
                )
            } else {
                Modifier
            }
        }
    }

    val clickModifier = if (onClick == null) {
        Modifier
    } else {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = Role.Button,
            onClickLabel = onClickLabel,
            onClick = onClick,
        )
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                clip = false,
                ambientColor = palette.shadow,
                spotColor = palette.shadow,
            )
            .clip(shape)
            .then(effectModifier)
            .background(palette.tint.copy(alpha = palette.fillAlpha), shape)
            .drawWithCache {
                val outline = shape.createOutline(size, layoutDirection, this)
                val borderWidth = 1.dp.toPx()
                val innerWidth = 0.75.dp.toPx()
                val inset = 2.dp.toPx()
                val topInset = radius.toPx().coerceAtMost(size.width * 0.32f)
                val borderBrush = Brush.verticalGradient(
                    colors = listOf(palette.borderTop, palette.borderMiddle, palette.borderBottom),
                    endY = size.height,
                )
                val innerBrush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, palette.innerHighlight, Color.Transparent),
                )

                onDrawWithContent {
                    drawContent()
                    drawOutline(outline = outline, brush = borderBrush, style = Stroke(borderWidth))
                    if (size.width > topInset * 2f) {
                        drawLine(
                            brush = innerBrush,
                            start = Offset(topInset, inset),
                            end = Offset(size.width - topInset, inset),
                            strokeWidth = innerWidth,
                        )
                    }
                    if (pressLight > 0f) {
                        drawOutline(
                            outline = outline,
                            color = Color.White.copy(alpha = 0.11f * pressLight),
                        )
                    }
                }
            }
            .then(clickModifier),
        contentAlignment = contentAlignment,
    ) {
        CompositionLocalProvider(LocalContentColor provides palette.content) {
            Box(
                modifier = Modifier.padding(contentPadding),
                contentAlignment = contentAlignment,
                content = content,
            )
        }
    }
}

private data class GlassPalette(
    val tint: Color,
    val tintAlpha: Float,
    val fillAlpha: Float,
    val fallbackAlpha: Float,
    val effectAlpha: Float,
    val blurBackground: Color,
    val content: Color,
    val borderTop: Color,
    val borderMiddle: Color,
    val borderBottom: Color,
    val innerHighlight: Color,
    val shadow: Color,
)

private fun resolvePalette(settings: GlassSettings, darkTheme: Boolean): GlassPalette {
    val darkGlass = when (settings.styleMode) {
        GlassStyleMode.AUTO -> darkTheme
        GlassStyleMode.LIGHT -> false
        GlassStyleMode.DARK -> true
    }
    val userTint = Color(settings.tintArgb)
    val adjustedTint = if (darkGlass) {
        lerp(userTint, Color.Black, 0.34f)
    } else {
        lerp(userTint, Color.White, 0.28f)
    }
    val opacity = 1f - settings.transparency
    val fillAlpha = 0.09f + (opacity * 0.54f)

    return GlassPalette(
        tint = adjustedTint,
        tintAlpha = 0.08f + (opacity * 0.2f),
        fillAlpha = fillAlpha,
        fallbackAlpha = fillAlpha.coerceAtLeast(0.16f),
        effectAlpha = 0.82f + (opacity * 0.18f),
        blurBackground = if (darkGlass) Color(0xFF12151C) else Color(0xFFF3F7FF),
        content = if (darkGlass) Color(0xFFF8FAFF) else Color(0xFF111722),
        borderTop = Color.White.copy(alpha = if (darkGlass) 0.72f else 0.9f),
        borderMiddle = Color.White.copy(alpha = if (darkGlass) 0.28f else 0.5f),
        borderBottom = if (darkGlass) {
            Color.White.copy(alpha = 0.1f)
        } else {
            Color.Black.copy(alpha = 0.14f)
        },
        innerHighlight = Color.White.copy(alpha = if (darkGlass) 0.42f else 0.72f),
        shadow = Color.Black.copy(alpha = if (darkGlass) 0.34f else 0.24f),
    )
}
