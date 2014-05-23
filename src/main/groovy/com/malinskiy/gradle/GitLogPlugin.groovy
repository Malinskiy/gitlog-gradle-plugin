package com.malinskiy.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class GitLogPlugin implements Plugin<Project> {

    def void apply(Project project) {
        project.task('generateGitLog', type: GitLogTask)
    }
}