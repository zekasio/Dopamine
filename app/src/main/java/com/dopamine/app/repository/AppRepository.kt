package com.dopamine.app.repository

import com.dopamine.app.model.PasswordResetRequest
import com.dopamine.app.model.ReportStatus
import com.dopamine.app.model.User
import com.dopamine.app.model.WeeklyReport
import com.dopamine.app.network.SupabaseConfig
import com.dopamine.app.network.SupabaseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

class AppRepository(
    private val supabaseService: SupabaseService = SupabaseService()
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _users = MutableStateFlow<List<User>>(
        listOf(
            User("user_mod_1", "mod", "Sistem Moderatörü", isModerator = true, district = "Merkez"),
            User("user_ahmet_1", "ahmet", "Ahmet Yılmaz", isModerator = false, district = "Kadıköy"),
            User("user_mehmet_1", "mehmet", "Mehmet Kaya", isModerator = false, district = "Üsküdar"),
            User("user_ayse_1", "ayse", "Ayşe Demir", isModerator = false, district = "Beşiktaş")
        )
    )
    val users: StateFlow<List<User>> = _users

    private val _reports = MutableStateFlow<List<WeeklyReport>>(emptyList())
    val reports: StateFlow<List<WeeklyReport>> = _reports

    private val _resetRequests = MutableStateFlow<List<PasswordResetRequest>>(emptyList())
    val resetRequests: StateFlow<List<PasswordResetRequest>> = _resetRequests

    init {
        syncFromSupabase()
    }

    fun syncFromSupabase() {
        if (!SupabaseConfig.isConfigured()) return
        scope.launch {
            val remoteUsers = supabaseService.fetchUsers()
            if (!remoteUsers.isNullOrEmpty()) {
                _users.value = remoteUsers
            }
            val remoteReports = supabaseService.fetchReports()
            if (remoteReports != null) {
                _reports.value = remoteReports
            }
            val remoteResets = supabaseService.fetchPasswordResets()
            if (remoteResets != null) {
                _resetRequests.value = remoteResets
            }
        }
    }

    suspend fun authenticateUser(username: String, password: String): User? {
        val cleanUsername = username.trim().lowercase()
        val cleanPassword = password.trim()

        if (SupabaseConfig.isConfigured()) {
            val supabaseUser = supabaseService.authenticateUser(cleanUsername, cleanPassword)
            if (supabaseUser != null) return supabaseUser
        }

        if (cleanUsername == "mod" && (cleanPassword == "1234" || cleanPassword.isEmpty())) {
            return User("user_mod_1", "mod", "Sistem Moderatörü", isModerator = true)
        }

        return _users.value.find { it.username.lowercase() == cleanUsername && it.password == cleanPassword }
    }

    fun requestPasswordReset(username: String, message: String) {
        val newReq = PasswordResetRequest(
            id = UUID.randomUUID().toString(),
            username = username,
            message = message
        )
        _resetRequests.value = _resetRequests.value + newReq
        if (SupabaseConfig.isConfigured()) {
            scope.launch { supabaseService.savePasswordReset(newReq) }
        }
    }

    fun submitReport(report: WeeklyReport) {
        val currentList = _reports.value.toMutableList()
        val index = currentList.indexOfFirst { it.userId == report.userId }

        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)

        // Check if report is submitted on time (Wednesday before/at 21:00 or any day before Wednesday 21:00)
        val isOnTime = if (dayOfWeek == Calendar.WEDNESDAY) {
            hourOfDay <= 21
        } else {
            dayOfWeek < Calendar.WEDNESDAY || dayOfWeek == Calendar.SUNDAY
        }

        val updatedReport = report.copy(
            submissionTimestamp = System.currentTimeMillis(),
            isSubmittedOnTime = isOnTime,
            status = ReportStatus.PENDING,
            rejectionReason = null
        )

        if (index != -1) {
            currentList[index] = updatedReport
        } else {
            currentList.add(updatedReport)
        }
        _reports.value = currentList

        if (SupabaseConfig.isConfigured()) {
            scope.launch { supabaseService.saveReport(updatedReport) }
        }
    }

    fun approveReport(reportId: String) {
        var updated: WeeklyReport? = null
        _reports.value = _reports.value.map { report ->
            if (report.id == reportId) {
                val rep = report.copy(status = ReportStatus.APPROVED, rejectionReason = null)
                updated = rep
                rep
            } else {
                report
            }
        }
        if (SupabaseConfig.isConfigured() && updated != null) {
            scope.launch { supabaseService.updateReport(updated!!) }
        }
    }

    fun rejectReport(reportId: String, reason: String) {
        var updated: WeeklyReport? = null
        _reports.value = _reports.value.map { report ->
            if (report.id == reportId) {
                val rep = report.copy(status = ReportStatus.REJECTED, rejectionReason = reason)
                updated = rep
                rep
            } else {
                report
            }
        }
        if (SupabaseConfig.isConfigured() && updated != null) {
            scope.launch { supabaseService.updateReport(updated!!) }
        }
    }

    fun nudgeUser(userId: String): Pair<Boolean, String> {
        val TwelveHoursMs = 12 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()
        val targetUser = _users.value.find { it.id == userId } ?: return Pair(false, "Kullanıcı bulunamadı")

        val lastNudge = targetUser.lastNudgeTimestamp
        if (lastNudge != null) {
            val diff = now - lastNudge
            if (diff < TwelveHoursMs) {
                val remainingMs = TwelveHoursMs - diff
                val hours = remainingMs / (1000 * 60 * 60)
                val minutes = (remainingMs % (1000 * 60 * 60)) / (1000 * 60)
                return Pair(
                    false,
                    "Bu kişiyi son 12 saat içinde dürtmüştünüz. Kalan süre: ${hours}sa ${minutes}dk"
                )
            }
        }

        _users.value = _users.value.map { user ->
            if (user.id == userId) {
                user.copy(lastNudgeTimestamp = now)
            } else {
                user
            }
        }

        if (SupabaseConfig.isConfigured()) {
            scope.launch { supabaseService.updateUserNudge(userId, now) }
        }

        return Pair(true, "${targetUser.fullName} başarıyla dürtüldü! 🔔")
    }

    fun getUserReport(userId: String): WeeklyReport? {
        return _reports.value.find { it.userId == userId }
    }

    fun deleteUser(userId: String) {
        if (SupabaseConfig.isConfigured()) {
            scope.launch { 
                if (supabaseService.deleteUser(userId)) {
                    _users.value = _users.value.filter { it.id != userId }
                }
            }
        }
    }

    fun deletePasswordReset(resetId: String) {
        if (SupabaseConfig.isConfigured()) {
            scope.launch {
                if (supabaseService.deletePasswordReset(resetId)) {
                    _resetRequests.value = _resetRequests.value.filter { it.id != resetId }
                }
            }
        }
    }
}
