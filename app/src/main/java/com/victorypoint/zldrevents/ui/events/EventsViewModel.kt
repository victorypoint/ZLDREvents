package com.victorypoint.zldrevents.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorypoint.zldrevents.data.events.EventsRepository
import com.victorypoint.zldrevents.data.model.ZwiftEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EventsUiState {
    data object Loading : EventsUiState
    data class Success(val events: List<ZwiftEvent>) : EventsUiState
    data class Error(val message: String) : EventsUiState
}

class EventsViewModel(private val eventsRepository: EventsRepository) : ViewModel() {

    private val _cyclingState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val cyclingState = _cyclingState.asStateFlow()

    private val _runningState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val runningState = _runningState.asStateFlow()

    var savedSelectedTab = 0; private set
    var savedRunningScrollIndex = 0; private set
    var savedRunningScrollOffset = 0; private set
    var savedCyclingScrollIndex = 0; private set
    var savedCyclingScrollOffset = 0; private set

    fun saveSelectedTab(index: Int) { savedSelectedTab = index }
    fun saveRunningScroll(index: Int, offset: Int) { savedRunningScrollIndex = index; savedRunningScrollOffset = offset }
    fun saveCyclingScroll(index: Int, offset: Int) { savedCyclingScrollIndex = index; savedCyclingScrollOffset = offset }

    init {
        loadAll()
    }

    fun refresh() {
        viewModelScope.launch {
            eventsRepository.invalidateAndRefresh()
            loadAll()
        }
    }

    private fun loadAll() {
        loadCycling()
        loadRunning()
    }

    private fun loadCycling() {
        viewModelScope.launch {
            _cyclingState.value = EventsUiState.Loading
            val result = eventsRepository.getCyclingEvents()
            _cyclingState.value = result.fold(
                onSuccess = { EventsUiState.Success(it) },
                onFailure = { EventsUiState.Error(it.message ?: "Failed to load cycling events") },
            )
        }
    }

    private fun loadRunning() {
        viewModelScope.launch {
            _runningState.value = EventsUiState.Loading
            val result = eventsRepository.getRunningEvents()
            _runningState.value = result.fold(
                onSuccess = { EventsUiState.Success(it) },
                onFailure = { EventsUiState.Error(it.message ?: "Failed to load running events") },
            )
        }
    }
}
