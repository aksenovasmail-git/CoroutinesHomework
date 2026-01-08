package ru.otus.coroutineshomework.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.otus.coroutineshomework.ui.login.data.Credentials

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow<LoginViewState>(LoginViewState.Login())
    val state: StateFlow<LoginViewState> = _state.asStateFlow()
    private val loginApi = LoginApi()

    /**
     * Login to the network
     * @param name user name
     * @param password user password
     */
    fun login(name: String, password: String) {
        viewModelScope.launch {
            loginFlow(name, password).collect { viewState ->
                _state.value = viewState
            }
        }
    }

    private fun loginFlow(name: String, password: String): Flow<LoginViewState> = flow {
        emit(LoginViewState.LoggingIn)
        val user = loginApi.login(Credentials(name, password))
        emit(LoginViewState.Content(user))
    }.catch { e ->
        emit(LoginViewState.Login(error = e as? Exception))
    }.flowOn(Dispatchers.IO)

    /**
     * Logout from the network
     */
    fun logout() {
        viewModelScope.launch {
            _state.value = LoginViewState.LoggingOut
            try {
                withContext(Dispatchers.IO) {
                    loginApi.logout()
                }
                _state.value = LoginViewState.Login()
            } catch (e: Exception) {
                _state.value = LoginViewState.Login(error = e)
            }
        }
    }
}