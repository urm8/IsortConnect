package com.github.urm8.isortconnect.listeners

import com.github.urm8.isortconnect.service.SorterService
import com.intellij.openapi.vfs.VirtualFile

data class PyFileWithService(val file: VirtualFile, val service: SorterService)
