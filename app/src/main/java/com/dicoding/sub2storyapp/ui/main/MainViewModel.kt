package com.dicoding.sub2storyapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.sub2storyapp.data.local.datastore.UserPreference
import com.dicoding.sub2storyapp.model.Auth
import kotlinx.coroutines.launch

class MainViewModel(private val pref: UserPreference) : ViewModel() {
    fun getUser(): LiveData<Auth> {
        return pref.getUser().asLiveData()
    }

    fun saveUser(user: Auth) {
        viewModelScope.launch {
            pref.saveUser(user)
        }
    }

    fun logout() {
        viewModelScope.launch {
            pref.logout()
        }
    }
}