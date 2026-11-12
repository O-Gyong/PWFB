package com.pwfb.data

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.pwfb.domain.repository.ErrorReporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideErrorReporter(): ErrorReporter {
        val crashlytics = FirebaseCrashlytics.getInstance()
        return ErrorReporter { crashlytics.recordException(it) }
    }

    /**
     * 로그 파일 업로드 위치 (Storage의 logs/ 아래)
     */
    @Provides
    @Singleton
    fun provideLogStorageReference(): StorageReference = Firebase.storage.reference.child("logs")
}
