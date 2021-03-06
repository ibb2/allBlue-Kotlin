@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)

package com.subsolis.allblue

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.subsolis.compose.Material3AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity2 : ComponentActivity() {

    //    val CHANNEL_DEFAULT_IMPORTANCE = "Media Playing Service"
//    val devices_list : ArrayList<BluetoothDevice> = ArrayList()
//    val REQUEST_ENABLE_BT = 1
//    val TAG = "Main Activity"
//    lateinit var binding: ActivityMainBinding
    // Broadcast Receiver Val
    private val br: BroadcastReceiver = com.subsolis.allblue.BroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register Broadcast Receiver
        val filter = IntentFilter("com.example.allblue_kotlin.MUSIC_ACTIVE_STATUS_CHANGED").apply {
            addAction("com.example.allblue_kotlin.MUSIC_ACTIVE_STATUS_CHANGED")
        }
        registerReceiver(br, filter)

        setContent {
            val bluetoothViewModel: BluetoothViewModel = viewModel()
            val bluetoothState = bluetoothViewModel.viewState.collectAsState().value
            val context = LocalContext.current

            // Instantiate a model even if blank, of selected bluetooth device
            bluetoothViewModel.getSelectedDevice()

            // Get the name and address of saved device from UserDevice state
            val name = bluetoothState.selectedDevice.name
            val address = bluetoothState.selectedDevice.address

            // Runtime
            val multipleBluetoothPermissionState =
                rememberMultiplePermissionsState(listOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT)
                )

            if (!multipleBluetoothPermissionState.allPermissionsGranted) {
                SideEffect {
                    multipleBluetoothPermissionState.launchMultiplePermissionRequest()
                }
            }

            val permissionsRevoked =
                multipleBluetoothPermissionState.revokedPermissions.size == multipleBluetoothPermissionState.permissions.size

            LaunchedEffect(key1 = true, block = {
                bluetoothViewModel.createNotificationChannel(context)
            })

            Main(
                context,
                name,
                address,
                bluetoothViewModel::getPairedDevices,
                bluetoothState.pairedDevices,
                bluetoothViewModel::saveSelectedDevice,
                bluetoothViewModel::startMediaPlayingService,
                bluetoothViewModel::stopMediaPlayingService,
                bluetoothState.serviceActive,
                bluetoothViewModel::getServiceStatus,
            ) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(br)
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
    startService: (context: Context) -> Unit,
    stopService: (context: Context) -> Unit,
    serviceState: Boolean?,
    getServiceStatus: () -> Unit,
    Content: @Composable () -> Unit,
) {
    Material3AppTheme {

        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth())
        {
            Content()
            Scaffold(Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
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
                    Column(modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .padding(16.dp)
                    ) {
                        Section1(name, address, contentPadding)
                        Spacer(modifier = Modifier.padding(20.dp))
                        Section2(
                            getPairedDevices,
                            PairedDevices,
                            saveSelectedDevice,
                            context)
                        FAB(startService,
                            stopService,
                            serviceState,
                            getServiceStatus,
                            context,
                            contentPadding)
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
    Spacer(modifier = Modifier.padding(contentPadding))
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth(),
        content = {
            Column(modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()) {
                Text(text = stringResource(id = R.string.selected_device), fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary)
                Row(modifier = Modifier
                    .padding(vertical = 32.dp)
                    .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = bluetoothName,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .width(170.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = bluetoothAddress, textAlign = TextAlign.End)
                }
            }
        }
    )
}

@Composable
fun Section2(
    getPairedDevices: (context: Context) -> Unit,
    PairedDevices: ArrayList<BluetoothDevice>,
    saveSelectedDevice: (device: BluetoothDevice) -> Unit,
    context: Context,
) {


    val bluetoothPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            rememberMultiplePermissionsState(listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT)
            )
        } else {
            null
        }

    val permissionRevoked =
        (bluetoothPermission?.permissions?.size
            ?: 0) == (bluetoothPermission?.revokedPermissions?.size ?: 0)

    androidx.compose.material3.Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = stringResource(id = R.string.paired_bluetooth_devices),
                fontSize = 24.sp)
            Text(text = stringResource(id = R.string.select_device_that_you_want_to_connect_to),
                color = MaterialTheme.colorScheme.secondary)

            if ((!permissionRevoked && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)) || (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)) {

                getPairedDevices(context)

                // Lazy Column list of all paired bluetooth devices
                val pairedDevices: ArrayList<BluetoothDevice> = PairedDevices

                LazyColumn(modifier = Modifier
                    .height(300.dp)
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
                ) {
                    items(pairedDevices) { pairedDevice ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    saveSelectedDevice(pairedDevice)
                                    Log.d("Row1", "Clicked $pairedDevice")
                                },
                            horizontalArrangement = Arrangement.SpaceBetween
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
                            }
                            Text(text = pairedDevice.name,
                                textAlign = TextAlign.Start,
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                                    .width(170.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis)
                            Text(text = pairedDevice.address,
                                textAlign = TextAlign.End,
                                modifier = Modifier.padding(vertical = 16.dp))
                        }
                    }
                }
            } else {
                Text(
                    text = stringResource(id = R.string.enable_nearby_scanning_permission),
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
fun FAB(
    startService: (context: Context) -> Unit,
    stopService: (context: Context) -> Unit,
    serviceState: Boolean?,
    getServiceStatus: () -> Unit,
    context: Context,
    contentPadding: PaddingValues,
) {

    val initText = R.string.startallblue_service
    val elseText = R.string.stopallblue_service

    var btnText: Int

    if (serviceState == true || serviceState == null) {
        btnText = elseText
    } else {
        btnText = initText
    }


    Log.d("Service", "btnText: $btnText")

    Row(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        ExtendedFloatingActionButton(
            containerColor = MaterialTheme.colorScheme.tertiary,
            onClick = {
                if (btnText == initText) {
                    btnText = elseText
                    startService(context)
                } else {
                    btnText = initText
                    stopService(context)
                }
            }) {
            Text(text = stringResource(id = btnText),
                color = MaterialTheme.colorScheme.onTertiary)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
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
        startService = {},
        stopService = {},
        serviceState = BluetoothState().serviceActive,
        getServiceStatus = {},
    ) {}
}