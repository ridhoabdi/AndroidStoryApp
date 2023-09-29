package com.dicoding.sub2storyapp.di

import android.content.Context
import com.dicoding.sub2storyapp.data.local.database.StoryDatabase
import com.dicoding.sub2storyapp.data.remote.repository.StoryRepository
import com.dicoding.sub2storyapp.data.remote.retrofit.ApiConfig


object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()

        return StoryRepository(database, apiService)
    }
}