package se.lovef.git

private val versionCodeRegex = "-(\\d+)$".toRegex()
private val patchAndVersionCodeRegex = "^(\\d+)(-(\\d+))?$".toRegex()

class GitVersion(private val git: Git, val config: Config) {

    constructor(gitExecutor: GitExecutor, config: Config) : this(GitImpl(gitExecutor), config)

    interface Config {
        val baseVersion: String
        val useVersionCode: Boolean
    }

    private val baseVersion get() = config.baseVersion

    val version: String
        get() {
            val tagWithoutPrefix = tag?.substring(1) ?: return "$baseVersion-SNAPSHOT"
            if (config.useVersionCode) {
                return tagWithoutPrefix.replace(versionCodeRegex, "")
            }
            return tagWithoutPrefix
        }

    val versionCode: Int?
        get() {
            if (!config.useVersionCode) {
                return null
            }
            val tag = tag ?: return null
            val match = versionCodeRegex.find(tag) ?: return null
            return match.groupValues[1].toInt()
        }

    val tag: String?
        get() = tags.let { if (it.size == 1) it.first() else null }

    fun createTag(): String {
        assertNoReleaseTags()

        val prefix = "v$baseVersion."
        val currentVersion = currentVersion(prefix)
        val nextPatch = currentVersion?.patch?.plus(1) ?: 0
        var newTag = "$prefix$nextPatch"
        if (config.useVersionCode) {
            val currentVersionCode = currentVersion?.versionCode ?: lastVersionCode("v")
            val nextVersionCode = currentVersionCode?.plus(1) ?: 1
            newTag += "-$nextVersionCode"
        }

        git.tag(newTag)

        return newTag
    }

    private fun assertNoReleaseTags() {
        val tags = tags
        if (tags.isNotEmpty()) {
            throw AlreadyTaggedException(tags)
        }
    }

    private fun currentVersion(prefix: String): Version? {
        return git.matchingTags(prefix)
            .mapNotNull { createVersion(it, prefix) }
            .max()
    }

    private fun createVersion(versionTag: String, prefix: String): Version? {
        val suffix = versionTag.substring(prefix.length)
        val matchResult = patchAndVersionCodeRegex.matchEntire(suffix) ?: return null
        return Version(matchResult)
    }

    private class Version(patchAndVersionMatcher: MatchResult): Comparable<Version> {
        val patch: Int = patchAndVersionMatcher.groupValues[1].toInt()
        val versionCodeString = patchAndVersionMatcher.groupValues[3].let { if(it.isEmpty()) null else it }
        val versionCode: Int? get() = versionCodeString?.toInt()

        override fun compareTo(other: Version) = this.patch.compareTo(other.patch)
    }

    private fun lastVersionCode(prefix: String): Int? {
        return git.lastTagWitchVersionCode(prefix)
            ?.let { versionCodeRegex.find(it) }
            ?.let { it.groupValues[1].toInt() }
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
