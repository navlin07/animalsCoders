package com.architectcoders.animalcoders.main

import android.util.Log
import arrow.core.Either
import com.architectcoders.domain.interactors.AuthInteractor
import com.architectcoders.domain.model.Animal
import com.architectcoders.domain.model.Failure
import com.example.baseandroid.coroutines.CoroutineDispatchers
import com.example.baseandroid.viewmodel.BaseViewModel
import kotlinx.coroutines.Job

class MainActivityViewModel(
    private val authInteractor: AuthInteractor,
    dispatchers: CoroutineDispatchers
) :
    BaseViewModel<MainActivityViewState, MainActivityViewTransition>(dispatchers = dispatchers) {

    private var serviceAuthCall: Job? = null
    private var serviceLogOutCall: Job? = null

    override fun init() {
        viewState.value = MainActivityViewState.SearchState
        viewTransition.value = MainActivityViewTransition.NavigateToSearch
    }

    override fun initServices() {
        super.initServices()
        serviceAuthCall = executeBackground {
            when (val result = authInteractor.getCurrentUser()) {
                is Either.Left -> {
                    executeUI {
                        Log.d("REASON: ", result.a.reason.toString())
                        if (result.a.reason == Failure.Reason.USER_NOT_EXIST) viewTransition.value =
                            MainActivityViewTransition.NavigateToLogin
                    }
                }
                is Either.Right -> {
                    Log.d("EMAIL: ", result.b.email)
                }
            }
        }
    }

    fun logOut() {
        serviceLogOutCall = executeBackground {
            authInteractor.logOutCurrentUser()
        }
        viewTransition.value =
            MainActivityViewTransition.NavigateToLogin
    }

    override fun onCleared() {
        super.onCleared()
        serviceAuthCall?.cancel()
        serviceLogOutCall?.cancel()
    }

    fun onSearchTabClicked() {
        if (viewState.value != MainActivityViewState.SearchState) {
            viewState.value =
                MainActivityViewState.SearchState
            viewTransition.value = MainActivityViewTransition.NavigateToSearch
        }

    }

    fun onProfileTabClicked() {
        if (viewState.value != MainActivityViewState.ProfileState) {
            viewState.value =
                MainActivityViewState.ProfileState
            viewTransition.value = MainActivityViewTransition.NavigateToProfile
        }
    }

    fun navigateToDetail(animal: Animal) {
        viewTransition.value = MainActivityViewTransition.NavigateToAnimalDetail(animal)
    }

    fun navigateToFavourites() {
        viewTransition.value = MainActivityViewTransition.NavigateToFavourites
    }

}