package com.ngabroger.storyngapp.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ngabroger.storyngapp.data.local.entity.ListStoryItem
import com.ngabroger.storyngapp.data.local.entity.RemoteKeys



@Database(entities = [ListStoryItem::class, RemoteKeys::class], version = 1, exportSchema = false)

abstract class StoryDatabase : RoomDatabase(){
    abstract fun storyDao(): StoryDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        const val DATABASE_NAME = "story.db"

        @Volatile
        private var INSTANCE: StoryDatabase? = null
        fun getInstance(context: Context): StoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StoryDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}