package com.example.composesample.presentation.example.component.navigation

import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity.INPUT_METHOD_SERVICE
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.composesample.util.noRippleClickable


/**
 * Navigation에 사용되는 UI Component
 */
@Composable
fun ColumnScope.BottomNavigationAPIComponent(
    context: Context,
    navController: NavHostController,
) {
    MainContentComponent(
        navController = navController,
    )

    Spacer(modifier = Modifier.weight(1f))

    Text(
        modifier = Modifier.noRippleClickable {
            val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        },
        text = "클릭하면 키보드가 올라갑니다.\n키보드를 올려서 위치를 확인하세요.",
        color = Color.Black
    )
}

@Composable
fun NavigationView1(text: String) {
    Log.d("navigationLog", "composition Check - 1")
    Column {
        Text(
            text = "[First] Bottom Navigation : $text"
        )
    }
}

@Composable
fun NavigationView2(text: String) {
    Log.d("navigationLog", "composition Check - 2")
    Column {
        Text(
            text = "[Second] Bottom Navigation : $text"
        )
    }
}

@Composable
fun NavigationView3(text: String) {
    Log.d("navigationLog", "composition Check - 3")
    Column {
        Text(
            text = "[Third] Bottom Navigation : $text"
        )
    }
}

@Composable
fun NavigationView4(text: String) {
    Log.d("navigationLog", "composition Check - 4")
    Column {
        Text(
            text = "[Fourth] Bottom Navigation : $text"
        )
    }
}

/**
 * Custom하여 사용하기 위한 Navigation Component
 */
@Composable
fun CustomBottomNavigationComponent(
    clickTabIndex: MutableState<Int> = mutableStateOf(0),
    onClickHomeTab: () -> Unit = { },
    onClickAccountTab: () -> Unit = { },
    onClickSettingTab: () -> Unit = { },
) {
    Column(
        modifier = Modifier
            .height(54.dp)
            .fillMaxWidth()
            .background(color = Color.DarkGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .noRippleClickable {
                        onClickHomeTab.invoke()
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Default.Home,
                    contentDescription = "",
                    tint = if (clickTabIndex.value == 0) {
                        Color.Blue
                    } else {
                        Color.Gray
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "홈",
                    color = if (clickTabIndex.value == 0) {
                        Color.Blue
                    } else {
                        Color.Gray
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(68.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .noRippleClickable {
                        onClickAccountTab.invoke()
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Default.AccountBox,
                    contentDescription = "",
                    tint = if (clickTabIndex.value == 1) {
                        Color.Blue
                    } else {
                        Color.Gray
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "계정",
                    color = if (clickTabIndex.value == 1) {
                        Color.Blue
                    } else {
                        Color.Gray
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(68.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .noRippleClickable {
                        onClickSettingTab.invoke()
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Default.Settings,
                    contentDescription = "",
                    tint = if (clickTabIndex.value == 2) {
                        Color.Blue
                    } else {
                        Color.Gray
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "설정",
                    color = if (clickTabIndex.value == 2) {
                        Color.Blue
                    } else {
                        Color.Gray
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}