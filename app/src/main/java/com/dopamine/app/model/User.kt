package com.dopamine.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    @SerialName("full_name")
    val fullName: String,
    val password: String = "1234",
    @SerialName("is_moderator")
    val isModerator: Boolean = false,
    @SerialName("last_nudge_timestamp")
    val lastNudgeTimestamp: Long? = null,
    val district: String = "",
    @SerialName("fcm_token")
    val fcmToken: String? = null
)
