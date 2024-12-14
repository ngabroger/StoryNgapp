package com.ngabroger.storyngapp.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ngabroger.storyngapp.data.local.entity.RemoteKeys

@Dao
interface RemoteKeysDao {

    @Query("SELECT * FROM remote_keys WHERE storyId = :storyId")
    suspend fun keyById(storyId: String): RemoteKeys?

    @Query("DELETE FROM remote_keys")
    suspend fun clearRemoteKeys()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKeys: List<RemoteKeys>)
}