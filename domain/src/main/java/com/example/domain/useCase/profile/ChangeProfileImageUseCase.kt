package com.example.domain.useCase.profile

import android.net.Uri
import com.example.domain.repo.ProfileRepo

class ChangeProfileImageUseCase(private val repo: ProfileRepo) {
    suspend operator fun invoke(imageUri:Uri, onResult: (Boolean, String) -> Unit){
        repo.changeProfileImage(imageUri,onResult)
    }
}