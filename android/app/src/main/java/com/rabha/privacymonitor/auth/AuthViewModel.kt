package com.rabha.privacymonitor.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabha.privacymonitor.core.RabhaResult
import com.rabha.privacymonitor.core.unavailableText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val busy: Boolean = false,
    val error: String? = null,
    val notice: String? = null,
    val signedIn: Boolean = false,
    val signedInEmail: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        repository.refreshSession()
        val session = repository.session.value
        _state.value = _state.value.copy(signedIn = session.signedIn, signedInEmail = session.email)
    }

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, error = null)
    }

    fun signIn() {
        val current = _state.value
        if (current.email.isBlank() || current.password.length < 6) {
            _state.value = current.copy(error = "Enter an email and a password of at least 6 characters.")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, error = null, notice = null)
            when (val result = repository.signIn(current.email.trim(), current.password)) {
                is RabhaResult.Available -> _state.value = _state.value.copy(
                    busy = false,
                    signedIn = true,
                    signedInEmail = current.email.trim()
                )

                else -> _state.value = _state.value.copy(busy = false, error = result.unavailableText())
            }
        }
    }

    fun signUp() {
        val current = _state.value
        if (current.email.isBlank() || current.password.length < 6) {
            _state.value = current.copy(error = "Enter an email and a password of at least 6 characters.")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, error = null, notice = null)
            when (val result = repository.signUp(current.email.trim(), current.password)) {
                is RabhaResult.Available -> _state.value = _state.value.copy(
                    busy = false,
                    notice = "Account created. Check your inbox if email confirmation is enabled."
                )

                else -> _state.value = _state.value.copy(busy = false, error = result.unavailableText())
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _state.value = AuthUiState()
        }
    }
}
