package com.sslabs.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers.ofFile

@CacheableTask
abstract class DownloadGradleTask : DefaultTask() {

    init {
        description = "Downloads a Gradle distribution from Google's servers"
        group = "build"
    }

    @get:Input
    abstract val source: Property<String>

    /** The location where the Gradle distribution is downloaded to. */
    @get:OutputFile
    abstract val destinationFile: Property<File>

    @TaskAction
    fun download() {
        val downloadUrl = URI(source.get())
        val destination = destinationFile.get().toPath()

        val client = HttpClient.newBuilder()
            .followRedirects(Redirect.NORMAL)
            .build()

        val request = HttpRequest.newBuilder()
            .GET().uri(downloadUrl)
            .build()

        val response = client.send(request, ofFile(destination))
        response.assert2xx()
    }

    private fun <T : Any> HttpResponse<T>.assert2xx() {
        val status = statusCode()
        if (status < 200 || status >= 300) {
            throw GradleException("Cannot download Gradle distribution (${request().uri()}) - got a '$status' response")
        }
    }
}
