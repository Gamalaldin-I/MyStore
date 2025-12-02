package com.example.domain.util

import com.example.domain.model.category.UserRoles
import java.util.Locale

object Constants {


    /////////////////////////////////////////////////
    ////////////////////shared////////////////////////
    /////////////////////////////////////////////////
    //status
    const val STATUS_PENDING="Pending"
    const val SUCCESS = "Success"
    const val FAILURE ="Failure"
    //errors
    const val NO_INTERNET_CONNECTION = "No internet connection"
    const val CHECK_YOU_INTERNET_CONNECTION = "Check you internet connection!"



    /////////////////////////////////////////////////
    ////////////////////Roles////////////////////////
    /////////////////////////////////////////////////
    const val OWNER_ROLE = 0
    const val PARTNER_ROLE = 1
    const val ADMIN_ROLE = 2
    const val CASHIER_ROLE = 3
    const val EMPLOYEE_ROLE = 4

    private fun getTheEnglishNameOfRole(role:Int):String{
        return UserRoles.entries.find { it.role == role }?.englishName ?: "Unknown"
    }
    private fun getArabicNameOfRole(role:Int):String{
        return UserRoles.entries.find { it.role == role }?.arabicName ?: "غير معرف"
    }
    fun getRoleName(role:Int):String{
        return when(Locale.getDefault().language){
            "ar" -> getArabicNameOfRole(role)
            else -> getTheEnglishNameOfRole(role)
        }
    }

    /////////////////////////////////////////////////
    ////////////////////STATUS///////////////////////
    /////////////////////////////////////////////////


    //for employee status
    const val STATUS_HIRED="Hired"
    const val STATUS_FIRED="Fired"

    //for invitation status
    const val STATUS_ACCEPTED="Accepted"
    const val STATUS_REJECTED="Rejected"

    //for sell pending actions
    const val STATUS_APPROVED="Approved"

    /////////////////////////////////////////////////
    ////////////////////Sign in & register///////////
    ////////////////////////////////////////////////

    //providers
    const val GOOGLE_PROVIDER = "Google"
    const val EMAIL_PROVIDER = "Email"
    //errors
    const val SIGNUP_FIRST_ERROR = "sign up first please"
    const val ACCOUNT_FOUND_ERROR = "There is another account by this email"
    const val LOGIN_FAILED = "Login failed"
    const val REGISTER_FAILED = "Register failed"
    const val LOGOUT_ERROR = "Logout error"
    const val USER_NOT_FOUND = "User not found"

    //google errors
    const val PLEASE_WAIT = "Please wait seconds before trying again."
    const val GOOGLE_SIGN_FAILED = "Google sign-in failed: No user returned"

    //success messages
    const val SIGNUP_SUCCESS_MESSAGE = "Sign up successfully"
    const val LOGIN_SUCCESS_MESSAGE = "Login successfully"
    const val LOGOUT_SUCCESS_MESSAGE = "Logout successfully"
    const val GOOGLE_SIGN_SUCCESS_MESSAGE = "Google Sign-in successful"
    const val ACCOUNT_CREATED_MESSAGE = "Account created successfully"

    //////////////////////////////////////////////////
    ////////////////////Sell and pending actions/////
    ////////////////////////////////////////////////
    const val SELL_COMPLETED_MESSAGE = "Sale completed successfully"
    const val SELL_FAILED_MESSAGE = "Pending action created for retry later"



    /////////////////////////////////////////////////
    ////////////////////expenses////////////////////
    ////////////////////////////////////////////////
    const val EXPENSE_ADDED_MESSAGE = "Expense added successfully"
    const val EXPENSE_DELETED_MESSAGE = "Expense deleted successfully"
    const val EXPENSES_FETCHED ="Expenses fetched successfully"
    const val EXPENSE_ADDED_FAILED_MESSAGE = "Expense added failed"
    const val EXPENSE_DELETED_FAILED_MESSAGE = "Expense deleted failed"






}