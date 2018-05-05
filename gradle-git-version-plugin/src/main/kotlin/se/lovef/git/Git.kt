package se.lovef.git

interface Git {
    fun tag(name: String)
    fun publishTag(name: String)
    fun currentTags(): List<String>
    fun matchingTags(prefix: String): List<String>
}

class GitImpl(
    private val executor: GitExecutor
) : Git {

    override fun tag(name: String) {
        invokeGit("tag", name)
    }

    override fun publishTag(name: String) {
        invokeGit("push", "origin", name)
    }

    override fun currentTags(): List<String> =
        invokeGit("tag", "--points-at", "HEAD")
            .split("\n")
            .filterNot { it.isBlank() }

    override fun matchingTags(prefix: String): List<String> =
        invokeGit("tag", "-l", "$prefix*")
            .split("\n")
            .filterNot { it.isBlank() }

    private fun invokeGit(vararg arguments: String): String {
        try {
            return executor.invoke(*arguments)
        } catch (e: Exception) {
            throw GitExecutorException("Failed to invoke: git ${arguments.joinToString(" ")}", e)
        }
    }
}

class GitExecutorException(message: String, executorException: Exception) : RuntimeException(message, executorException)
