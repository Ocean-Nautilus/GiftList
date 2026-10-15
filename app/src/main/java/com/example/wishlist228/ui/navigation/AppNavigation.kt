package com.example.wishlist228.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wishlist228.ui.screens.FriendsScreen
import com.example.wishlist228.ui.screens.GiftDetailScreen
import com.example.wishlist228.ui.screens.GiftFormScreen
import com.example.wishlist228.ui.screens.GiftListScreen
import com.example.wishlist228.ui.screens.SettingsScreen
import com.example.wishlist228.ui.screens.StatsScreen
import com.example.wishlist228.viewmodels.FriendsViewModel
import com.example.wishlist228.viewmodels.GiftListViewModel
import com.example.wishlist228.viewmodels.SettingsViewModel
import com.example.wishlist228.viewmodels.StatsViewModel

/** Names of the screens (navigation routes) and helpers that build routes with arguments. */
object Routes {
    const val GIFTS = "gifts"
    const val FRIENDS = "friends"
    const val STATS = "stats"
    const val SETTINGS = "settings"

    const val ARG_GIFT_ID = "giftId"
    const val ARG_FRIEND_ID = "friendId"

    const val GIFT_DETAIL = "gift/{$ARG_GIFT_ID}"
    const val GIFT_FORM = "gift_form/{$ARG_GIFT_ID}?$ARG_FRIEND_ID={$ARG_FRIEND_ID}"

    /** Value of the optional friend argument that means "no friend was chosen". */
    const val NO_FRIEND = -1L

    fun giftDetail(giftId: Long) = "gift/$giftId"

    /** `giftId == 0` opens an empty form for a new gift, otherwise the gift is edited. */
    fun giftForm(giftId: Long = 0L, friendId: Long? = null) =
        "gift_form/$giftId?$ARG_FRIEND_ID=${friendId ?: NO_FRIEND}"
}

private data class BottomTab(val route: String, val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    BottomTab(Routes.GIFTS, "Подарки", Icons.Filled.CardGiftcard),
    BottomTab(Routes.FRIENDS, "Друзья", Icons.Filled.People),
    BottomTab(Routes.STATS, "Статистика", Icons.Filled.BarChart),
    BottomTab(Routes.SETTINGS, "Настройки", Icons.Filled.Settings)
)

/**
 * The root of the UI: bottom navigation between the four main screens, plus the detail and
 * the add/edit screens that open on top of them. All one-time messages of the ViewModels
 * are shown here in one Snackbar.
 */
@Composable
fun GiftListApp(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val giftViewModel: GiftListViewModel = viewModel()
    val friendsViewModel: FriendsViewModel = viewModel()
    val statsViewModel: StatsViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(giftViewModel) {
        giftViewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(friendsViewModel) {
        friendsViewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(settingsViewModel) {
        settingsViewModel.events.collect { snackbarHostState.showSnackbar(it) }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomTabs.any { it.route == currentRoute }

    fun openTab(route: String) {
        navController.navigate(route) {
            // Keep one copy of every tab and restore its state (scroll position, filters)
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { openTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.GIFTS,
            // The inner screens have their own top bars: do not add the insets twice
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(Routes.GIFTS) {
                GiftListScreen(
                    viewModel = giftViewModel,
                    onAddGift = { navController.navigate(Routes.giftForm()) },
                    onAddFriend = { openTab(Routes.FRIENDS) },
                    onOpenGift = { giftId -> navController.navigate(Routes.giftDetail(giftId)) }
                )
            }
            composable(Routes.FRIENDS) {
                FriendsScreen(viewModel = friendsViewModel)
            }
            composable(Routes.STATS) {
                StatsScreen(viewModel = statsViewModel)
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(viewModel = settingsViewModel)
            }
            composable(
                route = Routes.GIFT_DETAIL,
                arguments = listOf(navArgument(Routes.ARG_GIFT_ID) { type = NavType.LongType })
            ) { entry ->
                val giftId = entry.arguments?.getLong(Routes.ARG_GIFT_ID) ?: 0L
                GiftDetailScreen(
                    viewModel = giftViewModel,
                    giftId = giftId,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Routes.giftForm(giftId = id)) }
                )
            }
            composable(
                route = Routes.GIFT_FORM,
                arguments = listOf(
                    navArgument(Routes.ARG_GIFT_ID) { type = NavType.LongType },
                    navArgument(Routes.ARG_FRIEND_ID) {
                        type = NavType.LongType
                        defaultValue = Routes.NO_FRIEND
                    }
                )
            ) { entry ->
                val giftId = entry.arguments?.getLong(Routes.ARG_GIFT_ID) ?: 0L
                val friendId = entry.arguments?.getLong(Routes.ARG_FRIEND_ID)
                    ?.takeIf { it != Routes.NO_FRIEND }
                GiftFormScreen(
                    viewModel = giftViewModel,
                    giftId = giftId,
                    presetFriendId = friendId,
                    onClose = { navController.popBackStack() },
                    onAddFriend = {
                        navController.popBackStack()
                        openTab(Routes.FRIENDS)
                    }
                )
            }
        }
    }
}
