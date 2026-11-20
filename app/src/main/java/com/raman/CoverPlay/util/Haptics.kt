package com.raman.CoverPlay.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalView
import kotlin.math.roundToInt

object CoverPlayHaptics {
    const val PREFS_NAME = "coverplay_settings_v3"
    const val KEY_STRENGTH = "haptic_strength"
    const val DEFAULT_STRENGTH = 40

    private const val TAP_DURATION_MS = 12L
    private const val PRESS_DURATION_MS = 22L

    @Volatile private var cachedStrength: Int = -1

    fun strength(context: Context): Int {
        if (cachedStrength < 0) {
            cachedStrength = context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_STRENGTH, DEFAULT_STRENGTH)
                .coerceIn(0, 100)
        }
        return cachedStrength
    }

    fun setStrength(context: Context, value: Int) {
        val clamped = value.coerceIn(0, 100)
        cachedStrength = clamped
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_STRENGTH, clamped).apply()
    }

    fun label(strength: Int): String = when {
        strength <= 0 -> "Off"
        strength < 34 -> "Light"
        strength < 67 -> "Medium"
        else -> "Strong"
    }

    fun perform(view: View, firm: Boolean = false) {
        val context = view.context
        val level = strength(context)
        if (level <= 0) return
        if (!systemHapticsEnabled(context)) return

        val vibrator = vibratorOf(context)
        if (vibrator != null && vibrator.hasVibrator() && supportsAmplitude(vibrator)) {
            val amplitude = (18 + level * 2.2f).roundToInt().coerceIn(1, 255)
            val duration = if (firm) PRESS_DURATION_MS else TAP_DURATION_MS
            vibrator.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            view.performHapticFeedback(
                if (firm) HapticFeedbackConstants.LONG_PRESS else HapticFeedbackConstants.VIRTUAL_KEY
            )
        }
    }

    private fun supportsAmplitude(vibrator: Vibrator): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator.hasAmplitudeControl()

    private fun systemHapticsEnabled(context: Context): Boolean =
        Settings.System.getInt(context.contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) != 0

    private fun vibratorOf(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
}

@Composable
fun rememberCoverPlayHaptic(type: HapticFeedbackType = HapticFeedbackType.TextHandleMove): () -> Unit {
    val view = LocalView.current
    return remember(view, type) {
        val firm = type == HapticFeedbackType.LongPress
        ({ CoverPlayHaptics.perform(view, firm) })
    }
}

@Composable
fun rememberCoverPlayHapticFeedback(): HapticFeedback {
    val view = LocalView.current
    return remember(view) {
        object : HapticFeedback {
            override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                CoverPlayHaptics.perform(view, firm = hapticFeedbackType == HapticFeedbackType.LongPress)
            }
        }
    }
}
