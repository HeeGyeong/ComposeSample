package com.example.composesample.ui.base

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.composesample.main.MainActivity
import com.example.composesample.cal.CalActivity
import com.example.composesample.progress.ProgressActivity
import com.example.composesample.sub.SubActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@Composable
fun TopBar(title: String, scaffoldState: ScaffoldState, scope: CoroutineScope) {
    val context = LocalContext.current as Activity

    val expanded = remember { mutableStateOf(false) }
    val liked = remember { mutableStateOf(true) }

    TopAppBar(
        title = {
            Text(text = title)
        },
        navigationIcon = {
            // show drawer icon
            IconButton(
                onClick = {
                    scope.launch {
                        scaffoldState.drawerState.apply {
                            if (isClosed) open() else close()
                        }
                    }
                }
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "")
            }
        },
        actions = {
            IconButton(onClick = {
                context.startActivity(Intent(context, MainActivity::class.java))
                context.finish()
            }) {
                Icon(Icons.Filled.Home, contentDescription = "")
            }

            IconToggleButton(
                checked = liked.value,
                onCheckedChange = {
                    liked.value = it
                }
            ) {
                val tint by animateColorAsState(
                    if (liked.value) Color(0xFF7BB661)
                    else Color.LightGray
                )
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "Localized description",
                    tint = tint
                )
            }

            Box(
                Modifier
                    .wrapContentSize(Alignment.TopEnd)
            ) {
                IconButton(onClick = {
                    expanded.value = true
                }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "Localized description"
                    )
                }

                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },
                ) {
                    DropdownMenuItem(onClick = {
                        expanded.value = false

                        context.startActivity(
                            Intent().run {
                                this.putExtra("sample", "data")
                                setClass(context, SubActivity::class.java)
                            }
                        )
                        context.finish()
                    }) {
                        Text("SubActivity")
                    }

                    DropdownMenuItem(onClick = {
                        expanded.value = false

                        context.startActivity(Intent(context, ProgressActivity::class.java))
                        context.finish()
                    }) {
                        Text("ProgressActivity")
                    }

                    Divider()

                    DropdownMenuItem(onClick = {
                        expanded.value = false

                        context.startActivity(Intent(context, CalActivity::class.java))
                        context.finish()
                    }) {
                        Text("CalActivity")
                    }

                    Divider(thickness = 10.dp)

                    DropdownMenuItem(onClick = {
                        expanded.value = false
                    }) {
                        Text("Fourth item")
                    }
                }
            }
        },
        backgroundColor = Color(0xFDCD7F32),
        elevation = AppBarDefaults.TopAppBarElevation
    )
}

@Composable
fun DrawerItem(scaffoldState: ScaffoldState, scope: CoroutineScope) {
    Row {
        IconButton(
            onClick = {
                scope.launch {
                    scaffoldState.drawerState.close()
                }
            },
        ) {
            Row {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "",
                    tint = Color.Black
                )
            }
        }
        Text("Drawer title", modifier = Modifier.padding(11.5.dp))
    }
    Divider()

    Row(modifier = Modifier
        .clickable {
            scope.launch {
                scaffoldState.drawerState.close()
            }
        }
        .fillMaxWidth()
        .padding(8.dp)
        .padding(start = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Filled.Refresh, contentDescription = "")
        Text(
            text = "Refresh",
            fontWeight = FontWeight.Bold
        )
    }
}