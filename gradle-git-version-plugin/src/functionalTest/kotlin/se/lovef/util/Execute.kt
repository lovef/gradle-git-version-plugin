package se.lovef.util

import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Shorthand for [File.execute]. Assumes that all spaces are argument separators,
 * so no argument may contain a space.
 * ```kotlin
 *  // Example
 *  directory exec "git status"
 *
 *  // This fails since `'A` and `message'` will be considered as two arguments
 *  directory exec "git commit -m 'A message'"
 * ```
 * Inspired by https://stackoverflow.com/a/41495542/1020871
 */
infix fun File.exec(command: String): String {
    val arguments = command.split(' ').toTypedArray()
    return execute(*arguments)
}

/**
 * Executes command. Arguments may contain strings. More appropriate than [File.exec]
 * when using dynamic arguments.
 * ```kotlin
 *  // Example
 *  directory.execute("git", "commit", "-m", "A message")
 * ```
 * Inspired by https://stackoverflow.com/a/41495542/1020871
 */
fun File.execute(vararg arguments: String): String {
    val process = ProcessBuilder(*arguments)
        .directory(this)
        .start()
        .also { it.waitFor(10, TimeUnit.SECONDS) }

    if (process.exitValue() != 0) {
        throw Exception(process.errorStream.bufferedReader().readText())
    }
    return process.inputStream.bufferedReader().readText()
}
