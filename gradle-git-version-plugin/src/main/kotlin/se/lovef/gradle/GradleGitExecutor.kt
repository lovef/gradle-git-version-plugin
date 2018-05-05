package se.lovef.gradle

import org.gradle.api.Project
import se.lovef.git.GitExecutor
import se.lovef.git.GitExecutorException
import java.io.ByteArrayOutputStream

class GradleGitExecutor(private val project: Project) : GitExecutor {

    override fun invoke(vararg arguments: String): String {
        return execute("git", *arguments)
    }

    private fun execute(vararg arguments: String): String {
        val standardOutput = ByteArrayOutputStream()
        val errorOutput = ByteArrayOutputStream()
        try {
            project.exec {
                it.commandLine(*arguments)
                it.standardOutput = standardOutput
                it.errorOutput = errorOutput
            }
        } catch (e: Exception) {
            throw GitExecutorException(errorOutput.toString().trim(), e)
        }
        return standardOutput.toString()
    }
}
