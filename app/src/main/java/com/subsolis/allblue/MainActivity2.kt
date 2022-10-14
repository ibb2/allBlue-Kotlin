@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)

package com.subsolis.allblue

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
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
import com.adapty.models.PaywallModel
import com.adapty.models.ProductModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider.getCredential
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.subsolis.allblue.Adapty.AdaptyViewModel
import com.subsolis.allblue.Login.LoginViewModel
import com.subsolis.allblue.Login.UserState
import com.subsolis.compose.Material3AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

// Tags
private val TAG_SIGNIN = "Google Sign In"
private val TAG_FIREBASE = "Firebase backend auth"
private val TAG_GOOGLE = "Google Sign In Results"

@AndroidEntryPoint
class MainActivity2 : ComponentActivity() {

    //    val CHANNEL_DEFAULT_IMPORTANCE = "Media Playing Service"
//    val devices_list : ArrayList<BluetoothDevice> = ArrayList()
//    val REQUEST_ENABLE_BT = 1
//    val TAG = "Main Activity"
//    lateinit var binding: ActivityMainBinding
    // Broadcast Receiver Val
    private val br: BroadcastReceiver = com.subsolis.allblue.BroadcastReceiver()

    // Google Sign in
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var signUpRequest: BeginSignInRequest

    // Firebase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase Auth Instantiate
        auth = Firebase.auth

        // Register Broadcast Receiver
        val filter = IntentFilter("com.example.allblue_kotlin.MUSIC_ACTIVE_STATUS_CHANGED").apply {
            addAction("com.example.allblue_kotlin.MUSIC_ACTIVE_STATUS_CHANGED")
        }
        registerReceiver(br, filter)

        setContent {
            val bluetoothViewModel: BluetoothViewModel = viewModel()
            val bluetoothState = bluetoothViewModel.viewState.collectAsState().value
            val context = LocalContext.current
            val activity = LocalContext.current as Activity

            val loginViewModel: LoginViewModel = viewModel()
            val loginState = loginViewModel.viewstate.collectAsState().value

            loginViewModel.loginStatus(auth)

            // Instantiate a model even if blank, of selected bluetooth device
            bluetoothViewModel.getSelectedDevice()

            // Get the name and address of saved device from UserDevice state
            val name = bluetoothState.selectedDevice.name
            val address = bluetoothState.selectedDevice.address

            // Google Onetap Sign in
            oneTapClient = Identity.getSignInClient(context)
            signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true).setServerClientId(getString(R.string.firebase_client_id))
                    .setFilterByAuthorizedAccounts(true).build()).setAutoSelectEnabled(true).build()
            signUpRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true).setServerClientId(getString(R.string.firebase_client_id))
                    .setFilterByAuthorizedAccounts(false).build()).build()

            // Firebase Auth

            // Runtime
            val multipleBluetoothPermissionState =
                rememberMultiplePermissionsState(listOf(Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT))

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

            val loginStatus = loginState.LoggedIn

            // Adapty.io ViewModel and States
            val adaptyViewModel: AdaptyViewModel = viewModel()
            val premiumAccess = adaptyViewModel.viewState.value.premiumAccessLevel
            val paywalls: PaywallModel? = adaptyViewModel.viewState.value.paywalls
            val products: List<ProductModel> = adaptyViewModel.viewState.value.products

            Main(
                context,
                activity,
                name,
                address,
                bluetoothViewModel::getPairedDevices,
                bluetoothState.pairedDevices,
                bluetoothViewModel::saveSelectedDevice,
                bluetoothViewModel::startMediaPlayingService,
                bluetoothViewModel::stopMediaPlayingService,
                bluetoothState.serviceActive,
                bluetoothViewModel::getServiceStatus,
                oneTapClient,
                signInRequest,
                signUpRequest,
                auth,
                loginViewModel,
                loginState,
                loginStatus,
                // Adapty.io
                premiumAccess,
                paywalls,
                products,
            )
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
    activity: Activity,
    name: String,
    address: String,
    getPairedDevices: (context: Context) -> Unit,
    PairedDevices: ArrayList<BluetoothDevice>,
    saveSelectedDevice: (device: BluetoothDevice) -> Unit,
    startService: (context: Context) -> Unit,
    stopService: (context: Context) -> Unit,
    serviceState: Boolean?,
    getServiceStatus: () -> Unit,
    oneTapClient: SignInClient,
    signInRequest: BeginSignInRequest,
    signUpRequest: BeginSignInRequest,
    auth: FirebaseAuth,
    loginViewModel: LoginViewModel,
    loginState: UserState,
    loginStatus: Boolean,
    premiumAccess: Boolean,
    paywalls: PaywallModel?,
    products: List<ProductModel>,
) {

    if (loginStatus) {
        LoginScreen(activity, oneTapClient, signInRequest, signUpRequest, loginViewModel, auth)
    } else if (!premiumAccess) {
        SubscriptionUi(activity = activity, premiumAccess, paywalls, products)
    } else {
        MainBody(
            context,
            auth,
            name,
            address,
            getPairedDevices,
            PairedDevices,
            saveSelectedDevice,
            startService,
            stopService,
            serviceState,
            getServiceStatus,
            oneTapClient,
            loginViewModel,
            loginState,
        )
    }
}

@Composable
fun SubscriptionUi(
    activity: Activity,
    premiumAccess: Boolean,
    paywalls: PaywallModel?,
    products: List<ProductModel>,
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(), content = { padding ->
            Column(verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier
                    .padding(padding)
                    .fillMaxHeight(0.2f))
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "allBlue Premium Access",
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center)
                    Text(text = "Get access to the service provided by the allBlue.",
                        textAlign = TextAlign.Center)
                    Text(text = "${products.firstOrNull()?.localizedFreeTrialPeriod} trial, no commitment.",
                        textAlign = TextAlign.Center)
                    Text(text = "Cancel whenever.", textAlign = TextAlign.Center)
                }

                Spacer(modifier = Modifier.fillMaxHeight(0.6f))

                LazyColumn(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    items(products) { product ->
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(padding)
                        ) {
                            Text(text = "${product.currencyCode.toString()} ${product.localizedPrice} / ${product.localizedSubscriptionPeriod}",
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center)
                            ExtendedFloatingActionButton(modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                                onClick = { /*TODO*/ }) {
                                Text(text = "Subscribe")
                            }
                        }

                    }
                }
            }
        })
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainBody(
    context: Context,
    auth: FirebaseAuth,
    name: String,
    address: String,
    getPairedDevices: (context: Context) -> Unit,
    PairedDevices: ArrayList<BluetoothDevice>,
    saveSelectedDevice: (device: BluetoothDevice) -> Unit,
    startService: (context: Context) -> Unit,
    stopService: (context: Context) -> Unit,
    serviceState: Boolean?,
    getServiceStatus: () -> Unit,
    oneTapClient: SignInClient,
    loginViewModel: LoginViewModel,
    loginState: UserState,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        ModalDrawerSheet {
            Column(verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 32.dp)) {
                Text(text = "Menu", fontSize = 32.sp, modifier = Modifier.padding(start = 30.dp))


                Column() {
                    Text(text = "Account Management",
                        modifier = Modifier.padding(start = 30.dp, top = 32.dp, end = 32.dp))
                    Spacer(Modifier.height(12.dp))

                    NavigationDrawerItem(icon = {
                        Icon(painter = painterResource(id = R.drawable.baseline_logout_24),
                            contentDescription = "")
                    }, label = { Text("Sign out") }, selected = false, onClick = {
                        scope.launch {
                            drawerState.close()
                            loginViewModel.signOut(auth, oneTapClient)
                        }
                    }, modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding))
                }
            }
        }
    }, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
        Material3AppTheme {

            // A surface container using the 'background' color from the theme
            Surface(color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()) {
                Scaffold(Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(), topBar = {
                    CenterAlignedTopAppBar(title = { Text(text = "allBlue") }, navigationIcon = {
                        var menuStatus by remember {
                            mutableStateOf(false)
                        }

                        if (drawerState.isClosed) {
                            menuStatus = false
                        }

                        IconButton(onClick = {
                            if (drawerState.isClosed) {
                                menuStatus = true
                                scope.launch { drawerState.open() }
                            } else {
                                menuStatus = false
                                scope.launch { drawerState.close() }
                            }
                        }) {
                            if (menuStatus) {
                                Icon(painter = painterResource(id = R.drawable.round_menu_open_24),
                                    contentDescription = "General settings opened")
                            } else {
                                Icon(painter = painterResource(id = R.drawable.round_menu_24),
                                    contentDescription = "General settings")
                            }
                        }
                    })
                }, content = { contentPadding ->
                    Column(modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .padding(16.dp)) {
                        Section1(name, address, contentPadding)
                        Spacer(modifier = Modifier.padding(20.dp))
                        Section2(getPairedDevices, PairedDevices, saveSelectedDevice, context)
                        FAB(startService,
                            stopService,
                            serviceState,
                            getServiceStatus,
                            context,
                            contentPadding)
                    }
                })
            }
        }
    }
}

enum class CustomDialogPosition {
    BOTTOM, TOP
}

fun Modifier.customDialogModifier(pos: CustomDialogPosition) = layout { measurable, constraints ->

    val placeable = measurable.measure(constraints);
    layout(constraints.maxWidth, constraints.maxHeight) {
        when (pos) {
            CustomDialogPosition.BOTTOM -> {
                placeable.place(0, constraints.maxHeight - placeable.height, 10f)
            }
            CustomDialogPosition.TOP -> {
                placeable.place(0, 120, 10f)
            }
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
    OutlinedCard(modifier = Modifier.fillMaxWidth(), content = {
        Column(modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()) {
            Text(text = stringResource(id = R.string.selected_device),
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary)
            Row(modifier = Modifier
                .padding(vertical = 32.dp)
                .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = bluetoothName,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.width(170.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                Text(text = bluetoothAddress, textAlign = TextAlign.End)
            }
        }
    })
}

@Composable
fun Section2(
    getPairedDevices: (context: Context) -> Unit,
    PairedDevices: ArrayList<BluetoothDevice>,
    saveSelectedDevice: (device: BluetoothDevice) -> Unit,
    context: Context,
) {


    val bluetoothPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        rememberMultiplePermissionsState(listOf(Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT))
    } else {
        null
    }

    val permissionRevoked = (bluetoothPermission?.permissions?.size
        ?: 0) == (bluetoothPermission?.revokedPermissions?.size ?: 0)

    androidx.compose.material3.Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(id = R.string.paired_bluetooth_devices), fontSize = 24.sp)
            Text(text = stringResource(id = R.string.select_device_that_you_want_to_connect_to),
                color = MaterialTheme.colorScheme.secondary)

            if ((!permissionRevoked && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)) || (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)) {

                getPairedDevices(context)

                // Lazy Column list of all paired bluetooth devices
                val pairedDevices: ArrayList<BluetoothDevice> = PairedDevices

                LazyColumn(modifier = Modifier
                    .height(300.dp)
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()) {
                    items(pairedDevices) { pairedDevice ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                saveSelectedDevice(pairedDevice)
                                Log.d("Row1", "Clicked $pairedDevice")
                            }, horizontalArrangement = Arrangement.SpaceBetween) {
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
                Text(text = stringResource(id = R.string.enable_nearby_scanning_permission),
                    modifier = Modifier.padding(vertical = 16.dp))
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

    Row(modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom) {
        ExtendedFloatingActionButton(containerColor = MaterialTheme.colorScheme.tertiary,
            onClick = {
                if (btnText == initText) {
                    btnText = elseText
                    startService(context)
                } else {
                    btnText = initText
                    stopService(context)
                }
            }) {
            Text(text = stringResource(id = btnText), color = MaterialTheme.colorScheme.onTertiary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    activity: Activity,
    oneTapClient: SignInClient,
    signInRequest: BeginSignInRequest,
    signUpRequest: BeginSignInRequest,
    loginViewModel: LoginViewModel,
    auth: FirebaseAuth,
) {

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                Log.e("Credentials", credential.toString())
                val idToken = credential.googleIdToken

                if (idToken != null) {
                    // Got an ID token from Google. Use it to authenticate
                    // with your backend.

                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with Firebase.
                            val firebaseCredential = getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(activity) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG_FIREBASE, "signInWithCredential:success")
                                        loginViewModel.loginStatus(auth)
                                        val user = auth.currentUser
                                        //                                updateUI(user)
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG_FIREBASE,
                                            "signInWithCredential:failure",
                                            task.exception)
                                        //                                updateUI(null)
                                    }
                                }.addOnFailureListener(activity) { e ->
                                    Log.e("FIREBASE", "Failure: $e")
                                }
                        }
                        else -> {
                            // Shouldn't happen.
                            Log.d(TAG_GOOGLE, "No ID token!")
                        }
                    }
                    Log.d("LOG", idToken)
                } else {
                    Log.d("LOG", "Null Token 5")
                }
            } else {
                Log.e("Response", "No results were not ok ${result.resultCode}")
            }
        }

    Material3AppTheme() {
        Scaffold { padding ->
            padding
            Column(modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                val scope = rememberCoroutineScope()
                Surface(
                    onClick = {
                        scope.launch {
                            loginViewModel.signIn(activity,
                                auth,
                                oneTapClient,
                                signInRequest,
                                signUpRequest,
                                launcher)
                            loginViewModel.loginStatus(auth)
                        }
                    },
                    color = MaterialTheme.colorScheme.onPrimary,
                    shadowElevation = 0.dp,
                    shape = RoundedCornerShape(5.dp),
                    border = BorderStroke(width = 1.dp,
                        color = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Row(
                        modifier = Modifier.padding(
                            start = 12.dp,
                            end = 16.dp,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {

                        Icon(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = "SignInButton",
                            tint = androidx.compose.ui.graphics.Color.Unspecified,
                        )

                        Text(text = "Sign in with Google")
                    }
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {

    val context = LocalContext.current
    val activity = LocalContext.current as Activity


    val auth = Firebase.auth

    // Google Onetap Sign in
    val oneTapClient = Identity.getSignInClient(context)
    val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
            .setSupported(true).setServerClientId(context.getString(R.string.firebase_client_id))
            .setFilterByAuthorizedAccounts(true).build()).setAutoSelectEnabled(true).build()
    val signUpRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
            .setSupported(true).setServerClientId(context.getString(R.string.firebase_client_id))
            .setFilterByAuthorizedAccounts(false).build()).build()

    Main(
        context,
        activity,
        name = String(),
        address = String(),
        getPairedDevices = {},
        PairedDevices = ArrayList(),
        saveSelectedDevice = {},
        startService = {},
        stopService = {},
        serviceState = true,
        getServiceStatus = {},
        oneTapClient = oneTapClient,
        signInRequest = signInRequest,
        signUpRequest = signUpRequest,
        auth = auth,
        loginViewModel = viewModel(),
        loginState = UserState(),
        loginStatus = false,
        paywalls = null,
        premiumAccess = false,
        products = emptyList(),
    )
}