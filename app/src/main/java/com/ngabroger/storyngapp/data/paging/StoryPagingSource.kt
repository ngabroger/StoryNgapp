package com.ngabroger.storyngapp.data.paging

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType

import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction

import com.ngabroger.storyngapp.data.api.ApiService
import com.ngabroger.storyngapp.data.local.db.StoryDatabase
import com.ngabroger.storyngapp.data.local.entity.ListStoryItem
import com.ngabroger.storyngapp.data.local.entity.RemoteKeys


@OptIn(ExperimentalPagingApi::class)
class StoryPagingSource(private val storyDatabase: StoryDatabase? = null , private val apiService: ApiService) : RemoteMediator<Int, ListStoryItem>() {



    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, ListStoryItem>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeysClosestCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }

            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyLastItem(state)
                val nextPage = remoteKeys?.nextKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null
                )
                nextPage
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeysFirstItem(state)
                val prevPage = remoteKeys?.prevKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null
                )
                prevPage
            }
        }

        return try {
            val responseBody = apiService.getStories(page, state.config.pageSize)
            val stories = responseBody.listStory.orEmpty()
            val endOfPagination = stories.isEmpty()

            Log.d(TAG, "API response successful: ${stories.size} stories retrieved")

            if (storyDatabase != null) {

                storyDatabase.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        storyDatabase.storyDao().clearStories()
                        storyDatabase.remoteKeysDao().clearRemoteKeys()
                    }

                    val prevKey = if (page == INITIAL_PAGE_INDEX) null else page - 1
                    val nextKey = if (endOfPagination) null else page + 1
                    val keys = stories.map {
                        RemoteKeys(storyId = it?.id ?: "Test", prevKey = prevKey, nextKey = nextKey)
                    }

                    storyDatabase.remoteKeysDao().insertAll(keys)
                    storyDatabase.storyDao().insertAll(stories)
                }
            }

            MediatorResult.Success(endOfPaginationReached = endOfPagination)

        } catch (e: Exception) {
            Log.e(TAG, "API response error: $e")
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyLastItem(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return if (storyDatabase != null) {
            state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let {
                storyDatabase.remoteKeysDao().keyById(it.id)
            }
        } else null
    }

    private suspend fun getRemoteKeysFirstItem(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return if (storyDatabase != null) {
            state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let {
                storyDatabase.remoteKeysDao().keyById(it.id)
            }
        } else null
    }

    private suspend fun getRemoteKeysClosestCurrentPosition(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return if (storyDatabase != null) {
            state.anchorPosition?.let { position ->
                state.closestItemToPosition(position)?.id?.let {
                    storyDatabase.remoteKeysDao().keyById(it)
                }
            }
        } else null
    }

    companion object {
        const val INITIAL_PAGE_INDEX = 1
        const val TAG = "StoryPagingSource"
    }
}