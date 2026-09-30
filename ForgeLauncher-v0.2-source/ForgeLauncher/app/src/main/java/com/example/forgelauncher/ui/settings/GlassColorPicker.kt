package com.example.forgelauncher.ui.settings

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun GlassColorPicker(
    tintArgb: Long,
    onTintChanged: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hsv = remember(tintArgb) { tintArgb.toHsv() }
    val hueColor = remember(hsv.hue) { hsvToArgb(hsv.hue, 1f, 1f).toComposeColor() }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PresetColors.forEach { preset ->
                val selected = colorsAreClose(tintArgb, preset.argb)
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(preset.argb.toComposeColor(), CircleShape)
                        .border(
                            width = if (selected) 3.dp else 1.dp,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                Color.White.copy(alpha = 0.65f)
                            },
                            shape = CircleShape,
                        )
                        .semantics { contentDescription = "${preset.name} glass tint" }
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "Use ${preset.name} tint",
                            onClick = { onTintChanged(preset.argb) },
                        ),
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .background(Color.Black, RoundedCornerShape(16.dp))
                .semantics {
                    contentDescription = "Tint saturation and brightness. Drag to choose a color."
                }
                .pointerInput(hsv.hue) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        fun update(position: Offset) {
                            val saturation = (position.x / size.width).coerceIn(0f, 1f)
                            val value = (1f - position.y / size.height).coerceIn(0f, 1f)
                            onTintChanged(hsvToArgb(hsv.hue, saturation, value))
                        }
                        update(down.position)
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.firstOrNull()?.let { change ->
                                if (change.pressed) {
                                    update(change.position)
                                    change.consume()
                                }
                            }
                            if (event.changes.none { it.pressed }) break
                        }
                    }
                },
        ) {
            drawRect(
                brush = Brush.horizontalGradient(listOf(Color.White, hueColor)),
            )
            drawRect(
                brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)),
            )
            val marker = Offset(
                x = hsv.saturation * size.width,
                y = (1f - hsv.value) * size.height,
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.5f),
                radius = 8.dp.toPx(),
                center = marker,
            )
            drawCircle(
                color = Color.White,
                radius = 7.dp.toPx(),
                center = marker,
                style = Stroke(width = 2.dp.toPx()),
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .semantics { contentDescription = "Tint hue. Drag to change hue." }
                .pointerInput(hsv.saturation, hsv.value) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        fun update(position: Offset) {
                            val hue = (position.x / size.width).coerceIn(0f, 1f) * 360f
                            onTintChanged(hsvToArgb(hue, hsv.saturation, hsv.value))
                        }
                        update(down.position)
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.firstOrNull()?.let { change ->
                                if (change.pressed) {
                                    update(change.position)
                                    change.consume()
                                }
                            }
                            if (event.changes.none { it.pressed }) break
                        }
                    }
                },
        ) {
            drawRoundRect(
                brush = Brush.horizontalGradient(HueColors),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f),
            )
            val markerX = (hsv.hue / 360f) * size.width
            drawLine(
                color = Color.White,
                start = Offset(markerX, 2.dp.toPx()),
                end = Offset(markerX, size.height - 2.dp.toPx()),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Color.Black.copy(alpha = 0.45f),
                start = Offset(markerX, 3.dp.toPx()),
                end = Offset(markerX, size.height - 3.dp.toPx()),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        Text(
            text = "Tint ${tintArgb.toHexRgb()}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private data class HsvColor(
    val hue: Float,
    val saturation: Float,
    val value: Float,
)

private data class PresetColor(
    val name: String,
    val argb: Long,
)

private val PresetColors = listOf(
    PresetColor("Ice", 0xFFBFD7FFL),
    PresetColor("Violet", 0xFFC9B7FFL),
    PresetColor("Rose", 0xFFFFBDD7L),
    PresetColor("Amber", 0xFFFFD39AL),
    PresetColor("Mint", 0xFFAEEBD4L),
    PresetColor("Cyan", 0xFFA9E6F5L),
    PresetColor("Silver", 0xFFD8DCE5L),
)

private val HueColors = listOf(
    Color.Red,
    Color.Yellow,
    Color.Green,
    Color.Cyan,
    Color.Blue,
    Color.Magenta,
    Color.Red,
)

private fun Long.toHsv(): HsvColor {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(toInt(), hsv)
    return HsvColor(hsv[0], hsv[1], hsv[2])
}

private fun hsvToArgb(hue: Float, saturation: Float, value: Float): Long =
    AndroidColor.HSVToColor(floatArrayOf(hue, saturation, value)).toLong() and 0xFFFF_FFFFL

private fun Long.toComposeColor(): Color = Color(this and 0xFFFF_FFFFL)

private fun colorsAreClose(first: Long, second: Long): Boolean =
    (first and 0xFF_FFFFL) == (second and 0xFF_FFFFL)

private fun Long.toHexRgb(): String = "#%06X".format(this and 0xFF_FFFFL)
