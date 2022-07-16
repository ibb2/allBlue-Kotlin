package com.subsolis.allblue.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subsolis.allblue.R
import com.subsolis.compose.Material3AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            Text(text = "Welcome", fontSize = 32.sp)
            MainBody() {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBody(Content: @Composable () -> Unit) {
    Material3AppTheme() {
        Scaffold { padding ->
            padding
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GoogleSignIn()
            }
        }
    }
}

@Composable
fun GoogleSignIn(
) {
    Surface(
        color = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 0.dp,
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier
                .padding(
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

@Preview(showBackground = true)
@Composable
fun Preview() {
    MainBody() {

    }
}
