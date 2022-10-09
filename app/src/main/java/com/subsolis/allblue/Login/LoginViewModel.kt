package com.subsolis.allblue.Login

import android.app.Activity
import android.content.IntentSender
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

val viewModelAuth = Firebase.auth

data class UserState(
    val LoggedIn: Boolean = false,
    val firebaseAuth: FirebaseAuth = viewModelAuth,
    val cancelled: Boolean = false,
)

// TAGS
private val TAG_SIGNIN = "GoogleSignIn"
private val TAG_SIGNUP = "GoogleSignUp"

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repositoryImpl: LoginRepositoryImpl,
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    private val _viewstate = MutableStateFlow(UserState())
    var viewstate: StateFlow<UserState> = _viewstate


    fun loginStatus(firebaseAuth: FirebaseAuth?) {
        viewModelScope.launch {
            val loggedInStatus: Boolean = repositoryImpl.loggedInStatus(firebaseAuth)

            _viewstate.value = _viewstate.value.copy(
                LoggedIn = loggedInStatus
            )
        }
    }

    fun signIn(
        activity: Activity,
        oneTapClient: SignInClient,
        signInRequest: BeginSignInRequest,
        signUpRequest: BeginSignInRequest,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
    ) {

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(activity) { result ->
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    launcher.launch(intentSenderRequest)
                    Log.d(TAG_SIGNIN, "Started One Tap Sign In")
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG_SIGNIN, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(activity) { e ->

                e.localizedMessage?.let { Log.d(TAG_SIGNUP, "$it: non functional") }

                oneTapClient.beginSignIn(signUpRequest)
                    .addOnSuccessListener(activity) { result ->
                        try {
                            val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                            launcher.launch(intentSenderRequest)
                        } catch (e: IntentSender.SendIntentException) {
                            Log.e(TAG_SIGNUP, "Couldn't start One Tap UI: ${e.localizedMessage}")
                        }
                    }
                    .addOnFailureListener(activity) { e ->
                        // No Google Accounts found. Just continue presenting the signed-out UI.
                        e.localizedMessage?.let { Log.d(TAG_SIGNUP, "$it: even less non functional") }
                    }
            }
    }

    fun signOut(auth: FirebaseAuth, oneTapClient: SignInClient) {
        viewModelScope.launch{
            auth.signOut()
            oneTapClient.signOut().await()
        }
    }
}