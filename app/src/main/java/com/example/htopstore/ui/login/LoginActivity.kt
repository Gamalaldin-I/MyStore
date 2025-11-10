@file:Suppress("DEPRECATION")

package com.example.htopstore.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.util.Constants
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityLoginBinding
import com.example.htopstore.ui.createStore.CreateStoreActivity
import com.example.htopstore.ui.inbox.InboxActivity
import com.example.htopstore.ui.signup.SignupActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPref: SharedPref
    private lateinit var googleSignInClient: GoogleSignInClient
    private val vm: LoginViewModel by viewModels()

    // Modern activity result launcher replacing deprecated onActivityResult
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleGoogleSignInResult(result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize binding and SharedPreferences
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPref = SharedPref(this)

        // Check if user is already logged in
        if (checkExistingLogin()) return

        // Setup Google Sign-In
        setupGoogleSignIn()

        // Setup UI listeners
        setupClickListeners()

        // Observe ViewModel messages
        observeViewModel()
    }

    private fun checkExistingLogin(): Boolean {
        if (sharedPref.isLogin()) {
            navigateToHomeScreen()
            return true
        }
        return false
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_auth))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        binding.apply {
            loginBtn.setOnClickListener { handleEmailLogin() }
            googleSignInBtn.setOnClickListener { handleGoogleSignIn() }
            signupTv.setOnClickListener { navigateToSignup() }
            forgotPassword.setOnClickListener { handleForgotPassword() }
        }
    }

    private fun observeViewModel() {
        vm.msg.observe(this) { message ->
            showToast(getLocalizedMessage(message))
        }
    }

    // === Login Handlers ===

    private fun handleEmailLogin() {
        val email = binding.emailEt.text.toString().trim()
        val password = binding.passwordEt.text.toString().trim()

        // Validate inputs
        if (!validateInputs(email, password)) return

        // Show loading
        showLoading(getString(R.string.signing_in_progress))

        vm.login(email, password) { success, message ->
            hideLoading()
            if (success) {
                navigateToHomeScreen()
            } else {
                showToast(getLocalizedMessage(message))
            }
        }
    }

    private fun handleGoogleSignIn() {
        showLoading(getString(R.string.connecting_to_google))
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            // Save user info
            account?.let {
                sharedPref.setUserName(it.displayName ?: "")
                sharedPref.setProfileImage(it.photoUrl?.toString() ?: "")

                showLoading(getString(R.string.signing_in_progress))

                vm.loginWithGoogle(
                    idToken = it.idToken!!,
                    goToSign = {
                        hideLoading()
                        navigateToSignup()
                    },
                    onRes = {
                        hideLoading()
                        navigateToHomeScreen()
                    }
                )
            }
        } catch (_: ApiException) {
            hideLoading()
            showToast(getString(R.string.google_signin_failed))
        }
    }

    private fun handleForgotPassword() {
        val email = binding.emailEt.text.toString().trim()

        if (email.isEmpty()) {
            binding.emailLo.error = getString(R.string.enter_email_first)
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLo.error = getString(R.string.invalid_email)
            return
        }

        showLoading(getString(R.string.sending_reset_link))

        vm.resetPassword(email) {
            hideLoading()
            // ViewModel will handle the message via LiveData
        }
    }

    // === Validation ===

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.emailLo.error = getString(R.string.email_required)
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLo.error = getString(R.string.invalid_email)
            isValid = false
        } else {
            binding.emailLo.error = null
        }

        if (password.isEmpty()) {
            binding.passwordLo.error = getString(R.string.password_required)
            isValid = false
        } else if (password.length < 6) {
            binding.passwordLo.error = getString(R.string.password_too_short)
            isValid = false
        } else {
            binding.passwordLo.error = null
        }

        return isValid
    }

    // === Navigation ===

    private fun navigateToHomeScreen() {
        val intent = when (sharedPref.getRole()) {
            Constants.OWNER_ROLE -> Intent(this, CreateStoreActivity::class.java)
            else -> Intent(this, InboxActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToSignup() {
        startActivity(Intent(this, SignupActivity::class.java))
        finish()
    }

    // === Loading State ===

    private fun showLoading(message: String) {
        binding.apply {
            loadingOverlay.visibility = View.VISIBLE
            loadingText.text = message

            // Disable all interactive elements
            loginBtn.isEnabled = false
            googleSignInBtn.isEnabled = false
            signupTv.isClickable = false
            forgotPassword.isClickable = false
            emailEt.isEnabled = false
            passwordEt.isEnabled = false
        }
    }

    private fun hideLoading() {
        binding.apply {
            loadingOverlay.visibility = View.GONE

            // Re-enable all interactive elements
            loginBtn.isEnabled = true
            googleSignInBtn.isEnabled = true
            signupTv.isClickable = true
            forgotPassword.isClickable = true
            emailEt.isEnabled = true
            passwordEt.isEnabled = true
        }
    }

    // === Localization ===

    private fun getLocalizedMessage(message: String): String {
        return when (message) {
            "Login successful" -> getString(R.string.login_successful)
            "Login failed" -> getString(R.string.login_failed)
            "User not found" -> getString(R.string.user_not_found)
            "login error" -> getString(R.string.login_error)
            "Google sign-in failed: No user returned" -> getString(R.string.google_signin_failed)
            "Google Sign-in successful" -> getString(R.string.google_signin_successful)
            "Account created successfully" -> getString(R.string.account_created_successfully)
            "Google sign-in error" -> getString(R.string.google_signin_error)
            "Owner registered successfully" -> getString(R.string.owner_registered_successfully)
            "Registration failed" -> getString(R.string.registration_failed)
            "Registration error" -> getString(R.string.registration_error)
            "Employee registered successfully" -> getString(R.string.employee_registered_successfully)
            "Employee registration failed" -> getString(R.string.employee_registration_failed)
            "Employee registration error" -> getString(R.string.employee_registration_error)
            "Logout successful" -> getString(R.string.logout_successful)
            "Logout error" -> getString(R.string.logout_error)
            "Please wait seconds before trying again." -> getString(R.string.please_wait_before_signup)
            else -> message
        }
    }

    // === Utilities ===

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in favor of OnBackPressedDispatcher")
    override fun onBackPressed() {
        // Prevent back navigation on login screen
    }
}