package com.dicoding.sub2storyapp.ui.main

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.sub2storyapp.R
import com.dicoding.sub2storyapp.adapter.LoadingStateAdapter
import com.dicoding.sub2storyapp.adapter.StoryAdapter
import com.dicoding.sub2storyapp.data.local.datastore.UserPreference
import com.dicoding.sub2storyapp.databinding.ActivityMainBinding
import com.dicoding.sub2storyapp.model.StoryViewModel
import com.dicoding.sub2storyapp.ui.maps.MapsActivity
import com.dicoding.sub2storyapp.ui.post.PostingStoryActivity
import com.dicoding.sub2storyapp.ui.welcome.WelcomeActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token")

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private val storyViewModel: StoryViewModel by viewModels {
        StoryViewModel.ViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionButton()
        val layoutManager = LinearLayoutManager(this)
        binding.rvStoryuser.layoutManager = layoutManager

        setupView()
        getStory()
    }

    private fun getStory() {
        val adapter = StoryAdapter()
        binding.rvStoryuser.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.rvStoryuser.setHasFixedSize(true)
        binding.rvStoryuser.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )
        mainViewModel.getUser().observe(this) { userAuth ->
            if (userAuth != null) {
                storyViewModel.stories("Bearer " + userAuth.token).observe(this) { stories ->
                    adapter.submitData(lifecycle, stories)
                }
            }
        }

    }

    private fun setupView() {
        mainViewModel = ViewModelProvider(
            this,
            ViewModels(UserPreference.getInstance(dataStore))
        )[MainViewModel::class.java]
        mainViewModel.getUser().observe(this) { user ->
            if (user.isLogin) {
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }
    }

    private fun setupActionButton() {
        binding.btnAddStory.setOnClickListener {
            val intent = Intent(this, PostingStoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.Logout -> {
                mainViewModel.logout()
            }
            R.id.menu_map -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}