package com.dopamine.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ReportStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Serializable
data class WeeklyReport(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val username: String,
    @SerialName("user_full_name")
    val userFullName: String,
    @SerialName("new_members_count")
    val newMembersCount: Int = 0,
    @SerialName("home_visits_count")
    val homeVisitsCount: Int = 0,
    @SerialName("shop_visits_count")
    val shopVisitsCount: Int = 0,
    @SerialName("book_gifts_count")
    val bookGiftsCount: Int = 0,
    @SerialName("brochure_distribution_count")
    val brochureDistributionCount: Int = 0,
    @SerialName("sticker_pasting_count")
    val stickerPastingCount: Int = 0,
    @SerialName("logo_gifts_count")
    val logoGiftsCount: Int = 0,
    @SerialName("field_work_participants")
    val fieldWorkParticipants: String = "",
    @SerialName("submission_timestamp")
    val submissionTimestamp: Long = System.currentTimeMillis(),
    @SerialName("is_submitted_on_time")
    val isSubmittedOnTime: Boolean = true,
    val status: ReportStatus = ReportStatus.PENDING,
    @SerialName("rejection_reason")
    val rejectionReason: String? = null,
    val district: String = ""
)
