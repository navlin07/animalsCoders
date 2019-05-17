package com.architectcoders.animalcoders.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arrow.core.Either
import com.architectcoders.animalcoders.tools.Scope
import com.architectcoders.domain.interactors.LoginInteractor
import com.architectcoders.domain.model.Failure
import kotlinx.coroutines.launch

class LoginViewModel(private val interactor: LoginInteractor) : ViewModel(), Scope by Scope.Impl {

    private val _model = MutableLiveData<LoginViewState>()
    val model: LiveData<LoginViewState>
        get() {
            if (_model.value == null) initView()
            return _model
        }

    init {
        initScope()
    }

    private fun initView() {
        _model.value = LoginViewState.EmptyFields
    }

    fun cancelForm() {
        _model.value = LoginViewState.EmptyFields
    }

    fun validateCredentials(username: String, password: String) = launch {
        _model.value = LoginViewState.Loading
        when (val result = interactor.login(username, password)) {
            is Either.Left -> {
                Log.d("REASON: ", result.a.reason.toString())
                when (result.a.reason) {
                    Failure.Reason.BLANK_INVALID_USER -> _model.value = LoginViewState.UsernameError
                    Failure.Reason.BLANK_INVALID_PASS -> _model.value = LoginViewState.PasswordError
                    Failure.Reason.USER_NOT_EXIST -> _model.value = LoginViewState.Error(result.a.message)
                    Failure.Reason.CONNECTION_ISSUES -> _model.value = LoginViewState.Error(result.a.message)
                    else -> _model.value = LoginViewState.Error(result.a.message)
                }
            }
            is Either.Right -> {
                Log.d("EMAIL: ", result.b.email)
                _model.value = LoginViewState.EmptyFields
                _model.value = LoginViewState.NavigateToHome
            }
        }
    }

    override fun onCleared() {
        destroyScope()
        super.onCleared()
    }

}