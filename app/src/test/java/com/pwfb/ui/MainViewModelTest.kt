package com.pwfb.ui

import com.pwfb.domain.entity.PrefKey
import com.pwfb.domain.entity.PwfbResultEntity
import com.pwfb.domain.repository.FileRepository
import com.pwfb.domain.repository.PrefRepository
import com.pwfb.domain.usecase.PrefUseCase
import com.pwfb.domain.usecase.file.CreateLogFileUseCase
import com.pwfb.domain.usecase.file.GetLogFileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakePrefRepository(private val failOn: Set<PrefKey> = emptySet()) : PrefRepository {
        val values = PrefKey.values().associateWith { MutableStateFlow(it.default) }
        override fun observe(key: PrefKey): Flow<String> = values.getValue(key)
        override suspend fun set(key: PrefKey, value: String): PwfbResultEntity {
            if (key in failOn) return PwfbResultEntity.Failure(IOException("disk full"))
            values.getValue(key).value = value
            return PwfbResultEntity.Success
        }
        override fun observeFirstInit(): Flow<Boolean> = flowOf(true)
        override suspend fun setFirstInit(): PwfbResultEntity = PwfbResultEntity.Success
    }

    private class FakeFileRepository(private val todayFile: File? = null) : FileRepository {
        val logs = mutableListOf<String>()
        override suspend fun createLogFile(msg: String) { logs += msg }
        override suspend fun getFile(): File? = todayFile
    }

    private fun viewModel(prefs: PrefRepository, files: FileRepository = FakeFileRepository()) =
        MainViewModel(PrefUseCase(prefs), CreateLogFileUseCase(files), GetLogFileUseCase(files))

    @Test
    fun save_success_emitsResultAndUpdatesState() = runTest {
        val vm = viewModel(FakePrefRepository())
        val results = mutableListOf<SaveResult>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.saveResult.collect { results += it } }

        vm.save(PrefKey.WEIGHT, "72.5")
        advanceUntilIdle()

        assertEquals(listOf(SaveResult(PrefKey.WEIGHT, true)), results)
        assertEquals("72.5", vm.state(PrefKey.WEIGHT).first { it.isNotEmpty() })
    }

    @Test
    fun save_failure_emitsFalseAndWritesLog() = runTest {
        val files = FakeFileRepository()
        val vm = viewModel(FakePrefRepository(failOn = setOf(PrefKey.D_DAY)), files)
        val results = mutableListOf<SaveResult>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.saveResult.collect { results += it } }

        vm.save(PrefKey.D_DAY, "2026.11.01(일) 10:00⏳")
        advanceUntilIdle()

        assertEquals(listOf(SaveResult(PrefKey.D_DAY, false)), results)
        assertEquals(1, files.logs.size)
        assertTrue(files.logs[0].contains("D_DAY"))
    }

    @Test
    fun requestLogUpload_emitsTodayFileOrNull() = runTest {
        val file = File("today.txt")
        val vm = viewModel(FakePrefRepository(), FakeFileRepository(todayFile = file))
        val uploads = mutableListOf<File?>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.logUpload.collect { uploads += it } }

        vm.requestLogUpload()
        advanceUntilIdle()

        assertEquals(listOf<File?>(file), uploads)
    }
}
