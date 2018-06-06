package se.lovef.gradle

import groovy.lang.Closure
import org.gradle.api.Plugin
import org.gradle.api.Project
import se.lovef.git.GitVersion

/**
 * Date: 2018-04-30
 * @author Love
 */
class GradleGitVersionPlugin : Plugin<Project> {

    class Config(
        private val project: Project
    ) : GitVersion.Config {
        lateinit var gitVersionExtension: GitVersionExtension
        override val baseVersion: String
            get() = project.properties["baseVersion"] as? String ?: gitVersionExtension.baseVersion
    }

    override fun apply(project: Project) {
        val config = Config(project)
        val gitVersion = GitVersion(GradleGitExecutor(project), config)
        val gitVersionExtension = project.extensions.create(
            "gitVersion", GitVersionExtension::class.java,
            this, project, gitVersion
        )
        config.gitVersionExtension = gitVersionExtension
        project.version = gitVersion.version
        val tagTask = project.tasks.create("tag", TagTask::class.java) {
            it.gitVersion = gitVersion
        }
        project.tasks.create("publishTag", PublishTagTask::class.java) {
            it.gitVersion = gitVersion
            it.mustRunAfter(tagTask)
        }
    }

    open class GitVersionExtension(
        owner: GradleGitVersionPlugin,
        private val project: Project,
        private val gitVersion: GitVersion
    ) : Closure<String>(owner, owner) {

        var baseVersion = "0.0"
            set(value) {
                field = value
                project.version = version
            }

        val version get() = gitVersion.version

        val tag get() = gitVersion.tag

        fun doCall() = toString()

        fun doCall(baseVersion: String) = invoke(baseVersion)

        fun doCall(properties: Map<String, String?>): String {
            properties["baseVersion"]?.let { baseVersion = it }
            return version
        }

        operator fun invoke(baseVersion: String): String {
            this.baseVersion = baseVersion
            return version
        }

        override fun toString(): String {
            return """{ version: "$version", tag: ${tag.jsonValue} }"""
        }

        private val kotlin.String?.jsonValue get() = this?.let { "\"$it\"" }
    }
}
