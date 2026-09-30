package com.example.forgelauncher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.forgelauncher.data.preferences.GlassStyleMode
import com.example.forgelauncher.data.preferences.ThemeMode
import com.example.forgelauncher.domain.model.LaunchableApp
import com.example.forgelauncher.ui.drawer.AppDrawer
import com.example.forgelauncher.ui.glass.ProvideGlassEnvironment
import com.example.forgelauncher.ui.home.HomeScreen
import com.example.forgelauncher.ui.settings.SettingsSheet
import com.example.forgelauncher.ui.theme.ForgeLauncherTheme
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun ForgeLauncherApp(
    uiState: LauncherUiState,
    onOpenDrawer: (Boolean) -> Unit,
    onCloseDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onCloseSettings: () -> Unit,
    onQueryChange: (String) -> Unit,
    onAppClick: (LaunchableApp) -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onColumnsSelected: (Int) -> Unit,
    onGlassStyleSelected: (GlassStyleMode) -> Unit,
    onGlassTintChanged: (Long) -> Unit,
    onGlassTransparencyChanged: (Float) -> Unit,
    onGlassBlurChanged: (Float) -> Unit,
    onGlassCornersChanged: (Float) -> Unit,
    onRequestDefault: () -> Unit,
    onOpenWallpaperPicker: () -> Unit,
    onMessageShown: () -> Unit,
) {
    ForgeLauncherTheme(themeMode = uiState.themeMode) {
        val hazeState = rememberHazeState()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(uiState.message) {
            val message = uiState.message ?: return@LaunchedEffect
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }

        // A Home activity should not finish when Back is pressed on its root screen.
        BackHandler {
            when {
                uiState.settingsOpen -> onCloseSettings()
                uiState.drawerOpen -> onCloseDrawer()
                else -> Unit
            }
        }

        ProvideGlassEnvironment(
            settings = uiState.glassSettings,
            hazeState = hazeState,
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(snackbarHostState) },
            ) { innerPadding ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    // Haze can capture this in-window scrim while Android continues to draw the
                    // real system wallpaper underneath the transparent Activity window.
                    Box(
                        Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState, zIndex = 0f)
                            .background(Color.Black.copy(alpha = 0.18f)),
                    )
                    HomeScreen(
                        apps = uiState.apps,
                        isDefaultLauncher = uiState.isDefaultLauncher,
                        onOpenDrawer = onOpenDrawer,
                        onOpenSettings = onOpenSettings,
                        onRequestDefault = onRequestDefault,
                        onAppClick = onAppClick,
                        modifier = Modifier.hazeSource(hazeState, zIndex = 1f),
                    )

                    AnimatedVisibility(
                        visible = uiState.drawerOpen,
                        enter = slideInVertically(initialOffsetY = { it / 3 }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it / 3 }) + fadeOut(),
                    ) {
                        AppDrawer(
                            apps = uiState.apps,
                            query = uiState.query,
                            columns = uiState.columns,
                            isLoading = uiState.isLoading,
                            focusSearchNonce = uiState.focusSearchNonce,
                            onQueryChange = onQueryChange,
                            onAppClick = onAppClick,
                            onClose = onCloseDrawer,
                            onOpenSettings = onOpenSettings,
                            modifier = Modifier.hazeSource(hazeState, zIndex = 2f),
                        )
                    }

                    if (uiState.settingsOpen) {
                        SettingsSheet(
                            themeMode = uiState.themeMode,
                            columns = uiState.columns,
                            glassSettings = uiState.glassSettings,
                            isDefaultLauncher = uiState.isDefaultLauncher,
                            onThemeSelected = onThemeSelected,
                            onColumnsSelected = onColumnsSelected,
                            onGlassStyleSelected = onGlassStyleSelected,
                            onGlassTintChanged = onGlassTintChanged,
                            onGlassTransparencyChanged = onGlassTransparencyChanged,
                            onGlassBlurChanged = onGlassBlurChanged,
                            onGlassCornersChanged = onGlassCornersChanged,
                            onRequestDefault = onRequestDefault,
                            onOpenWallpaperPicker = onOpenWallpaperPicker,
                            onDismiss = onCloseSettings,
                        )
                    }
                }
            }
        }
    }
}
