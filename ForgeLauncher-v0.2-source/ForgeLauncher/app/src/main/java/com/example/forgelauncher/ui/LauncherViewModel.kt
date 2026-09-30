package com.example.forgelauncher.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.forgelauncher.data.apps.AndroidAppRepository
import com.example.forgelauncher.data.apps.AppRepository
import com.example.forgelauncher.data.preferences.GlassSettings
import com.example.forgelauncher.data.preferences.GlassStyleMode
import com.example.forgelauncher.data.preferences.LauncherPreferences
import com.example.forgelauncher.data.preferences.ThemeMode
import com.example.forgelauncher.domain.model.LaunchableApp
import com.example.forgelauncher.platform.DefaultLauncherManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class LauncherUiState(
    val apps: List<LaunchableApp> = emptyList(),
    val isLoading: Boolean = true,
    val query: String = "",
    val drawerOpen: Boolean = false,
    val settingsOpen: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val columns: Int = 4,
    val glassSettings: GlassSettings = GlassSettings(),
    val isDefaultLauncher: Boolean = false,
    val focusSearchNonce: Int = 0,
    val message: String? = null,
)

/** Coordinates launcher data and screen state without exposing Android services to Compose. */
class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository = AndroidAppRepository(application)
    private val preferences = LauncherPreferences(application)
    private val defaultLauncherManager = DefaultLauncherManager(application)
    private var glassSaveJob: Job? = null

    private val _uiState = MutableStateFlow(
        LauncherUiState(
            isDefaultLauncher = defaultLauncherManager.isDefaultLauncher(),
        ),
    )
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    init {
        repository.apps
            .onEach { apps -> _uiState.update { it.copy(apps = apps) } }
            .launchIn(viewModelScope)
        repository.isRefreshing
            .onEach { loading -> _uiState.update { it.copy(isLoading = loading) } }
            .launchIn(viewModelScope)
        preferences.settings
            .onEach { settings ->
                _uiState.update {
                    it.copy(
                        themeMode = settings.themeMode,
                        columns = settings.columns,
                        glassSettings = settings.glass,
                    )
                }
            }
            .launchIn(viewModelScope)
        repository.start()
    }

    fun onResume() {
        refreshDefaultLauncherStatus()
        repository.refresh()
    }

    fun openDrawer(focusSearch: Boolean = false) {
        _uiState.update {
            it.copy(
                drawerOpen = true,
                settingsOpen = false,
                focusSearchNonce = if (focusSearch) it.focusSearchNonce + 1 else it.focusSearchNonce,
            )
        }
    }

    fun closeDrawer() {
        _uiState.update { it.copy(drawerOpen = false, query = "") }
    }

    fun openSettings() {
        _uiState.update { it.copy(settingsOpen = true, drawerOpen = false, query = "") }
    }

    fun closeSettings() {
        persistGlassSettingsImmediately()
        _uiState.update { it.copy(settingsOpen = false) }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun launch(app: LaunchableApp) {
        repository.launch(app).onSuccess {
            closeDrawer()
        }.onFailure {
            _uiState.update { state ->
                state.copy(message = "Could not open ${app.label}.")
            }
        }
    }

    fun selectTheme(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun selectColumns(columns: Int) {
        val safeColumns = columns.coerceIn(4, 6)
        _uiState.update { it.copy(columns = safeColumns) }
        viewModelScope.launch { preferences.setColumns(safeColumns) }
    }

    fun selectGlassStyle(mode: GlassStyleMode) {
        updateGlassSettings { copy(styleMode = mode) }
    }

    fun updateGlassTint(tintArgb: Long) {
        updateGlassSettings { copy(tintArgb = tintArgb) }
    }

    fun updateGlassTransparency(transparency: Float) {
        updateGlassSettings { copy(transparency = transparency) }
    }

    fun updateGlassBlur(blurRadiusDp: Float) {
        updateGlassSettings { copy(blurRadiusDp = blurRadiusDp) }
    }

    fun updateGlassCorners(cornerRadiusDp: Float) {
        updateGlassSettings { copy(cornerRadiusDp = cornerRadiusDp) }
    }

    fun defaultLauncherRequestIntent(): Intent? = defaultLauncherManager.requestIntent()

    fun refreshDefaultLauncherStatus() {
        _uiState.update {
            it.copy(isDefaultLauncher = defaultLauncherManager.isDefaultLauncher())
        }
    }

    fun showMessage(message: String) {
        _uiState.update { it.copy(message = message) }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun goHome() {
        if (_uiState.value.settingsOpen) persistGlassSettingsImmediately()
        _uiState.update {
            it.copy(drawerOpen = false, settingsOpen = false, query = "")
        }
    }

    private fun updateGlassSettings(transform: GlassSettings.() -> GlassSettings) {
        _uiState.update { state ->
            state.copy(glassSettings = state.glassSettings.transform().sanitized())
        }
        glassSaveJob?.cancel()
        glassSaveJob = viewModelScope.launch {
            // Keeps a fast slider drag from creating dozens of disk writes.
            delay(GLASS_SAVE_DEBOUNCE_MS)
            preferences.setGlassSettings(_uiState.value.glassSettings)
        }
    }

    private fun persistGlassSettingsImmediately() {
        val settings = _uiState.value.glassSettings
        glassSaveJob?.cancel()
        glassSaveJob = viewModelScope.launch {
            preferences.setGlassSettings(settings)
        }
    }

    override fun onCleared() {
        repository.close()
    }

    private companion object {
        const val GLASS_SAVE_DEBOUNCE_MS = 140L
    }
}
