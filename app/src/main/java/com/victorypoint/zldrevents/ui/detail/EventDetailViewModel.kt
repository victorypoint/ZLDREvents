package com.victorypoint.zldrevents.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorypoint.zldrevents.data.events.EventsRepository
import com.victorypoint.zldrevents.data.model.ZwiftEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val event: ZwiftEvent) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class EventDetailViewModel(
    private val eventsRepository: EventsRepository,
    private val eventId: Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val result = eventsRepository.getEventDetail(eventId)
            _uiState.value = result.fold(
                onSuccess = { DetailUiState.Success(it) },
                onFailure = { DetailUiState.Error(it.message ?: "Failed to load event") },
            )
        }
    }
}
