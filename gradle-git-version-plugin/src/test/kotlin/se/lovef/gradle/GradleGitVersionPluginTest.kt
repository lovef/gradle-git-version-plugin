package se.lovef.gradle

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test
import se.lovef.assert.v1.shouldBeFalse
import se.lovef.assert.v1.shouldBeNull
import se.lovef.assert.v1.shouldBeTrue
import se.lovef.assert.v1.shouldEqual

/**
 * Date: 2018-05-06
 * @author Love
 */
class GradleGitVersionPluginTest {

    private val project = ProjectBuilder.builder().build()

    private val gitVersion by lazy {
        project.extensions.getByType(GradleGitVersionPlugin.GitVersionExtension::class.java)
    }

    @Before fun before() {
        project.pluginManager.apply(GradleGitVersionPlugin::class.java)
    }

    @Test fun `default version`() {
        gitVersion.version shouldEqual "0.0-SNAPSHOT"
    }

    @Test fun `default tag`() {
        gitVersion.tag.shouldBeNull()
    }

    @Test fun `default useVersionCode`() {
        gitVersion.useVersionCode.shouldBeFalse()
    }

    @Test fun `setting base version updates the project version`() {
        gitVersion.baseVersion = "123.456"
        gitVersion.version shouldEqual project.version shouldEqual "123.456-SNAPSHOT"
    }

    @Test fun `base version can be set by invoking gitVersion`() {
        gitVersion(baseVersion = "123.456") shouldEqual "123.456-SNAPSHOT"
        gitVersion.version shouldEqual project.version shouldEqual "123.456-SNAPSHOT"
    }

    @Test fun `base version and version code can be set by invoking gitVersion`() {
        gitVersion(baseVersion = "123.456", useVersionCode = true) shouldEqual "123.456-SNAPSHOT"
        gitVersion.version shouldEqual project.version shouldEqual "123.456-SNAPSHOT"
        gitVersion.useVersionCode.shouldBeTrue()
    }
}
