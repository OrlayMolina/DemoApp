package com.example.demoapp.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.uiDataStore: DataStore<Preferences> by preferencesDataStore(name = "ui_preferences")

@Singleton
class UiPreferencesDataStore @Inject constructor(
	@ApplicationContext private val context: Context
) {
	private object Keys {
		val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
	}

	val darkModeEnabledFlow: Flow<Boolean> = context.uiDataStore.data.map { prefs ->
		prefs[Keys.DARK_MODE_ENABLED] ?: false
	}

	suspend fun setDarkModeEnabled(enabled: Boolean) {
		context.uiDataStore.edit { prefs ->
			prefs[Keys.DARK_MODE_ENABLED] = enabled
		}
	}
}

