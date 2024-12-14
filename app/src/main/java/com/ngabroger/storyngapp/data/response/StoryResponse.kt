package com.ngabroger.storyngapp.data.response

import com.google.gson.annotations.SerializedName
import com.ngabroger.storyngapp.data.local.entity.ListStoryItem

data class StoryResponse(

	@field:SerializedName("listStory")
	val listStory: List<ListStoryItem> = emptyList(),

	@field:SerializedName("story")
	val story: ListStoryItem? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

