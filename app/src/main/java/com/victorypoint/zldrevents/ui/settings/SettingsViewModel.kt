package com.victorypoint.zldrevents.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.victorypoint.zldrevents.data.auth.AuthRepository
import com.victorypoint.zldrevents.data.events.EventsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val eventsRepository: EventsRepository,
) : ViewModel() {

    val username: String? get() = authRepository.tokenStore.username

    private val _logoutConfirmVisible = MutableStateFlow(false)
    val logoutConfirmVisible = _logoutConfirmVisible.asStateFlow()

    fun requestLogout() { _logoutConfirmVisible.value = true }
    fun dismissLogoutConfirm() { _logoutConfirmVisible.value = false }

    private val _clearConfirmVisible = MutableStateFlow(false)
    val clearConfirmVisible = _clearConfirmVisible.asStateFlow()

    fun requestClearCache() { _clearConfirmVisible.value = true }
    fun dismissClearConfirm() { _clearConfirmVisible.value = false }

    fun clearCache() {
        viewModelScope.launch {
            eventsRepository.invalidateAndRefresh()
            _clearConfirmVisible.value = false
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
