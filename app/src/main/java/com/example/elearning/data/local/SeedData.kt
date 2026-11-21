package com.example.elearning.data.local

import com.example.elearning.data.local.entity.CourseEntity
import com.example.elearning.data.local.entity.MaterialEntity
import com.example.elearning.data.local.entity.QuestionEntity
import com.example.elearning.data.local.entity.QuizEntity

object SeedData {
    val courses = listOf(
        CourseEntity("kotlin", "Podstawy Kotlina", "Zmienne, typy i funkcje.", "Programowanie"),
        CourseEntity("android", "Wprowadzenie do Androida", "Budowa aplikacji mobilnej.", "Systemy mobilne")
    )

    // Dla materiałów TEXT pole contentUrl tymczasowo zawiera treść lekcji.
    val materials = listOf(
        MaterialEntity("zmienne", "kotlin", "Zmienne", "TEXT",
            "Użyj val, aby przypisać wartość tylko raz. Użyj var, aby móc ją później zmienić."),
        MaterialEntity("manifest", "android", "Manifest aplikacji", "TEXT",
            "AndroidManifest.xml deklaruje komponenty aplikacji i potrzebne uprawnienia."),
        MaterialEntity("pdf", "android", "Przykładowy dokument", "PDF",
            "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf")
    )

    val quizzes = listOf(QuizEntity("kotlin-test", "kotlin", "Sprawdź podstawy Kotlina", 60))

    val questions = listOf(
        QuestionEntity("k1", "kotlin-test", "Jak zadeklarować zmienną do wielokrotnego przypisania?",
            listOf("var", "val", "fun", "class"), 0),
        QuestionEntity("k2", "kotlin-test", "Który typ przechowuje tekst?",
            listOf("Char", "String", "Int", "Double"), 1),
        QuestionEntity("k3", "kotlin-test", "Które słowo rozpoczyna deklarację funkcji?",
            listOf("val", "when", "fun", "return"), 2),
        QuestionEntity("k4", "kotlin-test", "Który typ dopuszcza wartość null?",
            listOf("Int", "Boolean", "String", "String?"), 3),
        QuestionEntity("k5", "kotlin-test", "Jak odczytać pierwszy element listy o nazwie liczby?",
            listOf("liczby[1]", "liczby[0]", "liczby.size", "liczby[-1]"), 1)
    )
}
