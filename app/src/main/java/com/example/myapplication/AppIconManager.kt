package com.example.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.myapplication.ui.theme.AppTheme

/**
 * Switches the launcher icon to match the active app theme using one <activity-alias> per theme.
 *
 * Changing an alias can make some launchers close the app, so the switch is requested
 * while the user is inside the app and applied in MainActivity.onStop().
 */
object AppIconManager {
    private const val PREFS = "coverplay_app_icon"
    private const val KEY_SYNC = "sync_icon_with_theme"
    private const val KEY_SELECTED_ICON = "selected_icon_theme"
    private const val KEY_PENDING = "pending_icon_theme"

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Alias class names — must match the <activity-alias android:name> entries in the manifest. */
    fun aliasFor(context: Context, theme: AppTheme): String = "com.example.myapplication." + when (theme) {
        AppTheme.IRIS_ROYAL -> "IconIris"
        AppTheme.NEON_VIOLET -> "IconViolet"
        AppTheme.CYBER_MAGENTA -> "IconRose"
        AppTheme.SOLAR_EMBER -> "IconEmber"
        AppTheme.HYPER_CYAN -> "IconAurora"
    }

    fun isSyncEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_SYNC, false)

    /** The icon the launcher is really showing: the enabled alias if one exists, otherwise the saved choice. */
    fun getActiveIconTheme(context: Context): AppTheme {
        runCatching {
            val pm = context.packageManager
            AppTheme.entries.firstOrNull {
                pm.getComponentEnabledSetting(ComponentName(context.packageName, aliasFor(context, it))) ==
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            }
        }.getOrNull()?.let { return it }
        val name = prefs(context).getString(KEY_SELECTED_ICON, AppTheme.IRIS_ROYAL.name)
        return AppTheme.entries.firstOrNull { it.name == name } ?: AppTheme.IRIS_ROYAL
    }

    /** The icon queued to be applied the next time the user leaves the app, if any. */
    fun getPendingIconTheme(context: Context): AppTheme? {
        val name = prefs(context).getString(KEY_PENDING, null) ?: return null
        return AppTheme.entries.firstOrNull { it.name == name }
    }

    /** Settings toggle. Turning it on queues the current theme's icon; off stops auto sync. */
    fun setSyncEnabled(context: Context, enabled: Boolean, currentTheme: AppTheme) {
        prefs(context).edit().putBoolean(KEY_SYNC, enabled).apply()
        if (enabled) {
            request(context, currentTheme)
        }
    }

    /** Manually set a specific launcher app icon. */
    fun setManualIcon(context: Context, theme: AppTheme) {
        request(context, theme)
    }

    /** A hand-picked icon always wins: auto-sync is switched off so the next theme change can't override it. */
    fun pickIconManually(context: Context, theme: AppTheme) {
        prefs(context).edit().putBoolean(KEY_SYNC, false).apply()
        request(context, theme)
    }

    /** Call whenever the app theme changes (hooked into setAppTheme). */
    fun onThemeChanged(context: Context, theme: AppTheme) {
        if (isSyncEnabled(context)) {
            setManualIcon(context, theme)
        }
    }

    private fun request(context: Context, theme: AppTheme) {
        val p = prefs(context)
        val e = p.edit().putString(KEY_SELECTED_ICON, theme.name)
        // Already showing this icon: nothing to queue.
        if (getActiveIconTheme(context) == theme) e.remove(KEY_PENDING) else e.putString(KEY_PENDING, theme.name)
        e.apply()
    }

    /** Call from MainActivity.onStop(). Applies a queued icon change, if any. */
    fun applyPending(context: Context) {
        val p = prefs(context)
        val name = p.getString(KEY_PENDING, null) ?: return
        val theme = AppTheme.entries.firstOrNull { it.name == name } ?: run {
            p.edit().remove(KEY_PENDING).apply(); return
        }
        apply(context, theme)
        p.edit().remove(KEY_PENDING).apply()
    }

    private fun apply(context: Context, target: AppTheme) {
        try {
            val pm = context.packageManager
            fun cn(t: AppTheme) = ComponentName(context.packageName, aliasFor(context, t))
            if (pm.getComponentEnabledSetting(cn(target)) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                pm.setComponentEnabledSetting(cn(target), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            }
            AppTheme.entries.filter { it != target }.forEach { t ->
                pm.setComponentEnabledSetting(cn(t), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            }
        } catch (e: Exception) {
            android.util.Log.w("AppIconManager", "Could not switch launcher icon (is the <activity-alias> declared?)", e)
        }
    }
}