package com.example.htopstore.ui.staff

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.htopstore.R
import com.example.htopstore.databinding.ActivityStaffBinding
import com.example.htopstore.util.BaseActivity
import com.example.htopstore.util.adapters.ViewPagerAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for managing store staff and invitations.
 * Provides two tabs: Employees and Invitations
 */
@AndroidEntryPoint
class StaffActivity : BaseActivity(){

    private lateinit var binding: ActivityStaffBinding
    private val viewModel: StaffViewModel by viewModels()

    // Tab Configuration
    private companion object {
        const val EMPLOYEES_TAB_INDEX = 0
        const val INVITATIONS_TAB_INDEX = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupViewPager()
        observeMessages()
    }

    /**
     * Setup edge-to-edge display with proper window insets
     */
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }
    }

    /**
     * Setup ViewPager with fragments and TabLayout
     */
    private fun setupViewPager() {
        val fragments = listOf(
            EmployeesFragment(),
            InvitesFragment()
        )

        val adapter = ViewPagerAdapter(this, fragments)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                EMPLOYEES_TAB_INDEX -> getString(R.string.members)
                INVITATIONS_TAB_INDEX -> getString(R.string.invitations)
                else -> ""
            }
        }.attach()
    }

    /**
     * Observe ViewModel for global messages
     */
    private fun observeMessages() {
        viewModel.msg.observe(this) { message ->
            if (message.isNotEmpty()) {
                showSnackbar(message)
            }
        }
    }
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onNetworkRestored() {
        super.onNetworkRestored()
        viewModel.getEmployees()
        viewModel.getAllStoreInvites()
    }
}
