package com.example.elearning.ui.viewmodel

import com.example.elearning.data.download.DownloadState
import com.example.elearning.data.download.MaterialDownloadRepository
import com.example.elearning.domain.download.DownloadPolicy
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elearning.data.repository.MaterialRepository
import com.example.elearning.domain.model.Material
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class MaterialUiState(
    val material: Material? = null,
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val loadError: Boolean = false
)

class MaterialViewModel(
    private val materialId: String,
    private val userId: String,
    private val repository: MaterialRepository,
    private val downloads: MaterialDownloadRepository
) : ViewModel() {
    private val material = repository.observeMaterial(materialId, userId)
    private val errors = Channel<Unit>(Channel.BUFFERED)
    val saveErrors = errors.receiveAsFlow()
    private val completionLock = Mutex()
    private val reloads = MutableStateFlow(0)
    private val sharing = SharingStarted.WhileSubscribed(5_000)
    val uiState = channelFlow {
        reloads.collectLatest {
            try {
                material.collect {
                    send(MaterialUiState(material = it, isLoading = false, notFound = it == null))
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                send(MaterialUiState(isLoading = false, loadError = true))
            }
        }
    }.stateIn(viewModelScope, sharing, MaterialUiState())

    fun reload() { reloads.value++ }

    private val eligible = uiState.map { state ->
        state.material?.let { DownloadPolicy.isDownloadable(it.type, it.contentUrl) } == true
    }.distinctUntilChanged()

    private val downloadErrorsChannel = Channel<Unit>(Channel.BUFFERED)
    val downloadErrors = downloadErrorsChannel.receiveAsFlow()
    val downloadState: StateFlow<DownloadState> = channelFlow {
        eligible.collectLatest { allowed ->
            if (allowed) {
                try {
                    downloads.observeState(materialId).collect { send(it) }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    downloadErrorsChannel.send(Unit)
                    send(DownloadState.NotDownloaded)
                }
            } else {
                send(DownloadState.NotDownloaded)
            }
        }
    }.stateIn(viewModelScope, sharing, DownloadState.NotDownloaded)

    fun startDownload() = downloadAction(downloads::enqueue)
    fun retryDownload() = startDownload()
    fun cancelDownload() = downloadAction(downloads::cancel)
    fun deleteDownload() = downloadAction(downloads::delete)

    private fun downloadAction(action: suspend (String) -> Unit) {
        viewModelScope.launch {
            try {
                val current = material.first() ?: return@launch
                if (DownloadPolicy.isDownloadable(current.type, current.contentUrl)) action(materialId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                downloadErrorsChannel.send(Unit)
            }
        }
    }

    fun toggleCompleted() {
        viewModelScope.launch {
            try {
                completionLock.withLock {
                    val current = material.first() ?: return@withLock
                    repository.setCompleted(userId, current.id, !current.isCompleted)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                errors.send(Unit)
            }
        }
    }

    class Factory(
        private val materialId: String,
        private val userId: String,
        private val repository: MaterialRepository,
        private val downloads: MaterialDownloadRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(MaterialViewModel::class.java))
            return modelClass.cast(MaterialViewModel(materialId, userId, repository, downloads))!!
        }
    }
}
