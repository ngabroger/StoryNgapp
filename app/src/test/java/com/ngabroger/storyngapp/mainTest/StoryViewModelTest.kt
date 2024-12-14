package com.ngabroger.storyngapp.mainTest


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.ngabroger.storyngapp.MainDispatcherRule
import com.ngabroger.storyngapp.adapter.StoryAdapter
import com.ngabroger.storyngapp.data.StoryRepository

import com.ngabroger.storyngapp.data.local.entity.ListStoryItem
import com.ngabroger.storyngapp.data.local.preference.UserPreferences

import com.ngabroger.storyngapp.getOrAwaitValue
import com.ngabroger.storyngapp.utils.StoryDummy
import com.ngabroger.storyngapp.viewmodel.StoryModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest


import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito

import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StoryModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Mock
    private lateinit var userPreferences: UserPreferences

    private lateinit var storyModel: StoryModel


    @Test
    fun `when Get Story Should Not Null and Return Data`() = runTest {
        val dummyStory = StoryDummy.GenerateDummyStory()
        val data: PagingData<ListStoryItem> = PagingData.from(dummyStory)
        val expectedStory = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStory.value = data

        Mockito.`when`(storyRepository.getAllStoriesWithPager()).thenReturn(flowOf(data).asLiveData())

        storyModel = StoryModel(storyRepository, userPreferences)

        val actualStory: PagingData<ListStoryItem> = storyModel.storiesPaging.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)

        assertNotNull(differ.snapshot().items)
        assertEquals(dummyStory.size, differ.snapshot().items.size)
        assertEquals(dummyStory[0], differ.snapshot().items[0])
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStoryItem> = PagingData.from(emptyList())
        val expectedData = MutableLiveData<PagingData<ListStoryItem>>()
        expectedData.value = data
        Mockito.`when`(storyRepository.getAllStoriesWithPager()).thenReturn(expectedData)

        storyModel = StoryModel(storyRepository, userPreferences)

        val actualStory: PagingData<ListStoryItem> = storyModel.storiesPaging.getOrAwaitValue()


        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)

        assertEquals(0, differ.snapshot().size)
    }
}

private val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}
