package com.example.data.remote.repo

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.NetworkHelperInterface
import com.example.domain.model.Store
import com.example.domain.model.UpdateStoreRequest
import com.example.domain.repo.StoreRepo
import com.example.domain.useCase.notifications.InsertNotificationUseCase
import com.example.domain.util.Constants
import com.example.domain.util.Constants.STATUS_HIRED
import com.example.domain.util.NotificationManager
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
    private val networkHelper: NetworkHelperInterface,
    private val notSender: InsertNotificationUseCase
) : StoreRepo {

    companion object {
        private const val TAG = "StoreRepoImp"
        private const val STORES_TABLE = "stores"
        private const val USERS_TABLE = "users"
        private const val STORES_LOGO_BUCKET = "Stores"
        private const val LOGO_NAME = "logo.jpg"
        private const val CATEGORY_SEPARATOR = ","

        private const val ERROR_CREATE_STORE = "Failed to create store"
        private const val ERROR_UPDATE_STORE = "Failed to update store"
        private const val ERROR_ADD_CATEGORY = "Failed to add category"
        private const val ERROR_DELETE_CATEGORY = "Failed to delete category"
        private const val ERROR_READ_IMAGE = "Failed to read image"

        private const val SUCCESS_CREATE_STORE = "Store created successfully"
        private const val SUCCESS_UPDATE_STORE = "Store updated successfully"
        private const val SUCCESS_ADD_CATEGORY = "Category added successfully"
        private const val SUCCESS_DELETE_CATEGORY = "Category deleted successfully"

        private const val CATEGORY_EXISTS = "Category already exists"
        private const val CATEGORY_NOT_EXISTS = "Category does not exist"
    }

    // Cache user and store to avoid repeated SharedPrefs reads
    private inline val cachedUser get() = pref.getUser()
    private inline val cachedStore get() = pref.getStore()

    override suspend fun createStore(store: Store): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            if (!networkHelper.isConnected()) return@withContext Pair(false, Constants.NO_INTERNET_CONNECTION)

            val user = cachedUser

            // Parallel execution: upload logo while preparing other operations
            val results = awaitAll(
                async { uploadLogo(store.logoUrl) },
                async {
                    val storeToInsert = store.copy(logoUrl = "") // Temporary, will update after logo upload
                    insertStore(storeToInsert)
                },
                async { updateUserStatus(user.id, store.id) }
            )

            val logoUrl = results[0] as String?
            val insertResult = results[1] as Boolean
            val updateResult = results[2] as Boolean

            if (!insertResult || !updateResult) {
                return@withContext Pair(false, ERROR_CREATE_STORE)
            }

            // Update store with logo URL if uploaded
            val finalStore = if (logoUrl != null) {
                store.copy(logoUrl = logoUrl)
            } else {
                store.copy(logoUrl = "")
            }

            // Update local prefs once
            pref.saveStore(finalStore)
            pref.saveUser(user.copy(storeId = finalStore.id, status = STATUS_HIRED))

            return@withContext Pair(true, SUCCESS_CREATE_STORE)
        } catch (e: Exception) {
            Log.e(TAG, "createStore error: ${e.message}", e)
            return@withContext Pair(false, e.message ?: ERROR_CREATE_STORE)
        }
    }

    override suspend fun updateStore(store: Store): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            if (!networkHelper.isConnected()) return@withContext Pair(false, Constants.NO_INTERNET_CONNECTION)

            val currentStore = cachedStore
            val user = cachedUser

            // Only upload logo if it changed
            val logoUrl = if (store.logoUrl != currentStore.logoUrl) {
                handleLogoUpdate(store.logoUrl)
            } else {
                store.logoUrl
            }

            val storeWithLogo = store.copy(logoUrl = logoUrl)

            // Parallel execution: update store and send notification
            val (_, _) = awaitAll(
                async {
                    val updateData = UpdateStoreRequest(
                        name = storeWithLogo.name,
                        phone = storeWithLogo.phone,
                        location = storeWithLogo.location,
                        planProductLimit = storeWithLogo.planProductLimit,
                        planOperationLimit = storeWithLogo.planOperationLimit,
                        plan = storeWithLogo.plan,
                        logoUrl = storeWithLogo.logoUrl
                    )
                    supabase.from(STORES_TABLE).update(updateData) {
                        filter { eq("id", currentStore.id) }
                    }
                },
                async {
                    val updatedNot = NotificationManager.createUpdateStoreNotification(
                        user = user,
                        storeId = storeWithLogo.id,
                        storeName = storeWithLogo.name
                    )
                    notSender(updatedNot)
                }
            )

            pref.saveStore(storeWithLogo)
            return@withContext Pair(true, SUCCESS_UPDATE_STORE)
        } catch (e: Exception) {
            Log.e(TAG, "updateStore error: ${e.message}", e)
            return@withContext Pair(false, e.message ?: ERROR_UPDATE_STORE)
        }
    }

    override suspend fun addCategory(category: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            if (category.isBlank()) return@withContext Pair(false, "Category name cannot be empty")
            if (!networkHelper.isConnected()) return@withContext Pair(false, Constants.NO_INTERNET_CONNECTION)

            val trimmedCategory = category.trim()
            val currentCategories = getCategoriesList()

            // Use case-insensitive set for faster lookup
            if (currentCategories.any { it.equals(trimmedCategory, ignoreCase = true) }) {
                return@withContext Pair(false, CATEGORY_EXISTS)
            }

            val categoriesString = buildString {
                if (currentCategories.isNotEmpty()) {
                    append(currentCategories.joinToString(CATEGORY_SEPARATOR))
                    append(CATEGORY_SEPARATOR)
                }
                append(trimmedCategory)
            }

            updateCategoriesInDb(categoriesString)
            pref.saveStore(cachedStore.copy(categories = categoriesString))

            return@withContext Pair(true, SUCCESS_ADD_CATEGORY)
        } catch (e: Exception) {
            Log.e(TAG, "addCategory error: ${e.message}", e)
            return@withContext Pair(false, e.message ?: ERROR_ADD_CATEGORY)
        }
    }

    override suspend fun deleteCategory(category: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            if (!networkHelper.isConnected()) return@withContext Pair(false, Constants.NO_INTERNET_CONNECTION)

            val currentCategories = getCategoriesList()
            if (!currentCategories.contains(category)) return@withContext Pair(false, CATEGORY_NOT_EXISTS)

            val categoriesString = currentCategories
                .filterNot { it == category }
                .joinToString(CATEGORY_SEPARATOR)

            updateCategoriesInDb(categoriesString)
            pref.saveStore(cachedStore.copy(categories = categoriesString))

            return@withContext Pair(true, SUCCESS_DELETE_CATEGORY)
        } catch (e: Exception) {
            Log.e(TAG, "deleteCategory error: ${e.message}", e)
            return@withContext Pair(false, e.message ?: ERROR_DELETE_CATEGORY)
        }
    }

    // ------------------ Helpers ------------------

    private suspend fun updateUserStatus(userId: String, storeId: String): Boolean {
        return try {
            supabase.from(USERS_TABLE).update(
                mapOf("storeId" to storeId, "status" to STATUS_HIRED)
            ) { filter { eq("id", userId) } }
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

    private fun getCategoriesList(): List<String> {
        val categories = cachedStore.categories
        return if (categories.isBlank()) {
            emptyList()
        } else {
            categories.splitToSequence(CATEGORY_SEPARATOR)
                .filter { it.isNotBlank() }
                .toList()
        }
    }

    private suspend fun updateCategoriesInDb(categories: String) {
        supabase.from(STORES_TABLE).update(mapOf("categories" to categories)) {
            filter { eq("id", cachedStore.id) }
        }
    }

    private suspend fun handleLogoUpdate(logoUri: String?): String {
        return when {
            logoUri.isNullOrBlank() -> cachedStore.logoUrl
            isLocalUri(logoUri) -> uploadLogo(logoUri) ?: cachedStore.logoUrl
            else -> logoUri
        }
    }

    private fun isLocalUri(uri: String): Boolean =
        uri.startsWith("content://") || uri.startsWith("file://") ||
                (!uri.startsWith("http://") && !uri.startsWith("https://"))

    private suspend fun uploadLogo(imageUriString: String?): String? {
        if (imageUriString.isNullOrBlank()) return null

        return try {
            val imageUri = imageUriString.toUri()
            val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: run {
                    Log.e(TAG, ERROR_READ_IMAGE)
                    return null
                }

            if (bytes.isEmpty()) return null

            val storeId = cachedStore.id
            val folderPath = "stores/$storeId"
            val bucket = supabase.storage.from(STORES_LOGO_BUCKET)
            val fullPath = "$folderPath/$LOGO_NAME"

            // Delete and upload in parallel would require transaction support
            // Instead, use upsert which handles overwrite automatically
            bucket.upload(fullPath, bytes, upsert = true)
            bucket.publicUrl(fullPath)
        } catch (e: Exception) {
            Log.e(TAG, "uploadLogo error: ${e.message}", e)
            null
        }
    }

    override suspend fun deleteStore(id: String):Pair<Boolean,String>{
        try {
            if(!networkHelper.isConnected()){
                return false to Constants.NO_INTERNET_CONNECTION
            }
        //delete the store first
            supabase.from(STORES_TABLE).delete {
                filter { eq("id", id) }
            }


        // edit the exist users to be status fired and set store id to empty
            supabase.from(USERS_TABLE).update(
                mapOf<String,String>(
                    "storeId" to "",
                    "status" to "Fired"
                )
            ){
                filter {
                    eq("storeId", id)
                }
            }
            return  true to "Store deleted successfully!"

        }catch (e: Exception){
            Log.e(TAG, "deleteStore error: ${e.message}", e)
            return false to "Error in deleting the store or update users"
        }



    }
    override fun getStore(id: String) { TODO("Not yet implemented") }
}