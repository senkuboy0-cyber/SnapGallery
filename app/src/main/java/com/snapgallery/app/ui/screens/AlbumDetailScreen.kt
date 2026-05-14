package com.snapgallery.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.snapgallery.app.data.model.MediaItem
import com.snapgallery.app.ui.theme.GradientBlue
import com.snapgallery.app.ui.theme.GradientCyan
import com.snapgallery.app.ui.theme.GradientPink
import com.snapgallery.app.ui.theme.GradientPurple
import com.snapgallery.app.ui.theme.PrimaryLight
import com.snapgallery.app.ui.viewmodel.GalleryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumId: String,
    albumName: String,
    onPhotoClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    viewModel: GalleryViewModel = viewModel()
) {
    val albumPhotos by viewModel.albumPhotos.collectAsState()
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedPhotos by remember { mutableStateOf(setOf<Long>()) }
    var viewMode by remember { mutableStateOf(0) } // 0 = grid, 1 = list

    LaunchedEffect(albumId) {
        viewModel.loadPhotosByAlbum(albumId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Animated Header Background
            AnimatedAlbumHeader(albumPhotos = albumPhotos)

            // Glass Top App Bar
            AlbumDetailTopBar(
                albumName = albumName,
                photoCount = albumPhotos.size,
                onBackClick = onBackClick,
                isSelectionMode = isSelectionMode,
                viewMode = viewMode,
                onViewModeToggle = { viewMode = if (viewMode == 0) 1 else 0 },
                onSelectionModeToggle = { isSelectionMode = !isSelectionMode }
            )

            // Selection Counter
            AnimatedVisibility(
                visible = isSelectionMode && selectedPhotos.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                AlbumSelectionCounter(
                    selectedCount = selectedPhotos.size,
                    onClear = {
                        isSelectionMode = false
                        selectedPhotos = emptySet()
                    }
                )
            }

            // Content
            Box(modifier = Modifier.weight(1f)) {
                if (albumPhotos.isEmpty()) {
                    EmptyAlbumDetailState()
                } else {
                    if (viewMode == 0) {
                        AlbumGridView(
                            photos = albumPhotos,
                            onPhotoClick = { index ->
                                if (isSelectionMode) {
                                    val photoId = albumPhotos[index].id
                                    selectedPhotos = if (selectedPhotos.contains(photoId)) {
                                        selectedPhotos - photoId
                                    } else {
                                        selectedPhotos + photoId
                                    }
                                } else {
                                    onPhotoClick(index)
                                }
                            },
                            isSelectionMode = isSelectionMode,
                            selectedPhotos = selectedPhotos
                        )
                    } else {
                        AlbumListView(
                            photos = albumPhotos,
                            onPhotoClick = { index ->
                                if (!isSelectionMode) {
                                    onPhotoClick(index)
                                }
                            },
                            isSelectionMode = isSelectionMode,
                            selectedPhotos = selectedPhotos,
                            onPhotoSelect = { photoId ->
                                selectedPhotos = if (selectedPhotos.contains(photoId)) {
                                    selectedPhotos - photoId
                                } else {
                                    selectedPhotos + photoId
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedAlbumHeader(albumPhotos: List<MediaItem>) {
    val firstPhoto = albumPhotos.firstOrNull()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        if (firstPhoto != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(firstPhoto.uri)
                    .crossfade(true)
                    .size(600)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                contentScale = ContentScale.Crop
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PrimaryLight.copy(alpha = 0.2f),
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                        )
                    )
                )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumDetailTopBar(
    albumName: String,
    photoCount: Int,
    onBackClick: () -> Unit,
    isSelectionMode: Boolean,
    viewMode: Int,
    onViewModeToggle: () -> Unit,
    onSelectionModeToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = albumName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (photoCount > 0) {
                        Text(
                            text = "$photoCount photos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            },
            navigationIcon = {
                GlassBackButton(onClick = onBackClick)
            },
            actions = {
                if (photoCount > 0) {
                    // View Mode Toggle
                    IconButton(onClick = onViewModeToggle) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = if (viewMode == 0) 
                                MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (viewMode == 0) Icons.Default.List else Icons.Default.GridView,
                                    contentDescription = "Toggle View",
                                    tint = if (viewMode == 0) 
                                        MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Selection Mode Toggle
                    IconButton(onClick = onSelectionModeToggle) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = if (isSelectionMode) 
                                MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isSelectionMode) Icons.Default.Check else Icons.Default.Photo,
                                    contentDescription = "Select",
                                    tint = if (isSelectionMode) 
                                        Color.White
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun GlassBackButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AlbumSelectionCounter(
    selectedCount: Int,
    onClear: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$selectedCount",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (selectedCount == 1) "photo selected" else "photos selected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onClear),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = "Clear",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun AlbumGridView(
    photos: List<MediaItem>,
    onPhotoClick: (Int) -> Unit,
    isSelectionMode: Boolean,
    selectedPhotos: Set<Long>
) {
    val gridState = rememberLazyGridState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = gridState,
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = 8.dp,
            bottom = 24.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = photos,
            key = { it.id }
        ) { photo ->
            val isSelected = selectedPhotos.contains(photo.id)
            
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { 
                        val index = photos.indexOf(photo)
                        onPhotoClick(index)
                    }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photo.uri)
                        .crossfade(true)
                        .crossfade(500)
                        .size(600)
                        .build(),
                    contentDescription = photo.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                
                AnimatedVisibility(
                    visible = isSelectionMode,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                else Color.Black.copy(alpha = 0.2f)
                            )
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        
                        if (!isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.5f))
                                    .clickable { 
                                        val index = photos.indexOf(photo)
                                        onPhotoClick(index)
                                    },
                                contentAlignment = Alignment.Center
                            ) {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumListView(
    photos: List<MediaItem>,
    onPhotoClick: (Int) -> Unit,
    isSelectionMode: Boolean,
    selectedPhotos: Set<Long>,
    onPhotoSelect: (Long) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = photos,
            key = { it.id }
        ) { photo ->
            val isSelected = selectedPhotos.contains(photo.id)
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        val index = photos.indexOf(photo)
                        onPhotoClick(index)
                    },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thumbnail
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(photo.uri)
                                .crossfade(true)
                                .size(400)
                                .build(),
                            contentDescription = photo.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        if (isSelectionMode) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        else Color.Transparent
                                    )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = photo.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatFileSize(photo.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    
                    // Selection Indicator
                    if (isSelectionMode) {
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { onPhotoSelect(photo.id) },
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAlbumDetailState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .shadow(16.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Album Empty",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No photos in this album yet.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "%.1f KB".format(size / 1024.0)
        else -> "%.1f MB".format(size / (1024.0 * 1024.0))
    }
}
