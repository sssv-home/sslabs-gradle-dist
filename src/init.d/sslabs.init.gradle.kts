
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
                github()
                gradlePluginPortal()
            }
        }
    }

    private fun Settings.configureVersionCatalog() {
        dependencyResolutionManagement {
            repositories {
                github()
                mavenCentral()
            }
        }
    }

    private fun Project.configureRepositories() {
        repositories {
            github()
            mavenCentral()
        }
    }

    private fun Project.configurePublishing() {
        pluginManager.withPlugin("maven-publish") {
            val publishing = the<PublishingExtension>()
            publishing.repositories {
                github()
            }
        }
    }

    private fun RepositoryHandler.github() {
        maven(PACKAGES_URL) {
            name = "GitHub"
            credentials {
                this.username = PACKAGES_USERNAME
                this.password = PACKAGES_PASSWORD
            }
        }
    }

    companion object {

        private val ENV: Map<String, String> = System.getenv()
        private val PROPS: Map<Any, Any> = System.getProperties()

        private const val PACKAGES_URL = "https://maven.pkg.github.com/sssv-home/packages"
        private val PACKAGES_USERNAME = ENV["GITHUB_PACKAGES_USERNAME"] ?: throw GradleException("Cannot initialize Gradle - 'GITHUB_PACKAGES_USERNAME' missing")
        private val PACKAGES_PASSWORD = ENV["GITHUB_PACKAGES_PASSWORD"] ?: throw GradleException("Cannot initialize Gradle - 'GITHUB_PACKAGES_PASSWORD' missing")
    }
}
