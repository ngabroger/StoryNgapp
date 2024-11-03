package com.ngabroger.storyngapp.data.local.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name= "user_preferences")
class UserPreferences  private constructor(private val dataStore: DataStore<Preferences>){


        private val KEY_AUTH = stringPreferencesKey("key_auth")
        private val USER_NAME = stringPreferencesKey("user_name")

    suspend fun saveAuth(token: String, name: String){
        dataStore.edit { preferences ->
            preferences[KEY_AUTH] = token
            preferences[USER_NAME] = name
        }
    }

     fun getAuth(): Flow<String?> {
        return dataStore.data.map {
            it[KEY_AUTH] ?: null

        }
    }

    fun getUserName(): Flow<String?> {
        return dataStore.data.map {
            it[USER_NAME] ?: null

        }
    }



    suspend fun clearAuth(){
        dataStore.edit {
            it.clear()
        }
    }



    companion object{
        @Volatile
        private var INSTANCE : UserPreferences? = null
        fun getInstance(context: Context): UserPreferences{
            return INSTANCE ?: synchronized(this){
                val instance = UserPreferences(context.dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}