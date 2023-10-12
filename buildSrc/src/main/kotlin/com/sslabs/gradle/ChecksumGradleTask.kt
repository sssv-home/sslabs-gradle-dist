package com.sslabs.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File
import java.io.OutputStream.nullOutputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

@CacheableTask
abstract class ChecksumGradleTask : DefaultTask() {

    init {
        description = "Creates the SHA-256 checksum for a Gradle distribution"
        group = "distribution"
    }

    /** The file whose checksum is calculated. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFile: Property<File>

    /** The destination where the sha256 checksum is written. */
    @get:OutputFile
    abstract val destinationFile: Property<File>

    @TaskAction
    fun execute() {
        val packageFile = sourceFile.get()
        val checksum = packageFile.sha256()

        val destination = destinationFile.get()
        destination.writeText(checksum)
    }

    private fun File.sha256(): String {
        // Digest the entire file.
        val md = createDigest()
        val input = DigestInputStream(inputStream().buffered(), md)
        nullOutputStream().use { input.copyTo(it) }
        // Format the SHA as a hex-string.
        return md.digest().joinToString("") {
            String.format("%02X", it).lowercase()
        }
    }

    private fun createDigest(): MessageDigest {
        return try {
            MessageDigest.getInstance("SHA-256")
        } catch (ex: NoSuchAlgorithmException) {
            throw IllegalStateException("SHA-256 algorithm not found", ex)
        }
    }
}
