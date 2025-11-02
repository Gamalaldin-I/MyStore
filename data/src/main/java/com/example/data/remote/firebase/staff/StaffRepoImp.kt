package com.example.data.remote.firebase.staff

import android.content.Context
import com.example.data.local.sharedPrefs.SharedPref
import com.example.data.remote.Mapper.hash
import com.example.data.remote.firebase.FirebaseUtils
import com.example.domain.model.Store
import com.example.domain.model.remoteModels.Invite
import com.example.domain.model.remoteModels.StoreEmployee
import com.example.domain.repo.StaffRepo
import com.example.domain.util.Constants.STATUS_ACCEPTED
import com.example.domain.util.Constants.STATUS_HIRED
import com.example.domain.util.Constants.STATUS_PENDING
import com.example.domain.util.Constants.STATUS_REJECTED
import com.example.domain.util.DateHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StaffRepoImp(
    private val db: FirebaseFirestore,
    private val pref: SharedPref
) : StaffRepo {

    // ---------------------------------------------------------
    // 🔹 VARIABLES
    // ---------------------------------------------------------

    private val fu = FirebaseUtils
    private var inviteListener: ListenerRegistration? = null
    private var employeeListener: ListenerRegistration? = null

    private val _invitesFlow = MutableStateFlow<List<Invite>>(emptyList())
    override val invitesFlow = _invitesFlow.asStateFlow()

    private val _employeesFlow = MutableStateFlow<List<StoreEmployee>>(emptyList())
    override val employeesFlow = _employeesFlow.asStateFlow()

    // ---------------------------------------------------------
    // 🔹 INVITES SECTION
    // ---------------------------------------------------------

    override fun listenToInvites() {
        inviteListener?.remove()

        val invitesRef = db.collection(fu.INVITES).whereEqualTo(
            "storeId", pref.getStore().id
        )

        inviteListener = invitesRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Invite::class.java)?.copy(code = doc.id)
            } ?: emptyList()
            _invitesFlow.value = list
        }
    }

    override fun addInvite(email: String, code: String, onResult: (Boolean, String) -> Unit) {
        val invite = Invite(
            email = email,
            code = code,
            status = STATUS_PENDING,
            createdAt = "${DateHelper.getCurrentDate()} ${DateHelper.getCurrentTime()}",
            ownerId = pref.getUser().id,
            storeId = pref.getStore().id,
            storeName = pref.getStore().name
        )


        val invitesRef = db.collection(fu.INVITES)

        invitesRef.document(code).set(invite.hash())
            .addOnSuccessListener { onResult(true, "Invite sent successfully") }
            .addOnFailureListener { e -> onResult(false, e.message ?: "Error sending invite") }
    }

    override fun deleteInvite(invite: Invite, onResult: (Boolean, String) -> Unit) {
        val invitesRef = db.collection(fu.INVITES)
        invitesRef.document(invite.code!!).delete()
            .addOnSuccessListener { onResult(true, "Invite deleted successfully") }
            .addOnFailureListener { e -> onResult(false, e.message ?: "Error deleting invite") }
    }

    override fun getAllInvitesForEmployee(onResult: (Boolean, String) -> Unit) {
        inviteListener?.remove()
        val email = pref.getUser().email
        val ref = db.collection(fu.INVITES)
            .whereEqualTo("email", email)
            .whereEqualTo("status", STATUS_PENDING)
            inviteListener = ref.addSnapshotListener { snapshot , error ->
                if (error != null) {
                    onResult(false, error.message ?: "Error fetching invites")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Invite::class.java)?.copy(code = doc.id)
                }?:emptyList()
                _invitesFlow.value = list
                onResult(true, "Invites fetched successfully")
            }
    }

    override fun acceptInvite(
        invite: Invite,
        code: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val user = pref.getUser()

        // Step 1: Validate the invite code
        if (invite.code != code) {
            onResult(false, "Invalid invite code.")
            return
        }

        //  Step 2: Prepare updated invite
        val updatedInvite = invite.copy(
            status = STATUS_ACCEPTED,
            acceptedAt = "${DateHelper.getCurrentDate()} ${DateHelper.getCurrentTime()}"
        )

        //  Step 3: FireStore references
        val invitesRef = db.collection(fu.INVITES).document(invite.code!!)
        val usersRef = db.collection("users").document(user.id)
        val employeeRef = db.collection(fu.EMPLOYEES).document(user.id)
        val storeEmployeesRef = db.collection(fu.OWNERS)
            .document(invite.ownerId!!)
            .collection(fu.STORES)
            .document(invite.storeId!!)
            .collection(fu.EMPLOYEES)

        //  Step 4: Prepare new employee data
        val newEmployee = StoreEmployee(
            id = user.id,
            email = user.email,
            name = user.name,
            role = user.role,
            status = STATUS_HIRED,
            joinedAt = "${DateHelper.getCurrentDate()} ${DateHelper.getCurrentTime()}"
        )

        // ✅ Step 5: Chain tasks sequentially
        usersRef.update("status", STATUS_HIRED)
            .continueWithTask {
                // Update invite status
                invitesRef.set(updatedInvite.hash())
            }.continueWithTask {
                // Add employee to store
                storeEmployeesRef.document(user.id).set(newEmployee.hash())
            }.continueWithTask {
                // Update employee main collection
                employeeRef.update(
                    mapOf(
                        "storeId" to invite.storeId,
                        "ownerId" to invite.ownerId,
                        "status" to STATUS_HIRED,
                        "acceptedAt" to updatedInvite.acceptedAt
                    )
                )
            }.continueWithTask {
                // Fetch store info to save locally
                db.collection(fu.OWNERS).document(invite.ownerId!!)
                    .collection(fu.STORES)
                    .document(invite.storeId!!)
                    .get()
            }.addOnSuccessListener { storeDoc ->
                val phone = storeDoc.getString("phone").orEmpty()
                val location = storeDoc.getString("location").orEmpty()

                // ✅ Save store locally
                pref.saveStore(
                    id = invite.storeId!!,
                    name = invite.storeName!!,
                    phone = phone,
                    location = location,
                    ownerId = invite.ownerId!!
                )

                onResult(true, "Invite accepted successfully.")
            }.addOnFailureListener { e ->
                onResult(false, e.message ?: "Failed to accept invite.")
            }
    }


    override fun rejectInvite(invite: Invite, onResult: (Boolean, String) -> Unit) {
        val rejectedInvite = invite.copy(status =STATUS_REJECTED)

        val invitesRef = db.collection(fu.INVITES)

        invitesRef.document(invite.code!!).set(rejectedInvite.hash())
            .addOnSuccessListener { onResult(true, "Invite rejected successfully") }
            .addOnFailureListener { e -> onResult(false, e.message ?: "Error rejecting invite") }
    }

    // ---------------------------------------------------------
    // 🔹 EMPLOYEES SECTION
    // ---------------------------------------------------------

    override fun listenToEmployees() {
        employeeListener?.remove()

        val storeEmployeesRef = db.collection(fu.OWNERS).document(pref.getStore().ownerId)
            .collection(fu.STORES).document(pref.getStore().id)
            .collection(fu.EMPLOYEES)

        employeeListener = storeEmployeesRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(StoreEmployee::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            _employeesFlow.value = list
        }
    }

    override fun rejectOrRehireEmployee(
        employeeId: String,
        reject: Boolean,
        onResult: (Boolean, String) -> Unit,
    ) {
        val newStatus = if (reject) STATUS_REJECTED else STATUS_HIRED
        val msgSuccess = if (reject) "The employee has been rejected." else "The employee has been hired."
        val msgFailure = if (reject) "Failed to reject the employee." else "Failed to hire the employee."

        val employeeRef = db.collection(fu.EMPLOYEES).document(employeeId)
        val ownerStoreEmployeeRef = db.collection(fu.OWNERS)
            .document(pref.getUser().id)
            .collection(fu.STORES)
            .document(pref.getStore().id)
            .collection(fu.EMPLOYEES)
            .document(employeeId)
        val userRef = db.collection("users").document(employeeId)

        employeeRef.get()
            .addOnSuccessListener { doc ->
                val storeId = doc.getString("storeId")
                val currentStoreId = pref.getStore().id

                if (storeId != currentStoreId) {
                    onResult(false, "This employee is not part of the current store.")
                    return@addOnSuccessListener
                }
                userRef.update("status",newStatus)
                    .continueWithTask{

                employeeRef.update("status", newStatus)

                    }.continueWithTask {
                        ownerStoreEmployeeRef.update("status", newStatus)
                    }
                    .addOnSuccessListener {
                        onResult(true, msgSuccess)
                    }
                    .addOnFailureListener { e ->
                        onResult(false, e.message ?: msgFailure)
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message ?: "Failed to fetch employee data.")
            }
    }

    override fun updateStore(
        name: String,
        phone: String,
        location: String,
        onResult: (success: Boolean, msg: String) -> Unit){
        val storeRef = db.collection(fu.OWNERS).document(pref.getUser().id)
            .collection(fu.STORES).document(pref.getStore().id)

            storeRef.get().addOnSuccessListener {
                val newStoreData = Store(
                    id =pref.getStore().id,
                    name = name,
                    phone = phone,
                    location = location,
                    ownerId = pref.getUser().id
                )
                storeRef.update(newStoreData.hash())
                    .addOnSuccessListener {
                        onResult(true, "Store updated successfully")
                    }.addOnFailureListener{ f->
                        onResult(false, f.message ?: "Failed to update store")
                    }
            }
    }


    /**
     * Professional email message for employee invitation
     */
    private fun createInvitationEmail(storeName: String, code: String,phone:String): String {
        return """
Dear Team Member,

Greetings from STORA!

We are delighted to inform you that you have been invited to join $storeName as a valued member of our team.

To get started:
1. Download the STORA app from the Play Store (if you haven't already)
2. Sign up as an employee
3. Use the following invitation code to complete your registration:

Invitation Code: $code
and this it our phone for details: ${phone}

We look forward to having you on board and working together to achieve great success.

If you have any questions or need assistance, please don't hesitate to reach out.

Best regards,
The STORA Team
    """.trimIndent()
    }

    /**
     * Send invitation email using device's email client
     */
    override fun sendEmail(
        context: Context,
        recipientEmail: String,
        code: String
    ):Pair<String,String>{
        val subject = "Invitation to Join ${pref.getStore().name} - STORA"
        val message = createInvitationEmail(pref.getStore().name, code,pref.getStore().phone)
        return Pair(subject,message)
    }
    // ---------------------------------------------------------
    // 🔹 LISTENER CONTROL
    // ---------------------------------------------------------

    override fun stopListening() {
        inviteListener?.remove()
        employeeListener?.remove()
        inviteListener = null
        employeeListener = null
    }
}
