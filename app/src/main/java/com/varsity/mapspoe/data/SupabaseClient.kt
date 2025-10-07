package com.varsity.mapspoe.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {

    private const val SUPABASE_URL = "https://odjfrgemqfsaxqctlapk.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9kamZyZ2VtcWZzYXhxY3RsYXBrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk3NTQxMjIsImV4cCI6MjA3NTMzMDEyMn0.y1JYzhQ46YqM36A9_orCieFM557pQ93idKC_erZaEMM"

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
    }
}
