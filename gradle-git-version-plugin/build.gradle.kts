import com.jfrog.bintray.gradle.BintrayExtension
import groovy.lang.Closure
import org.gradle.api.tasks.GradleBuild
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import se.lovef.git.GitVersion
import se.lovef.gradle.GradleGitVersionPlugin
import java.util.*

plugins {
    groovy
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "1.2.41"
    id("com.jfrog.bintray") version "1.8.0"
    id("com.gradle.plugin-publish") version "0.9.10"
}

description = "Gradle Git Version Plugin"

println("$group:${project.name}:$version")

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib")
    compile(gradleApi())
    compile(localGroovy())

    testCompile("se.lovef:kotlin-assert-utils:0.5.0")
    testCompile("com.nhaarman:mockito-kotlin-kt1.1:1.5.0")
    testCompile("org.spockframework:spock-core:1.1-groovy-2.4") {
        exclude(group = "org.codehaus.groovy", module = "groovy-all")
    }
}

sourceSets {
    val javadoc by registering { resources { srcDir("src/main/javadoc") } }
}

tasks {
    val setupPluginTest by registering {
        dependsOn(publish)
        doLast {
            rootProject.file("test-project/gradle.properties").writeText("pluginVersion = $version")
        }
    }

    val testPlugin by registering(GradleBuild::class) {
        dependsOn(setupPluginTest)
        group = "verification"
        buildFile = rootProject.file("test-project/build.gradle")
        tasks = listOf("printGitVersion")
    }

    check { dependsOn(testPlugin) }
}

val sourceJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

/** Creates a javadoc jar with README to comply with Sonatypes requirements:
 * http://central.sonatype.org/pages/requirements.html */
val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.javadoc)
    classifier = "javadoc"
    from(sourceSets["javadoc"].output)
}

val publication = "maven"
val mavenDir = "build/repo"

object Info {
    const val github = "https://github.com/lovef/gradle-git-version-plugin"
    const val git = "https://github.com/lovef/gradle-git-version-plugin.git"
    const val issues = "https://github.com/lovef/gradle-git-version-plugin/issues"
}

publishing {
    publications {
        create<MavenPublication>(publication) {
            from(components["java"])
            artifact(sourceJar.get())
            artifact(javadocJar.get())

            @Suppress("UnstableApiUsage") pom {
                name.set(project.name)
                description.set(project.description)
                url.set(Info.github)
                scm {
                    url.set(Info.github)
                    connection.set("scm:git:${Info.git}")
                    developerConnection.set("scm:git:${Info.git}")
                }
                licenses {
                    license {
                        name.set("Apache License Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("lovef")
                        name.set("Love F")
                        email.set("lovef.code@gmail.com")
                    }
                }
            }
        }
        repositories {
            maven {
                url = uri(mavenDir)
            }
        }
    }
}

bintray {
    user = project.properties["BINTRAY_API_USER"]?.toString() ?: System.getenv("BINTRAY_API_USER")
    key = project.properties["BINTRAY_API_KEY"]?.toString() ?: System.getenv("BINTRAY_API_KEY")
    setPublications(publication)

    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = project.name
        desc = project.description
        userOrg = user
        setLicenses("Apache-2.0")
        setLabels("gradle", "gradle-plugin", "git", "version")
        websiteUrl = Info.github
        vcsUrl = Info.git
        issueTrackerUrl = Info.issues
        publish = false
        publicDownloadNumbers = true

        version(delegateClosureOf<BintrayExtension.VersionConfig> {
            name = project.version.toString()
            vcsTag = extra["tag"]?.toString()
            desc = project.description
            gpg(delegateClosureOf<BintrayExtension.GpgConfig> {
                sign = true //Determines whether to GPG sign the files. The default is false
                passphrase = null //Optional. The passphrase for GPG signing"
            })
        })
    })
}

pluginBundle {
    website = Info.github
    vcsUrl = Info.github
    description = "Creates version tags in git"
    tags = listOf("git", "version")

    (plugins) {
        "gitVersionPlugin" {
            id = "se.lovef.git-version"
            displayName = "Git Version"
        }
    }
}
