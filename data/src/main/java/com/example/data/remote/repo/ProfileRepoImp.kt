package com.example.data.remote.repo

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.repo.ProfileRepo
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

class ProfileRepoImp (
    private val supabase: SupabaseClient,
    private val pref: SharedPref,
    private val context: Context
): ProfileRepo {
    companion object{
        private const val AVATARS_BUCKET = "Avatars"
        private const val TAG = "ProfileRepoImp"
        private const val USERS="users"
    }
    override suspend fun changeProfileImage(
        imageUri: Uri,
        onResult: (Boolean, String) -> Unit
    ){
        val fileName = "${pref.getUser().id}.jpg"
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes() ?:
            return onResult(false, "Error")
            val bucket = supabase.storage.from(AVATARS_BUCKET)
            bucket.upload(fileName, bytes, upsert = true)
            val url =    "https://ayoanqjzciolnahljauc.supabase.co/storage/v1/object/public/$AVATARS_BUCKET/$fileName"
            //save url
            pref.setProfileImage(url)
            //update in user
            supabase.from(USERS).update(
                mapOf("photoUrl" to url)
            ) {
                filter {
                    eq("id", pref.getUser().id)
                }
            }

            onResult(true, "Image uploaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image: ${e.message}", e)
        }
    }

    override suspend fun removeProfileImage(onResult: (Boolean, String) -> Unit){
        try{
            val bucket = supabase.storage.from(AVATARS_BUCKET)
            //delete
            bucket.delete("${pref.getUser().id}.jpg")
            //update
            supabase.from(USERS).update(
                mapOf("photoUrl" to "")
            ){
                filter {
                    eq("id", pref.getUser().id)
                }
            }
            onResult(true, "Image removed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing photo: ${e.message}", e)
            onResult(false, "Error removing photo: ${e.message}")
        }
    }

    override suspend fun resetPassword(
        email: String,
        onResult: (Boolean, String) -> Unit
    ) {
        try {
        supabase.auth.resetPasswordForEmail(
            email = email,
            redirectUrl = "https://reset-password-api.vercel.app/"
            )
            onResult(true, "Password reset email sent successfully")
        }
        catch (e: Exception) {
            Log.e(TAG, "Error resetting password: ${e.message}", e)
            onResult(false, "Error resetting password: ${e.message}")
        }
    }


    override suspend fun updateName(name: String): Pair<Boolean, String> {
        return try {
            val userId = pref.getUser().id
            supabase.from(USERS).update(mapOf("name" to name)) {
                filter { eq("id", userId) }
            }
            true to "Name updated successfully"
        } catch (e: Exception) {
            Log.e(TAG, "Error updating name: ${e.message}", e)
            false to "Error updating name: ${e.message}"
        }
    }


    override suspend fun updateEmail(
        newEmail: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }
}

