package com.example.composesample.main

import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composesample.R
import com.example.composesample.example.BlogExampleActivity
import com.example.composesample.ui.base.BottomBar
import com.example.composesample.ui.base.DrawerItem
import com.example.composesample.ui.base.TopBar
import com.example.composesample.ui.theme.ComposeSampleTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@ExperimentalAnimationApi
@Composable
fun AppbarSample(
    title: String,
    isViewLegacyPage: MutableState<Boolean>
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopBar(title, scaffoldState, scope)
        },
        bottomBar = {
            BottomBar()
        },
        // Top, Bottom 사이에 들어갈 item
        content = { padding ->
            ListTest(
                modifier = Modifier.padding(padding),
                itemList = itemList,
                data = "data",
                isViewLegacyPage = isViewLegacyPage
            )
        },
        // floating Button
        floatingActionButton = {
            // Make SnackBar Floating Button
            ExtendedFloatingActionButton(
                text = { Text("+") },
                onClick = {
                    scope.launch {
                        val result = scaffoldState.snackbarHostState
                            .showSnackbar(
                                message = "Go to Blog Example List Screen",
                                actionLabel = "Go",
                                // Defaults to SnackbarDuration.Short
                                duration = SnackbarDuration.Short
                            )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                Log.d("FloatingButton", "ActionPerformed~")
                                context.startActivity(
                                    Intent(
                                        context,
                                        BlogExampleActivity::class.java
                                    )
                                )
                            }

                            SnackbarResult.Dismissed -> {
                                Log.d("FloatingButton", "Dismissed~")
                            }
                        }
                    }
                }
            )

            // Navi Drawer Open Floating Button
            /*ExtendedFloatingActionButton(
                text = { Text("A") },
                onClick = {
                    scope.launch {
                        scaffoldState.drawerState.apply {
                            if (isClosed) open() else close()
                        }
                    }
                }
            )*/
        },
        // floating Button 위치.
        floatingActionButtonPosition = FabPosition.Center,
        // button이 bottom과 겹치는지 여부
        isFloatingActionButtonDocked = true,
        // Navi Drawer Layout
        drawerContent = {
            DrawerItem(scaffoldState, scope)
        },
        // Navi Drag 가능 여부.
        drawerGesturesEnabled = false
    )
}

@ExperimentalAnimationApi
@Composable
fun TestButton(
    text: String,
    isViewLegacyPage: MutableState<Boolean>
) {
    Button(
        onClick = {
            Log.d("ComposeLog", "click test Button")
            isViewLegacyPage.value = false
        },
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 12.dp,
            end = 20.dp,
            bottom = 12.dp
        ),
    ) {
        Text("Go to Blog Example List Screen")
    }
}

@ExperimentalAnimationApi
@Composable
fun ListTest(
    modifier: Modifier,
    itemList: List<Message>,
    data: String,
    isViewLegacyPage: MutableState<Boolean>
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 스크롤의 position의 상태를 저장.
        val scrollState = rememberLazyListState()

        LazyColumn(
            state = scrollState,
            modifier = Modifier.padding(bottom = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(itemList) { index, item ->
                if (index == 3) {
                    TestButton(
                        text = data,
                        isViewLegacyPage = isViewLegacyPage
                    )
                } else {
                    CardView(item)
                }
            }

            items(itemList) { item ->
                item.body
            }
        }

        // scrollState.firstVisibleItemIndex > 0 값이 변경될 때 재구성.
        val showButton by remember {
            derivedStateOf {
                scrollState.firstVisibleItemIndex > 0
            }
        }

        var flowShowButton by remember { mutableStateOf(false) }
        LaunchedEffect(scrollState) {
            snapshotFlow { scrollState.firstVisibleItemIndex }
                .map { index -> index > 0 }
                .distinctUntilChanged() // 값이 변경 될 때 수집
                .collect {
                    flowShowButton = it
                }
        }

        if (/*showButton*/flowShowButton) {
            val coroutineScope = rememberCoroutineScope()
            FloatingActionButton(
                backgroundColor = MaterialTheme.colors.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 5.dp, bottom = 60.dp),
                onClick = {
                    coroutineScope.launch {
                        scrollState.scrollToItem(0)
                    }
                }
            ) {
                Text("Top")
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

        val animatedColor = animateColorAsState(
            if (isExpanded) Color.Green else Color.White
        )

        Column(modifier = Modifier
            .clickable {
                coroutineScope.launch {
                    msg.open = !msg.open
                    isExpanded = msg.open
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
//        AppbarSample("itemList")
    }
}