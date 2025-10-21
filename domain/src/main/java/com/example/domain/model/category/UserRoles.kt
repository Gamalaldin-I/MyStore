package com.example.domain.model.category

import com.example.domain.util.Constants.ADMIN_ROLE
import com.example.domain.util.Constants.CASHIER_ROLE
import com.example.domain.util.Constants.OWNER_ROLE


enum class UserRoles(val role:Int, val roleName:String){
    Manager(OWNER_ROLE,"Owner"),
    Admin(ADMIN_ROLE,"Admin"),
    Cashier(CASHIER_ROLE,"Cashier")
}