package com.example.coordinator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import org.koin.androidx.compose.koinViewModel

/**
 * Coordinator pattern Flow
 * Module간 이동하는 것이 단순한 UI라면 AAC navigation을 사용하는 것이 바람직.
 * Module간 Activity 간 전환이 발생하기 떄문에 Coordinator Pattern을 적용
 *
 * 1. Core Module - Navigation Interface, Navigation Interface를 매개변수로 가지는 Class(A)생성
 * 2. Core Module - A를 매개변수로 가지는 BaseViewModel 생성 : 모든 ViewModel이 상속받아서 사용, ViewModel에서 화면전환을 위함
 * 3. 각 Module - ViewModel에서 BaseViewModel을 상속 받아서 사용 : 상속을 위해 모든 viewModel이 Class A를 매개 변수로 가짐.
 * 4. App Module - Navigation Interface의 구현체 NavigationImpl 생성 (Coordinator) : 실제 화면 변경을 위한 함수 호출하는 부분
 * 5. 각 viewModel - baseViewModel에 선언한 class A 함수를 호출 -> NavigationImpl(Coordinator)의 changeActivity 호출
 * 6. NavigationImpl(Coordinator)의 changeActivity에서는 각 Module에 선언 되어있는 Activity 변경을 수행하는 함수(Func B)를 호출.
 * 7. 각 Module에서 선언된 Func B를 통해 Activity 전환이 일어남.
 *
 * B Module Activity -> BViewModel을 통해 changeActivity 호출
 * -> 구현체인 App Module의 NavigationImpl(Coordinator)의 changeActivity 호출
 * -> 각 Module의 Func B 호출 -> 실제 화면 전환
 */
class CoordinatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                color = Color.White
            ) {
                val context = LocalContext.current
                val coordinatorViewModel: CoordinatorViewModel = koinViewModel()
                Greeting(
                    name = "Android",
                    onClick = {
                        coordinatorViewModel.changeToActivity(
                            context = context,
                            fromActivity = "MainModuleUI"
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Hello $name!. Click Here !",
        modifier = modifier
            .clickable {
                onClick.invoke()
            }
    )
}

