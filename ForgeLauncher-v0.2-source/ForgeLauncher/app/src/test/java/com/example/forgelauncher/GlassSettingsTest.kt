package com.example.forgelauncher

import com.example.forgelauncher.data.preferences.GlassSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class GlassSettingsTest {
    @Test
    fun outOfRangeValuesAreClampedBeforeUseOrStorage() {
        val settings = GlassSettings(
            transparency = 4f,
            blurRadiusDp = -12f,
            cornerRadiusDp = 200f,
        ).sanitized()

        assertEquals(0.9f, settings.transparency)
        assertEquals(0f, settings.blurRadiusDp)
        assertEquals(40f, settings.cornerRadiusDp)
    }

    @Test
    fun tintIsKeptAsA32BitArgbColor() {
        val settings = GlassSettings(tintArgb = 0x12_FFBFD7FFL).sanitized()

        assertEquals(0xFFBFD7FFL, settings.tintArgb)
    }
}
