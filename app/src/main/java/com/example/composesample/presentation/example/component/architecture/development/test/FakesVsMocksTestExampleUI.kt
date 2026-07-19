package com.example.composesample.presentation.example.component.architecture.development.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

// ─── 테스트 대상 도메인 (실동작 데모용 최소 모델) ───────────────────────────────

private data class DemoUser(val id: String, val name: String)

private interface UserDao {
    fun getUser(id: String): DemoUser?
    fun insertUser(user: DemoUser)
}

/**
 * Fake: 실제로 동작하는 경량 구현체. 인메모리 Map에 진짜로 저장/조회하므로
 * insert 한 값을 그대로 조회할 수 있음 (production 코드와 동일한 계약을 만족).
 */
private class FakeUserDao : UserDao {
    private val storage = mutableMapOf<String, DemoUser>()
    override fun getUser(id: String): DemoUser? = storage[id]
    override fun insertUser(user: DemoUser) {
        storage[user.id] = user
    }
}

/**
 * Mock: 사전에 프로그래밍(stub)된 응답만 반환. insertUser 호출은 몇 번 호출됐는지만 기록할 뿐
 * 실제 상태에 반영되지 않음 — u1 은 스텁돼 있어 조회되지만, insert 로 새로 넣은 u2 는 조회되지 않음.
 */
private class MockUserDao : UserDao {
    private val stubbedResponses = mapOf("u1" to DemoUser("u1", "Alice"))
    var insertCallCount = 0
        private set

    override fun getUser(id: String): DemoUser? = stubbedResponses[id]
    override fun insertUser(user: DemoUser) {
        insertCallCount++
    }
}

private data class LogEntry(val text: String, val color: Color)

@Composable
fun FakesVsMocksTestExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        MainHeader(title = "Fakes vs Mocks in Testing", onBackIconClicked = onBackEvent)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SectionTitle("1. 기초 개념: 두 종류의 테스트 더블") }
            item {
                ConceptCard(
                    title = "Mock — 상호작용을 검증",
                    description = "특정 메서드가 특정 인자로 몇 번 호출됐는지(verify)를 검증하는 테스트 더블. 반환값은 사전에 프로그래밍(stub)된 값만 나오며, 실제 상태를 저장하지 않음.",
                    color = Color(0xFF4A90D9)
                )
            }
            item {
                ConceptCard(
                    title = "Fake — 상태를 검증",
                    description = "실제로 동작하는 경량 구현체(예: 인메모리 Map). production 코드와 동일한 계약을 만족하므로, insert→get 라운드트립처럼 실제 로직을 그대로 실행해 최종 상태를 검증.",
                    color = Color(0xFF50C878)
                )
            }

            item { Spacer(Modifier.height(4.dp)) }
            item { SectionTitle("2. 라이브 데모: 같은 시나리오, 다른 결과") }
            item { LiveDemoCard() }

            item { Spacer(Modifier.height(4.dp)) }
            item { SectionTitle("3. 브리틀니스(Brittleness) 비교: 리팩터링에 대한 내성") }
            item { BrittlenessCard() }

            item { Spacer(Modifier.height(4.dp)) }
            item { SectionTitle("4. 실제 테스트 코드 비교 (MockK / JUnit)") }
            item { TestCodeCard() }

            item { Spacer(Modifier.height(4.dp)) }
            item { SectionTitle("5. 언제 무엇을 쓸까") }
            item { ComparisonCard() }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ─── 섹션 타이틀 ──────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

// ─── 개념 카드 ────────────────────────────────────────────────────────────────

@Composable
private fun ConceptCard(title: String, description: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(Modifier.height(6.dp))
            Text(text = description, color = Color(0xFFCCCCCC), fontSize = 13.sp, lineHeight = 20.sp)
        }
    }
}

// ─── 라이브 데모 카드: Fake vs Mock 같은 시나리오 실행 ─────────────────────────

@Composable
private fun LiveDemoCard() {
    var resetKey by remember { mutableIntStateOf(0) }
    val fakeDao = remember(resetKey) { FakeUserDao() }
    val mockDao = remember(resetKey) { MockUserDao() }
    val log = remember(resetKey) { mutableStateListOf<LogEntry>() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "동일한 UserDao 시나리오를 Fake / Mock 양쪽에서 실행합니다.",
                color = Color(0xFFAAAAAA),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val fakeResult = fakeDao.getUser("u1")
                        val mockResult = mockDao.getUser("u1")
                        log.add(0, LogEntry("① getUser(\"u1\")  [사전 스텁된 값]", Color(0xFF888888)))
                        log.add(0, LogEntry("   Fake → ${fakeResult?.name ?: "null (아직 insert 안 함)"}", Color(0xFF50C878)))
                        log.add(0, LogEntry("   Mock → ${mockResult?.name ?: "null"}  (stub 목록에 있어 반환됨)", Color(0xFF4A90D9)))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90D9))
                ) {
                    Text(text = "① u1 조회", color = Color.White, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        val newUser = DemoUser("u2", "Bob")
                        fakeDao.insertUser(newUser)
                        mockDao.insertUser(newUser)
                        val fakeResult = fakeDao.getUser("u2")
                        val mockResult = mockDao.getUser("u2")
                        log.add(0, LogEntry("② insertUser(u2, Bob) 후 getUser(\"u2\")", Color(0xFF888888)))
                        log.add(0, LogEntry("   Fake → ${fakeResult?.name ?: "null"}  ✓ 실제 저장됐으므로 조회됨", Color(0xFF50C878)))
                        log.add(0, LogEntry("   Mock → ${mockResult?.name ?: "null"}  ✗ stub에 없어 insert가 반영 안 됨", Color(0xFFFF6B6B)))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF50C878))
                ) {
                    Text(text = "② u2 insert 후 조회", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { resetKey++ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF555555)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "초기화", color = Color.White, fontSize = 12.sp)
            }

            if (log.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFF444444))
                Spacer(Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1A), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    log.forEach { entry ->
                        Text(
                            text = entry.text,
                            color = entry.color,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── 브리틀니스 비교 카드 ─────────────────────────────────────────────────────

@Composable
private fun BrittlenessCard() {
    var result by remember { mutableStateOf<Pair<Boolean, Boolean>?>(null) } // (mockVerifyPassed, fakeStatePassed)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "\"리팩터링\": insertUser 를 1회 대신 2회 호출하도록 저장 로직을 변경(최종 상태는 동일).",
                color = Color(0xFFAAAAAA),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    val fakeDao = FakeUserDao()
                    val mockDao = MockUserDao()
                    val user = DemoUser("u3", "Carol")

                    // 리팩터링 후 저장 로직: insertUser 를 2번 호출 (예: 캐시 무효화용)
                    fakeDao.insertUser(user)
                    fakeDao.insertUser(user)
                    mockDao.insertUser(user)
                    mockDao.insertUser(user)

                    val mockVerifyPassed = mockDao.insertCallCount == 1 // verify(exactly = 1)
                    val fakeStatePassed = fakeDao.getUser("u3") == user // 상태만 검증

                    result = mockVerifyPassed to fakeStatePassed
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C00)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "리팩터링 시뮬레이션 실행", color = Color.White, fontSize = 12.sp)
            }

            result?.let { (mockVerifyPassed, fakeStatePassed) ->
                Spacer(Modifier.height(10.dp))
                Text(
                    text = if (mockVerifyPassed) "✓ Mock: verify(exactly = 1) 통과" else "✗ Mock: verify(exactly = 1) { insertUser() } 실패 — 실제로는 2번 호출됨",
                    color = if (mockVerifyPassed) Color(0xFF50C878) else Color(0xFFFF6B6B),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (fakeStatePassed) "✓ Fake: assertEquals(user, fakeDao.getUser(\"u3\")) 통과 — 몇 번 호출됐는지 무관" else "✗ Fake: 상태 검증 실패",
                    color = if (fakeStatePassed) Color(0xFF50C878) else Color(0xFFFF6B6B),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ─── 실제 테스트 코드 비교 카드 ───────────────────────────────────────────────

@Composable
private fun TestCodeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Mock (MockK) — 상호작용 검증",
                color = Color(0xFF4A90D9),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            CodeSnippet(
                code = """@Test
fun `저장 시 insertUser 가 1번 호출된다`() {
    val mockDao = mockk<UserDao>(relaxed = true)
    val repository = UserRepository(mockDao)

    repository.saveUser(DemoUser("u3", "Carol"))

    verify(exactly = 1) { mockDao.insertUser(any()) }
}"""
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF444444))
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Fake — 상태 검증",
                color = Color(0xFF50C878),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            CodeSnippet(
                code = """@Test
fun `저장한 유저를 다시 조회할 수 있다`() {
    val fakeDao = FakeUserDao()
    val repository = UserRepository(fakeDao)

    repository.saveUser(DemoUser("u3", "Carol"))

    assertEquals(DemoUser("u3", "Carol"), repository.getUser("u3"))
}"""
            )
        }
    }
}

// ─── 비교 카드 ────────────────────────────────────────────────────────────────

@Composable
private fun ComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2D)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            ComparisonRow("검증 방식", "상호작용 (몇 번, 어떤 인자로 호출됐는지)", "상태 (최종 결과가 무엇인지)")
            ComparisonRow("리팩터링 내성", "낮음 — 구현 세부사항에 결합", "높음 — 공개 계약만 지키면 안 깨짐")
            ComparisonRow("적합한 대상", "외부 시스템 호출 여부 (결제 API, 분석 이벤트)", "데이터 계층 (Repository/DAO) 같은 상태 보유 컴포넌트")
            ComparisonRow("재사용성", "테스트마다 stub 재설정 필요", "여러 테스트에서 동일 인스턴스 재사용 가능")
        }
    }
}

@Composable
private fun ComparisonRow(label: String, mock: String, fake: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = label, color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(text = "Mock: $mock", color = Color(0xFF7B9FFF), fontSize = 12.sp, lineHeight = 17.sp)
        Text(text = "Fake: $fake", color = Color(0xFF88CC88), fontSize = 12.sp, lineHeight = 17.sp)
    }
}

// ─── 코드 스니펫 표시 ─────────────────────────────────────────────────────────

@Composable
private fun CodeSnippet(code: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A), RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Text(
            text = code,
            color = Color(0xFFE0E0E0),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 17.sp
        )
    }
}
