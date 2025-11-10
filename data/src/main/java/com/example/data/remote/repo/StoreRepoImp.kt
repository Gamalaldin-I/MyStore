package com.example.data.remote.repo

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.Store
import com.example.domain.model.UpdateStoreRequest
import com.example.domain.repo.StoreRepo
import com.example.domain.util.Constants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class StoreRepoImp(
    private val supabase: SupabaseClient,
    private val pref: SharedPref,
    private val context: Context
) : StoreRepo {

    companion object {
        private const val TAG = "StoreRepoImp"
        private const val STORES_TABLE = "stores"
        private const val USERS_TABLE = "users"
        private const val STORES_LOGO_BUCKET = "Stores"
        private const val LOGO_PREFIX = "logo_"
        private const val LOGO_EXTENSION = ".jpg"
    }

    override suspend fun createStore(store: Store): Pair<Boolean, String> {
        return try {
            withContext(Dispatchers.IO) {
                // Upload logo if provided
                val logoUrl = uploadLogo(store.logoUrl, pref.getUser().id)
                store.logoUrl = logoUrl ?: ""

                // Execute database operations in parallel
                val updateUserTask = async {
                    supabase.from(USERS_TABLE).update(
                        mapOf("storeId" to store.id)
                    ) {
                        filter { eq("id", pref.getUser().id) }
                    }
                }

                val insertStoreTask = async {
                    supabase.from(STORES_TABLE).insert(store)
                }

                // Wait for both operations to complete
                awaitAll(updateUserTask, insertStoreTask)

                // Update local preferences
                pref.saveStore(store)
                val updatedUser = pref.getUser().copy(
                    storeId = store.id,
                    status = Constants.STATUS_HIRED
                )
                pref.saveUser(updatedUser)

                Pair(true, "Store created successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "createStore error: ${e.message}", e)
            Pair(false, e.message ?: "Failed to create store")
        }
    }

    override suspend fun updateStore(store: Store): Pair<Boolean, String> {
        return try {
            withContext(Dispatchers.IO) {
                // Handle logo update
                val logoUrl = handleLogoUpdate(store.logoUrl)
                store.logoUrl = logoUrl

                // Prepare update request
                val updateData = UpdateStoreRequest(
                    name = store.name,
                    phone = store.phone,
                    location = store.location,
                    planProductLimit = store.planProductLimit,
                    planOperationLimit = store.planOperationLimit,
                    plan = store.plan,
                    logoUrl = store.logoUrl
                )

                // Update store in database
                supabase.from(STORES_TABLE).update(updateData) {
                    filter { eq("id", pref.getStore().id) }
                }

                // Update local preferences
                pref.saveStore(store)

                Pair(true, "Store updated successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateStore error: ${e.message}", e)
            Pair(false, e.message ?: "Failed to update store")
        }
    }

    /**
     * Handles logo update logic:
     * - If new logo URI is provided, upload it
     * - Otherwise, keep the existing logo URL
     */
    private suspend fun handleLogoUpdate(logoUri: String?): String {
        return when {
            // New logo provided - upload it
            !logoUri.isNullOrEmpty() && isLocalUri(logoUri) -> {
                uploadLogo(logoUri, pref.getUser().id) ?: pref.getStore().logoUrl
            }
            // Keep existing logo
            else -> pref.getStore().logoUrl
        }
    }

    /**
     * Checks if the URI is a local file URI (not a remote URL)
     */
    private fun isLocalUri(uri: String): Boolean {
        return uri.startsWith("content://") ||
                uri.startsWith("file://") ||
                !uri.startsWith("http")
    }

    /**
     * Uploads a logo image to Supabase storage
     * @param imageUriString The URI string of the image to upload
     * @param userId The user ID to organize the storage folder
     * @return The public URL of the uploaded image, or null if upload fails
     */
    private suspend fun uploadLogo(imageUriString: String?, userId: String): String? {
        if (imageUriString.isNullOrEmpty()) return null

        return try {
            val imageUri = imageUriString.toUri()

            // Read image bytes from URI
            val bytes = readImageBytes(imageUri) ?: run {
                Log.e(TAG, "uploadLogo: Failed to read image bytes")
                return null
            }

            // Delete old logos before uploading new one
            deleteOldLogos(userId)

            // Upload new logo
            val url = uploadLogoToStorage(bytes, userId)

            // Save URL to preferences
            pref.setStoreLogoUrl(url)

            Log.d(TAG, "uploadLogo: Image uploaded successfully - $url")
            url
        } catch (e: Exception) {
            Log.e(TAG, "uploadLogo error: ${e.message}", e)
            null
        }
    }

    /**
     * Reads image bytes from a URI
     */
    private fun readImageBytes(imageUri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            Log.e(TAG, "readImageBytes error: ${e.message}", e)
            null
        }
    }

    /**
     * Deletes old logo files from storage
     */
    private suspend fun deleteOldLogos(userId: String) {
        try {
            val folderPath = "stores/$userId"
            val bucket = supabase.storage.from(STORES_LOGO_BUCKET)

            val existingFiles = bucket.list(folderPath)
            existingFiles.forEach { file ->
                if (file.name.startsWith(LOGO_PREFIX)) {
                    bucket.delete("$folderPath/${file.name}")
                    Log.d(TAG, "deleteOldLogos: Deleted old logo - ${file.name}")
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "deleteOldLogos: No old logos to delete or error occurred - ${e.message}")
        }
    }

    /**
     * Uploads image bytes to Supabase storage
     */
    private suspend fun uploadLogoToStorage(bytes: ByteArray, userId: String): String {
        val folderPath = "stores/$userId"
        val fileName = "$LOGO_PREFIX${System.currentTimeMillis()}$LOGO_EXTENSION"
        val fullPath = "$folderPath/$fileName"

        val bucket = supabase.storage.from(STORES_LOGO_BUCKET)

        bucket.upload(
            path = fullPath,
            data = bytes,
            upsert = true
        )

        return bucket.publicUrl(fullPath)
    }

    override fun deleteStore(id: String) {
        TODO("Not yet implemented")
    }

    override fun getStore(id: String) {
        TODO("Not yet implemented")
    }
}