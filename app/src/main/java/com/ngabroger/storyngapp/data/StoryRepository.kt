package com.ngabroger.storyngapp.data


import android.util.Log
import com.google.gson.Gson
import com.ngabroger.storyngapp.data.api.ApiService
import com.ngabroger.storyngapp.data.local.preference.UserPreferences
import com.ngabroger.storyngapp.data.response.ErrorResponse
import com.ngabroger.storyngapp.data.response.ListStoryItem
import com.ngabroger.storyngapp.data.response.LoginResponse
import com.ngabroger.storyngapp.data.response.RegisterResponse
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class StoryRepository private constructor(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences? = null
) {
    suspend fun register(name: String , email: String , password: String ): Result<RegisterResponse> {
        return try{
            val response = apiService.register(name, email, password)
            if(response.isSuccessful){
                val responseBody = response.body()
                if (responseBody != null && responseBody.error == false){
                    Result.Success(responseBody)
                }else{
                    val errorMessage = response.errorBody()?.string() ?: response.message()

                    responseBody?.message?.let { Log.e("Error", it) }
                    Result.Error(errorMessage)
                }
            }else{

                val errorResponse = response.errorBody()?.string()?.let {
                    Gson().fromJson(it, ErrorResponse::class.java)
                }
                val errorMessage = errorResponse?.message ?: response.message()
                Result.Error(errorMessage)
            }

        }catch (e: Exception){
            Log.e("Error", e.message.toString())
            Result.Error(e.message.toString() )
        }
    }

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(email, password)
            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null && responseBody.error == false) {
                    Result.Success(responseBody)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: response.message()
                    Result.Error(errorMessage)
                }
            } else {
                val errorResponse = response.errorBody()?.string()?.let {
                    Gson().fromJson(it, ErrorResponse::class.java)
                }
                val errorMessage = errorResponse?.message ?: response.message()
                Result.Error(errorMessage)
            }
        } catch (e: Exception) {
            Log.e("Error", e.message.toString())
            Result.Error(e.message.toString())
        }
    }

    suspend fun getStories():Result<List<ListStoryItem>>{
        return try {
            val response = apiService.getStories()
            if (response.isSuccessful){
                val responseBody = response.body()
              if (responseBody != null && responseBody.error == false){
                  val listStory = responseBody.listStory?.filterNotNull() ?: emptyList()
                    Result.Success(listStory)
              }else{
                  val errorMessage = response.errorBody()?.string()?: "Error"
                  Result.Error(errorMessage)
              }
            }else{
                val errorResponse = Gson().fromJson(response.errorBody()?.string(), ErrorResponse::class.java)
                Result.Error(errorResponse.message)
            }

        }catch (e:Exception){
            Result.Error(e.message.toString())
        }
    }

    suspend fun getStoryById(id:String):Result<List<ListStoryItem>>{
        return try{
            val response=  apiService.getStoryById(id)
            if (response.isSuccessful){
                val responseBody = response.body()
                if (responseBody != null && responseBody.error == false){
                    val story =  responseBody.story?.let { listOf(it) } ?: emptyList()
                    Result.Success(story)
                }else{
                    val errorMessage = response.errorBody()?.string()?: "error"
                    Result.Error(errorMessage)
                }
            }else{
                val errorResponse = Gson().fromJson(response.errorBody()?.string(), ErrorResponse::class.java)
                Result.Error(errorResponse.message)
            }
        }catch (e:Exception){
            Result.Error(e.message.toString())
        }
    }

    suspend fun sendStory(description: String, filePath: String): Result<ErrorResponse>{
        return try {
            val file = File(filePath)
            Log.d("StoryRepository", "sendStory: $file")
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)


            val response = apiService.sendStory(description, body)

            if (response.isSuccessful){
                val responseBody = response.body()
                if (responseBody != null && !responseBody.error){
                    Log.d("StoryRepository", "sendStory: ${responseBody.message}")
                    Result.Success(responseBody)
                }else{
                    val errorMessage = response.errorBody()?.string() ?: response.message()
                    Log.e("StoryRepository", "sendStory: $errorMessage")
                    Result.Error(errorMessage)
                }
            }else{
                val errorResponse = response.errorBody()?.string()?.let {
                    Gson().fromJson(it, ErrorResponse::class.java)
                }
                val errorMessage = errorResponse?.message ?: response.message()
                Log.e("StoryRepository", "sendStory: $errorMessage")
                Result.Error(errorMessage)
            }
        }catch (e:Exception){
            Log.e("Error", e.message.toString())
            Result.Error(e.message.toString())
        }
    }





    companion object{
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(apiService: ApiService, userPreferences: UserPreferences? = null): StoryRepository =
            instance ?: synchronized(this){
                instance ?: StoryRepository(apiService,userPreferences)
            }
    }
}