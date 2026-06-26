package com.artbirwww.messenger.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.artbirwww.messenger.data.repository.AuthRepository
import com.artbirwww.messenger.data.repository.ChatRepository
import com.artbirwww.messenger.ui.components.NotificationHelper
import com.artbirwww.messenger.ui.screens.chat.ChatListScreen
import com.artbirwww.messenger.ui.screens.chat.ChatScreen
import com.artbirwww.messenger.ui.screens.login.LoginScreen
import com.artbirwww.messenger.ui.screens.profile.ProfileScreen
import com.artbirwww.messenger.ui.screens.register.RegisterScreen
import com.artbirwww.messenger.ui.theme.MessengerTheme
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            MessengerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDestination = if (AuthRepository.getCurrentUser() != null) "chat_list" else "login"

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("login") {
                            LoginScreen(
                                onNavigateToChat = {
                                    navController.navigate("chat_list") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                },
                                onNavigateToChat = {
                                    navController.navigate("chat_list") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("chat_list") {
                            ChatListScreen(
                                onChatSelected = { chatId, otherUserId, otherUserName ->
                                    navController.navigate("chat/$chatId/$otherUserId")
                                },
                                onNavigateToProfile = {
                                    navController.navigate("profile")
                                }
                            )
                        }
                        composable(
                            route = "chat/{chatId}/{otherUserId}",
                            arguments = listOf(
                                navArgument("chatId") { type = NavType.StringType },
                                navArgument("otherUserId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                            val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""
                            ChatScreen(
                                chatId = chatId,
                                otherUserId = otherUserId,
                                onNavigateToProfile = {
                                    navController.navigate("profile")
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Start listening for messages globally for notifications
        startGlobalMessageListener()
    }
    
    private fun startGlobalMessageListener() {
        val currentUser = AuthRepository.getCurrentUser() ?: return
        lifecycleScope.launch {
            ChatRepository.getChats(currentUser.uid).drop(1).collect { chats ->
                // Basic logic: if latest chat has a message from someone else, notify
                val latestChat = chats.firstOrNull() ?: return@collect
                if (latestChat.lastSenderId != currentUser.uid && latestChat.lastMessageTime > System.currentTimeMillis() - 5000) {
                    NotificationHelper.showNotification(
                        this@MainActivity,
                        "Новое сообщение",
                        latestChat.lastMessage
                    )
                }
            }
        }
    }
}
