package com.example.data.remote.repo

import android.util.Log
import com.example.data.local.sharedPrefs.SharedPref
import com.example.domain.model.Store
import com.example.domain.model.User
import com.example.domain.model.remoteModels.EmployeeInvitationAcceptanceResponse
import com.example.domain.model.remoteModels.Invitation
import com.example.domain.repo.InvitationsRepo
import com.example.domain.useCase.notifications.InsertNotificationUseCase
import com.example.domain.util.Constants
import com.example.domain.util.DateHelper
import com.example.domain.util.NotificationManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class InvitationsRepoImp(
    private val supabase: SupabaseClient,
    private val pref: SharedPref,
    private val notificationSender: InsertNotificationUseCase
) : InvitationsRepo {

    companion object {
        private const val INVITES = "invitations"
        private const val USERS = "users"
        private const val TAG = "INVITATIONS_REPO"
        private const val STORES = "stores"
    }

    // Cache user and store to avoid repeated SharedPrefs reads
    private var cachedUser: User = pref.getUser()
    private var cachedStore: Store = pref.getStore()

    override suspend fun createInvitation(email: String, code: String): Pair<Boolean, String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val invitation = Invitation(
                    id = code,
                    code = code,
                    email = email,
                    storeId = cachedStore.id,
                    storeIcon = cachedStore.logoUrl,
                    storeName = cachedStore.name,
                    createdAt = DateHelper.getCurrentTimestampTz(),
                    status = Constants.STATUS_PENDING,
                    acceptedAt = ""
                )

                supabase.from(INVITES).insert(invitation)
                true to "Invitation created successfully"
            }.getOrElse { e ->
                Log.e(TAG, "Error creating invitation", e)
                false to "Error creating invitation"
            }
        }

    override suspend fun deleteInvite(invite: Invitation): Pair<Boolean, String> =
        withContext(Dispatchers.IO) {
            runCatching {
                supabase.from(INVITES).delete {
                    filter { eq("id", invite.id!!) }
                }
                true to "Invitation deleted successfully"
            }.getOrElse {
                false to "Error deleting invitation"
            }
        }

    override suspend fun getAllInvitesForStore(): Pair<List<Invitation>, String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val list = supabase.from(INVITES).select {
                    filter { eq("storeId", cachedStore.id) }
                }.decodeList<Invitation>()
                list to "Invitations fetched successfully"
            }.getOrElse {
                emptyList<Invitation>() to "Error fetching invitations"
            }
        }

    override suspend fun getAllEmailPendingInvites(): Pair<List<Invitation>, String> =
        withContext(Dispatchers.IO) {
            runCatching {
                // Single filter with multiple conditions is more efficient
                val list = supabase.from(INVITES).select {
                    filter {
                        eq("email", cachedUser.email)
                        eq("status", Constants.STATUS_PENDING)
                    }
                }.decodeList<Invitation>()
                list to "Invitations fetched successfully"
            }.getOrElse {
                emptyList<Invitation>() to "Error fetching invitations"
            }
        }

    override suspend fun acceptInvite(invite: Invitation, code: String): Pair<Boolean, String> {
        // Early return for validation
        if (code != invite.code) {
            return false to "Invalid invitation code"
        }

        return withContext(Dispatchers.IO) {
            runCatching {
                // Use coroutineScope for parallel execution
                coroutineScope {
                    // Update invitation status and hire employee in parallel
                    val updateJob = async {
                        supabase.from(INVITES).update(
                            mapOf(
                                "status" to Constants.STATUS_ACCEPTED,
                                "acceptedAt" to System.currentTimeMillis().toString()
                            )
                        ) {
                            filter { eq("id", invite.id!!) }
                        }
                    }

                    val hireJob = async {
                        hireTheEmployee(invite.storeId!!)
                    }

                    // Wait for both operations to complete
                    updateJob.await()
                    hireJob.await()
                }

                true to "Invitation accepted successfully ✅"
            }.getOrElse { e ->
                Log.e(TAG, "Error accepting invitation", e)
                false to (e.message ?: "Error accepting invitation ❌")
            }
        }
    }

    private suspend fun hireTheEmployee(storeId: String) {
        coroutineScope {
            // Execute user update and store fetch in parallel
            val userUpdateJob = async {
                supabase.from(USERS).update(
                    EmployeeInvitationAcceptanceResponse(
                        storeId = storeId,
                        status = Constants.STATUS_HIRED,
                        role = Constants.EMPLOYEE_ROLE
                    )
                ) {
                    filter { eq("id", cachedUser.id) }
                }
            }

            val storeFetchJob = async {
                supabase.from(STORES).select {
                    filter { eq("id", storeId) }
                }.decodeSingle<Store>()
            }

            // Wait for both operations
            userUpdateJob.await()
            val store = storeFetchJob.await()

            // Update cached values and save to preferences
            cachedUser = cachedUser.copy(
                storeId = storeId,
                role = Constants.EMPLOYEE_ROLE,
                status = Constants.STATUS_HIRED
            )
            cachedStore = store

            pref.saveUser(cachedUser)
            pref.saveStore(cachedStore)

            // Send notification asynchronously (fire and forget)
            val joinedNotification = NotificationManager.createAddUserNotification(
                cachedUser,
                cachedStore.id,
                cachedUser.name
            )
            notificationSender(joinedNotification)
        }
    }

    override suspend fun rejectInvite(invite: Invitation): Pair<Boolean, String> =
        withContext(Dispatchers.IO) {
            runCatching {
                supabase.from(INVITES).update(
                    mapOf(
                        "status" to Constants.STATUS_REJECTED,
                        "acceptedAt" to System.currentTimeMillis().toString()
                    )
                ) {
                    filter { eq("id", invite.id!!) }
                }
                true to "Rejected"
            }.getOrElse {
                false to "Error rejecting invitation"
            }
        }

    override fun sendEmail(code: String): Pair<String, String> = runCatching {
        val storeName = cachedStore.name
        val subject = "Your Invitation to Join $storeName | Action Required"
        val emailBody = buildEmailContent(code)
        subject to emailBody
    }.getOrElse { e ->
        "Error" to "Unable to send invitation: ${e.message}"
    }

    // Optimized email content builder using buildString for better performance
    private fun buildEmailContent(code: String): String = buildString {
        append("Dear Team Member,\n\n")
        append("Congratulations! We are pleased to extend an official invitation for you to join the ")
        append(cachedStore.name)
        append(" team.\n\n")
        append("After careful consideration, we believe your skills and expertise will be a valuable addition to our organization. ")
        append("We are excited about the opportunity to work together and achieve great results.\n\n\n")

        append("NEXT STEPS - ACTION REQUIRED\n")
        append("━".repeat(42))
        append("\n\n")
        append("To activate your account and complete your onboarding, please use your unique invitation code:\n\n")
        append("🔐 Invitation Code: ")
        append(code)
        append("\n\n")
        append("1. Open the Stora application\n")
        append("2. Navigate to the invitation code section\n")
        append("3. Enter the code above to validate your access\n")
        append("4. Complete your profile setup\n\n")
        append("This code is confidential and intended solely for your use. Please do not share it with anyone.\n\n\n")

        append("WHAT YOU'LL GAIN\n")
        append("━".repeat(42))
        append("\n\n")
        append("✓ Seamless team collaboration tools\n")
        append("✓ Real-time communication with your team\n")
        append("✓ Access to store management features\n")
        append("✓ Professional development opportunities\n")
        append("✓ Streamlined workflow and task management\n\n\n")

        append("NEED ASSISTANCE?\n")
        append("━".repeat(42))
        append("\n\n")
        append("Our team is here to support you every step of the way:\n\n")
        append("📍 Location: ")
        append(cachedStore.location)
        append("\n")
        append("📞 Phone: ")
        append(cachedStore.phone)
        append("\n")
        append("📧 Reply to this email for any questions\n\n")
        append("We recommend completing your registration within the next 24 hours to ensure uninterrupted access to all features.\n\n")
        append("We look forward to welcoming you aboard and building success together!\n\n\n")

        append("Warm regards,\n\n")
        append("The ")
        append(cachedStore.name)
        append(" Management Team\n")
        append("Powered by Stora\n\n\n")
        append("─".repeat(43))
        append("\n")
        append("This is an automated message from Stora. Please do not reply directly to this email address.\n")
        append("For support, please contact your store manager or use the contact information provided above.")
    }
}