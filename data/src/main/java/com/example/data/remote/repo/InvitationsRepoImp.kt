package com.example.data.remote.repo

import android.util.Log
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.remoteModels.EmployeeInvitationAcceptanceResponse
import com.example.domain.model.remoteModels.Invitation
import com.example.domain.repo.InvitationsRepo
import com.example.domain.util.Constants
import com.example.domain.util.DateHelper
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class InvitationsRepoImp(
    private val supabase: SupabaseClient,
    private val pref: SharedPref
): InvitationsRepo {

    companion object{
        private const val INVITES = "invitations"
        private const val USERS = "users"
        private const val TAG = "INVITATIONS_REPO"
    }


    override suspend fun createInvitation(email:String,code: String): Pair<Boolean, String> {
        try {
            val invitation = Invitation(
                id=code,
                code=code,
                email=email,
                storeId=pref.getStore().id,
                storeIcon = pref.getStore().logoUrl,
                storeName = pref.getStore().name,
                createdAt = "${DateHelper.getCurrentDate()} ${DateHelper.getCurrentTime()}",
                status = Constants.STATUS_PENDING,
                acceptedAt = ""
            )

            supabase.from(INVITES).insert(invitation)
            return Pair(true, "Invitation created successfully")
        }catch(e: Exception){
            Log.d(TAG, e.message.toString())
            return Pair(false, "Error creating invitation")
        }
    }

    override suspend fun deleteInvite(invite: Invitation): Pair<Boolean, String> {
        try {
            supabase.from(INVITES).delete {
                filter {
                    eq("id", invite.id!!)
                }
            }
            return Pair(true, "Invitation deleted successfully")
        }catch(_: Exception){
            return Pair(false, "Error deleting invitation")
        }
    }

    override suspend fun getAllInvitesForStore(): Pair<List<Invitation>, String> {
        try {
           val list = supabase.from(INVITES).select{
                filter {
                    eq("storeId", pref.getStore().id)
                }
            }.decodeList<Invitation>()
            return Pair(list, "Invitations fetched successfully")
        }catch (_: Exception){
            return Pair(emptyList(), "Error fetching invitations")
        }
    }

    override suspend fun getAllEmailPendingInvites(): Pair<List<Invitation>, String> {
        try{
            val list =supabase.from(INVITES).select{
                filter {
                    eq("email", pref.getUser().email)
                    eq("status", Constants.STATUS_PENDING)
                }
            }.decodeList<Invitation>()
            return Pair(list, "Invitations fetched successfully")
        }catch (_: Exception){
            return Pair(emptyList(), "Error fetching invitations")
        }
    }

    override suspend fun acceptInvite(
        invite: Invitation,
        code: String
    ): Pair<Boolean, String> {
        return try {
            supabase.from(INVITES).update(
                mapOf<String,String>(
                    "status" to Constants.STATUS_ACCEPTED,
                    "acceptedAt" to System.currentTimeMillis().toString()
                )
            ) {
                filter {
                    eq("id", invite.id!!)
                }
            }

            hireTheEmployee(invite.storeId!!)

            Pair(true, "Invitation accepted successfully ✅")
        } catch (e: Exception) {
            Pair(false, e.message ?: "Error accepting invitation ❌")
        }
    }


    private suspend fun hireTheEmployee(storeId:String){
        supabase.from(USERS).update(EmployeeInvitationAcceptanceResponse(
            storeId = storeId,
            status = Constants.STATUS_HIRED,
            role = Constants.EMPLOYEE_ROLE
        )
        ){
            filter {
                eq("id", pref.getUser().id)
            }
        }
        //saveTheUser
        pref.saveUser(pref.getUser().copy(
            storeId = storeId,
            role = Constants.EMPLOYEE_ROLE,
            status = Constants.STATUS_HIRED)
        )

    }

    override suspend fun rejectInvite(invite: Invitation): Pair<Boolean, String>{
        return try{
            supabase.from(INVITES).update(
                mapOf(
                    "status" to Constants.STATUS_REJECTED,
                    "acceptedAt" to System.currentTimeMillis().toString())) {
                filter {
                    eq("id", invite.id!!)
                }}
                Pair(true, "Rejected")
            }catch (_: Exception){
                Pair(false, "Error rejecting invitation")
            }
    }
    override fun sendEmail(
        code: String
    ): Pair<String, String> {
        return try {
            val storeName = pref.getStore().name
            val subject = "Your Invitation to Join $storeName | Action Required"
            val emailBody = getEmailContent(code)
            return Pair(subject, emailBody)
        } catch (e: Exception) {
            Pair("Error", "Unable to send invitation: ${e.message}")
        }
    }

    private fun getEmailContent(code: String): String {
        val storePhone = pref.getStore().phone
        val storeName = pref.getStore().name
        val storeLocation = pref.getStore().location

        return """
        Dear Team Member,
        
        Congratulations! We are pleased to extend an official invitation for you to join the $storeName team.
        
        After careful consideration, we believe your skills and expertise will be a valuable addition to our organization. We are excited about the opportunity to work together and achieve great results.
        
        
        NEXT STEPS - ACTION REQUIRED
        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        To activate your account and complete your onboarding, please use your unique invitation code:
        
        🔐 Invitation Code: $code
        
        1. Open the Stora application
        2. Navigate to the invitation code section
        3. Enter the code above to validate your access
        4. Complete your profile setup
        
        This code is confidential and intended solely for your use. Please do not share it with anyone.
        
        
        WHAT YOU'LL GAIN
        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        ✓ Seamless team collaboration tools
        ✓ Real-time communication with your team
        ✓ Access to store management features
        ✓ Professional development opportunities
        ✓ Streamlined workflow and task management
        
        
        NEED ASSISTANCE?
        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        
        Our team is here to support you every step of the way:
        
        📍 Location: $storeLocation
        📞 Phone: $storePhone
        📧 Reply to this email for any questions
        
        We recommend completing your registration within the next 48 hours to ensure uninterrupted access to all features.
        
        We look forward to welcoming you aboard and building success together!
        
        
        Warm regards,
        
        The $storeName Management Team
        Powered by Stora
        
        
        ───────────────────────────────────────
        This is an automated message from Stora. Please do not reply directly to this email address.
        For support, please contact your store manager or use the contact information provided above.
    """.trimIndent()
    }


}