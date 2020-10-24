package com.github.urm8.isortconnect.services

import com.intellij.openapi.project.Project
import com.github.urm8.isortconnect.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
