package com.example.forgelauncher

import com.example.forgelauncher.domain.model.matchesAppSearch
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSearchTest {
    @Test
    fun emptyQueryMatchesAnyApp() {
        assertTrue(matchesAppSearch("Camera", "com.android.camera", "  "))
    }

    @Test
    fun labelSearchIgnoresCase() {
        assertTrue(matchesAppSearch("Google Photos", "com.google.android.apps.photos", "PHOtOs"))
    }

    @Test
    fun packageNameCanBeSearched() {
        assertTrue(matchesAppSearch("Messages", "com.google.android.apps.messaging", "messaging"))
    }

    @Test
    fun everySearchTermMustMatch() {
        assertTrue(matchesAppSearch("Google Photos", "com.google.android.apps.photos", "google photos"))
        assertFalse(matchesAppSearch("Google Photos", "com.google.android.apps.photos", "google camera"))
    }
}
