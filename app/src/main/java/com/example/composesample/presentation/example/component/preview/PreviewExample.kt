package com.example.composesample.presentation.example.component.preview

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.composesample.presentation.example.component.shimmer.ShimmerExampleUI

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
