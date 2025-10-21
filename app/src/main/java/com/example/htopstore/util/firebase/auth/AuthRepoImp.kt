package com.example.htopstore.util.firebase.auth

import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.repo.AuthRepo
import com.example.domain.util.Constants.OWNER_ROLE
import com.example.htopstore.util.firebase.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepoImp(private val db: FirebaseFirestore,private val pref: SharedPref): AuthRepo {
    val auth = FirebaseAuth.getInstance()
    val fu = FirebaseUtils
    override fun login(
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
            val ownerId = it.user!!.uid
            db.collection(fu.OWNERS).document(ownerId).get().addOnSuccessListener {
                val name = it.get(fu.OWNER_NAME).toString()
                val email = it.get(fu.OWNER_EMAIL).toString()
                val role = it.get("role").toString().toInt()
                val storeId = it.get(fu.OWNER_STORE_ID).toString()
                db.collection(fu.STORES).document(storeId).get().addOnSuccessListener {
                    val storeName = it.get(fu.STORE_NAME).toString()
                    val storeLocation = it.get(fu.STORE_LOCATION).toString()
                    val storePhone = it.get(fu.STORE_PHONE).toString()
                    pref.setLogin()
                    pref.saveUser(
                        ownerId,
                        name,
                        role,
                        email,
                        password
                    )
                    pref.saveStore(
                        storeId,
                        storeName,
                        storePhone,
                        storeLocation)
                    onResult(true,"Success")
                }.addOnFailureListener {
                    onResult(false,it.message.toString())
                }
        }
        }
    }

    override fun registerOwner(
        email: String,
        password: String,
        name: String,
        storeName: String,
        storeLocation: String,
        storePhone: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener { res ->
            val ownerId = res.user!!.uid
            val storeId = "${storeName}${ownerId}"
            val owner = hashMapOf(
                fu.OWNER_NAME to name,
                fu.OWNER_EMAIL to email,
                fu.OWNER_PASSWORD to password,
                fu.OWNER_ID to ownerId,
                fu.OWNER_STORE_ID to storeId,
                fu.OWNER_ROLE to OWNER_ROLE,
                fu.OWNER_CREATED_AT to FieldValue.serverTimestamp())
            //add owner to owners collection
            db.collection(fu.OWNERS).document(ownerId).set(owner).addOnSuccessListener {
                //add store to stores collections
                val store = hashMapOf(
                    fu.STORE_NAME to storeName,
                    fu.STORE_LOCATION to storeLocation,
                    fu.STORE_PHONE to storePhone)
                db.collection(fu.STORES).document(storeId).set(store).addOnSuccessListener {
                    onResult(true,"Success")
                    //save data in shared pref
                    pref.setLogin()
                    pref.saveUser(
                        ownerId,
                        name,
                        OWNER_ROLE,
                        email,
                        password
                    )
                    pref.saveStore(
                        storeId,
                        storeName,
                        storePhone,
                        storeLocation)

                }.addOnFailureListener {
                    onResult(false,it.message.toString())
                }
            }.addOnFailureListener {
                onResult(false,it.message.toString())
            }
        }
    }

    override fun createInvite(
        storeId: String,
        email: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        TODO("Not yet implemented")
    }

    override fun registerEmployee(
        name: String,
        email: String,
        password: String,
        code: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        TODO("Not yet implemented")
    }

    override fun approveEmployee(
        uid: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        TODO("Not yet implemented")
    }

    override fun rejectEmployee(
        uid: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        TODO("Not yet implemented")
    }

    override fun logout(onResult: (Boolean, String) -> Unit) {
        auth.signOut()
        onResult(true,"Success")
    }


}