package com.example.forgelauncher.ui.home

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forgelauncher.domain.model.LaunchableApp
import com.example.forgelauncher.ui.components.AppIcon
import com.example.forgelauncher.ui.glass.GlassSurface
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    apps: List<LaunchableApp>,
    isDefaultLauncher: Boolean,
    onOpenDrawer: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onRequestDefault: () -> Unit,
    onAppClick: (LaunchableApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dockApps = remember(apps) { selectDockApps(apps) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .swipeUp { onOpenDrawer(false) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "FORGE",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.24f),
                ) {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Launcher settings",
                            tint = Color.White,
                        )
                    }
                }
            }

            if (!isDefaultLauncher) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    color = Color.Black.copy(alpha = 0.38f),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Ready to become your Home screen",
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f),
                        )
                        Button(
                            onClick = onRequestDefault,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC7BFFF),
                                contentColor = Color(0xFF24195C),
                            ),
                        ) {
                            Text("Set", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(58.dp))
            ClockAndDate()
            Spacer(Modifier.weight(1f))

            if (dockApps.isNotEmpty()) {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        dockApps.forEach { app ->
                            AppIcon(
                                app = app,
                                onClick = { onAppClick(app) },
                                modifier = Modifier.width(68.dp),
                                compact = true,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            GlassSurface(
                onClick = { onOpenDrawer(true) },
                onClickLabel = "Open app search",
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 13.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = LocalContentColor.current.copy(alpha = 0.9f),
                    )
                    Text(
                        text = "Search ${apps.size} apps",
                        color = LocalContentColor.current.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                    )
                }
            }

            Row(
                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    Modifier
                        .size(width = 28.dp, height = 4.dp)
                        .background(Color.White.copy(alpha = 0.72f), CircleShape),
                )
                Text(
                    text = "Swipe up for all apps",
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun ClockAndDate() {
    val context = LocalContext.current
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            val current = LocalDateTime.now()
            now = current
            delay((60 - current.second).coerceAtLeast(1) * 1_000L)
        }
    }

    val timePattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm"
    val time = remember(now, timePattern) {
        now.format(DateTimeFormatter.ofPattern(timePattern, Locale.getDefault()))
    }
    val date = remember(now.toLocalDate()) {
        now.toLocalDate().format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.getDefault()),
        )
    }

    GlassSurface(
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 16.dp),
        shadowElevation = 10.dp,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = time,
                color = LocalContentColor.current,
                fontSize = 64.sp,
                lineHeight = 70.sp,
                fontWeight = FontWeight.Light,
            )
            Text(
                text = date,
                color = LocalContentColor.current.copy(alpha = 0.88f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun selectDockApps(apps: List<LaunchableApp>): List<LaunchableApp> {
    val preferredMatchers: List<(LaunchableApp) -> Boolean> = listOf(
        { app ->
            app.packageName.contains("dialer", ignoreCase = true) ||
                app.label.equals("Phone", ignoreCase = true) ||
                app.label.equals("Dialer", ignoreCase = true)
        },
        { app ->
            app.packageName.contains("messag", ignoreCase = true) ||
                app.packageName.endsWith(".mms", ignoreCase = true) ||
                app.label.startsWith("Messages", ignoreCase = true)
        },
        { app ->
            app.packageName.contains("chrome", ignoreCase = true) ||
                app.packageName.contains("browser", ignoreCase = true)
        },
        { app ->
            app.packageName.contains("camera", ignoreCase = true) ||
                app.label.equals("Camera", ignoreCase = true)
        },
    )
    val selected = preferredMatchers.mapNotNull { matches ->
        apps.firstOrNull(matches)
    }.distinctBy(LaunchableApp::stableKey).toMutableList()

    apps.asSequence()
        .filterNot { candidate -> selected.any { it.stableKey == candidate.stableKey } }
        .take(4 - selected.size)
        .forEach(selected::add)
    return selected.take(4)
}

private fun Modifier.swipeUp(onSwipeUp: () -> Unit): Modifier = pointerInput(onSwipeUp) {
    var upwardDrag = 0f
    detectVerticalDragGestures(
        onDragStart = { upwardDrag = 0f },
        onVerticalDrag = { change, dragAmount ->
            if (dragAmount < 0) upwardDrag += -dragAmount
            change.consume()
        },
        onDragEnd = {
            if (upwardDrag > 72.dp.toPx()) onSwipeUp()
            upwardDrag = 0f
        },
        onDragCancel = { upwardDrag = 0f },
    )
}
