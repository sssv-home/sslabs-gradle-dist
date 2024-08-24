package com.sslabs.gradle

import org.gradle.api.GradleException
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect
import java.net.http.HttpResponse

internal fun makeClient(): HttpClient {
    return HttpClient.newBuilder()
        .followRedirects(Redirect.NORMAL)
        .build()
}

internal fun <T : Any> HttpResponse<T>.assert2xx(message: String) {
    val status = statusCode()
    if (status < 200 || status >= 300) {
        throw GradleException("$message (${request().uri()}) - got a '$status' response")
    }
}
