package com.ngabroger.storyngapp.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.alperenbabagil.simpleanimationpopuplibrary.SapDialog
import com.bumptech.glide.Glide
import com.ngabroger.storyngapp.R
import com.ngabroger.storyngapp.data.Result
import com.ngabroger.storyngapp.data.response.ListStoryItem
import com.ngabroger.storyngapp.databinding.ActivityDetailBinding
import com.ngabroger.storyngapp.viewmodel.StoryModel
import com.ngabroger.storyngapp.viewmodel.StoryModelFactory

class DetailActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityDetailBinding
    private lateinit var viewModel:StoryModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            blurView.setupWith(binding.root)
            blurView.setBlurEnabled(true)
            blurView.setBlurRadius(25f)
            blurView.setBlurAutoUpdate(true)
        }
        binding.vwBlurLoading.apply {
            setupWith(binding.root)
            setBlurRadius(25f)
            setBlurAutoUpdate(true)
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }


        val factory= StoryModelFactory.getInstance(this)
        viewModel = ViewModelProvider(this,factory)[StoryModel::class.java]
        val id = intent.getStringExtra("STORY_ID")
        if (id != null) {
            viewModel.getStoryById(id)

            viewModel.storyResult.observe(this){
                if (it != null){
                    when(it){
                        is Result.Error -> {
                            binding.vwBlurLoading.visibility = View.GONE
                            SapDialog(this).apply {
                                titleText = "Error"
                                messageText = it.error
                                isCancellable = true
                            }.build().show()
                        }
                        Result.Loading -> {
                            binding.vwBlurLoading.visibility = View.VISIBLE
                        }
                        is Result.Success -> {
                            binding.vwBlurLoading.visibility = View.GONE
                            val storyItem = it.data.firstOrNull()
                            Log.d("DetailActivity", "Success: $storyItem")
                            if (storyItem != null) {
                                bindStoryDetail(storyItem)
                            }

                        }
                    }

                }else{
                    SapDialog(this).apply {
                        titleText = "Error"
                        messageText = "Unknown error"
                        isCancellable = true
                    }.build().show()
                }
            }
        }
    }

    private fun bindStoryDetail(storyItem: ListStoryItem) {
        binding.tvUsernameDetailStory.text= storyItem.name
        binding.tvDescriptionStory.text= storyItem.description

        Glide.with(this)
            .load(storyItem.photoUrl)
            .into(binding.ivDetailStory)


        Glide.with(this)
            .load(storyItem.photoUrl)
            .into(binding.ivBlurStory)
    }
}