package com.snapgallery.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.snapgallery.app.data.model.Album
import com.snapgallery.app.data.model.MediaItem
import com.snapgallery.app.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)

    private val _photos = MutableStateFlow<List<MediaItem>>(emptyList())
    val photos: StateFlow<List<MediaItem>> = _photos.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _albumPhotos = MutableStateFlow<List<MediaItem>>(emptyList())
    val albumPhotos: StateFlow<List<MediaItem>> = _albumPhotos.asStateFlow()

    fun loadPhotos() {
        viewModelScope.launch {
            _photos.value = repository.getAllPhotos()
        }
    }

    fun loadAlbums() {
        viewModelScope.launch {
            _albums.value = repository.getAlbums()
        }
    }

    fun loadPhotosByAlbum(bucketId: String) {
        viewModelScope.launch {
            _albumPhotos.value = repository.getPhotosByAlbum(bucketId)
        }
    }
}
