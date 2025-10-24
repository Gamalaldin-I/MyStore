package com.example.htopstore.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.util.Constants
import com.example.htopstore.databinding.ActivityLoginBinding
import com.example.htopstore.ui.inbox.InboxActivity
import com.example.htopstore.ui.main.MainActivity
import com.example.htopstore.ui.signup.SignupActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPref: SharedPref
    private val vm: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
            }
        }
        binding.signupTv.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}