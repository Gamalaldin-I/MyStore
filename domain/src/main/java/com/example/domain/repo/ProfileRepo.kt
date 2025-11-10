package com.example.domain.repo

import android.net.Uri

interface ProfileRepo {
    suspend fun changeProfileImage(
        imageUri: Uri,
        onResult: (Boolean, String) -> Unit
    )
    suspend fun removeProfileImage(onResult: (Boolean, String) -> Unit)

    suspend fun resetPassword(email: String, onResult: (Boolean, String) -> Unit)
    suspend fun updateName(name:String): Pair<Boolean,String>
    suspend fun updateEmail(newEmail:String,password: String):Pair<Boolean,String>
}