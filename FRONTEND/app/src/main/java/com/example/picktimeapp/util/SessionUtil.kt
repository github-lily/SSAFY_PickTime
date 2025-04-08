package com.example.picktimeapp.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")
private val SESSION_ID_KEY = stringPreferencesKey("session_id")

suspend fun saveSessionId(context: Context, sessionId: String) {
    context.dataStore.edit { prefs ->
        prefs[SESSION_ID_KEY] = sessionId
    }
}

val Context.sessionIdFlow: Flow<String?>
    get() = dataStore.data.map { prefs -> prefs[SESSION_ID_KEY] }

suspend fun getSessionId(context: Context): String? {
    return context.dataStore.data.map { prefs -> prefs[SESSION_ID_KEY] }.firstOrNull()
}