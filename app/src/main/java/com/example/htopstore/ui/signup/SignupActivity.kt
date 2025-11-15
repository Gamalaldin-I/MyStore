package com.example.htopstore.ui.signup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
        const val GOOGLE_CODE = 1000
    }

    private lateinit var binding: ActivitySignupBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val vm: SignupViewModel by viewModels()

    private var step = -1
    private lateinit var roleSelectionFragment: RoleSelectionFragment
    private lateinit var userFormFragment: UserFormFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleSignIn()
        observeViewModel()
        initializeFragments()
        setupNavigation()

        binding.backArrow.visibility = View.GONE
        addFragment(roleSelectionFragment)
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_auth))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun observeViewModel() {
        vm.goto()
        vm.message.observe(this) { msg ->
            val localizedMessage = getLocalizedMessage(msg)
            Toast.makeText(this, localizedMessage, Toast.LENGTH_SHORT).show()
        }

        vm.goTo.observe(this) { act ->
            when(act){
                SignupViewModel.CREATE_STORE_ACTIVITY -> {
                    startActivity(Intent(this, CreateStoreActivity::class.java))
                }
                SignupViewModel.INBOX_ACTIVITY -> {
                    startActivity(Intent(this, InboxActivity::class.java))
                }
                else -> {
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
        }

        vm.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }
    }

    private fun getLocalizedMessage(msg: String): String {
        return when (msg) {
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
            else -> msg
        }
    }

    private fun initializeFragments() {
        roleSelectionFragment = RoleSelectionFragment.newInstance()
        userFormFragment = UserFormFragment.newInstance()
    }

    private fun setupNavigation() {
        roleSelectionFragment.setOnNext { role ->
            vm.afterRoleSelection(role) {
                addFragment(userFormFragment)
            }
        }

        userFormFragment.setOnNext { name, email, password ->
            showLoading(true, getString(R.string.creating_account))
            vm.afterUserFormFill(name, email, password) {
                showLoading(false)
                navigateToMainScreen()
            }
        }

        userFormFragment.setONSignWithGoogle {
            val googleIntent = googleSignInClient.signInIntent
            startActivityForResult(googleIntent, GOOGLE_CODE)
        }


        roleSelectionFragment.setOnLogin {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.backArrow.setOnClickListener {
            handleBackNavigation()
        }
    }

    private fun showLoading(show: Boolean, message: String = getString(R.string.please_wait)) {
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.loadingText.text = message
        }
    }

    private fun navigateToMainScreen() {
        val intent = if (vm.getRole() == OWNER_ROLE) {
            Intent(this, CreateStoreActivity::class.java)
        } else {
            Intent(this, InboxActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun handleBackNavigation() {
        step--
        if (step == 0) {
            binding.backArrow.visibility = View.GONE
        }
        supportFragmentManager.popBackStack()
    }

    @SuppressLint("CommitTransaction")
    private fun addFragment(fragment: Fragment) {
        step++
        if (step == 1) {
            binding.backArrow.visibility = View.VISIBLE
        }
        supportFragmentManager.beginTransaction()
            .replace(binding.frameLayout11.id, fragment)
            .addToBackStack(null)
            .commit()
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Use OnBackPressedDispatcher")
    override fun onBackPressed() {
        if (step != 0) {
            handleBackNavigation()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val photoUrl = account.photoUrl.toString()
                val name = account.displayName.toString()

                showLoading(true, getString(R.string.signing_in_with_google))
                vm.onSignWithGoogle(account.idToken!!, photoUrl, name) {
                    showLoading(false)
                    navigateToMainScreen()
                }
            } catch (e: ApiException) {
                showLoading(false)
                val errorMsg = "${getString(R.string.google_signin_failed)}: ${e.message}"
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}