package se.lovef.git

/**
 * Date: 2018-05-05
 * @author Love
 */
interface GitExecutor {

    @Throws(Exception::class)
    operator fun invoke(vararg arguments: String): String
}
