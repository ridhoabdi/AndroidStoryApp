package com.dicoding.sub2storyapp.ui.detailstory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.dicoding.sub2storyapp.R
import com.dicoding.sub2storyapp.databinding.ActivityDetailStoryBinding
import com.dicoding.sub2storyapp.model.StoryModel

class DetailStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStoryBinding

    companion object {
        const val DETAIL_STORY = "detail_story"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val story = intent.getParcelableExtra<StoryModel>(DETAIL_STORY) as StoryModel
        binding.tvDesc.text = story.description
        supportActionBar?.title = story.name
        Glide.with(this)
            .load(story.photo)
            .error(R.drawable.ic_baseline_broken_image_black_24)
            .into(binding.ivDetail)
    }
}