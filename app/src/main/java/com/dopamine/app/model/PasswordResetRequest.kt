package com.dopamine.app.model

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetRequest(
    val id: String,
    val username: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
