package com.example.composesample.presentation.example.component.intent

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.parcelize.Parcelize

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun PassingIntentDataExample(
    onBackButtonClick: () -> Unit
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .background(color = Color.White)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = {
                                onBackButtonClick.invoke()
                            }
                        ) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "")
                        }
                    }
                }
            }
        }

        item {
            Button(onClick = {
                val intent =
                    Intent(
                        context,
                        PassingIntentDataActivity::class.java
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("BooleanData", true)
                        putExtra("NumberData", 100)
                        putExtra("StringData", "String Data")
                        putExtra(
                            "ParcelableData", ParcelableDataClass(
                                name = "Parcelable Data",
                                age = 10,
                                isMale = true,
                                address = "Address Data"
                            )
                        )
                    }
                context.startActivity(intent)
            }) { Text(text = "Passing Intent Data Type 1") }

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = {
                val bundle = Bundle().apply {
                    putBoolean("BooleanData", true)
                    putInt("NumberData", 100)
                    putString("StringData", "String Data")
                    putParcelable(
                        "ParcelableData", ParcelableDataClass(
                            name = "Parcelable Data",
                            age = 10,
                            isMale = true,
                            address = "Address Data"
                        )
                    )
                }

                val intent =
                    Intent(
                        context,
                        PassingIntentDataActivity::class.java
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("BundleData", bundle)
                    }
                context.startActivity(intent)

            }) { Text(text = "Passing Intent Data Type 2") }
        }
    }
}