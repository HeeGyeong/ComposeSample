package com.example.composesample.presentation.example.component.architecture.development.preview

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.composesample.presentation.example.BlogExampleViewModel
import com.example.composesample.presentation.example.component.ui.media.shimmer.ShimmerExampleUI
import com.example.composesample.presentation.legacy.cal.CalViewModel
import com.example.core.navigation.Navigation
import com.example.core.navigation.NavigationInterface

/**
 * 가장 기본적인 Preview 사용 방법
 */
@Composable
fun DynamicColor() {
    Button(onClick = { }) {
        Text(text = "Hellow Word")
    }
}

@Preview
@Composable
fun SquareComposablePreview() {
    MaterialTheme {
        ShimmerExampleUI(onBackButtonClick = {})
    }
}

/**
 * FontScalePreviews 어노테이션
 * 다양한 폰트 크기를 테스트하기 위한 미리보기 설정을 정의합니다.
 */
@Preview(
    name = "small font",
    group = "font scales",
    fontScale = 0.5f // 작은 폰트 크기
)
@Preview(
    name = "medium font",
    group = "font scales",
    fontScale = 1.0f // 중간 폰트 크기
)
@Preview(
    name = "large font",
    group = "font scales",
    fontScale = 1.5f // 큰 폰트 크기
)
annotation class FontScalePreviews

// FontScalePreviews 어노테이션을 사용하여 다양한 폰트 크기로 "Hello World" 텍스트를 미리보기합니다.
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
        CustomUser("Heegs"),
        CustomUser("Blog"),
        CustomUser("Github")
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

/**
 * Preview에서 viewModel을 사용하는 방법 1
 *
 * viewModel을 인자로 받아서 사용하기
 */
private class MockNavigationInterface : NavigationInterface {
    override fun changeActivity(context: Context, fromActivity: String?, data: Any?) {
        Log.d("Preview", "preview Dummy changeActivity Call")
    }
}

private val mockNavigation = Navigation(MockNavigationInterface())

@Preview
@Composable
fun ViewModelPreview1(
    calViewModel: CalViewModel = CalViewModel(),
    blogExampleViewModel: BlogExampleViewModel = BlogExampleViewModel(
        navigation = mockNavigation,
        application = Application()
    )
) {
    calViewModel.addCounter()
    val counterData = calViewModel.counter.collectAsStateWithLifecycle().value

    blogExampleViewModel.setPreviewExampleData("Sample Data1")
    val viewModelData = blogExampleViewModel.previewExampleData.collectAsStateWithLifecycle().value

    Column {
        Text("$counterData")
        Text(viewModelData)
    }
}

/**
 * Preview에서 viewModel을 사용하는 방법 2
 *
 * viewModelProvider를 사용하기 : Compose 1.2.0 버전 이상에서만 사용 가능.
 */
class PreviewViewModelProvider : PreviewParameterProvider<BlogExampleViewModel> {
    override val values = sequenceOf(
        BlogExampleViewModel(
            navigation = mockNavigation,
            application = Application()
        )
    )
}

@Preview
@Composable
fun ViewModelPreview2(
    @PreviewParameter(PreviewViewModelProvider::class) blogExampleViewModel: BlogExampleViewModel
) {
    blogExampleViewModel.setPreviewExampleData("Sample Data2")
    val viewModelData = blogExampleViewModel.previewExampleData.collectAsStateWithLifecycle().value

    Text(viewModelData)
}

/**
 * Preview에서 viewModel을 사용하는 방법 3
 *
 * 수동으로 preview 내부에서 viewModel 인스턴스 생성하여 사용하기
 */
@Preview
@Composable
fun ViewModelPreview3() {
    val blogExampleViewModel = BlogExampleViewModel(
        navigation = mockNavigation,
        application = Application()
    )

    blogExampleViewModel.setPreviewExampleData("Sample Data3")
    val viewModelData = blogExampleViewModel.previewExampleData.collectAsStateWithLifecycle().value

    Text(viewModelData)
}


/**
 * Preview에서 viewModel을 사용하는 방법 응용
 *
 * LocalInspectionMode.current를 사용하여 preview 환경을 감지하는 표준 방법.
 * @Preview 환경에서 LocalInspectionMode.current는 자동으로 true가 되므로
 * 커스텀 CompositionLocal 없이도 preview 분기 처리가 가능합니다.
 */
@Composable
fun ViewModelPreview4() {
    val blogExampleViewModel = BlogExampleViewModel(
        navigation = mockNavigation,
        application = Application()
    )
    val viewModelData = blogExampleViewModel.previewExampleData.collectAsStateWithLifecycle().value
    // @Preview 환경에서 자동으로 true — 커스텀 CompositionLocal 불필요
    if (LocalInspectionMode.current) {
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
    // LocalInspectionMode.current가 자동으로 true이므로 별도 Provider 불필요
    ViewModelPreview4()
}

/**
 * LightThemePreview
 * 밝은 테마의 UI 요소를 미리보기합니다.
 */
@Preview(name = "Light Theme", showBackground = true)
@Composable
fun LightThemePreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Column {
            Text("Light Theme", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { }) {
                Text("Click Me")
            }
        }
    }
}

/**
 * DarkThemePreview
 * 어두운 테마의 UI 요소를 미리보기합니다.
 */
@Preview(name = "Dark Theme", showBackground = true)
@Composable
fun DarkThemePreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Column {
            Text("Dark Theme", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { }) {
                Text("Click Me")
            }
        }
    }
}