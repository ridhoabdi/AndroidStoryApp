package com.dicoding.sub2storyapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Auth(
    val token: String, val isLogin: Boolean
) : Parcelable
