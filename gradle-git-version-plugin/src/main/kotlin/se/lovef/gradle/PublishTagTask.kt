package se.lovef.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import se.lovef.git.GitVersion
import se.lovef.git.NoTagException

/**
 * Date: 2018-05-01
 * @author Love
 */
open class PublishTagTask : DefaultTask() {

    lateinit var gitVersion: GitVersion

    @TaskAction fun publishTag() {
        try {
            gitVersion.publish()
        } catch (e: NoTagException) {
            printNoTag(e)
            throw e
        }
    }

    private fun printNoTag(e: NoTagException) {
        val message = """
            ${e.message}

            Commands:

                ./gradlew tag               # to create tag
                ./gradlew tag publishTag    # to create tag and publish it"""
        println(message.trimIndent())
    }
}
