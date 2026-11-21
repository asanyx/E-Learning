package com.example.elearning.ui.navigation

import android.net.Uri

object Routes {
    const val Login = "login"
    const val Register = "register"
    const val CourseList = "courses"
    const val CourseDetail = "course/{courseId}"
    fun course(courseId: String) = "course/${Uri.encode(courseId)}"
    const val Material = "material/{materialId}"
    fun material(materialId: String) = "material/${Uri.encode(materialId)}"
    const val Quiz = "quiz/{quizId}"
    fun quiz(quizId: String) = "quiz/${Uri.encode(quizId)}"
    const val QuizResult = "quizResult/{quizId}"
    fun quizResult(quizId: String) = "quizResult/${Uri.encode(quizId)}"
    const val Progress = "progress"
    const val Profile = "profile"
}
