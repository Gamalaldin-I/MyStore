package com.example.htopstore.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.domain.util.Constants.CASHIER_ROLE
import com.example.domain.util.Constants.OWNER_ROLE
import com.example.htopstore.databinding.FragmentRoleSelectionBinding

class RoleSelectionFragment private constructor() : Fragment() {
    private lateinit var binding: FragmentRoleSelectionBinding
    private lateinit var oNextStep: (role: Int) -> Unit
    private lateinit var onLoginChoice: () -> Unit

    companion object {
        fun newInstance(): RoleSelectionFragment {
            return RoleSelectionFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRoleSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setControllers()
    }

    private fun setControllers() {
        binding.next.setOnClickListener {
            val role = when (binding.radioG.checkedRadioButtonId) {
                binding.owner.id -> OWNER_ROLE
                binding.employee.id -> CASHIER_ROLE
                else -> -1
            }
            if (::oNextStep.isInitialized) {
                oNextStep(role)
            }
        }
        binding.loginBtn.setOnClickListener {
            if (::onLoginChoice.isInitialized) {
                onLoginChoice()
            }
        }
    }

    fun setOnLogin(onLoginChoice: () -> Unit) {
        this.onLoginChoice = onLoginChoice
    }

    fun setOnNext(doThis: (role: Int) -> Unit) {
        oNextStep = doThis
    }
}