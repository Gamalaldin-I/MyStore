package com.example.htopstore.util.firebase.auth

import android.util.Log
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.remoteModels.Employee
import com.example.domain.repo.AuthRepo
import com.example.domain.util.Constants.EMPLOYEE_ROLE
import com.example.domain.util.Constants.OWNER_ROLE
import com.example.domain.util.Constants.STATUS_HIRED
import com.example.domain.util.Constants.STATUS_PENDING
import com.example.domain.util.DateHelper
import com.example.htopstore.util.firebase.FirebaseUtils
import com.example.htopstore.util.firebase.Mapper.hash
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

 class AuthRepoImp(
    private val db: FirebaseFirestore,
    private val pref: SharedPref, ) : AuthRepo {

    private val auth = FirebaseAuth.getInstance()
    private val fu = FirebaseUtils

    private val _employeeStatus = MutableStateFlow(STATUS_HIRED)
    override val employeeStatus: StateFlow<String> = _employeeStatus
    private var employeeStatusListener: ListenerRegistration? = null

    // ---------------------------------------------------------
    // 🔹 LOGIN
    // ---------------------------------------------------------
    override fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { res ->
                val userId = res.user?.uid
                if (userId == null) {
                    onResult(false, "Error: User ID not found")
                    return@addOnSuccessListener
                }
                fetchUserData(userId, email, onResult)
            }
            .addOnFailureListener {
                onResult(false, it.message ?: "Invalid email or password")
            }
    }

    private fun fetchUserData(userId: String, email: String, onResult: (Boolean, String) -> Unit) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { snapshot ->
                val role = snapshot.getLong("role")?.toInt()
                val pendingEmail = snapshot.getString("pendingEmail")
                val oldEmail = snapshot.getString("email").orEmpty()

                if (role == null) {
                    onResult(false, "Error: User role not found")
                    return@addOnSuccessListener
                }

                if (pendingEmail != null) {
                    updatePendingEmail(userId, role, pendingEmail, oldEmail)
                }

                manageUser(role, userId, email, onResult)
            }
            .addOnFailureListener {
                onResult(false, it.message ?: "Error fetching user data")
            }
    }

    private fun updatePendingEmail(userId: String, role: Int, pendingEmail: String, oldEmail: String) {
        val collection = if (role == OWNER_ROLE) fu.OWNERS else fu.EMPLOYEES
        db.collection(collection).document(userId).get()
            .addOnSuccessListener { doc ->
                val ownerId = if (role == OWNER_ROLE) userId else doc.getString("ownerId").orEmpty()
                val storeId = if (role == OWNER_ROLE) doc.getString("storeID").orEmpty() else doc.getString("storeId").orEmpty()
                val status = doc.getString("status") ?: STATUS_HIRED

                checkAndUpdateEmailInFireStore(
                    uid = userId,
                    ownerId = ownerId,
                    storeId = storeId,
                    role = role,
                    status = status,
                    pendingEmail = pendingEmail,
                    oldEmail = oldEmail
                ) { success, msg ->
                    Log.d("EmailUpdate", "[$role] Email update result: $success - $msg")
                }
            }
            .addOnFailureListener {
                Log.e("EmailUpdate", "Fetch failed: ${it.message}")
            }
    }

    // ---------------------------------------------------------
    // 🔹 MANAGE USER DATA (OWNER / EMPLOYEE)
    // ---------------------------------------------------------
    private fun manageUser(role: Int, userId: String, email: String, onResult: (Boolean, String) -> Unit) {
        if (role == OWNER_ROLE) {
            db.collection(fu.OWNERS).document(userId).get()
                .addOnSuccessListener { snapshot ->
                    val name = snapshot.getString(fu.OWNER_NAME) ?: "Owner"
                    val storeId = snapshot.getString(fu.OWNER_STORE_ID)
                        ?: return@addOnSuccessListener onResult(false, "Owner store ID not found")

                    getStoreData(userId, storeId) { success, msg ->
                        if (success) {
                            pref.saveUser(userId, name, role, email)
                            onResult(true, "Owner logged in successfully")
                        } else onResult(false, msg)
                    }
                }
                .addOnFailureListener {
                    onResult(false, it.message ?: "Error fetching owner data")
                }

        } else {
            db.collection(fu.EMPLOYEES).document(userId).get()
                .addOnSuccessListener { snapshot ->
                    val name = snapshot.getString("name") ?: "Employee"
                    val storeId = snapshot.getString("storeId")
                    val ownerId = snapshot.getString("ownerId")
                    val status = snapshot.getString("status") ?: STATUS_PENDING

                    pref.saveUser(userId, name, role, email)

                    if (status != STATUS_HIRED) {
                        pref.saveStore("", "", "", "", "")
                        onResult(true, "Logged in successfully, but not yet linked to any store.")
                        return@addOnSuccessListener
                    }

                    getStoreData(ownerId!!, storeId!!) { success, msg ->
                        if (success) onResult(true, "Employee logged in successfully")
                        else onResult(false, msg)
                    }
                }
                .addOnFailureListener {
                    onResult(false, it.message ?: "Error fetching employee data")
                }
        }
    }

    // ---------------------------------------------------------
    // 🔹 GET STORE DATA (SHARED)
    // ---------------------------------------------------------
    private fun getStoreData(ownerId: String, storeId: String, onResult: (Boolean, String) -> Unit) {
        db.collection(fu.OWNERS).document(ownerId)
            .collection(fu.STORES).document(storeId).get()
            .addOnSuccessListener { snapshot ->
                val storeName = snapshot.getString(fu.STORE_NAME)
                val storeLocation = snapshot.getString(fu.STORE_LOCATION)
                val storePhone = snapshot.getString(fu.STORE_PHONE)

                if (storeName == null || storeLocation == null || storePhone == null) {
                    onResult(false, "Incomplete store data")
                    return@addOnSuccessListener
                }

                pref.saveStore(storeId, storeName, storePhone, storeLocation, ownerId)
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
                    fu.OWNER_ID to ownerId,
                    fu.OWNER_STORE_ID to storeId,
                    fu.OWNER_ROLE to OWNER_ROLE,
                    fu.OWNER_CREATED_AT to FieldValue.serverTimestamp()
                )

                db.collection("users").document(ownerId).set(
                    hashMapOf(
                        "role" to OWNER_ROLE,
                        "name" to name,
                        "email" to email,
                        "status" to STATUS_HIRED
                    )
                )

                db.collection(fu.OWNERS).whereEqualTo(fu.OWNER_EMAIL, email).get()
                    .addOnSuccessListener { query ->
                        if (query.isEmpty) {
                            db.collection(fu.OWNERS).document(ownerId).set(owner)
                                .addOnSuccessListener {
                                    val store = hashMapOf(
                                        fu.STORE_NAME to storeName,
                                        fu.STORE_LOCATION to storeLocation,
                                        fu.STORE_PHONE to storePhone
                                    )

                                    db.collection(fu.OWNERS).document(ownerId)
                                        .collection(fu.STORES).document(storeId)
                                        .set(store)
                                        .addOnSuccessListener {
                                            pref.saveUser(ownerId, name, OWNER_ROLE, email)
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

    // ---------------------------------------------------------
    // 🔹 REGISTER EMPLOYEE
    // ---------------------------------------------------------
    override fun registerEmployee(name: String, email: String, password: String, onResult: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { it ->
                val uid = it.user!!.uid
                val employee = Employee(
                    id = uid,
                    name = name,
                    email = email,
                    ownerId = null,
                    storeId = null,
                    joinedAt = "${DateHelper.getCurrentDate()} ${DateHelper.getCurrentTime()}",
                    role = EMPLOYEE_ROLE,
                    acceptedAt = null,
                    status = STATUS_PENDING
                )

                db.collection("users").document(uid).set(
                    hashMapOf(
                        "role" to EMPLOYEE_ROLE,
                        "name" to name,
                        "email" to email,
                        "status" to STATUS_PENDING
                    )
                )

                db.collection(fu.EMPLOYEES).document(uid).set(employee.hash())
                    .addOnSuccessListener {
                        pref.saveUser(uid, name, EMPLOYEE_ROLE, email)
                        pref.saveStore("", "", "", "", "")
                        onResult(true, "Employee account created. Await approval.")
                    }
                    .addOnFailureListener {
                        onResult(false, it.message ?: "Error creating employee record")
                    }
            }
            .addOnFailureListener {
                onResult(false, it.message ?: "Error creating employee account")
            }
    }

    // ---------------------------------------------------------
    // 🔹 EMAIL & PASSWORD UPDATES
    // ---------------------------------------------------------
    override fun resetPassword(email: String, onResult: (Boolean, String) -> Unit) {
        if (email.isEmpty()) return onResult(false, "Please enter your email.")
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onResult(true, "Password reset email sent.") }
            .addOnFailureListener { e -> onResult(false, e.message ?: "Something went wrong.") }
    }

    override fun changePassword(oldPassword: String, newPassword: String, onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser ?: return onResult(false, "User not authenticated")
        val credential = EmailAuthProvider.getCredential(pref.getUser().email, oldPassword)

        user.reauthenticate(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                user.updatePassword(newPassword).addOnCompleteListener { task ->
                    if (task.isSuccessful) onResult(true, "Password changed successfully")
                    else onResult(false, task.exception?.message ?: "Failed to change password")
                }
            } else onResult(false, it.exception?.message ?: "Invalid old password")
        }
    }

     override fun updateName(name: String, onResult: (Boolean, String) -> Unit) {
         val role = pref.getRole()
         if (role == OWNER_ROLE) {
             val task1 = db.collection(fu.OWNERS).document(pref.getUser().id)
                 .update("name", name)
             val task2 = db.collection("users").document(pref.getUser().id)
                 .update("name", name)
             Tasks.whenAllComplete(task1, task2).addOnSuccessListener {
                 pref.setUserName(name)
                 onResult(true, "Name updated successfully") }.addOnFailureListener {
                     onResult(false, it.message ?: "Failed to update name") }
         } else {
             val userRef = db.collection("users").document(pref.getUser().id)
             val employeeRef = db.collection("employees").document(pref.getUser().id)
             val employeeInStoreRef = db.collection(fu.OWNERS).document(pref.getStore().ownerId)
                 .collection(fu.STORES).document(pref.getStore().id)
                 .collection(fu.EMPLOYEES).document(pref.getUser().id)
             val task1 = userRef.update("name", name)
             val task2 = employeeRef.update("name", name)
             val task3 = employeeInStoreRef.update("name", name)
             Tasks.whenAllComplete(task1, task2, task3) .addOnSuccessListener {
                 pref.setUserName(name)
                 onResult(true, "Name updated successfully") }
                 .addOnFailureListener { e ->
                     onResult(false, e.message ?: "Failed to update name") }
         }
     }

     override fun updateEmail(newEmail: String, password: String, onResult: (Boolean, String) -> Unit) {
        val currentUser = auth.currentUser ?: return onResult(false, "User not authenticated")
        val credential = EmailAuthProvider.getCredential(pref.getUser().email, password)

        currentUser.reauthenticate(credential).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                currentUser.verifyBeforeUpdateEmail(newEmail)
                    .addOnSuccessListener {
                        db.collection("users").document(pref.getUser().id)
                            .update("pendingEmail", newEmail)
                            .addOnSuccessListener {
                                onResult(true, "Verification link sent to $newEmail.")
                            }
                            .addOnFailureListener { e ->
                                onResult(false, e.message ?: "Failed to save pending email")
                            }
                    }
                    .addOnFailureListener { e ->
                        onResult(false, e.message ?: "Failed to send verification email")
                    }
            } else onResult(false, authTask.exception?.message ?: "Incorrect password")
        }
    }

    private fun checkAndUpdateEmailInFireStore(
        uid: String,
        ownerId: String,
        storeId: String,
        role: Int,
        status: String,
        pendingEmail: String,
        oldEmail: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val currentUser = auth.currentUser ?: return onResult(false, "User not authenticated")

        currentUser.reload().addOnCompleteListener { reloadTask ->
            if (!reloadTask.isSuccessful) return@addOnCompleteListener onResult(false, "Failed to reload user data")

            val currentEmail = currentUser.email
            when {
                currentEmail == pendingEmail && currentEmail != oldEmail ->
                    updateEmailInFireStore(uid, ownerId, storeId, role, status,
                        currentEmail, onResult)

                currentEmail == oldEmail ->
                    onResult(true, "Email verification still pending")

                else -> onResult(false, "Unexpected email state")
            }
        }
    }

    private fun updateEmailInFireStore(
        uid: String,
        ownerId: String,
        storeId: String,
        role: Int,
        status: String,
        newEmail: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val isOwner = role == OWNER_ROLE

        val updateTasks = if (isOwner) {
            listOf(
                db.collection(fu.OWNERS).document(ownerId).update("email", newEmail),
                db.collection("users").document(uid).update("email", newEmail)
            )
        } else if (status != STATUS_HIRED) {
            listOf(
                db.collection("users").document(uid).update("email", newEmail),
                db.collection("employees").document(uid).update("email", newEmail)
            )
        } else {
            listOf(
                db.collection("users").document(uid).update("email", newEmail),
                db.collection("employees").document(uid).update("email", newEmail),
                db.collection(fu.OWNERS).document(ownerId)
                    .collection(fu.STORES).document(storeId)
                    .collection("employees").document(uid)
                    .update("email", newEmail)
            )
        }

        Tasks.whenAllComplete(updateTasks)
            .addOnSuccessListener { tasks ->
                val failed = tasks.any { !it.isSuccessful }
                if (failed) return@addOnSuccessListener onResult(false, "Some updates failed in Firestorm")

                db.collection("users").document(uid)
                    .update("pendingEmail", FieldValue.delete())
                    .addOnSuccessListener {
                        pref.setEmail(newEmail)
                        onResult(true, "Email updated successfully")
                    }
                    .addOnFailureListener { e ->
                        onResult(false, e.message ?: "Failed to clear pending email")
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message ?: "Failed to update email in Firestorm")
            }
    }

    // ---------------------------------------------------------
    // 🔹 EMPLOYEE LISTENER & LOGOUT
    // ---------------------------------------------------------
    override fun listenToEmployee() {
        employeeStatusListener?.remove()
        val ref = db.collection(fu.EMPLOYEES).document(pref.getUser().id)
        employeeStatusListener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("EmployeeListener", "Error: ${error.message}")
                return@addSnapshotListener
            }
            _employeeStatus.value = snapshot?.getString("status") ?: STATUS_PENDING
            Log.d("EmployeeListener", "Status updated: ${_employeeStatus.value}")
        }
    }

    override fun logout(onResult: (Boolean, String) -> Unit) {
        auth.signOut()
        db.clearPersistence()
        pref.clearPrefs()
        onResult(true, "Logged out successfully")
    }

    override fun stopListening() {
        employeeStatusListener?.remove()
        employeeStatusListener = null
    }

    override fun updateStoreData(name: String, phone: String, location: String, onResult: (Boolean, String) -> Unit) {
        TODO("Not yet implemented")
    }
}
