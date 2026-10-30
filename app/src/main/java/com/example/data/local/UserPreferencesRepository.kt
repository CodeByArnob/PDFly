package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AppThemeMode
import com.example.domain.model.CompressionPreset
import com.example.domain.model.PaperSizePreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.pdflyDataStore: DataStore<Preferences> by preferencesDataStore(name = "pdfly_settings")

data class UserPreferencesState(
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val hasCompletedOnboarding: Boolean = true,
    val defaultCompression: CompressionPreset = CompressionPreset.MEDIUM,
    val defaultPaperSize: PaperSizePreset = PaperSizePreset.A4,
    val autoCleanTempFiles: Boolean = true,
    val reducedMotionEnabled: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val DEFAULT_COMPRESSION = stringPreferencesKey("default_compression")
        val DEFAULT_PAPER_SIZE = stringPreferencesKey("default_paper_size")
        val AUTO_CLEAN_TEMP = booleanPreferencesKey("auto_clean_temp")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
    }

    val preferencesFlow: Flow<UserPreferencesState> = context.pdflyDataStore.data.map { prefs ->
        val theme = prefs[Keys.THEME_MODE]?.let {
            runCatching { AppThemeMode.valueOf(it) }.getOrNull()
        } ?: AppThemeMode.DARK

        val compression = prefs[Keys.DEFAULT_COMPRESSION]?.let {
            runCatching { CompressionPreset.valueOf(it) }.getOrNull()
        } ?: CompressionPreset.MEDIUM

        val paperSize = prefs[Keys.DEFAULT_PAPER_SIZE]?.let {
            runCatching { PaperSizePreset.valueOf(it) }.getOrNull()
        } ?: PaperSizePreset.A4

        UserPreferencesState(
            themeMode = theme,
            hasCompletedOnboarding = prefs[Keys.ONBOARDING_DONE] ?: true,
            defaultCompression = compression,
            defaultPaperSize = paperSize,
            autoCleanTempFiles = prefs[Keys.AUTO_CLEAN_TEMP] ?: true,
            reducedMotionEnabled = prefs[Keys.REDUCED_MOTION] ?: false
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.pdflyDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.pdflyDataStore.edit { it[Keys.ONBOARDING_DONE] = completed }
    }

    suspend fun setDefaultCompression(preset: CompressionPreset) {
        context.pdflyDataStore.edit { it[Keys.DEFAULT_COMPRESSION] = preset.name }
    }

    suspend fun setDefaultPaperSize(preset: PaperSizePreset) {
        context.pdflyDataStore.edit { it[Keys.DEFAULT_PAPER_SIZE] = preset.name }
    }

    suspend fun setAutoCleanTempFiles(enabled: Boolean) {
        context.pdflyDataStore.edit { it[Keys.AUTO_CLEAN_TEMP] = enabled }
    }

    suspend fun setReducedMotionEnabled(enabled: Boolean) {
        context.pdflyDataStore.edit { it[Keys.REDUCED_MOTION] = enabled }
    }
}
