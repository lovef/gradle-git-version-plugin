package se.lovef.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import se.lovef.git.GitVersion

/**
 * Date: 2018-04-30
 * @author Love
 */
class GradleGitVersionPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val gitVersion = GitVersion(GradleGit(project), project.properties["baseVersion"] as String)
        project.version = gitVersion.version
        project.tasks.create("tag", TagTask::class.java) {
            it.gitVersion = gitVersion
        }
    }
}
