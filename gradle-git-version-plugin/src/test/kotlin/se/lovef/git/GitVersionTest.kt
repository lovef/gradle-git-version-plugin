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

    @Test fun `get current version tag`() {
        val gitVersion = GitVersion(git, baseVersion = "1.0")

        git.currentTags = emptyList()
        gitVersion.getCurrentReleaseTag().shouldBeNull()

        git.currentTags = listOf("v1.0.0")
        gitVersion.getCurrentReleaseTag() shouldEqual "v1.0.0"
    }

    @Test fun `current release tag must match base version`() {
        val gitVersion = GitVersion(git, baseVersion = "1.0")

        git.currentTags = listOf("v1.0.123")
        gitVersion.getCurrentReleaseTag() shouldEqual "v1.0.123"

        git.currentTags = listOf("v0.1.123")
        gitVersion.getCurrentReleaseTag().shouldBeNull()

        git.currentTags = listOf("v0.1.123", "v1.0.123")
        gitVersion.getCurrentReleaseTag() shouldEqual "v1.0.123"
    }

    @Test fun `to many release tags result in an exception`() {
        val gitVersion = GitVersion(git, baseVersion = "1.0")

        val matchingReleaseTags = listOf("v1.0.0", "v1.0.1")
        git.currentTags = listOf("v0.1.0") + matchingReleaseTags

        { gitVersion.getCurrentReleaseTag() }
            .throws(MultipleReleaseTagsException::class)
            .also {
                it.releaseTags shouldEqual matchingReleaseTags
                it.message shouldContain matchingReleaseTags
            }
    }
}
