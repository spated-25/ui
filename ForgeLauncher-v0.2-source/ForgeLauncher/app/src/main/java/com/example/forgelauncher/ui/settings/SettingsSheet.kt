package com.example.forgelauncher.ui.settings

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.forgelauncher.data.preferences.GlassSettings
import com.example.forgelauncher.data.preferences.GlassStyleMode
import com.example.forgelauncher.data.preferences.ThemeMode
import com.example.forgelauncher.ui.glass.GlassSurface
import com.example.forgelauncher.ui.glass.ProvideGlassEnvironment
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    themeMode: ThemeMode,
    columns: Int,
    glassSettings: GlassSettings,
    isDefaultLauncher: Boolean,
    onThemeSelected: (ThemeMode) -> Unit,
    onColumnsSelected: (Int) -> Unit,
    onGlassStyleSelected: (GlassStyleMode) -> Unit,
    onGlassTintChanged: (Long) -> Unit,
    onGlassTransparencyChanged: (Float) -> Unit,
    onGlassBlurChanged: (Float) -> Unit,
    onGlassCornersChanged: (Float) -> Unit,
    onRequestDefault: () -> Unit,
    onOpenWallpaperPicker: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 8.dp),
        ) {
            Text(
                text = "Launcher settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Glass changes appear here immediately and are saved automatically.",
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )

            GlassLivePreview(glassSettings)

            SectionTitle("Glass style")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GlassStyleMode.entries.forEach { mode ->
                    FilterChip(
                        selected = glassSettings.styleMode == mode,
                        onClick = { onGlassStyleSelected(mode) },
                        label = { Text(mode.displayName()) },
                    )
                }
            }
            Text(
                text = "Auto follows the launcher's current light or dark theme.",
                modifier = Modifier.padding(top = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )

            SectionTitle("Glass tint")
            GlassColorPicker(
                tintArgb = glassSettings.tintArgb,
                onTintChanged = onGlassTintChanged,
            )

            SettingSlider(
                title = "Transparency",
                valueLabel = "${(glassSettings.transparency * 100f).roundToInt()}%",
                value = glassSettings.transparency,
                valueRange = 0f..0.9f,
                onValueChange = onGlassTransparencyChanged,
            )
            SettingSlider(
                title = "Blur strength",
                valueLabel = "${glassSettings.blurRadiusDp.roundToInt()} dp",
                value = glassSettings.blurRadiusDp,
                valueRange = 0f..40f,
                onValueChange = onGlassBlurChanged,
            )
            SettingSlider(
                title = "Corner roundness",
                valueLabel = "${glassSettings.cornerRadiusDp.roundToInt()} dp",
                value = glassSettings.cornerRadiusDp,
                valueRange = 8f..40f,
                onValueChange = onGlassCornersChanged,
            )

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                Text(
                    text = "This phone uses the lightweight translucent fallback instead of live blur. Tint, borders, highlights, shadows, corners, and press animation still work.",
                    modifier = Modifier.padding(top = 6.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            SectionTitle("Launcher theme")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = themeMode == mode,
                        onClick = { onThemeSelected(mode) },
                        label = { Text(mode.name.lowercase().replaceFirstChar(Char::uppercase)) },
                    )
                }
            }

            OutlinedButton(
                onClick = onOpenWallpaperPicker,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Choose system wallpaper")
            }

            SectionTitle("App drawer columns")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                (4..6).forEach { count ->
                    FilterChip(
                        selected = columns == count,
                        onClick = { onColumnsSelected(count) },
                        label = { Text(count.toString()) },
                    )
                }
            }

            SectionTitle("Default Home app")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = if (isDefaultLauncher) {
                            "Forge Launcher is your default Home app."
                        } else {
                            "Android must approve which app opens when you press Home."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (!isDefaultLauncher) {
                        Button(
                            onClick = onRequestDefault,
                            modifier = Modifier.padding(top = 12.dp),
                        ) {
                            Text("Make default launcher")
                        }
                    }
                }
            }

            SectionTitle("Next foundations")
            Text(
                text = "Home icon placement, folders, gestures, widgets, icon packs, backup and restore. Refraction remains reserved for a later Android 13+ iteration.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(Modifier.height(28.dp))
            Text(
                text = "Forge Launcher 0.2.0",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun GlassLivePreview(settings: GlassSettings) {
    val previewHazeState = rememberHazeState()
    ProvideGlassEnvironment(settings = settings, hazeState = previewHazeState) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(154.dp)
                .clip(RoundedCornerShape(30.dp)),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(previewHazeState),
            ) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF3D2C8D),
                            Color(0xFF2E8BB8),
                            Color(0xFFEE8E73),
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height),
                    ),
                )
                drawCircle(
                    color = Color(0xFFFFD36E).copy(alpha = 0.85f),
                    radius = size.minDimension * 0.3f,
                    center = Offset(size.width * 0.8f, size.height * 0.15f),
                )
                drawCircle(
                    color = Color(0xFF73E7C1).copy(alpha = 0.7f),
                    radius = size.minDimension * 0.24f,
                    center = Offset(size.width * 0.16f, size.height * 0.82f),
                )
            }

            GlassSurface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.78f),
                onClick = {},
                onClickLabel = "Try the glass press animation",
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                shadowElevation = 14.dp,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Live glass preview",
                        color = LocalContentColor.current,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Touch to test the soft press",
                        color = LocalContentColor.current.copy(alpha = 0.76f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingSlider(
    title: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = valueLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(top = 22.dp, bottom = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
    )
}

private fun GlassStyleMode.displayName(): String = when (this) {
    GlassStyleMode.AUTO -> "Auto"
    GlassStyleMode.LIGHT -> "Light"
    GlassStyleMode.DARK -> "Dark"
}
