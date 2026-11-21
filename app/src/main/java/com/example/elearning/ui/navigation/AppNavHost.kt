package com.example.elearning.ui.navigation

import androidx.activity.compose.BackHandler
import com.example.elearning.domain.quiz.buildReview
import com.example.elearning.ui.screens.QuitQuizDialog
import com.example.elearning.ui.viewmodel.QuizViewModel
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
import androidx.compose.ui.platform.LocalContext
import com.example.elearning.ElearningApp
import com.example.elearning.ui.viewmodel.CourseListViewModel
import com.example.elearning.ui.viewmodel.MaterialViewModel
import com.example.elearning.ui.viewmodel.CourseDetailViewModel
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
    val container = (LocalContext.current.applicationContext as ElearningApp).container
    val factory = remember { AuthViewModel.Factory(container.authRepository) }
    val auth: AuthViewModel = viewModel(factory = factory)
    val state by auth.uiState.collectAsStateWithLifecycle()
    if (state.isInitializing) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val nav = rememberNavController()
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
            val courses: CourseListViewModel = viewModel(factory = remember {
                CourseListViewModel.Factory(container.courseRepository)
            })
            val listState by courses.uiState.collectAsStateWithLifecycle()
            CourseListScreen(
                uiState = listState,
                onQueryChange = courses::setQuery,
                onCategoryChange = courses::selectCategory,
                onCourseClick = { nav.navigate(Routes.course(it)) },
                onProgress = { nav.navigate(Routes.Progress) },
                onProfile = { nav.navigate(Routes.Profile) }
            )
        }
        composable(Routes.CourseDetail, arguments = listOf(navArgument("courseId") { type = NavType.StringType })) { entry ->
            val userId = state.user?.uid ?: return@composable
            val courseId = requireNotNull(entry.arguments?.getString("courseId"))
            val detail: CourseDetailViewModel = viewModel(key = userId, factory = remember(courseId, userId) {
                CourseDetailViewModel.Factory(courseId, userId, container.courseRepository,
                    container.materialRepository, container.quizRepository)
            })
            val detailState by detail.uiState.collectAsStateWithLifecycle()
            CourseDetailScreen(
                uiState = detailState,
                onBack = { nav.popBackStack(Routes.CourseList, inclusive = false) },
                onToggleCompleted = detail::toggleCompleted,
                onMaterialClick = { nav.navigate(Routes.material(it)) },
                onQuizClick = { nav.navigate(Routes.quiz(it)) }
            )
        }
        composable(Routes.Material, arguments = listOf(navArgument("materialId") { type = NavType.StringType })) { entry ->
            val userId = state.user?.uid ?: return@composable
            val materialId = requireNotNull(entry.arguments?.getString("materialId"))
            val model: MaterialViewModel = viewModel(key = userId, factory = remember(materialId, userId) {
                MaterialViewModel.Factory(materialId, userId, container.materialRepository)
            })
            val materialState by model.uiState.collectAsStateWithLifecycle()
            MaterialScreen(
                uiState = materialState,
                onToggleCompleted = model::toggleCompleted,
                onBack = { if (nav.currentBackStackEntry == entry) nav.navigateUp() }
            )
        }
        composable(Routes.Quiz, arguments = listOf(navArgument("quizId") { type = NavType.StringType })) { entry ->
            val quizId = requireNotNull(entry.arguments?.getString("quizId"))
            val model: QuizViewModel = viewModel(viewModelStoreOwner = entry, factory = remember(quizId) {
                QuizViewModel.Factory(quizId, container.quizRepository)
            })
            val quiz by model.uiState.collectAsStateWithLifecycle()
            var confirmExit by rememberSaveable { mutableStateOf(false) }
            val active = quiz.currentQuestion != null && quiz.outcome == null
            val leave: () -> Unit = { if (nav.currentBackStackEntry == entry) nav.navigateUp() }
            BackHandler(enabled = active) { confirmExit = true }
            QuizScreen(
                state = quiz,
                onBack = { if (active) confirmExit = true else leave() },
                onAnswer = model::selectAnswer,
                onPrevious = model::previous,
                onNext = model::next,
                onFinish = {
                    model.finish()
                    if (model.uiState.value.outcome != null && nav.currentBackStackEntry == entry) {
                        nav.navigate(Routes.quizResult(quizId))
                    }
                }
            )
            if (confirmExit && active) {
                QuitQuizDialog(onQuit = leave, onStay = { confirmExit = false })
            }
        }
        composable(Routes.QuizResult, arguments = listOf(navArgument("quizId") { type = NavType.StringType })) { entry ->
            val quizId = requireNotNull(entry.arguments?.getString("quizId"))
            val quizEntry = remember(entry) { nav.getBackStackEntry(Routes.quiz(quizId)) }
            val model: QuizViewModel = viewModel(viewModelStoreOwner = quizEntry, factory = remember(quizId) {
                QuizViewModel.Factory(quizId, container.quizRepository)
            })
            val quiz by model.uiState.collectAsStateWithLifecycle()
            val outcome = quiz.outcome
            val returnToCourse: () -> Unit = {
                if (nav.currentBackStackEntry == entry) {
                    nav.popBackStack(Routes.CourseDetail, inclusive = false)
                }
            }
            BackHandler(onBack = returnToCourse)
            if (outcome == null) {
                LaunchedEffect(entry) { returnToCourse() }
            } else {
                QuizResultScreen(
                    outcome = outcome,
                    review = remember(quiz.questions, outcome) { buildReview(quiz.questions, outcome) },
                    onRetry = {
                        if (nav.currentBackStackEntry == entry && nav.popBackStack(Routes.quiz(quizId), false)) {
                            model.restart()
                        }
                    },
                    onReturnToCourse = returnToCourse
                )
            }
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
