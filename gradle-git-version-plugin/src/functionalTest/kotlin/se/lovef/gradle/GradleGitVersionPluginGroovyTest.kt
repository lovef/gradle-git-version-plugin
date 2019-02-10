@file:Suppress("FunctionName")

package se.lovef.gradle


import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import se.lovef.assert.v1.shouldContain
import se.lovef.assert.v1.shouldEqual
import se.lovef.util.exec
import se.lovef.util.execute
import java.io.File

/**
 * Date: 2018-05-06
 * @author Love
 */
class GradleGitVersionPluginGroovyTest {

    @get:Rule val testProjectDir = TemporaryFolder()
    private val dir get() = testProjectDir.root
    private val buildGradle by lazy {
        testProjectDir.newFile("build.gradle")
            .also { it.appendText("plugins { id 'se.lovef.git-version' }\n") }
    }


    private fun gradle(vararg command: String): BuildResult = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments(*command)
        .withPluginClasspath()
        .build()

    private val git = object {

        fun execute(vararg arguments: String) = dir.execute("git", *arguments)

        fun initWithBuildFile() = also {
            dir exec "git init"
            execute("add", buildGradle.name)
            execute("commit", "-m", "added build.gradle")
        }

        fun tag() = (dir exec "git tag").split('\n').filterNot { it.isBlank() }
    }


    operator fun File.plusAssign(@Language("groovy") text: String) {
        buildGradle.appendText(text.trimIndent())
    }


    @Test fun `set version from git`() {
        buildGradle += /* language=groovy */ """
            version gitVersion('1.0')
            println "version: " + version
        """

        git.initWithBuildFile()

        gradle().output shouldContain "version: 1.0-SNAPSHOT"
    }

    @Test fun `create version tag`() {
        buildGradle += /* language=groovy */ """
            version gitVersion('1.0')

            task print {
                doLast {
                    println "> created version: " + gitVersion.version + ", version code " + gitVersion.versionCode + ' <'
                }
            }

            print.mustRunAfter tag
        """

        git.initWithBuildFile()

        val result = gradle("tag", "print")
        git.tag() shouldEqual listOf("v1.0.0")
        result.output shouldContain "> created version: 1.0.0, version code null <"
        result.task(":tag")?.outcome shouldEqual TaskOutcome.SUCCESS
    }

    @Test fun `create version tag with version code`() {
        buildGradle += /* language=groovy */ """
            version gitVersion(baseVersion: '1.0', useVersionCode: true)

            task print {
                doLast {
                    println "> created version: " + gitVersion.version + ", version code " + gitVersion.versionCode + ' <'
                }
            }

            print.mustRunAfter tag
        """

        git.initWithBuildFile()

        val result = gradle("tag", "print")
        git.tag() shouldEqual listOf("v1.0.0-1")
        result.output shouldContain "> created version: 1.0.0, version code 1 <"
        result.task(":tag")?.outcome shouldEqual TaskOutcome.SUCCESS
    }
}
