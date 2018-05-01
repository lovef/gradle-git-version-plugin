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
        project.extensions.extraProperties["gitVersion"] = ExternalGitVersion(gitVersion)
        val tagTask = project.tasks.create("tag", TagTask::class.java) {
            it.gitVersion = gitVersion
        }
        project.tasks.create("publishTag", PublishTagTask::class.java) {
            it.gitVersion = gitVersion
            it.shouldRunAfter(tagTask)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    class ExternalGitVersion(private val gitVersion: GitVersion) {
        val version get() = gitVersion.version
        val tag get() = gitVersion.tag

        override fun toString(): String {
            return """{ version: "$version", tag: ${tag.jsonValue} }"""
        }

        private val String?.jsonValue get() = this?.let { "\"$it\"" }
    }
}
