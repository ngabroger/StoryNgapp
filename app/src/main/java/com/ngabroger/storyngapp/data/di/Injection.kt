package com.ngabroger.storyngapp.data.di

import android.content.Context
import com.ngabroger.storyngapp.data.StoryRepository
import com.ngabroger.storyngapp.data.api.ApiConfig
import com.ngabroger.storyngapp.data.local.preference.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideUserPreferences(context: Context): UserPreferences {
        return UserPreferences.getInstance(context)
    }


    fun provideRepository(context: Context): StoryRepository {
        val pref = UserPreferences.getInstance(context)
        val user = runBlocking { pref.getAuth().first() }
        val apiService = ApiConfig.getApiService(user.toString())
        return StoryRepository.getInstance(apiService, pref)
    }
}