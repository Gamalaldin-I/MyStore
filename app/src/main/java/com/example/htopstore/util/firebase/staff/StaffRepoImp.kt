package com.example.htopstore.util.firebase.staff

import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.remoteModels.Invite
import com.example.domain.model.remoteModels.StoreEmployee
import com.example.domain.repo.StaffRepo
import com.example.domain.util.Constants
import com.example.domain.util.Constants.STATUS_ACCEPTED
import com.example.domain.util.Constants.STATUS_HIRED
import com.example.domain.util.Constants.STATUS_PENDING
import com.example.domain.util.DateHelper
import com.example.htopstore.util.firebase.FirebaseUtils
import com.example.htopstore.util.firebase.Mapper.hash
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

    override fun acceptInvite(invite: Invite, code: String, onResult: (Boolean, String) -> Unit) {
        val user = pref.getUser()

        // Step 1️⃣: Update invite status
        val updatedInvite = invite.copy(
            status = STATUS_ACCEPTED,
            acceptedAt = "${DateHelper.getCurrentDate()} ${DateHelper.getCurrentTime()}"
        )

        val invitesRef = db.collection(fu.INVITES)

        invitesRef.document(invite.code!!).set(updatedInvite.hash())
            .addOnSuccessListener {
                // Step 2️⃣: Add employee to store
                val storeEmployee = StoreEmployee(
                    id = user.id,
                    email = user.email,
                    name = user.name,
                    role = user.role,
                    status = STATUS_HIRED,
                    joinedAt = "${DateHelper.getCurrentDate()} ${DateHelper.getCurrentTime()}"
                )

                val storeEmployeesRef = db.collection(fu.OWNERS).document(invite.ownerId!!)
                    .collection(fu.STORES).document(invite.storeId!!)
                    .collection(fu.EMPLOYEES)

                val employeeRef = db.collection(fu.EMPLOYEES).document(user.id)

                storeEmployeesRef.document(user.id).set(storeEmployee.hash())
                    .addOnSuccessListener {
                        // Step 3️⃣: Update main employee collection
                        employeeRef.update(
                            "storeId", invite.storeId,
                            "ownerId", invite.ownerId,
                            "status", STATUS_HIRED,
                            "acceptedAt", updatedInvite.acceptedAt
                        ).addOnSuccessListener { snapshot ->
                                onResult(true, "Employee accepted successfully")
                                    }
                            .addOnFailureListener { e ->
                                onResult(false, e.message ?: "Error updating employee data")
                            }
                    }.addOnFailureListener { e ->
                        onResult(false, e.message ?: "Error adding employee to store")
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message ?: "Error updating invite status")
            }
    }

    override fun rejectInvite(invite: Invite, onResult: (Boolean, String) -> Unit) {
        val rejectedInvite = invite.copy(status = Constants.STATUS_REJECTED)

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
