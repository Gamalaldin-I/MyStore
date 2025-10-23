package com.example.htopstore.util.firebase.auth

import android.util.Log
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.remoteModels.Employee
import com.example.domain.repo.AuthRepo
import com.example.domain.util.Constants
import com.example.domain.util.Constants.EMPLOYEE_ROLE
import com.example.domain.util.Constants.OWNER_ROLE
import com.example.domain.util.Constants.STATUS_HIRED
import com.example.domain.util.DateHelper
import com.example.htopstore.util.firebase.FirebaseUtils
import com.example.htopstore.util.firebase.Mapper.hash
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthRepoImp(
    private val db: FirebaseFirestore,
    private val pref: SharedPref,
) : AuthRepo {
    private val _employeeStatus = MutableStateFlow<String>(STATUS_HIRED)
    override val employeeStatus: StateFlow<String> = _employeeStatus

    //empStoreIdListener
    var employeeStatusListener: ListenerRegistration? = null


    private val auth = FirebaseAuth.getInstance()
    private val fu = FirebaseUtils

    // ---------------------------------------------------------
    // 🔹 LOGIN FUNCTION
    // ---------------------------------------------------------
    override fun login(
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                // getting the role to determine the next step
                val userId = authResult.user?.uid
                if (userId == null) {
                    onResult(false, "Error: User ID not found")
                    return@addOnSuccessListener
                }

                db.collection("users").document(userId).get()
                    .addOnSuccessListener { snapshot ->
                        val role = snapshot.getLong("role")?.toInt()
                        if (role == null) {
                            onResult(false, "Error: User role not found")
                            return@addOnSuccessListener
                        }

                        manageUser(role, userId, email, password) { success, msg ->
                            onResult(success, msg)
                        }
                    }
                    .addOnFailureListener {
                        onResult(false, it.message ?: "Error fetching user role")
                    }
            }
            .addOnFailureListener {
                onResult(false, it.message ?: "Invalid email or password")
            }
    }

    // ---------------------------------------------------------
    // 🔹 MANAGE USER DATA (OWNER / EMPLOYEE)
    // ---------------------------------------------------------
    private fun manageUser(
        role: Int,
        userId: String,
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        /** document
         * if the user is owner (get the store data and save all)
         * if the user is employee (save the employee(user data) first and then get the store data
         * if he was hired in a store before )
         * */
        if (role == OWNER_ROLE) {
            // -------------------- OWNER --------------------
            db.collection(fu.OWNERS).document(userId).get()
                .addOnSuccessListener { snapshot ->
                    val name = snapshot.getString(fu.OWNER_NAME) ?: "Owner"
                    val storeId = snapshot.getString(fu.OWNER_STORE_ID)
                        ?: return@addOnSuccessListener onResult(false, "Owner store ID not found")

                    getStoreData(userId, storeId) { success, msg ->
                        if (success) {
                            pref.saveUser(userId, name, role, email, password)
                            onResult(true, "Owner logged in successfully")
                        } else {
                            onResult(false, msg)
                        }
                    }
                }
                .addOnFailureListener {
                    onResult(false, it.message ?: "Error fetching owner data")
                }

        } else {
            // -------------------- EMPLOYEE --------------------
            db.collection(fu.EMPLOYEES).document(userId).get()
                .addOnSuccessListener { snapshot ->
                    val name = snapshot.getString("name") ?: "Employee"
                    val storeId = snapshot.getString("storeId")
                    val ownerId = snapshot.getString("ownerId")
                    val status = snapshot.getString("status") ?: Constants.STATUS_PENDING

                    pref.saveUser(userId, name, role, email, password)

                    if (status != STATUS_HIRED) {
                        pref.saveStore(
                            id = "",
                            name = "",
                            phone = "",
                            location = "",
                            ownerId = ""
                        )
                        onResult(true, "Logged in successfully, but you are not yet linked to any store.")
                        return@addOnSuccessListener
                    }
                    getStoreData(ownerId!!, storeId!!) { success, msg ->
                        if (success) {
                            onResult(true, "Employee logged in successfully")
                        } else {
                            onResult(false, msg)
                        }
                    }
                }
                .addOnFailureListener {
                    onResult(false, it.message ?: "Error fetching employee data")
                }
        }

        }

    // ---------------------------------------------------------
    // 🔹 GET STORE DATA (SHARED BETWEEN OWNER / EMPLOYEE)
    // ---------------------------------------------------------
    private fun getStoreData(
        ownerId: String,
        storeId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        db.collection(fu.OWNERS).document(ownerId)
            .collection(fu.STORES).document(storeId)
            .get()
            .addOnSuccessListener { snapshot ->
                val storeName = snapshot.getString(fu.STORE_NAME)
                val storeLocation = snapshot.getString(fu.STORE_LOCATION)
                val storePhone = snapshot.getString(fu.STORE_PHONE)

                if (storeName == null || storeLocation == null || storePhone == null) {
                    onResult(false, "Incomplete store data")
                    return@addOnSuccessListener
                }

                pref.saveStore(
                    id = storeId,
                    name = storeName,
                    phone = storePhone,
                    location = storeLocation,
                    ownerId = ownerId
                )

                onResult(true, "Store data loaded successfully")
            }
            .addOnFailureListener {
                onResult(false, it.message ?: "Error fetching store data")
            }
    }

    // ---------------------------------------------------------
    // 🔹 REGISTER OWNER
    // ---------------------------------------------------------
    override fun registerOwner(
        email: String,
        password: String,
        name: String,
        storeName: String,
        storeLocation: String,
        storePhone: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { res ->
                val ownerId = res.user!!.uid
                val storeId = "${storeName}${ownerId}"
                val owner = hashMapOf(
                    fu.OWNER_NAME to name,
                    fu.OWNER_EMAIL to email,
                    fu.OWNER_PASSWORD to password,
                    fu.OWNER_ID to ownerId,
                    fu.OWNER_STORE_ID to storeId,
                    fu.OWNER_ROLE to OWNER_ROLE,
                    fu.OWNER_CREATED_AT to FieldValue.serverTimestamp()
                )

                // Save base user record
                db.collection("users").document(ownerId).set(
                    hashMapOf(
                        "role" to OWNER_ROLE,
                        "name" to name,
                        "email" to email
                    )
                )

                // Check if already exists
                db.collection(fu.OWNERS).whereEqualTo(fu.OWNER_EMAIL, email).get()
                    .addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.isEmpty) {
                            db.collection(fu.OWNERS).document(ownerId).set(owner)
                                .addOnSuccessListener {
                                    val store = hashMapOf(
                                        fu.STORE_NAME to storeName,
                                        fu.STORE_LOCATION to storeLocation,
                                        fu.STORE_PHONE to storePhone
                                    )

                                    db.collection(fu.OWNERS).document(ownerId)
                                        .collection(fu.STORES).document(storeId).set(store)
                                        .addOnSuccessListener {
                                            pref.saveUser(ownerId, name, OWNER_ROLE, email, password)
                                            pref.saveStore(storeId, storeName, storePhone, storeLocation, ownerId)
                                            onResult(true, "Owner registered successfully")
                                        }
                                        .addOnFailureListener {
                                            onResult(false, it.message ?: "Failed to create store")
                                        }
                                }
                                .addOnFailureListener {
                                    onResult(false, it.message ?: "Failed to create owner")
                                }
                        } else {
                            onResult(false, "Email already exists")
                        }
                    }
                    .addOnFailureListener {
                        onResult(false, it.message ?: "Failed to check email")
                    }
            }
            .addOnFailureListener {
                onResult(false, it.message ?: "Failed to create account")
            }
    }

    override fun listenToEmployee() {
        employeeStatusListener?.remove()
        val ref = db.collection(fu.EMPLOYEES).document(pref.getUser().id)
        employeeStatusListener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("EmployeeListener", "Error: ${error.message}")
                return@addSnapshotListener
            }
            val status = snapshot?.getString("status")
            Log.d("EmployeeListener", "Status updated: $status")
            _employeeStatus.value = status ?: Constants.STATUS_PENDING
        }

    }

    // ---------------------------------------------------------
    // 🔹 REGISTER EMPLOYEE
    // ---------------------------------------------------------
    override fun registerEmployee(
        name: String,
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = it.user!!.uid
                val employee = Employee(
                    id = uid,
                    name = name,
                    email = email,
                    password = password,
                    ownerId = null,
                    storeId = null,
                    joinedAt = "${DateHelper.getCurrentDate()} ${DateHelper.getCurrentTime()}",
                    role = EMPLOYEE_ROLE,
                    acceptedAt = null,
                    status = Constants.STATUS_PENDING // Pending until accepted by store
                )

                // Save base user record
                db.collection("users").document(uid).set(
                    hashMapOf(
                        "role" to EMPLOYEE_ROLE,
                        "name" to name,
                        "email" to email
                    )
                )

                // Save employee record
                db.collection(fu.EMPLOYEES).document(uid).set(employee.hash())
                    .addOnSuccessListener {
                        pref.saveUser(uid, name, EMPLOYEE_ROLE, email, password)
                        pref.saveStore("", "", "", "", "")
                        onResult(true, "Employee account created successfully. Please wait for approval.")
                    }
                    .addOnFailureListener {
                        onResult(false, it.message ?: "Error creating employee record")
                    }
            }
            .addOnFailureListener {
                onResult(false, it.message ?: "Error creating employee account")
            }
    }





    override fun rejectEmployee(
        employeeId: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        db.collection(fu.EMPLOYEES)
            .document(employeeId)
            .update("status", Constants.STATUS_REJECTED)
            .addOnSuccessListener {
                onResult(true, "The employee has been fired.")
            }
            .addOnFailureListener { e ->
                onResult(false, e.message ?: "Error while firing the employee.")
            }
    }


    override fun logout(onResult: (Boolean, String) -> Unit) {
        auth.signOut()
        db.clearPersistence()
        pref.clearPrefs()
        onResult(true, "Success")

    }

    override fun stopListening() {
        employeeStatusListener?.remove()
        employeeStatusListener = null
    }
}

