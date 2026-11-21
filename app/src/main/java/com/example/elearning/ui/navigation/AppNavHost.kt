package com.example.elearning.ui.navigation

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.elearning.data.remote.FirebaseAuthRepository
import com.example.elearning.ui.viewmodel.AuthViewModel
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
    val factory = remember { AuthViewModel.Factory(FirebaseAuthRepository()) }
    val auth: AuthViewModel = viewModel(factory = factory)
    val state by auth.uiState.collectAsStateWithLifecycle()
    if (state.isInitializing) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val startDestination = rememberSaveable {
        if (state.user == null) Routes.Login else Routes.CourseList
    }
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val isAuthRoute = route == Routes.Login || route == Routes.Register

    LaunchedEffect(entry) {
        if (isAuthRoute || route == Routes.Profile) auth.clearErrors()
    }
    LaunchedEffect(route, state.user, state.isLoading, state.authError) {
        if (isAuthRoute && state.user != null && !state.isLoading && state.authError == null) {
            nav.navigate(Routes.CourseList) { popUpTo(0) { inclusive = true } }
        } else if (route != null && !isAuthRoute && state.user == null) {
            nav.navigate(Routes.Login) { popUpTo(0) { inclusive = true } }
        }
    }

    NavHost(nav, startDestination = startDestination, modifier = Modifier.safeDrawingPadding()) {
        composable(Routes.Login) {
            LoginScreen(
                uiState = state,
                onLogin = auth::login,
                onRegister = { nav.navigate(Routes.Register) { launchSingleTop = true } }
            )
        }
        composable(Routes.Register) {
            RegisterScreen(
                uiState = state,
                onRegister = auth::register,
                onLogin = { nav.popBackStack() }
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
            val user = state.user ?: return@composable
            var name by rememberSaveable(user.uid, user.name) { mutableStateOf(user.name) }
            ProfileScreen(
                user = user,
                name = name,
                onNameChange = { name = it },
                onSave = { auth.updateName(name) },
                isLoading = state.isLoading,
                nameError = state.validationErrors.firstOrNull(),
                authError = state.authError,
                onSignOut = auth::signOut,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
