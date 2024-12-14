package com.ngabroger.storyngapp.data


import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.ngabroger.storyngapp.data.api.ApiService
import com.ngabroger.storyngapp.data.local.db.StoryDatabase
import com.ngabroger.storyngapp.data.local.entity.ListStoryItem
import com.ngabroger.storyngapp.data.local.preference.UserPreferences
import com.ngabroger.storyngapp.data.paging.StoryPagingSource
import com.ngabroger.storyngapp.data.response.ErrorResponse
import com.ngabroger.storyngapp.data.response.LoginResponse
import com.ngabroger.storyngapp.data.response.RegisterResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class StoryRepository private constructor(
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase,
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
            Result.Error(e.message.toString())
        }
    }



    suspend fun getStoryById(id:String):Result<List<ListStoryItem>>{
        return try{
            val response=  apiService.getStoryById(id)
            if (response.isSuccessful){
                val responseBody = response.body()
                if (responseBody != null && responseBody.error == false){
                    val story = responseBody.story?.let {
                        // Removing extra quotes from description if present
                        it.description = it.description?.trim('"').toString()
                        listOf(it)
                    } ?: emptyList()
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

    suspend fun sendStory(description: String, filePath: String , latLng: LatLng?): Result<ErrorResponse>{
        return try {
            val file = File(filePath)
            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

            val lat = latLng?.latitude?.toFloat()
            val lon = latLng?.longitude?.toFloat()

            val response = apiService.sendStory(description, body, lat, lon)

            if (response.isSuccessful){
                val responseBody = response.body()

                if (responseBody != null && !responseBody.error){
                    Result.Success(responseBody)
                }else{
                    val errorMessage = response.errorBody()?.string() ?: response.message()

                    Result.Error(errorMessage)
                }
            }else{
                val errorResponse = response.errorBody()?.string()?.let {
                    Gson().fromJson(it, ErrorResponse::class.java)
                }
                val errorMessage = errorResponse?.message ?: response.message()
                Result.Error(errorMessage)
            }
        }catch (e:Exception){
            Result.Error(e.message.toString())
        }
    }


    @SuppressLint("SuspiciousIndentation")
    suspend fun fetchStorieswithLocation(location: Int): Result<List<ListStoryItem>>{
        return try{
            val response = apiService.getStoriesWithLocation(location)
            if (response.isSuccessful){
                val responseBody = response.body()
                    if (responseBody != null && responseBody.error == false){
                        val listStory = responseBody.listStory?.filterNotNull() ?: emptyList()
                        Result.Success(listStory)
                    }else{
                        val errorMessage = response.errorBody()?.string() ?: "Error"
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

    fun getAllStoriesWithPager(): LiveData<PagingData<ListStoryItem>> {

        @OptIn(ExperimentalPagingApi::class)
            return Pager(
                config = PagingConfig(
                    pageSize = 5,
                    enablePlaceholders = true
                ),
                remoteMediator = StoryPagingSource(storyDatabase, apiService),
                pagingSourceFactory = {
                    storyDatabase.storyDao().getAllStories()
                }
            ).liveData


        }


    companion object{
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(apiService: ApiService,storyDatabase: StoryDatabase, userPreferences: UserPreferences? = null): StoryRepository =
            instance ?: synchronized(this){
                instance ?: StoryRepository(apiService,storyDatabase,userPreferences )
            }
    }
}