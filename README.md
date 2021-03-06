[![Build Status](https://travis-ci.org/lovef/gradle-git-version-plugin.svg?branch=master)](https://travis-ci.org/lovef/gradle-git-version-plugin)
[![Download](https://api.bintray.com/packages/lovef/maven/gradle-git-version-plugin/images/download.svg?version=0.3.2)](https://bintray.com/lovef/maven/gradle-git-version-plugin/0.3.2/link)

# Gradle Git Version Plugin

Set version based on git tags and create new git version tags.

Example:

```gradle
// build.gradle

plugins {
    id 'se.lovef.git-version' version '0.3.2'
}

version gitVersion('1.0')

println "gitVersion: $gitVersion"

task printGitVersion {
    doLast {
        println "gitVersion: $gitVersion"
    }
}

printGitVersion.mustRunAfter tag
```

```kotlin
// build.gradle.kts

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
```

Output:

```bash
$ ./gradlew tag printGitVersion

> Configure project :
gitVersion: { version: "1.0-SNAPSHOT", tag: null }

> Task :tag
Created tag v1.0.0

Commands:

    git tag --delete v1.0.0    # delete
    git push origin  v1.0.0    # push to origin

New version: 1.0.0

> Task :printGitVersion
gitVersion: { version: "1.0.0", tag: "v1.0.0" }

BUILD SUCCESSFUL in 1s
2 actionable tasks: 2 executed
```

## An attribution

This project is in part inspired by
[Git-Version Gradle Plugin](https://github.com/palantir/gradle-git-version)
