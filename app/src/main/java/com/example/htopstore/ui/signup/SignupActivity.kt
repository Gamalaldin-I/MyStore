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
    private lateinit var googleSignInClient: GoogleSignInClient


    companion object{
        const val GOOGLE_CODE = 1000
    }


    private var step =-1
    private lateinit var binding: ActivitySignupBinding
    private lateinit var roleSelectionFragment: RoleSelectionFragment
    private lateinit var userFormFragment: UserFormFragment
    private lateinit var storeFormFragment: StoreFormFragment
    private val vm: SignupViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vm.message.observe(this){
            msg->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
        vm.isLoggedIn.observe(this){
            isLogin->
            if (isLogin){
                if(vm.getRole()==OWNER_ROLE){
                startActivity(Intent(this, MainActivity::class.java))}
                else{
                    startActivity(Intent(this, InboxActivity::class.java))
                }
                finish()
            }
        }
        setFragments()
        addFragment(roleSelectionFragment)

        setSteps()
        binding.backArrow.visibility = View.GONE


    }
    private fun setFragments(){
        roleSelectionFragment = RoleSelectionFragment.newInstance()
        userFormFragment = UserFormFragment.newInstance()
        storeFormFragment = StoreFormFragment.newInstance()
    }
    private fun setSteps(){

        roleSelectionFragment.setOnNext {it->
            vm.afterRoleSelection(it){
                if (it == OWNER_ROLE)addFragment(storeFormFragment)
                    else addFragment(userFormFragment)

            }
        }

        userFormFragment.setOnNext {
            name,email,password->
            vm.afterUserFormFill(name,email,password){
                val role = vm.getRole()
                if (role == OWNER_ROLE){
                    startActivity(Intent(this, MainActivity::class.java))
                }
                else{
                    startActivity(Intent(this, InboxActivity::class.java))
                }
                finish()
            }
        }
        userFormFragment.setONSignWithGoogle {
            val googleIntent = googleSignInClient.signInIntent
                startActivityForResult(googleIntent, GOOGLE_CODE)
        }
        storeFormFragment.setOnNextStep {
            name,location,phone->
            vm.afterStoreFormFill(name,location,phone){
                addFragment(userFormFragment)
            }
        }

        binding.backArrow.setOnClickListener {
            backFragment()
        }
        roleSelectionFragment.setOnLogin {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }
    private fun backFragment(){
        step-=1
        if(step == 0){
            binding.backArrow.visibility = View.GONE
        }
        supportFragmentManager.popBackStack()
    }
    @SuppressLint("CommitTransaction")
    private fun addFragment(fragment: Fragment){
        step+=1
        if(step == 1){
            binding.backArrow.visibility = View.VISIBLE
        }
        supportFragmentManager.beginTransaction()
            .replace(binding.frameLayout11.id, fragment)
            .addToBackStack(null)
            .commit()
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (step != 0) {
            backFragment()
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                vm.onSignWithGoogle(account.idToken!!){
                    val role = vm.getRole()
                    if (role == OWNER_ROLE){
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
    }

}