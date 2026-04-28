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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

/**
 * Recomposition 추적을 위한 카운터 클래스
 *
 * 핵심: Compose State가 아닌 일반 클래스를 사용하여
 * SideEffect에서 카운트를 증가시켜도 recomposition이 발생하지 않음
 */
class RecompositionCounter {
    var count = 0
        private set

    fun increment() {
        count++
    }

    fun reset() {
        count = 0
    }
}

/**
 * Catching Excessive Recompositions in Jetpack Compose with Tests
 *
 * 이 예제에서 배울 수 있는 것:
 * 1. SideEffect를 사용한 Recomposition 추적 방법
 * 2. Compose State vs 일반 객체의 차이
 * 3. @Stable/@Immutable 어노테이션의 효과
 * 4. remember를 통한 최적화
 * 5. derivedStateOf의 올바른 사용법
 * 6. 람다 안정화 기법
 */
@Composable
fun RecompositionTestExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        MainHeader(
            title = "Recomposition Test",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { RecompositionCounterDemoCard() }
            item { ParentChildRecompositionCard() }
            item { UnstableTypesDemoCard() }
            item { StableAnnotationDemoCard() }
            item { RememberOptimizationCard() }
            item { DerivedStateOfCard() }
            item { LambdaStabilityCard() }
        }
    }
}

/**
 * 기본적인 Recomposition 카운터 데모
 *
 * 버튼을 클릭할 때마다:
 * 1. displayCount state가 변경됨
 * 2. 이 Composable이 recomposition됨
 * 3. SideEffect가 실행되어 counter.increment() 호출
 * 4. "Refresh Count" 버튼으로 현재 카운트를 UI에 반영
 */
@Composable
private fun RecompositionCounterDemoCard() {
    // 일반 객체 - SideEffect에서 안전하게 수정 가능
    val counter = remember { RecompositionCounter() }

    // UI 표시용 state - 버튼 클릭 시에만 갱신
    var displayCount by remember { mutableIntStateOf(0) }
    var clickCount by remember { mutableIntStateOf(0) }

    // 이 SideEffect는 Composable이 recomposition될 때마다 실행됨
    // counter는 일반 객체이므로 increment()가 recomposition을 유발하지 않음
    SideEffect {
        counter.increment()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 기본 Recomposition 카운터",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "State 변경 → Recomposition → SideEffect 실행",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1976D2).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Click Count: $clickCount",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                        CounterBadge(
                            count = displayCount,
                            color = Color(0xFF1976D2),
                            label = "Recomposition"
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "실제 카운터 값: ${counter.count}",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { clickCount++ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Click +1", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = { displayCount = counter.count },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Refresh Count", color = Color.White, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        counter.reset()
                        displayCount = 0
                        clickCount = 0
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reset", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 Click 버튼 → Recomposition 발생 → Refresh로 확인",
                fontSize = 11.sp,
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 부모-자식 Recomposition 전파 데모
 *
 * 핵심 개념:
 * - 부모 state 변경 시 자식도 함께 recomposition됨
 * - 자식 state만 변경 시 부모는 recomposition되지 않음
 */
@Composable
private fun ParentChildRecompositionCard() {
    val parentCounter = remember { RecompositionCounter() }
    val childCounter = remember { RecompositionCounter() }

    var parentDisplayCount by remember { mutableIntStateOf(0) }
    var childDisplayCount by remember { mutableIntStateOf(0) }
    var parentState by remember { mutableIntStateOf(0) }

    // 부모 Recomposition 추적
    SideEffect {
        parentCounter.increment()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "👨‍👧 부모-자식 Recomposition 전파",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC2185B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "부모 state 변경 시 자식도 함께 recomposition됩니다.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Parent
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFC2185B).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Parent (State: $parentState)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC2185B)
                        )
                        CounterBadge(
                            count = parentDisplayCount,
                            color = Color(0xFFC2185B),
                            label = "Parent"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Child - 별도 Composable로 분리
                    ChildComposable(
                        counter = childCounter,
                        displayCount = childDisplayCount
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { parentState++ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2185B)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Parent +1", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        parentDisplayCount = parentCounter.count
                        childDisplayCount = childCounter.count
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Refresh", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        parentCounter.reset()
                        childCounter.reset()
                        parentDisplayCount = 0
                        childDisplayCount = 0
                        parentState = 0
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reset", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 Parent +1 클릭 후 Refresh → 둘 다 증가 확인!",
                fontSize = 11.sp,
                color = Color(0xFFC2185B),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ChildComposable(
    counter: RecompositionCounter,
    displayCount: Int
) {
    SideEffect {
        counter.increment()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFF9800).copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Child Composable",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )
            CounterBadge(
                count = displayCount,
                color = Color(0xFFFF9800),
                label = "Child"
            )
        }
    }
}

@Composable
private fun CounterBadge(count: Int, color: Color, label: String = "Recomposition") {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color
    ) {
        Text(
            text = "$label: $count",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

// Unstable class - 매번 새 인스턴스로 인식
data class UnstableUser(
    val name: String,
    val items: List<String> // List는 불안정한 타입
)

// Stable class
@Stable
data class StableUser(
    val name: String,
    val itemCount: Int // primitive는 안정적
)

/**
 * 불안정한 타입 데모
 *
 * List, Map 등 컬렉션 타입은 Compose에서 불안정한 타입으로 인식됩니다.
 * 같은 내용이라도 매번 새 인스턴스로 생성되면 recomposition을 유발합니다.
 */
@Composable
private fun UnstableTypesDemoCard() {
    val counter = remember { RecompositionCounter() }
    var displayCount by remember { mutableIntStateOf(0) }
    var trigger by remember { mutableIntStateOf(0) }

    // 불안정한 타입 - remember 없이 매번 새 리스트 생성
    val unstableUser = UnstableUser(
        name = "John",
        items = listOf("A", "B", "C")
    )

    SideEffect {
        counter.increment()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚠️ 불안정한 타입 문제",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "List, Map 등 컬렉션은 불안정한 타입으로 인식됩니다.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = """
data class UnstableUser(
    val name: String,
    val items: List<String> // ❌ 불안정
)

// 매번 새 인스턴스로 인식
val user = UnstableUser(
    name = "John",
    items = listOf("A", "B", "C")
)
                """.trimIndent(),
                backgroundColor = Color(0xFFC62828).copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UnstableUserDisplay",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        CounterBadge(count = displayCount, color = Color(0xFFC62828))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "User: ${unstableUser.name}, Items: ${unstableUser.items.size}",
                        fontSize = 11.sp,
                        color = Color(0xFF666666)
                    )

                    Text(
                        text = "Trigger: $trigger",
                        fontSize = 11.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { trigger++ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Trigger", color = Color.White, fontSize = 11.sp)
                }

                Button(
                    onClick = { displayCount = counter.count },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Refresh", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        counter.reset()
                        displayCount = 0
                        trigger = 0
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reset", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Immutable
data class ImmutableConfig(
    val theme: String,
    val language: String
)

/**
 * @Stable / @Immutable 어노테이션 데모
 *
 * 이 어노테이션들은 Compose 컴파일러에게 타입의 안정성을 보장합니다.
 * remember로 감싸면 같은 인스턴스가 유지되어 불필요한 recomposition을 방지합니다.
 */
@Composable
private fun StableAnnotationDemoCard() {
    val stableCounter = remember { RecompositionCounter() }
    val immutableCounter = remember { RecompositionCounter() }

    var stableDisplayCount by remember { mutableIntStateOf(0) }
    var immutableDisplayCount by remember { mutableIntStateOf(0) }
    var trigger by remember { mutableIntStateOf(0) }

    // remember로 감싸서 인스턴스 유지
    val stableUser = remember { StableUser(name = "Jane", itemCount = 5) }
    val immutableConfig = remember { ImmutableConfig(theme = "Dark", language = "KO") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "✅ @Stable / @Immutable",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "어노테이션으로 Compose에게 안정성을 보장합니다.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = """
@Stable
data class StableUser(
    val name: String,
    val itemCount: Int // primitive ✅
)

@Immutable
data class ImmutableConfig(
    val theme: String,
    val language: String
)
                """.trimIndent(),
                backgroundColor = Color(0xFF2E7D32).copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    StableUserDisplay(
                        user = stableUser,
                        counter = stableCounter,
                        displayCount = stableDisplayCount
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ImmutableConfigDisplay(
                        config = immutableConfig,
                        counter = immutableCounter,
                        displayCount = immutableDisplayCount
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Trigger: $trigger",
                    fontSize = 11.sp,
                    color = Color(0xFF666666)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { trigger++ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Trigger", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        stableDisplayCount = stableCounter.count
                        immutableDisplayCount = immutableCounter.count
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Refresh", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        stableCounter.reset()
                        immutableCounter.reset()
                        stableDisplayCount = 0
                        immutableDisplayCount = 0
                        trigger = 0
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reset", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 Trigger 후 Refresh → 안정적인 객체는 skip될 수 있음!",
                fontSize = 11.sp,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StableUserDisplay(
    user: StableUser,
    counter: RecompositionCounter,
    displayCount: Int
) {
    SideEffect { counter.increment() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "@Stable User",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = "${user.name}, Items: ${user.itemCount}",
                fontSize = 11.sp,
                color = Color(0xFF666666)
            )
        }
        CounterBadge(count = displayCount, color = Color(0xFF2E7D32), label = "Stable")
    }
}

@Composable
private fun ImmutableConfigDisplay(
    config: ImmutableConfig,
    counter: RecompositionCounter,
    displayCount: Int
) {
    SideEffect { counter.increment() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "@Immutable Config",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Text(
                text = "Theme: ${config.theme}, Lang: ${config.language}",
                fontSize = 11.sp,
                color = Color(0xFF666666)
            )
        }
        CounterBadge(count = displayCount, color = Color(0xFF1976D2), label = "Immutable")
    }
}

/**
 * remember 최적화 데모
 *
 * remember를 사용하면 계산 결과를 캐싱하여
 * 불필요한 재계산을 방지할 수 있습니다.
 */
@Composable
private fun RememberOptimizationCard() {
    val withoutRememberCounter = remember { RecompositionCounter() }
    val withRememberCounter = remember { RecompositionCounter() }

    var withoutRememberDisplay by remember { mutableIntStateOf(0) }
    var withRememberDisplay by remember { mutableIntStateOf(0) }
    var trigger by remember { mutableIntStateOf(0) }

    val items = listOf("Apple", "Banana", "Cherry", "Date", "Elderberry")

    // remember 없이 - 매번 새로 정렬
    val sortedWithout = items.sortedDescending()

    // remember 사용 - 캐싱
    val sortedWith = remember(items) { items.sortedDescending() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💾 remember로 최적화",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "remember를 사용하여 계산 결과를 캐싱합니다.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Without remember
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFC62828).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        SideEffect { withoutRememberCounter.increment() }

                        Text(
                            text = "❌ Without remember",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "items.sorted()",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF666666)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Recompose: $withoutRememberDisplay",
                            fontSize = 10.sp,
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // With remember
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2E7D32).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        SideEffect { withRememberCounter.increment() }

                        Text(
                            text = "✅ With remember",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "remember { sorted() }",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF666666)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Recompose: $withRememberDisplay",
                            fontSize = 10.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { trigger++ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Trigger ($trigger)", color = Color.White, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        withoutRememberDisplay = withoutRememberCounter.count
                        withRememberDisplay = withRememberCounter.count
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Refresh", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        withoutRememberCounter.reset()
                        withRememberCounter.reset()
                        withoutRememberDisplay = 0
                        withRememberDisplay = 0
                        trigger = 0
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reset", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * derivedStateOf 데모
 *
 * derivedStateOf는 다른 State에서 파생된 State를 생성할 때 사용합니다.
 * 원본 State가 변경될 때만 재계산되므로 효율적입니다.
 */
@Composable
private fun DerivedStateOfCard() {
    var items by remember { mutableStateOf(listOf("A", "B", "C")) }
    var query by remember { mutableStateOf("") }

    val derivedCounter = remember { RecompositionCounter() }
    val composableCounter = remember { RecompositionCounter() }

    var derivedDisplay by remember { mutableIntStateOf(0) }
    var composableDisplay by remember { mutableIntStateOf(0) }

    // derivedStateOf 사용 - query나 items가 변경될 때만 재계산
    val filteredDerived by remember(query, items) {
        derivedStateOf {
            derivedCounter.increment()
            items.filter { it.contains(query, ignoreCase = true) }
        }
    }

    SideEffect {
        composableCounter.increment()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1BEE7)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎯 derivedStateOf",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "파생 상태를 효율적으로 계산합니다.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = """
val filtered by remember(query, items) {
    derivedStateOf {
        items.filter { 
            it.contains(query) 
        }
    }
}
                """.trimIndent(),
                backgroundColor = Color(0xFF7B1FA2).copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Items: ${items.joinToString()}",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )

                    Text(
                        text = "Query: \"$query\"",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )

                    Text(
                        text = "Filtered: ${filteredDerived.joinToString()}",
                        fontSize = 12.sp,
                        color = Color(0xFF7B1FA2),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "derivedStateOf: ${derivedDisplay}회",
                            fontSize = 10.sp,
                            color = Color(0xFF7B1FA2)
                        )
                        Text(
                            text = "Composable: ${composableDisplay}회",
                            fontSize = 10.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { query = if (query.isEmpty()) "A" else "" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Query", color = Color.White, fontSize = 10.sp)
                }

                Button(
                    onClick = {
                        items = items + ('A' + items.size).toString()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add", color = Color.White, fontSize = 10.sp)
                }

                Button(
                    onClick = {
                        derivedDisplay = derivedCounter.count
                        composableDisplay = composableCounter.count
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Refresh", color = Color.White, fontSize = 10.sp)
                }

                Button(
                    onClick = {
                        items = listOf("A", "B", "C")
                        query = ""
                        derivedCounter.reset()
                        composableCounter.reset()
                        derivedDisplay = 0
                        composableDisplay = 0
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reset", color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}

/**
 * 람다 안정화 데모
 *
 * 람다를 매번 새로 생성하면 불필요한 recomposition이 발생합니다.
 * remember를 사용하여 람다를 캐싱하면 이를 방지할 수 있습니다.
 */
@Composable
private fun LambdaStabilityCard() {
    val badLambdaCounter = remember { RecompositionCounter() }
    val goodLambdaCounter = remember { RecompositionCounter() }

    var badLambdaDisplay by remember { mutableIntStateOf(0) }
    var goodLambdaDisplay by remember { mutableIntStateOf(0) }
    var trigger by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB2EBF2)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔗 람다 안정화",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00838F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "매번 새로운 람다 인스턴스 생성을 방지합니다.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Bad lambda
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFC62828).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "❌ Bad Lambda",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 매번 새 람다 생성
                        BadLambdaButton(
                            onClick = { /* action */ },
                            counter = badLambdaCounter,
                            displayCount = badLambdaDisplay
                        )
                    }
                }

                // Good lambda
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2E7D32).copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "✅ Good Lambda",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // remember로 안정화
                        val stableOnClick = remember { { /* action */ } }
                        GoodLambdaButton(
                            onClick = stableOnClick,
                            counter = goodLambdaCounter,
                            displayCount = goodLambdaDisplay
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { trigger++ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00838F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Trigger ($trigger)", color = Color.White, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        badLambdaDisplay = badLambdaCounter.count
                        goodLambdaDisplay = goodLambdaCounter.count
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Refresh", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        badLambdaCounter.reset()
                        goodLambdaCounter.reset()
                        badLambdaDisplay = 0
                        goodLambdaDisplay = 0
                        trigger = 0
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reset", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun BadLambdaButton(
    onClick: () -> Unit,
    counter: RecompositionCounter,
    displayCount: Int
) {
    SideEffect { counter.increment() }

    Column {
        Text(
            text = "onClick = { }",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Recompose: $displayCount",
            fontSize = 10.sp,
            color = Color(0xFFC62828)
        )
    }
}

@Composable
private fun GoodLambdaButton(
    onClick: () -> Unit,
    counter: RecompositionCounter,
    displayCount: Int
) {
    SideEffect { counter.increment() }

    Column {
        Text(
            text = "remember { { } }",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Recompose: $displayCount",
            fontSize = 10.sp,
            color = Color(0xFF2E7D32)
        )
    }
}

@Composable
private fun CodeBlock(
    code: String,
    backgroundColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Text(
            text = code,
            modifier = Modifier.padding(12.dp),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = if (backgroundColor == Color(0xFF263238)) Color(0xFFB0BEC5) else Color(
                0xFF333333
            ),
            lineHeight = 14.sp
        )
    }
}
