package com.dopamine.app.network

object SupabaseConfig {
    // Embedded fixed Supabase Credentials (never changeable by end-users)
    const val SUPABASE_URL: String = "https://wabwjiwvtscppensgxrv.supabase.co"
    const val SUPABASE_ANON_KEY: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndhYndqaXd2dHNjcHBlbnNneHJ2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODUwMDMyOTUsImV4cCI6MjEwMDU3OTI5NX0.M-mRVaacAWnPxhS-3esVq91eiYwB53WhH_rWBtvK9JI"

    fun isConfigured(): Boolean {
        return SUPABASE_URL.isNotBlank() &&
                !SUPABASE_URL.contains("YOUR_SUPABASE_PROJECT") &&
                SUPABASE_ANON_KEY.isNotBlank() &&
                !SUPABASE_ANON_KEY.contains("YOUR_SUPABASE_ANON_KEY")
    }
}
