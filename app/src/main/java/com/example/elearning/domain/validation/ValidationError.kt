package com.example.elearning.domain.validation

enum class ValidationError {
    EMPTY_NAME,
    NAME_TOO_SHORT,
    EMPTY_EMAIL,
    INVALID_EMAIL,
    EMPTY_PASSWORD,
    PASSWORD_TOO_SHORT,
    PASSWORD_NEEDS_LETTER_AND_DIGIT,
    PASSWORDS_DO_NOT_MATCH
}
