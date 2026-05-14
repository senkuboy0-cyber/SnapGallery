package com.snapgallery.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.snapgallery.app.data.model.MediaItem
import com.snapgallery.app.ui.theme.GradientPink
import com.snapgallery.app.ui.theme.GradientPurple
import com.snapgallery.app.ui.viewmodel.GalleryViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerScreen(
    initialIndex: Int,
    onBackClick: () -> Unit,
    viewModel: GalleryViewModel
) {
    val photos by viewModel.photos.collectAsState()
    val context = LocalContext.current
    
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (photos.size - 1).coerceAtLeast(0)),
        pageCount = { photos.size }
    )
    
    var showMoreMenu by remember { mutableStateOf(false) }
    var currentPhoto by remember { mutableStateOf<MediaItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showInfoSheet by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var isZoomed by remember { mutableStateOf(false) }
    
    val controlsAlpha by animateFloatAsState(
        targetValue = if (showControls) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "controlsAlpha"
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                if (page < photos.size) {
                    currentPhoto = photos[page]
                    isZoomed = false
                }
            }
    }
    
    LaunchedEffect(photos) {
        if (photos.isNotEmpty() && initialIndex < photos.size) {
            currentPhoto = photos[initialIndex]
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { 
                        if (!isZoomed) {
                            showControls = !showControls 
                        }
                    },
                    onDoubleTap = {
                        isZoomed = !isZoomed
                    }
                )
            }
    ) {
        // Photo Pager with Smooth Transitions
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            if (page < photos.size) {
                ZoomableImage(
                    photoUri = photos[page].uri,
                    isZoomed = isZoomed,
                    onZoomChange = { isZoomed = it }
                )
            }
        }

        // Top Gradient Bar with Glass Effect
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            GlassTopControls(
                currentPage = pagerState.currentPage,
                totalPages = photos.size,
                onBackClick = onBackClick,
                onMoreClick = { showMoreMenu = true },
                showMoreMenu = showMoreMenu,
                onDismissMenu = { showMoreMenu = false },
                currentPhoto = currentPhoto,
                onShareClick = { currentPhoto?.let { shareImage(context, it.uri) } },
                onInfoClick = { showInfoSheet = true },
                onSetWallpaperClick = { currentPhoto?.let { setAsWallpaper(context, it.uri) } },
                onDeleteClick = { showDeleteDialog = true },
                onDownloadClick = { currentPhoto?.let { downloadImage(context, it) } }
            )
        }

        // Bottom Quick Actions with Glass Effect
        AnimatedVisibility(
            visible = showControls && !isZoomed,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            GlassBottomControls(
                onShareClick = { currentPhoto?.let { shareImage(context, it.uri) } },
                onInfoClick = { showInfoSheet = true },
                onWallpaperClick = { currentPhoto?.let { setAsWallpaper(context, it.uri) } },
                onDeleteClick = { showDeleteDialog = true },
                onZoomInClick = { isZoomed = true }
            )
        }

        // Zoom Indicator
        AnimatedVisibility(
            visible = isZoomed,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 100.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2x Zoom",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            GlassDeleteDialog(
                onConfirm = {
                    showDeleteDialog = false
                    currentPhoto?.let { photo ->
                        deletePhoto(context, photo.uri)
                        viewModel.loadPhotos()
                        onBackClick()
                    }
                },
                onDismiss = { showDeleteDialog = false }
            )
        }

        // Info Bottom Sheet
        if (showInfoSheet && currentPhoto != null) {
            InfoBottomSheet(
                photo = currentPhoto!!,
                onDismiss = { showInfoSheet = false }
            )
        }
    }
}

@Composable
private fun GlassTopControls(
    currentPage: Int,
    totalPages: Int,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    showMoreMenu: Boolean,
    onDismissMenu: () -> Unit,
    currentPhoto: MediaItem?,
    onShareClick: () -> Unit,
    onInfoClick: () -> Unit,
    onSetWallpaperClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.8f),
                        Color.Transparent
                    )
                )
            )
            .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button with Glass Effect
            GlassIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                onClick = onBackClick
            )

            // Page indicator with Glass Effect
            GlassPageIndicator(
                current = currentPage + 1,
                total = totalPages
            )

            // More Menu Button
            Box {
                GlassIconButton(
                    icon = Icons.Default.MoreVert,
                    onClick = onMoreClick
                )
                
                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = onDismissMenu,
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                ) {
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Share", fontWeight = FontWeight.Medium)
                            }
                        },
                        onClick = {
                            onDismissMenu()
                            onShareClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Download", fontWeight = FontWeight.Medium)
                            }
                        },
                        onClick = {
                            onDismissMenu()
                            onDownloadClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Details", fontWeight = FontWeight.Medium)
                            }
                        },
                        onClick = {
                            onDismissMenu()
                            onInfoClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Wallpaper, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Set as Wallpaper", fontWeight = FontWeight.Medium)
                            }
                        },
                        onClick = {
                            onDismissMenu()
                            onSetWallpaperClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                            }
                        },
                        onClick = {
                            onDismissMenu()
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.15f),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun GlassPageIndicator(
    current: Int,
    total: Int
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.15f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gradient dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientPurple, GradientPink)
                        )
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$current / $total",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun GlassBottomControls(
    onShareClick: () -> Unit,
    onInfoClick: () -> Unit,
    onWallpaperClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onZoomInClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.7f)
                    )
                )
            )
            .padding(bottom = 48.dp, start = 16.dp, end = 16.dp, top = 48.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickAction(
                icon = Icons.Default.Share,
                label = "Share",
                onClick = onShareClick
            )
            QuickAction(
                icon = Icons.Default.Info,
                label = "Info",
                onClick = onInfoClick
            )
            QuickAction(
                icon = Icons.Default.ZoomIn,
                label = "Zoom",
                onClick = onZoomInClick
            )
            QuickAction(
                icon = Icons.Default.Wallpaper,
                label = "Wallpaper",
                onClick = onWallpaperClick
            )
            QuickAction(
                icon = Icons.Default.Delete,
                label = "Delete",
                onClick = onDeleteClick,
                isDestructive = true
            )
        }
    }
}

@Composable
private fun QuickAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isDestructive) 
                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                else Color.White.copy(alpha = 0.15f),
            modifier = Modifier.size(52.dp),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isDestructive) MaterialTheme.colorScheme.error else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isDestructive) MaterialTheme.colorScheme.error else Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ZoomableImage(
    photoUri: Uri,
    isZoomed: Boolean,
    onZoomChange: (Boolean) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 5f)
                    if (scale > 1f) {
                        offsetX += pan.x
                        offsetY += pan.y
                        onZoomChange(true)
                    } else {
                        offsetX = 0f
                        offsetY = 0f
                        onZoomChange(false)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoUri)
                .crossfade(true)
                .size(1080)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = animatedScale,
                    scaleY = animatedScale,
                    translationX = offsetX,
                    translationY = offsetY
                ),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun GlassDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp).size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "Delete Photo?",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Text(
                "This action cannot be undone. The photo will be permanently deleted.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Delete", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable
private fun InfoBottomSheet(
    photo: MediaItem,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Photo Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Preview Image with rounded corners
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photo.uri)
                        .crossfade(true)
                        .size(800)
                        .build(),
                    contentDescription = photo.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Info Items with Glass effect cards
                InfoCard(label = "Name", value = photo.name)
                Spacer(modifier = Modifier.height(12.dp))
                InfoCard(label = "Type", value = photo.mimeType)
                Spacer(modifier = Modifier.height(12.dp))
                InfoCard(label = "Size", value = formatFileSize(photo.size))
                Spacer(modifier = Modifier.height(12.dp))
                InfoCard(label = "Date", value = formatDate(photo.dateModified))
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Close",
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard(label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun shareImage(context: Context, uri: Uri) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share via SnapGallery"))
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to share", Toast.LENGTH_SHORT).show()
    }
}

private fun setAsWallpaper(context: Context, uri: Uri) {
    try {
        val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
            setDataAndType(uri, "image/*")
            putExtra("finish", true)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Set as"))
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to set wallpaper", Toast.LENGTH_SHORT).show()
    }
}

private fun deletePhoto(context: Context, uri: Uri) {
    try {
        val deleted = context.contentResolver.delete(uri, null, null)
        if (deleted > 0) {
            Toast.makeText(context, "Photo deleted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to delete photo", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun downloadImage(context: Context, photo: MediaItem) {
    try {
        val inputStream = context.contentResolver.openInputStream(photo.uri)
        val fileName = "SnapGallery_${System.currentTimeMillis()}.jpg"
        val file = File(context.getExternalFilesDir(null), fileName)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        Toast.makeText(context, "Downloaded to ${file.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "%.1f KB".format(size / 1024.0)
        else -> "%.1f MB".format(size / (1024.0 * 1024.0))
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp * 1000))
}
