package de.triplet.gradle.play

import org.gradle.api.Project
import org.gradle.api.internal.plugins.PluginApplicationException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static de.triplet.gradle.play.DependsOn.dependsOn
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertThat

class PlayPublisherPluginTest {

    @Test(expected = PluginApplicationException.class)
    public void testThrowsOnLibraryProjects() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'com.android.library'
        project.apply plugin: 'com.github.triplet.play'
    }

    @Test
    public void testCreatesDefaultTask() {
        Project project = TestHelper.evaluatableProject()
        project.evaluate()

        assertNotNull(project.tasks.publishRelease)
        assertEquals(project.tasks.publishApkRelease.variant, project.android.applicationVariants[1])
    }

    @Test
    public void testCreatesFlavorTasks() {
        Project project = TestHelper.evaluatableProject()

        project.android.productFlavors {
            free
            paid
        }

        project.evaluate()

        assertNotNull(project.tasks.publishPaidRelease)
        assertNotNull(project.tasks.publishFreeRelease)

        assertEquals(project.tasks.publishApkFreeRelease.variant, project.android.applicationVariants[3])
        assertEquals(project.tasks.publishApkPaidRelease.variant, project.android.applicationVariants[1])
    }

    @Test
    public void testDefaultTrack() {
        Project project = TestHelper.evaluatableProject()
        project.evaluate()

        assertEquals('alpha', project.extensions.findByName("play").track)
    }

    @Test
    public void testTrack() {
        Project project = TestHelper.evaluatableProject()

        project.play {
            track 'production'
        }

        project.evaluate()

        assertEquals('production', project.extensions.findByName("play").track)
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsOnInvalidTrack() {
        Project project = TestHelper.evaluatableProject()

        project.play {
            track 'gamma'
        }
    }

    @Test
    public void testPublishListingTask() {
        Project project = TestHelper.evaluatableProject()

        project.android.productFlavors {
            free
            paid
        }

        project.evaluate()

        assertNotNull(project.tasks.publishListingFreeRelease)
        assertNotNull(project.tasks.publishListingPaidRelease)
    }


    @Test
    public void testSplits() {
        Project project = TestHelper.evaluatableProject()

        project.android {
            splits {
                abi {
                    enable true
                    reset()
                    include 'x86', 'armeabi-v7a', 'mips'
                }
            }
        }

        project.evaluate()

        assertThat(project.tasks.publishApkRelease, dependsOn('assembleX86Release'))
        assertThat(project.tasks.publishApkRelease, dependsOn('assembleArmeabi-v7aRelease'))
        assertThat(project.tasks.publishApkRelease, dependsOn('assembleMipsRelease'))
    }
}
