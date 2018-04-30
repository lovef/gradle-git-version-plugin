package se.lovef.git

interface Git {
    val currentTags: List<String>
    fun tag(name: String)
    fun matchingTags(prefix: String): List<String>
}
