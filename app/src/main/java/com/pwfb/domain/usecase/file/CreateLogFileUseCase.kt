package com.pwfb.domain.usecase.file

import com.pwfb.domain.repository.FileRepository
import javax.inject.Inject

class CreateLogFileUseCase @Inject constructor(private val fileRepository: FileRepository) {
    suspend operator fun invoke(msg: String) = fileRepository.createLogFile(msg)
}
