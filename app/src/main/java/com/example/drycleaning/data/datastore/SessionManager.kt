package com.example.drycleaning.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.drycleaning.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

/** Менеджер сессии пользователя на основе DataStore */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_USER_ID = longPreferencesKey("user_id")
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_FULL_NAME = stringPreferencesKey("full_name")
        private val KEY_ROLE = stringPreferencesKey("role")
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_LAST_ACTIVITY = longPreferencesKey("last_activity")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_LOGGED_IN] ?: false
    }

    val userId: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ID] ?: -1L
    }

    val username: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USERNAME] ?: ""
    }

    val fullName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_FULL_NAME] ?: ""
    }

    val role: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_ROLE] ?: "MANAGER"
    }

    val theme: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_THEME] ?: "system"
    }

    suspend fun saveSession(userId: Long, username: String, fullName: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USERNAME] = username
            prefs[KEY_FULL_NAME] = fullName
            prefs[KEY_ROLE] = role
            prefs[KEY_LAST_ACTIVITY] = System.currentTimeMillis()
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = false
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USERNAME)
            prefs.remove(KEY_FULL_NAME)
            prefs.remove(KEY_ROLE)
            prefs.remove(KEY_LAST_ACTIVITY)
        }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme
        }
    }

    suspend fun updateLastActivity() {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_ACTIVITY] = System.currentTimeMillis()
        }
    }

    suspend fun isSessionExpired(): Boolean {
        val prefs = context.dataStore.data.first()
        val isLoggedIn = prefs[KEY_IS_LOGGED_IN] ?: false
        if (!isLoggedIn) return false
        val lastActivity = prefs[KEY_LAST_ACTIVITY] ?: return false
        return System.currentTimeMillis() - lastActivity > Constants.SESSION_TIMEOUT_MS
    }
}
