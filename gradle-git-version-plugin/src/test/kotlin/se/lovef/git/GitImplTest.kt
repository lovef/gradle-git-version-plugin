package se.lovef.git

import com.nhaarman.mockito_kotlin.*
import org.junit.Test
import se.lovef.assert.v1.shouldBeNull
import se.lovef.assert.v1.shouldContain
import se.lovef.assert.v1.shouldEqual
import se.lovef.assert.v1.throws

/**
 * Date: 2018-05-05
 * @author Love
 */
class GitImplTest {

    private val git: GitExecutor = mock()
    private val gitImpl = GitImpl(git)

    private val name = "tag name"

    @Test fun `tag commit`() {
        gitImpl.tag(name)
        verify(git).invoke("tag", name)
    }


    @Test fun `publish tag`() {
        gitImpl.publishTag(name)
        verify(git).invoke("push", "origin", name)
    }


    @Test fun `current tags`() {
        val tags = listOf("tag-a", "tag-b", "v1.0.0")
        doReturn(tags.joinToString("\n", postfix = "\n")).whenever(git).invoke(any())

        gitImpl.currentTags() shouldEqual tags

        verify(git).invoke("tag", "--points-at", "HEAD")
    }

    @Test fun `no current tags`() {
        doReturn("\n").whenever(git).invoke(any())

        gitImpl.currentTags() shouldEqual emptyList()

        verify(git).invoke("tag", "--points-at", "HEAD")
    }


    @Test fun `matching tags`() {
        val prefix = "tagPrefix"
        val tagsWithPrefix = listOf("a", "b", "c").map { "$prefix-$it" }
        doReturn(tagsWithPrefix.joinToString("\n", postfix = "\n")).whenever(git).invoke(any())

        gitImpl.matchingTags(prefix) shouldEqual tagsWithPrefix

        verify(git).invoke("tag", "-l", "$prefix*")
    }

    @Test fun `last version tag with version code`() {
        val prefix = "tagPrefix"
        val tag = "${prefix}12.23.34-45"
        doReturn(tag + "\n").whenever(git).invoke(any())

        gitImpl.lastTagWitchVersionCode(prefix) shouldEqual tag

        verifyGitInvoked("""describe --abbrev=0 HEAD --tags --match "$prefix*-*"""")
    }

    @Test fun `last version tag with version code when no match is found`() {
        val toBeThrown = Exception()
        doThrow(toBeThrown).whenever(git).invoke(any())
        val prefix = "prefix"

        gitImpl.lastTagWitchVersionCode(prefix).shouldBeNull()

        verifyGitInvoked("""describe --abbrev=0 HEAD --tags --match "$prefix*-*"""")
    }

    private fun verifyGitInvoked(command: String) {
        val arguments = command.split(' ').toTypedArray()
        verify(git).invoke(*arguments)
    }
}

class GitImplExecutionFailureTest {

    private lateinit var executorException: Exception
    private val git = object : GitExecutor {
        override fun invoke(vararg arguments: String): String {
            throw executorException
        }
    }

    private val gitImpl = GitImpl(git)

    @Test fun `tag executor exceptions are wrapped`() {
        { gitImpl.tag("tag-name") } shouldThrowExceptionWithMessage "git tag tag-name"
    }

    @Test fun `publish tag executor exceptions are wrapped`() {
        { gitImpl.publishTag("tag-name") } shouldThrowExceptionWithMessage "git push origin tag-name"
    }

    @Test fun `current tag executor exceptions are wrapped`() {
        { gitImpl.currentTags() } shouldThrowExceptionWithMessage "git tag --points-at HEAD"
    }

    @Test fun `matching tag executor exceptions are wrapped`() {
        { gitImpl.matchingTags("prefix") } shouldThrowExceptionWithMessage "git tag -l prefix*"
    }

    private infix fun (() -> Any).shouldThrowExceptionWithMessage(message: String) {
        executorException = Exception("Test exception")
        throws(GitExecutorException::class)
            .also {
                it.message shouldContain message
                it.cause shouldEqual executorException
            }
    }
}
