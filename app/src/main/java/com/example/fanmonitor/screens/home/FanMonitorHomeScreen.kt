package com.example.fanmonitor.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fanmonitor.R
import com.example.fanmonitor.navigation.FanMonitorScreens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val DATABASE_URL = "https://fan-monitor-d0c5b-default-rtdb.europe-west1.firebasedatabase.app/"

private data class VoltageReading(val voltage: Double?, val timestamp: Long?, val utcTime: String?)

@Composable
fun FanMonitorHomeScreen(navController: NavController) {
    Scaffold(
        topBar = { FanMonitorTopAppBar(title = "Sensor Monitor", navController = navController) },
        floatingActionButton = { FABContent {} }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding)) {
            HomeContent()
        }
    }
}

@Composable
fun HomeContent() {
    var devices by remember { mutableStateOf<List<String>?>(null) }
    var readings by remember { mutableStateOf<Map<String, VoltageReading>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val database = FirebaseDatabase.getInstance(DATABASE_URL)
        val devicesRef = database.getReference("devices")
        val readingsRef = database.getReference("voltageReadings")
        val devicesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                devices = snapshot.children.mapNotNull { it.key }.sorted()
            }
            override fun onCancelled(databaseError: DatabaseError) {
                error = databaseError.message
            }
        }
        val readingsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latest = mutableMapOf<String, VoltageReading>()
                for (item in snapshot.children) {
                    val device = item.child("device").getValue(String::class.java) ?: continue
                    val voltage = (item.child("voltage").value as? Number)?.toDouble()
                    val timestamp = (item.child("timestamp").value as? Number)?.toLong()
                    val reading = VoltageReading(voltage, timestamp, item.child("utc_time").getValue(String::class.java))
                    val previous = latest[device]
                    if (previous == null || (timestamp ?: Long.MIN_VALUE) > (previous.timestamp ?: Long.MIN_VALUE)) {
                        latest[device] = reading
                    }
                }
                readings = latest
            }
            override fun onCancelled(databaseError: DatabaseError) {
                error = databaseError.message
            }
        }
        devicesRef.addValueEventListener(devicesListener)
        readingsRef.addValueEventListener(readingsListener)
        onDispose {
            devicesRef.removeEventListener(devicesListener)
            readingsRef.removeEventListener(readingsListener)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        TitleSection(label = "Sensor list")
        when {
            error != null -> Text("Unable to load sensors: $error", modifier = Modifier.padding(16.dp))
            devices == null || readings == null -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            devices!!.isEmpty() -> Text("No sensors found.", modifier = Modifier.padding(16.dp))
            else -> LazyColumn {
                items(devices!!, key = { it }) { device ->
                    val reading = readings!![device]
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(device, style = MaterialTheme.typography.titleMedium)
                            Text(reading?.voltage?.let { "Voltage: $it V" } ?: "No voltage reading")
                            reading?.utcTime?.let { Text("Time (UTC): $it") }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FanMonitorTopAppBar(
    title: String,
    showProfile: Boolean = true,
    navController: NavController
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showProfile) {
                    Image(
                        painter = painterResource(R.drawable.sensor_icon),
                        contentDescription = "Sensor icon",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .size(45.dp)
                            .padding(start = 5.dp, end = 5.dp)
                    )
                }
                Text(
                    text = title,
                    color = Color.Black.copy(alpha = 0.7f),
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                )
                Spacer(modifier = Modifier.width(150.dp))
            }
        },
        //Sign out button
        actions = {
            IconButton(
                onClick = {
                    FirebaseAuth.getInstance().signOut().run {
                        navController.navigate(FanMonitorScreens.LoginScreen.name)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "Favorites"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF92CBDF),
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun TitleSection(modifier: Modifier = Modifier, label: String) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
    ) {
        Column(modifier = Modifier.background(Color(0xFF92CBDF))) {
            Text(
                text = label,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    fontStyle = FontStyle.Normal
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 15.dp, bottom = 15.dp)
            )
        }
    }
}

@Composable
fun FABContent(onTap: () -> Unit) {
    FloatingActionButton(
        onClick = { onTap() },
        shape = RoundedCornerShape(50.dp),
        containerColor = Color(0xFF92CBDF)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Fan",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview
@Composable
fun TitleSectionPreview() {
    TitleSection(label = "Default Label")
}