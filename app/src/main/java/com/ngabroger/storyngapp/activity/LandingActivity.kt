package com.ngabroger.storyngapp.activity


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ngabroger.storyngapp.data.StoryRepository
import com.ngabroger.storyngapp.data.api.ApiConfig
import com.ngabroger.storyngapp.data.local.preference.UserPreferences
import com.ngabroger.storyngapp.databinding.ActivityLandingBinding
import com.ngabroger.storyngapp.viewmodel.UserModel
import com.ngabroger.storyngapp.viewmodel.UserModelFactory


class LandingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLandingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLandingBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val pref = UserPreferences.getInstance(this)
        val repository = StoryRepository.getInstance(ApiConfig.getApiServiceWithoutToken())
       val factory = UserModelFactory(repository, pref)
        val viewModel = ViewModelProvider(this, factory)[UserModel::class.java]
        viewModel.getToken()
        viewModel.token.observe(this){
            if (it != null){
                Intent(this, MainActivity::class.java).also {
                    startActivity(it)
                    finish()
                }

            }else{
                Log.e("ERROR DATA", "Token is null")
            }
        }



        binding.registerButton.setOnClickListener {
            Intent(this, RegisterActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }

        binding.signinButton.setOnClickListener {
            Intent(this, LoginActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }

    }


}