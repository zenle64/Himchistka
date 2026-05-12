package com.example.drycleaning.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drycleaning.data.entity.User
import com.example.drycleaning.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Состояние экрана авторизации */
sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

/** ViewModel экрана авторизации */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    val isLoggedIn = authRepository.isLoggedIn

    fun login(username: String, password: String) {
        if (username.isBlank()) {
            _loginState.value = LoginState.Error("Введите логин")
            return
        }
        if (password.isBlank()) {
            _loginState.value = LoginState.Error("Введите пароль")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = authRepository.login(username, password)
            _loginState.value = result.fold(
                onSuccess = { LoginState.Success(it) },
                onFailure = { LoginState.Error(it.message ?: "Ошибка авторизации") }
            )
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
