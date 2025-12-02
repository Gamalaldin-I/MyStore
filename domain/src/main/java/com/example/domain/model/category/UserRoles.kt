package com.example.domain.model.category

import com.example.domain.util.Constants.ADMIN_ROLE
import com.example.domain.util.Constants.CASHIER_ROLE
import com.example.domain.util.Constants.EMPLOYEE_ROLE
import com.example.domain.util.Constants.OWNER_ROLE
import com.example.domain.util.Constants.PARTNER_ROLE

enum class UserRoles(
    val role: Int,
    val englishName: String,
    val arabicName: String
) {
    Manager(
        OWNER_ROLE,
        "Owner",
        "مالك"
    ),
    Admin(
        ADMIN_ROLE,
        "Admin",
        "مسؤول"
    ),
    Partner(
        PARTNER_ROLE,
        "Partner",
        "شريك"
    ),
    Cashier(
        CASHIER_ROLE,
        "Cashier",
        "كاشير"
    ),
    Employee(
        EMPLOYEE_ROLE,
        "Employee",
        "موظف"
    );
}
