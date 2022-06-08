@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.allblue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.compose.Material3AppTheme

class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Main() {
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(content: @Composable () -> Unit) {
    Material3AppTheme() {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background) {
            content()
            Scaffold(
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
                Body()
            }
        }
    }
}

@Composable
fun Body() {
    Column {
        section1()
        section2()
    }
}

@Composable
fun section1() {
    // Display the selected Bluetooth device by user

    Text(text = "Selected Device")
    Text(text = "Bluetooth device name: Mac Address")    
}

@Composable
fun section2() {
    // Lazy Column list of all paired bluetooth devices
    Text(text = "Paired Devices")
    Text(text = "Blah blah blahhhhh")
    Text(text = "Bluetooth device name: Mac Address")
    Text(text = "Bluetooth device name: Mac Address")
    Text(text = "Bluetooth device name: Mac Address")
    Text(text = "Bluetooth device name: Mac Address")
    Text(text = "Bluetooth device name: Mac Address")
    Text(text = "Bluetooth device name: Mac Address")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Main {
    }
}