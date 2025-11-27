package com.example.data.remote.repo

import android.util.Log
import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.NetworkHelperInterface
import com.example.domain.model.Store
import com.example.domain.model.User
import com.example.domain.repo.AuthRepo
import com.example.domain.util.Constants
import com.example.domain.util.Constants.EMAIL_PROVIDER
import com.example.domain.util.Constants.GOOGLE_PROVIDER
import com.example.domain.util.Constants.STATUS_PENDING
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

class AuthRepoImp
    (
    private val supabase: SupabaseClient,
    private val sharedPref: SharedPref,
    private val networkHelper: NetworkHelperInterface
) : AuthRepo {
    val c = Constants

    companion object {
        private const val TAG = "SUPABASE_AUTH"
        private const val USERS = "users"
        private const val STORES = "stores"
        private const val SIGNUP_COOLDOWN = 60000L // 60 seconds
        private const val EMAIL ="email"
        private const val ID ="id"
    }

    private var lastSignupTime = 0L

    // =====================================================
    // LOGIN WITH EMAIL & PASSWORD
    // =====================================================
    override fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        if(!networkHelper.isConnected()) {
            onResult(false, c.NO_INTERNET_CONNECTION)
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                supabase.auth.currentUserOrNull()?.let { user ->
                    val email = user.email ?: ""
                    val fUser = fetchUser(user.id, email)
                    if (fUser != null) {
                        if (fUser.storeId.isNotEmpty()) {
                            fetchStore(fUser.storeId)?.let { store ->
                                sharedPref.saveStore(store)
                            }
                        }
                        if (fUser.provider == GOOGLE_PROVIDER) sharedPref.setLoginFromGoogle()
                        sharedPref.saveUser(fUser)
                        withContext(Dispatchers.Main) { onResult(true,c.LOGIN_SUCCESS_MESSAGE) }
                    } else {
                        withContext(Dispatchers.Main) { onResult(false,c.USER_NOT_FOUND) }
                    }
                } ?: withContext(Dispatchers.Main) {
                    onResult(false,c.LOGIN_FAILED)
                }

            } catch (e: Exception) {
                Log.e(TAG, "login error: ${e.message}", e)
                withContext(Dispatchers.Main) { onResult(false,c.LOGIN_FAILED) }
            }
        }
    }

    private suspend fun fetchUser(id: String, email: String): User? {
        try {
            supabase.from(USERS).update(
                mapOf(EMAIL to email)
            ) {
                filter {
                    eq(ID, id)
                }
            }
            return supabase.from(USERS).select { filter { eq(ID, id) } }.decodeSingle<User>()
        } catch (e: Exception) {
            Log.e(TAG, "fetchUser error: ${e.message}", e)
            return null
        }
    }

    private suspend fun fetchStore(storeId: String): Store? = try {
        supabase.from(STORES).select { filter { eq(ID, storeId) } }.decodeSingle<Store>()
    } catch (e: Exception) {
        Log.e(TAG, "fetchStore error: ${e.message}", e)
        null
    }

    // =====================================================
    // GOOGLE SIGN-IN
    // =====================================================
    override suspend fun signWithGoogle(
        idToken: String,
        role: Int,
        fromLoginScreen: Boolean
    ): Pair<Boolean, String> {
        return try {
            if(!networkHelper.isConnected()) {
                return  Pair(false, c.NO_INTERNET_CONNECTION)}

            //sign with google
            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }

            val user = supabase.auth.currentUserOrNull()
                ?: return Pair(false,c.GOOGLE_SIGN_FAILED)

            //get the user
            val existingUser = try {
                supabase.from(USERS)
                    .select {
                        filter { eq(ID, user.id) }
                    }
                    .decodeSingle<User>()
            } catch (_: Exception) {
                null //case not found
            }
            //if the user exist login
            if (existingUser != null && existingUser.email.isNotEmpty()) {

                //if the user from the signUp screen
                if (!fromLoginScreen) {
                    return Pair(false, Constants.ACCOUNT_FOUND_ERROR)
                }
                sharedPref.saveUser(existingUser)
                sharedPref.setLoginFromGoogle()
                //if he was in store
                if (existingUser.storeId.isNotEmpty()) {
                    fetchStore(existingUser.storeId)?.let { store ->
                        sharedPref.saveStore(store)
                    }
                }

                return Pair(true, c.GOOGLE_SIGN_SUCCESS_MESSAGE)
            }

            //case new user

            // check if  from login screen
            if (fromLoginScreen) {
                return Pair(false, Constants.SIGNUP_FIRST_ERROR)
            }

            // create new user based on the role
            val newUser = User(
                id = user.id,
                email = user.email ?: "",
                role = role,
                photoUrl = sharedPref.getProfileImage(),
                status = STATUS_PENDING,
                storeId = "",
                name = sharedPref.getUserName(),
                provider = GOOGLE_PROVIDER
            )

        //save the user
        supabase.from(USERS).insert(newUser)
        sharedPref.saveUser(newUser)
        sharedPref.setLoginFromGoogle()

        Pair(true, c.ACCOUNT_CREATED_MESSAGE)

    } catch (e: Exception)
    {
        Log.e("SignWithGoogle", "Error: ${e.message}", e)
        Pair(false,c.GOOGLE_SIGN_FAILED)
    }
}




    // =====================================================
    // REGISTER User with email (with cooldown)
    // =====================================================
    override fun registerOwner(
        email: String,
        password: String,
        name: String,
        role:Int,
        onResult: (Boolean, String) -> Unit
    ) {
        if(!networkHelper.isConnected()) {
            onResult(false, c.NO_INTERNET_CONNECTION)
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastSignupTime < SIGNUP_COOLDOWN) {
                val wait = ((SIGNUP_COOLDOWN - (currentTime - lastSignupTime)) / 1000).toInt()
                Log.w(TAG, "Please wait $wait seconds before another signup.")
                withContext(Dispatchers.Main) {
                    onResult(false,c.PLEASE_WAIT)
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
                    val newUser = User(
                        id = user.id,
                        name = name,
                        role = role,
                        photoUrl = "",
                        status = STATUS_PENDING,
                        storeId ="",
                        email = email,
                        provider = EMAIL_PROVIDER
                    )

                    supabase.from(USERS).insert(newUser)
                    sharedPref.saveUser(newUser)

                    lastSignupTime = System.currentTimeMillis()
                    withContext(Dispatchers.Main) {
                        onResult(true,c.SIGNUP_SUCCESS_MESSAGE)
                    }
                } else {
                    withContext(Dispatchers.Main) { onResult(false,c.REGISTER_FAILED) }
                }

            } catch (e: Exception) {
                Log.e(TAG, "registerOwner error: ${e.message}", e)
                withContext(Dispatchers.Main) { onResult(false,c.CHECK_YOU_INTERNET_CONNECTION) }
            }
        }
    }


    // =====================================================
    // LOGOUT
    // =====================================================
    override suspend fun logout(): Pair<Boolean,String> {
            return try {
                if(!networkHelper.isConnected()){
                    return Pair(false,c.NO_INTERNET_CONNECTION)
                }
                supabase.auth.signOut()
                 Pair(true,c.LOGOUT_SUCCESS_MESSAGE)
            } catch (e: Exception) {
                Log.e(TAG, "${c.LOGOUT_ERROR}: ${e.message}", e)
                 Pair(false,c.CHECK_YOU_INTERNET_CONNECTION)
            }
        }
    }
