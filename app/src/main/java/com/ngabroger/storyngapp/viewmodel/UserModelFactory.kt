package com.ngabroger.storyngapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ngabroger.storyngapp.data.StoryRepository
import com.ngabroger.storyngapp.data.local.preference.UserPreferences

class UserModelFactory(private val storyRepository: StoryRepository, private val userPreferences: UserPreferences?=null) : ViewModelProvider.Factory {
    constructor(storyRepository: StoryRepository) :this(storyRepository, null)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return storyRepository?.let { UserModel(it, userPreferences) } as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}