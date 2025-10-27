package com.example.htopstore.util

object DataValidator{

    private const val STR_WORD = "Strong"

    fun String.validEmail():Boolean{
        return AuthChecker.isValidEmail(this)
    }
    private fun String.validPasswordLength():Boolean{
        return AuthChecker.isPasswordLengthValid(this)
    }
    fun String.validPasswordMatch(password:String):Boolean{
        return (this==password)
    }
    private fun String.validStrongPassword():String{
        return AuthChecker.getPasswordStrength(this)
    }
    fun String.isValidName():Boolean{
        return AuthChecker.isValidName(this)
    }
    fun String.validPhone():Boolean{
        return AuthChecker.isValidPhoneNumber(this)
    }
    fun String.validSSN():Boolean {
        return AuthChecker.isValidSSN(this)
    }
    private fun String.strongPassword():Boolean{
        return (this.validStrongPassword() == STR_WORD || this.validStrongPassword() =="Medium")
    }
    fun String.validPassword():Boolean{
        return this.validPasswordLength() && this.strongPassword()
    }

}