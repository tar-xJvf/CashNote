package com.ahs.cashnote.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object ThemePreference {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val THEME_KEY = stringPreferencesKey("theme_mode")

    suspend fun savedTheme(context: Context, theme: String) {
        context.dataStore.edit { settings ->
            settings[THEME_KEY] = theme
        }

    }

    fun getTheme(context: Context): Flow<String> {

        return context.dataStore.data
            .map { it[THEME_KEY] ?: "system" }
    }
}
