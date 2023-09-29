package com.dicoding.sub2storyapp.model

import android.content.Context
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.sub2storyapp.data.remote.repository.StoryRepository
import com.dicoding.sub2storyapp.data.remote.response.ListStoryItem
import com.dicoding.sub2storyapp.di.Injection

class StoryViewModel(private val storiesRepository: StoryRepository) : ViewModel() {

    fun stories(header: String): LiveData<PagingData<ListStoryItem>> =
        storiesRepository.getStory(header).cachedIn(viewModelScope)

    class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StoryViewModel(Injection.provideRepository(context)) as T
            } else throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}