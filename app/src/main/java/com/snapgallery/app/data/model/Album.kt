package com.snapgallery.app.data.model

data class Album(
    val id: String,
    val name: String,
    val coverUri: android.net.Uri?,
    val count: Int
)
