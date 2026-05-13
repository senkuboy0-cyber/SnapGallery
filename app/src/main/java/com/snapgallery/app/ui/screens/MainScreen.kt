package com.snapgallery.app.ui.screens

import android.Manifest
import android.os.Build
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.snapgallery.app.data.model.MediaItem
import com.snapgallery.app.ui.theme.GradientBlue
import com.snapgallery.app.ui.theme.GradientCyan
import com.snapgallery.app.ui.theme.GradientPink
import com.snapgallery.app.ui.theme.GradientPurple
import com.snapgallery.app.ui.theme.PrimaryLight
import com.snapgallery.app.ui.viewmodel.GalleryViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    onNavigateToAlbums: () -> Unit,
    onPhotoClick: (Int) -> Unit,
    viewModel: GalleryViewModel = viewModel()
) {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionState = rememberPermissionState(permission)
    val photos by viewModel.photos.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedPhotos by remember { mutableStateOf(setOf<Long>()) }

    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            viewModel.loadPhotos()
        }
    }

    // Animate tab indicator
    val indicatorScale by animateFloatAsState(
        targetValue = if (selectedTab == 0) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "indicator"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradient Header Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                PrimaryLight.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Custom Top Bar
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Gradient Logo Icon
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(GradientPurple, GradientPink)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "SnapGallery",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        if (photos.isNotEmpty()) {
                            IconButton(onClick = { isSelectionMode = !isSelectionMode }) {
                                Icon(
                                    imageVector = Icons.Default.SelectAll,
                                    contentDescription = "Select",
                                    tint = if (isSelectionMode) MaterialTheme.colorScheme.primary 
                                           else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )

                // Selection counter
                AnimatedVisibility(
                    visible = isSelectionMode && selectedPhotos.isNotEmpty(),
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${selectedPhotos.size} selected",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Main Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        !permissionState.status.isGranted -> {
                            PermissionRequest(
                                onRequestPermission = { permissionState.launchPermissionRequest() }
                            )
                        }
                        photos.isEmpty() -> {
                            EmptyState()
                        }
                        else -> {
                            PhotoGrid(
                                photos = photos,
                                onPhotoClick = { index ->
                                    if (isSelectionMode) {
                                        selectedPhotos = selectedPhotos + photos[index].id
                                    } else {
                                        onPhotoClick(index)
                                    }
                                },
                                isSelectionMode = isSelectionMode,
                                selectedPhotos = selectedPhotos
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Glass Morphism Bottom Navigation
                GlassNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelect = { tab ->
                        selectedTab = tab
                        if (tab == 1) onNavigateToAlbums()
                    }
                )
            }
        }
    }
}

@Composable
private fun GlassNavigationBar(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItem(
                    icon = Icons.Default.Image,
                    label = "Photos",
                    isSelected = selectedTab == 0,
                    gradient = listOf(GradientPurple, GradientPink),
                    onClick = { onTabSelect(0) }
                )
                NavBarItem(
                    icon = Icons.Default.Collections,
                    label = "Albums",
                    isSelected = selectedTab == 1,
                    gradient = listOf(GradientBlue, GradientCyan),
                    onClick = { onTabSelect(1) }
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (isSelected) 48.dp else 40.dp)
                .scale(scale)
                .then(
                    if (isSelected) {
                        Modifier
                            .background(
                                brush = Brush.linearGradient(gradient),
                                shape = CircleShape
                            )
                    } else {
                        Modifier.background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(if (isSelected) 24.dp else 20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PermissionRequest(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Gradient Circle
        Box(
            modifier = Modifier
                .size(140.dp)
                .shadow(20.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientPurple, GradientPink)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(70.dp),
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Welcome to SnapGallery",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Grant access to view your photos",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Allow Access",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .shadow(16.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(70.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "No Photos Yet",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start capturing moments!\nYour photos will appear here.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PhotoGrid(
    photos: List<MediaItem>,
    onPhotoClick: (Int) -> Unit,
    isSelectionMode: Boolean,
    selectedPhotos: Set<Long>
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = 8.dp,
            bottom = 100.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(
            items = photos,
            key = { _, photo -> photo.id }
        ) { index, photo ->
            val isSelected = selectedPhotos.contains(photo.id)
            
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onPhotoClick(index) }
            ) {
                // Image with smooth loading
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photo.uri)
                        .crossfade(true)
                        .size(600)
                        .build(),
                    contentDescription = photo.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Selection Overlay
                AnimatedVisibility(
                    visible = isSelectionMode,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                else Color.Transparent
                            )
                    ) {
                        // Selection Checkmark
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
                    }
                }
            }
        }
    }
}
