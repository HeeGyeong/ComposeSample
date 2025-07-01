package com.example.composesample.presentation.example.component.system.platform.shortcut

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.composesample.presentation.MainHeader
import com.example.composesample.presentation.example.BlogExampleActivity
import com.example.composesample.util.ConstValue.Companion.ExampleType
import com.example.composesample.util.ConstValue.Companion.IntentType
import com.example.composesample.util.ConstValue.Companion.ShortCutKey
import com.example.composesample.util.createDynamicShortcut
import com.example.composesample.util.createPinShortcut

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ShortcutExampleUI(
    onBackButtonClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        MainHeader(
            title = "Init Test Example",
            onBackIconClicked = onBackButtonClick
        )


        Button(
            onClick = {
                createDynamicShortcut(
                    context = context,
                    intent = Intent(context, BlogExampleActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra(ShortCutKey, "type_dynamic")
                    }
                )
            },
        ) {
            Text("Add Dynamic Shortcut")
        }

        Button(
            onClick = {
                createPinShortcut(
                    context = context,
                    intent = Intent(context, BlogExampleActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra(ShortCutKey, "type_pin")
                    }
                )
            },
        ) {
            Text("Add Pin Shortcut")
        }
    }
}