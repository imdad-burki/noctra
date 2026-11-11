package com.imdadburki.noctra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.imdadburki.noctra.core.datastore.SettingsStore
import com.imdadburki.noctra.core.designsystem.NoctraAmoledScheme
import com.imdadburki.noctra.core.designsystem.NoctraColorScheme
import com.imdadburki.noctra.core.designsystem.NoctraColors
import com.imdadburki.noctra.core.designsystem.NoctraLightScheme
import com.imdadburki.noctra.core.designsystem.NoctraTypography
import com.imdadburki.noctra.feature.clock.ClockScreen
import com.imdadburki.noctra.feature.discover.DiscoverScreen
import com.imdadburki.noctra.feature.home.AboutScreen
import com.imdadburki.noctra.feature.home.HomeScreen

/**
 * NOCTRA v2 — only four destinations, by Imdad's order:
 * Home, Clocks (150+ styles), Wallpapers, About (simple).
 * Alarm / Timer / Stopwatch / World Clock / Widgets / Library / Profile /
 * Studio (custom clock maker) were removed in v2 — clocks + wallpapers only.
 */
private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab("home", "Home", Icons.Filled.Dashboard),
    Tab("clocks", "Clocks", Icons.Filled.AccessTime),
    Tab("wallpapers", "Wallpapers", Icons.Filled.Wallpaper),
    Tab("about", "About", Icons.Filled.Info)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val store = SettingsStore(applicationContext)
            val themeName by store.theme.collectAsState(initial = "system")
            val isDarkSystem = isSystemInDarkTheme()
            val scheme = when (themeName) {
                "light" -> NoctraLightScheme
                "amoled" -> NoctraAmoledScheme
                "dark" -> NoctraColorScheme
                else -> if (isDarkSystem) NoctraColorScheme else NoctraLightScheme
            }
            MaterialTheme(colorScheme = scheme, typography = NoctraTypography) {
                val navController = rememberNavController()
                val backStack by navController.currentBackStackEntryAsState()
                val currentRoute = backStack?.destination?.route ?: "home"
                val showBottomBar = TABS.any { it.route == currentRoute }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(containerColor = NoctraColors.Surface) {
                                for (tab in TABS) {
                                    NavigationBarItem(
                                        selected = currentRoute == tab.route,
                                        onClick = {
                                            navController.navigate(tab.route) {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = NoctraColors.Bg,
                                            selectedTextColor = NoctraColors.Gold,
                                            indicatorColor = NoctraColors.Gold,
                                            unselectedIconColor = NoctraColors.Secondary,
                                            unselectedTextColor = NoctraColors.Secondary
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(padding)
                    ) {
                        composable("home") {
                            HomeScreen(onNavigate = { route -> navController.navigate(route) })
                        }
                        composable("clocks") { ClockScreen() }
                        composable("wallpapers") { DiscoverScreen() }
                        composable("about") { AboutScreen() }
                    }
                }
            }
        }
    }
}
