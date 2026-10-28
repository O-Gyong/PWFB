package com.pwfb.domain.usecase

import com.pwfb.domain.entity.PrefKey
import com.pwfb.domain.entity.PwfbResultEntity
import com.pwfb.domain.repository.PrefRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PrefUseCase @Inject constructor(
    private val repository: PrefRepository
) {
    /**
     * 설정 값 관찰
     * 저장된 값이 바뀔 때마다 새 값을 방출하고, 저장된 값이 없으면 [PrefKey.default]를 방출한다.
     */
    fun observe(key: PrefKey): Flow<String> = repository.observe(key)

    /**
     * 설정 값 저장
     * 성공하면 [PwfbResultEntity.Success], 저장 중 예외가 발생하면 [PwfbResultEntity.Failure]를 반환한다.
     */
    suspend fun set(key: PrefKey, value: String): PwfbResultEntity = repository.set(key, value)

    /**
     * 최초 진입 여부 관찰
     * 저장된 값이 없으면 앱 최초 진입(true)으로 판단한다.
     */
    fun observeFirstInit(): Flow<Boolean> = repository.observeFirstInit()

    /**
     * 최초 진입 완료 처리
     * 이후 [observeFirstInit]이 false를 방출한다.
     */
    suspend fun setFirstInit(): PwfbResultEntity = repository.setFirstInit()
}
