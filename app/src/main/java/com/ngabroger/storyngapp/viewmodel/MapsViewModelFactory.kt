package com.ngabroger.storyngapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ngabroger.storyngapp.data.StoryRepository
import com.ngabroger.storyngapp.data.di.Injection
import com.ngabroger.storyngapp.data.local.preference.UserPreferences

class MapsViewModelFactory(private val storyRepository: StoryRepository, private val userPreferences: UserPreferences) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapsViewModel(storyRepository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    companion object{
        @Volatile
        private var instance: MapsViewModelFactory? = null
        fun getInstance(context: Context): MapsViewModelFactory {
            val userPreferences = Injection.provideUserPreferences(context)
            val storyRepository = Injection.provideRepository(context)
            return instance ?: synchronized(this){
                instance ?: MapsViewModelFactory(storyRepository, userPreferences)
            }.also { instance = it }
        }

    }
}