[![Build Status](https://travis-ci.org/lovef/gradle-git-version-plugin.svg?branch=master)](https://travis-ci.org/lovef/gradle-git-version-plugin)

# Gradle Git Version Plugin

Creates version tags in git

Example:

```properties
# gradle.properties
baseVersion = 1.0
```

```gradle
// build.gradle

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "se.lovef:gradle-git-version-plugin:0.0.2"
    }
}

apply plugin: 'se.lovef.git-version'

println "Version: $version"
println "gitVersion: $project.ext.gitVersion"

task printGitVersion {
    doLast {
        println "gitVersion: $project.ext.gitVersion"
    }
}

printGitVersion.shouldRunAfter tag
```

Output:

```bash
$ ./gradlew tag printGitVersion

> Configure project :
Version: 1.0-SNAPSHOT
gitVersion: { version: "1.0-SNAPSHOT", tag: null }

> Task :tag
Created tag v1.0.0

Commands:

    git tag --delete v1.0.0    # delete
    git push origin  v1.0.0    # push to origin

New version: 1.0.0

> Task :printGitVersion
gitVersion: { version: "1.0.0", tag: "v1.0.0" }

BUILD SUCCESSFUL in 0s
2 actionable tasks: 2 executed
```
