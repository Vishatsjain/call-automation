package com.vibecoder.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.vibecoder.app.models.NotificationTime
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATION_TIMES = stringPreferencesKey("notification_times")
        val FIRST_RUN = booleanPreferencesKey("first_run")
    }
    
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DARK_MODE] ?: false
    }
    
    val notificationTimes: Flow<List<NotificationTime>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[PreferencesKeys.NOTIFICATION_TIMES]
        if (jsonString != null) {
            try {
                Json.decodeFromString<List<NotificationTime>>(jsonString)
            } catch (e: Exception) {
                // Return default notification time if parsing fails
                listOf(NotificationTime(hour = 9, minute = 0))
            }
        } else {
            // Default notification time: 9:00 AM
            listOf(NotificationTime(hour = 9, minute = 0))
        }
    }
    
    val isFirstRun: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FIRST_RUN] ?: true
    }
    
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = enabled
        }
    }
    
    suspend fun setNotificationTimes(times: List<NotificationTime>) {
        context.dataStore.edit { preferences ->
            val jsonString = Json.encodeToString(times)
            preferences[PreferencesKeys.NOTIFICATION_TIMES] = jsonString
        }
    }
    
    suspend fun addNotificationTime(time: NotificationTime) {
        val currentTimes = getCurrentNotificationTimes()
        val updatedTimes = currentTimes + time
        setNotificationTimes(updatedTimes)
    }
    
    suspend fun removeNotificationTime(timeId: String) {
        val currentTimes = getCurrentNotificationTimes()
        val updatedTimes = currentTimes.filter { it.id != timeId }
        setNotificationTimes(updatedTimes)
    }
    
    suspend fun updateNotificationTime(updatedTime: NotificationTime) {
        val currentTimes = getCurrentNotificationTimes()
        val updatedTimes = currentTimes.map { time ->
            if (time.id == updatedTime.id) updatedTime else time
        }
        setNotificationTimes(updatedTimes)
    }
    
    suspend fun setFirstRunComplete() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_RUN] = false
        }
    }
    
    private suspend fun getCurrentNotificationTimes(): List<NotificationTime> {
        return try {
            val preferences = context.dataStore.data.map { it }.first()
            val jsonString = preferences[PreferencesKeys.NOTIFICATION_TIMES]
            if (jsonString != null) {
                Json.decodeFromString<List<NotificationTime>>(jsonString)
            } else {
                listOf(NotificationTime(hour = 9, minute = 0))
            }
        } catch (e: Exception) {
            listOf(NotificationTime(hour = 9, minute = 0))
        }
    }
}

// Extension to get first element from Flow synchronously
private suspend fun <T> Flow<T>.first(): T {
    var result: T? = null
    collect { value ->
        result = value
        return@collect
    }
    return result!!
}