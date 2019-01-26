plugins {
    id("se.lovef.git-version") version "0.2.1"
    id("org.jetbrains.kotlin.jvm") version "1.2.41" apply false
    id("com.jfrog.bintray") version "1.8.0" apply false
    id("com.gradle.plugin-publish") version "0.9.10" apply false
}

version = gitVersion(properties["baseVersion"])
println(version)

allprojects {
    version = rootProject.version
    group = "se.lovef"

    repositories {
        jcenter() // JCenter is used by the gradle portal
    }
}
