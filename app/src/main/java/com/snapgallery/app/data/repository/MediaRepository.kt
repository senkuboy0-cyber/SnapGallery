package com.snapgallery.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.snapgallery.app.data.model.Album
import com.snapgallery.app.data.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(private val context: Context) {

    suspend fun getAllPhotos(): List<MediaItem> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<MediaItem>()
        
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val name = cursor.getString(nameColumn) ?: "Unknown"
                val dateModified = cursor.getLong(dateColumn)
                val size = cursor.getLong(sizeColumn)
                val mimeType = cursor.getString(mimeColumn) ?: "image/*"
                val bucketId = cursor.getString(bucketIdColumn) ?: ""
                val bucketName = cursor.getString(bucketNameColumn) ?: "Unknown"

                photos.add(
                    MediaItem(
                        id = id,
                        uri = uri,
                        name = name,
                        dateModified = dateModified,
                        size = size,
                        mimeType = mimeType,
                        bucketId = bucketId,
                        bucketName = bucketName
                    )
                )
            }
        }
        photos
    }

    suspend fun getAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val photos = getAllPhotos()
        photos.groupBy { it.bucketId }
            .map { (bucketId, items) ->
                Album(
                    id = bucketId,
                    name = items.first().bucketName,
                    coverUri = items.firstOrNull()?.uri,
                    count = items.size
                )
            }
            .sortedByDescending { it.count }
    }

    suspend fun getPhotosByAlbum(bucketId: String): List<MediaItem> = withContext(Dispatchers.IO) {
        getAllPhotos().filter { it.bucketId == bucketId }
    }
}
