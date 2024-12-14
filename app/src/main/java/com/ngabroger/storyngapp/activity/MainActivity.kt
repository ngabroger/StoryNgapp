package com.ngabroger.storyngapp.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.ngabroger.storyngapp.adapter.StoryAdapter
import com.ngabroger.storyngapp.adapter.StoryLoadStateAdapter
import com.ngabroger.storyngapp.databinding.ActivityMainBinding
import com.ngabroger.storyngapp.viewmodel.StoryModel
import com.ngabroger.storyngapp.viewmodel.StoryModelFactory
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: StoryAdapter
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        CREATING A ADAPTER DAWG
        adapter = StoryAdapter()
        val footerAdapter = StoryLoadStateAdapter { adapter.retry() }
        binding.rvStory.layoutManager = LinearLayoutManager(this)
        binding.rvStory.adapter = adapter.withLoadStateFooter(footerAdapter)

        val factory = StoryModelFactory.getInstance(this)
        val viewModel =ViewModelProvider(this, factory)[StoryModel::class.java]

       viewModel.storiesPaging.observe(this) {
           lifecycleScope.launch {
               adapter.submitData(it)
           }
       }
        lifecycleScope.launch {
            viewModel.getUsername()
        }

        adapter.addLoadStateListener { loadState ->
            lifecycleScope.launch{
                binding.loadingItem.isVisible = loadState.source.refresh is LoadState.Loading
                binding.errorView.isVisible = loadState.source.refresh is LoadState.Error
                binding.emptyView.isVisible = loadState.source.refresh is LoadState.NotLoading &&
                        adapter.itemCount == 0
            }



        }

        binding.floatingActionButton.setOnClickListener {
            Intent(this, AddStoryActivity::class.java).also {
                startActivity(it)
            }
        }


        binding.btnLocation.setOnClickListener {
            Intent(this, MapsActivity::class.java).also {
                startActivity(it)
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
       adapter.refresh()


    }

}