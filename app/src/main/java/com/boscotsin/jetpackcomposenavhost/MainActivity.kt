package com.boscotsin.jetpackcomposenavhost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.boscotsin.jetpackcomposenavhost.ui.theme.JetpackComposeNavHostTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

sealed class Screen(val route: String, val title: String) {
    object MainScreen : Screen("main_screen", "Main")
    object DetailScreen : Screen("detail_screen", "Detail")
    object DetailScreen2 : Screen("detail_screen2", "Detail 2")
    object DetailScreen3 : Screen("detail_screen3", "Detail 3")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetpackComposeNavHostTheme {
                Navigation()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentScreen = when {
        currentRoute == Screen.MainScreen.route -> Screen.MainScreen
        currentRoute == Screen.DetailScreen2.route -> Screen.DetailScreen2
        currentRoute == Screen.DetailScreen3.route -> Screen.DetailScreen3
        //We might have arguments for the route hence "startWith".
        currentRoute?.startsWith(Screen.DetailScreen.route) == true -> Screen.DetailScreen
        else -> Screen.MainScreen
    }

    val canNavigateBack = navController.previousBackStackEntry != null

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(currentScreen.title) },
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.MainScreen.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = Screen.MainScreen.route) {
                MainScreen(navController = navController)
            }
            composable(
                route = Screen.DetailScreen.route + "/{name}",
                arguments = listOf(
                    navArgument("name") {
                        type = NavType.StringType
                        defaultValue = "Bosco"
                        nullable = true
                    }
                )
            ) {
                DetailScreen(
                    name = it.arguments?.getString("name"),
                    navController = navController
                )
            }
            composable(route = Screen.DetailScreen2.route) {
                DetailScreen2(navController = navController)
            }
            composable(route = Screen.DetailScreen3.route) {
                DetailScreen3(navController = navController)
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    var text by remember {
        mutableStateOf("")
    }
    Column (
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 50.dp)
    ) {
        TextField(
            value = text,
            onValueChange = {
                text = it
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(
            modifier = Modifier.padding(8.dp)
        )
        Button(
            modifier = Modifier.align(Alignment.End),
            onClick = {
                navController.navigate(Screen.DetailScreen.route + "/$text")
            }
        ) {
            Text(text = "To DetailScreen")
        }
        Spacer(
            modifier = Modifier.padding(8.dp)
        )
        Button(
            modifier = Modifier.align(Alignment.End),
            onClick = {
                navController.navigate(Screen.DetailScreen2.route)
            }
        ) {
            Text(text = "To DetailScreen2")
        }
        Spacer(
            modifier = Modifier.padding(8.dp)
        )
        Button(
            modifier = Modifier.align(Alignment.End),
            onClick = {
                navController.navigate(Screen.DetailScreen3.route)
            }
        ) {
            Text(text = "To DetailScreen3")
        }
    }
}

@Composable
fun DetailScreen(name: String?, navController: NavController) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Hello, $name")
            Spacer(modifier = Modifier.padding(8.dp))
            Button(
                onClick = { navController.popBackStack() }
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
fun DetailScreen2(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Detail Screen 2",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "This is a card component",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Text(
            text = "Simple text element",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(onClick = { }) {
            Text("Sample Button")
        }

        Text(
            text = "Another text element for testing",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { navController.popBackStack() }
        ) {
            Text("Back to Main")
        }
    }
}

@Composable
fun DetailScreen3(navController: NavController) {
    var jsonData by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val data = fetchJsonData()
            jsonData = data
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "US Nuclear Energy Portfolio",
            style = MaterialTheme.typography.headlineMedium
        )

        when {
            isLoading -> {
                CircularProgressIndicator()
                Text("Loading data...")
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            jsonData != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = jsonData!!,
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { navController.popBackStack() }
        ) {
            Text("Back to Main")
        }
    }
}

suspend fun fetchJsonData(): String {
    return withContext(Dispatchers.IO) {
        val url = URL("https://www.hkdrphileeresearch.com/fetchOHLC/v2/USNuclearEnergy")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                throw Exception("HTTP Error: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }
}