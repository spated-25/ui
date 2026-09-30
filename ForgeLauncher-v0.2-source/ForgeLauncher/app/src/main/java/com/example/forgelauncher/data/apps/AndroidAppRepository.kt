package com.example.forgelauncher.data.apps

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.createBitmap
import com.example.forgelauncher.domain.model.LaunchableApp
import java.text.Collator
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AndroidAppRepository(context: Context) : AppRepository {
    private val appContext = context.applicationContext
    private val launcherApps = appContext.getSystemService(LauncherApps::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val started = AtomicBoolean(false)
    private var refreshJob: Job? = null

    private val _apps = MutableStateFlow<List<LaunchableApp>>(emptyList())
    override val apps: StateFlow<List<LaunchableApp>> = _apps.asStateFlow()

    private val _isRefreshing = MutableStateFlow(true)
    override val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val callback = object : LauncherApps.Callback() {
        override fun onPackageAdded(packageName: String, user: UserHandle) = refresh()
        override fun onPackageRemoved(packageName: String, user: UserHandle) = refresh()
        override fun onPackageChanged(packageName: String, user: UserHandle) = refresh()
        override fun onPackagesAvailable(
            packageNames: Array<out String>,
            user: UserHandle,
            replacing: Boolean,
        ) = refresh()

        override fun onPackagesUnavailable(
            packageNames: Array<out String>,
            user: UserHandle,
            replacing: Boolean,
        ) = refresh()
    }

    override fun start() {
        if (started.compareAndSet(false, true)) {
            launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        }
        refresh()
    }

    override fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = scope.launch {
            _isRefreshing.value = true
            try {
                _apps.value = loadApps()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    override fun launch(app: LaunchableApp): Result<Unit> = runCatching {
        launcherApps.startMainActivity(app.componentName, app.user, null, null)
    }

    override fun close() {
        if (started.compareAndSet(true, false)) {
            runCatching { launcherApps.unregisterCallback(callback) }
        }
        scope.cancel()
    }

    private fun loadApps(): List<LaunchableApp> {
        val profiles = runCatching { launcherApps.profiles }
            .getOrDefault(listOf(Process.myUserHandle()))
            .ifEmpty { listOf(Process.myUserHandle()) }

        val iconSize = (56f * appContext.resources.displayMetrics.density)
            .roundToInt()
            .coerceAtLeast(1)
        val densityDpi = appContext.resources.displayMetrics.densityDpi

        val loaded = profiles.flatMap { user ->
            runCatching { launcherApps.getActivityList(null, user) }
                .getOrDefault(emptyList())
                .mapNotNull { info ->
                    val packageName = info.componentName.packageName
                    if (packageName == appContext.packageName) return@mapNotNull null

                    val label = info.label.toString().trim().ifEmpty {
                        packageName.substringAfterLast('.')
                    }
                    val icon = runCatching {
                        info.getBadgedIcon(densityDpi).toBitmap(
                            width = iconSize,
                            height = iconSize,
                            config = Bitmap.Config.ARGB_8888,
                        )
                    }.getOrElse { fallbackIcon(label, iconSize) }

                    LaunchableApp(
                        label = label,
                        packageName = packageName,
                        componentName = info.componentName,
                        user = user,
                        icon = icon,
                    )
                }
        }

        val collator = Collator.getInstance(Locale.getDefault())
        return loaded
            .distinctBy(LaunchableApp::stableKey)
            .sortedWith(compareBy(collator) { it.label })
    }

    private fun fallbackIcon(label: String, size: Int): Bitmap {
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        val hue = (label.hashCode().toUInt().toLong() % 360).toFloat()
        val background = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.HSVToColor(floatArrayOf(hue, 0.55f, 0.78f))
        }
        canvas.drawRoundRect(
            RectF(0f, 0f, size.toFloat(), size.toFloat()),
            size * 0.22f,
            size * 0.22f,
            background,
        )

        val glyph = label.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = size * 0.48f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val baseline = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(glyph, size / 2f, baseline, textPaint)
        return bitmap
    }
}
