@file:Suppress("FunctionName")

package se.lovef.gradle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import se.lovef.assert.v1.shouldContain
import se.lovef.assert.v1.shouldEqual
import se.lovef.util.exec
import se.lovef.util.execute
import java.io.File

class GradleGitVersionPluginKotlinTest {

    @get:Rule val testProjectDir = TemporaryFolder()
    private val dir get() = testProjectDir.root
    private val buildGradleKts by lazy {
        testProjectDir.newFile("build.gradle.kts")
            .also { it.appendText("""plugins { id("se.lovef.git-version")}""") }
    }
    private val printTask = """
            tasks.register("print") {
                doLast {
                    println("> created version: " + gitVersion.version + ", version code " + gitVersion.versionCode + " <")
                }
                mustRunAfter(tasks["tag"])
            }
    """.trimIndent()

    operator fun File.plusAssign(text: String) {
        appendText('\n' + text.trimIndent())
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
            execute("add", buildGradleKts.name)
            execute("commit", "-m", "added build.gradle")
        }

        fun tag() = (dir exec "git tag").split('\n').filterNot { it.isBlank() }
    }


    @Test fun `set version from git`() {
        buildGradleKts += """
            version = gitVersion("1.0")
            println("version: " + version)
        """

        git.initWithBuildFile()

        gradle().output shouldContain "version: 1.0-SNAPSHOT"
    }

    @Test fun `create version tag`() {
        buildGradleKts += """
            version = gitVersion("1.0")
        """
        buildGradleKts += printTask

        git.initWithBuildFile()

        val result = gradle("tag", "print")
        git.tag() shouldEqual listOf("v1.0.0")
        result.output shouldContain "> created version: 1.0.0, version code null <"
        result.task(":tag")?.outcome shouldEqual TaskOutcome.SUCCESS
    }

    @Test fun `create version tag with version code`() {
        buildGradleKts += """
            version = gitVersion("1.0", useVersionCode = true)
        """
        buildGradleKts += printTask

        git.initWithBuildFile()

        val result = gradle("tag", "print")
        git.tag() shouldEqual listOf("v1.0.0-1")
        result.output shouldContain "> created version: 1.0.0, version code 1 <"
        result.task(":tag")?.outcome shouldEqual TaskOutcome.SUCCESS
    }
}
