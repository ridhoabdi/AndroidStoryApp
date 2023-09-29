package com.dicoding.sub2storyapp.ui.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.dicoding.sub2storyapp.R
import com.dicoding.sub2storyapp.data.local.datastore.UserPreference
import com.dicoding.sub2storyapp.data.remote.response.LoginResponse
import com.dicoding.sub2storyapp.data.remote.retrofit.ApiConfig
import com.dicoding.sub2storyapp.databinding.ActivityLoginBinding
import com.dicoding.sub2storyapp.model.Auth
import com.dicoding.sub2storyapp.ui.main.MainActivity
import com.dicoding.sub2storyapp.ui.main.MainViewModel
import com.dicoding.sub2storyapp.ui.main.ViewModels
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token")

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mainViewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.emailEditText.type = "email"
        binding.passwordEditText.type = "password"

        binding.btnLogin.setOnClickListener {
            val inputEmail = binding.emailEditText.text.toString()
            val inputPassword = binding.passwordEditText.text.toString()

            login(inputEmail, inputPassword)
        }

        setupView()
    }

    private fun setupView() {
        mainViewModel = ViewModelProvider(
            this,
            ViewModels(UserPreference.getInstance(dataStore))
        )[MainViewModel::class.java]

        mainViewModel.getUser().observe(this) { user ->
            if (user.isLogin) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun login(inputEmail: String, inputPassword: String) {
        showLoading(true)
        val client = ApiConfig.getApiService().login(inputEmail, inputPassword)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                showLoading(false)
                val responseBody = response.body()
                if (response.isSuccessful && responseBody?.message == "success") {
                    mainViewModel.saveUser(Auth(responseBody.loginResult.token, true))
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.login_succes),
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.failed_login),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.failed_login),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}