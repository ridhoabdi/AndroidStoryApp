package com.dicoding.sub2storyapp.ui.post

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.dicoding.sub2storyapp.R
import com.dicoding.sub2storyapp.data.local.datastore.UserPreference
import com.dicoding.sub2storyapp.data.remote.response.FileUploadResponse
import com.dicoding.sub2storyapp.data.remote.retrofit.ApiConfig
import com.dicoding.sub2storyapp.databinding.ActivityPostingStoryBinding
import com.dicoding.sub2storyapp.ui.main.MainActivity
import com.dicoding.sub2storyapp.ui.main.MainViewModel
import com.dicoding.sub2storyapp.ui.main.ViewModels
import com.dicoding.sub2storyapp.utils.uriToFile
import com.dicoding.sub2storyapp.utils.reduceFileImage
import com.dicoding.sub2storyapp.utils.createTempFile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PostingStoryActivity : AppCompatActivity() {
    private lateinit var postingViewModels: MainViewModel
    private lateinit var binding: ActivityPostingStoryBinding
    private var getFile: File? = null

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostingStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTakePhoto.setOnClickListener {
            startTakePhoto()
        }

        binding.btnStartGallery.setOnClickListener {
            startGallery()
        }

        binding.btnUploadImage.setOnClickListener {
            uploadImage()
        }

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        setupView()
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)
        createTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@PostingStoryActivity,
                "com.dicoding.sub2storyapp",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }


    private fun uploadImage() {
        showLoading(true)

        if (getFile != null) {
            val file = reduceFileImage(getFile as File)

            val description = binding.textDescription.text.toString()
                .toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )

            postingViewModels.getUser().observe(this) {
                if (it != null) {
                    val client = ApiConfig.getApiService()
                        .uploadStories("Bearer " + it.token, imageMultipart, description)
                    client.enqueue(object : Callback<FileUploadResponse> {
                        override fun onResponse(
                            call: Call<FileUploadResponse>,
                            response: Response<FileUploadResponse>
                        ) {
                            showLoading(false)
                            val responseBody = response.body()
                            if (response.isSuccessful && responseBody?.message == "Story created successfully") {
                                Toast.makeText(
                                    this@PostingStoryActivity,
                                    getString(R.string.upload_story_succes),
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent =
                                    Intent(this@PostingStoryActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this@PostingStoryActivity,
                                    getString(R.string.failed_upload_story),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                            showLoading(false)
                            Toast.makeText(
                                this@PostingStoryActivity,
                                getString(R.string.failed_upload_story),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }
            }
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this)
            getFile = myFile
            binding.imgPost.setImageURI(selectedImg)
        }
    }


    private lateinit var currentPhotoPath: String
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            val result = BitmapFactory.decodeFile(myFile.path)

            getFile = myFile
            binding.imgPost.setImageBitmap(result)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun setupView() {
        postingViewModels = ViewModelProvider(
            this,
            ViewModels(UserPreference.getInstance(dataStore))
        )[MainViewModel::class.java]
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}