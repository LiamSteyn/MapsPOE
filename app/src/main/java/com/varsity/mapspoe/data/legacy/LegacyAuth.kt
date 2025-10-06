package com.varsity.mapspoe.data.legacy

object LegacyAuth {
    private val users = mutableListOf(
        User(name = "Demo User", email = "demo@demo.com", password = "1234")
    )

    fun register(user: User): Boolean {
        if (users.any { it.email.equals(user.email, ignoreCase = true) }) return false
        users += user
        return true
    }

    fun login(email: String, password: String): Boolean =
        users.any { it.email.equals(email, ignoreCase = true) && it.password == password }

    fun users(): List<User> = users.toList()
}
