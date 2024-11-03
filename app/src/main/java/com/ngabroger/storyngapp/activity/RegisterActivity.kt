package com.ngabroger.storyngapp.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alperenbabagil.simpleanimationpopuplibrary.SapDialog
import com.ngabroger.storyngapp.data.Result
import com.ngabroger.storyngapp.data.StoryRepository
import com.ngabroger.storyngapp.data.api.ApiConfig
import com.ngabroger.storyngapp.databinding.ActivityRegisterBinding
import com.ngabroger.storyngapp.viewmodel.UserModel
import com.ngabroger.storyngapp.viewmodel.UserModelFactory

class RegisterActivity : AppCompatActivity() {
    private lateinit var  binding : ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        val repository = StoryRepository.getInstance(ApiConfig.getApiServiceWithoutToken())

        val factory = UserModelFactory(repository)
        val viewmodel = ViewModelProvider(this, factory)[UserModel::class.java]

        viewmodel.registerResult.observe(this) { result ->
            when (result) {
                is Result.Error -> {
                    binding.loadingItem.visibility = View.GONE
                    SapDialog(this).apply {
                        titleText = "Not Valid"
                        messageText = result.error
                        isCancellable = true
                    }.build().show()

                }

                Result.Loading -> {
                    binding.loadingItem.visibility = View.VISIBLE
                    binding.loadingItem.progress = 0f
                    binding.loadingItem.addAnimatorUpdateListener { animator ->
                        if (animator.animatedValue as Float >= 0.3) {
                            binding.loadingItem.loop(true)
                        }

                    }
                }

                is Result.Success -> {
                    binding.loadingItem.visibility = View.GONE
                    intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Register Success", Toast.LENGTH_SHORT).show()
                    finish()

                }
            }
        }

        binding.registerButton.setOnClickListener{
            val name = binding.edRegistername.text.toString()
            val email = binding.edRegisteremail.text.toString()
            val password = binding.edRegisterPassword.text.toString()
            viewmodel.register(name, email, password)
        }

    }
}