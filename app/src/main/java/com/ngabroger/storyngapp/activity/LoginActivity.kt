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
import com.ngabroger.storyngapp.costumview.CustomEditText.ValidationType
import com.ngabroger.storyngapp.data.Result
import com.ngabroger.storyngapp.data.StoryRepository
import com.ngabroger.storyngapp.data.api.ApiConfig
import com.ngabroger.storyngapp.data.local.db.StoryDatabase
import com.ngabroger.storyngapp.data.local.preference.UserPreferences
import com.ngabroger.storyngapp.databinding.ActivityLoginBinding
import com.ngabroger.storyngapp.viewmodel.UserModel
import com.ngabroger.storyngapp.viewmodel.UserModelFactory

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTextWatcher()

        val pref = UserPreferences.getInstance(this)
        val db = StoryDatabase.getInstance(this)
        val repository = StoryRepository.getInstance(ApiConfig.getApiServiceWithoutToken(),db)
        val factory = UserModelFactory(repository, pref)
        val viewmodel= ViewModelProvider(this, factory)[UserModel::class.java]



        viewmodel.loginResult.observe(this, Observer {
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
                    if (it.data.error == true){
                        SapDialog(this).apply {
                            title = "Error"
                            messageText = it.data.message
                            isCancellable = true
                        }.build().show()
                    }else{
                        val data = it.data.loginResult
                        if (data != null) {
                            data.token?.let { it1 -> data.name?.let { it2 ->
                                viewmodel.saveUserToken(it1,
                                    it2
                                )
                            } }
                            Intent(this, MainActivity::class.java).also { item->
                                startActivity(item)
                                finish()
                            }



                        }else{
                            Toast.makeText(this, "data tidak tersimpan", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }
        })
        binding.tvToRegister.setOnClickListener{
            intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.btnLogin.setOnClickListener{
            val email = binding.edEmailText.text.toString()
            val password = binding.edPasswordText.text.toString()
            viewmodel.login(email,password)

        }
    }

    private fun setupTextWatcher(){
        binding.edEmailText.apply {
            setValidationType(ValidationType.EMAIL)
            setInputLayout(binding.textInputLayoutEmailLogin)
        }

        binding.edPasswordText.apply {
            setValidationType(ValidationType.PASSWORD)
            setInputLayout(binding.textInputLayoutPasswordLogin)
        }
    }
}