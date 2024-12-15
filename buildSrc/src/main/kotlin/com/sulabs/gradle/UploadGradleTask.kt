package com.sulabs.gradle

import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import java.io.File
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.nio.file.Path
import kotlin.io.path.name

@CacheableTask
abstract class UploadGradleTask : DefaultTask() {

    init {
        description = "Uploads the Gradle distribution to GitHub"
        group = "publishing"
    }

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val packageFile: Property<File>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val checksumFile: Property<File>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val releaseFile: Property<File>

    private val token: Provider<String> = project.providers.environmentVariable("GITHUB_GRADLE_DIST_TOKEN")
    private val tokenValue by lazy { token.orNull ?: throw GradleException("Cannot upload Gradle distribution - credentials not set (GITHUB_GRADLE_DIST_TOKEN)") }

    private val json = Json { ignoreUnknownKeys = true }

    @TaskAction
    fun execute() {
        val releaseFile = releaseFile.get()
        val release = json.decodeFromString<Release>(releaseFile.readText())

        uploadAsset(release, packageFile.get().toPath(), contentType = "application/zip")
        uploadAsset(release, checksumFile.get().toPath(), contentType = "text/plain")
    }

    private fun uploadAsset(release: Release, asset: Path, contentType: String) {
        val client = makeClient()

        val url = URI("$UPLOAD_URL/releases/${release.id}/assets?name=${asset.name}")
        val content = BodyPublishers.ofFile(asset)

        val request = HttpRequest.newBuilder()
            .PUT(content).uri(url)
            .header("Authorization", "Bearer $tokenValue")
            .header("Content-Type", contentType)
            .build()

        val discarding = HttpResponse.BodyHandlers.discarding()
        val response = client.send(request, discarding)
        response.assert2xx(message = "Cannot upload Gradle distribution")
    }

    companion object {
        private const val UPLOAD_URL = "https://uploads.github.com/repos/sssv-home/sulabs-gradle-dist"
    }
}
