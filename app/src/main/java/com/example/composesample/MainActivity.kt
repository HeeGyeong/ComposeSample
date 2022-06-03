package com.example.composesample

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composesample.cal.CalActivity
import com.example.composesample.progress.ProgressActivity
import com.example.composesample.sub.SubActivity
import com.example.composesample.ui.theme.ComposeSampleTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SetSystemUI()
            ComposeSampleTheme {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background) {
                    AppbarSample(itemList)
                }
            }
        }
    }
}

var itemList = listOf(
    Message("A1",
        "b1 app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" " +
                "app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\"", true),
    Message("A2",
        "b1 app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" " +
                "app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\"", true),
    Message("A3",
        "b1 app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" " +
                "app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\"", false),
    Message("A4", "b4", false),
    Message("A5", "b5", false),
    Message("A6", "b6", false),
    Message("A7", "b7", false),
    Message("A8", "b8", false),
    Message("A9", "b9", false),
    Message("A0", "b0", false),
    Message("A1", "b1", false),
    Message("A2",
        "b1 app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" " +
                "app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\"", false),
    Message("A3", "b3", false),
    Message("A4", "b4", false),
    Message("A5", "b5", false),
    Message("A6", "b6", false),
    Message("A7", "b7", false),
    Message("A8", "b8", false),
    Message("A9", "b9", false),
    Message("A0",
        "b1 app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" " +
                "app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\"", true),
    Message("A1", "b1", false),
    Message("A2", "b2", false),
    Message("A3", "b3", false),
    Message("A4", "b4", false),
    Message("A5", "b5", false),
    Message("A6", "b6", false),
    Message("A7", "b7", false),
    Message("A8", "b8", false),
    Message("A9", "b9", false),
    Message("A0", "b0", false),
)

data class Message(val head: String, val body: String, var open: Boolean)

@Composable
fun SetSystemUI() {
    // Gradle .. implementation "com.google.accompanist:accompanist-systemuicontroller:0.17.0"
    val systemUiController = rememberSystemUiController()

    // Top + Bottom System UI
//    systemUiController.setSystemBarsColor(Color.Blue)
    // Top System UI
    systemUiController.setStatusBarColor(Color.Black)
    // Bottom System UI
    systemUiController.setNavigationBarColor(Color.Yellow)
}

@ExperimentalAnimationApi
@Composable
fun AppbarSample(itemList: List<Message>) {
    val context = LocalContext.current

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val result = remember { mutableStateOf("") }
    val expanded = remember { mutableStateOf(false) }
    val liked = remember { mutableStateOf(true) }
    val selectedItem = remember { mutableStateOf("upload") }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Top app bar")
                },
                navigationIcon = {
                    // show drawer icon
                    IconButton(
                        onClick = {
                            result.value = "Drawer icon clicked"
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
                        result.value = " Play icon clicked"
                    }) {
                        Icon(Icons.Filled.Home, contentDescription = "")
                    }

                    IconToggleButton(
                        checked = liked.value,
                        onCheckedChange = {
                            liked.value = it
                            if (liked.value) {
                                result.value = "Liked"
                            } else {
                                result.value = "Unliked"
                            }
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
                            result.value = "More icon clicked"
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
                                result.value = "First item clicked"

                                context.startActivity(
                                    Intent().run {
                                        this.putExtra("sample", "data")
                                        setClass(context, SubActivity::class.java)
                                    }
                                )
                            }) {
                                Text("SubActivity")
                            }

                            DropdownMenuItem(onClick = {
                                expanded.value = false
                                result.value = "Second item clicked"

                                context.startActivity(Intent(context, ProgressActivity::class.java))
                            }) {
                                Text("ProgressActivity")
                            }

                            Divider()

                            DropdownMenuItem(onClick = {
                                expanded.value = false
                                result.value = "Third item clicked"

                                context.startActivity(Intent(context, CalActivity::class.java))
                            }) {
                                Text("CalActivity")
                            }

                            Divider(thickness = 10.dp)

                            DropdownMenuItem(onClick = {
                                expanded.value = false
                                result.value = "Fourth item clicked"
                            }) {
                                Text("Fourth item")
                            }
                        }
                    }
                },
                backgroundColor = Color(0xFDCD7F32),
                elevation = AppBarDefaults.TopAppBarElevation
            )
        },
        bottomBar = {
            BottomAppBar(
                cutoutShape = MaterialTheme.shapes.small.copy(
                    CornerSize(percent = 50)
                ),
                backgroundColor = Color.DarkGray,
                content = {
                    BottomNavigation() {
                        BottomNavigationItem(
                            icon = {
                                Icon(Icons.Filled.Favorite, "")
                            },
                            label = { Text(text = "Favorite") },
                            selected = selectedItem.value == "favorite",
                            onClick = {
                                result.value = "Favorite icon clicked"
                                selectedItem.value = "favorite"
                            },
                            alwaysShowLabel = false
                        )

                        BottomNavigationItem(
                            icon = {
                                Icon(Icons.Filled.Home, "")
                            },
                            label = { Text(text = "Home") },
                            selected = selectedItem.value == "Home",
                            onClick = {
                                result.value = "Home icon clicked"
                                selectedItem.value = "Home"
                            },
                            alwaysShowLabel = false
                        )

                        BottomNavigationItem(
                            icon = {
                                Icon(Icons.Filled.Menu, "")
                            },


                            label = { Text(text = "Menu") },
                            selected = selectedItem.value == "Menu",
                            onClick = {
                                result.value = "Menu icon clicked"
                                selectedItem.value = "Menu"
                            },
                            alwaysShowLabel = false
                        )

                        BottomNavigationItem(
                            icon = {
                                Icon(Icons.Filled.MoreVert, "")
                            },
                            label = { Text(text = "MoreVert") },
                            selected = selectedItem.value == "MoreVert",
                            onClick = {
                                result.value = "MoreVert icon clicked"
                                selectedItem.value = "MoreVert"
                            },
                            alwaysShowLabel = false
                        )
                    }
                }
            )
        },
        // Top, Bottom 사이에 들어갈 item
        content = {
            ListTest(itemList, "data")
            /*Box(
                Modifier
                    .background(Color(0XFFE3DAC9))
                    .padding(16.dp)
                    .fillMaxSize(),
            ) {
                Text(
                    text = result.value,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.align(Alignment.Center)
                )
            }*/
        },
        floatingActionButton = {
            // Empty Floating Button
            /*FloatingActionButton(
                onClick = {
                    Log.d("logggggggging", "floating~")
                }
            ) {

            }*/

            // Make SnackBar Floating Button
            /*ExtendedFloatingActionButton(
                text = { Text("Show snackbar") },
                onClick = {
                    scope.launch {
                        val result = scaffoldState.snackbarHostState
                            .showSnackbar(
                                message = "Snackbar",
                                actionLabel = "Action",
                                // Defaults to SnackbarDuration.Short
                                duration = SnackbarDuration.Short
                            )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                Log.d("logggggggging", "ActionPerformed~")
                            }
                            SnackbarResult.Dismissed -> {
                                Log.d("logggggggging", "Dismissed~")
                            }
                        }
                    }
                }
            )*/

            // Navi Drawer Open Floating Button
            ExtendedFloatingActionButton(
                text = { Text("A") },
                onClick = {
                    scope.launch {
                        scaffoldState.drawerState.apply {
                            if (isClosed) open() else close()
                        }
                    }
                }
            )
        },
        // floating Button 위치.
        floatingActionButtonPosition = FabPosition.Center,
        // button이 bottom과 겹치는지 여부
        isFloatingActionButtonDocked = true,
        // Navi Drawer Layout
        drawerContent = {
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
                        result.value = "Refresh clicked"
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
        },
        // Navi Drag 가능 여부.
        drawerGesturesEnabled = false
    )
}

@ExperimentalAnimationApi
@Composable
fun TestButton(text: String) {
    val context = LocalContext.current
    Button(
        onClick = {
            Log.d("ComposeLog", "click test Button")
            context.startActivity(Intent(context, MainActivity::class.java))
        },
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 12.dp,
            end = 20.dp,
            bottom = 12.dp
        ),

        ) {
        Text("Like : $text")
    }
}

@ExperimentalAnimationApi
@Composable
fun ListTest(itemList: List<Message>, data: String) {
    // 스크롤의 position의 상태를 저장.
    val scrollState = rememberLazyListState()

    LazyColumn(
        state = scrollState,
        modifier = Modifier.padding(bottom = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(itemList) { index, item ->
            if (index == 3) {
                TestButton(data)
            } else {
                CardView(item)
            }
        }
    }
}

@Composable
fun CardView(msg: Message) {
    val coroutineScope = rememberCoroutineScope()

    Row(modifier = Modifier.padding(all = 8.dp)) {
        Image(
            painter = painterResource(R.drawable.profile_picture),
            contentDescription = "Contact profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colors.secondary, CircleShape),
            colorFilter = ColorFilter.tint(
                Color.Yellow,
                BlendMode.ColorBurn
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        var isExpanded by remember { mutableStateOf(msg.open) }
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
        )

        val animatedColor = animateColorAsState(
            if (isExpanded) Color.Green else Color.White
        )

        Column(modifier = Modifier
            .clickable {
                coroutineScope.launch {
                    msg.open = !msg.open
                    isExpanded = msg.open
                    Log.d("ComposeLog", "isExpanded ? ${msg.open}")
                }
            }
            .fillMaxWidth()
        ) {
            Text(
                text = msg.head,
                color = MaterialTheme.colors.secondaryVariant,
                style = MaterialTheme.typography.subtitle2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                elevation = 10.dp,
                color = animatedColor.value,
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

@ExperimentalAnimationApi
@Preview(
    name = "Light Mode",
    showBackground = true
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun DefaultPreview() {
    ComposeSampleTheme {
        AppbarSample(itemList)
    }
}