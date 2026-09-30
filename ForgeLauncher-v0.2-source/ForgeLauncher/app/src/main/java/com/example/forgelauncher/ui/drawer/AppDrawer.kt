package com.example.forgelauncher.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.forgelauncher.domain.model.LaunchableApp
import com.example.forgelauncher.domain.model.matchesAppSearch
import com.example.forgelauncher.ui.components.AppIcon
import com.example.forgelauncher.ui.glass.GlassSurface

@Composable
fun AppDrawer(
    apps: List<LaunchableApp>,
    query: String,
    columns: Int,
    isLoading: Boolean,
    focusSearchNonce: Int,
    onQueryChange: (String) -> Unit,
    onAppClick: (LaunchableApp) -> Unit,
    onClose: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val filteredApps = remember(apps, query) {
        apps.filter { matchesAppSearch(it.label, it.packageName, query) }
    }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(focusSearchNonce) {
        if (focusSearchNonce > 0) {
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }

    GlassSurface(
        modifier = modifier.fillMaxSize(),
        cornerRadius = 0.dp,
        shadowElevation = 0.dp,
        blurStrengthMultiplier = 0.85f,
        contentAlignment = Alignment.TopStart,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .swipeDown(onClose)
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .size(width = 42.dp, height = 5.dp)
                        .background(LocalContentColor.current.copy(alpha = 0.45f), CircleShape),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close app drawer")
                    }
                    Text(
                        text = "All apps",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Launcher settings")
                    }
                }
            }

            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                shadowElevation = 7.dp,
            ) {
                val searchContentColor = LocalContentColor.current
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    placeholder = { Text("Search apps") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = searchContentColor,
                        unfocusedTextColor = searchContentColor,
                        focusedLeadingIconColor = searchContentColor,
                        unfocusedLeadingIconColor = searchContentColor.copy(alpha = 0.8f),
                        focusedTrailingIconColor = searchContentColor,
                        unfocusedTrailingIconColor = searchContentColor,
                        focusedPlaceholderColor = searchContentColor.copy(alpha = 0.65f),
                        unfocusedPlaceholderColor = searchContentColor.copy(alpha = 0.65f),
                        cursorColor = searchContentColor,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                )
            }

            Text(
                text = if (query.isBlank()) "${apps.size} apps" else "${filteredApps.size} matches",
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                color = LocalContentColor.current.copy(alpha = 0.72f),
                style = MaterialTheme.typography.labelLarge,
            )

            when {
                isLoading && apps.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = LocalContentColor.current)
                }

                filteredApps.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No apps match \"$query\"",
                        color = LocalContentColor.current.copy(alpha = 0.72f),
                    )
                }

                else -> BoxWithConstraints(Modifier.fillMaxSize()) {
                    val maxColumnsForWidth = (maxWidth / 58.dp).toInt().coerceAtLeast(4)
                    val responsiveColumns = columns.coerceIn(4, minOf(6, maxColumnsForWidth))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(responsiveColumns),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 8.dp,
                            end = 8.dp,
                            top = 4.dp,
                            bottom = 32.dp,
                        ),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        items(
                            items = filteredApps,
                            key = LaunchableApp::stableKey,
                        ) { app ->
                            AppIcon(
                                app = app,
                                onClick = { onAppClick(app) },
                                modifier = Modifier.padding(horizontal = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.swipeDown(onSwipeDown: () -> Unit): Modifier = pointerInput(onSwipeDown) {
    var downwardDrag = 0f
    detectVerticalDragGestures(
        onDragStart = { downwardDrag = 0f },
        onVerticalDrag = { change, dragAmount ->
            if (dragAmount > 0) downwardDrag += dragAmount
            change.consume()
        },
        onDragEnd = {
            if (downwardDrag > 54.dp.toPx()) onSwipeDown()
            downwardDrag = 0f
        },
        onDragCancel = { downwardDrag = 0f },
    )
}
