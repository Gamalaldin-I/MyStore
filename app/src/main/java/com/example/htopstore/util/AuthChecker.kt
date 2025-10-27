package com.example.htopstore.util

import android.util.Patterns

/**
 * Utility object for authentication and data validation.
 * Provides methods to validate email, password, phone numbers, and Egyptian National IDs.
 */
object AuthChecker {

    private const val MIN_PASSWORD_LENGTH = 8
    private const val MIN_NAME_LENGTH = 3
    private const val EGYPTIAN_PHONE_LENGTH = 13
    private const val EGYPTIAN_NID_LENGTH = 14

    private val EGYPTIAN_PHONE_PREFIXES = listOf("+2010", "+2011", "+2012", "+2015")

    /**
     * Validates email address format.
     * @param email The email string to validate
     * @return true if the email is valid, false otherwise
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false

        return email.endsWith(".com") &&
                email.contains('@') &&
                !email.contains(' ') &&
                email.indexOf(".com") - email.indexOf('@') >= 2 &&
                email.indexOf('@') > 0 &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Checks if password meets minimum length requirement.
     * @param password The password to check
     * @return true if password is 8 characters or longer
     */
    fun isPasswordLengthValid(password: String): Boolean {
        return password.length >= MIN_PASSWORD_LENGTH
    }

    /**
     * Evaluates password strength based on character diversity.
     * @param password The password to evaluate
     * @return String indicating password strength: "Less than 8", "Weak", "Medium", or "Strong"
     */
    fun getPasswordStrength(password: String): String {
        if (password.length < MIN_PASSWORD_LENGTH) return "Less than 8"

        var hasUpperCase = false
        var hasLowerCase = false
        var hasDigit = false
        var hasSpecialChar = false

        password.forEach { char ->
            when {
                char.isUpperCase() -> hasUpperCase = true
                char.isLowerCase() -> hasLowerCase = true
                char.isDigit() -> hasDigit = true
                else -> hasSpecialChar = true
            }
        }

        val criteriaCount = listOf(hasUpperCase, hasLowerCase, hasDigit, hasSpecialChar)
            .count { it }

        return when (criteriaCount) {
            1 -> "Weak"
            2 -> "Weak"
            3 -> "Medium"
            4 -> "Strong"
            else -> ""
        }
    }

    /**
     * Validates that a name contains only letters and spaces, with minimum length.
     * @param name The name to validate
     * @return true if name is valid
     */
    fun isValidName(name: String): Boolean {
        return name.isNotBlank() &&
                name.any { it.isLetter() } &&
                name.all { it.isLetter() || it == ' ' } &&
                name.length >= MIN_NAME_LENGTH
    }

    /**
     * Validates Egyptian phone number format.
     * Must start with Egyptian mobile prefix (+2010, +2011, +2012, or +2015)
     * and be exactly 13 characters long.
     * @param phone The phone number to validate
     * @return true if phone number is valid
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        if (phone.length != EGYPTIAN_PHONE_LENGTH) return false

        val hasValidPrefix = EGYPTIAN_PHONE_PREFIXES.any { phone.startsWith(it) }
        if (!hasValidPrefix) return false

        // Check that all characters except the '+' are digits
        return phone.drop(1).all { it.isDigit() }
    }

    /**
     * Validates Egyptian National ID (SSN).
     * @param ssn The national ID to validate
     * @return true if the ID is valid
     */
    fun isValidSSN(ssn: String): Boolean {
        return isValidEgyptianNationalID(ssn)
    }

    /**
     * Validates Egyptian National ID format and rules.
     * - Must be exactly 14 digits
     * - First digit indicates century (2 for 1900s, 3 for 2000s)
     * - Contains valid date of birth
     * - Contains valid governorate code (1-88)
     *
     * @param id The national ID string
     * @return true if the ID follows Egyptian National ID rules
     */
    private fun isValidEgyptianNationalID(id: String): Boolean {
        if (id.length != EGYPTIAN_NID_LENGTH) return false
        if (!id.all { it.isDigit() }) return false

        val century = id[0]
        if (century != '2' && century != '3') return false

        // Century validation: 2 (1950-1999), 3 (2000-2007)
        val year = id.substring(1, 3).toIntOrNull() ?: return false
        if (century == '2' && year < 50) return false
        if (century == '3' && year > 7) return false

        // Date validation
        val month = id.substring(3, 5).toIntOrNull() ?: return false
        val day = id.substring(5, 7).toIntOrNull() ?: return false

        if (month !in 1..12 || day !in 1..31) return false

        // Governorate code validation
        val governorateCode = id.substring(7, 9).toIntOrNull() ?: return false
        return governorateCode in 1..88
    }
}