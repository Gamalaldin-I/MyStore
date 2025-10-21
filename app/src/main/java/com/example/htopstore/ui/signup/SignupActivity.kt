package com.example.htopstore.ui.signup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.domain.util.Constants.OWNER_ROLE
import com.example.htopstore.databinding.ActivitySignupBinding
import com.example.htopstore.ui.login.LoginActivity
import com.example.htopstore.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@Suppress("DEPRECATION")
@AndroidEntryPoint
class SignupActivity : AppCompatActivity() {
    private var step =-1
    private lateinit var binding: ActivitySignupBinding
    private lateinit var roleSelectionFragment: RoleSelectionFragment
    private lateinit var userFormFragment: UserFormFragment
    private lateinit var storeFormFragment: StoreFormFragment
    private lateinit var sendRequestFragment: SendRequestFragment
    private val vm: SignupViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vm.msg.observe(this){
            msg->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
        vm.isLogin.observe(this){
            isLogin->
            if (isLogin){
                startActivity(Intent(this, MainActivity::class.java))
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
        sendRequestFragment = SendRequestFragment.newInstance()
    }
    private fun setSteps(){

        roleSelectionFragment.setOnNext {it->
            vm.afterRoleSelection(it){
            addFragment(userFormFragment)}
        }

        userFormFragment.setOnNext {
            name,email,password->
            vm.afterUserFormFill(name,email,password){
            val role = vm.getRole()
            if (role == OWNER_ROLE){
                addFragment(storeFormFragment)
            }
            else{
                addFragment(sendRequestFragment)}
            }
        }
        storeFormFragment.setOnNext {
            name,location,phone->
            vm.afterStoreFormFill(name,location,phone){
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        sendRequestFragment.setOnNext {
            code->
            vm.afterSendCode(code){}
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

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (step == 0) {
            finish()
        } else {
            backFragment()
        }
    }
}