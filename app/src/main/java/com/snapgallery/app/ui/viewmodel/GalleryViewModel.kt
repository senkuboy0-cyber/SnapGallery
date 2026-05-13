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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadPhotos() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _photos.value = repository.getAllPhotos()
            } catch (e: Exception) {
                _error.value = "Failed to load photos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAlbums() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _albums.value = repository.getAlbums()
            } catch (e: Exception) {
                _error.value = "Failed to load albums: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPhotosByAlbum(bucketId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _albumPhotos.value = repository.getPhotosByAlbum(bucketId)
            } catch (e: Exception) {
                _error.value = "Failed to load album photos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        loadPhotos()
        loadAlbums()
    }

    fun clearError() {
        _error.value = null
    }
}
