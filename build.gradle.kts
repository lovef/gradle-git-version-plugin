plugins {
    id("se.lovef.git-version") version "0.2.3"
}

version = gitVersion(properties["baseVersion"])
println(version)
println(gitVersion)

allprojects {
    version = rootProject.version
    group = "se.lovef"
    extra["tag"] = rootProject.gitVersion.tag

    repositories {
        jcenter() // JCenter is used by the gradle portal
    }
}
