package com.example.data.remote.repo

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.NetworkHelperInterface
import com.example.domain.model.User
import com.example.domain.repo.ProfileRepo
import com.example.domain.useCase.notifications.InsertNotificationUseCase
import com.example.domain.useCase.store.DeleteStoreUseCase
import com.example.domain.util.Constants
import com.example.domain.util.NotificationManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

class ProfileRepoImp(
    private val supabase: SupabaseClient,
    private val pref: SharedPref,
    private val context: Context,
    private val networkHelper: NetworkHelperInterface,
    private val notificationUseCase: InsertNotificationUseCase,
    val deleteStoreUseCase:DeleteStoreUseCase
) : ProfileRepo {

    companion object {
        private const val AVATARS_BUCKET = "Avatars"
        private const val USERS = "users"
        private const val TAG = "ProfileRepoImp"
        private const val RESET_PASSWORD_URL = "https://reset-password-api.vercel.app/index.html"
    }

    private suspend fun insertNot(){
        val upNot = NotificationManager.createUpdateUserNotification(
            pref.getUser(),
            pref.getStore().id,
            pref.getUser().name
        )
        notificationUseCase(upNot)
    }

    // -------------------------------
    // Helpers
    // -------------------------------

    private fun checkInternet(onResult: (Boolean, String) -> Unit): Boolean {
        if (!networkHelper.isConnected()) {
            onResult(false, Constants.NO_INTERNET_CONNECTION)
            return false
        }
        return true
    }

    private fun getAvatarFileName(): String = "${pref.getUser().id}.jpg"

    private fun buildPublicUrl(file: String): String {
        return "${supabase.supabaseUrl}/storage/v1/object/public/$AVATARS_BUCKET/$file"
    }

    private fun logError(msg: String, e: Exception) {
        Log.e(TAG, "$msg: ${e.message}", e)
    }

    // -------------------------------
    // Upload Profile Image
    // -------------------------------

    override suspend fun changeProfileImage(
        imageUri: Uri,
        onResult: (Boolean, String) -> Unit
    ) {
        if (!checkInternet(onResult)) return

        try {
            val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: return onResult(false, "Error reading image")

            val fileName = getAvatarFileName()
            val bucket = supabase.storage.from(AVATARS_BUCKET)

            bucket.upload(fileName, bytes, upsert = true)

            val url = buildPublicUrl(fileName)

            // Save in local storage
            pref.setProfileImage(url)

            // Update DB
            supabase.from(USERS).update(
                mapOf("photoUrl" to url)
            ) { filter { eq("id", pref.getUser().id) } }

            onResult(true, "Image uploaded successfully")

        } catch (e: Exception) {
            logError("Error uploading image", e)
            onResult(false, "Error: ${e.message}")
        }
    }

    // -------------------------------
    // Delete Profile Image
    // -------------------------------

    override suspend fun removeProfileImage(onResult: (Boolean, String) -> Unit) {
        if (!checkInternet(onResult)) return

        try {
            val bucket = supabase.storage.from(AVATARS_BUCKET)
            bucket.delete(getAvatarFileName())

            // Clear DB
            supabase.from(USERS).update(
                mapOf("photoUrl" to "")
            ) { filter { eq("id", pref.getUser().id) } }

            // Clear shared pref
            pref.setProfileImage("")

            onResult(true, "Image removed successfully")

        } catch (e: Exception) {
            logError("Error removing photo", e)
            onResult(false, "Error: ${e.message}")
        }
    }

    // -------------------------------
    // Reset Password
    // -------------------------------

    override suspend fun resetPassword(
        email: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (!checkInternet(onResult)) return

        try {
            supabase.auth.resetPasswordForEmail(email, redirectUrl = RESET_PASSWORD_URL)
            onResult(true, "Password reset email sent successfully")
        } catch (e: Exception) {
            logError("Error resetting password", e)
            onResult(false, "Error: ${e.message}")
        }
    }

    // -------------------------------
    // Update Name
    // -------------------------------

    override suspend fun updateName(name: String): Pair<Boolean, String> {
        if (!networkHelper.isConnected())
            return false to Constants.NO_INTERNET_CONNECTION

        return try {
            val userId = pref.getUser().id

            supabase.from(USERS).update(mapOf("name" to name)) {
                filter { eq("id", userId) }
            }

            // Update locally
            pref.saveUser(pref.getUser().copy(name = name))
            insertNot()

            true to "Name updated successfully"

        } catch (e: Exception) {
            logError("Error updating name", e)
            false to "Error: ${e.message}"
        }
    }

    // -------------------------------
    // Update Email
    // -------------------------------

    override suspend fun updateEmail(newEmail: String, password: String): Pair<Boolean, String> {
        if (!networkHelper.isConnected())
            return false to Constants.NO_INTERNET_CONNECTION

        return try {
            val current = supabase.auth.currentUserOrNull()
                ?: return false to "Invalid session"

            val currentEmail = current.email
                ?: return false to "Invalid session"

            // Re-authenticate
            try {
                supabase.auth.signInWith(Email) {
                    email = currentEmail
                    this.password = password
                }
            } catch (_: Exception) {
                return false to "The current password is incorrect"
            }

            // Change email
            supabase.auth.modifyUser { email = newEmail }

            true to "Email confirmation sent. Please check your new inbox."

        } catch (e: Exception) {
            logError("Error updating email", e)
            false to "Error: ${e.message}"
        }
    }

    override suspend fun deleteAccount(): Pair<Boolean, String> {
        if (!networkHelper.isConnected())
            return false to Constants.NO_INTERNET_CONNECTION

        return try {
            Log.d(TAG, "Starting account deletion...")

            //  update ـ session
            supabase.auth.refreshCurrentSession()

            val session = supabase.auth.currentSessionOrNull()
            if (session == null) {
                Log.e(TAG, "No active session found")
                return false to "User not logged in or session expired"
            }

            val token = session.accessToken
            if (token.isEmpty()) {
                Log.e(TAG, "Access token is empty")
                return false to "Access token is empty"
            }

            Log.d(TAG, "Token length: ${token.length}")
            Log.d(TAG, "Token preview: ${token.take(50)}...")

            // delete profile image
            try {
                removeProfileImageSuspend()
                Log.d(TAG, "Profile image removed")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to remove profile image: ${e.message}")
            }
            // delete profile image
            try {
                deleteStoreIfRequired()
                Log.d(TAG, "store checked")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to remove profile image: ${e.message}")
            }

            // ✅  Edge Function
            Log.d(TAG, "Calling delete-account function...")
            val response = supabase.functions.invoke(
                function = "delete-account")


            val responseBody = response.bodyAsText()
            Log.d(TAG, "Response status: ${response.status}")
            Log.d(TAG, "Response body: $responseBody")

            if (response.status != HttpStatusCode.OK) {
                Log.e(TAG, "Delete failed with status: ${response.status}")
                return false to "Error deleting account: $responseBody"
            }

            Log.d(TAG, "Account deleted successfully!")
            true to "Account deleted successfully"

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting account", e)
            false to "Error: ${e.message}"
        }
    }

    private suspend fun removeProfileImageSuspend() {
        val bucket = supabase.storage.from(AVATARS_BUCKET)
        bucket.delete(getAvatarFileName())
    }
    private suspend fun deleteStoreIfRequired(){
        //get the user first
        val userId = pref.getUser().id
        val user = supabase.from(USERS).select{filter { eq("id", userId) } }.decodeSingle<User>()
        if(user.role == Constants.OWNER_ROLE){
            deleteStoreUseCase(user.storeId)
        }
    }


    // -------------------------------
    // Observe Role from DB
    // -------------------------------

    override suspend fun observeRole(): Int {
        if (!networkHelper.isConnected()) return -1

        return try {
            val id = pref.getUser().id

            val user = supabase.from(USERS).select {
                filter { eq("id", id) }
            }.decodeSingle<User>()

            user.role

        } catch (e: Exception) {
            logError("Error observing role", e)
            -1
        }
    }

}
