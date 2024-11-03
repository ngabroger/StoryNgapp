package com.ngabroger.storyngapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngabroger.storyngapp.data.Result
import com.ngabroger.storyngapp.data.StoryRepository
import com.ngabroger.storyngapp.data.local.preference.UserPreferences
import com.ngabroger.storyngapp.data.response.ErrorResponse
import com.ngabroger.storyngapp.data.response.ListStoryItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StoryModel(private val storyRepository: StoryRepository, private val userPreferences: UserPreferences) : ViewModel(){
    private val _storyResult = MutableLiveData<Result<List<ListStoryItem>>>()
    val storyResult: LiveData<Result<List<ListStoryItem>>> = _storyResult

    private val _postStoryResult = MutableLiveData<Result<ErrorResponse>>()
    val postStoryResult: LiveData<Result<ErrorResponse>> = _postStoryResult

    private val _name = MutableLiveData<String?>()
    val name: MutableLiveData<String?> = _name

    init {

        getUsername()
    }
     fun getStories(){
        _storyResult.value = Result.Loading
        viewModelScope.launch {
            _storyResult.value = storyRepository.getStories()
        }
    }

    fun logout(){
        viewModelScope.launch {
            userPreferences.clearAuth()
        }
    }

    private fun getUsername() {
        viewModelScope.launch {
          val name = userPreferences.getUserName().first()
            _name.value = name
        }
    }

    fun getStoryById(id:String){
        _storyResult.value = Result.Loading
        viewModelScope.launch {
            _storyResult.value = storyRepository.getStoryById(id)
        }
    }

    fun postStory (description: String, photo: String){
        _postStoryResult.value = Result.Loading
        viewModelScope.launch {
            _postStoryResult.value = storyRepository.sendStory(description, photo)
        }
    }

}