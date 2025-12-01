package com.example.htopstore.ui.createStore

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.domain.model.Plan
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityCreateStoreBinding
import com.example.htopstore.ui.login.LoginActivity
import com.example.htopstore.ui.main.MainActivity
import com.example.htopstore.util.adapters.CategoriesAdapter
import com.example.htopstore.util.helper.DialogBuilder
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateStoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateStoreBinding
    private val viewModel: CreateStoreViewModel by viewModels()
    private val adapter: CategoriesAdapter by lazy { CategoriesAdapter(viewModel.getCategories()){
        cat, onDeleteView ->
            DialogBuilder.showAlertDialog(
                context = this,
                title = cat,
                message = "Are you sure to delete $cat category",
                negativeButton = "Cancel",
                positiveButton = "Confirm",
                onConfirm = {
                    viewModel.deleteCategory(cat){
                        onDeleteView()
                    }},
                onCancel = {})
    }
    }

    private var selectedImageUri: Uri? = null
    private lateinit var selectedPlan: Plan
    private val isUpdateMode: Boolean by lazy {
        intent.getBooleanExtra(EXTRA_FROM_UPDATE, false)
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleImageSelection(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCreateStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupClickListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        manageActivityMode()
    }

    private fun setupUI() {
        if (isUpdateMode) {
            setupUpdateMode()
        } else {
            setupCreateMode()
        }
    }

    private fun setupCreateMode() {
        selectedPlan = viewModel.getFreePlan()
        //binding.storePlanSec.rbFree.isChecked = true
        //updatePlanCardStyling(binding.storePlanSec.planFree, true)
        binding.categoryLo.root.visibility = View.GONE


        viewModel.validateToMain {
            navigateToMain()
        }
    }

    private fun setupUpdateMode() {
        val store = viewModel.getStore()
        selectedPlan = viewModel.getFreePlan()

        binding.apply {
            // Hide elements not needed in update mode
            logoutBtn.visibility = View.GONE
            successMessage.visibility = View.GONE
            subtitleText.visibility = View.GONE
            categoryLo.root.visibility = View.VISIBLE
            //setupCategoryAdapter
            categoryLo.rvCategories.adapter = adapter

            // Update text labels
            btnCreateStore.text = getString(R.string.update_store)
            titleText.text = getString(R.string.update_store)

            // Populate form fields
            storeFormSec.etStoreName.setText(store.name)
            storeFormSec.etStorePhone.setText(store.phone.removePrefix("+20"))
            storeFormSec.etStoreAddress.setText(store.location)

            // Load store logo
            loadStoreLogo(store.logoUrl)

            // Set selected plan
            //selectPlan(store.plan)
        }
    }

    private fun loadStoreLogo(logoUrl: String?) {
        if (!logoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(logoUrl)
                .placeholder(R.drawable.nav_store)
                .error(R.drawable.nav_store)
                .into(binding.storeLogoSec.ivStoreLogo)

            binding.storeLogoSec.apply {
                ivStoreLogo.setPadding(0, 0, 0, 0)
                ivStoreLogo.clearColorFilter()
                uploadOverlay.visibility = View.GONE
                cameraIcon.visibility = View.GONE
            }
        }
    }

    private fun selectPlan(planName: String) {
        /*binding.storePlanSec.apply {
            when (planName) {
                getString(R.string.free_plan) -> {
                    rbFree.isChecked = true
                    selectedPlan = viewModel.getFreePlan()
                    updatePlanCardStyling(planFree, true)
                }
                getString(R.string.silver_plan) -> {
                    rbSilver.isChecked = true
                    selectedPlan = viewModel.getSilverPlan()
                    updatePlanCardStyling(planSilver, true)
                }
                getString(R.string.gold_plan) -> {
                    rbGold.isChecked = true
                    selectedPlan = viewModel.getGoldPLan()
                    updatePlanCardStyling(planGold, true)
                }
                getString(R.string.platinum) -> {
                    rbPlatinum.isChecked = true
                    selectedPlan = viewModel.getPlatinumPlan()
                    updatePlanCardStyling(planPlatinum, true)
                }
                else -> {
                    rbFree.isChecked = true
                    selectedPlan = viewModel.getFreePlan()
                    updatePlanCardStyling(planFree, true)
                }
            }
        }*/
    }

    private fun setupClickListeners() {
        binding.apply {
            // Logo upload listeners
            storeLogoSec.btnUploadLogo.setOnClickListener {
                imagePickerLauncher.launch("image/*")
            }

            storeLogoSec.logoImageCard.setOnClickListener {
                imagePickerLauncher.launch("image/*")
            }

            // Logout button
            logoutBtn.setOnClickListener {
                handleLogout()
            }

            // Plan selection
            //setupPlanSelection()

            // Submit button
            btnCreateStore.setOnClickListener {
                handleSubmit()
            }
            categoryLo.btnAddCategory.setOnClickListener {
                val cat = categoryLo.etCategoryName.text.toString()
                if(cat.isNotEmpty()){
                    viewModel.addCategory(cat.trim()){
                        adapter.insertNew(cat.trim())
                        categoryLo.etCategoryName.clearFocus()
                        categoryLo.etCategoryName.setText("")
                    }
                }
            }

        }
    }

    private fun setupPlanSelection() {
        /*binding.storePlanSec.apply {
            val planItems = listOf(
                PlanItem(planFree, rbFree, viewModel.getFreePlan()),
                PlanItem(planSilver, rbSilver, viewModel.getSilverPlan()),
                PlanItem(planGold, rbGold, viewModel.getGoldPLan()),
                PlanItem(planPlatinum, rbPlatinum, viewModel.getPlatinumPlan())
            )

            planItems.forEach { item ->
                item.card.setOnClickListener {
                    selectPlanItem(item, planItems)
                }

                item.radioButton.setOnClickListener {
                    item.card.performClick()
                }
            }
        }*/
    }

    private fun selectPlanItem(selected: PlanItem, allItems: List<PlanItem>) {
        // Update selection
        allItems.forEach { it.radioButton.isChecked = false }
        selected.radioButton.isChecked = true
        selectedPlan = selected.plan

        // Update styling
        allItems.forEach {
            updatePlanCardStyling(it.card, it == selected)
        }
    }

    private fun updatePlanCardStyling(card: MaterialCardView, isSelected: Boolean) {
        if (isSelected) {
            card.strokeWidth = 6
            card.strokeColor = getColor(R.color.action_primary)
            card.cardElevation = 8f
        } else {
            card.strokeWidth = 2
            card.strokeColor = getColor(R.color.input_border_focus)
            card.cardElevation = 2f
        }
    }

    private fun handleImageSelection(uri: Uri) {
        selectedImageUri = uri
        binding.storeLogoSec.apply {
            ivStoreLogo.setImageURI(uri)
            ivStoreLogo.setPadding(0, 0, 0, 0)
            ivStoreLogo.clearColorFilter()
            uploadOverlay.visibility = View.GONE
            cameraIcon.visibility = View.GONE
        }
    }

    private fun handleLogout() {
        viewModel.logout { success, message ->
            if (success) {
                navigateToLogin()
            } else {
                showToast(message)
            }
        }
    }

    private fun handleSubmit() {
        val storeName = binding.storeFormSec.etStoreName.text.toString().trim()
        val storePhone = binding.storeFormSec.etStorePhone.text.toString().trim()
        val storeAddress = binding.storeFormSec.etStoreAddress.text.toString().trim()

        if (!validateInputs(storeName, storePhone, storeAddress)) {
            return
        }

        val storeData = StoreData(
            name = storeName,
            phone = "+20$storePhone",
            address = storeAddress,
            logoUri = selectedImageUri
        )

        if (isUpdateMode) {
            updateStore(storeData)
        } else {
            createStore(storeData)
        }
    }

    private fun createStore(storeData: StoreData) {
        showLoading(getString(R.string.creating_your_store))

        viewModel.createStore(storeData, selectedPlan) { success, message ->
            hideLoading()
            if (success) {
                viewModel.addCategory("General"){
                    showToast(getString(R.string.store_created_successfully))
                    navigateToMain()
                }
            } else {
                showToast(message)
            }
        }
    }

    private fun updateStore(storeData: StoreData) {
        showLoading(getString(R.string.updating_store))

        viewModel.updateStore(storeData, selectedPlan) { success, message ->
            hideLoading()
            if (success) {
                showToast(getString(R.string.update_store_data))
                finish()
            } else {
                showToast(message)
            }
        }
    }

    private fun validateInputs(name: String, phone: String, address: String): Boolean {
        var isValid = true

        binding.storeFormSec.apply {
            // Validate store name
            when {
                name.isEmpty() -> {
                    storeNameLo.error = getString(R.string.store_name_required)
                    isValid = false
                }
                name.length < MIN_NAME_LENGTH -> {
                    storeNameLo.error = getString(R.string.store_name_min_length, MIN_NAME_LENGTH)
                    isValid = false
                }
                else -> storeNameLo.error = null
            }

            // Validate phone
            when {
                phone.isEmpty() -> {
                    storePhoneLo.error = getString(R.string.phone_required)
                    isValid = false
                }
                phone.length != PHONE_LENGTH -> {
                    storePhoneLo.error = getString(R.string.phone_length, PHONE_LENGTH)
                    isValid = false
                }
                !phone.matches(PHONE_REGEX) -> {
                    storePhoneLo.error = getString(R.string.phone_digits_only)
                    isValid = false
                }
                else -> storePhoneLo.error = null
            }

            // Validate address
            when {
                address.isEmpty() -> {
                    storeLocationLo.error = getString(R.string.address_required)
                    isValid = false
                }
                address.length < MIN_ADDRESS_LENGTH -> {
                    storeLocationLo.error = getString(R.string.address_min_length, MIN_ADDRESS_LENGTH)
                    isValid = false
                }
                else -> storeLocationLo.error = null
            }
        }

        return isValid
    }

    private fun observeViewModel() {
        viewModel.message.observe(this) { message ->
            showToast(message)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (!isLoading) hideLoading()
        }
    }

    private fun showLoading(message: String) {
        binding.apply {
            loadingOverlay.visibility = View.VISIBLE
            loadingText.text = message
            //setInteractiveElementsEnabled(false)
        }
    }

    private fun hideLoading() {
        binding.apply {
            loadingOverlay.visibility = View.GONE
            //setInteractiveElementsEnabled(true)
        }
    }

    private fun ActivityCreateStoreBinding.setInteractiveElementsEnabled(enabled: Boolean) {
        btnCreateStore.isEnabled = enabled
        storeLogoSec.btnUploadLogo.isEnabled = enabled
        storeFormSec.etStoreName.isEnabled = enabled
        storeFormSec.etStorePhone.isEnabled = enabled
        storeFormSec.etStoreAddress.isEnabled = enabled

       /* storePlanSec.apply {
            planFree.isClickable = enabled
            planSilver.isClickable = enabled
            planGold.isClickable = enabled
            planPlatinum.isClickable = enabled
        }*/
    }

    private fun manageActivityMode() {
        if (!isUpdateMode) {
            viewModel.validateToMain {
                navigateToMain()
            }
        }
    }

    private fun navigateToMain() {
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }
        finish()
    }

    private fun navigateToLogin() {
        Intent(this, LoginActivity::class.java).apply {
            startActivity(this)
        }
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private data class PlanItem(
        val card: MaterialCardView,
        val radioButton: RadioButton,
        val plan: Plan
    )

    companion object {
        const val EXTRA_FROM_UPDATE = "fromUpdate"

        private const val MIN_NAME_LENGTH = 3
        private const val MIN_ADDRESS_LENGTH = 5
        private const val PHONE_LENGTH = 10
        private val PHONE_REGEX = Regex("^[0-9]+$")
    }
}

data class StoreData(
    val name: String,
    val phone: String,
    val address: String,
    val logoUri: Uri?
)