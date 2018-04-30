package se.lovef.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import se.lovef.git.AlreadyTaggedException
import se.lovef.git.GitVersion

/**
 * Date: 2018-04-30
 * @author Love
 */
open class TagTask : DefaultTask() {

    lateinit var gitVersion: GitVersion

    @TaskAction fun tag() {
        try {
            val tag = gitVersion.createTag()
            printCreatedTag(tag)
        } catch (e: AlreadyTaggedException) {
            printAlreadyTagged(e)
            throw e
        }
    }

    private fun printCreatedTag(tag: String) {
        val message = """
            Created tag $tag

            Commands:

                git tag --delete $tag # To delete
                git push origin $tag  # To push

            New version: ${gitVersion.version}"""
        println(message.trimIndent())
    }

    private fun printAlreadyTagged(e: AlreadyTaggedException) {
        val message = """
            ${e.message}

            Commands:

                git tag --delete ${e.tags.joinToString(" ")} # To delete"""
        println(message.trimIndent())
    }
}
