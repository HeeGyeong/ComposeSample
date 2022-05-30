package com.example.composesample

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExtendedFloatingActionButton
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
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.ui.theme.ComposeSampleTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

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

val itemList = listOf(
    Message("A1",
        "b1 app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\""),
    Message("A2",
        "b1 app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\""),
    Message("A3",
        "b1 app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\""),
    Message("A4", "b4"),
    Message("A5", "b5"),
    Message("A6", "b6"),
    Message("A7", "b7"),
    Message("A8", "b8"),
    Message("A9", "b9"),
    Message("A0", "b0"),
    Message("A1", "b1"),
    Message("A2",
        "b1 app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\""),
    Message("A3", "b3"),
    Message("A4", "b4"),
    Message("A5", "b5"),
    Message("A6", "b6"),
    Message("A7", "b7"),
    Message("A8", "b8"),
    Message("A9", "b9"),
    Message("A0",
        "b1 app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\" app:layout_constraintBottom_toBottomOf=\"parent\""),
    Message("A1", "b1"),
    Message("A2", "b2"),
    Message("A3", "b3"),
    Message("A4", "b4"),
    Message("A5", "b5"),
    Message("A6", "b6"),
    Message("A7", "b7"),
    Message("A8", "b8"),
    Message("A9", "b9"),
    Message("A0", "b0"),
)

data class Message(val head: String, val body: String)

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

@Composable
fun AppbarSample(itemList: List<Message>) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val result = remember { mutableStateOf("") }
    val expanded = remember { mutableStateOf(false) }
    val liked = remember { mutableStateOf(true) }

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
                            }) {
                                Text("First Item")
                            }

                            DropdownMenuItem(onClick = {
                                expanded.value = false
                                result.value = "Second item clicked"
                            }) {
                                Text("Second item")
                            }

                            Divider()

                            DropdownMenuItem(onClick = {
                                expanded.value = false
                                result.value = "Third item clicked"
                            }) {
                                Text("Third item")
                            }

                            Divider()

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
                backgroundColor = Color.DarkGray
            ) { }
        },
        content = {
            Box(
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
            }
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
                text = { Text("Open or close drawer") },
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
        //floatingActionButtonPosition = FabPosition.Center,
        // button이 bottom과 겹치는지 여부
        isFloatingActionButtonDocked = true,
        // Navi Drawer Layout
        drawerContent = {
            Row {
                IconButton(
                    onClick = {
                        scope.launch {
                            scaffoldState.drawerState.apply {
                                if (isClosed) open() else close()
                            }
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
        },
        // Navi Drag 가능 여부.
        drawerGesturesEnabled = false
        // Top, Bottom 사이에 들어갈 item
//        content = { ListTest(itemList) }
    )
}

@Composable
fun TestButton(text: String) {
    Button(
        onClick = { Log.d("ComposeLog", "click test Button") },
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

@Composable
fun ListTest(itemList: List<Message>) {
    // 스크롤의 position의 상태를 저장.
    val scrollState = rememberLazyListState()

    LazyColumn(
        state = scrollState,
        modifier = Modifier.padding(bottom = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(itemList) { index, item ->
            if (index == 3) {
                TestButton("Sample")
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
                .border(1.5.dp, MaterialTheme.colors.secondary, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        var isExpanded by remember { mutableStateOf(false) }
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
        )

        Column(modifier = Modifier
            .clickable {
                coroutineScope.launch {
                    isExpanded = !isExpanded
                    Log.d("ComposeLog", "isExpanded ? $isExpanded")
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
                color = surfaceColor,
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