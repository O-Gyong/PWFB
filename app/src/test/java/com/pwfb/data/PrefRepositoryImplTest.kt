package com.pwfb.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pwfb.common.DataStoreResult
import com.pwfb.domain.entity.PrefKey
import com.pwfb.domain.entity.PwfbResultEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PrefRepositoryImplTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: PrefRepositoryImpl

    @Before
    fun setUp() {
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = { File(tempFolder.root, "test.preferences_pb") }
        )
        repository = PrefRepositoryImpl(dataStore)
    }

    @Test
    fun observe_returnsDefaultWhenNothingSaved() = runBlocking {
        assertEquals("NameNull", repository.observe(PrefKey.NAME).first())
        assertEquals("WeightNull", repository.observe(PrefKey.WEIGHT).first())
        assertEquals("", repository.observe(PrefKey.D_DAY).first())
        assertEquals("", repository.observe(PrefKey.CARBOHYDRATE).first())
    }

    @Test
    fun set_thenObserve_returnsSavedValue() = runBlocking {
        val result = repository.set(PrefKey.WEIGHT, "72.5")

        assertEquals(PwfbResultEntity.Success(DataStoreResult.RESULT_SUCCESS), result)
        assertEquals("72.5", repository.observe(PrefKey.WEIGHT).first())
    }

    @Test
    fun set_doesNotAffectOtherKeys() = runBlocking {
        repository.set(PrefKey.PROTEIN, "150")

        assertEquals("150", repository.observe(PrefKey.PROTEIN).first())
        assertEquals("", repository.observe(PrefKey.FAT).first())
    }

    @Test
    fun firstInit_isTrueUntilSet() = runBlocking {
        assertTrue(repository.observeFirstInit().first())

        repository.setFirstInit()

        assertEquals(false, repository.observeFirstInit().first())
    }

    /**
     * 저장 키 이름이 바뀌면 기존 사용자의 저장 데이터를 읽지 못하므로, 이전 버전의 키 이름을 고정해서 검증한다.
     */
    @Test
    fun keyNames_matchLegacyStoredKeys() = runBlocking {
        val legacy = mapOf(
            PrefKey.NAME to "name",
            PrefKey.WEIGHT to "weight",
            PrefKey.D_DAY to "dDay",
            PrefKey.TRAINING_PROGRAM to "trainingProgram",
            PrefKey.CARBOHYDRATE to "carbohydrate",
            PrefKey.PROTEIN to "protein",
            PrefKey.FAT to "fat",
            PrefKey.WATER to "water",
            PrefKey.SODIUM to "sodium",
            PrefKey.POTASSIUM to "potassium",
            PrefKey.CREATINE to "creatine",
            PrefKey.DIETARY_FIBER to "dietaryFiber"
        )
        assertEquals(PrefKey.values().toSet(), legacy.keys)

        legacy.forEach { (key, legacyName) ->
            // Windows에서는 DataStore 1.0.0이 기존 파일을 덮어쓰지 못하므로 키마다 새 파일을 사용한다
            val store = PreferenceDataStoreFactory.create(
                produceFile = { File(tempFolder.root, "legacy_$legacyName.preferences_pb") }
            )
            val result = PrefRepositoryImpl(store).set(key, "v-$legacyName")
            assertEquals("$key: $result", PwfbResultEntity.Success(DataStoreResult.RESULT_SUCCESS), result)

            assertEquals(legacyName, key.keyName)
            assertEquals("v-$legacyName", store.data.first()[stringPreferencesKey(legacyName)])
        }
    }
}
