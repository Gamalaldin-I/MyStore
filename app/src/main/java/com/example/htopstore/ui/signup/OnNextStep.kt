package com.example.htopstore.ui.signup

interface OnNextStep {
    fun afterRoleSelection(role: Int,nextAction:()->Unit)
    fun afterUserFormFill(name: String, email: String, password:String,nextAction:()->Unit)
    fun onSignWithGoogle(
        token: String,
        profileUrl:String,
        name:String,
        nextAction: () -> Unit
    )
    ////////////////////////////////

}