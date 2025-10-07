package com.varsity.mapspoe.data

import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.runBlocking

// Singleton object for handling app data and Supabase interaction
object DataRepository {

    // Local list of dispensaries (still in-memory)
    private val dispensaries = mutableListOf<Dispensary>()

    // In-memory cache of users (optional, used to sync or for local operations)
    private val users = mutableListOf<User>()

    // Initialize sample dispensary data
    init {
        dispensaries.addAll(
            listOf(
                Dispensary(
                    1,
                    "The 420 Doctor - Rondebosch",
                    "Rondebosch Court, Fountain Square, Rondebosch, Cape Town, 7701, South Africa",
                    "1.2 km",
                    4.6,
                    true,
                    googlePlaceId = "places/ChIJdy6oxx5dzB0RiWyZzXOX_mg"
                ),
                Dispensary(
                    2,
                    "The 420 Doctor - 24/7 Medical Cannabis Store",
                    "14 Marine Rd, Sea Point, Cape Town, 8005, South Africa",
                    "2.3 km",
                    4.2,
                    false,
                    googlePlaceId = "places/ChIJLQvECFxnzB0R2q4F6iCgsyk"
                ),
                Dispensary(
                    3,
                    "Cannabis Life",
                    "111 Ottery Rd, Wynberg, Cape Town, 7800, South Africa",
                    "3.1 km",
                    4.9,
                    true,
                    googlePlaceId = "places/ChIJD5HmgdpDzB0RIzdRt-2HQK0"
                )
            )
        )
    }

    /**
     * Registers a new user by inserting into Supabase "users" table.
     * Also caches the user locally for quick lookup.
     * Returns true if registration is successful, false if user already exists or failed.
     */
    @JvmStatic
    fun registerUser(user: User): Boolean = runBlocking {
        try {
            val client = SupabaseClient.client

            // Check if a user with the same email already exists
            val existingUsers = client.from("users")
                .select {
                    filter {
                        eq("email", user.email ?: "")
                    }
                }
                .decodeList<User>()  // this is a suspend function, will await automatically in runBlocking

            if (existingUsers.isNotEmpty()) {
                // User already exists
                return@runBlocking false
            }

            // Insert new user record into the Supabase table
            client.from("users").insert(
                mapOf(
                    "name" to (user.name ?: ""),
                    "email" to (user.email ?: ""),
                    "password" to (user.password ?: "")

                )


            ).decodeList<User>() // await the result here

            // Add to local cache
            users.add(user)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    /**
     * Logs in a user by checking if email and password match in Supabase.
     * Returns true if credentials are valid, false otherwise.
     */
    @JvmStatic
    fun login(email: String, password: String): Boolean = runBlocking {
        try {
            val client = SupabaseClient.client

            // Query Supabase "users" table for matching credentials
            val foundUsers = client.from("users")
                .select {
                    filter {
                        eq("email", email)
                        eq("password", password)
                    }
                }
                .decodeList<User>() // suspend function, auto-await in runBlocking

            if (foundUsers.isNotEmpty()) {
                // Cache user locally if not already cached
                val loggedUser = foundUsers.first()
                if (!users.any { it.email == loggedUser.email }) {
                    users.add(loggedUser)
                }
                true
            } else {
                false
            }
        } catch (e: HttpRequestException) {
            e.printStackTrace()
            false
        }
    }


    /**
     * Returns all dispensaries currently stored in memory.
     */
    @JvmStatic
    fun getDispensaries(): List<Dispensary> = dispensaries

    /**
     * Returns a list of users cached in memory (for Java interop).
     */
    @JvmStatic
    fun getUsers(): List<User> = users
}
