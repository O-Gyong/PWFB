package com.pwfb.data

import com.pwfb.domain.repository.ErrorReporter
import com.pwfb.domain.repository.FileRepository
import com.pwfb.util.getCurrentTime
import com.pwfb.util.getCurrentToday
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class FileRepositoryImpl(
    private val logsDirectory: File,
    private val errorReporter: ErrorReporter,
    private val retentionDays: Int = DEFAULT_RETENTION_DAYS
) : FileRepository {

    companion object {
        const val DEFAULT_RETENTION_DAYS = 7
    }

    override suspend fun createLogFile(msg: String) = withContext(Dispatchers.IO) {
        try {
            logsDirectory.mkdirs()
            val header = "【ERROR】[${getCurrentToday()} ${getCurrentTime()}]"
            todayFile().appendText("$header $msg\n")
            deleteExpiredLogs()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorReporter.report(e)
        }
    }

    override suspend fun getFile(): File? = withContext(Dispatchers.IO) {
        todayFile().takeIf { it.exists() }
    }

    private fun todayFile() = File(logsDirectory, "${getCurrentToday()}.txt")

    /**
     * 로그가 무한히 쌓이지 않도록 보관 기간이 지난 파일을 지운다.
     */
    private fun deleteExpiredLogs() {
        val expireBefore = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(retentionDays.toLong())
        logsDirectory.listFiles()
            ?.filter { it.isFile && it.lastModified() < expireBefore }
            ?.forEach { it.delete() }
    }
}
