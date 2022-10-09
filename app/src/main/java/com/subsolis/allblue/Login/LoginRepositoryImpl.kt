package com.subsolis.allblue.Login

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(private val dataStore: DataStore<Preferences>): LoginRepository {

    val LOGGED_IN = booleanPreferencesKey("logged_in")

    override suspend fun loggedInStatus(auth: FirebaseAuth?): Boolean {

        return if (auth?.currentUser != null) {
            dataStore.edit { settings ->
                settings[LOGGED_IN] = true
            }

            true
        } else {
            dataStore.edit { settings ->
                settings[LOGGED_IN] = false
            }

            false
        }
    }
}