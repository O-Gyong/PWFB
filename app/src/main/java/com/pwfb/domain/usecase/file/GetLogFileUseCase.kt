package com.pwfb.domain.usecase.file

import com.pwfb.domain.repository.FileRepository
import java.io.File
import javax.inject.Inject

class GetLogFileUseCase @Inject constructor(private val fileRepository: FileRepository) {
    suspend operator fun invoke(): File? = fileRepository.getFile()
}
