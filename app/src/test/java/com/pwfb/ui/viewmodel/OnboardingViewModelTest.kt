package com.pwfb.ui.viewmodel

import com.pwfb.domain.entity.PrefKey
import com.pwfb.domain.entity.PwfbResultEntity
import com.pwfb.domain.repository.PrefRepository
import com.pwfb.domain.usecase.PrefUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

/**
 * 온보딩 ViewModel 3개(이름 · 체중 · D-Day)의 저장 규칙
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakePrefRepository(
        private val failOn: Set<PrefKey> = emptySet(),
        private val failFirstInit: Boolean = false
    ) : PrefRepository {
        val values = PrefKey.values().associateWith { MutableStateFlow(it.default) }
        var firstInitDone = false
        override fun observe(key: PrefKey): Flow<String> = values.getValue(key)
        override suspend fun set(key: PrefKey, value: String): PwfbResultEntity {
            if (key in failOn) return PwfbResultEntity.Failure(IOException("disk full"))
            values.getValue(key).value = value
            return PwfbResultEntity.Success
        }
        override fun observeFirstInit(): Flow<Boolean> = flowOf(!firstInitDone)
        override suspend fun setFirstInit(): PwfbResultEntity {
            if (failFirstInit) return PwfbResultEntity.Failure(IOException("disk full"))
            firstInitDone = true
            return PwfbResultEntity.Success
        }
    }

    /** 이벤트 흐름을 리스트로 모은다 */
    private fun TestScope.record(flow: SharedFlow<Boolean>): List<Boolean> {
        val events = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.collect { events += it } }
        return events
    }

    // ── 이름 ──

    @Test
    fun name_trimsAndEmitsSuccess() = runTest {
        val repo = FakePrefRepository()
        val vm = NameViewModel(PrefUseCase(repo))
        val events = record(vm.saved)

        vm.setName("  오기용 ")
        advanceUntilIdle()

        assertEquals(listOf(true), events)
        assertEquals("오기용", repo.values.getValue(PrefKey.NAME).value)
    }

    @Test
    fun name_blank_isIgnored() = runTest {
        val vm = NameViewModel(PrefUseCase(FakePrefRepository()))
        val events = record(vm.saved)

        vm.setName("   ")
        advanceUntilIdle()

        assertTrue(events.isEmpty())
    }

    // ── 체중 ──

    @Test
    fun weight_invalid_isIgnored() = runTest {
        val vm = WeightViewModel(PrefUseCase(FakePrefRepository()))
        val events = record(vm.saved)

        vm.setWeight("0")
        vm.setWeight("abc")
        advanceUntilIdle()

        assertTrue(events.isEmpty())
    }

    @Test
    fun weight_failure_emitsFalse() = runTest {
        val vm = WeightViewModel(PrefUseCase(FakePrefRepository(failOn = setOf(PrefKey.WEIGHT))))
        val events = record(vm.saved)

        vm.setWeight("72.5")
        advanceUntilIdle()

        assertEquals(listOf(false), events)
    }

    @Test
    fun isValidWeight_acceptsPositiveNumbersOnly() {
        assertTrue(WeightViewModel.isValidWeight("72.5"))
        assertTrue(WeightViewModel.isValidWeight(" 80 "))
        assertFalse(WeightViewModel.isValidWeight("0"))
        assertFalse(WeightViewModel.isValidWeight("-1"))
        assertFalse(WeightViewModel.isValidWeight(""))
        assertFalse(WeightViewModel.isValidWeight("7,5"))
    }

    // ── D-Day ──

    @Test
    fun dDay_storesTextAndMarksFirstInit() = runTest {
        val repo = FakePrefRepository()
        val vm = DdayViewModel(PrefUseCase(repo))
        val events = record(vm.completed)

        vm.setDDay(LocalDate.of(2026, 11, 14), 18, 5)
        advanceUntilIdle()

        assertEquals("2026.11.14(토) 18:05⏳", repo.values.getValue(PrefKey.D_DAY).value)
        assertTrue(repo.firstInitDone)
        assertEquals(listOf(true), events)
    }

    @Test
    fun dDay_firstInitFailure_emitsFalse() = runTest {
        val repo = FakePrefRepository(failFirstInit = true)
        val vm = DdayViewModel(PrefUseCase(repo))
        val events = record(vm.completed)

        vm.setDDay(LocalDate.of(2026, 11, 14), 9, 0)
        advanceUntilIdle()

        assertEquals(listOf(false), events)
        assertFalse(repo.firstInitDone)
    }

    @Test
    fun dDay_saveFailure_doesNotTouchFirstInit() = runTest {
        val repo = FakePrefRepository(failOn = setOf(PrefKey.D_DAY))
        val vm = DdayViewModel(PrefUseCase(repo))
        val events = record(vm.completed)

        vm.setDDay(LocalDate.of(2026, 11, 14), 9, 0)
        advanceUntilIdle()

        assertEquals(listOf(false), events)
        assertFalse(repo.firstInitDone)
    }
}
