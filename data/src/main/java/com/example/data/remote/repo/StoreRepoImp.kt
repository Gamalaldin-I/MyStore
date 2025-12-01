package com.example.data.remote.repo

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.NetworkHelperInterface
import com.example.domain.model.Store
import com.example.domain.model.UpdateStoreRequest
import com.example.domain.repo.StoreRepo
import com.example.domain.util.Constants
import com.example.domain.util.Constants.STATUS_HIRED
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
    private val context: Context,
    private val networkHelper: NetworkHelperInterface
) : StoreRepo {

    companion object {
        private const val TAG = "StoreRepoImp"
        private const val STORES_TABLE = "stores"
        private const val USERS_TABLE = "users"
        private const val STORES_LOGO_BUCKET = "Stores"
        private const val LOGO_PREFIX = "logo_"
        private const val LOGO_EXTENSION = ".jpg"
        private const val CATEGORY_SEPARATOR = ","

        // Error messages
        private const val ERROR_CREATE_STORE = "Failed to create store"
        private const val ERROR_UPDATE_STORE = "Failed to update store"
        private const val ERROR_ADD_CATEGORY = "Failed to add category"
        private const val ERROR_DELETE_CATEGORY = "Failed to delete category"
        private const val ERROR_UPLOAD_LOGO = "Failed to upload logo"
        private const val ERROR_READ_IMAGE = "Failed to read image"

        private const val SUCCESS_CREATE_STORE = "Store created successfully"
        private const val SUCCESS_UPDATE_STORE = "Store updated successfully"
        private const val SUCCESS_ADD_CATEGORY = "Category added successfully"
        private const val SUCCESS_DELETE_CATEGORY = "Category deleted successfully"

        private const val CATEGORY_EXISTS = "Category already exists"
        private const val CATEGORY_NOT_EXISTS = "Category does not exist"
    }

    override suspend fun createStore(store: Store): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                // Check network connectivity
                if (!networkHelper.isConnected()) {
                    return@withContext Pair(false, Constants.NO_INTERNET_CONNECTION)
                }

                // Upload logo if provided
                val logoUrl = uploadLogo(store.logoUrl, pref.getUser().id)
                val storeWithLogo = store.copy(logoUrl = logoUrl ?: "")

                // Execute database operations in parallel
                val results = awaitAll(
                    async { updateUserStatus(pref.getUser().id, storeWithLogo.id) },
                    async { insertStore(storeWithLogo) }
                )

                // Check if any operation failed
                if (results.any { !it }) {
                    return@withContext Pair(false, ERROR_CREATE_STORE)
                }

                // Update local preferences
                updateLocalStoreData(storeWithLogo)

                Pair(true, SUCCESS_CREATE_STORE)
            } catch (e: Exception) {
                Log.e(TAG, "createStore error: ${e.message}", e)
                Pair(false, e.message ?: ERROR_CREATE_STORE)
            }
        }
    }

    override suspend fun updateStore(store: Store): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                // Check network connectivity
                if (!networkHelper.isConnected()) {
                    return@withContext Pair(false, Constants.NO_INTERNET_CONNECTION)
                }

                // Handle logo update
                val logoUrl = handleLogoUpdate(store.logoUrl)
                val storeWithLogo = store.copy(logoUrl = logoUrl)

                // Prepare and execute update
                val updateData = createUpdateRequest(storeWithLogo)

                supabase.from(STORES_TABLE).update(updateData) {
                    filter { eq("id", pref.getStore().id) }
                }

                // Update local preferences
                pref.saveStore(storeWithLogo)

                Pair(true, SUCCESS_UPDATE_STORE)
            } catch (e: Exception) {
                Log.e(TAG, "updateStore error: ${e.message}", e)
                Pair(false, e.message ?: ERROR_UPDATE_STORE)
            }
        }
    }

    override suspend fun addCategory(category: String): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                // Validate inputs
                if (category.isBlank()) {
                    return@withContext Pair(false, "Category name cannot be empty")
                }

                if (!networkHelper.isConnected()) {
                    return@withContext Pair(false, Constants.NO_INTERNET_CONNECTION)
                }

                val trimmedCategory = category.trim()
                val currentCategories = getCategoriesList()

                // Check if category already exists (case-insensitive)
                if (currentCategories.any { it.equals(trimmedCategory, ignoreCase = true) }) {
                    return@withContext Pair(false, CATEGORY_EXISTS)
                }

                // Add new category
                val updatedCategories = currentCategories + trimmedCategory
                val categoriesString = updatedCategories.joinToString(CATEGORY_SEPARATOR)

                // Update in database
                updateCategoriesInDb(categoriesString)

                // Update local preferences
                pref.saveStore(pref.getStore().copy(categories = categoriesString))

                Pair(true, SUCCESS_ADD_CATEGORY)
            } catch (e: Exception) {
                Log.e(TAG, "addCategory error: ${e.message}", e)
                Pair(false, e.message ?: ERROR_ADD_CATEGORY)
            }
        }
    }

    override suspend fun deleteCategory(category: String): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                if (!networkHelper.isConnected()) {
                    return@withContext Pair(false, Constants.NO_INTERNET_CONNECTION)
                }

                val currentCategories = getCategoriesList()

                // Check if category exists
                if (!currentCategories.contains(category)) {
                    return@withContext Pair(false, CATEGORY_NOT_EXISTS)
                }

                // Remove category
                val updatedCategories = currentCategories - category
                val categoriesString = updatedCategories.joinToString(CATEGORY_SEPARATOR)

                // Update in database
                updateCategoriesInDb(categoriesString)

                // Update local preferences
                pref.saveStore(pref.getStore().copy(categories = categoriesString))

                Pair(true, SUCCESS_DELETE_CATEGORY)
            } catch (e: Exception) {
                Log.e(TAG, "deleteCategory error: ${e.message}", e)
                Pair(false, e.message ?: ERROR_DELETE_CATEGORY)
            }
        }
    }

    // Private helper methods

    private suspend fun updateUserStatus(userId: String, storeId: String): Boolean {
        return try {
            supabase.from(USERS_TABLE).update(
                mapOf(
                    "storeId" to storeId,
                    "status" to STATUS_HIRED
                )
            ) {
                filter { eq("id", userId) }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "updateUserStatus error: ${e.message}", e)
            false
        }
    }

    private suspend fun insertStore(store: Store): Boolean {
        return try {
            supabase.from(STORES_TABLE).insert(store)
            true
        } catch (e: Exception) {
            Log.e(TAG, "insertStore error: ${e.message}", e)
            false
        }
    }

    private fun updateLocalStoreData(store: Store) {
        pref.saveStore(store)
        val updatedUser = pref.getUser().copy(
            storeId = store.id,
            status = STATUS_HIRED
        )
        pref.saveUser(updatedUser)
    }

    private fun createUpdateRequest(store: Store): UpdateStoreRequest {
        return UpdateStoreRequest(
            name = store.name,
            phone = store.phone,
            location = store.location,
            planProductLimit = store.planProductLimit,
            planOperationLimit = store.planOperationLimit,
            plan = store.plan,
            logoUrl = store.logoUrl
        )
    }

    private fun getCategoriesList(): List<String> {
        val categories = pref.getStore().categories
        return if (categories.isBlank()) {
            emptyList()
        } else {
            categories.split(CATEGORY_SEPARATOR).filter { it.isNotBlank() }
        }
    }

    private suspend fun updateCategoriesInDb(categories: String) {
        supabase.from(STORES_TABLE).update(
            mapOf("categories" to categories)
        ) {
            filter { eq("id", pref.getStore().id) }
        }
    }

    private suspend fun handleLogoUpdate(logoUri: String?): String {
        return when {
            logoUri.isNullOrBlank() -> pref.getStore().logoUrl
            isLocalUri(logoUri) -> uploadLogo(logoUri, pref.getUser().id) ?: pref.getStore().logoUrl
            else -> logoUri // Already a remote URL
        }
    }

    private fun isLocalUri(uri: String): Boolean {
        return uri.startsWith("content://") ||
                uri.startsWith("file://") ||
                (!uri.startsWith("http://") && !uri.startsWith("https://"))
    }

    private suspend fun uploadLogo(imageUriString: String?, userId: String): String? {
        if (imageUriString.isNullOrBlank()) return null

        return try {
            val imageUri = imageUriString.toUri()

            // Read image bytes
            val bytes = readImageBytes(imageUri) ?: run {
                Log.e(TAG, "uploadLogo: $ERROR_READ_IMAGE")
                return null
            }

            // Validate image size (optional)
            if (bytes.isEmpty()) {
                Log.e(TAG, "uploadLogo: Image is empty")
                return null
            }

            // Delete old logos
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

    private suspend fun deleteOldLogos(userId: String) {
        try {
            val folderPath = "stores/$userId"
            val bucket = supabase.storage.from(STORES_LOGO_BUCKET)

            val existingFiles = bucket.list(folderPath)
            existingFiles
                .filter { it.name.startsWith(LOGO_PREFIX) }
                .forEach { file ->
                    bucket.delete("$folderPath/${file.name}")
                    Log.d(TAG, "deleteOldLogos: Deleted old logo - ${file.name}")
                }
        } catch (e: Exception) {
            // Not a critical error - log and continue
            Log.d(TAG, "deleteOldLogos: ${e.message}")
        }
    }

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