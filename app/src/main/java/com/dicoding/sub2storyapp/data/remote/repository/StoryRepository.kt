package com.dicoding.sub2storyapp.data.remote.repository

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.dicoding.sub2storyapp.data.local.database.StoryDatabase
import com.dicoding.sub2storyapp.data.remote.response.ListStoryItem
import com.dicoding.sub2storyapp.data.remote.retrofit.ApiService


class StoryRepository(
    private val storydatabase: StoryDatabase,
    private val apiService: ApiService
) {
    fun getStory(header: String): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
            ),
            pagingSourceFactory = {
                StoryPagingSource(apiService, header)
            }
        ).liveData
    }
}