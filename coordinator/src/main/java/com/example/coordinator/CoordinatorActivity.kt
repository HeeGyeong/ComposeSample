package com.example.coordinator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import org.koin.androidx.compose.koinViewModel


class CoordinatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                color = Color.White
            ) {
                val context = LocalContext.current
                val coordinatorViewModel: CoordinatorViewModel = koinViewModel()
                Greeting(
                    name = "Android",
                    onClick = {
                        coordinatorViewModel.changeToActivity(
                            context = context,
                            fromActivity = "MainModuleUI"
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Hello $name!. Click Here !",
        modifier = modifier
            .clickable {
                onClick.invoke()
            }
    )
}

