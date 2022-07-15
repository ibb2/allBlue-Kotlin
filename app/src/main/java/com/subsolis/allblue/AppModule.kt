package com.subsolis.allblue

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

private const val DEVICE = "selected_device"

@Module
@InstallIn(SingletonComponent::class)
class AppModule() {

     @Singleton
     @Provides
     fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
          return PreferenceDataStoreFactory.create(
               migrations = listOf(SharedPreferencesMigration(context, DEVICE)),
               scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
               produceFile = {context.preferencesDataStoreFile(DEVICE)}
          )
     }
}