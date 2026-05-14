package com.snapgallery.app.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            viewModel.loadPhotos()
        }
    }

    val filteredPhotos = if (searchQuery.isNotEmpty()) {
        photos.filter { it.name.contains(searchQuery, ignoreCase = true) }
    } else photos

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Animated Gradient Background
            AnimatedGradientBackground()

            Column(modifier = Modifier.fillMaxSize()) {
                // Custom Top Bar with Glassmorphism
                GlassTopBar(
                    showSearch = showSearch,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchToggle = { showSearch = !showSearch },
                    isSelectionMode = isSelectionMode,
                    onSelectionModeToggle = { isSelectionMode = !isSelectionMode },
                    photos = photos
                )

                // Tab Indicator
                AnimatedTabIndicator(
                    selectedTab = selectedTab,
                    onTabSelect = { tab ->
                        selectedTab = tab
                        if (tab == 1) onNavigateToAlbums()
                    }
                )

                // Selection Counter Bar
                AnimatedVisibility(
                    visible = isSelectionMode && selectedPhotos.isNotEmpty(),
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                ) {
                    SelectionCounterBar(
                        selectedCount = selectedPhotos.size,
                        onClear = {
                            isSelectionMode = false
                            selectedPhotos = emptySet()
                        }
                    )
                }

                // Main Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        !permissionState.status.isGranted -> {
                            WelcomePermissionScreen(
                                onRequestPermission = { permissionState.launchPermissionRequest() }
                            )
                        }
                        filteredPhotos.isEmpty() && photos.isNotEmpty() -> {
                            NoSearchResultsScreen(query = searchQuery)
                        }
                        filteredPhotos.isEmpty() -> {
                            EmptyGalleryScreen()
                        }
                        else -> {
                            EnhancedPhotoGrid(
                                photos = filteredPhotos,
                                onPhotoClick = { index ->
                                    if (isSelectionMode) {
                                        val photoId = filteredPhotos[index].id
                                        selectedPhotos = if (selectedPhotos.contains(photoId)) {
                                            selectedPhotos - photoId
                                        } else {
                                            selectedPhotos + photoId
                                        }
                                    } else {
                                        val actualIndex = photos.indexOf(filteredPhotos[index])
                                        onPhotoClick(actualIndex.coerceAtLeast(0))
                                    }
                                },
                                isSelectionMode = isSelectionMode,
                                selectedPhotos = selectedPhotos
                            )
                        }
                    }
                }

                // Bottom Navigation with Glass Effect
                GlassBottomNav(
                    selectedTab = selectedTab,
                    onTabSelect = { tab ->
                        selectedTab = tab
                        if (tab == 1) onNavigateToAlbums()
                    },
                    hasPhotos = photos.isNotEmpty()
                )
            }
        }
    }
}

@Composable
private fun AnimatedGradientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000)
        ),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GradientPurple.copy(alpha = 0.1f),
                        GradientBlue.copy(alpha = 0.05f),
                        Color.Transparent,
                        GradientPink.copy(alpha = 0.05f)
                    )
                )
            )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassTopBar(
    showSearch: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    isSelectionMode: Boolean,
    onSelectionModeToggle: () -> Unit,
    photos: List<MediaItem>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        tonalElevation = 4.dp
    ) {
        Column {
            TopAppBar(
                title = {
                    if (showSearch) {
                        SearchField(
                            query = searchQuery,
                            onQueryChange = onSearchQueryChange
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GradientIcon()
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "SnapGallery",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    if (!showSearch && photos.isNotEmpty()) {
                        IconButton(onClick = onSearchToggle) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onSelectionModeToggle) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = "Select",
                                tint = if (isSelectionMode) MaterialTheme.colorScheme.primary 
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (showSearch) {
                        IconButton(onClick = {
                            onSearchToggle()
                            onSearchQueryChange("")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Close Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Photo count indicator
            if (!showSearch && photos.isNotEmpty()) {
                Text(
                    text = "${photos.size} photos",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(start = 72.dp, bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun GradientIcon() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(GradientPurple, GradientPink)
                )
            )
            .shadow(8.dp, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Search photos...",
                        color = MaterialTheme.colorScheme.outline
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
    }
}

@Composable
private fun AnimatedTabIndicator(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TabButton(
            text = "All Photos",
            icon = Icons.Default.Image,
            isSelected = selectedTab == 0,
            onClick = { onTabSelect(0) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            text = "Albums",
            icon = Icons.Default.Collections,
            isSelected = selectedTab == 1,
            onClick = { onTabSelect(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "tabScale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        shadowElevation = if (isSelected) 8.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SelectionCounterBar(
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
private fun WelcomePermissionScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Welcome Icon
        AnimatedWelcomeIcon()
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Welcome to SnapGallery",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Your beautiful photo gallery awaits",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Gradient Button
        GradientButton(
            text = "Allow Access",
            onClick = onRequestPermission
        )
    }
}

@Composable
private fun AnimatedWelcomeIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "welcome")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500)
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(160.dp)
            .scale(scale)
            .shadow(24.dp, CircleShape)
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
            modifier = Modifier.size(80.dp),
            tint = Color.White
        )
    }
}

@Composable
private fun GradientButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(GradientPurple, GradientPink)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun NoSearchResultsScreen(query: String) {
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
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "No Results Found",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "No photos matching \"$query\"",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyGalleryScreen() {
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
private fun EnhancedPhotoGrid(
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
            bottom = 120.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(
            items = photos,
            key = { _, photo -> photo.id }
        ) { index, photo ->
            val isSelected = selectedPhotos.contains(photo.id)
            
            PhotoItem(
                photo = photo,
                isSelected = isSelected,
                isSelectionMode = isSelectionMode,
                onClick = { onPhotoClick(index) }
            )
        }
    }
}

@Composable
private fun PhotoItem(
    photo: MediaItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "photoScale"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
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
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        
        // Selection Overlay
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
                // Selection Checkmark
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .align(Alignment.TopEnd),
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
                
                // Selection Circle
                if (!isSelected) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.5f))
                            .border(2.dp, Color.White, CircleShape)
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun GlassBottomNav(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit,
    hasPhotos: Boolean
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
            shadowElevation = 16.dp,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassNavItem(
                    icon = Icons.Default.Image,
                    label = "Photos",
                    isSelected = selectedTab == 0,
                    onClick = { onTabSelect(0) }
                )
                GlassNavItem(
                    icon = Icons.Default.Collections,
                    label = "Albums",
                    isSelected = selectedTab == 1,
                    onClick = { onTabSelect(1) }
                )
            }
        }
    }
}

@Composable
private fun GlassNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "navScale"
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
                                brush = Brush.linearGradient(
                                    colors = if (icon == Icons.Default.Image) 
                                        listOf(GradientPurple, GradientPink)
                                        else listOf(GradientBlue, GradientCyan)
                                ),
                                shape = CircleShape
                            )
                            .shadow(8.dp, CircleShape)
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
