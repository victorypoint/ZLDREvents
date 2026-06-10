package com.victorypoint.zldrevents.data

import android.content.Context

private const val PREFS_FILE = "app_prefs"
private const val KEY_BATTERY_PROMPT_SHOWN = "battery_prompt_shown"

class AppPrefsStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    var batteryPromptShown: Boolean
        get() = prefs.getBoolean(KEY_BATTERY_PROMPT_SHOWN, false)
        set(value) = prefs.edit().putBoolean(KEY_BATTERY_PROMPT_SHOWN, value).apply()
}
