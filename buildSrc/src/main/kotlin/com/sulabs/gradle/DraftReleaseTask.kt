package com.sulabs.gradle

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse

@CacheableTask
abstract class DraftReleaseTask : DefaultTask() {

    init {
        description = "Creates a draft GitHub release for the Gradle distribution"
        group = "publishing"
    }

    @get:Input
    abstract val version: Property<String>

    /** The destination where the release information is written. */
    @get:OutputFile
    abstract val releaseFile: Property<File>

    private val token: Provider<String> = project.providers.environmentVariable("GITHUB_GRADLE_DIST_TOKEN")
    private val tokenValue by lazy { token.orNull ?: throw GradleException("Cannot create release - credentials not set (GITHUB_GRADLE_DIST_TOKEN)") }

    @TaskAction
    fun execute() {
        val client = makeClient()

        val url = URI("$API_URL/releases")
        val body = makeBody()

        val request = HttpRequest.newBuilder()
            .POST(body).uri(url)
            .header("Authorization", "Bearer $tokenValue")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        response.assert2xx(message = "Cannot create release")

        val releaseFile = releaseFile.get()
        releaseFile.writeText(response.body())
    }

    private fun makeBody(): HttpRequest.BodyPublisher {
        val version = version.get()
        val release = CreateRelease(
            tag_name = "v$version",
            name = "Gradle - v$version",
            draft = true,
        )

        return BodyPublishers.ofString(Json.encodeToString(release))
    }

    @Serializable
    @Suppress("PropertyName")
    private data class CreateRelease(
        val tag_name: String,
        val name: String,
        val draft: Boolean,
    )

    companion object {
        private const val API_URL = "https://api.github.com/repos/sssv-home/sulabs-gradle-dist"
    }
}
