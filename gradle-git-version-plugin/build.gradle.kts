apply plugin: 'kotlin'
apply plugin: 'groovy'
apply plugin: 'maven-publish'
apply plugin: 'com.jfrog.bintray'
apply plugin: 'com.gradle.plugin-publish'

description = "Gradle Git Version Plugin"

println "$group:${project.name}:$version"

dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib"
    compile gradleApi()
    compile localGroovy()

    testCompile 'se.lovef:kotlin-assert-utils:0.5.0'
    testCompile 'com.nhaarman:mockito-kotlin-kt1.1:1.5.0'
    testCompile('org.spockframework:spock-core:1.1-groovy-2.4') {
        exclude group: 'org.codehaus.groovy', module: 'groovy-all'
    }
}

task setupPluginTest(dependsOn: publish) {
    doLast {
        rootProject.file('test-project/gradle.properties').text = "pluginVersion = $version"
    }
}

task testPlugin(type: GradleBuild, dependsOn: setupPluginTest, group: 'verification') {
    buildFile = rootProject.file('test-project/build.gradle')
    tasks = ['printGitVersion']
}

check.dependsOn testPlugin


def mavenDir = "build/repo"

def info = [
        "github": "https://github.com/lovef/gradle-git-version-plugin",
        "git"   : "https://github.com/lovef/gradle-git-version-plugin.git",
        "issues": "https://github.com/lovef/gradle-git-version-plugin/issues",
]

//noinspection GroovyAssignabilityCheck,GrUnresolvedAccess
task sourceJar(type: Jar) {
    classifier = 'sources'
    from sourceSets.main.allSource
}

sourceSets {
    javadoc { resources { srcDir 'src/main/javadoc' } }
}

/** Creates a javadoc jar with README to comply with Sonatypes requirements:
 * http://central.sonatype.org/pages/requirements.html */
//noinspection GroovyAssignabilityCheck,GrUnresolvedAccess
task javadocJar(type: Jar, dependsOn: javadoc) {
    classifier = 'javadoc'
    from sourceSets.javadoc.output
}

//noinspection GrUnresolvedAccess
publishing {
    publications {
        maven(MavenPublication) {
            from components.java
            artifact sourceJar
            artifact javadocJar
            pom.withXml {
                asNode().children().last() + {
                    resolveStrategy = Closure.DELEGATE_FIRST
                    name project.name
                    description description
                    url info.github
                    scm {
                        url info.github
                        connection "scm:git:${info.git}"
                        developerConnection "scm:git:${info.git}"
                    }
                    licenses {
                        license {
                            name 'Apache License Version 2.0'
                            url 'http://www.apache.org/licenses/LICENSE-2.0'
                            distribution 'repo'
                        }
                    }
                    developers {
                        developer {
                            id 'lovef'
                            name 'Love F'
                            email 'lovef.code@gmail.com'
                        }
                    }
                }
            }
        }
    }
    repositories {
        maven {
            url mavenDir
        }
    }
}

//noinspection GrUnresolvedAccess
bintray {
    user = project.properties.BINTRAY_API_USER ?: System.getenv('BINTRAY_API_USER')
    key = project.properties.BINTRAY_API_KEY ?: System.getenv('BINTRAY_API_KEY')
    publications = ['maven']
    //noinspection GrUnresolvedAccess
    pkg {
        repo = 'maven'
        name = project.name
        desc = project.description
        userOrg = user
        licenses = ['Apache-2.0']
        labels = ['gradle', 'gradle-plugin', 'git', 'version']
        websiteUrl = info.github
        vcsUrl = info.git
        issueTrackerUrl = info.issues
        //noinspection GroovyAssignabilityCheck
        publish = false
        publicDownloadNumbers = true

        //noinspection GroovyAssignabilityCheck
        version {
            name = project.version
            vcsTag = gitVersion.tag
            desc = project.description
            //noinspection GrUnresolvedAccess
            gpg {
                sign = true //Determines whether to GPG sign the files. The default is false
                passphrase = null //Optional. The passphrase for GPG signing'
            }
        }
    }
}

pluginBundle {
    website = info.github
    vcsUrl = info.github
    description = 'Creates version tags in git'
    tags = ['git', 'version']

    plugins {
        gitVersionPlugin {
            id = 'se.lovef.git-version'
            displayName = 'Git Version'
        }
    }
}
