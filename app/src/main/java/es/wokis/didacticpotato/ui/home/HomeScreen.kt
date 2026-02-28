package es.wokis.didacticpotato.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import es.wokis.didacticpotato.ui.components.SensorChipsRow
import org.koin.compose.koinInject

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinInject(),
    onSensorClick: (String) -> Unit = {},
    onAddSensorClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    state.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error)
        }
    }
    
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        HomeContent(
            modifier = Modifier.padding(paddingValues),
            state = state,
            onRefresh = viewModel::refresh,
            onSensorClick = onSensorClick,
            onAddSensorClick = onAddSensorClick
        )
    }
}

@Composable
private fun HomeContent(
    modifier: Modifier,
    state: HomeState,
    onRefresh: () -> Unit = {},
    onSensorClick: (String) -> Unit = {},
    onAddSensorClick: () -> Unit = {}
) {
    if (state.isLoading) {
        HomeSkeletonLoading()
    } else {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Greeting
                Text(
                    text = if (state.userName.isNotEmpty()) "Hello, ${state.userName}" else "Hello",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                // Last Sensor Data Card
                LastSensorDataCard(
                    sensors = state.sensors,
                    onRefresh = onRefresh
                )

                // My Sensors Card
                MySensorsCard(
                    sensors = state.sensors,
                    onSensorClick = onSensorClick,
                    onAddSensorClick = onAddSensorClick
                )

                // Bottom spacer
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HomeSkeletonLoading() {
    val skeletonColor = MaterialTheme.colorScheme.surfaceVariant
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Skeleton for greeting
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(40.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(skeletonColor)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Last Sensor Data card with skeleton content
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Last sensor data",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp)
                )
                // Disabled refresh button placeholder
                IconButton(
                    onClick = {},
                    enabled = false
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
            
            // Skeleton sensor items inside the card
            repeat(2) {
                SensorItemSkeleton(
                    color = skeletonColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                if (it < 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // My Sensors card with skeleton content
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "My Sensors",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(8.dp)
            )
            
            // Horizontal row of sensor chip skeletons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    SensorChipSkeleton(skeletonColor)
                }
            }
        }
    }
}

@Composable
private fun SensorItemSkeleton(color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        // Sensor name skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Info rows skeletons
        repeat(3) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun SensorChipSkeleton(color: Color) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Name skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Temp skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Humidity skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun LastSensorDataCard(
    sensors: List<SensorVO>,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Last sensor data",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(
                    onClick = onRefresh
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }

            sensors.forEach { sensor ->
                SensorData(
                    sensor = sensor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MySensorsCard(
    sensors: List<SensorVO>,
    onSensorClick: (String) -> Unit,
    onAddSensorClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "My Sensors",
                style = MaterialTheme.typography.titleMedium
            )

            SensorChipsRow(
                sensors = sensors,
                onSensorClick = onSensorClick,
                onAddSensorClick = onAddSensorClick
            )
        }
    }
}

@Composable
fun SensorData(
    sensor: SensorVO,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = sensor.name,
            fontWeight = FontWeight.Bold,
        )
        SensorInfoItem(
            Icons.Default.DeviceThermostat,
            "${sensor.temp}ºC ${sensor.humidity}%",
            Modifier.padding(top = 8.dp)
        )
        SensorInfoItem(
            Icons.Default.BatteryStd,
            sensor.batteryPercentage?.let { "$it%" } ?: "Unknown",
            Modifier.padding(top = 8.dp)
        )
        SensorInfoItem(
            Icons.Default.DateRange,
            sensor.lastUpdate,
            Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun SensorInfoItem(
    imageVector: ImageVector,
    data: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(text = data)
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeContent(
            modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.background),
            state = HomeState(
                userName = "Test User",
                sensors = listOf(
                    SensorVO(
                        id = "1",
                        name = "manoleteeeeeeeeeeeeeeee",
                        temp = 27.0,
                        humidity = 69,
                        batteryPercentage = 36,
                        lastUpdate = "17/01/2026 13:30:00"
                    ),
                    SensorVO(
                        id = "2",
                        name = "manolete2",
                        temp = 25.0,
                        humidity = 70,
                        batteryPercentage = null,
                        lastUpdate = "17/01/2026 14:00:00"
                    )
                )
            )
        )
    }
}
