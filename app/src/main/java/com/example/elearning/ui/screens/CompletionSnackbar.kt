package com.example.elearning.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.platformaELearningowa.R
import kotlinx.coroutines.flow.Flow

@Composable
internal fun CompletionSnackbar(errors: Flow<Unit>, content: @Composable () -> Unit) {
    val host = remember { SnackbarHostState() }
    val owner = LocalLifecycleOwner.current
    val message = stringResource(R.string.completion_save_failed)
    LaunchedEffect(errors, owner, message) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            errors.collect { host.showSnackbar(message) }
        }
    }
    Box(Modifier.fillMaxSize()) {
        content()
        SnackbarHost(host, Modifier.align(Alignment.BottomCenter))
    }
}
