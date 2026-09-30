package com.example.forgelauncher.data.preferences

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val FILE_NAME = "launcher_preferences"

private val Context.launcherDataStore by preferencesDataStore(
    name = FILE_NAME,
    produceMigrations = { context ->
        // Keeps theme and column choices made by people who used version 0.1.
        listOf(SharedPreferencesMigration(context, FILE_NAME))
    },
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class GlassStyleMode {
    AUTO,
    LIGHT,
    DARK,
}

data class GlassSettings(
    val tintArgb: Long = DEFAULT_GLASS_TINT_ARGB,
    val transparency: Float = DEFAULT_GLASS_TRANSPARENCY,
    val blurRadiusDp: Float = DEFAULT_GLASS_BLUR_DP,
    val cornerRadiusDp: Float = DEFAULT_GLASS_CORNER_DP,
    val styleMode: GlassStyleMode = GlassStyleMode.AUTO,
) {
    fun sanitized(): GlassSettings = copy(
        tintArgb = tintArgb and 0xFFFF_FFFFL,
        transparency = transparency.coerceIn(0f, 0.9f),
        blurRadiusDp = blurRadiusDp.coerceIn(0f, 40f),
        cornerRadiusDp = cornerRadiusDp.coerceIn(8f, 40f),
    )
}

data class LauncherSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val columns: Int = DEFAULT_COLUMNS,
    val glass: GlassSettings = GlassSettings(),
)

class LauncherPreferences(context: Context) {
    private val dataStore = context.applicationContext.launcherDataStore

    val settings: Flow<LauncherSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map(Preferences::toLauncherSettings)

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.themeMode] = mode.name }
    }

    suspend fun setColumns(columns: Int) {
        dataStore.edit { it[Keys.columns] = columns.coerceIn(4, 6) }
    }

    suspend fun setGlassSettings(settings: GlassSettings) {
        val safe = settings.sanitized()
        dataStore.edit { preferences ->
            preferences[Keys.glassTintArgb] = safe.tintArgb
            preferences[Keys.glassTransparency] = safe.transparency
            preferences[Keys.glassBlurRadiusDp] = safe.blurRadiusDp
            preferences[Keys.glassCornerRadiusDp] = safe.cornerRadiusDp
            preferences[Keys.glassStyle] = safe.styleMode.name
        }
    }
}

private fun Preferences.toLauncherSettings(): LauncherSettings = LauncherSettings(
    themeMode = enumValueOrDefault(this[Keys.themeMode], ThemeMode.SYSTEM),
    columns = (this[Keys.columns] ?: DEFAULT_COLUMNS).coerceIn(4, 6),
    glass = GlassSettings(
        tintArgb = this[Keys.glassTintArgb] ?: DEFAULT_GLASS_TINT_ARGB,
        transparency = this[Keys.glassTransparency] ?: DEFAULT_GLASS_TRANSPARENCY,
        blurRadiusDp = this[Keys.glassBlurRadiusDp] ?: DEFAULT_GLASS_BLUR_DP,
        cornerRadiusDp = this[Keys.glassCornerRadiusDp] ?: DEFAULT_GLASS_CORNER_DP,
        styleMode = enumValueOrDefault(this[Keys.glassStyle], GlassStyleMode.AUTO),
    ).sanitized(),
)

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, fallback: T): T =
    value?.let { saved -> enumValues<T>().firstOrNull { it.name == saved } } ?: fallback

private object Keys {
    val themeMode = stringPreferencesKey("theme_mode")
    val columns = intPreferencesKey("drawer_columns")
    val glassTintArgb = longPreferencesKey("glass_tint_argb")
    val glassTransparency = floatPreferencesKey("glass_transparency")
    val glassBlurRadiusDp = floatPreferencesKey("glass_blur_radius_dp")
    val glassCornerRadiusDp = floatPreferencesKey("glass_corner_radius_dp")
    val glassStyle = stringPreferencesKey("glass_style")
}

const val DEFAULT_COLUMNS = 4
const val DEFAULT_GLASS_TINT_ARGB = 0xFFBFD7FFL
const val DEFAULT_GLASS_TRANSPARENCY = 0.52f
const val DEFAULT_GLASS_BLUR_DP = 24f
const val DEFAULT_GLASS_CORNER_DP = 28f
