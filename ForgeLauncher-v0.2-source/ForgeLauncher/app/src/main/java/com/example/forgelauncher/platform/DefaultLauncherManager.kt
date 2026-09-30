package com.example.forgelauncher.platform

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

class DefaultLauncherManager(context: Context) {
    private val appContext = context.applicationContext

    fun isDefaultLauncher(): Boolean {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolvedPackage = appContext.packageManager
            .resolveActivity(homeIntent, 0)
            ?.activityInfo
            ?.packageName
        return resolvedPackage == appContext.packageName
    }

    fun requestIntent(): Intent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = appContext.getSystemService(RoleManager::class.java)
            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            }
            if (roleManager.isRoleHeld(RoleManager.ROLE_HOME)) return null
        }
        return Intent(Settings.ACTION_HOME_SETTINGS)
    }
}
