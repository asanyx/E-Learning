package com.example.elearning.domain.validation

import com.example.elearning.domain.validation.ValidationError.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorTest {
    private val validator = AuthValidator

    @Test
    fun validRegistrationAcceptsPolishLettersAndTrimsNameAndEmail() {
        val errors = validator.validateRegistration(" Łą ", " ala@example.pl ", "żółćźąę1", "żółćźąę1")
        assertEquals(emptyList<ValidationError>(), errors)
    }

    @Test
    fun blankFieldsReportEmptyErrors() {
        for (blank in listOf("", "   ")) {
            assertEquals(listOf(EMPTY_NAME, EMPTY_EMAIL, EMPTY_PASSWORD),
                validator.validateRegistration(blank, blank, blank, blank))
            assertEquals(listOf(EMPTY_EMAIL, EMPTY_PASSWORD), validator.validateLogin(blank, blank))
        }
    }

    @Test
    fun malformedEmailIsRejected() {
        for (email in listOf("ala.example.pl", "ala@", "ala@localhost", "a la@example.pl")) {
            assertEquals(INVALID_EMAIL, validator.validateEmail(email))
        }
    }

    @Test
    fun registrationReportsErrorsAcrossAllFields() {
        assertEquals(listOf(NAME_TOO_SHORT, INVALID_EMAIL, PASSWORD_TOO_SHORT, PASSWORDS_DO_NOT_MATCH),
            validator.validateRegistration(" A ", "ala", "abcdef1", "abcdef2"))
    }

    @Test
    fun passwordRequiresBothLettersAndDigits() {
        for (password in listOf("abcdefgh", "12345678")) {
            assertEquals(PASSWORD_NEEDS_LETTER_AND_DIGIT, validator.validatePassword(password))
        }
        assertNull(validator.validatePassword("1234567Ł"))
        assertEquals(PASSWORDS_DO_NOT_MATCH, validator.validatePasswordConfirmation("1234567Ł", "1234567Ł "))
    }

    @Test
    fun loginOnlyChecksEmailFormatAndPasswordPresence() {
        assertEquals(emptyList<ValidationError>(), validator.validateLogin("ala@example.pl", "x"))
        assertEquals(listOf(INVALID_EMAIL), validator.validateLogin("ala", "x"))
    }
}
