package com.ngabroger.storyngapp.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alperenbabagil.simpleanimationpopuplibrary.SapDialog
import com.ngabroger.storyngapp.adapter.StoryAdapter
import com.ngabroger.storyngapp.data.Result
import com.ngabroger.storyngapp.databinding.ActivityMainBinding
import com.ngabroger.storyngapp.viewmodel.StoryModel
import com.ngabroger.storyngapp.viewmodel.StoryModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: StoryAdapter
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        val recycleView:RecyclerView= binding.rvStory
        recycleView.setHasFixedSize(true)
        recycleView.layoutManager = LinearLayoutManager(this)
        adapter = StoryAdapter(listOf())
        binding.rvStory.adapter=adapter

        setContentView(binding.root)

        binding.floatingActionButton.setOnClickListener {
            Intent(this, AddStoryActivity::class.java).also {
                startActivity(it)
            }
        }



        val factory = StoryModelFactory.getInstance(this)
        val viewModel =ViewModelProvider(this, factory)[StoryModel::class.java]

        viewModel.getStories()



        viewModel.storyResult.observe(this){
            when(it){
                is Result.Error -> {
                    binding.loadingItem.visibility = View.GONE
                    SapDialog(this).apply {
                        titleText = "Not Valid"
                        messageText = it.error
                        isCancellable = true
                    }.build().show()
                }
                Result.Loading -> {
                    binding.loadingItem.visibility = View.VISIBLE
                    binding.loadingItem.progress = 0f
                    binding.loadingItem.addAnimatorUpdateListener {animator ->
                        if (animator.animatedValue as Float >= 0.3){
                            binding.loadingItem.loop(true)
                        }

                    }
                }
                is Result.Success -> {
                    binding.loadingItem.visibility = View.GONE
                    adapter.updateData(it.data)
                }
            }
        }

        viewModel.name.observe(this){
            binding.tvShowUserName.text = "Welcome, $it"
        }



        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            finish()
            startActivity(Intent(this, LandingActivity::class.java))

        }

    }
    override fun onResume() {
        super.onResume()
        val factory = StoryModelFactory.getInstance(this)
        val viewModel =ViewModelProvider(this, factory)[StoryModel::class.java]



        viewModel.getStories()

    }

}