package com.example.htopstore.ui.staff

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.domain.model.User
import com.example.domain.util.Constants
import com.example.domain.util.Constants.STATUS_HIRED
import com.example.htopstore.databinding.FragmentEmployeesBinding
import com.example.htopstore.ui.emlpoyee.EmployeeActivity
import com.example.htopstore.util.adapters.EmployeeAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Fragment for displaying and managing store employees.
 * Features: Search, filter by role/status, hire/fire actions
 */
class EmployeesFragment : Fragment() {

    private var _binding: FragmentEmployeesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StaffViewModel by activityViewModels()
    private lateinit var adapter: EmployeeAdapter

    // State management
    private val searchQuery = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow(FilterType.ALL)
    private var allEmployees = emptyList<User>()
    private var isFilterVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployeesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchBar()
        setupFilterChips()
        observeEmployees()
        observeLoadingState()
        observeSearchAndFilter()

        // Initial data load
        viewModel.getEmployees()
    }

    /**
     * Setup RecyclerView with adapter
     */
    private fun setupRecyclerView() {
        adapter = EmployeeAdapter(onFireOrHire = {
            employee, shouldFire ->
            employee.id.let { employeeId ->
                viewModel.hireOrFire(employeeId, shouldFire)
            }
        }){ emp->
            val intent = Intent(requireContext(), EmployeeActivity::class.java)
            intent.putExtra("employee_id", emp.id)
            intent.putExtra("employee_name", emp.name)
            intent.putExtra("employee_email", emp.email)
            intent.putExtra("employee_role", emp.role)
            intent.putExtra("employee_status", emp.status)
            intent.putExtra("employee_photo_url", emp.photoUrl)
            startActivity(intent)
            requireActivity().finish()
        }
        binding.recyclerView.adapter = adapter
    }

    /**
     * Setup search functionality
     */
    private fun setupSearchBar() {
        binding.searchEditText.addTextChangedListener { text ->
            searchQuery.value = text?.toString()?.trim().orEmpty()
        }
    }

    /**
     * Setup filter chips and toggle button
     */
    private fun setupFilterChips() {
        // Toggle filter visibility
        binding.filterBtn.setOnClickListener {
            isFilterVisible = !isFilterVisible
            binding.filterChipsScroll.visibility =
                if (isFilterVisible) View.VISIBLE else View.GONE
        }

        // Filter chip listeners
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedFilter.value = FilterType.ALL
        }

        binding.chipActive.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedFilter.value = FilterType.ACTIVE
        }

        binding.chipManagers.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedFilter.value = FilterType.MANAGERS
        }

        binding.chipStaff.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedFilter.value = FilterType.STAFF
        }
    }

    /**
     * Observe employees list changes
     */
    private fun observeEmployees() {
        viewModel.employees.observe(viewLifecycleOwner) { employeeList ->
            allEmployees = employeeList

            if (employeeList.isEmpty()) {
                showEmptyState(true)
                updateEmployeeCount(0)
            } else {
                showEmptyState(false)
                applyFilters(searchQuery.value, selectedFilter.value)
            }
        }
    }

    /**
     * Observe loading state
     */
    private fun observeLoadingState() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            toggleLoadingState(isLoading)
        }
    }

    /**
     * Observe search query and filter changes
     */
    private fun observeSearchAndFilter() {
        viewLifecycleOwner.lifecycleScope.launch {
            combine(searchQuery, selectedFilter) { query, filter ->
                Pair(query, filter)
            }.collect { (query, filter) ->
                applyFilters(query, filter)
            }
        }
    }

    /**
     * Apply search and filter logic to employee list
     */
    private fun applyFilters(query: String, filter: FilterType) {
        var filteredEmployees = allEmployees

        // Apply role/status filter
        filteredEmployees = when (filter) {
            FilterType.ALL -> filteredEmployees
            FilterType.ACTIVE -> filteredEmployees.filter {
                it.status == STATUS_HIRED
            }
            FilterType.MANAGERS -> filteredEmployees.filter { employee ->
                getUserRoleName(employee.role)
                    ?.contains("Manager", ignoreCase = true) == true
            }
            FilterType.STAFF -> filteredEmployees.filter { employee ->
                val roleName = getUserRoleName(employee.role)
                roleName?.contains("Staff", ignoreCase = true) == true ||
                        roleName?.contains("Employee", ignoreCase = true) == true
            }
        }

        // Apply search query
        if (query.isNotEmpty()) {
            filteredEmployees = filteredEmployees.filter { employee ->
                employee.name?.contains(query, ignoreCase = true) == true ||
                        employee.email?.contains(query, ignoreCase = true) == true ||
                        getUserRoleName(employee.role)?.contains(query, ignoreCase = true) == true
            }
        }

        // Update UI
        adapter.submitList(filteredEmployees)
        updateEmployeeCount(filteredEmployees.size)

        // Handle empty filtered results
        val shouldShowEmptyState = filteredEmployees.isEmpty() && allEmployees.isNotEmpty()
        showEmptyState(shouldShowEmptyState, isSearchOrFilterResult = true)
    }

    /**
     * Get role name from role ID
     */
    private fun getUserRoleName(roleId:Int): String? {
        return Constants.getRoleName(roleId)
    }

    /**
     * Update employee count text
     */
    private fun updateEmployeeCount(count: Int) {
        binding.employeeCount.text = when (count) {
            0 -> "No members"
            1 -> "1 member"
            else -> "$count members"
        }
    }

    /**
     * Toggle empty state visibility
     */
    private fun showEmptyState(show: Boolean, isSearchOrFilterResult: Boolean = false) {
        binding.emptyStateLayout.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (show) View.GONE else View.VISIBLE

        // Optionally customize empty state message based on context
        // Access TextViews in emptyStateLayout to update message
    }

    /**
     * Toggle loading state
     */
    private fun toggleLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Filter types for employee list
     */
    enum class FilterType {
        ALL,
        ACTIVE,
        MANAGERS,
        STAFF
    }
}

