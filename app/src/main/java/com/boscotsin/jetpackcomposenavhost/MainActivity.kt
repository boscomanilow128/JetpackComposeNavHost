package com.boscotsin.jetpackcomposenavhost

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object MainScreen : Screen("main_screen", "Main", Icons.Filled.Home)
    object DetailScreen : Screen("detail_screen", "Detail", Icons.Filled.Info)
    object DetailScreen2 : Screen("detail_screen2", "Detail 2", Icons.Filled.List)
    object DetailScreen3 : Screen("detail_screen3", "Detail 3", Icons.Filled.Star)
    object DetailScreen4 : Screen("detail_screen4", "Detail 4", Icons.Filled.ThumbUp)
}

data class StockData(
    val ticker: String,
    val companyName: String,
    val entryDate: String,
    val entryPrice: String,
    val latestClose: String,
    val dailyChange: String,
    val totalProfit: String
)

data class PortfolioResponse(
    val success: Boolean,
    val portfolioName: String,
    val timestamp: String,
    val data: List<StockData>,
    val errors: String?
)

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
        currentRoute == Screen.DetailScreen4.route -> Screen.DetailScreen4
        //We might have arguments for the route hence "startWith".
        currentRoute?.startsWith(Screen.DetailScreen.route) == true -> Screen.DetailScreen
        else -> Screen.MainScreen
    }

    val canNavigateBack = navController.previousBackStackEntry != null

    val bottomNavItems = listOf(
        Screen.MainScreen,
        Screen.DetailScreen2,
        Screen.DetailScreen3,
        Screen.DetailScreen4
    )

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
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = currentScreen.route == screen.route ||
                                (screen == Screen.DetailScreen && currentRoute?.startsWith(Screen.DetailScreen.route) == true),
                        onClick = {
                            if (screen == Screen.DetailScreen) {
                                // Navigate to DetailScreen with default parameter
                                navController.navigate(Screen.DetailScreen.route + "/DefaultUser") {
                                    popUpTo(Screen.MainScreen.route)
                                    launchSingleTop = true
                                }
                            } else {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.MainScreen.route)
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
                // Add DetailScreen as a separate item
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Screen.DetailScreen.icon,
                            contentDescription = Screen.DetailScreen.title
                        )
                    },
                    label = { Text(Screen.DetailScreen.title) },
                    selected = currentRoute?.startsWith(Screen.DetailScreen.route) == true,
                    onClick = {
                        navController.navigate(Screen.DetailScreen.route + "/DefaultUser") {
                            popUpTo(Screen.MainScreen.route)
                            launchSingleTop = true
                        }
                    }
                )
            }
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
            composable(route = Screen.DetailScreen4.route) {
                DetailScreen4(navController = navController)
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
        Spacer(
            modifier = Modifier.padding(8.dp)
        )
        Button(
            modifier = Modifier.align(Alignment.End),
            onClick = {
                navController.navigate(Screen.DetailScreen4.route)
            }
        ) {
            Text(text = "To DetailScreen4")
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

@Composable
fun DetailScreen4(navController: NavController) {
    var portfolioData by remember { mutableStateOf<PortfolioResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val data = fetchPortfolioData()
            portfolioData = data
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Nuclear Energy Portfolio",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading portfolio data...")
                    }
                }
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            portfolioData != null -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = portfolioData!!.portfolioName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Last updated: ${portfolioData!!.timestamp.split('T')[0]}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(portfolioData!!.data) { stock ->
                        StockCard(stock = stock)
                    }
                }
            }
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Back to Main")
        }
    }
}

@Composable
fun StockCard(stock: StockData) {
    val profitColor = if (stock.totalProfit.replace("%", "").toDoubleOrNull()?.let { it >= 0 } == true) {
        Color(0xFF4CAF50) // Green for profit
    } else {
        Color(0xFFF44336) // Red for loss
    }

    val dailyChangeColor = if (stock.dailyChange.replace("%", "").toDoubleOrNull()?.let { it >= 0 } == true) {
        Color(0xFF4CAF50) // Green for positive change
    } else {
        Color(0xFFF44336) // Red for negative change
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stock.ticker,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stock.companyName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = stock.totalProfit,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = profitColor
                    )
                    Text(
                        text = stock.dailyChange,
                        style = MaterialTheme.typography.bodySmall,
                        color = dailyChangeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Entry Price",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${stock.entryPrice}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column {
                    Text(
                        text = "Latest Close",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${stock.latestClose}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column {
                    Text(
                        text = "Entry Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stock.entryDate,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
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

suspend fun fetchPortfolioData(): PortfolioResponse {
    return withContext(Dispatchers.IO) {
        val url = URL("https://www.hkdrphileeresearch.com/fetchOHLC/v2/USNuclearEnergy")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                parsePortfolioResponse(jsonString)
            } else {
                throw Exception("HTTP Error: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }
}

fun parsePortfolioResponse(jsonString: String): PortfolioResponse {
    val jsonObject = JSONObject(jsonString)

    val success = jsonObject.getBoolean("success")
    val portfolioName = jsonObject.getString("portfolioName")
    val timestamp = jsonObject.getString("timestamp")
    val errors = if (jsonObject.isNull("errors")) null else jsonObject.getString("errors")

    val dataArray = jsonObject.getJSONArray("data")
    val stockList = mutableListOf<StockData>()

    for (i in 0 until dataArray.length()) {
        val stockObject = dataArray.getJSONObject(i)
        val stock = StockData(
            ticker = stockObject.getString("ticker"),
            companyName = stockObject.getString("companyName"),
            entryDate = stockObject.getString("entryDate"),
            entryPrice = stockObject.getString("entryPrice"),
            latestClose = stockObject.getString("latestClose"),
            dailyChange = stockObject.getString("dailyChange"),
            totalProfit = stockObject.getString("totalProfit")
        )
        stockList.add(stock)
    }

    return PortfolioResponse(
        success = success,
        portfolioName = portfolioName,
        timestamp = timestamp,
        data = stockList,
        errors = errors
    )
}