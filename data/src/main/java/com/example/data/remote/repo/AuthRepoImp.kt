package com.example.data.remote.repo

import android.util.Log
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.Store
import com.example.domain.model.User
import com.example.domain.repo.AuthRepo
import com.example.domain.util.Constants.EMPLOYEE_ROLE
import com.example.domain.util.Constants.OWNER_ROLE
import com.example.domain.util.Constants.STATUS_HIRED
import com.example.domain.util.Constants.STATUS_PENDING
import com.example.domain.util.IdGenerator
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthRepoImp(
    private val supabase: SupabaseClient,
    private val sharedPref: SharedPref
) : AuthRepo {

    companion object {
        private const val TAG = "SUPABASE_AUTH"
        private const val USERS = "users"
        private const val STORES = "stores"
        private const val SIGNUP_COOLDOWN = 60000L // 60 seconds
    }

    private var lastSignupTime = 0L

    // =====================================================
    // LOGIN WITH EMAIL & PASSWORD
    // =====================================================
    override fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                supabase.auth.currentUserOrNull()?.let { user ->
                    val fUser = fetchUser(user.id)
                    if (fUser != null) {
                        if (fUser.storeId.isNotEmpty()) {
                            fetchStore(fUser.storeId)?.let { store ->
                                sharedPref.saveStore(store)
                            }
                        }
                        sharedPref.saveUser(fUser)
                        withContext(Dispatchers.Main) { onResult(true, "Login successful") }
                    } else {
                        withContext(Dispatchers.Main) { onResult(false, "User not found") }
                    }
                } ?: withContext(Dispatchers.Main) {
                    onResult(false, "Login failed")
                }

            } catch (e: Exception) {
                Log.e(TAG, "login error: ${e.message}", e)
                withContext(Dispatchers.Main) { onResult(false, e.message ?: "Login error") }
            }
        }
    }

    private suspend fun fetchUser(id: String): User? = try {
        supabase.from(USERS).select { filter { eq("id", id) } }.decodeSingle<User>()
    } catch (e: Exception) {
        Log.e(TAG, "fetchUser error: ${e.message}", e)
        null
    }

    private suspend fun fetchStore(storeId: String): Store? = try {
        supabase.from(STORES).select { filter { eq("id", storeId) } }.decodeSingle<Store>()
    } catch (e: Exception) {
        Log.e(TAG, "fetchStore error: ${e.message}", e)
        null
    }

    // =====================================================
    // GOOGLE SIGN-IN
    // =====================================================
    override fun signWithGoogle(
        idToken: String,
        role: Int,
        storePhone: String,
        storeName: String,
        storeLocation: String,
        onResult: (Boolean, String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabase.auth.signInWith(IDToken) {
                    this.idToken = idToken
                    provider = Google
                }

                supabase.auth.currentUserOrNull()?.let { user ->
                    fetchUser(user.id)?.let {
                        sharedPref.saveUser(it)
                        withContext(Dispatchers.Main) { onResult(true, "Google Sign-in successful") }
                    } ?: withContext(Dispatchers.Main) { onResult(false, "User not found") }
                } ?: withContext(Dispatchers.Main) {
                    onResult(false, "Google Sign-in failed")
                }

            } catch (e: Exception) {
                Log.e(TAG, "signWithGoogle error: ${e.message}", e)
                withContext(Dispatchers.Main) { onResult(false, e.message ?: "Google Sign-in error") }
            }
        }
    }

    // =====================================================
    // REGISTER OWNER (with cooldown)
    // =====================================================
    override fun registerOwner(
        email: String,
        password: String,
        name: String,
        storeName: String,
        storeLocation: String,
        storePhone: String,
        onResult: (Boolean, String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastSignupTime < SIGNUP_COOLDOWN) {
                val wait = ((SIGNUP_COOLDOWN - (currentTime - lastSignupTime)) / 1000).toInt()
                Log.w(TAG, "Please wait $wait seconds before another signup.")
                withContext(Dispatchers.Main) {
                    onResult(false, "Please wait $wait seconds before trying again.")
                }
                return@launch
            }

            try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }

                val user = supabase.auth.currentUserOrNull()
                if (user != null) {
                    val storeId = IdGenerator.generateTimestampedId()
                    val newUser = User(
                        id = user.id,
                        name = name,
                        role = OWNER_ROLE,
                        photoUrl = "",
                        status = STATUS_HIRED,
                        storeId = storeId,
                        email = email
                    )
                    val newStore = Store(
                        id = storeId,
                        name = storeName,
                        location = storeLocation,
                        phone = storePhone,
                        ownerId = user.id
                    )
                    supabase.from(STORES).insert(newStore)
                    supabase.from(USERS).insert(newUser)
                    sharedPref.saveUser(newUser)
                    sharedPref.saveStore(newStore)

                    lastSignupTime = System.currentTimeMillis()
                    withContext(Dispatchers.Main) {
                        onResult(true, "Owner registered successfully")
                    }
                } else {
                    withContext(Dispatchers.Main) { onResult(false, "Registration failed") }
                }

            } catch (e: Exception) {
                Log.e(TAG, "registerOwner error: ${e.message}", e)
                withContext(Dispatchers.Main) { onResult(false, e.message ?: "Registration error") }
            }
        }
    }

    // =====================================================
    // REGISTER EMPLOYEE
    // =====================================================
    override fun registerEmployee(
        name: String,
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }

                supabase.auth.currentUserOrNull()?.let { user ->
                    val newUser = User(
                        id = user.id,
                        name = name,
                        role = EMPLOYEE_ROLE,
                        photoUrl = "",
                        status = STATUS_PENDING,
                        storeId = "",
                        email = email
                    )
                    supabase.from(USERS).insert(newUser)

                    withContext(Dispatchers.Main) {
                        onResult(true, "Employee registered successfully")
                    }
                } ?: withContext(Dispatchers.Main) {
                    onResult(false, "Employee registration failed")
                }

            } catch (e: Exception) {
                Log.e(TAG, "registerEmployee error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onResult(false, e.message ?: "Employee registration error")
                }
            }
        }
    }

    // =====================================================
    // LOGOUT
    // =====================================================
    override fun logout(onResult: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabase.auth.signOut()
                sharedPref.clearPrefs()
                withContext(Dispatchers.Main) { onResult(true, "Logout successful") }
            } catch (e: Exception) {
                Log.e(TAG, "logout error: ${e.message}", e)
                withContext(Dispatchers.Main) { onResult(false, e.message ?: "Logout error") }
            }
        }
    }
}
