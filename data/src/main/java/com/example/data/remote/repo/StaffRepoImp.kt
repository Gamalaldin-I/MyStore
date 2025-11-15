package com.example.data.remote.repo

import android.util.Log
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.User
import com.example.domain.repo.StaffRepo
import com.example.domain.util.Constants.OWNER_ROLE
import com.example.domain.util.Constants.STATUS_FIRED
import com.example.domain.util.Constants.STATUS_HIRED
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class StaffRepoImp(
    private val supabase: SupabaseClient,
    private val pref: SharedPref
) : StaffRepo {

    companion object{
        private const val TAG = "StaffRepoImp"
        private const val USERS = "users"
        private const val STORE_ID = "storeId"
        private const val ROLE = "role"
        private const val USER_ID = "id"
        private const val STATUS = "status"
    }


    override suspend fun getEmployees(): Pair<List<User>, String> {
        return try{
            val storeId = pref.getStore().id
            val storeEmployees = supabase.from(USERS).select{
                filter {
                    eq(STORE_ID,storeId)
                    //get only employees
                    neq(ROLE,OWNER_ROLE)
                }
            }.decodeList<User>()
            Pair(storeEmployees,"")

                }catch (e: Exception){
            Log.d(TAG,"Error getting employees: ${e.message}")
            Pair(emptyList<User>(),"Error getting employees")
            }
    }


    override suspend fun fireOrRehireEmployee(
        employeeId: String,
        reject: Boolean
    ): Pair<Boolean, String> {
        return try{
            val status = if(reject) STATUS_FIRED else STATUS_HIRED
            supabase.from(USERS).update(
                mapOf(STATUS to status)
            ){
                filter {
                    eq(USER_ID,employeeId)
                }
            }
            Pair(true,"")
        }catch (e: Exception){
            Log.d(TAG,"Error firing or rehiring employee: ${e.message}")
            Pair(false,"Error firing or rehiring employee")
        }
    }

    override suspend fun preformAction(): Pair<Boolean, String> {
         try {
            val userId = pref.getUser().id
            val status = supabase.from(USERS).select{
                filter {
                    eq(USER_ID,userId)
                }
            }.decodeSingle<User>().status
            val result = when(status) {
                STATUS_HIRED -> {
                    Pair(true,STATUS_HIRED)
                }
                else -> {
                    Pair(false,status)
                }
            }
             return result
         }catch (e: Exception){
            Log.d(TAG,"Error getting user status: ${e.message}")
              return Pair(false,"Error getting user status")

            }

        }

}





