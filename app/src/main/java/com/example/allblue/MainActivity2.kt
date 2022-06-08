@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.allblue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.compose.Material3AppTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource

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

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Main {
    }
}