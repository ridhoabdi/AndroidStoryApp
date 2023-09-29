package com.dicoding.sub2storyapp.ui.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.dicoding.sub2storyapp.R
import com.dicoding.sub2storyapp.data.remote.response.FileUploadResponse
import com.dicoding.sub2storyapp.data.remote.retrofit.ApiConfig
import com.dicoding.sub2storyapp.databinding.ActivityRegisterBinding
import com.dicoding.sub2storyapp.ui.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.nameEditText.type = "name"
        binding.emailEditText.type = "email"
        binding.passwordEditText.type = "password"

        binding.btnRegister.setOnClickListener {
            val inputName = binding.nameEditText.text.toString()
            val inputEmail = binding.emailEditText.text.toString()
            val inputPassword = binding.passwordEditText.text.toString()
            createAccount(inputName, inputEmail, inputPassword)
        }
    }

    private fun createAccount(inputName: String, inputEmail: String, inputPassword: String) {
        showLoading(true)
        val client = ApiConfig.getApiService().createAccount(inputName, inputEmail, inputPassword)
        client.enqueue(object : Callback<FileUploadResponse> {
            override fun onResponse(call: Call<FileUploadResponse>, response: Response<FileUploadResponse>) {
                showLoading(false)
                val responseBody = response.body()
                if (response.isSuccessful && responseBody?.message == "User created") {
                    Toast.makeText(
                        this@RegisterActivity,
                        getString(R.string.register_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        getString(R.string.failed_register),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(
                    this@RegisterActivity,
                    getString(R.string.failed_register),
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}