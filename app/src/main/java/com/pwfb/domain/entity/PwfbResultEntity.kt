package com.pwfb.domain.entity

sealed class PwfbResultEntity {
    data object Success : PwfbResultEntity()
    data class Failure(val exception: Exception) : PwfbResultEntity()
}
