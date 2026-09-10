package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.LayoutHeader
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.ConsultationScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.HomeoClinicTheme
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.ConsultationViewModel
import com.example.ui.viewmodel.HistoryViewModel

class MainActivity : ComponentActivity() {

    private val consultationViewModel: ConsultationViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()
    private val adminViewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HomeoClinicTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    topBar = {
                        if (currentRoute != "splash") {
                            LayoutHeader(
                                onNavigateHome = {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                showHomeButton = currentRoute != "home"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("splash") {
                            SplashScreen(
                                onSplashFinished = {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                onStartConsultation = {
                                    consultationViewModel.resetConsultation()
                                    navController.navigate("consultation")
                                },
                                onViewHistory = {
                                    navController.navigate("history")
                                },
                                onOpenAdmin = {
                                    navController.navigate("admin")
                                }
                            )
                        }

                        composable("consultation") {
                            ConsultationScreen(
                                viewModel = consultationViewModel,
                                onNavigateHome = {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                },
                                onNavigateResult = { consultationId ->
                                    navController.navigate("result/$consultationId") {
                                        popUpTo("consultation") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(
                            route = "result/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getInt("id") ?: 0
                            ResultScreen(
                                consultationId = id,
                                viewModel = consultationViewModel,
                                onNewConsultation = {
                                    consultationViewModel.resetConsultation()
                                    navController.navigate("consultation")
                                },
                                onReturnHome = {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("history") {
                            HistoryScreen(
                                viewModel = historyViewModel,
                                onSelectConsultation = { consultationId ->
                                    navController.navigate("result/$consultationId")
                                },
                                onStartFirstConsultation = {
                                    consultationViewModel.resetConsultation()
                                    navController.navigate("consultation")
                                }
                            )
                        }

                        composable("admin") {
                            AdminScreen(
                                viewModel = adminViewModel,
                                onNavigateHome = {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
