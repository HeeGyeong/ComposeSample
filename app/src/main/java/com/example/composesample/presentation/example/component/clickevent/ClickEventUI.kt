package com.example.composesample.presentation.example.component.clickevent

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.composesample.util.noRippleClickable
import com.example.composesample.util.singleClickEvent
import com.example.composesample.util.singleClickable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClickEventUI(onBackButtonClick: () -> Unit) {
    LazyColumn(modifier = Modifier.padding(horizontal = 20.dp)) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .background(color = Color.White)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
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

        item {
            val firstClick = remember { mutableStateOf("Button") }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable {
                        if (firstClick.value.length > 10) firstClick.value = "Button"
                        else firstClick.value = firstClick.value + "o"
                    },
                text = firstClick.value,
            )

            val secondClick = remember { mutableStateOf("Button") }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .noRippleClickable {
                        if (secondClick.value.length > 10) secondClick.value = "Button"
                        else secondClick.value = secondClick.value + "o"
                    },
                text = secondClick.value,
            )

            /*Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable(
                        indication = null, // Ripple Effect 제거
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        ...
                    },
                text = "...",
            )*/

            val thirdClick = remember { mutableStateOf("Button") }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .singleClickable {
                        if (thirdClick.value.length > 10) thirdClick.value = "Button"
                        else thirdClick.value = thirdClick.value + "o"
                    },
                text = thirdClick.value,
            )

            val buttonCase = remember { mutableStateOf("Button") }

            singleClickEvent { singleEvent ->
                Button(onClick = {
                    singleEvent.event {
                        if (buttonCase.value.length > 10) buttonCase.value = "Button"
                        else buttonCase.value = buttonCase.value + "o"
                    }
                }) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        text = buttonCase.value,
                    )
                }
            }
        }
    }
}