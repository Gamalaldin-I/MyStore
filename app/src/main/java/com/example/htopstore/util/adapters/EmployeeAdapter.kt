package com.example.htopstore.util.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.domain.model.User
import com.example.domain.model.category.UserRoles
import com.example.domain.util.Constants.STATUS_HIRED
import com.example.htopstore.R
import com.example.htopstore.databinding.StaffMemberCardBinding
import com.example.htopstore.util.helper.DialogBuilder
import java.util.Locale

class EmployeeAdapter(
    private val onFireOrHire: (employee: User, fire: Boolean) -> Unit
) : ListAdapter<User, EmployeeAdapter.EHolder>(DiffCallback()) {

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
            // Employee basic info
            employeeName.text = employee.name ?: "Unknown"
            employeeEmail.text = employee.email ?: "No email"

            // Role
            val role = UserRoles.entries.find { it.role == employee.role }
            employeeRole.text = role?.roleName ?: "Unknown Role"

            // Status chip
            val isHired = employee.status == STATUS_HIRED
            empStatusChip.apply {
                text = if (isHired) "Active" else "Inactive"
                setChipBackgroundColorResource(
                    if (isHired) R.color.primary_accent_blue else R.color.alert_critical
                )
                setTextColor(context.getColor(android.R.color.white))
            }
            Glide.with(employeeAvatar.context)
                .load(employee.photoUrl)
                .placeholder(R.drawable.icon_profile)
                .error(R.drawable.icon_profile)
                .into(employeeAvatar)

            // Click listener for status change
            empStatusChip.setOnClickListener {
                val fire = isHired
                val actionText = if (fire) "fire" else "hire"
                val titleText = actionText.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                }

                DialogBuilder.showAlertDialog(
                    context = holder.itemView.context,
                    title = titleText,
                    message = "Are you sure you want to $actionText ${employee.name}?",
                    positiveButton = "Yes",
                    negativeButton = "Cancel",
                    onConfirm = { onFireOrHire(employee, fire) },
                    onCancel = {}
                )
            }

            // Item click listener (optional - for viewing employee details)
            root.setOnClickListener {
                // TODO: Navigate to employee detail screen
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem
    }
}