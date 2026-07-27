package com.dopamine.app.network

import com.dopamine.app.model.PasswordResetRequest
import com.dopamine.app.model.User
import com.dopamine.app.model.WeeklyReport
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.delete
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SupabaseService {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    private val baseUrl: String get() = SupabaseConfig.SUPABASE_URL.trimEnd('/')
    private val anonKey: String get() = SupabaseConfig.SUPABASE_ANON_KEY.trim()

    suspend fun authenticateUser(username: String, password: String): User? {
        if (!SupabaseConfig.isConfigured()) return null
        return try {
            val url = "$baseUrl/rest/v1/users?username=eq.${username.lowercase().trim()}&password=eq.${password.trim()}&select=*"
            val response = client.get(url) {
                header("apikey", anonKey)
                header("Authorization", "Bearer $anonKey")
            }
            val bodyText = response.bodyAsText()
            val list = json.decodeFromString<List<User>>(bodyText)
            list.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchUsers(): List<User>? {
        if (!SupabaseConfig.isConfigured()) return null
        return try {
            val url = "$baseUrl/rest/v1/users?select=*"
            val response = client.get(url) {
                header("apikey", anonKey)
                header("Authorization", "Bearer $anonKey")
            }
            val bodyText = response.bodyAsText()
            json.decodeFromString<List<User>>(bodyText)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchReports(): List<WeeklyReport>? {
        if (!SupabaseConfig.isConfigured()) return null
        return try {
            val url = "$baseUrl/rest/v1/reports?select=*"
            val response = client.get(url) {
                header("apikey", anonKey)
                header("Authorization", "Bearer $anonKey")
            }
            val bodyText = response.bodyAsText()
            json.decodeFromString<List<WeeklyReport>>(bodyText)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveReport(report: WeeklyReport): Boolean {
        if (!SupabaseConfig.isConfigured()) return false
        return try {
            val url = "$baseUrl/rest/v1/reports"
            val jsonPayload = json.encodeToString(report)
            android.util.Log.d("SupabaseService", "saveReport URL: $url")
            android.util.Log.d("SupabaseService", "saveReport payload: $jsonPayload")
            val response = client.post(url) {
                header("apikey", anonKey)
                header("Authorization", "Bearer $anonKey")
                header("Prefer", "resolution=merge-duplicates")
                contentType(ContentType.Application.Json)
                setBody(jsonPayload)
            }
            val responseBody = response.bodyAsText()
            android.util.Log.d("SupabaseService", "saveReport response ${response.status.value}: $responseBody")
            response.status.value in 200..299
        } catch (e: Exception) {
            android.util.Log.e("SupabaseService", "saveReport error", e)
            false
        }
    }

    suspend fun updateReport(report: WeeklyReport): Boolean {
        if (!SupabaseConfig.isConfigured()) return false
        return try {
            val url = "$baseUrl/rest/v1/reports?id=eq.${report.id}"
            val jsonPayload = json.encodeToString(report)
            android.util.Log.d("SupabaseService", "updateReport URL: $url")
            val response = client.patch(url) {
                header("apikey", anonKey)
                header("Authorization", "Bearer $anonKey")
                contentType(ContentType.Application.Json)
                setBody(jsonPayload)
            }
            val responseBody = response.bodyAsText()
            android.util.Log.d("SupabaseService", "updateReport response ${response.status.value}: $responseBody")
            response.status.value in 200..299
        } catch (e: Exception) {
            android.util.Log.e("SupabaseService", "updateReport error", e)
            false
        }
    }

    suspend fun updateUserNudge(userId: String, timestamp: Long): Boolean {
        if (!SupabaseConfig.isConfigured()) return false
        return try {
            val url = "$baseUrl/rest/v1/users?id=eq.$userId"
            val jsonPayload = """{"last_nudge_timestamp": $timestamp}"""
            val response = client.patch(url) {
                header("apikey", anonKey)
                header("Authorization", "Bearer $anonKey")
                contentType(ContentType.Application.Json)
                setBody(jsonPayload)
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun savePasswordReset(request: PasswordResetRequest): Boolean {
        if (!SupabaseConfig.isConfigured()) return false
        return try {
            val url = "$baseUrl/rest/v1/password_resets"
            val jsonPayload = json.encodeToString(request)
            val response = client.post(url) {
                header("apikey", anonKey)
                header("Authorization", "Bearer $anonKey")
                contentType(ContentType.Application.Json)
                setBody(jsonPayload)
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun fetchPasswordResets(): List<PasswordResetRequest>? {
        if (!SupabaseConfig.isConfigured()) return null
        return try {
            val url = "$baseUrl/rest/v1/password_resets?select=*"
            val response = client.get(url) {
                header("apikey", anonKey)
                header("Authorization", "Bearer $anonKey")
            }
            val bodyText = response.bodyAsText()
            json.decodeFromString<List<PasswordResetRequest>>(bodyText)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deletePasswordReset(resetId: String): Boolean {
        if (!SupabaseConfig.isConfigured()) return false
        return try {
            val url = "$baseUrl/rest/v1/password_resets?id=eq.$resetId"
            val response = client.delete(url) {
                header("apikey", anonKey)
                header("Authorization", "Bearer $anonKey")
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteUser(userId: String): Boolean {
        if (!SupabaseConfig.isConfigured()) return false
        return try {
            val url = "$baseUrl/rest/v1/users?id=eq.$userId"
            val response = client.delete(url) {
                header("apikey", anonKey)
                header("Authorization", "Bearer $anonKey")
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    suspend fun saveUser(user: User): Boolean {
        if (!SupabaseConfig.isConfigured()) return false
        return try {
            val url = "$baseUrl/rest/v1/users"
            val jsonPayload = json.encodeToString(user)
            val response = client.post(url) {
                header("apikey", anonKey)
                header("Authorization", "Bearer $anonKey")
                contentType(ContentType.Application.Json)
                setBody(jsonPayload)
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateUser(user: User): Boolean {
        if (!SupabaseConfig.isConfigured()) return false
        return try {
            val url = "$baseUrl/rest/v1/users?id=eq.${user.id}"
            val jsonPayload = json.encodeToString(user)
            val response = client.patch(url) {
                header("apikey", anonKey)
                header("Authorization", "Bearer $anonKey")
                contentType(ContentType.Application.Json)
                setBody(jsonPayload)
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteReport(reportId: String): Boolean {
        if (!SupabaseConfig.isConfigured()) return false
        return try {
            val url = "$baseUrl/rest/v1/reports?id=eq.${reportId}"
            val response = client.delete(url) {
                header("apikey", anonKey)
                header("Authorization", "Bearer $anonKey")
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
