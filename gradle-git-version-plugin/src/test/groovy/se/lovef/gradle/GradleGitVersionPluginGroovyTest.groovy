package se.lovef.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Date: 2018-05-06
 * @author Love
 */
class GradleGitVersionPluginGroovyTest extends Specification {

    Project project
    GradleGitVersionPlugin.GitVersionExtension gitVersion

    def setup() {
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply 'se.lovef.git-version'
        gitVersion = project.extensions.getByName('gitVersion') as GradleGitVersionPlugin.GitVersionExtension
    }

    def "gitVersion implicit toString"() {
        expect:
        "$gitVersion" == gitVersion.toString()
        gitVersion.doCall() == gitVersion.toString()
    }

    def "invoke gitVersion to set base version"() {
        expect:
        gitVersion.doCall("123.456") == "123.456-SNAPSHOT"
        gitVersion("123.456") == "123.456-SNAPSHOT"
        gitVersion.version == "123.456-SNAPSHOT"
    }

    def "invoke gitVersion with properties"() {
        expect:
        gitVersion.doCall(baseVersion: "123.456", useVersionCode: true) == "123.456-SNAPSHOT"
        gitVersion(baseVersion: "123.456", useVersionCode: true) == "123.456-SNAPSHOT"
        gitVersion.version == "123.456-SNAPSHOT"
        gitVersion.useVersionCode
    }

    def "invoke gitVersion with empty properties does not change existing properties"() {
        when:
        gitVersion.baseVersion = "123.456"

        then:
        gitVersion([:]) == "123.456-SNAPSHOT"
    }
}
