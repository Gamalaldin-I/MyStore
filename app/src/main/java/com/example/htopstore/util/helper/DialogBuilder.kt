package com.example.htopstore.util.helper

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import com.example.domain.model.Expense
import com.example.domain.model.SoldProduct
import com.example.domain.util.DateHelper
import com.example.domain.util.IdGenerator
import com.example.htopstore.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

object DialogBuilder {
    var expenseDetailsDialog : Dialog? = null
    var addExpenseDialog : Dialog? = null
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
    fun showAddExpenseDialog(context: Context, onConfirm: (cat:String, des:String, amount:Double, method:String) -> Unit){
        //init
        if (addExpenseDialog != null) {
            addExpenseDialog?.dismiss()
        }
        this.addExpenseDialog = Dialog(context)
        this.addExpenseDialog?.setContentView(R.layout.take_new_expense_dialog)
        this.addExpenseDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        this.addExpenseDialog?.setCancelable(true)
        // inflate the views
        val categoryLo = this.addExpenseDialog?.findViewById<TextInputLayout>(R.id.categoryLo)
        val categoryEt = this.addExpenseDialog?.findViewById<AutoCompleteTextView>(R.id.categoryEt)
        val amountLo = this.addExpenseDialog?.findViewById<TextInputLayout>(R.id.amountLo)
        val amount = this.addExpenseDialog?.findViewById<TextInputEditText>(R.id.amountET)
        val desLo = this.addExpenseDialog?.findViewById<TextInputLayout>(R.id.desLo)
        val des = this.addExpenseDialog?.findViewById<TextInputEditText>(R.id.desET)
        val methodLo = this.addExpenseDialog?.findViewById<TextInputLayout>(R.id.methodLo)
        val method = this.addExpenseDialog?.findViewById<AutoCompleteTextView>(R.id.methodET)
        val confirmBtn = this.addExpenseDialog?.findViewById<Button>(R.id.confirmBtn)
        // set the adapters
        categoryEt?.setAdapter(AutoCompleteHelper.getExpenseCategoryAdapter(context))
        method?.setAdapter(AutoCompleteHelper.getPaymentMethodAdapter(context))
        // set the listeners

        confirmBtn?.setOnClickListener {
            val allISFilled = (amount?.text.toString().isNotEmpty() &&
                    des?.text.toString().isNotEmpty() &&
                    categoryEt?.text.toString().isNotEmpty() &&
                    method?.text.toString().isNotEmpty())
            if (allISFilled){
            onConfirm(
                categoryEt?.text.toString(),
                des?.text.toString(),
                amount?.text.toString().toDouble(),
                method?.text.toString()
            )
        }
            else {
                if(amount?.text.toString().isEmpty()){
                    amountLo?.error = "Amount is required"
                }
                if(des?.text.toString().isEmpty()){
                    desLo?.error = "Description is required"
                }
                if(categoryEt?.text.toString().isEmpty()){
                    categoryLo?.error = "Category is required"
                }
                if(method?.text.toString().isEmpty()){
                    methodLo?.error = "Method is required"
                }
            }

    }
    addExpenseDialog?.show()
    }
    fun hideExpenseDetailsDialog(){
        this.expenseDetailsDialog?.dismiss()
    }
    fun hideAddExpenseDialog(){
        this.addExpenseDialog?.dismiss()
    }

    fun showReturnDialog(context: Context, soldProduct: SoldProduct, onConfirm: (soldProduct: SoldProduct) -> Unit){
        //init
        if (returnDialog != null) {
            returnDialog?.dismiss()
        }
        this.returnDialog = Dialog(context)
        this.returnDialog?.setContentView(R.layout.return_dialog)
        this.returnDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        this.returnDialog?.setCancelable(true)
        // inflate the views
        val confirmBtn = this.returnDialog?.findViewById<Button>(R.id.confirm)
        val numberPicker = this.returnDialog?.findViewById<NumberPicker>(R.id.numberPicker)
        numberPicker?.minValue = 1
        numberPicker?.maxValue = soldProduct.quantity
        numberPicker?.value = 1
        // set the listeners
        confirmBtn?.setOnClickListener {
            val willBack = SoldProduct(
                productId = soldProduct.productId,
                saleId = soldProduct.saleId,
                quantity = numberPicker!!.value,
                type = soldProduct.type,
                price = soldProduct.price,
                sellingPrice = soldProduct.sellingPrice,
                sellDate = DateHelper.getCurrentDate(),
                sellTime = DateHelper.getCurrentTime(),
                name = soldProduct.name,
                detailId = IdGenerator.generateTimestampedId()
            )
            onConfirm(
                willBack
            )
            hideReturnDialog()
        }
        returnDialog?.show()
    }
    fun hideReturnDialog(){
        this.returnDialog?.dismiss()
    }



}