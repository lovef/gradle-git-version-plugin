package se.lovef.git

import org.junit.Before
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

    object Config : GitVersion.Config {
        override lateinit var baseVersion: String
    }

    private val gitVersion = GitVersion(git, Config)

    @Before fun before() {
        Config.baseVersion = "1.0"
    }

    @Test fun `version defaults to base version with -SNAPSHOT suffix`() {
        gitVersion.version shouldEqual "1.0-SNAPSHOT"
    }

    @Test fun `version returns default on exception`() {
        git.currentTags = listOf("v1.0.123")
        gitVersion.version shouldEqual "1.0.123" // Control
        git.shouldThrowException()
        gitVersion.version shouldEqual "1.0-SNAPSHOT"
    }

    @Test fun `version is taken from current release tag if it exists`() {
        git.currentTags = listOf("v1.0.123")
        gitVersion.version shouldEqual "1.0.123"

        git.currentTags = listOf("v0.1.123", "v1.0.123")
        gitVersion.version shouldEqual "1.0.123"
    }

    @Test fun `multiple or no release tags results in default version`() {
        git.currentTags = listOf("v0.1.123")
        gitVersion.version shouldEqual "1.0-SNAPSHOT"

        git.currentTags = listOf("v0.1.0", "v1.0.0", "v1.0.1")
        gitVersion.version shouldEqual "1.0-SNAPSHOT"
    }


    @Test fun `current release tag`() {
        git.currentTags = listOf("v0.0.0", "v1.0.0", "v2.0.0")

        gitVersion.tag shouldEqual "v1.0.0"
    }

    @Test fun `current release tag default null`() {
        git.currentTags = listOf("v0.0.0", "v2.0.0") // No tag matching the base version

        gitVersion.tag.shouldBeNull()
    }

    @Test fun `multiple release tags defaults to null`() {
        git.currentTags = listOf("v1.0.0", "v1.0.1") // Multiple tags matching the base version

        gitVersion.tag.shouldBeNull()
    }

    @Test fun `current release tag returns null on exception`() {
        git.currentTags = listOf("v0.0.0", "v1.0.0", "v2.0.0")
        gitVersion.tag shouldEqual "v1.0.0" // Control
        git.shouldThrowException()
        gitVersion.tag.shouldBeNull()
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
