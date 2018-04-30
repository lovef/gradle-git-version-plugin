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
        override var currentTags: List<String> = emptyList()

        override fun tag(name: String) {
            currentTags += name
        }

        var matchingTags: Map<String, List<String>> = emptyMap()

        override fun matchingTags(prefix: String) = matchingTags[prefix] ?: emptyList()
    }

    @Test fun `version defaults to base version with -SNAPSHOT suffix`() {
        val gitVersion = GitVersion(git, baseVersion = "1.0")

        gitVersion.version shouldEqual "1.0-SNAPSHOT"
    }

    @Test fun `version is taken from current release tag if it exists`() {
        val gitVersion = GitVersion(git, baseVersion = "1.0")

        git.currentTags = listOf("v1.0.123")
        gitVersion.version shouldEqual "1.0.123"

        git.currentTags = listOf("v0.1.123", "v1.0.123")
        gitVersion.version shouldEqual "1.0.123"
    }

    @Test fun `multiple or no release tags results in default version`() {
        val gitVersion = GitVersion(git, baseVersion = "1.0")

        git.currentTags = listOf("v0.1.123")
        gitVersion.version shouldEqual "1.0-SNAPSHOT"

        git.currentTags = listOf("v0.1.0", "v1.0.0", "v1.0.1")
        gitVersion.version shouldEqual "1.0-SNAPSHOT"
    }


    @Test fun `create release tag`() {
        val gitVersion = GitVersion(git, baseVersion = "1.0")

        gitVersion.createTag() shouldEqual "v1.0.0"
        git.currentTags shouldEqual listOf("v1.0.0")
        gitVersion.version shouldEqual "1.0.0"
    }

    @Test fun `patch number is incremented when creating new tag`() {
        val gitVersion = GitVersion(git, baseVersion = "1.0")

        git.matchingTags = mapOf("v1.0." to listOf("v1.0.0", "v1.0.10", "v1.0.2")) // Scrambled list

        gitVersion.createTag() shouldEqual "v1.0.11"
        git.currentTags shouldEqual listOf("v1.0.11")
    }

    @Test fun `create release tag fails if current tag exists`() {
        val gitVersion = GitVersion(git, baseVersion = "1.0")

        val matchingReleaseTags = listOf("v1.0.0", "v1.0.1")
        git.currentTags = listOf("v0.1.0") + matchingReleaseTags

        { gitVersion.createTag() }
            .throws(AlreadyTaggedException::class)
            .also {
                it.tags shouldEqual matchingReleaseTags
                it.message shouldContain matchingReleaseTags
            }
    }
}
