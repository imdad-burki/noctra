package com.imdadburki.noctra.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "noctra_settings")

/**
 * User settings (DataStore) — v2, trimmed to what the app still has:
 * clocks + wallpapers only. Alarm/timer/profile/creator keys were removed
 * with those features (v2). Old keys are simply never read again.
 */
class SettingsStore(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme") // system | dark | amoled | light
        val IS_24_HOUR = booleanPreferencesKey("is_24_hour")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val SHOW_DATE = booleanPreferencesKey("show_date")
        /** Catalogue display name of the selected clock (see ClockCatalog). */
        val DEFAULT_CLOCK_NAME = stringPreferencesKey("default_clock_name")
        val DEFAULT_RESOLUTION = stringPreferencesKey("default_resolution") // 720p | 1080p | 1440p
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val theme: Flow<String> = context.dataStore.data.map { it[Keys.THEME] ?: "system" }
    val is24Hour: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_24_HOUR] ?: false }
    val showSeconds: Flow<Boolean> = context.dataStore.data.map { it[Keys.SHOW_SECONDS] ?: true }
    val showDate: Flow<Boolean> = context.dataStore.data.map { it[Keys.SHOW_DATE] ?: true }
    val defaultClockName: Flow<String> =
        context.dataStore.data.map { it[Keys.DEFAULT_CLOCK_NAME] ?: "Solid Standard Gold" }
    val defaultResolution: Flow<String> =
        context.dataStore.data.map { it[Keys.DEFAULT_RESOLUTION] ?: "1080p" }
    val onboardingDone: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    suspend fun setTheme(v: String) = edit { it[Keys.THEME] = v }
    suspend fun set24Hour(v: Boolean) = edit { it[Keys.IS_24_HOUR] = v }
    suspend fun setShowSeconds(v: Boolean) = edit { it[Keys.SHOW_SECONDS] = v }
    suspend fun setShowDate(v: Boolean) = edit { it[Keys.SHOW_DATE] = v }
    suspend fun setDefaultClockName(v: String) = edit { it[Keys.DEFAULT_CLOCK_NAME] = v }
    suspend fun setDefaultResolution(v: String) = edit { it[Keys.DEFAULT_RESOLUTION] = v }
    suspend fun setOnboardingDone(v: Boolean) = edit { it[Keys.ONBOARDING_DONE] = v }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }
}
