package com.example.htopstore.util.firebase

import com.example.domain.model.remoteModels.Invite

object Mapper {

    fun Invite.hash(): HashMap<String, Any?>{
        return hashMapOf(
            "acceptedAt" to this.acceptedAt,
            "createdAt" to this.createdAt,
            "email" to this.email,
            "code" to this.code,
            "status" to this.status,
            "ownerId" to this.ownerId,
            "storeId" to this.storeId,
        )
    }

    fun HashMap<String,Any?>.toInvite(id:String):Invite{
        return Invite(
            acceptedAt = this["acceptedAt"] as String?,
            createdAt = this["createdAt"] as String?,
            email = this["email"] as String?,
            code = this["code"] as String?,
            status = this["status"] as String?,
            ownerId = this["ownerId"] as String?,
            storeId = this["storeId"] as String?,
        )
    }
}