package com.sslabs.gradle

import kotlinx.serialization.Serializable

@Serializable
@Suppress("PropertyName")
data class Release(
    /** The ID of the release. */
    val id: Long,
    /** The tag that identifies this release. */
    val tag_name: String,
    /** The timestamp when this release was created. */
    val created_at: String,
)
