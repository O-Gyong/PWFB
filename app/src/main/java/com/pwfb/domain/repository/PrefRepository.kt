package com.pwfb.domain.repository

import com.pwfb.domain.entity.PrefKey
import com.pwfb.domain.entity.PwfbResultEntity
import kotlinx.coroutines.flow.Flow

interface PrefRepository {

    /**
     * 설정 값 관찰. 저장된 값이 없으면 PrefKey.default를 방출한다.
     */
    fun observe(key: PrefKey): Flow<String>

    /**
     * 설정 값 저장
     */
    suspend fun set(key: PrefKey, value: String): PwfbResultEntity

    /**
     * 최초 진입 여부. 저장된 값이 없으면 최초 진입(true)으로 판단한다.
     */
    fun observeFirstInit(): Flow<Boolean>

    suspend fun setFirstInit(): PwfbResultEntity
}
