package com.example.htopstore.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.htopstore.databinding.FragmentStoreFormBinding

class StoreFormFragment private constructor() : Fragment() {

    private lateinit var binding: FragmentStoreFormBinding
    private val viewModel: SignupViewModel by activityViewModels()

    private var onNextStepCallback: ((name: String, location: String, phone: String) -> Unit)? = null

    companion object {
        /**
         * Factory method to create a new instance of StoreFormFragment.
         * @return A new instance of StoreFormFragment
         */
        fun newInstance(): StoreFormFragment {
            return StoreFormFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStoreFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    /**
     * Sets up click listeners for the UI components.
     */
    private fun setupListeners() {
        binding.finishBtn.setOnClickListener {
            validateAndSubmitStoreData()
        }
    }

    /**
     * Validates all store form fields and proceeds if valid.
     * Extracts data from input fields, validates them, and triggers the next step callback.
     */
    private fun validateAndSubmitStoreData() {
        val storeName = binding.nameEt.text.toString().trim()
        val storeLocation = binding.locationEt.text.toString().trim()
        val phoneNumber = binding.phoneEt.text.toString().trim()

        // Add Egyptian phone prefix if not present
        val formattedPhone = if (!phoneNumber.startsWith("+20")) {
            "+20$phoneNumber"
        } else {
            phoneNumber
        }

        if (viewModel.isStoreDataValid(storeName, storeLocation, formattedPhone)) {
            onNextStepCallback?.invoke(storeName, storeLocation, formattedPhone)
        }
    }

    /**
     * Sets the callback to be invoked when store data is validated successfully.
     * @param callback Lambda function that receives store name, location, and phone number
     */
    fun setOnNextStep(callback: (name: String, location: String, phone: String) -> Unit) {
        onNextStepCallback = callback
    }

}