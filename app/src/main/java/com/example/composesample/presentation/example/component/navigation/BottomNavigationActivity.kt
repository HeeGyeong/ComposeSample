package com.example.composesample.presentation.example.component.navigation

import android.graphics.Rect
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.composesample.presentation.example.BlogExampleViewModel
import com.example.composesample.presentation.legacy.base.SetSystemUI

@ExperimentalAnimationApi
class BottomNavigationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val blogExampleViewModel = viewModel<BlogExampleViewModel>()
            val bottomNavigationBarIndex = remember { mutableStateOf(0) }
            var navigationType by remember { mutableStateOf(false) }

            LaunchedEffect(key1 = Unit, block = {
                blogExampleViewModel.initExampleObject()
            })

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    Box(
                        modifier = Modifier
                            .background(color = Color.White)
                            .padding(top = 10.dp, bottom = 10.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            IconButton(
                                onClick = {
                                    finish()
                                }
                            ) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "")
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            Button(
                                onClick = {
                                    navigationType = navigationType.not()
                                }
                            ) {
                                Text(text = "Navigation Type Change")
                            }
                        }
                    }
                },
                bottomBar = {
                    if (navigationType) {
                        CustomBottomNavigationComponent(
                            clickTabIndex = bottomNavigationBarIndex,
                            onClickHomeTab = {
                                bottomNavigationBarIndex.value = 0
                            },
                            onClickAccountTab = {
                                bottomNavigationBarIndex.value = 1
                            },
                            onClickSettingTab = {
                                bottomNavigationBarIndex.value = 2
                            }
                        )
                    } else {
                        BottomNavigationBar(navController = navController)
                    }
                }
            ) { innerPadding ->
                val keyboard = keyboardAsState()
                val isKeyboardOpened = remember { mutableStateOf(false) }
                val offsetY = remember { Animatable(0f) }

                LaunchedEffect(key1 = keyboard.value) {
                    when (keyboard.value) {
                        Keyboard.Opened -> {
                            isKeyboardOpened.value = true
                        }

                        Keyboard.Closed -> {
                            isKeyboardOpened.value = false
                        }
                    }
                }

                LaunchedEffect(isKeyboardOpened.value) {
                    offsetY.animateTo(
                        targetValue = if (isKeyboardOpened.value) {
                            -20f
                        } else {
                            0f
                        },
                        animationSpec = tween(durationMillis = 300),
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(color = Color.Gray)
                ) {
                    AnimatedContent(
                        targetState = navigationType,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(2000, delayMillis = 90))
                                .togetherWith(fadeOut(animationSpec = tween(200, delayMillis = 90)))
                        },
                        modifier = Modifier.background(color = Color.Yellow),
                        label = "TitleAnimation"
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Now Navigation Type : ${
                                if (!navigationType) {
                                    "Bottom Navigation API"
                                } else {
                                    "Custom Bottom Navigation"
                                }
                            }",
                            textAlign = TextAlign.Center,
                            fontSize = 32.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier
                            .offset(y = offsetY.value.dp)
                    ) {
                        AnimatedVisibility(
                            visible = keyboard.value != Keyboard.Opened,
                            enter = fadeIn(tween(1000, delayMillis = 90)),
                            exit = fadeOut(tween(1000, delayMillis = 1000))
                        ) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 20.dp),
                                text = "123123123123123123123123123123123123123123123123123123123123123123123123123123123",
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 20.dp),
                            text = "456456456456456456456456456456456456456456456456456456456456456456456456456456456",
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 20.dp),
                            text = "789789789789789789789789789789789789789789789789789789789789789789789789789789789",
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (navigationType) {
                        val offsetY = animateDpAsState(
                            targetValue = if (bottomNavigationBarIndex.value == 1) (-20).dp else 0.dp,
                            animationSpec = tween(durationMillis = 500)
                        )

                        when (bottomNavigationBarIndex.value) {
                            0 -> {
                                NavigationView1(
                                    text = "CustomBottomNavi 1",
                                    modifier = Modifier
//                                        .graphicsLayer {
//                                            translationY = offsetY.value.toPx()
//                                        }
                                        .background(color = Color.Green)
                                )
                            }

                            1 -> {
                                NavigationView2(
                                    text = "CustomBottomNavi 2",
                                    modifier = Modifier
                                        .graphicsLayer {
                                            translationY = offsetY.value.toPx()
                                        }
                                        .background(color = Color.Green)
                                )
                            }

                            2 -> {
                                NavigationView3(
                                    text = "CustomBottomNavi 3",
                                    modifier = Modifier
                                        .graphicsLayer {
                                            translationY = offsetY.value.toPx()
                                        }
                                        .background(color = Color.Green)
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.background(color = Color.Red)
                        ) {
                            BottomNavigationAPIComponent(
                                context = this@BottomNavigationActivity,
                                navController = navController,
                            )
                        }
                    }
                }
            }
            SetSystemUI()
        }
    }
}

/**
 * composable API를 사용하여 선언해둔 라우트에 연결.
 *
 * 연결 된 라우트에 선언 된 UI를 보여주게 된다.
 *
 * BottomNavigationBar의 navController.navigate(item.route) 선언한 Route 값이 존재해야한다.
 */
@Composable
fun MainContentComponent(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavigationType.HOME) {
        composable(NavigationType.HOME) { NavigationView1("BottomNavi API 1") }
        composable(NavigationType.SEARCH) { NavigationView2("BottomNavi API 2") }
        composable(NavigationType.PROFILE) { NavigationView3("BottomNavi API 3") }
        composable(NavigationType.SETTINGS) { NavigationView4("BottomNavi API 4") }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    /**
     * Navigation 가능한 tab List
     */
    val navigationTabList = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Profile,
        BottomNavItem.Settings
    )
    var selectedItem by remember { mutableStateOf(0) }

    BottomNavigation(
        backgroundColor = Color.Gray,
        elevation = 8.dp
    ) {
        navigationTabList.forEachIndexed { index, item ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(text = item.title)
                },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index

                    // Tab에 따라서 UI를 변경하기 위해 사용
                    navController.navigate(item.route)
                }
            )
        }
    }
}

enum class Keyboard {
    Opened, Closed
}

@Composable
fun keyboardAsState(): State<Keyboard> {
    val keyboardState = remember { mutableStateOf(Keyboard.Closed) }
    val view = LocalView.current
    DisposableEffect(view) {
        val onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height

            val keypadHeight = screenHeight - rect.bottom
            keyboardState.value = if (keypadHeight > screenHeight * 0.15) {
                Keyboard.Opened
            } else {
                Keyboard.Closed
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)

        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
        }
    }
    return keyboardState
}