package com.example.forgelauncher.data.apps

import com.example.forgelauncher.domain.model.LaunchableApp
import kotlinx.coroutines.flow.StateFlow

interface AppRepository {
    val apps: StateFlow<List<LaunchableApp>>
    val isRefreshing: StateFlow<Boolean>

    fun start()
    fun refresh()
    fun launch(app: LaunchableApp): Result<Unit>
    fun close()
}
