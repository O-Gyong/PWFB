package com.pwfb.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.pwfb.domain.repository.PrefRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// DataStore는 같은 파일에 대해 인스턴스가 하나만 존재해야 하므로 반드시 최상위에서 선언
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pref_pwfb")

@Module
@InstallIn(SingletonComponent::class)
object PrefDataSource {
    @Provides
    @Singleton
    fun providePrefRepository(@ApplicationContext context: Context): PrefRepository =
        PrefRepositoryImpl(context.dataStore)
}
