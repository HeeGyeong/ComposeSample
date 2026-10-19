package com.example.composesample.presentation.example.component.system.deeplink

import android.app.Application
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow
import org.koin.androidx.compose.koinViewModel

/** '실제 수신' 탭의 ViewModel — 설정·기록은 데모 액티비티와 같은 [DeepLinkDemoStore] */
class DeepLinkReceiveViewModel(application: Application) : AndroidViewModel(application) {
    private val store = DeepLinkDemoStore.get(application)

    val settings: StateFlow<DeepLinkSettings> = store.settings
    val log: StateFlow<List<String>> = store.log
    val samples: List<String> = DEEP_LINK_SAMPLES

    fun updateSettings(transform: (DeepLinkSettings) -> DeepLinkSettings) = store.updateSettings(transform)

    fun clearLog() = store.clearLog()
}

/**
 * Dynamic App Links 예제의 '실제 수신' 탭. 다른 탭은 규칙 매칭 시뮬레이션이고 앱 매니페스트에는 받는 쪽이 없었다 —
 * 이 탭은 매니페스트에 등록한 커스텀 스킴 액티비티 두 개(singleTop · standard)로 링크를 실제로 받는다.
 */
@Composable
internal fun DeepLinkReceiveTab() {
    val viewModel: DeepLinkReceiveViewModel = koinViewModel()
    val settings by viewModel.settings.collectAsState()
    val log by viewModel.log.collectAsState()
    val context = LocalContext.current
    val open: (String) -> Unit = { link -> context.startActivity(Intent(Intent.ACTION_VIEW, link.toUri())) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ReceiveCard(
                title = "📥 커스텀 스킴을 실제로 받는다",
                description = "매니페스트의 DeepLinkDemoActivity(launchMode singleTop)가 composesample:// 를, DeepLinkStandardActivity(standard)가 " +
                    "composesample-standard:// 를 받는다. 액티비티는 문자열만 넘기고 해석·백스택·보류는 DeepLinkCore.kt 가 한다.",
                color = Color(0xFFE3F2FD)
            ) {
                SubTitle("콜드 — 데모 액티비티가 없을 때 연다 (onCreate)")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.samples.forEach { link ->
                        ActionButton(link.substringAfter("://"), Color(0xFF1565C0)) { open(link) }
                    }
                }
                SubTitle("standard 액티비티로")
                ActionButton("standard · order/4821", Color(0xFF6A1B9A)) { open("$DEEP_LINK_SCHEME_STANDARD://order/4821") }
            }
        }
        item {
            ReceiveCard(
                title = "⚙️ 설정 — 함정을 켜고 같은 링크를 다시 연다",
                description = "데모 액티비티와 같은 값을 쓴다(SharedPreferences — 앱을 죽인 뒤 링크로 콜드 스타트해도 같은 설정).",
                color = Color(0xFFFFF3E0)
            ) {
                SettingsChips(settings) { viewModel.updateSettings(it) }
            }
        }
        item {
            ReceiveCard(
                title = "🧪 확인할 것",
                description = "각 항목을 켜고 끄며 데모 화면의 백스택과 아래 기록을 본다.",
                color = Color(0xFFE8F5E9)
            ) {
                Note(
                    "① 파서 — 'https 처럼 path 만' 이면 order/4821 이 '알 수 없는 경로: 4821' 이 된다(order 가 host 로 빠짐). " +
                        "기록의 android.net.Uri 줄도 host=order, pathSegments=[4821] 이다."
                )
                Note(
                    "② 콜드 vs 웜 — 데모 화면 안의 버튼은 이미 떠 있는 액티비티로 링크를 보낸다. singleTop 은 onNewIntent 로 같은 인스턴스, " +
                        "standard 는 새 인스턴스(#번호가 늘고 task 는 같다)가 쌓여 뒤로 가면 이전 인스턴스가 다시 나온다."
                )
                Note(
                    "③ 재생성 — 데모 화면을 띄운 채 다크 모드를 바꾼다(이 액티비티는 configChanges 가 없어 재생성된다). " +
                        "'재생성이면 다시 처리 안 함' 을 끄면 같은 intent 를 다시 처리해 백스택에 주문이 한 번 더 쌓인다. " +
                        "다시 처리되는 것은 마지막 웜 링크가 아니라 처음 링크다 — onNewIntent 에서 setIntent 를 하지 않으면 getIntent() 는 처음 intent 그대로다."
                )
                Note(
                    "④ 준비 전 링크 — onCreate 는 setContent 보다 먼저라 백스택을 쥔 화면이 아직 없다. '준비 전 링크 보류' 를 끄면 " +
                        "콜드 링크가 버려져 빈 화면이 된다."
                )
                Note("⑤ 백스택 합성 — 끄면 [목적지] 하나라 뒤로 한 번에 데모 액티비티가 닫힌다. 켜면 [홈, 목적지] 라 뒤로 = 홈.")
                Note("⑥ 잘못된 링크 — order/(번호 없음)·order/abc(형식 아님)·nope(모르는 경로)는 추측하지 않고 '잘못된 링크' 화면으로.")
            }
        }
        item {
            ReceiveCard(
                title = "📏 실측 (Galaxy A72 · API 33, debug·release)",
                description = "기록 카드의 줄을 옮긴 것이다.",
                color = Color(0xFFFFFDE7)
            ) {
                Note(
                    "콜드: onCreate 에서 '내비게이션 준비 전 → 보류' 뒤 첫 컴포지션에서 '보류분 배출' — 백스택 [home, order/4821]. " +
                        "android.net.Uri 도 host=order, pathSegments=[4821]. 보류 칸을 끄면 '버려짐' 과 빈 화면."
                )
                Note(
                    "웜: singleTop 은 같은 인스턴스의 onNewIntent 로 [home, order/4821, order/77] — 뒤로 세 번이면 액티비티가 닫힌다. " +
                        "standard 는 같은 task 에 새 인스턴스가 쌓이고 각자 백스택을 가져, 위 인스턴스를 다 비우면 아래 인스턴스의 주문 4821 이 다시 나온다."
                )
                Note(
                    "재생성: 가드를 켜면 기록에 '재생성이라 다시 처리하지 않음' 이 남고 백스택 [home, 4821, 77] 이 그대로 복원된다. 끄면 " +
                        "[home, 4821, 77, 4821] 로 바뀌어 보던 주문 77 대신 4821 이 뜬다. HOME → am kill → 최근 앱에서 다시 열어도(새 pid) 같은 규칙으로 복원된다."
                )
                Note(
                    "adb: force-stop 뒤 am start -d 는 새 프로세스·새 task 에서 콜드 시작(LaunchState COLD). 떠 있는 동안 보내면 " +
                        "'intent has been delivered to currently running top-most instance' 와 함께 onNewIntent."
                )
            }
        }
        item {
            ReceiveCard(
                title = "💻 adb 로 — 프로세스가 없을 때(진짜 콜드)와 떠 있을 때(웜)",
                description = "앱 밖에서 링크를 보낸다. force-stop 뒤에 보내면 프로세스부터 새로 뜬다(기록의 pid 가 바뀐다).",
                color = Color(0xFFECEFF1)
            ) {
                Code(
                    "adb shell am force-stop com.example.composesample\n" +
                        "adb shell am start -a android.intent.action.VIEW \\\n" +
                        "    -d \"composesample://order/4821\" com.example.composesample\n\n" +
                        "# 데모 화면이 떠 있는 채로 다시 → onNewIntent\n" +
                        "adb shell am start -a android.intent.action.VIEW \\\n" +
                        "    -d \"composesample://profile/kim_01\" com.example.composesample"
                )
            }
        }
        item {
            ReceiveCard(
                title = "📜 수신 기록 (최신이 위, 데모 액티비티가 남긴 것)",
                description = "#번호 = 액티비티 인스턴스, pid = 앱 프로세스.",
                color = Color(0xFFF3E5F5)
            ) {
                ActionButton("기록 지우기", Color(0xFF616161)) { viewModel.clearLog() }
                if (log.isEmpty()) Mono("아직 없음")
                log.take(30).forEach { Mono(it) }
            }
        }
    }
}

@Composable
private fun ReceiveCard(title: String, description: String, color: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(6.dp))
            Text(description, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SubTitle(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF455A64),
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    )
}

@Composable
private fun Note(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x14000000))
            .padding(10.dp),
        fontSize = 12.sp,
        lineHeight = 17.sp,
        color = Color(0xFF424242)
    )
}

@Composable
private fun Code(code: String) {
    Text(
        text = code,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF263238))
            .horizontalScroll(rememberScrollState())
            .padding(10.dp),
        fontSize = 10.sp,
        lineHeight = 14.sp,
        color = Color(0xFFE0E0E0),
        fontFamily = FontFamily.Monospace
    )
}

@Composable
private fun Mono(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = Color(0xFF616161),
        fontFamily = FontFamily.Monospace,
        lineHeight = 15.sp,
        modifier = Modifier.padding(top = 2.dp)
    )
}

@Composable
private fun ActionButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}
