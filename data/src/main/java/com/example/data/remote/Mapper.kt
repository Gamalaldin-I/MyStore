package com.example.data.remote

import com.example.domain.model.Store
import com.example.domain.model.remoteModels.Employee
import com.example.domain.model.remoteModels.Invite
import com.example.domain.model.remoteModels.StoreEmployee

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
            "storeName" to this.storeName
        )
    }
    fun StoreEmployee.hash(): HashMap<String, Any?>{
        return hashMapOf(
            "id" to this.id,
            "email" to this.email,
            "name" to this.name,
            "role" to this.role,
            "status" to this.status,
            "joinedAt" to this.joinedAt)
    }
    fun Employee.hash(): HashMap<String, Any?>{
        return hashMapOf(
            "id" to this.id,
            "email" to this.email,
            "name" to this.name,
            "ownerId" to this.ownerId,
            "storeId" to this.storeId,
            "acceptedAt" to this.acceptedAt,
            "joinedAt" to this.joinedAt,
            "status" to this.status,
            "role" to this.role
        )}
    fun Store.hash():HashMap<String,Any?>{
        return hashMapOf(
            "name" to this.name,
            "phone" to this.phone,
            "location" to this.location,
        )
    }

}