package com.pwfb.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pwfb.common.DataStoreResult
import com.pwfb.domain.entity.PrefKey
import com.pwfb.domain.entity.PwfbResultEntity
import com.pwfb.domain.repository.PrefRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class PrefRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PrefRepository {

    private val firstInitKey = booleanPreferencesKey("firstInit") // 앱 최초 진입 여부

    private val preferences: Flow<Preferences> = dataStore.data.catch { e ->
        if (e is IOException) {
            emit(emptyPreferences())
        } else {
            throw e
        }
    }

    override fun observe(key: PrefKey): Flow<String> {
        val prefKey = stringPreferencesKey(key.keyName)
        return preferences.map { it[prefKey] ?: key.default }
    }

    override suspend fun set(key: PrefKey, value: String): PwfbResultEntity {
        val prefKey = stringPreferencesKey(key.keyName)
        return update { it[prefKey] = value }
    }

    override fun observeFirstInit(): Flow<Boolean> {
        // firstInitKey 값이 없을 경우 앱 최초 진입으로 판단
        return preferences.map { it[firstInitKey] ?: true }
    }

    override suspend fun setFirstInit(): PwfbResultEntity {
        return update { it[firstInitKey] = false }
    }

    private suspend fun update(transform: (MutablePreferences) -> Unit): PwfbResultEntity {
        return try {
            dataStore.edit { transform(it) }
            PwfbResultEntity.Success(DataStoreResult.RESULT_SUCCESS)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            PwfbResultEntity.Failure(e)
        }
    }
}
