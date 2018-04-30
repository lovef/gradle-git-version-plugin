package se.lovef.gradle

import org.gradle.api.Project
import se.lovef.git.Git
import java.io.ByteArrayOutputStream

class GradleGit(private val project: Project) : Git {

    override val currentTags: List<String> get() = execForOutput("git", "tag", "--points-at", "HEAD")
        .split('\n')
        .filterNot { it.isBlank() }

    override fun tag(name: String) {
        execForOutput("git", "tag", name)
    }

    override fun matchingTags(prefix: String) = execForOutput("git", "tag", "-l", "$prefix*")
        .split('\n')
        .filterNot { it.isBlank() }

    private fun execForOutput(vararg arguments: String): String {
        try {
            val stdout = ByteArrayOutputStream()
            project.exec {
                it.commandLine(*arguments)
                it.standardOutput = stdout
            }
            return stdout.toString()
        } catch (e: Exception) {
            println("Exception on invoking ${arguments.joinToString(" ")}: $e")
            return ""
        }
    }
}
