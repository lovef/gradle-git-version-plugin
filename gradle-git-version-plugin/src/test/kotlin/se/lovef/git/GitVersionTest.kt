package se.lovef.git

import org.junit.Test
import se.lovef.assert.v1.shouldBeNull
import se.lovef.assert.v1.shouldContain
import se.lovef.assert.v1.shouldEqual
import se.lovef.assert.v1.throws

/**
 * Date: 2018-04-30
 * @author Love
 */
class GitVersionTest {

    private val git = object : Git {

        var shouldThrowException = false
        fun shouldThrowException() {
            shouldThrowException = true
        }

        var currentTags: List<String> = emptyList()
        override fun currentTags() = if (shouldThrowException) throw Exception() else currentTags

        override fun tag(name: String) {
            if (shouldThrowException) throw Exception()
            currentTags += name
        }

        val publishedTags = ArrayList<String>()
        override fun publishTag(name: String) {
            if (shouldThrowException) throw Exception()
            publishedTags += name
        }

        var matchingTags: Map<String, List<String>> = emptyMap()
        override fun matchingTags(prefix: String) = if (shouldThrowException) throw Exception()
        else matchingTags[prefix] ?: emptyList()
    }

    private val config = object : GitVersion.Config {
        override var baseVersion = "1.0"
    }
    private val gitVersion = GitVersion(git, config)
    private val version get() = gitVersion.version

    private fun givenTags(vararg tags: String) = apply { git.currentTags = tags.toList() }

    @Test fun `version defaults to base version with -SNAPSHOT suffix`() {
        version shouldEqual "1.0-SNAPSHOT"
    }

    @Test fun `version is default on exception`() {
        givenTags("v1.0.123")
        git.shouldThrowException()

        version shouldEqual "1.0-SNAPSHOT"
    }

    @Test fun `version is taken from current release tag if it exists`() {
        givenTags("v1.0.123").version shouldEqual "1.0.123"
        givenTags("v1.0.123-code").version shouldEqual "1.0.123-code"
        givenTags("invalid-tag", "v1.0.123").version shouldEqual "1.0.123"
    }

    @Test fun `multiple or no release tags results in default version`() {
        givenTags("v0.1.123").version shouldEqual "1.0-SNAPSHOT"
        givenTags("v0.1.0", "v1.0.0", "v1.0.1").version shouldEqual "1.0-SNAPSHOT"
    }


    @Test fun `current release tag`() {
        givenTags("v0.0.0", "v1.0.0", "v2.0.0").gitVersion.tag shouldEqual "v1.0.0"
    }

    @Test fun `current release tag default null`() {
        givenTags("v0.0.0", "v2.0.0").gitVersion.tag.shouldBeNull()
    }

    @Test fun `multiple release tags defaults to null`() {
        givenTags("v1.0.0", "v1.0.1").gitVersion.tag.shouldBeNull()
    }

    @Test fun `current release tag returns null on exception`() {
        git.shouldThrowException()
        givenTags("v0.0.0", "v1.0.0", "v2.0.0").gitVersion.tag.shouldBeNull()
    }


    @Test fun `create release tag`() {
        gitVersion.createTag() shouldEqual "v1.0.0"
        git.currentTags() shouldEqual listOf("v1.0.0")
        gitVersion.version shouldEqual "1.0.0"
    }

    @Test fun `patch number is incremented when creating new tag`() {
        git.matchingTags = mapOf("v1.0." to listOf("v1.0.0", "v1.0.10", "v1.0.2")) // Scrambled list

        gitVersion.createTag() shouldEqual "v1.0.11"
        git.currentTags() shouldEqual listOf("v1.0.11")
    }

    @Test fun `create release tag fails if current tag exists`() {
        val matchingReleaseTags = listOf("v1.0.0", "v1.0.1")
        git.currentTags = listOf("v0.1.0") + matchingReleaseTags

        { gitVersion.createTag() }
            .throws(AlreadyTaggedException::class)
            .also {
                it.tags shouldEqual matchingReleaseTags
                it.message shouldContain matchingReleaseTags
            }
    }


    @Test fun `publish version tag`() {
        git.currentTags = listOf("v1.0.0")
        gitVersion.publish()
        git.publishedTags shouldEqual listOf("v1.0.0")
    }

    @Test fun `publish version tag fails if there is no tag`() {
        { gitVersion.publish() }
            .throws(NoTagException::class)
            .message shouldContain gitVersion.config.baseVersion
    }
}
