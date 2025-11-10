package com.example.data.remote.repo

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.remoteModels.Invite
import com.example.domain.repo.StaffRepo
import io.github.jan.supabase.SupabaseClient

class StaffRepoImp(
    private val supabase: SupabaseClient,
    private val pref: SharedPref
) : StaffRepo {

    companion object{
        private const val TAG = "StaffRepoImp"
        //private const val INVITES = "invites"
        private const val USERS = "users"
        private const val STORES = "stores"
        private const val STORE_NAME="name"
        private const val STORE_LOCATION="location"
        private const val STORE_PHONE="phone"

    }


    override fun addInvite(
        email: String,
        code: String,
        onResult: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun deleteInvite(
        invite: Invite,
        onResult: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun listenToEmployees() {
        TODO("Not yet implemented")
    }


    override fun getAllInvitesForEmployee(onResult: (Boolean, String) -> Unit) {
        onResult(true, "Success")
    }

    override fun acceptInvite(
        invite: Invite,
        code: String,
        onResult: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun rejectInvite(
        invite: Invite,
        onResult: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun rejectOrRehireEmployee(
        employeeId: String,
        reject: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }



    override fun sendEmail(
        context: Context,
        recipientEmail: String,
        code: String
    ): Pair<String, String> {
        return try {
            val storeName = pref.getStore().name
            val subject = "Your Invitation to Join $storeName | Action Required"
            val emailBody = getEmailContent(code)

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, emailBody)
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(Intent.createChooser(intent, "Send invitation via email"))
                Pair("Success", "Email invitation sent successfully")
            } else {
                Pair("Error", "No email application available on this device")
            }
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