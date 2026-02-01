package es.wokis.didacticpotato

import android.os.Bundle
import android.view.WindowManager
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import es.wokis.didacticpotato.data.auth.TokenProvider
import es.wokis.didacticpotato.data.local.SettingsRepository
import es.wokis.didacticpotato.di.dataModule
import es.wokis.didacticpotato.di.domainModule
import es.wokis.didacticpotato.di.uiModule
import es.wokis.didacticpotato.ui.auth.LoginScreen
import es.wokis.didacticpotato.ui.auth.RegisterScreen
import es.wokis.didacticpotato.ui.home.HomeScreen
import es.wokis.didacticpotato.ui.profile.ProfileScreen
import es.wokis.didacticpotato.ui.profile.ProfileViewModel
import es.wokis.didacticpotato.ui.profile.edit.EditProfileScreen
import es.wokis.didacticpotato.ui.profile.options.OptionsScreen
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
    val context = LocalContext.current
    val tokenProvider = koinInject<TokenProvider>()
    val settingsRepository = remember { SettingsRepository(context) }
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    // Auth state
    var showAuth by rememberSaveable { mutableStateOf(false) }
    var isRegisterMode by rememberSaveable { mutableStateOf(false) }

    // Profile sub-screen navigation
    var profileSubScreen by rememberSaveable { mutableStateOf<ProfileSubScreen?>(null) }

    ApplySecureScreenFlag(settingsRepository)

    if (showAuth) {
        AuthContent(
            isRegisterMode = isRegisterMode,
            onRegisterSuccess = {
                showAuth = false
                currentDestination = AppDestinations.PROFILE
            },
            onLoginSuccess = {
                showAuth = false
                currentDestination = AppDestinations.PROFILE
            },
            onToggleAuthMode = { isRegisterMode = !isRegisterMode }
        )
    } else {
        MainAppContent(
            currentDestination = currentDestination,
            tokenProvider = tokenProvider,
            profileSubScreen = profileSubScreen,
            onDestinationChange = { destination ->
                if (destination == AppDestinations.PROFILE && !tokenProvider.hasToken()) {
                    showAuth = true
                    isRegisterMode = false
                } else {
                    currentDestination = destination
                }
            },
            onProfileSubScreenChange = { profileSubScreen = it },
            onLogout = {
                showAuth = true
                isRegisterMode = false
                profileSubScreen = null
            }
        )
    }
}

@Composable
private fun ApplySecureScreenFlag(settingsRepository: SettingsRepository) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    DisposableEffect(Unit) {
        val isPrivateEnabled = settingsRepository.isPrivateScreenEnabled()
        activity?.window?.let { window ->
            if (isPrivateEnabled) {
                window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
        onDispose { }
    }
}

@Composable
private fun AuthContent(
    isRegisterMode: Boolean,
    onRegisterSuccess: () -> Unit,
    onLoginSuccess: () -> Unit,
    onToggleAuthMode: () -> Unit
) {
    if (isRegisterMode) {
        RegisterScreen(
            onRegisterSuccess = onRegisterSuccess,
            onNavigateToLogin = onToggleAuthMode
        )
    } else {
        LoginScreen(
            onLoginSuccess = onLoginSuccess,
            onNavigateToRegister = onToggleAuthMode
        )
    }
}

@Composable
private fun MainAppContent(
    currentDestination: AppDestinations,
    tokenProvider: TokenProvider, // TODO: Verify if needed for auth state check - currently prepared for future use
    profileSubScreen: ProfileSubScreen?,
    onDestinationChange: (AppDestinations) -> Unit,
    onProfileSubScreenChange: (ProfileSubScreen?) -> Unit,
    onLogout: () -> Unit
) {
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    selected = destination == currentDestination,
                    onClick = { onDestinationChange(destination) }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            AppDestinationContent(
                currentDestination = currentDestination,
                profileSubScreen = profileSubScreen,
                innerPadding = innerPadding,
                onProfileSubScreenChange = onProfileSubScreenChange,
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun AppDestinationContent(
    currentDestination: AppDestinations,
    profileSubScreen: ProfileSubScreen?,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    onProfileSubScreenChange: (ProfileSubScreen?) -> Unit,
    onLogout: () -> Unit
) {
    when (currentDestination) {
        AppDestinations.HOME -> HomeScreen(
            modifier = Modifier.padding(innerPadding),
            onSensorClick = { sensorId ->
                println("Sensor clicked: $sensorId")
            },
            onAddSensorClick = {
                println("Add sensor clicked")
            }
        )
        AppDestinations.SENSORS -> Greeting(
            name = currentDestination.label,
            modifier = Modifier.padding(innerPadding)
        )
        AppDestinations.PROFILE -> ProfileDestination(
            profileSubScreen = profileSubScreen,
            innerPadding = innerPadding,
            onProfileSubScreenChange = onProfileSubScreenChange,
            onLogout = onLogout
        )
    }
}

@Composable
private fun ProfileDestination(
    profileSubScreen: ProfileSubScreen?,
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    onProfileSubScreenChange: (ProfileSubScreen?) -> Unit,
    onLogout: () -> Unit
) {
    val profileViewModel = koinInject<ProfileViewModel>()
    when (profileSubScreen) {
        ProfileSubScreen.EDIT_PROFILE -> EditProfileScreen(
            modifier = Modifier.padding(innerPadding),
            onNavigateBack = { onProfileSubScreenChange(null) }
        )
        ProfileSubScreen.OPTIONS -> OptionsScreen(
            modifier = Modifier.padding(innerPadding),
            onNavigateBack = { onProfileSubScreenChange(null) },
            onSetup2FA = {
                println("Navigate to 2FA setup")
            },
            onLogout = onLogout
        )
        null -> ProfileScreen(
            modifier = Modifier.padding(innerPadding),
            onNavigateToEditProfile = { onProfileSubScreenChange(ProfileSubScreen.EDIT_PROFILE) },
            onNavigateToOptions = { onProfileSubScreenChange(ProfileSubScreen.OPTIONS) },
            onResendVerificationEmail = { profileViewModel.resendVerificationEmail() }
        )
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

enum class ProfileSubScreen {
    EDIT_PROFILE,
    OPTIONS
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
