package com.example.elearning.ui.navigation

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.elearning.ui.screens.LoginScreen
import com.example.elearning.ui.screens.RegisterScreen
import com.example.elearning.ui.screens.CourseListScreen
import com.example.elearning.ui.screens.CourseDetailScreen
import com.example.elearning.ui.screens.MaterialScreen
import com.example.elearning.ui.screens.QuizScreen
import com.example.elearning.ui.screens.QuizResultScreen
import com.example.elearning.ui.screens.ProgressScreen
import com.example.elearning.ui.screens.ProfileScreen

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    NavHost(nav, startDestination = Routes.Login, modifier = Modifier.safeDrawingPadding()) {
        composable(Routes.Login) {
            LoginScreen(
                onRegister = { nav.navigate(Routes.Register) },
                onCourseList = { nav.navigate(Routes.CourseList) }
            )
        }
        composable(Routes.Register) {
            RegisterScreen(
                onCourseList = { nav.navigate(Routes.CourseList) }
            )
        }
        composable(Routes.CourseList) {
            CourseListScreen(
                onCourseDetail = { nav.navigate(Routes.course(it)) },
                onProgress = { nav.navigate(Routes.Progress) },
                onProfile = { nav.navigate(Routes.Profile) }
            )
        }
        composable(Routes.CourseDetail, arguments = listOf(navArgument("courseId") { type = NavType.StringType })) { entry ->
            CourseDetailScreen(
                courseId = requireNotNull(entry.arguments?.getString("courseId")),
                onMaterial = { nav.navigate(Routes.material(it)) }
            )
        }
        composable(Routes.Material, arguments = listOf(navArgument("materialId") { type = NavType.StringType })) { entry ->
            MaterialScreen(
                materialId = requireNotNull(entry.arguments?.getString("materialId")),
                onQuiz = { nav.navigate(Routes.quiz(it)) }
            )
        }
        composable(Routes.Quiz, arguments = listOf(navArgument("quizId") { type = NavType.StringType })) { entry ->
            QuizScreen(
                quizId = requireNotNull(entry.arguments?.getString("quizId")),
                onQuizResult = { nav.navigate(Routes.quizResult(it)) }
            )
        }
        composable(Routes.QuizResult, arguments = listOf(navArgument("quizId") { type = NavType.StringType })) { entry ->
            QuizResultScreen(
                quizId = requireNotNull(entry.arguments?.getString("quizId")),
                onProgress = { nav.navigate(Routes.Progress) }
            )
        }
        composable(Routes.Progress) {
            ProgressScreen(
                onProfile = { nav.navigate(Routes.Profile) }
            )
        }
        composable(Routes.Profile) {
            ProfileScreen(
                onBack = { nav.popBackStack() }
            )
        }
    }
}
