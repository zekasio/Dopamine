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
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import org.json.JSONArray

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
            if (supabaseUser != null) {
                com.onesignal.OneSignal.login(supabaseUser.id)
                return supabaseUser
            }
        }

        if (cleanUsername == "mod" && (cleanPassword == "1234" || cleanPassword.isEmpty())) {
            com.onesignal.OneSignal.login("user_mod_1")
            return User("user_mod_1", "mod", "Sistem Moderatörü", isModerator = true)
        }

        val localUser = _users.value.find { it.username.lowercase() == cleanUsername && it.password == cleanPassword }
        if (localUser != null) {
            com.onesignal.OneSignal.login(localUser.id)
        }
        return localUser
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

        // Check if report is submitted on time
        val isOnTime = if (dayOfWeek == Calendar.WEDNESDAY) {
            hourOfDay <= 21
        } else if (dayOfWeek == Calendar.SUNDAY) {
            hourOfDay <= 21 // Assuming 21:00 as deadline for Sunday too
        } else {
            true
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
        
        // Notify moderators
        scope.launch {
            val user = _users.value.find { it.id == report.userId }
            sendOneSignalNotificationToModerators(
                "Yeni Rapor Gönderildi",
                "${user?.fullName ?: "Bir kullanıcı"} haftalık raporunu sisteme yükledi."
            )
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

        // Send push notification
        scope.launch {
            sendOneSignalNotification(
                targetUserId = targetUser.id,
                title = "Dürtüldünüz!",
                message = "Moderatör raporunuzu göndermenizi hatırlatıyor. Lütfen en kısa sürede raporunuzu girin."
            )
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

    fun saveUser(user: User) {
        val currentUsers = _users.value.toMutableList()
        currentUsers.add(user)
        _users.value = currentUsers
        if (SupabaseConfig.isConfigured()) {
            scope.launch { supabaseService.saveUser(user) }
        }
    }

    fun updateUser(user: User) {
        val currentUsers = _users.value.toMutableList()
        val index = currentUsers.indexOfFirst { it.id == user.id }
        if (index != -1) {
            currentUsers[index] = user
            _users.value = currentUsers
        }
        if (SupabaseConfig.isConfigured()) {
            scope.launch { supabaseService.updateUser(user) }
        }
    }

    private fun sendOneSignalNotification(targetUserId: String, title: String, message: String) {
        sendOneSignalRequest(listOf(targetUserId), title, message)
    }

    private fun sendOneSignalNotificationToModerators(title: String, message: String) {
        val modIds = _users.value.filter { it.isModerator }.map { it.id }
        if (modIds.isNotEmpty()) {
            sendOneSignalRequest(modIds, title, message)
        }
    }

    private fun sendOneSignalRequest(targetUserIds: List<String>, title: String, message: String) {
        try {
            val url = URL("https://onesignal.com/api/v1/notifications")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Authorization", "Basic os_v2_app_q5cjfot66rasxgyy4t6ghh6kolt26sdqhckexq4upwtzu3vjfta4qbksbkwxldm2ngzf6kaiwikw26nhljda6njpbpcbbvvbvlvc36a")
            connection.doOutput = true

            val jsonBody = JSONObject().apply {
                put("app_id", "874492ba-7ef4-412b-9b18-e4fc639fca72")
                put("target_channel", "push")
                put("include_aliases", JSONObject().apply {
                    put("external_id", JSONArray(targetUserIds))
                })
                put("headings", JSONObject().put("en", title).put("tr", title))
                put("contents", JSONObject().put("en", message).put("tr", message))
            }

            val os = connection.outputStream
            val input = jsonBody.toString().toByteArray(Charsets.UTF_8)
            os.write(input, 0, input.size)
            os.flush()
            os.close()

            val responseCode = connection.responseCode
            println("OneSignal Response: $responseCode")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
