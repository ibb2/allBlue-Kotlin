package com.example.allblue

import android.content.Context
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
class AppModule() {

//     private lateinit var dataStore: DataStore<Preferences>

     @ActivityScoped
     fun returnPreferences(@ApplicationContext context: Context) {
     }
}