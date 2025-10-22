package com.example.htopstore.util.firebase.staff
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.remoteModels.Invite
import com.example.domain.model.remoteModels.StoreEmployee
import com.example.domain.repo.StaffRepo
import com.example.domain.util.Constants.STATUS_PENDING
import com.example.domain.util.DateHelper
import com.example.htopstore.util.firebase.FirebaseUtils
import com.example.htopstore.util.firebase.Mapper.hash
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StaffRepoImp(
     db: FirebaseFirestore,
     private val pref: SharedPref,
 ) : StaffRepo {

    private val fu = FirebaseUtils
    private val storeRef = db.collection(fu.OWNERS)
        .document(pref.getUser().id)
        .collection(fu.STORES)
        .document(pref.getStore().id)

    private val invitesRef = storeRef.collection(fu.INVITES)
    private val employeesRef = storeRef.collection(fu.EMPLOYEES)

    private var inviteListener: ListenerRegistration? = null
    private var employeeListener: ListenerRegistration? = null

    private val _invitesFlow = MutableStateFlow<List<Invite>>(emptyList())
    override val invitesFlow = _invitesFlow.asStateFlow()

    private val _employeesFlow = MutableStateFlow<List<StoreEmployee>>(emptyList())
    override val employeesFlow = _employeesFlow.asStateFlow()

     override fun listenToInvites() {
        inviteListener?.remove()
        inviteListener = invitesRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            snapshot?.let {
                val list = it.documents.mapNotNull { doc ->
                    doc.toObject(Invite::class.java)?.copy(code = doc.id)
                }
                _invitesFlow.value = list
            }
        }
    }

    override fun listenToEmployees() {
        employeeListener?.remove()
        employeeListener = employeesRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            snapshot?.let {
                val list = it.documents.mapNotNull { doc ->
                    doc.toObject(StoreEmployee::class.java)?.copy(id = doc.id)
                }
                _employeesFlow.value = list
            }
        }
    }

    override fun addInvite(email: String, code:String,onResult:(success: Boolean,msg:String)->Unit) {
        val newInvite = Invite(email = email,
            code = code,
            status = STATUS_PENDING,
            createdAt = "${DateHelper.getCurrentDate()} ${DateHelper.getCurrentTime()}",
            acceptedAt = null,
            ownerId = pref.getUser().id,
            storeId = pref.getStore().id
            )
        val inviteRef = invitesRef.document(newInvite.code!!)
        inviteRef.set(newInvite.hash()).addOnSuccessListener {
            onResult(true,"Invite sent successfully")
        }.addOnFailureListener {
            onResult(false,it.message ?:"Error sending invite")
        }
    }

    override fun deleteInvite(invite: Invite,onResult:(success: Boolean,msg:String)->Unit){
        val inviteRef = invitesRef.document(invite.code!!)
        inviteRef.delete()
            .addOnSuccessListener {
                onResult(true,"Invite deleted successfully")
            }.addOnFailureListener {
                onResult(false,it.message ?:"Error deleting invite")
            }
    }


     override fun stopListening() {
        inviteListener?.remove()
        employeeListener?.remove()
    }

}
