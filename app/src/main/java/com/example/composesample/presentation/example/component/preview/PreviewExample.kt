package com.example.composesample.presentation.example.component.preview

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.composesample.presentation.example.BlogExampleViewModel
import com.example.composesample.presentation.example.component.shimmer.ShimmerExampleUI
import com.example.composesample.presentation.legacy.cal.CalViewModel

/**
 * 가장 기본적인 Preview 사용 방법
 */
@Composable
fun DynamicColor() {
    Button(onClick = { /*TODO*/ }) {
        Text(text = "Hellow Word")
    }
}

@Preview(
    showBackground = true,
)
@Composable
fun SquareComposablePreview() {
    MaterialTheme {
//        DynamicColor()
        ShimmerExampleUI(onBackButtonClick = {})
//        ShimmerItem()
    }
}

/**
 * Annotation 생성하여 사용
 */
@Preview(
    name = "small font",
    group = "font scales",
    fontScale = 0.5f
)
@Preview(
    name = "medium font",
    group = "font scales",
    fontScale = 1.0f
)
@Preview(
    name = "large font",
    group = "font scales",
    fontScale = 1.5f
)
annotation class FontScalePreviews

// Preview 가 아닌 생성한 Annotation 을 사용
@FontScalePreviews
@Composable
fun HelloWorldPreview() {
    Text("Hello World")
}

/**
 * Preview에 Dummy 파라미터 전달.
 */
data class CustomUser(
    val name: String,
)

class UserPreviewParameterProvider : PreviewParameterProvider<CustomUser> {
    override val values = sequenceOf(
        CustomUser("Elise"),
        CustomUser("Frank"),
        CustomUser("Julia")
    )
}

@Composable
fun UserProfileComponent(
    user: CustomUser
) {
    Text("Hello $user")
}

@Preview
@Composable
fun UserProfilePreview(
    @PreviewParameter(UserPreviewParameterProvider::class) user: CustomUser
) {
    UserProfileComponent(user)
}

// val blogExampleViewModel = viewModel<BlogExampleViewModel>()

/**
 * Preview에서 viewModel을 사용하는 방법 1
 */
@Preview
@Composable
fun ViewModelPreview1(
    calViewModel: CalViewModel = CalViewModel(),
    blogExampleViewModel: BlogExampleViewModel = BlogExampleViewModel(application = Application())
) {
    calViewModel.addCounter()
    val counterData = calViewModel.counter.collectAsState().value

    blogExampleViewModel.setPreviewExampleData("Sample Data1")
    val viewModelData = blogExampleViewModel.previewExampleData.collectAsState().value

    Column {
        Text("$counterData")
        Text(viewModelData)
    }
}

/**
 * Preview에서 viewModel을 사용하는 방법 2
 */
class PreviewViewModelProvider : PreviewParameterProvider<BlogExampleViewModel> {
    override val values = sequenceOf(
        BlogExampleViewModel(application = Application())
    )
}

@Preview
@Composable
fun ViewModelPreview2(
    @PreviewParameter(PreviewViewModelProvider::class) blogExampleViewModel: BlogExampleViewModel
) {
    blogExampleViewModel.setPreviewExampleData("Sample Data2")
    val viewModelData = blogExampleViewModel.previewExampleData.collectAsState().value

    Text(viewModelData)
}

/**
 * Preview에서 viewModel을 사용하는 방법 3
 */
@Preview
@Composable
fun ViewModelPreview3() {
    val blogExampleViewModel = BlogExampleViewModel(application = Application())

    blogExampleViewModel.setPreviewExampleData("Sample Data3")
    val viewModelData = blogExampleViewModel.previewExampleData.collectAsState().value

    Text(viewModelData)
}


/**
 * Preview에서 viewModel을 사용하는 방법 응용편
 */
val LocalPreviewMode = compositionLocalOf { false }

@Composable
fun ViewModelPreview4() {
    val blogExampleViewModel = BlogExampleViewModel(application = Application())
    val viewModelData = blogExampleViewModel.previewExampleData.collectAsState().value
    if (LocalPreviewMode.current) {
        // Preview 용 구현
        blogExampleViewModel.setPreviewExampleData("Sample Data4")
        Text(viewModelData)
    } else {
        // 실제 구현
        blogExampleViewModel.setPreviewExampleData("Use UI")
        Text(viewModelData)
    }
}

@Preview
@Composable
fun MyScreenPreview() {
    CompositionLocalProvider(LocalPreviewMode provides true) {
        ViewModelPreview4()
    }
}