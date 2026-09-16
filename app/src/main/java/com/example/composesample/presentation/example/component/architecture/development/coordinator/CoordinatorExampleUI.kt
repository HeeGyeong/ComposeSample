package com.example.composesample.presentation.example.component.architecture.development.coordinator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.MainHeader
import com.example.composesample.presentation.example.BlogExampleViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CoordinatorExampleUI(
    onBackEvent: () -> Unit,
) {
    // BlogExampleViewModel 은 Koin 이 Navigation·Application 을 주입해 만든다. AndroidX 의
    // viewModel() 기본 팩토리는 <init>(Application) 을 찾다가 NoSuchMethodException 을 던지고,
    // 컴포지션이 조용히 실패해 화면이 비어 버린다(렌더 감사에서 0x0 으로 검출).
    // 지금까지 앱에서 동작한 이유는 BlogExampleActivity 가 같은 스토어에 인스턴스를 미리 만들어 뒀기
    // 때문일 뿐이라, 프로젝트 규약대로 koinViewModel() 로 직접 얻는다.
    val blogExampleViewModel: BlogExampleViewModel = koinViewModel()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Coordinator Example",
            onBackIconClicked = onBackEvent
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    blogExampleViewModel.changeToActivity(
                        context = context,
                        fromActivity = "CoordinatorModuleUI"
                    )
                }
            ) {
                Text(text = "이동하기")
            }
        }
    }
} 