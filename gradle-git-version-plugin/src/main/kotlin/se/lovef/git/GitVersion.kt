package se.lovef.git

class GitVersion(private val git: Git, val config: Config) {

    constructor(gitExecutor: GitExecutor, config: Config) : this(GitImpl(gitExecutor), config)

    interface Config {
        val baseVersion: String
    }

    private val baseVersion get() = config.baseVersion

    val version: String
        get() = tag?.substring(1) ?: "$baseVersion-SNAPSHOT"

    val tag: String?
        get() = tags.let { if (it.size == 1) it.first() else null }

    fun createTag(): String {
        assertNoReleaseTags()

        val prefix = "v$baseVersion."
        val newPatch = newPatch(prefix)
        val newTag = "$prefix$newPatch"

        git.tag(newTag)

        return newTag
    }

    private fun assertNoReleaseTags() {
        val tags = tags
        if (tags.isNotEmpty()) {
            throw AlreadyTaggedException(tags)
        }
    }

    private fun newPatch(prefix: String): Int {
        return git.matchingTags(prefix)
            .map { it.substring(prefix.length).toInt() }
            .max()
            ?.let { it + 1 } ?: 0
    }

    fun publish() {
        val name = tag ?: throw NoTagException(config.baseVersion)
        git.publishTag(name)
    }

    private val tags: List<String>
        get() =
            try {
                git.currentTags().filter { it.startsWith("v$baseVersion.") }
            } catch (exception: Exception) {
                emptyList()
            }
}

abstract class GitVersionException(message: String) : RuntimeException(message)

class AlreadyTaggedException(
    val tags: List<String>
) : GitVersionException("Already tagged with $tags")

class NoTagException(
    baseVersion: String
) : GitVersionException("There is no tag to publish for base version $baseVersion")
