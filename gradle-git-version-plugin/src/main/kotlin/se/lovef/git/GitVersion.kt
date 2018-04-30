package se.lovef.git

class GitVersion(private val git: Git, val baseVersion: String) {

    val version: String
        get() = currentReleaseTags.let {
            return if (it.size == 1) {
                it.first().substring(1)
            } else {
                "$baseVersion-SNAPSHOT"
            }
        }

    fun getCurrentReleaseTag(): String? = currentReleaseTags
        .also { if(it.size > 1) throw MultipleReleaseTagsException(it) }
        .firstOrNull()

    private val currentReleaseTags: List<String>
        get() = git.currentTags
            .filter { it.startsWith("v$baseVersion.") }

}

abstract class GitVersionException(message: String) : RuntimeException(message)

class MultipleReleaseTagsException(
    val releaseTags: List<String>
) : GitVersionException("Multiple release tags found: $releaseTags")
