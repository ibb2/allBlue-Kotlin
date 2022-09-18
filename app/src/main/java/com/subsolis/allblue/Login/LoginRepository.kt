package com.subsolis.allblue.Login

import com.google.firebase.auth.FirebaseAuth

interface LoginRepository {

    suspend fun loggedInStatus(auth: FirebaseAuth?): Boolean
}