package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.category.UserRoles
import com.example.domain.model.remoteModels.StoreEmployee
import com.example.domain.util.Constants.STATUS_HIRED
import com.example.htopstore.R
import com.example.htopstore.databinding.StaffMemberCardBinding
import com.example.htopstore.util.helper.DialogBuilder
import java.util.Locale

class EmployeeAdapter(
    private val onFireOrHire: (employee: StoreEmployee, fire: Boolean) -> Unit
) : ListAdapter<StoreEmployee, EmployeeAdapter.EHolder>(DiffCallback()) {

    // ViewHolder
    class EHolder(val binding: StaffMemberCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EHolder {
        val binding = StaffMemberCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: EHolder, position: Int) {
        val employee = getItem(position)
        with(holder.binding) {
            employeeName.text = employee.name
            employeeEmail.text = employee.email
            employeeRole.text =
                UserRoles.entries.find { it.role == employee.role }?.roleName ?: "Unknown"

            val isHired = employee.status == STATUS_HIRED
            empStatusChip.apply {
                text = if (isHired) "Active" else "Inactive"
                setChipBackgroundColorResource(
                    if (isHired) R.color.primary_accent_blue else R.color.alert_critical
                )
            }

            empStatusChip.setOnClickListener {
                val fire = isHired
                val actionText = if (fire) "fire" else "hire"
                val titleText = if (fire) "fire" else "hire"

                DialogBuilder.showAlertDialog(
                    context = holder.itemView.context,
                    title = titleText.capitalize(Locale.ROOT),
                    message = "Are you sure you want to $actionText this employee?",
                    positiveButton = "Yes",
                    negativeButton = "No",
                    onConfirm = { onFireOrHire(employee, fire) },
                    onCancel = {}
                )
            }
        }
    }
    class DiffCallback : DiffUtil.ItemCallback<StoreEmployee>() {
        override fun areItemsTheSame(oldItem: StoreEmployee, newItem: StoreEmployee): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: StoreEmployee, newItem: StoreEmployee): Boolean =
            oldItem == newItem
    }

}
