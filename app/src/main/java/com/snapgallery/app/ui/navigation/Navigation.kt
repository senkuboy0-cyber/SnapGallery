package com.snapgallery.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.snapgallery.app.ui.screens.AlbumsScreen
import com.snapgallery.app.ui.screens.AlbumDetailScreen
import com.snapgallery.app.ui.screens.PhotoViewerScreen
import com.snapgallery.app.ui.screens.MainScreen
import com.snapgallery.app.ui.viewmodel.GalleryViewModel

sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Albums : Screen("albums")
    data object AlbumDetail : Screen("album/{albumId}/{albumName}") {
        fun createRoute(albumId: String, albumName: String) = "album/$albumId/$albumName"
    }
    data object PhotoViewer : Screen("photo/{photoIndex}") {
        fun createRoute(photoIndex: Int) = "photo/$photoIndex"
    }
}

@Composable
fun SnapGalleryNavHost() {
    val navController = rememberNavController()
    val viewModel: GalleryViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToAlbums = { navController.navigate(Screen.Albums.route) },
                onPhotoClick = { index ->
                    navController.navigate(Screen.PhotoViewer.createRoute(index))
                },
                viewModel = viewModel
            )
        }

        composable(Screen.Albums.route) {
            AlbumsScreen(
                onAlbumClick = { albumId, albumName ->
                    navController.navigate(Screen.AlbumDetail.createRoute(albumId, albumName))
                },
                onBackClick = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.AlbumDetail.route,
            arguments = listOf(
                navArgument("albumId") { type = NavType.StringType },
                navArgument("albumName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
            val albumName = backStackEntry.arguments?.getString("albumName") ?: ""
            AlbumDetailScreen(
                albumId = albumId,
                albumName = albumName,
                onPhotoClick = { index ->
                    navController.navigate(Screen.PhotoViewer.createRoute(index))
                },
                onBackClick = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(
            route = Screen.PhotoViewer.route,
            arguments = listOf(navArgument("photoIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val photoIndex = backStackEntry.arguments?.getInt("photoIndex") ?: 0
            PhotoViewerScreen(
                initialIndex = photoIndex,
                onBackClick = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
    }
}