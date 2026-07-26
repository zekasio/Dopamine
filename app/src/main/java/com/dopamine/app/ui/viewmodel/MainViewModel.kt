package com.dopamine.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dopamine.app.model.PasswordResetRequest
import com.dopamine.app.model.ReportStatus
import com.dopamine.app.model.User
import com.dopamine.app.model.WeeklyReport
import com.dopamine.app.notification.AppNotificationManager
import com.dopamine.app.repository.AppRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: AppRepository = AppRepository()
    private val notificationManager = AppNotificationManager(application.applicationContext)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    val users: StateFlow<List<User>> = repository.users
    val reports: StateFlow<List<WeeklyReport>> = repository.reports
    val resetRequests: StateFlow<List<PasswordResetRequest>> = repository.resetRequests

    // Login state
    var loginUsername = MutableStateFlow("")
    var loginPassword = MutableStateFlow("")
    var loginError = MutableStateFlow<String?>(null)
    var isLoggingIn = MutableStateFlow(false)

    // Password reset dialog state
    var isResetDialogOpen = MutableStateFlow(false)
    var resetMessageInput = MutableStateFlow("Lütfen şifremi sıfırlayın.")
    var resetDialogSuccess = MutableStateFlow<String?>(null)

    // Moderator Reject Dialog state
    var isRejectDialogOpen = MutableStateFlow(false)
    var rejectingReportId = MutableStateFlow<String?>(null)
    var rejectionReasonInput = MutableStateFlow("")

    // Toast/Snackbar notifications
    var toastMessage = MutableStateFlow<String?>(null)

    private var previousReportStatus: ReportStatus? = null
    private var previousNudgeTimestamp: Long? = null

    init {
        startSupabaseRealtimePolling()
    }

    private fun startSupabaseRealtimePolling() {
        viewModelScope.launch {
            while (isActive) {
                delay(5000)
                repository.syncFromSupabase()

                // Check for status changes for notification
                val user = _currentUser.value
                if (user != null && !user.isModerator) {
                    val currentReport = repository.getUserReport(user.id)
                    if (currentReport != null) {
                        if (previousReportStatus != null && previousReportStatus != currentReport.status) {
                            if (currentReport.status == ReportStatus.APPROVED) {
                                notificationManager.sendApprovalNotification(user.fullName)
                            } else if (currentReport.status == ReportStatus.REJECTED) {
                                notificationManager.sendRejectionNotification(
                                    user.fullName,
                                    currentReport.rejectionReason ?: "Detay eksik"
                                )
                            }
                        }
                        previousReportStatus = currentReport.status
                    }
                    
                    val currentUserData = users.value.find { it.id == user.id }
                    if (currentUserData != null) {
                        val newNudge = currentUserData.lastNudgeTimestamp
                        if (previousNudgeTimestamp != null && newNudge != null && newNudge > previousNudgeTimestamp!!) {
                            notificationManager.sendNudgeNotification(user.fullName)
                        }
                        previousNudgeTimestamp = newNudge
                    }
                }
            }
        }
    }

    fun refreshData() {
        repository.syncFromSupabase()
    }

    fun login() {
        val username = loginUsername.value.trim()
        val password = loginPassword.value.trim()

        if (username.isEmpty() || password.isEmpty()) {
            loginError.value = "Lütfen kullanıcı adı ve şifrenizi girin"
            return
        }

        isLoggingIn.value = true
        loginError.value = null

        viewModelScope.launch {
            val authenticatedUser = repository.authenticateUser(username, password)
            isLoggingIn.value = false

            if (authenticatedUser != null) {
                _currentUser.value = authenticatedUser
                loginError.value = null
                toastMessage.value = "Hoş geldiniz, ${authenticatedUser.fullName} ✨"

                // Reminders
                val cal = Calendar.getInstance()
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                
                if (!authenticatedUser.isModerator) {
                    if (dayOfWeek == Calendar.WEDNESDAY && hour in 21..23) {
                        notificationManager.sendReminder(authenticatedUser.username, Calendar.WEDNESDAY, hour)
                    } else if (dayOfWeek == Calendar.SUNDAY && hour in 12..23) {
                        notificationManager.sendReminder(authenticatedUser.username, Calendar.SUNDAY, hour)
                    }
                }
            } else {
                loginError.value = "Giriş başarısız! Kullanıcı adı veya şifre hatalı."
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        loginUsername.value = ""
        loginPassword.value = ""
        loginError.value = null
    }

    fun sendPasswordResetRequest() {
        val username = loginUsername.value.ifEmpty { "Kullanıcı" }
        repository.requestPasswordReset(username, resetMessageInput.value)
        resetDialogSuccess.value = "Sıfırlama talebiniz veritabanına ve yöneticiye iletildi."
        toastMessage.value = "Sıfırlama talebiniz veritabanına iletildi!"
    }

    fun submitReport(
        newMembers: Int,
        homeVisits: Int,
        shopVisits: Int,
        bookGifts: Int,
        brochureDist: Int,
        stickerPasting: Int,
        logoGifts: Int,
        fieldWorkParticipants: String
    ) {
        val user = _currentUser.value ?: return

        val report = WeeklyReport(
            id = user.id,
            userId = user.id,
            username = user.username,
            userFullName = user.fullName,
            newMembersCount = newMembers,
            homeVisitsCount = homeVisits,
            shopVisitsCount = shopVisits,
            bookGiftsCount = bookGifts,
            brochureDistributionCount = brochureDist,
            stickerPastingCount = stickerPasting,
            logoGiftsCount = logoGifts,
            fieldWorkParticipants = fieldWorkParticipants,
            district = user.district,
            status = ReportStatus.PENDING
        )

        repository.submitReport(report)
        toastMessage.value = "Raporunuz Supabase veritabanına gönderildi! 🚀"
    }

    fun approveReport(reportId: String) {
        repository.approveReport(reportId)
        toastMessage.value = "Rapor onaylandı! (Bildirim kullanıcıya iletildi)"
    }

    fun openRejectDialog(reportId: String) {
        rejectingReportId.value = reportId
        rejectionReasonInput.value = ""
        isRejectDialogOpen.value = true
    }

    fun confirmRejectReport() {
        val reportId = rejectingReportId.value ?: return
        val reason = rejectionReasonInput.value.trim()
        if (reason.isEmpty()) {
            toastMessage.value = "Lütfen red nedenini yazın"
            return
        }

        repository.rejectReport(reportId, reason)
        isRejectDialogOpen.value = false
        rejectingReportId.value = null
        toastMessage.value = "Rapor reddedildi! (Bildirim kullanıcıya iletildi)"
    }

    fun nudgeUser(targetUserId: String) {
        val result = repository.nudgeUser(targetUserId)
        toastMessage.value = result.second
    }

    fun clearToast() {
        toastMessage.value = null
    }

    fun getCurrentUserReport(): WeeklyReport? {
        val uid = _currentUser.value?.id ?: return null
        return repository.getUserReport(uid)
    }
    fun deleteUser(userId: String) {
        repository.deleteUser(userId)
        toastMessage.value = "Kullanıcı silindi"
    }

    fun deletePasswordReset(resetId: String) {
        repository.deletePasswordReset(resetId)
        toastMessage.value = "Talep silindi"
    }
}
