package com.example.composesample.scope

import android.app.Activity
import android.content.Intent
import android.text.format.DateFormat
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.scope.sub.LaunchedEffectActivity
import com.example.composesample.scope.sub.LaunchedEffectViewModel
import com.example.composesample.scope.sub.ProduceStateActivity
import com.example.composesample.scope.sub.RememberCoroutineActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.*


@ExperimentalAnimationApi
@Composable
fun GoSubActivity() {
    val context = LocalContext.current as Activity

    fun getTime(): String {
        return DateFormat.format("hh:mm:ss",
            Date(System.currentTimeMillis())).toString()
    }

    val timerFlow = flow {
        delay(1000)
        emit(getTime())
    }

    val currentTime = timerFlow.collectAsState(initial = getTime())

    Column {
        Text(
            text = currentTime.value,
            style = TextStyle(fontSize = 40.sp)
        )

        Button(
            onClick = {
                context.startActivity(Intent(context, RememberCoroutineActivity::class.java))
                context.finish()
            },
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 12.dp
            ),
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        ) {
            Text("Go RememberCoroutine Activity")
        }

        Button(
            onClick = {
                context.startActivity(Intent(context, LaunchedEffectActivity::class.java))
                context.finish()
            },
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 12.dp
            ),
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        ) {
            Text("Go LaunchedEffect Activity")
        }

        Button(
            onClick = {
                context.startActivity(Intent(context, ProduceStateActivity::class.java))
                context.finish()
            },
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 12.dp
            ),
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
        ) {
            Text("Go ProduceState Activity")
        }
    }
}


/**
 * rememberCoroutineScope() ??? ???????????? ??????,
 * ?????? Composable??? lifeCycle??? ????????????.
 *
 * ?????????, ?????? ????????? ?????? ?????? ??????????????? ?????? ???????????? ?????????
 * rememberCoroutineScope()??? ?????? ???????????? ??????????????? ?????? ?????? ??????.
 *
 * LaunchedScope??? composable function?????? composable function ???????????? ????????? ???????????? ?????????
 * ????????? ???????????? coroutine scope??? ?????? ???????????? rememberCoroutineScope??? ???????????? ?????????.
 * ?????? ?????????, ???????????? ????????? ?????? ?????????.
 */
@Composable
fun CoroutineScreen(
    scaffoldState: ScaffoldState,
    changeState: () -> Unit,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
) {
    Column {
        Button(
            onClick = {
                coroutineScope.launch {
                    scaffoldState.snackbarHostState
                        .showSnackbar("Show Snackbar $coroutineScope")
                }
            },
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 12.dp
            ),
        ) {
            Text("Make SnackBar")
        }

        Button(
            onClick = {
                changeState()
            },
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 12.dp
            ),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Change State")
        }
    }
}

@Composable
fun CoroutineTempScreen() {
    Text("Second Screen")
}

@Composable
fun LaunchedScreen(viewModel: LaunchedEffectViewModel) {

    var textState by remember { mutableStateOf("Default") }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = textState,
            onValueChange = { change ->
                textState = change
                viewModel.onChangeText(textState)
            },
            label = { Text("Input go") }
        )

        RememberUpdateTestText(textState)
    }
}

@Composable
fun RememberUpdateTestText(text: String) {
    val rememberText by remember { mutableStateOf(text) }
        .apply {
            value = text
        }
    val rememberUpdatedText by rememberUpdatedState(text)

    Text("RememberText : $rememberText")
    Text("UpdatedText : $rememberUpdatedText")
}

@Composable
fun ProduceStateScreen(scope: CoroutineScope = rememberCoroutineScope()) {
    var isTimer by remember { mutableStateOf(false) }

    val timer by produceState(initialValue = 0, isTimer) {
        var job: Job? = null
        Log.d("composeLog", "ProduceState .. $value")
        if (isTimer) {
            job = scope.launch {
                while (true) {
                    delay(1000)
                    value++
                }
            }
        }

        // ?????? ??? ??????
        awaitDispose {
            Log.d("composeLog", "ProduceState in awaitDispose .. $value")
            job?.cancel()
        }
    }

    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Time $timer")
            Button(onClick = {
                isTimer = !isTimer
            }) {
                Text(if (isTimer) "Stop" else "Start")
            }
        }
    }
}