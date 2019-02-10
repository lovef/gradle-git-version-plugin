plugins {
    id("se.lovef.git-version") version "0.3.2"
}

version = gitVersion("1.0")

println("gitVersion: $gitVersion")

tasks.register("printGitVersion") {
    doLast {
        println("gitVersion: $gitVersion")
    }
    mustRunAfter(tasks["tag"])
}
