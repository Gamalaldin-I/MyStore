@file:Suppress("DEPRECATION")

package com.example.htopstore.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.util.Constants
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityLoginBinding
import com.example.htopstore.ui.inbox.InboxActivity
import com.example.htopstore.ui.main.MainActivity
import com.example.htopstore.ui.signup.SignupActivity
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint

@Suppress("DEPRECATION")
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    companion object{
        const val GOOGLE_SIGN_IN = 9001
    }
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPref: SharedPref
    //private lateinit var googleSignInClient: GoogleSignInClient
    private val vm: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        //googleSignInClient = GoogleSignIn.getClient(this, gso)

        sharedPref = SharedPref(this)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setControllers()
        if (sharedPref.isLogin()) {
            if(sharedPref.getRole()== Constants.OWNER_ROLE){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)}
            else{
                val intent = Intent(this, InboxActivity::class.java)
                startActivity(intent)
            }
            finish()
        }
        vm.msg.observe(this){
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setControllers() {
        binding.loginBtn.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            vm.login(email, password) { success, msg ->
                if (success) {
                    if(sharedPref.getRole()== Constants.OWNER_ROLE){
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    else{
                        startActivity(Intent(this, InboxActivity::class.java))
                    }
            finish()
                }
                else {
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            }}
        //}
        /*binding.googleSignInBtn.setOnClickListener {
            val googleIntent = googleSignInClient.signInIntent
            startActivityForResult(googleIntent, GOOGLE_SIGN_IN)
        }*/
        binding.signupTv.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.forgotPassword.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            vm.resetPassword(email){
            }
        }
    }

    /*@Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                vm.loginWithGoogle(account.idToken!!,{
                    val intent = Intent(this, SignupActivity::class.java)
                    startActivity(intent)
                    finish()
                }){
                    if(sharedPref.getRole()== Constants.OWNER_ROLE){
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    else{
                        startActivity(Intent(this, InboxActivity::class.java))
                    }
                    finish()
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }*/

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
    }

}