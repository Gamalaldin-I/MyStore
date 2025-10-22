package com.example.domain.model.remoteModels

data class StoreEmployee(
    val id: String? = null,
    var email: String? = null,
    var name: String? = null,
    var role: Int? = null,
    var status: String?,
    var joinedAt: String? = null
) {
    constructor() : this(null, null, null, null, null, null)
}

