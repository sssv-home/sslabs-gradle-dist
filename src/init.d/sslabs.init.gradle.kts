apply<SSLabsPlugin>()

@Suppress("UnstableApiUsage")
class SSLabsPlugin : Plugin<Gradle> {

    override fun apply(target: Gradle) {
        target.beforeSettings {
            configurePluginManagement()
            configureVersionCatalog()
        }

        target.beforeProject {
            configureRepositories()
        }

        target.afterProject {
            configurePublishing()
        }
    }

    private fun Settings.configurePluginManagement() {
        pluginManagement {
            repositories {
                spaceSnapshot()
                spaceRelease()
                gradlePluginPortal()
            }
        }
    }

    private fun Settings.configureVersionCatalog() {
        dependencyResolutionManagement {
            repositories {
                spaceSnapshot()
                spaceRelease()
                mavenCentral()
            }
        }
    }

    private fun Project.configureRepositories() {
        repositories {
            spaceSnapshot()
            spaceRelease()
            mavenCentral()
        }
    }

    private fun Project.configurePublishing() {
        pluginManager.withPlugin("maven-publish") {
            val publishing = the<PublishingExtension>()
            publishing.repositories {
                spacePublish(version.toString())
            }
        }
    }

    private fun RepositoryHandler.spaceSnapshot(name: String = "Space Snapshot") {
        space(name, SNAPSHOT_URL, SNAPSHOT_USERNAME, SNAPSHOT_PASSWORD)
    }

    private fun RepositoryHandler.spaceRelease(name: String = "Space Release") {
        space(name, RELEASE_URL, RELEASE_USERNAME, RELEASE_PASSWORD)
    }

    private fun RepositoryHandler.spacePublish(version: String) {
        when (version.endsWith("SNAPSHOT")) {
            true -> spaceSnapshot(name = "Space")
            false -> spaceRelease(name = "Space")
        }
    }

    private fun RepositoryHandler.space(name: String, url: String, username: String, password: String) {
        maven(url) {
            setName(name)
            credentials {
                this.username = username
                this.password = password
            }
        }
    }

    companion object {

        private val ENV: Map<String, String> = System.getenv()
        private val PROPS: Map<Any, Any> = System.getProperties()

        private const val RELEASE_URL = "https://maven.pkg.jetbrains.space/sslabs/p/libraries/maven-release"
        private val RELEASE_USERNAME = ENV["JB_SPACE_MAVEN_RELEASE_USERNAME"] ?: throw GradleException("Cannot initialize Gradle - 'JB_SPACE_MAVEN_RELEASE_USERNAME' missing")
        private val RELEASE_PASSWORD = ENV["JB_SPACE_MAVEN_RELEASE_PASSWORD"] ?: throw GradleException("Cannot initialize Gradle - 'JB_SPACE_MAVEN_RELEASE_PASSWORD' missing")

        private const val SNAPSHOT_URL = "https://maven.pkg.jetbrains.space/sslabs/p/libraries/maven-snapshot"
        private val SNAPSHOT_USERNAME = ENV["JB_SPACE_MAVEN_SNAPSHOT_USERNAME"] ?: throw GradleException("Cannot initialize Gradle - 'JB_SPACE_MAVEN_SNAPSHOT_USERNAME' missing")
        private val SNAPSHOT_PASSWORD = ENV["JB_SPACE_MAVEN_SNAPSHOT_PASSWORD"] ?: throw GradleException("Cannot initialize Gradle - 'JB_SPACE_MAVEN_SNAPSHOT_PASSWORD' missing")
    }
}
