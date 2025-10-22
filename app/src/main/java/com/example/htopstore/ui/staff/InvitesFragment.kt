package com.example.htopstore.ui.staff

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.domain.model.remoteModels.Invite
import com.example.domain.util.IdGenerator
import com.example.htopstore.databinding.FragmentInvitesBinding
import com.example.htopstore.util.adapters.InvitesAdapter
import com.example.htopstore.util.helper.DialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@Suppress("DEPRECATION")
class InvitesFragment : Fragment() {
    private lateinit var adapter: InvitesAdapter
    private lateinit var binding: FragmentInvitesBinding
    private val vm: StaffViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentInvitesBinding.inflate(inflater, container, false)
        setupInvitesRecyclerView()
        observeChanges()
        //add invite()
        vm.msg.observe(viewLifecycleOwner){
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
        binding.createInvite.setOnClickListener {
            val emailTxt = binding.emailET.text.toString().trim()
            if(emailTxt.isNotEmpty()){
                //valid email pattern
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()) {
                    Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                vm.addInvite(
                    email = emailTxt,
                    code = IdGenerator.generateTimestampedId(7)
                ){
                    binding.emailET.text = null
                }
            }
            else{
                Toast.makeText(requireContext(), "Please enter an email", Toast.LENGTH_SHORT).show()
            }

        }
        return binding.root

    }
    fun setupInvitesRecyclerView() {
        adapter = InvitesAdapter(mutableListOf(),{
            DialogBuilder.showAlertDialog(
                context = requireContext(),
                title = "Delete Invite",
                message = "Are you sure you want to delete this invite?",
                positiveButton = "Delete",
                negativeButton = "Cancel",
                onConfirm = {
                    vm.deleteInvite(it){}
                },
                onCancel = {
                }
            )
        }){
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, it.code)
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }
        binding.recyclerView.adapter = adapter
    }

    fun observeChanges(){
        vm.getInvites()
        lifecycleScope.launchWhenStarted {
            vm.invites.collect { list ->
                if(list.isNotEmpty()){
                adapter.updateList(list as MutableList<Invite>)
            }
        }
    }
    }

}