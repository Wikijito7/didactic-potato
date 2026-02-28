package es.wokis.didacticpotato.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import es.wokis.didacticpotato.ui.home.SensorVO

@Composable
fun SensorChipsRow(
    sensors: List<SensorVO>,
    onSensorClick: (String) -> Unit,
    onAddSensorClick: () -> Unit,
    modifier: Modifier = Modifier,
    showAddButton: Boolean = true
) {
    LazyRow(
        modifier = modifier
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sensors) { sensor ->
            SensorChip(
                sensor = sensor,
                onClick = { onSensorClick(sensor.id) }
            )
        }
        if (showAddButton) {
            item {
                AddSensorChip(onClick = onAddSensorClick)
            }
        }
    }
}

@Composable
fun SensorChip(
    sensor: SensorVO,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = sensor.name,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${sensor.temp}ºC",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${sensor.humidity}%",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AddSensorChip(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add sensor",
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Add",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
