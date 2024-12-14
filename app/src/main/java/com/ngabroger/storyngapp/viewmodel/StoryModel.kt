package com.ngabroger.storyngapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.google.android.gms.maps.model.LatLng
import com.ngabroger.storyngapp.data.Result
import com.ngabroger.storyngapp.data.StoryRepository
import com.ngabroger.storyngapp.data.local.entity.ListStoryItem
import com.ngabroger.storyngapp.data.local.preference.UserPreferences
import com.ngabroger.storyngapp.data.response.ErrorResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StoryModel(val storyRepository: StoryRepository, val userPreferences: UserPreferences) : ViewModel(){

    val storiesPaging: LiveData<PagingData<ListStoryItem>> = storyRepository.getAllStoriesWithPager()


    private val _storyResult = MutableLiveData<Result<List<ListStoryItem>>>()
    val storyResult: LiveData<Result<List<ListStoryItem>>> = _storyResult

    private val _postStoryResult = MutableLiveData<Result<ErrorResponse>>()
    val postStoryResult: LiveData<Result<ErrorResponse>> = _postStoryResult

    private val _name = MutableLiveData<String?>()
    val name: MutableLiveData<String?> = _name





    fun logout(){
        viewModelScope.launch {
            userPreferences.clearAuth()
        }
    }

     fun getUsername() {
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

    fun postStory (description: String, photo: String , latLng: LatLng?){
        _postStoryResult.value = Result.Loading
        viewModelScope.launch {
            _postStoryResult.value = storyRepository.sendStory(description, photo, latLng)
        }
    }

}