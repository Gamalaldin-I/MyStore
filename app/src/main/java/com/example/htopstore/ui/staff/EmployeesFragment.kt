
package com.example.htopstore.ui.staff

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.domain.model.category.UserRoles
import com.example.domain.model.remoteModels.StoreEmployee
import com.example.domain.util.Constants.STATUS_HIRED
import com.example.htopstore.databinding.FragmentEmployeesBinding
import com.example.htopstore.util.adapters.EmployeeAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class EmployeesFragment : Fragment() {

    private var _binding: FragmentEmployeesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: EmployeeAdapter
    private val vm: StaffViewModel by activityViewModels()

    private val searchQuery = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow(FilterType.ALL)
    private var allEmployees = listOf<StoreEmployee>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployeesBinding.inflate(inflater, container, false)

        setupEmployeesRecyclerView()
        setupSearchAndFilter()
        observeChanges()

        return binding.root
    }

    private fun setupEmployeesRecyclerView() {
        adapter = EmployeeAdapter { employee, fire ->
            vm.hireOrFire(employee.id!!, fire)
        }
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearchAndFilter() {

        // Search functionality
        binding.searchEditText.addTextChangedListener { text ->
            searchQuery.value = text?.toString()?.trim() ?: ""
        }

        // Filter button - toggle chips visibility
        var filterVisible = false
        binding.filterBtn.setOnClickListener {
            filterVisible = !filterVisible
            binding.filterChipsScroll.visibility = if (filterVisible) View.VISIBLE else View.GONE
        }

        // Filter chips
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

        // Combine search and filter
        viewLifecycleOwner.lifecycleScope.launch {
            combine(searchQuery, selectedFilter) { query, filter ->
                Pair(query, filter)
            }.collect { (query, filter) ->
                filterEmployees(query, filter)
            }
        }
    }

    private fun observeChanges() {
        showLoading(true)
        vm.getEmployees()

        viewLifecycleOwner.lifecycleScope.launch {
            vm.employees.collect { list ->
                showLoading(false)
                allEmployees = list

                if (list.isEmpty()) {
                    showEmptyState(true)
                    updateEmployeeCount(0)
                } else {
                    showEmptyState(false)
                    filterEmployees(searchQuery.value, selectedFilter.value)
                }
            }
        }
    }

    private fun filterEmployees(query: String, filter: FilterType) {
        var filteredList = allEmployees

        // Apply role/status filter
        filteredList = when (filter) {
            FilterType.ALL -> filteredList
            FilterType.ACTIVE -> filteredList.filter { it.status == STATUS_HIRED }
            FilterType.MANAGERS -> filteredList.filter {
                UserRoles.entries.find { role -> role.role == it.role }?.roleName?.contains("Manager", ignoreCase = true) == true
            }
            FilterType.STAFF -> filteredList.filter {
                val roleName = UserRoles.entries.find { role -> role.role == it.role }?.roleName
                roleName?.contains("Staff", ignoreCase = true) == true ||
                        roleName?.contains("Employee", ignoreCase = true) == true
            }
        }

        // Apply search query
        if (query.isNotEmpty()) {
            filteredList = filteredList.filter { employee ->
                employee.name?.contains(query, ignoreCase = true) == true ||
                        employee.email?.contains(query, ignoreCase = true) == true ||
                        UserRoles.entries.find { it.role == employee.role }?.roleName?.contains(query, ignoreCase = true) == true
            }
        }

        adapter.submitList(filteredList)
        updateEmployeeCount(filteredList.size)

        // Show empty state if filtered list is empty but we have employees
        if (filteredList.isEmpty() && allEmployees.isNotEmpty()) {
            showEmptyState(true, isSearchResult = true)
        } else if (filteredList.isNotEmpty()) {
            showEmptyState(false)
        }
    }

    private fun updateEmployeeCount(count: Int) {
        binding.employeeCount.text = when (count) {
            0 -> "No members"
            1 -> "1 member"
            else -> "$count members"
        }
    }

    private fun showEmptyState(show: Boolean, isSearchResult: Boolean = false) {
        binding.emptyStateLayout.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (show) View.GONE else View.VISIBLE

        // You can customize empty state message based on whether it's a search result
        // by accessing the TextViews in emptyStateLayout
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class FilterType {
        ALL, ACTIVE, MANAGERS, STAFF
    }
}