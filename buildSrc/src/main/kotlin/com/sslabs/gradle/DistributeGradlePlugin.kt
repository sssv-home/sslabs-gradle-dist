package com.sslabs.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.distribution.plugins.DistributionPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.*
import org.gradle.kotlin.dsl.register
import java.net.URI

open class DistributeGradlePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.pluginManager.apply(DistributionPlugin::class.java)

        target.configureTasks()
    }

    private fun Project.configureTasks() {
        val downloaderBin = registerDownloadTask(BIN)
        val packagerBin = registerPackageTask(BIN, downloaderBin)
        val checksumBin = registerChecksumTask(BIN, packagerBin)
        val publishBin = registerPublishTask(BIN, packagerBin, checksumBin)

        val downloaderAll = registerDownloadTask(ALL)
        val packagerAll = registerPackageTask(ALL, downloaderAll)
        val checksumAll = registerChecksumTask(ALL, packagerAll)
        val publishAll = registerPublishTask(ALL, packagerAll, checksumAll)

        tasks.register<Task>("publish") {
            description = "Publishes the Gradle distributions to Space"
            group = "publishing"

            dependsOn(publishBin, publishAll)
        }
    }

    private fun Project.registerPublishTask(dist: DistributionType, packager: TaskProvider<Zip>, checksum: TaskProvider<ChecksumGradleTask>): TaskProvider<PublishGradleTask> {
        val distTitle = dist.toTitleCase()

        return tasks.register<PublishGradleTask>("publishGradle$distTitle") {
            val packagerDestination = packager.flatMap { it.archiveFile }.map { it.asFile }
            packageFile.set(packagerDestination)

            val checksumDestination = checksum.flatMap { it.destinationFile }
            checksumFile.set(checksumDestination)
        }
    }

    private fun Project.registerChecksumTask(dist: DistributionType, packager: TaskProvider<Zip>): TaskProvider<ChecksumGradleTask> {
        val distTitle = dist.toTitleCase()

        return tasks.register<ChecksumGradleTask>("checksumGradle$distTitle") {
            val packagerDestination = packager.flatMap { it.archiveFile }.map { it.asFile }
            sourceFile.set(packagerDestination)

            val packagerDestinationName = packager.flatMap { it.archiveFileName }
            val checksumDestination = packagerDestination.zip(packagerDestinationName) { f, n -> f.resolveSibling("$n.sha256") }
            destinationFile.set(checksumDestination)
        }
    }

    private fun Project.registerPackageTask(dist: DistributionType, downloader: TaskProvider<DownloadGradleTask>): TaskProvider<Zip> {
        val distTitle = dist.toTitleCase()
        val distString = dist.toLowerCase()

        return tasks.register<Zip>("packageGradle$distTitle") {
            val (gradleVersion, _) = versions

            description = "Packages a customized Gradle distribution"
            group = "distribution"

            archiveBaseName.set("sslabs-gradle")
            archiveVersion.set(versionString)
            archiveClassifier.set(distString)

            val downloaderDestination = downloader.flatMap { it.destinationFile }
            from(project.zipTree(downloaderDestination))

            into("gradle-$gradleVersion/init.d") {
                it.from(project.file("src/init.d"))
            }
        }
    }

    private fun Project.registerDownloadTask(dist: DistributionType): TaskProvider<DownloadGradleTask> {
        val distTitle = dist.toTitleCase()
        val distString = dist.toLowerCase()

        return tasks.register<DownloadGradleTask>("downloadGradle$distTitle") {
            val (gradleVersion, _) = versions
            val distFile = "gradle-$gradleVersion-$distString.zip"
            val downloadUrl = "https://services.gradle.org/distributions/$distFile"
            source.set(downloadUrl)

            val tempDir = project.layout.buildDirectory.asFile.map { it.resolve("tmp/gradle/$distFile") }
            destinationFile.set(tempDir)
        }
    }

    private val Project.versionString: String
        get() = version.toString()

    private val Project.versions: Versions
        get() {
            val (gradleVersion, buildVersion) = versionString.split("-")
            return Versions(gradleVersion, buildVersion)
        }

    private fun DistributionType.toLowerCase() = when (this) {
        BIN -> "bin"
        ALL -> "all"
    }

    private fun DistributionType.toTitleCase() = when (this) {
        BIN -> "Bin"
        ALL -> "All"
    }

    private data class Versions(
        val gradle: String,
        val build: String,
    )
}
