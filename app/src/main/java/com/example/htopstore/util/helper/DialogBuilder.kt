package com.example.htopstore.util.helper

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.domain.model.Expense
import com.example.domain.model.SoldProduct
import com.example.domain.model.User
import com.example.domain.model.category.UserRoles
import com.example.domain.util.DateHelper
import com.example.domain.util.IdGenerator
import com.example.htopstore.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DialogBuilder {
    var expenseDetailsDialog : Dialog? = null
    var returnDialog : Dialog? = null

    fun showAlertDialog(
        context: Context,
        message: String,
        title: String,
        positiveButton: String,
        negativeButton: String,
        onConfirm: () -> Unit,
        onCancel: () -> Unit,
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setCancelable(true)
        builder.setTitle(title)

        builder.setPositiveButton(positiveButton) { dialog, _ ->
            onConfirm()
            dialog.dismiss()
        }
        builder.setNegativeButton(negativeButton) { dialog, _ ->
            onCancel()
            dialog.dismiss()

        }
        val dialog = builder.create()

        dialog.show()

    }
    fun showExpensesDetailsDialog(expense: Expense,user:User,context: Context){
        if (expenseDetailsDialog != null) {
            expenseDetailsDialog?.dismiss()
        }
        this.expenseDetailsDialog = Dialog(context)
        this.expenseDetailsDialog?.setContentView(R.layout.reason_details_dialog)
        this.expenseDetailsDialog?.setCancelable(true)
        // set the views
        val reason = this.expenseDetailsDialog?.findViewById<TextView>(R.id.dDescription)
        val name = this.expenseDetailsDialog?.findViewById<TextView>(R.id.employeeName)
        val role = this.expenseDetailsDialog?.findViewById<TextView>(R.id.employeeRole)
        val avatar = this.expenseDetailsDialog?.findViewById<ImageView>(R.id.employeeAvatar)
        // set the data
        reason?.text = expense.description
        name?.text = user.name
        role?.text = UserRoles.entries[user.role].name
        Glide.with(context)
            .load(user.photoUrl)
            .placeholder(R.drawable.ic_camera)
            .error(R.drawable.ic_camera)
            .into(avatar!!)
        // show the dialog
        this.expenseDetailsDialog?.show()
    }


    @SuppressLint("SetTextI18n")
    fun showReturnDialog(context: Context, soldProduct: SoldProduct, onConfirm: (soldProduct: SoldProduct) -> Unit) {
        // init
        if (returnDialog != null) {
            returnDialog?.dismiss()
        }
        this.returnDialog = Dialog(context)
        this.returnDialog?.setContentView(R.layout.return_dialog)
        this.returnDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        this.returnDialog?.setCancelable(true)

        // inflate the views
        val confirmBtn = this.returnDialog?.findViewById<MaterialButton>(R.id.confirm)
        val cancelBtn = this.returnDialog?.findViewById<MaterialButton>(R.id.cancel)
        val increaseBtn = this.returnDialog?.findViewById<MaterialButton>(R.id.increaseBtn)
        val decreaseBtn = this.returnDialog?.findViewById<MaterialButton>(R.id.decreaseBtn)
        val quantityDisplay = this.returnDialog?.findViewById<TextView>(R.id.quantityDisplay)
        val productName = this.returnDialog?.findViewById<TextView>(R.id.name)
        val maxQuantityInfo = this.returnDialog?.findViewById<TextView>(R.id.maxQuantityInfo)

        // set product info
        productName?.text = soldProduct.name
        maxQuantityInfo?.text = "Max: ${soldProduct.quantity} items"

        // initialize quantity
        var currentQuantity = 1
        quantityDisplay?.text = currentQuantity.toString()

        // update button states
        fun updateButtonStates() {
            decreaseBtn?.isEnabled = currentQuantity > 1
            increaseBtn?.isEnabled = currentQuantity < soldProduct.quantity
        }

        updateButtonStates()

        // set the listeners
        increaseBtn?.setOnClickListener {
            if (currentQuantity < soldProduct.quantity) {
                currentQuantity++
                quantityDisplay?.text = currentQuantity.toString()
                updateButtonStates()
            }
        }

        decreaseBtn?.setOnClickListener {
            if (currentQuantity > 1) {
                currentQuantity--
                quantityDisplay?.text = currentQuantity.toString()
                updateButtonStates()
            }
        }

        cancelBtn?.setOnClickListener {
            hideReturnDialog()
        }

        confirmBtn?.setOnClickListener {
            val willBack = SoldProduct(
                productId = soldProduct.productId,
                billId = soldProduct.billId,
                quantity = currentQuantity,
                type = soldProduct.type,
                price = soldProduct.price,
                sellingPrice = soldProduct.sellingPrice,
                sellDate = DateHelper.getCurrentDate(),
                sellTime = DateHelper.getCurrentTime(),
                name = soldProduct.name,
                id = IdGenerator.generateTimestampedId(),
                lastUpdate = DateHelper.getCurrentTimestampTz(),
                deleted = false
            )
            onConfirm(willBack)
            hideReturnDialog()
        }

        returnDialog?.show()
    }
    fun hideReturnDialog(){
        this.returnDialog?.dismiss()
    }

     fun showSuccessDialog(context: Context, message: String, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Success")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onConfirm() // Close activity and return to previous screen
            }
            .setCancelable(false)
            .show()
    }

     fun showForgotPasswordDialog(
        context: Context,
        onConfirm: () -> Unit,
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Forgot Password?")
            .setMessage("Do you want to reset your password? You will receive a password reset link via email.")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                onConfirm()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }



}