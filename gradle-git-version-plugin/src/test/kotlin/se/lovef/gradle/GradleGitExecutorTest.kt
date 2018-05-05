package se.lovef.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
import se.lovef.assert.doesContain
import se.lovef.assert.v1.throws
import se.lovef.git.GitExecutorException

/**
 * Date: 2018-05-05
 * @author Love
 */
class GradleGitExecutorTest {

    private val project = ProjectBuilder.builder().build()
    private val executor = GradleGitExecutor(project)

    @Test fun `read git version`() {
        executor.invoke("--version")
    }

    @Test fun `git fail`() {
        { executor.invoke("--invalid-argument") }
            .throws(GitExecutorException::class)
            .message doesContain "--invalid-argument"
    }
}
