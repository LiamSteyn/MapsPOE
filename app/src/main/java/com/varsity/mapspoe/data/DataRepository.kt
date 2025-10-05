package com.varsity.mapspoe.data

// Singleton object that stores app data in-memory
object DataRepository {

    // List to hold registered users
    private val users = mutableListOf<User>()

    // List to hold dispensary data
    private val dispensaries = mutableListOf<Dispensary>()

    // Initialization block to pre-populate some dispensary data
    init {
        dispensaries.addAll(
            listOf(
                // Each Dispensary has an ID, name, address, distance, rating, and open/closed status
                Dispensary(1, "Green Leaf Dispensary", "123 Main St, Cape Town", "1.2 km", 4.6, true),
                Dispensary(2, "Herbal House", "45 Long Street, Cape Town", "2.3 km", 4.2, false),
                Dispensary(3, "Mary Jane Market", "78 Bree St, Cape Town", "3.1 km", 4.9, true)
            )
        )
    }

    /**
     * Registers a new user.
     * @param user The user to register.
     * @return true if registration is successful, false if a user with the same email already exists.
     */
    fun registerUser(user: User): Boolean {
        // Check if user already exists based on email
        if (users.any { it.email == user.email }) return false
        // Add new user to the list
        users.add(user)
        return true
    }

    /**
     * Checks if the provided email and password match a registered user.
     * @param email The email entered by the user.
     * @param password The password entered by the user.
     * @return true if credentials are valid, false otherwise.
     */
    fun login(email: String, password: String): Boolean {
        return users.any { it.email == email && it.password == password }
    }

    /**
     * Returns the list of dispensaries.
     * @return List of Dispensary objects.
     */
    fun getDispensaries(): List<Dispensary> = dispensaries
}
