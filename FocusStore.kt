package com.example.focusguard

import android.content.Context

object FocusStore {
    private const val PREFS = "focusguard"
    private const val KEY_END = "focus_end"
    private const val KEY_BLOCKLIST = "blocklist"

    fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    fun endAt(context: Context): Long = prefs(context).getLong(KEY_END, 0L)
    fun isActive(context: Context): Boolean = endAt(context) > System.currentTimeMillis()
    fun setEndAt(context: Context, time: Long) { prefs(context).edit().putLong(KEY_END, time).apply() }
    fun clear(context: Context) { setEndAt(context, 0L) }

    fun blocklist(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_BLOCKLIST, emptySet())?.toSet() ?: emptySet()

    fun setBlocklist(context: Context, raw: String) {
        val packages = raw.split(",", "\n", ";")
            .map { it.trim() }
            .filter { it.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+")) }
            .toSet()
        prefs(context).edit().putStringSet(KEY_BLOCKLIST, packages).apply()
    }
}
