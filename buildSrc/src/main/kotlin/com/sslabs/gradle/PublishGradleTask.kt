package com.sslabs.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.nio.file.Path
import kotlin.io.path.name

@CacheableTask
abstract class PublishGradleTask : DefaultTask() {

    init {
        description = "Publishes the Gradle distribution to Space"
        group = "publishing"
    }

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val packageFile: Property<File>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val checksumFile: Property<File>

    private val token: Provider<String> = project.providers.environmentVariable("JB_SPACE_GRADLE_PUBLISH_TOKEN")

    @TaskAction
    fun execute() {
        put(packageFile.get().toPath(), contentType = "application/zip")
        put(checksumFile.get().toPath(), contentType = "text/plain")
    }

    private fun put(content: Path, contentType: String) {
        val url = URI("$BASE_URL/${content.name}")
        val body = BodyPublishers.ofFile(content)
        val token = token.orNull
            ?: throw GradleException("Cannot upload Gradle distribution - credentials not set (JB_SPACE_GRADLE_PUBLISH_TOKEN)")

        val client = HttpClient.newBuilder()
            .followRedirects(Redirect.NORMAL)
            .build()

        val request = HttpRequest.newBuilder()
            .PUT(body).uri(url)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", contentType)
            .build()

        val discarding = HttpResponse.BodyHandlers.discarding()
        val response = client.send(request, discarding)
        response.assert2xx()
    }

    private fun <T : Any> HttpResponse<T>.assert2xx() {
        val status = statusCode()
        if (status < 200 || status >= 300) {
            throw GradleException("Cannot upload Gradle distribution (${request().uri()}) - got a '$status' response")
        }
    }

    companion object {
        private const val BASE_URL = "https://files.pkg.jetbrains.space/sslabs/p/gradle-wrapper/gradle-dist"
    }
}
