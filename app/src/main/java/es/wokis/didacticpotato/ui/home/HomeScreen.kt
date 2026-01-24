package es.wokis.didacticpotato.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.koin.compose.koinInject

@Composable
fun HomeScreen(
    modifier: Modifier,
    viewModel: HomeViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    HomeContent(modifier, state)
}

@Composable
private fun HomeContent(modifier: Modifier, state: HomeState) {
    Column(modifier) {
        Text(
            text = "Hello, ${state.userName}",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Last sensor data",
                    modifier = Modifier
                        .padding(8.dp)
                )
                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = ""
                    )
                }
            }

            state.sensors.forEach { sensor ->
                SensorData(sensor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "Sensors",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun SensorData(sensor: SensorVO, modifier: Modifier) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = sensor.name,
            fontWeight = FontWeight.Bold,
        )
        SensorInfoItem(Icons.Default.DeviceThermostat, "${sensor.temp}ºC ${sensor.humidity} %", Modifier.padding(top = 8.dp))
        SensorInfoItem(Icons.Default.BatteryStd, sensor.batteryPercentage?.let { "$it %" } ?: "Unknown", Modifier.padding(top = 8.dp))
        SensorInfoItem(Icons.Default.DateRange, sensor.lastUpdate, Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun SensorInfoItem(imageVector: ImageVector, data: String, modifier: Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = "",
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = data,
        )
    }
}

@Composable
private fun Sensor() {
    Column {

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
