package com.ngabroger.storyngapp.data.local.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ngabroger.storyngapp.data.local.entity.ListStoryItem

@Dao
interface StoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stories: List<ListStoryItem>)

    @Query("SELECT * FROM story ")
    fun getAllStories(): PagingSource<Int, ListStoryItem>

    @Query("DELETE FROM story")
    suspend fun clearStories()
}