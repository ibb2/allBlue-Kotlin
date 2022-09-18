package com.subsolis.allblue.Login

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

val viewModelAuth = Firebase.auth

data class UserState(
    val LoggedIn: Boolean? = false,
    val firebaseAuth: FirebaseAuth? = viewModelAuth,
    val token: Int? = 0,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repositoryImpl: LoginRepositoryImpl,
    private val dataStore: DataStore<Preferences>
    ): ViewModel() {

    private val _viewstate = MutableStateFlow(UserState())
    var viewstate: StateFlow<UserState> = _viewstate

    fun LogInStatus(firebaseAuth: FirebaseAuth?) {
        viewModelScope.launch {
            val loggedInStatus: Boolean = repositoryImpl.loggedInStatus(firebaseAuth)

            _viewstate.value = _viewstate.value.copy(
                LoggedIn = loggedInStatus
            )
        }
    }

}