package com.example.composesample.presentation.example.component.architecture.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

/**
 * Navigation 3 예제의 실물 카드(#117) — 같은 화면의 시뮬레이션 카드 4개가 흉내 낸 것을 navigation3 1.2.2 로 실제로 돌린다.
 * 백스택은 화면 최상위의 rememberNavBackStack(키를 직렬화해 재생성 · 프로세스 종료 뒤 복원) — 카드가 스크롤로 빠져도 남는다.
 */
@Serializable
sealed interface BasicRealKey : NavKey {
    @Serializable
    data object Home : BasicRealKey

    /** 타입 있는 인자 — 문자열 경로("profile/{id}")를 만들고 다시 파싱하지 않는다 */
    @Serializable
    data class Profile(val userId: String) : BasicRealKey

    @Serializable
    data object Settings : BasicRealKey

    @Serializable
    data class Detail(val itemId: Int) : BasicRealKey
}

/** 백스택 줄에 쓰는 이름 */
internal fun basicRealLabel(key: NavKey): String = when (key) {
    BasicRealKey.Home -> "Home"
    is BasicRealKey.Profile -> "Profile(${key.userId})"
    BasicRealKey.Settings -> "Settings"
    is BasicRealKey.Detail -> "Detail(#${key.itemId})"
    else -> key.toString()
}

private val HomeBlue = Color(0xFF1565C0)
private val ProfilePurple = Color(0xFF6A1B9A)
private val SettingsGray = Color(0xFF455A64)
private val DetailOrange = Color(0xFFE65100)

@Composable
internal fun Navigation3RealCard(backStack: NavBackStack<NavKey>) {
    fun push(key: BasicRealKey) {
        backStack.add(key)
    }

    fun pop() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    RealNavCard(
        title = "NavDisplay · entryProvider · rememberNavBackStack",
        subtitle = "아래 네 카드는 개념 시뮬레이션이다. 이 카드는 navigation3 1.2.2 를 실제로 쓴다 — 백스택은 NavKey 리스트이고, " +
            "NavDisplay 가 맨 위 키에 맞는 entry 를 그린다."
    ) {
        RealNavFrame {
            NavDisplay(
                backStack = backStack,
                onBack = { pop() },
                entryProvider = entryProvider {
                    entry<BasicRealKey.Home> {
                        RealPane("Home", HomeBlue) {
                            Text(text = "앱의 첫 화면", fontSize = 12.sp)
                            RealButton("Profile(user_123) →", HomeBlue) { push(BasicRealKey.Profile("user_123")) }
                            RealButton("Settings →", HomeBlue) { push(BasicRealKey.Settings) }
                        }
                    }
                    entry<BasicRealKey.Profile> { key ->
                        RealPane("Profile", ProfilePurple) {
                            Text(text = "userId = ${key.userId}", fontSize = 12.sp)
                            Text(text = "키의 필드를 그대로 읽는다 — 타입이 틀리면 컴파일이 안 된다", fontSize = 11.sp, color = Color.Gray)
                            RealButtonRow {
                                RealButton("Detail #7 →", ProfilePurple) { push(BasicRealKey.Detail(7)) }
                                RealButton("← 뒤로", SettingsGray) { pop() }
                            }
                        }
                    }
                    entry<BasicRealKey.Settings> {
                        RealPane("Settings", SettingsGray) {
                            Text(text = "인자 없는 화면은 data object 키", fontSize = 12.sp)
                            RealButton("← 뒤로", SettingsGray) { pop() }
                        }
                    }
                    entry<BasicRealKey.Detail> { key ->
                        RealPane("Detail #${key.itemId}", DetailOrange) {
                            Text(text = "같은 화면 종류를 다른 인자로 몇 번이든 쌓을 수 있다", fontSize = 12.sp)
                            RealButtonRow {
                                RealButton("Detail #${key.itemId + 1} →", DetailOrange) { push(BasicRealKey.Detail(key.itemId + 1)) }
                                RealButton("← 뒤로", SettingsGray) { pop() }
                            }
                        }
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        RealInfoLine("백스택", backStack.joinToString(" › ") { basicRealLabel(it) })
        RealInfoLine("크기", "${backStack.size}")
        Spacer(modifier = Modifier.height(4.dp))
        RealButtonRow {
            RealButton("Pop", SettingsGray, enabled = backStack.size > 1) { pop() }
            RealButton("처음으로", SettingsGray, enabled = backStack.size > 1) {
                // 리스트를 직접 고친다 — 여러 개를 한 번에 빼도 NavDisplay 는 새 맨 위로 넘어간다
                while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        RealCaption(
            "• 백스택은 그냥 리스트다 — add · removeAt 으로 바꾸면 NavDisplay 가 따라 그린다(시뮬레이션의 '완전한 제어' 와 같다).\n" +
                "• rememberNavBackStack 은 키를 직렬화해 저장한다 — 다크 모드 전환(액티비티 재생성)과 프로세스 종료 뒤에도 같은 백스택으로 돌아온다. " +
                "그래서 키는 전부 @Serializable + NavKey 다.\n" +
                "• 이 카드의 백스택이 Home 보다 깊으면 시스템 뒤로 가기는 예제를 닫기 전에 이 백스택을 먼저 하나 줄인다(NavDisplay 의 뒤로 가기 처리)."
        )
    }
}
