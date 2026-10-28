package com.pwfb.data

import android.content.Context
import com.pwfb.domain.repository.ErrorReporter
import com.pwfb.domain.repository.FileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FileModule {
    @Provides
    @Singleton
    fun provideFileRepository(
        @ApplicationContext applicationContext: Context,
        errorReporter: ErrorReporter
    ): FileRepository = FileRepositoryImpl(File(applicationContext.filesDir, "logs"), errorReporter)
}
