package com.pwfb.data

import com.pwfb.domain.repository.ErrorReporter
import com.pwfb.util.getCurrentToday
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.concurrent.TimeUnit

class FileRepositoryImplTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private val reported = mutableListOf<Throwable>()
    private val errorReporter = ErrorReporter { reported += it }

    private fun repository(dir: File = File(tempFolder.root, "logs")) =
        FileRepositoryImpl(dir, errorReporter, retentionDays = 7)

    @Test
    fun getFile_returnsNullWhenNoLogToday() = runBlocking {
        assertNull(repository().getFile())
    }

    @Test
    fun createLogFile_appendsLinesToTodayFile() = runBlocking {
        val repo = repository()

        repo.createLogFile("first")
        repo.createLogFile("second")

        val file = repo.getFile()!!
        assertEquals("${getCurrentToday()}.txt", file.name)
        val lines = file.readLines()
        assertEquals(2, lines.size)
        assertTrue(lines[0].startsWith("【ERROR】[${getCurrentToday()} "))
        assertTrue(lines[0].endsWith(" first"))
        assertTrue(lines[1].endsWith(" second"))
        assertTrue(reported.isEmpty())
    }

    @Test
    fun createLogFile_deletesLogsOlderThanRetention() = runBlocking {
        val dir = File(tempFolder.root, "logs").apply { mkdirs() }
        val old = File(dir, "2000-01-01.txt").apply { writeText("old") }
        old.setLastModified(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(8))
        val recent = File(dir, "recent.txt").apply { writeText("recent") }

        repository(dir).createLogFile("now")

        assertFalse(old.exists())
        assertTrue(recent.exists())
        assertTrue(File(dir, "${getCurrentToday()}.txt").exists())
    }

    @Test
    fun createLogFile_reportsErrorInsteadOfThrowing() = runBlocking {
        // 디렉터리 자리에 파일이 있어 mkdirs/쓰기가 실패하는 상황
        val blocked = File(tempFolder.root, "logs").apply { writeText("not a directory") }

        repository(blocked).createLogFile("msg")

        assertEquals(1, reported.size)
    }
}
