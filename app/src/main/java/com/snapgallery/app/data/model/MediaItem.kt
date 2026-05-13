package com.snapgallery.app.data.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val dateModified: Long,
    val size: Long,
    val mimeType: String,
    val bucketId: String,
    val bucketName: String
)
