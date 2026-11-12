package com.pwfb.domain.repository

import java.io.File

interface FileRepository {
    /**
     * 오늘 날짜 로그 파일에 한 줄을 덧붙인다. 보관 기간이 지난 로그는 함께 삭제한다.
     */
    suspend fun createLogFile(msg: String)

    /**
     * 오늘 날짜 로그 파일. 없으면 null.
     */
    suspend fun getFile(): File?
}
