package com.example.allblue

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.Material3AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity2 : ComponentActivity() {

//    val CHANNEL_DEFAULT_IMPORTANCE = "Media Playing Service"
//    val devices_list : ArrayList<BluetoothDevice> = ArrayList()
//    val REQUEST_ENABLE_BT = 1
//    val TAG = "Main Activity"
//    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val bluetoothViewModel: BluetoothViewModel = viewModel()
            val bluetoothState = bluetoothViewModel.viewState.collectAsState().value
            val context = LocalContext.current

            // Instantiate a model even if blank, of selected bluetooth device
            bluetoothViewModel.getSelectedDevice()

            // Get the name and address of saved device from UserDevice state
            val name = bluetoothState.selectedDevice.name
            val address = bluetoothState.selectedDevice.address

            Main(
                context,
                name,
                address,
                bluetoothViewModel::getPairedDevices,
                bluetoothState.pairedDevices,
                bluetoothViewModel::saveSelectedDevice,
            ) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(
    context: Context,
    name: String,
    address: String,
    getPairedDevices: (context: Context) -> Unit,
    PairedDevices: ArrayList<BluetoothDevice>,
    saveSelectedDevice: (device: BluetoothDevice) -> Unit,
    Content: @Composable () -> Unit,
) {
    Material3AppTheme() {

        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
            Content()
            Scaffold(Modifier
                .fillMaxHeight(1f)
                .fillMaxWidth(1f)
                .padding(16.dp),
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(text = "allBlue") },
                        navigationIcon = {
                            var menuStatus by remember {
                                mutableStateOf(false)
                            }

                            IconButton(onClick = { menuStatus = !menuStatus }) {
                                if (menuStatus) {
                                    Icon(painter = painterResource(id = R.drawable.round_menu_open_24),
                                        contentDescription = "General settings opened")
                                } else {
                                    Icon(painter = painterResource(id = R.drawable.round_menu_24),
                                        contentDescription = "General settings")
                                }
                            }
                        }
                    )
                },
                content = { contentPadding ->
                    Column() {
                        Section1(name, address, contentPadding)
                        Section2(getPairedDevices,
                            PairedDevices,
                            saveSelectedDevice,
                            context)
                        FAB(contentPadding)
                    }
                }
            )
        }
    }
}



@Composable
fun Section1(
    bluetoothName: String,
    bluetoothAddress: String,
    contentPadding: PaddingValues,
) {

    Card(
        modifier = Modifier.size(height = 300.dp, width = 250.dp)
    ) {
        Text(text = stringResource(id = R.string.selected_device))
        Row(modifier = Modifier
            .size(width = 250.dp, height = 300.dp)
            .fillMaxWidth()
            .padding(contentPadding),
            horizontalArrangement = Arrangement.Start) {
            Text(text = bluetoothName)
            Text(text = bluetoothAddress)
        }
    }
}

@Composable
fun Section2(
    getPairedDevices: (context: Context) -> Unit,
    PairedDevices: ArrayList<BluetoothDevice>,
    saveSelectedDevice: (device: BluetoothDevice) -> Unit,
    context: Context,
) {

    getPairedDevices(context)

    // Lazy Column list of all paired bluetooth devices
    val pairedDevices: ArrayList<BluetoothDevice> = PairedDevices

    LazyColumn(modifier = Modifier
        .size(300.dp)
        .fillMaxWidth()
        .padding(horizontal = 16.dp)) {
        items(pairedDevices) { pairedDevice ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clickable {
                        saveSelectedDevice(pairedDevice)
                        Log.d("Row1", "Clicked $pairedDevice")
                    },
                horizontalArrangement = Arrangement.Center,
            ) {
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Toast.makeText(context, "Enable connect permission", Toast.LENGTH_SHORT).show()
                }
                Text(text = pairedDevice.name.toString())
                Text(text = pairedDevice.address)
            }
        }
    }
}

@Composable
fun FAB(contentPadding: PaddingValues) {

    val initText = R.string.startallblue_service
    val elseText = R.string.stopallblue_service

    var btnText by remember {
        mutableStateOf(initText)
    }

    Row(
        modifier = Modifier
            .fillMaxHeight(1f)
            .fillMaxWidth()
            .padding(contentPadding),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        androidx.compose.material.ExtendedFloatingActionButton(text = {
        Text(text = stringResource(id = btnText))
        }, onClick = {
            btnText = if (btnText == initText) {
                elseText
            } else {
                initText
            }
        })
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {

    val context = LocalContext.current

    Main(
        context,
        name = UserDevice().name,
        address = UserDevice().address,
        getPairedDevices = {},
        PairedDevices = BluetoothState().pairedDevices,
        saveSelectedDevice = {},
    ) {}
}