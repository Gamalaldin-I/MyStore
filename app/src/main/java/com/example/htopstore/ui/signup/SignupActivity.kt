@file:Suppress("DEPRECATION")

package com.example.htopstore.ui.signup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.example.domain.util.Constants.OWNER_ROLE
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivitySignupBinding
import com.example.htopstore.ui.createStore.CreateStoreActivity
import com.example.htopstore.ui.inbox.InboxActivity
import com.example.htopstore.ui.login.LoginActivity
import com.example.htopstore.ui.main.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint

@Suppress("DEPRECATION")
@AndroidEntryPoint
class SignupActivity : AppCompatActivity() {

    companion object {
        private const val GOOGLE_SIGN_IN_CODE = 1000
        private const val SPLASH_DURATION = 3000L

        // Animation durations
        private const val LOGO_ANIMATION_DURATION = 800L
        private const val TEXT_ANIMATION_DURATION = 1000L
        private const val SPLASH_FADE_OUT_DURATION = 600L
    }

    private lateinit var binding: ActivitySignupBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val viewModel: SignupViewModel by viewModels()

    private var currentFragmentStep = -1
    private var navigationAction = ""

    private lateinit var roleSelectionFragment: RoleSelectionFragment
    private lateinit var userFormFragment: UserFormFragment

    // ==================== Lifecycle Methods ====================

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeUI()
        observeViewModel()
        showSplashScreen()
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Use OnBackPressedDispatcher")
    override fun onBackPressed() {
        if (currentFragmentStep > 0) {
            handleBackNavigation()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN_CODE) {
            handleGoogleSignInResult(data)
        }
    }

    // ==================== Initialization ====================

    private fun initializeUI() {
        binding.backArrow.visibility = View.GONE
        viewModel.goto()
    }

    private fun initializeFragments() {
        roleSelectionFragment = RoleSelectionFragment.newInstance()
        userFormFragment = UserFormFragment.newInstance()
    }

    private fun setupGoogleSignIn() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_auth))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }

    // ==================== Splash Screen ====================

    private fun showSplashScreen() {
        with(binding) {
            splashOverlay.visibility = View.VISIBLE
            splashOverlay.alpha = 1f
        }

        animateSplashElements()
        scheduleSplashDismissal()
    }

    private fun animateSplashElements() {
        animateLogo()
        animateAppName()
        animateTagline()
    }

    private fun animateLogo() {
        binding.appLogo.apply {
            scaleX = 0.5f
            scaleY = 0.5f
            alpha = 0f

            animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(LOGO_ANIMATION_DURATION)
                .setInterpolator(OvershootInterpolator(1.2f))
                .start()
        }
    }

    private fun animateAppName() {
        binding.appName.apply {
            alpha = 0f
            translationY = 30f

            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(TEXT_ANIMATION_DURATION)
                .setStartDelay(200)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun animateTagline() {
        binding.tagline.apply {
            alpha = 0f
            translationY = 30f

            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(TEXT_ANIMATION_DURATION)
                .setStartDelay(400)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun scheduleSplashDismissal() {
        Handler(Looper.getMainLooper()).postDelayed({
            hideSplash()
        }, SPLASH_DURATION)
    }

    private fun hideSplash() {
        binding.splashOverlay.animate()
            .alpha(0f)
            .setDuration(SPLASH_FADE_OUT_DURATION)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                binding.splashOverlay.visibility = View.GONE
                onSplashComplete()
            }
            .start()
    }

    private fun onSplashComplete() {
        if (navigationAction.isNotEmpty()) {
            navigateToDestination(navigationAction)
        } else {
            initializeApp()
        }
    }

    private fun initializeApp() {
        setupGoogleSignIn()
        initializeFragments()
        setupNavigation()
        addFragment(roleSelectionFragment)
    }

    // ==================== ViewModel Observation ====================

    private fun observeViewModel() {
        observeMessages()
        observeNavigation()
        observeLoadingState()
    }

    private fun observeMessages() {
        viewModel.message.observe(this) { message ->
            val localizedMessage = getLocalizedMessage(message)
            Toast.makeText(this, localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeNavigation() {
        viewModel.goTo.observe(this) { destination ->
            navigationAction = destination
        }
    }

    private fun observeLoadingState() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }
    }

    // ==================== Navigation ====================

    private fun setupNavigation() {
        setupRoleSelectionNavigation()
        setupUserFormNavigation()
        setupBackNavigation()
    }

    private fun setupRoleSelectionNavigation() {
        roleSelectionFragment.setOnNext { selectedRole ->
            viewModel.afterRoleSelection(selectedRole) {
                addFragment(userFormFragment)
            }
        }

        roleSelectionFragment.setOnLogin {
            navigateToLogin()
        }
    }

    private fun setupUserFormNavigation() {
        userFormFragment.setOnNext { name, email, password ->
            handleUserFormSubmission(name, email, password)
        }

        userFormFragment.setONSignWithGoogle {
            initiateGoogleSignIn()
        }
    }

    private fun setupBackNavigation() {
        binding.backArrow.setOnClickListener {
            handleBackNavigation()
        }
    }

    private fun handleUserFormSubmission(name: String, email: String, password: String) {
        showLoading(true, getString(R.string.creating_account))

        viewModel.afterUserFormFill(name, email, password) {
            showLoading(false)
            navigateToMainScreen()
        }
    }

    private fun navigateToDestination(destination: String) {
        val intent = when (destination) {
            SignupViewModel.CREATE_STORE_ACTIVITY -> Intent(this, CreateStoreActivity::class.java)
            SignupViewModel.INBOX_ACTIVITY -> Intent(this, InboxActivity::class.java)
            SignupViewModel.MAIN_ACTIVITY -> Intent(this, MainActivity::class.java)
            else -> return
        }

        startActivity(intent)
        finish()
    }

    private fun navigateToMainScreen() {
        val destinationClass = if (viewModel.getRole() == OWNER_ROLE) {
            CreateStoreActivity::class.java
        } else {
            InboxActivity::class.java
        }

        startActivity(Intent(this, destinationClass))
        finish()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun handleBackNavigation() {
        currentFragmentStep--

        binding.backArrow.visibility = if (currentFragmentStep == 0) {
            View.GONE
        } else {
            View.VISIBLE
        }

        supportFragmentManager.popBackStack()
    }

    // ==================== Fragment Management ====================

    @SuppressLint("CommitTransaction")
    private fun addFragment(fragment: Fragment) {
        currentFragmentStep++

        binding.backArrow.visibility = if (currentFragmentStep >= 1) {
            View.VISIBLE
        } else {
            View.GONE
        }

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(binding.frameLayout11.id, fragment)
            .addToBackStack(null)
            .commit()
    }

    // ==================== Google Sign-In ====================

    private fun initiateGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_CODE)
    }

    private fun handleGoogleSignInResult(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)

        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken?:""
            val photoUrl = account.photoUrl?.toString() ?: ""
            val displayName = account.displayName ?: ""

            showLoading(true, getString(R.string.signing_in_with_google))

            viewModel.onSignWithGoogle(idToken, photoUrl, displayName) {
                showLoading(false)
                navigateToMainScreen()
            }

        } catch (exception: ApiException) {
            showLoading(false)
            handleGoogleSignInError(exception)
        }
    }

    private fun handleGoogleSignInError(exception: ApiException) {
        val errorMessage = "${getString(R.string.google_signin_failed)}: ${exception.message}"
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    // ==================== UI State ====================

    private fun showLoading(show: Boolean, message: String = getString(R.string.please_wait)) {
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE

        if (show) {
            binding.loadingText.text = message
        }
    }

    // ==================== Localization ====================

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
}