package com.ngabroger.storyngapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngabroger.storyngapp.data.StoryRepository
import com.ngabroger.storyngapp.data.Result
import com.ngabroger.storyngapp.data.local.entity.ListStoryItem
import com.ngabroger.storyngapp.data.local.preference.UserPreferences

import kotlinx.coroutines.launch

class MapsViewModel (private val storyRepository: StoryRepository, private val userPreferences: UserPreferences) : ViewModel(){
    private val _storyResult = MutableLiveData<Result<List<ListStoryItem>>>()
    val storyResult: LiveData<Result<List<ListStoryItem>>> = _storyResult

    fun fetchStoriesWithLocation(){
        _storyResult.value=Result.Loading
        viewModelScope.launch{
            _storyResult.value = storyRepository.fetchStorieswithLocation(1)
        }
    }
}