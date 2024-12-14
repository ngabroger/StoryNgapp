package com.ngabroger.storyngapp.utils

import com.ngabroger.storyngapp.data.local.entity.ListStoryItem

object StoryDummy {

    fun GenerateDummyStory(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryItem(
                photoUrl = "photoUrl $i",
                createdAt = "createdAt $i",
                name = "name $i",
                description = "description $i",
                lon = i.toDouble(),
                id = "id $i",
                lat = i.toDouble()
            )
            items.add(story)
        }
        return items
    }
}