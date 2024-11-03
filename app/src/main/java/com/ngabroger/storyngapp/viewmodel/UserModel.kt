package com.ngabroger.storyngapp.viewmodel



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import androidx.lifecycle.viewModelScope
import com.ngabroger.storyngapp.data.StoryRepository
import com.ngabroger.storyngapp.data.response.RegisterResponse
import com.ngabroger.storyngapp.data.Result
import com.ngabroger.storyngapp.data.local.preference.UserPreferences
import com.ngabroger.storyngapp.data.response.LoginResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UserModel(private val storyRepository: StoryRepository? = null, private val userPreferences: UserPreferences?) : ViewModel(){
    private val _registerResult = MutableLiveData<Result<RegisterResponse>>()
    val registerResult: LiveData<Result<RegisterResponse>> = _registerResult

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult

    private val _token = MutableLiveData<String>()
    val token: LiveData<String> = _token




    fun register(name: String , email: String , password: String){
        _registerResult.value = Result.Loading
        viewModelScope.launch {
            _registerResult.value = storyRepository?.register(name, email, password)
        }
    }

    fun login(email: String, password: String){
        _loginResult.value = Result.Loading
        viewModelScope.launch {
            _loginResult.value = storyRepository?.login(email, password)
        }
    }

    fun saveUserToken(token: String, username: String){
       viewModelScope.launch {
           userPreferences?.saveAuth(token,username)

       }
    }

    fun getToken() {
        viewModelScope.launch {
            _token.value = userPreferences?.getAuth()?.first()
        }
    }








}