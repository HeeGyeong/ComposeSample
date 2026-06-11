package com.example.composesample.presentation.example.component.ui.layout.drawer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.composesample.util.noRippleClickable
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldDrawerExampleUI(
    onBackButtonClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f),
        contentAlignment = Alignment.Center
    ) {
        // M3 Scaffold 에는 scaffoldState/drawerContent 가 없으므로
        // ModalNavigationDrawer 로 감싸고 DrawerState/SnackbarHostState 를 직접 관리한다.
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                ModalDrawerSheet {
                    DrawerContainer(
                        onClickEvent = {
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        }
                    )
                }
            }
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = { Text(text = "scaffold drawerContent") },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.apply {
                                            if (isClosed) open() else close()
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Menu, contentDescription = "drawerIcon")
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    onBackButtonClick.invoke()
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                            }
                        }
                    )
                },
                content = { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sample Contents"
                        )

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Menu, contentDescription = "drawerIcon")
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        "ggg",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "snackBarIcon")
                        }

                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColumnScope.DrawerContainer(
    onClickEvent: () -> Unit,
) {
    DrawerContents(itemTitle = "start item", onClickEvent = onClickEvent)
    DrawerContents(itemTitle = "other item", onClickEvent = onClickEvent)
    DrawerContents(itemTitle = "dummy item", onClickEvent = onClickEvent)
    DrawerContents(itemTitle = "last item", visibleDivider = false, onClickEvent = onClickEvent)

    LazyColumn {
        repeat(10) { index ->
            item {
                DrawerContents(itemTitle = "start item[$index]", onClickEvent = onClickEvent)
                DrawerContents(itemTitle = "other item[$index]", onClickEvent = onClickEvent)
                DrawerContents(itemTitle = "dummy item[$index]", onClickEvent = onClickEvent)
                DrawerContents(
                    itemTitle = "last item[$index]",
                    visibleDivider = false,
                    onClickEvent = onClickEvent
                )
            }
        }
    }
}

@Composable
fun ColumnScope.DrawerContents(
    itemTitle: String,
    visibleDivider: Boolean = true,
    onClickEvent: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .noRippleClickable {
                onClickEvent.invoke()
            }
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = itemTitle
        )
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "",
            tint = Color.Black
        )
    }
    if (visibleDivider) {
        HorizontalDivider()
    }
}