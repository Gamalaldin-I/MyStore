package com.example.htopstore.util.helper

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.widget.TextView
import com.example.domain.model.Expense
import com.example.domain.model.SoldProduct
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
    fun showExpensesDetailsDialog(expense: Expense, context: Context){
        if (expenseDetailsDialog != null) {
            expenseDetailsDialog?.dismiss()
        }
        this.expenseDetailsDialog = Dialog(context)
        this.expenseDetailsDialog?.setContentView(R.layout.reason_details_dialog)
        this.expenseDetailsDialog?.setCancelable(true)
        // set the views
        val reason = this.expenseDetailsDialog?.findViewById<TextView>(R.id.dDescription)
        val date = this.expenseDetailsDialog?.findViewById<TextView>(R.id.dDate)
        val amount = this.expenseDetailsDialog?.findViewById<TextView>(R.id.dTotal)
        val method = this.expenseDetailsDialog?.findViewById<TextView>(R.id.dMethod)
        val time = this.expenseDetailsDialog?.findViewById<TextView>(R.id.dTime)
        val category = this.expenseDetailsDialog?.findViewById<TextView>(R.id.dCategory)
        // set the data
        reason?.text = expense.description
        date?.text = expense.date
        amount?.text = expense.amount.toString()
        method?.text = expense.paymentMethod
        time?.text = expense.time
        category?.text = expense.category
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
                saleId = soldProduct.saleId,
                quantity = currentQuantity,
                type = soldProduct.type,
                price = soldProduct.price,
                sellingPrice = soldProduct.sellingPrice,
                sellDate = DateHelper.getCurrentDate(),
                sellTime = DateHelper.getCurrentTime(),
                name = soldProduct.name,
                detailId = IdGenerator.generateTimestampedId(),
                lastUpdate = "${DateHelper.getCurrentDate()}/${DateHelper.getCurrentTime()}"
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