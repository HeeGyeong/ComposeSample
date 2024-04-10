package com.example.composesample.example.ui.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

@Composable
fun ModalDrawerUI(
    onBackButtonClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val changeAlign = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
            .background(color = Color.White),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(
            if (changeAlign.value) {
                LocalLayoutDirection provides LayoutDirection.Rtl
            } else {
                LocalLayoutDirection provides LayoutDirection.Ltr
            }
        ) {
            ModalDrawer(
                drawerState = drawerState,
                gesturesEnabled = drawerState.isOpen,
                drawerContent = {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        DrawerContainer(
                            onClickEvent = {
                                coroutineScope.launch {
                                    drawerState.close()
                                }
                            }
                        )
                    }
                },
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.open()
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Menu, contentDescription = "drawerIcon")
                            }

                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    onBackButtonClick.invoke()
                                }
                            ) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "")
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Sample Contents",
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                changeAlign.value = changeAlign.value.not()
                            },
                        ) {
                            Text("Change Align")
                        }


                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            )
        }
    }
}