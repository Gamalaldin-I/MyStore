package com.example.domain.repo

import com.example.domain.model.User

interface StaffRepo {
    // for owner
    suspend fun getEmployees():Pair<List<User>,String>
    // for employee
    suspend fun fireOrRehireEmployee(employeeId:String,reject:Boolean):Pair<Boolean,String>
    suspend fun preformAction(): Pair<Boolean,String>
    suspend fun changeRoleOfEmployee(newRole:Int,empId:String):Pair<Boolean,String>



}