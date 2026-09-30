package com.example.forgelauncher.domain.model

import android.content.ComponentName
import android.graphics.Bitmap
import android.os.UserHandle

data class LaunchableApp(
    val label: String,
    val packageName: String,
    val componentName: ComponentName,
    val user: UserHandle,
    val icon: Bitmap,
) {
    val stableKey: String = "${user.hashCode()}:${componentName.flattenToShortString()}"
}
