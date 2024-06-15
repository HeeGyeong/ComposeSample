package com.example.composesample.ui.base

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.cal.CalActivity
import com.example.composesample.presentation.hash.HashTagActivity
import com.example.composesample.presentation.main.MainActivity
import com.example.composesample.presentation.movie.MovieActivity
import com.example.composesample.presentation.progress.ProgressActivity
import com.example.composesample.presentation.scope.ScopeActivity
import com.example.composesample.presentation.sub.SubActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@Composable
fun TopBar(
    title: String,
    scaffoldState: ScaffoldState,
    scope: CoroutineScope = rememberCoroutineScope(), // 인자가 안넘어오면 생성. 안정성을 위해 추가.
) {
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

                    scope.launch {
                        Toast.makeText(context, "click Toggle", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                val tint by animateColorAsState(
                    if (liked.value) Color.Red
                    else Color.LightGray
                )
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "Favorite description",
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
                        contentDescription = "TopEnd description"
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

                    Divider(thickness = 4.dp)

                    DropdownMenuItem(onClick = {
                        expanded.value = false

                        context.startActivity(Intent(context, ScopeActivity::class.java))
                        context.finish()
                    }) {
                        Text("Scope Activity")
                    }

                    Divider(thickness = 8.dp)

                    DropdownMenuItem(onClick = {
                        expanded.value = false

                        context.startActivity(Intent(context, MovieActivity::class.java))
                        context.finish()
                    }) {
                        Text("Movie Activity")
                    }

                    Divider(thickness = 8.dp)

                    DropdownMenuItem(onClick = {
                        expanded.value = false

                        context.startActivity(Intent(context, HashTagActivity::class.java))
                        context.finish()
                    }) {
                        Text("HashTag Activity")
                    }
                }
            }
        },
        backgroundColor = Color(0xFDCD7F32),
        elevation = AppBarDefaults.TopAppBarElevation
    )
}

@ExperimentalAnimationApi
@Composable
fun DrawerItem(scaffoldState: ScaffoldState, scope: CoroutineScope) {
    val context = LocalContext.current as Activity

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
        Text("Navigation Drawer", modifier = Modifier.padding(top = 13.5.dp))
    }
    Divider()

    Row(modifier = Modifier
        .clickable {
            scope.launch {
                scaffoldState.drawerState.close()
                context.startActivity(Intent(context, ScopeActivity::class.java))
                context.finish()
            }
        }
        .fillMaxWidth()
        .padding(8.dp)
        .padding(start = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Filled.Refresh, contentDescription = "")
        Text(
            text = "Go to Scope Activity",
            fontWeight = FontWeight.Bold
        )
    }

    Row(modifier = Modifier
        .clickable {
            scope.launch {
                context.startActivity(Intent(context, ProgressActivity::class.java))
                context.finish()
                scaffoldState.drawerState.close()
            }
        }
        .fillMaxWidth()
        .padding(8.dp)
        .padding(start = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Filled.Call, contentDescription = "")
        Text(
            text = "Go to progress Activity",
            fontWeight = FontWeight.Bold
        )
    }
}