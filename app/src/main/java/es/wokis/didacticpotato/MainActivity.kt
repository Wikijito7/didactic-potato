package es.wokis.didacticpotato

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import es.wokis.didacticpotato.data.auth.TokenProvider
import es.wokis.didacticpotato.di.dataModule
import es.wokis.didacticpotato.di.domainModule
import es.wokis.didacticpotato.di.uiModule
import es.wokis.didacticpotato.ui.auth.LoginScreen
import es.wokis.didacticpotato.ui.home.HomeScreen
import es.wokis.didacticpotato.ui.theme.DidacticpotatoTheme
import org.koin.android.ext.koin.androidContext
import org.koin.compose.koinInject
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        startKoin {
            androidContext(this@MainActivity)
            modules(dataModule, domainModule, uiModule)
        }

        setContent {
            DidacticpotatoTheme {
                DidacticpotatoApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun DidacticpotatoApp() {
    val tokenProvider = koinInject<TokenProvider>()
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var showLogin by rememberSaveable { mutableStateOf(false) }

    if (showLogin) {
        LoginScreen(
            onLoginSuccess = {
                showLogin = false
                currentDestination = AppDestinations.PROFILE
            }
        )
    } else {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach {
                    item(
                        icon = {
                            Icon(
                                it.icon,
                                contentDescription = it.label
                            )
                        },
                        label = { Text(it.label) },
                        selected = it == currentDestination,
                        onClick = {
                            if (it == AppDestinations.PROFILE && !tokenProvider.hasToken()) {
                                showLogin = true
                            } else {
                                currentDestination = it
                            }
                        }
                    )
                }
            }
        ) {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                when (currentDestination) {
                    AppDestinations.HOME -> HomeScreen(
                        modifier = Modifier.padding(innerPadding),
                        onSensorClick = { sensorId ->
                            // TODO: Navigate to sensor detail screen
                            println("Sensor clicked: $sensorId")
                        },
                        onAddSensorClick = {
                            // TODO: Implement add sensor workflow (Bluetooth pairing with ESP32)
                        }
                    )
                    AppDestinations.SENSORS -> Greeting(
                        name = currentDestination.label,
                        modifier = Modifier.padding(innerPadding)
                    )
                    AppDestinations.PROFILE -> Greeting(
                        name = currentDestination.label,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    SENSORS("Sensors", Icons.Default.Menu),
    PROFILE("Profile", Icons.Default.AccountBox),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DidacticpotatoTheme {
        Greeting("Android")
    }
}
