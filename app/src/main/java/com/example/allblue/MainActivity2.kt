@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.allblue

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.allblue.databinding.ActivityMainBinding
import com.example.compose.Material3AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity2 : ComponentActivity() {

    val CHANNEL_DEFAULT_IMPORTANCE = "Media Playing Service"
    val devices_list : ArrayList<BluetoothDevice> = ArrayList()
    val REQUEST_ENABLE_BT = 1
    val TAG = "Main Activity"
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        startService(Intent(this, MediaPlayingService::class.java))

        setContent {
            Main(){}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(content: @Composable () -> Unit) {

    val viewModel: BluetoothViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val context = LocalContext.current
    viewModel.getPairedDevices(context)

    Material3AppTheme() {

        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colorScheme.background) {
            content()
            Scaffold(Modifier.fillMaxHeight(1f),
                topBar = {
                   CenterAlignedTopAppBar(
                       title = { Text(text = "allBlue")},
                       navigationIcon = {
                           var menuStatus by remember {
                               mutableStateOf(false)
                           }

                           IconButton(onClick = { menuStatus = !menuStatus }) {
                               if (menuStatus) {
                                   Icon(painter = painterResource(id = R.drawable.round_menu_open_24), contentDescription ="General settings opened")
                               } else {
                                   Icon(painter = painterResource(id = R.drawable.round_menu_24), contentDescription = "General settings")
                               }
                           }
                       }
                   )
                }
                ) {
                val context = LocalContext.current
                Body(context)
            }
        }
    }
}

@Composable
fun Body(context: Context) {
    Column(
        Modifier.fillMaxHeight(1f),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.End
    ) {
        Section1()
        Section2(context)
        FAB()
    }
}

@Composable
fun Section1(bluetoothViewModel: BluetoothViewModel = viewModel()) {

    // Instantiate a model even if blank, of selected bluetooth device
    bluetoothViewModel.getSelectedDevice()

    // Display the selected Bluetooth device by user
    val bluetoothState = bluetoothViewModel.viewState.collectAsState(initial = BluetoothState())
    val selectedDevice = bluetoothState.value.selectedDevice
    val name = selectedDevice?.name
    val address = selectedDevice?.address

    Row (modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
        horizontalArrangement = Arrangement.Start) {
            Text(text = name ?: "")
            Text(text = address ?: "")
        }
}

@Composable
fun Section2(context: Context, viewModel: BluetoothViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    // Lazy Column list of all paired bluetooth devices

    val bluetoothState = viewModel.viewState.collectAsState(initial = BluetoothState())
    val pairedDevices: ArrayList<BluetoothDevice> = bluetoothState.value.pairedDevices

    LazyColumn(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)) {
        items(pairedDevices) { pairedDevice ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clickable {
                        viewModel.saveSelectedDevice(pairedDevice)
                        Log.d("Row1", "Clicked $pairedDevice")
                    },
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(text = pairedDevice.name)
                Text(text = pairedDevice.address)
            }
        }
    }
}

@Composable
fun FAB() {

    val initText = R.string.startallblue_service
    val elseText = R.string.stopallblue_service

    var btnText by remember {
        mutableStateOf(initText)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.End) {
        ExtendedFloatingActionButton(onClick = {

            btnText = if (btnText == initText) {
                elseText
            } else {
                initText
            }
        }, text = {
            Text(text = stringResource(id = btnText))
        })
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Main() {
        val context = LocalContext.current
        Body(context)
    }
}