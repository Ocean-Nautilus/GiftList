package com.example.wishlist228

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Wishlist : Screen("wishlist", "Вишлист", Icons.Default.CardGiftcard)
    object Friends : Screen("friends", "Друзья", Icons.Default.People)
    object Ideas : Screen("ideas", "Идеи", Icons.Default.Explore)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OhMyWishesTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: WishlistViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Wishlist) }
    var showAddBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val items = listOf(Screen.Wishlist, Screen.Friends, Screen.Ideas)
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OhMyPink,
                            selectedTextColor = OhMyPink,
                            indicatorColor = OhMyPink.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                Screen.Wishlist -> WishlistScreen(
                    state = uiState,
                    onCategorySelected = { viewModel.selectCategory(it) },
                    onReserveWish = { viewModel.toggleReservation(it) },
                    onAddWishClick = { showAddBottomSheet = true }
                )
                Screen.Friends -> FriendsScreen(friends = uiState.friends)
                Screen.Ideas -> IdeaFeedScreen(collections = uiState.collections)
            }
        }

        if (showAddBottomSheet) {
            AddWishBottomSheet(
                onDismiss = { showAddBottomSheet = false },
                onAddWish = { title, price, link, priority ->
                    viewModel.addWish(title, price, link, priority)
                }
            )
        }
    }
}