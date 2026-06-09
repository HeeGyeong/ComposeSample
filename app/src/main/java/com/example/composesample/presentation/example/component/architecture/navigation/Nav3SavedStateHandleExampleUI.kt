package com.example.composesample.presentation.example.component.architecture.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

/**
 * Nav3 SavedStateHandle 크래시 & 복원 예제
 *
 * Navigation 3 의 백스택은 State<List<NavKey>> 이며, 프로세스 종료(process death) 복원을 위해
 * 각 NavKey 는 SavedState(Bundle)로 직렬화/역직렬화된다.
 * 이때 NavKey 에 "직렬화 불가능한 복합 객체"를 담으면 복원 단계에서 역직렬화가 실패하며 크래시가 난다.
 *
 * 원문은 Hilt Assisted Injection 기반이지만, 본 예제는 라이브러리 의존성(Nav3 alpha / Hilt) 없이
 * 순수 Compose 상태로 "직렬화 → 프로세스 종료 → 역직렬화" 흐름만 시뮬레이션하고,
 * 해결책에서는 본 프로젝트 컨벤션인 Koin Repository 주입 패턴으로 각색한다.
 *
 * 1) ❌ 복합 객체를 키에 담기: 람다/런타임 필드를 가진 UserProfile 을 NavKey 에 직접 담아 복원 실패 재현
 * 2) ✅ id + Repository: 식별자(String)만 키에 담고, SavedStateHandle 의 id 로 Repository 에서 객체 재조회
 */
@Composable
fun Nav3SavedStateHandleExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Nav3 SavedStateHandle 크래시 & 복원",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { IntroCard() }
            item { CrashCard() }
            item { SafeRestoreCard() }
        }
    }
}

// ==================== 모델 ====================

// Nav3 백스택에 들어가는 키. process death 복원을 위해 SavedState(Bundle)로 직렬화된다.
private sealed interface DemoNavKey

// ❌ 안티패턴: 복합 객체를 통째로 키에 담는다.
private data class UnsafeDetailKey(val profile: UserProfile) : DemoNavKey

// ✅ 권장: 직렬화 가능한 식별자(primitive)만 담는다.
private data class SafeDetailKey(val userId: String) : DemoNavKey

// 화면에 표시할 복합 객체. onRefresh 같은 런타임 전용 필드는 Bundle 로 직렬화할 수 없다.
private class UserProfile(
    val id: String,
    val name: String,
    val grade: String,
    // 람다(런타임 참조) — process death 시 Bundle 에 쓸 수 없어 복원 실패의 원인이 된다.
    val onRefresh: () -> Unit
)

// 실제 프로젝트에서는 Koin 으로 주입되는 Repository: single { UserRepository() }
// id 만 SavedStateHandle 에 보관하고, 객체는 여기서 다시 조회한다.
private class UserRepository {
    private val users = mapOf(
        "u-100" to UserProfile("u-100", "Alice", "Pro", onRefresh = {}),
        "u-200" to UserProfile("u-200", "Bob", "Free", onRefresh = {})
    )

    fun findById(id: String): UserProfile? = users[id]
}

// SavedStateHandle 는 id(String)만 보관 → VM 이 Repository 로 객체를 다시 만든다.
// 실제: class DetailViewModel(savedStateHandle: SavedStateHandle, repo: UserRepository)
private class DetailViewModel(savedUserId: String, repo: UserRepository) {
    val resolved: UserProfile? = repo.findById(savedUserId)
}

// ==================== 직렬화 시뮬레이션 ====================

// onSaveInstanceState 시뮬레이션: NavKey 를 Bundle 문자열로 인코딩한다.
private fun encodeKey(key: DemoNavKey): String = when (key) {
    // 복합 객체를 직렬화하려면 모든 필드가 직렬화 가능해야 한다.
    // onRefresh 같은 람다는 직렬화 대상에서 빠지므로, 복원 시 객체를 온전히 재구성할 수 없다.
    is UnsafeDetailKey -> "Unsafe|name=${key.profile.name}|onRefresh=<lambda>"
    is SafeDetailKey -> "Safe|userId=${key.userId}"
}

// 복원(역직렬화) 시뮬레이션: 프로세스 재시작 후 Bundle 문자열을 NavKey 로 되돌린다.
private fun decodeKey(encoded: String): DemoNavKey {
    val type = encoded.substringBefore("|")
    return when (type) {
        // 람다/런타임 참조는 복원할 수 없어 역직렬화 단계에서 예외가 발생한다.
        "Unsafe" -> throw RuntimeException(
            "SavedState 역직렬화 실패: UserProfile.onRefresh (kotlin.Function0) 는 " +
                    "Parcelable/Serializable 이 아니어서 복원할 수 없습니다."
        )

        "Safe" -> SafeDetailKey(userId = encoded.substringAfter("userId="))
        else -> throw IllegalStateException("알 수 없는 키: $encoded")
    }
}

// ==================== UI ====================

@Composable
private fun IntroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "💾 NavKey 와 SavedStateHandle",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5E35B1)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Nav3 백스택은 State<List<NavKey>> 입니다. 프로세스가 종료되었다 복원될 때 " +
                        "각 NavKey 는 SavedState(Bundle)로 직렬화/역직렬화됩니다. " +
                        "이때 키에 람다·런타임 객체 같은 직렬화 불가능한 필드를 담으면 복원 단계에서 크래시가 납니다. " +
                        "아래에서 크래시를 재현하고, id + Repository 패턴으로 안전하게 복원합니다.",
                fontSize = 12.sp,
                color = Color(0xFF4527A0),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun CrashCard() {
    // 복합 객체(람다 포함)를 통째로 키에 담는 안티패턴
    val profile = remember { UserProfile("u-100", "Alice", "Pro", onRefresh = {}) }
    var backStack by remember { mutableStateOf<List<DemoNavKey>>(listOf(SafeDetailKey("home"))) }
    var savedState by remember { mutableStateOf<List<String>>(emptyList()) }
    var restoreError by remember { mutableStateOf<String?>(null) }
    var restored by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "❌ 복합 객체를 키에 담기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "UnsafeDetailKey(profile: UserProfile) — 람다 onRefresh 를 가진 객체를 키에 담음",
                fontSize = 11.sp,
                color = Color(0xFFB71C1C)
            )
            Spacer(Modifier.height(12.dp))

            BackStackBox(backStack.size, Color(0xFFC62828))

            Spacer(Modifier.height(8.dp))

            SavedStateBox(savedState, Color(0xFFC62828))

            if (restoreError != null) {
                Spacer(Modifier.height(8.dp))
                CrashBox(restoreError!!)
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        backStack = backStack + UnsafeDetailKey(profile)
                        savedState = emptyList()
                        restoreError = null
                        restored = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Detail 이동", fontSize = 12.sp, color = Color.White)
                }
                Button(
                    onClick = {
                        // 1) onSaveInstanceState: 백스택을 Bundle 로 직렬화
                        savedState = backStack.map { encodeKey(it) }
                        // 2) 프로세스 재시작 후 역직렬화 시도 → 복합 객체에서 크래시
                        restoreError = try {
                            savedState.map { decodeKey(it) }
                            restored = true
                            null
                        } catch (e: Exception) {
                            restored = false
                            "${e::class.java.simpleName}: ${e.message}"
                        }
                    },
                    enabled = backStack.size > 1,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("종료 후 복원", fontSize = 12.sp, color = Color.White)
                }
            }

            Spacer(Modifier.height(8.dp))

            HintBox(
                "💡 'Detail 이동' → '종료 후 복원' 순서로 누르면, Bundle 로 직렬화된 키를 되돌리는 " +
                        "역직렬화 단계에서 onRefresh(람다)를 복원하지 못해 크래시가 납니다.",
                Color(0xFFC62828)
            )
        }
    }
}

@Composable
private fun SafeRestoreCard() {
    val repo = remember { UserRepository() }
    var backStack by remember { mutableStateOf<List<DemoNavKey>>(listOf(SafeDetailKey("home"))) }
    var savedState by remember { mutableStateOf<List<String>>(emptyList()) }
    var restoredProfile by remember { mutableStateOf<UserProfile?>(null) }
    var restoreDone by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "✅ id + Repository 복원",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "SafeDetailKey(userId: String) — 직렬화 가능한 id 만 담고, 객체는 Repository 에서 재조회",
                fontSize = 11.sp,
                color = Color(0xFF0D47A1)
            )
            Spacer(Modifier.height(12.dp))

            BackStackBox(backStack.size, Color(0xFF1565C0))

            Spacer(Modifier.height(8.dp))

            SavedStateBox(savedState, Color(0xFF1565C0))

            if (restoreDone) {
                Spacer(Modifier.height(8.dp))
                ResolvedBox(restoredProfile)
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        // 객체 자체가 아니라 식별자만 키에 담는다.
                        backStack = backStack + SafeDetailKey("u-200")
                        savedState = emptyList()
                        restoredProfile = null
                        restoreDone = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Detail 이동", fontSize = 12.sp, color = Color.White)
                }
                Button(
                    onClick = {
                        // 1) onSaveInstanceState: id 만 직렬화 → 항상 성공
                        savedState = backStack.map { encodeKey(it) }
                        // 2) 역직렬화: SafeDetailKey 복원 성공
                        val keys = savedState.map { decodeKey(it) }
                        // 3) SavedStateHandle 의 id 로 VM 이 Repository 에서 객체 재조회
                        val topKey = keys.last()
                        restoredProfile = if (topKey is SafeDetailKey) {
                            DetailViewModel(topKey.userId, repo).resolved
                        } else {
                            null
                        }
                        restoreDone = true
                    },
                    enabled = backStack.size > 1,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("종료 후 복원", fontSize = 12.sp, color = Color.White)
                }
            }

            Spacer(Modifier.height(8.dp))

            HintBox(
                "💡 키에는 \"u-200\" 같은 id 만 들어가므로 Bundle 직렬화/역직렬화가 항상 안전합니다. " +
                        "복원 후 ViewModel(SavedStateHandle)이 id 로 Repository(Koin)를 조회해 객체를 다시 만듭니다.",
                Color(0xFF1565C0)
            )
        }
    }
}

// ==================== 공용 컴포넌트 ====================

@Composable
private fun BackStackBox(size: Int, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Text(
            text = "백스택 깊이: ${size}개 엔트리",
            fontSize = 11.sp,
            color = color,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
private fun SavedStateBox(savedState: List<String>, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.05f)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                "직렬화된 SavedState(Bundle)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (savedState.isEmpty()) {
                Text("(아직 저장 안 됨)", fontSize = 10.sp, color = Color.Gray)
            } else {
                savedState.forEach { entry ->
                    Text(
                        "• $entry",
                        fontSize = 10.sp,
                        color = Color(0xFF555555),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun CrashBox(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF311B1B), RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                "💥 복원 크래시",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF9A9A)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                message,
                fontSize = 10.sp,
                color = Color(0xFFFFCDD2),
                fontFamily = FontFamily.Monospace,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun ResolvedBox(profile: UserProfile?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1B3320), RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                "✓ 복원 성공 (Repository 재조회)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA5D6A7)
            )
            Spacer(Modifier.height(4.dp))
            if (profile != null) {
                Text(
                    "id=${profile.id} / name=${profile.name} / grade=${profile.grade}",
                    fontSize = 10.sp,
                    color = Color(0xFFC8E6C9),
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Text(
                    "(Repository 에서 사용자를 찾지 못함)",
                    fontSize = 10.sp,
                    color = Color(0xFFC8E6C9),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun HintBox(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Text(text, fontSize = 10.sp, color = color, lineHeight = 14.sp)
    }
}
