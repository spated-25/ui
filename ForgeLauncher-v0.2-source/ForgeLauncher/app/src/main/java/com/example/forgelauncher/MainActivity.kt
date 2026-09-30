package com.example.forgelauncher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.graphics.drawable.toDrawable
import com.example.forgelauncher.ui.ForgeLauncherApp
import com.example.forgelauncher.ui.LauncherViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: LauncherViewModel by viewModels()

    private val defaultLauncherRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        viewModel.refreshDefaultLauncherStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            ForgeLauncherApp(
                uiState = uiState,
                onOpenDrawer = viewModel::openDrawer,
                onCloseDrawer = viewModel::closeDrawer,
                onOpenSettings = viewModel::openSettings,
                onCloseSettings = viewModel::closeSettings,
                onQueryChange = viewModel::updateQuery,
                onAppClick = viewModel::launch,
                onThemeSelected = viewModel::selectTheme,
                onColumnsSelected = viewModel::selectColumns,
                onGlassStyleSelected = viewModel::selectGlassStyle,
                onGlassTintChanged = viewModel::updateGlassTint,
                onGlassTransparencyChanged = viewModel::updateGlassTransparency,
                onGlassBlurChanged = viewModel::updateGlassBlur,
                onGlassCornersChanged = viewModel::updateGlassCorners,
                onRequestDefault = ::requestDefaultLauncher,
                onOpenWallpaperPicker = ::openWallpaperPicker,
                onMessageShown = viewModel::consumeMessage,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.goHome()
    }

    private fun requestDefaultLauncher() {
        val intent = viewModel.defaultLauncherRequestIntent()
        if (intent == null) {
            viewModel.showMessage("Forge Launcher is already your default Home app.")
            viewModel.refreshDefaultLauncherStatus()
        } else {
            defaultLauncherRequest.launch(intent)
        }
    }

    private fun openWallpaperPicker() {
        val intent = Intent(Intent.ACTION_SET_WALLPAPER)
        runCatching { startActivity(Intent.createChooser(intent, "Choose wallpaper")) }
            .onFailure { viewModel.showMessage("No wallpaper picker was found on this phone.") }
    }
}
