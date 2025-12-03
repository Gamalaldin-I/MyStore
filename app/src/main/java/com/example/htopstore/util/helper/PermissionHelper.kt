package com.example.htopstore.util.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.domain.util.Constants

object PermissionHelper {
    val c= Constants
    fun allPermissionsGranted(context: Context) =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    fun canViewProduct(r:Int): Boolean{
        return (r!=c.EMPLOYEE_ROLE && r!=c.CASHIER_ROLE)
    }
    fun isAdmin(r:Int): Boolean{
        //if he was owner, partner or admin
        return (r!=c.EMPLOYEE_ROLE && r!=c.CASHIER_ROLE)
    }
    fun isCashier(r:Int): Boolean{
        return (r==c.CASHIER_ROLE)
    }
}