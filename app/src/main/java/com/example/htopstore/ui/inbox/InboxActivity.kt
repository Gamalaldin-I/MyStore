package com.example.htopstore.ui.inbox

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.domain.model.remoteModels.Invite
import com.example.htopstore.databinding.ActivityInboxBinding
import com.example.htopstore.ui.login.LoginActivity
import com.example.htopstore.ui.main.MainActivity
import com.example.htopstore.util.adapters.InboxAdapter
import com.example.htopstore.util.helper.DialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@Suppress("DEPRECATION")
@AndroidEntryPoint
class InboxActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInboxBinding
    private lateinit var adapter: InboxAdapter
    private val vm: InboxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //check if the user is logged in
        vm.validToGoHome{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }


        vm.msg.observe(this){
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }


        //get all invites
        vm.getAllInvites()
        getAllTheInvitesThatPending()
        //setupAdapter
        adapter = InboxAdapter(mutableListOf(),{code,invite->
            vm.accept(
                invite = invite,
                code = code
            ){
                //handle go to the main
                startActivity(Intent(this,MainActivity::class.java))
            }
        }){ invite,position->
            //on reject show alert
            DialogBuilder.showAlertDialog(
                context = this,
                title = "Reject Invite",
                message = "Are you sure you want to reject this invite?",
                positiveButton = "Reject",
                negativeButton = "Cancel",
                onConfirm = {
                    vm.reject(invite){
                        //update the adapter
                        //adapter.deleteItem(position)
                    }
                },
                onCancel = {}
            )

        }

        binding.recycler.adapter = adapter
        binding.logout.setOnClickListener {
            vm.logout { success , msg ->
                if(success){
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                else{
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.myEmail.text = vm.getEmail()

    }
    fun getAllTheInvitesThatPending(){
        lifecycleScope.launchWhenStarted {
            vm.invites.collect{
                list->
                if(list.isNotEmpty()){
                adapter.update(list as MutableList<Invite>)
            }}
        }
    }

}