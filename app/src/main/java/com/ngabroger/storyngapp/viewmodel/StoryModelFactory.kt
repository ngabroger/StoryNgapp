package com.ngabroger.storyngapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ngabroger.storyngapp.data.StoryRepository
import com.ngabroger.storyngapp.data.di.Injection
import com.ngabroger.storyngapp.data.local.preference.UserPreferences

class StoryModelFactory(private val storyRepository: StoryRepository, private val userPreferences: UserPreferences):ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoryModel(storyRepository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    companion object{
        @Volatile
        private var instance: StoryModelFactory? = null
        fun getInstance(context: Context):StoryModelFactory {
            val userPreferences = Injection.provideUserPreferences(context)
            val storyRepository = Injection.provideRepository(context)
            return instance ?: synchronized(this){
                instance ?: StoryModelFactory(storyRepository, userPreferences)
            }.also { instance = it }
        }

    }

}