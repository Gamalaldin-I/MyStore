package com.example.domain.model.remoteModels

data class Invite(
    var acceptedAt: String? = null,
    val code: String? = null,
    var createdAt: String? = null,
    val storeId: String? = null,
    val ownerId: String? = null,
    val email: String? = null,
    var status: String? = null
) {
    constructor() : this(null, null, null, null, null, null, null)
}
