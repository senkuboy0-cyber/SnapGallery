package com.snapgallery.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory

class SnapGalleryApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .build()
    }
}
