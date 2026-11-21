package com.example.elearning.domain.validation

object AuthValidator {
    private val emailPattern = Regex("""[^\s@]+@[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)+""")

    fun validateName(name: String): ValidationError? = when {
        name.isBlank() -> ValidationError.EMPTY_NAME
        name.trim().length < 2 -> ValidationError.NAME_TOO_SHORT
        else -> null
    }

    fun validateEmail(email: String): ValidationError? = when {
        email.isBlank() -> ValidationError.EMPTY_EMAIL
        !emailPattern.matches(email.trim()) -> ValidationError.INVALID_EMAIL
        else -> null
    }

    fun validatePassword(password: String): ValidationError? = when {
        password.isBlank() -> ValidationError.EMPTY_PASSWORD
        password.length < 8 -> ValidationError.PASSWORD_TOO_SHORT
        !password.codePoints().anyMatch(Character::isLetter) ||
            !password.codePoints().anyMatch(Character::isDigit) -> ValidationError.PASSWORD_NEEDS_LETTER_AND_DIGIT
        else -> null
    }

    fun validatePasswordConfirmation(password: String, confirmation: String): ValidationError? =
        if (password == confirmation) null else ValidationError.PASSWORDS_DO_NOT_MATCH

    fun validateLogin(email: String, password: String): List<ValidationError> =
        listOfNotNull(validateEmail(email), ValidationError.EMPTY_PASSWORD.takeIf { password.isBlank() })

    fun validateRegistration(name: String, email: String, password: String, confirmation: String): List<ValidationError> =
        listOfNotNull(validateName(name), validateEmail(email), validatePassword(password),
            validatePasswordConfirmation(password, confirmation))
}
