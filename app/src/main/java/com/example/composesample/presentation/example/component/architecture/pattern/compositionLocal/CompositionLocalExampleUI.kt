package com.example.composesample.presentation.example.component.architecture.pattern.compositionLocal

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel

private const val LOG_TAG = "CompositionLocal"

// CompositionLocal 선언
val LocalUserName = compositionLocalOf { "Default User" }
val LocalUserAge = compositionLocalOf { 0 }
val LocalColor = compositionLocalOf { lightColors() }
val LocalViewModel = compositionLocalOf<CompositionLocalViewModel> { error("No ViewModel found!") }

@Composable
fun CompositionLocalExampleUI(
    onBackButtonClick: () -> Unit
) {
    Log.d(LOG_TAG, "CompositionLocalExampleUI")

    val viewModel: CompositionLocalViewModel = koinViewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Gray)
    ) {
        MainHeader(
            title = "Text Shimmer Example",
            onBackIconClicked = onBackButtonClick
        )

        ExampleDivider(title = "DefaultCompositionLocalUse")
        DefaultCompositionLocalUse()

        ExampleDivider(title = "NoRecompositionCheckCase")
        NoRecompositionCheckCase()

        ExampleDivider(title = "RecompositionCheckCase")
        RecompositionCheckCase()

        ExampleDivider(title = "ColorThemeCase")
        ColorThemeCase()

        ExampleDivider(title = "MultiCompositionLocalUse")
        MultiCompositionLocalUse()

        ExampleDivider(title = "CompositionLocalViewModelCase")
        CompositionLocalViewModelCase(viewModel)

        ExampleDivider(title = "InnerCompositionLocalProvider")
        LocalVariableCompositionLocalProvider()
    }
}

@Composable
fun LocalVariableCompositionLocalProvider() {
    val localVariableData = compositionLocalOf { 0 }

    CompositionLocalProvider(localVariableData provides 40) {
        TextPrintUIWithLocalData(localVariableData)
    }
}

@Composable
fun TextPrintUIWithLocalData(localData: ProvidableCompositionLocal<Int>) {
    val data = localData.current

    Log.d(LOG_TAG, "TextPrintUIWithLocalData[$data]")

    Text(text = "Data: $data")
}

@Composable
fun CompositionLocalViewModelCase(viewModel: CompositionLocalViewModel) {
    Log.d(LOG_TAG, "CompositionLocalViewModel")

    CompositionLocalProvider(LocalViewModel provides viewModel) {
        val compositionLocalViewModel = LocalViewModel.current

        val text = compositionLocalViewModel.compositionLocalTextData.collectAsState().value
        MutableTextPrintUI(text)

        ButtonUI(
            onButtonClick = {
                compositionLocalViewModel.changeTextData("Change Click")
            }
        )
    }
}

@Composable
fun MultiCompositionLocalUse() {
    CompositionLocalProvider(
        LocalUserName provides "Alice",
        LocalUserAge provides 30,
        LocalColor provides lightColors()
    ) {
        UserProfile()
    }
}

@Composable
fun UserProfile() {
    val userName = LocalUserName.current
    val userAge = LocalUserAge.current
    val colors = LocalColor.current

    Column {
        Text(text = "Name: $userName", color = colors.background)
        Text(text = "Age: $userAge", color = colors.primary)
    }
}

@Composable
fun ColorThemeCase() {
    CompositionLocalProvider(LocalColor provides darkColors()) {
        ThemedButton()
    }
}

@Composable
fun ThemedButton() {
    val colors = LocalColor.current
    Button(onClick = {}, colors = ButtonDefaults.buttonColors(contentColor = colors.background)) {
        Text("Themed Button")
    }
}

@Composable
fun DefaultCompositionLocalUse() {
    Log.d(LOG_TAG, "DefaultCompositionLocalUse")
    CompositionLocalProvider(LocalUserName provides "Alice") {
        TextPrintUI()
    }
}

@Composable
fun NoRecompositionCheckCase() {
    Log.d(LOG_TAG, "RecompositionCheckCase")

    var mutableText by remember { mutableStateOf("Mutable") }

    CompositionLocalProvider(LocalUserName provides mutableText) {
        Log.d(LOG_TAG, "CompositionLocalProvider")
        NoRecompositionTextLayer1()
    }

    Spacer(modifier = Modifier.height(10.dp))

    ButtonUI(
        onButtonClick = {
            mutableText = "Recomposition"
        }
    )
}

@Composable
fun RecompositionCheckCase() {
    Log.d(LOG_TAG, "RecompositionCheckCase")

    var mutableText by remember { mutableStateOf("Mutable") }

    CompositionLocalProvider(LocalUserName provides mutableText) {
        Log.d(LOG_TAG, "CompositionLocalProvider")
        RecompositionTextLayer1(mutableText)
    }

    Spacer(modifier = Modifier.height(10.dp))

    ButtonUI(
        onButtonClick = {
            mutableText = "Recomposition"
        }
    )
}

@Composable
fun RecompositionTextLayer1(mutableText: String) {
    Log.d(LOG_TAG, "RecompositionTextLayer1")
    RecompositionTextLayer2(mutableText)
}

@Composable
fun RecompositionTextLayer2(mutableText: String) {
    Log.d(LOG_TAG, "RecompositionTextLayer2")
    RecompositionTextLayer3(mutableText)
    TextPrintUI()
}

@Composable
fun RecompositionTextLayer3(mutableText: String) {
    Log.d(LOG_TAG, "RecompositionTextLayer3")
}

@Composable
fun MutableTextPrintUI(mutableText: String) {
    Log.d(LOG_TAG, "MutableTextPrintUI[$mutableText]")

    Text(text = "InputData : $mutableText!")
}

@Composable
fun NoRecompositionTextLayer1() {
    Log.d(LOG_TAG, "NoRecompositionTextLayer1")
    NoRecompositionTextLayer2()
}

@Composable
fun NoRecompositionTextLayer2() {
    Log.d(LOG_TAG, "NoRecompositionTextLayer2")
    NoRecompositionTextLayer3()
    TextPrintUI()
}

@Composable
fun NoRecompositionTextLayer3() {
    Log.d(LOG_TAG, "NoRecompositionTextLayer3")
}

@Composable
fun TextPrintUI() {
    val userName = LocalUserName.current

    Log.d(LOG_TAG, "TextPrintUI[$userName]")

    Text(text = "InputData : $userName!")
}

@Composable
fun ButtonUI(
    onButtonClick: () -> Unit
) {
    Button(onClick = onButtonClick) {
        Text(text = "Change Text")
    }
}

@Composable
fun ExampleDivider(title: String) {
    Spacer(modifier = Modifier.height(5.dp))
    Text(
        text = "------------------------------------",
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    )
    Text(
        text = title,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.labelSmall,
        color = Color.Black
    )
    Text(
        text = "------------------------------------",
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    )
    Spacer(modifier = Modifier.height(5.dp))
}

@Preview
@Composable
fun ExamplePreview() {
    CompositionLocalExampleUI(onBackButtonClick = { })
}