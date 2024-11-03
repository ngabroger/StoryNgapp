package com.ngabroger.storyngapp.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.ngabroger.storyngapp.activity.DetailActivity
import com.ngabroger.storyngapp.data.response.ListStoryItem
import com.ngabroger.storyngapp.databinding.ItemStoryBinding

class StoryAdapter (private var storyList: List<ListStoryItem>) : RecyclerView.Adapter<StoryAdapter.ViewHolder>() {
    class ViewHolder (val binding: ItemStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(story:ListStoryItem){
           binding.tvUserStory.text = story.name

            Glide.with(itemView.context)
                .load(story.photoUrl)

                .into(binding.ivStory)


        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newStory: List<ListStoryItem>) {
        storyList = newStory
        notifyDataSetChanged()
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = storyList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(storyList[position])
        holder.itemView.setOnClickListener{
            val idList = storyList[position].id
            val intent = Intent(holder.itemView.context, DetailActivity::class.java).apply {
                putExtra("STORY_ID", idList)
            }
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                holder.itemView.context as Activity,
                Pair(holder.binding.ivStory,"storyImage")
            )
            holder.itemView.context.startActivity(intent,options.toBundle())
        }
    }


    class StoryDiffCallback(
        private val oldList: List<ListStoryItem>,
        private val storyList: List<ListStoryItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = storyList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == storyList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == storyList[newItemPosition]
        }
    }
}