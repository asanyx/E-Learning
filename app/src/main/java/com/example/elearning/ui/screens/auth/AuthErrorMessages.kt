package com.example.elearning.ui.screens.auth

import androidx.annotation.StringRes
import com.example.elearning.domain.AuthError
import com.example.elearning.domain.validation.ValidationError
import com.example.platformaELearningowa.R

@StringRes
internal fun ValidationError.messageRes(): Int = when (this) {
    ValidationError.EMPTY_NAME -> R.string.auth_empty_name
    ValidationError.NAME_TOO_SHORT -> R.string.auth_name_too_short
    ValidationError.EMPTY_EMAIL -> R.string.auth_empty_email
    ValidationError.INVALID_EMAIL -> R.string.auth_invalid_email
    ValidationError.EMPTY_PASSWORD -> R.string.auth_empty_password
    ValidationError.PASSWORD_TOO_SHORT -> R.string.auth_password_too_short
    ValidationError.PASSWORD_NEEDS_LETTER_AND_DIGIT -> R.string.auth_password_needs_letter_and_digit
    ValidationError.PASSWORDS_DO_NOT_MATCH -> R.string.auth_passwords_do_not_match
}

@StringRes
internal fun AuthError.messageRes(): Int = when (this) {
    AuthError.INVALID_CREDENTIALS -> R.string.auth_invalid_credentials
    AuthError.EMAIL_ALREADY_IN_USE -> R.string.auth_email_already_in_use
    AuthError.WEAK_PASSWORD -> R.string.auth_weak_password
    AuthError.NETWORK -> R.string.auth_network
    AuthError.UNKNOWN -> R.string.auth_unknown
}
