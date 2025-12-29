package com.example.composesample.presentation.example.component.architecture.development.test

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

/**
 * Catching Excessive Recompositions in Jetpack Compose with Tests
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
            item { UnstableTypesDemoCard() }
            item { StableAnnotationDemoCard() }
            item { RememberOptimizationCard() }
            item { DerivedStateOfCard() }
            item { LambdaStabilityCard() }
        }
    }
}

@Composable
private fun RecompositionCounterDemoCard() {
    var parentCounter by remember { mutableIntStateOf(0) }
    var childCounter by remember { mutableIntStateOf(0) }
    var parentState by remember { mutableIntStateOf(0) }
    var childState by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE3F2FD),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 RecompositionCounter 데모",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SideEffect를 사용하여 Recomposition 횟수를 추적합니다.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Parent Composable
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1976D2).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Track parent recomposition
                    SideEffect {
                        parentCounter++
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Parent Composable",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                        CounterBadge(count = parentCounter, color = Color(0xFF1976D2))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Parent State: $parentState",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Child Composable
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFF9800).copy(alpha = 0.1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            SideEffect {
                                childCounter++
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Child Composable",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                                CounterBadge(count = childCounter, color = Color(0xFFFF9800))
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Child State: $childState",
                                fontSize = 11.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
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
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Parent +1", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = { childState++ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Child +1", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        parentCounter = 0
                        childCounter = 0
                        parentState = 0
                        childState = 0
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9E9E9E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reset", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 Parent 상태 변경 시 Child도 함께 Recompose됩니다!",
                fontSize = 11.sp,
                color = Color(0xFFF44336),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CounterBadge(count: Int, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color
    ) {
        Text(
            text = "Recomposition: $count",
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

@Composable
private fun UnstableTypesDemoCard() {
    var recomposeCount by remember { mutableIntStateOf(0) }
    var trigger by remember { mutableIntStateOf(0) }

    // 불안정한 타입 - 매번 새 리스트 생성
    val unstableUser = UnstableUser(
        name = "John",
        items = listOf("A", "B", "C")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFEBEE),
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

            // 코드 예시
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

            // 데모
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    SideEffect {
                        recomposeCount++
                    }

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
                        CounterBadge(count = recomposeCount, color = Color(0xFFC62828))
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
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFC62828)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Trigger Recompose", color = Color.White, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        recomposeCount = 0
                        trigger = 0
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9E9E9E)),
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

@Composable
private fun StableAnnotationDemoCard() {
    var stableRecomposeCount by remember { mutableIntStateOf(0) }
    var immutableRecomposeCount by remember { mutableIntStateOf(0) }
    var trigger by remember { mutableIntStateOf(0) }

    val stableUser = remember { StableUser(name = "Jane", itemCount = 5) }
    val immutableConfig = remember { ImmutableConfig(theme = "Dark", language = "KO") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E9),
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

            // Stable 데모
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    StableUserDisplay(
                        user = stableUser,
                        onRecompose = { stableRecomposeCount++ }
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    ImmutableConfigDisplay(
                        config = immutableConfig,
                        onRecompose = { immutableRecomposeCount++ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "@Stable: ${stableRecomposeCount}회",
                    fontSize = 11.sp,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "@Immutable: ${immutableRecomposeCount}회",
                    fontSize = 11.sp,
                    color = Color(0xFF1976D2),
                    fontWeight = FontWeight.Medium
                )
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
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Trigger", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        stableRecomposeCount = 0
                        immutableRecomposeCount = 0
                        trigger = 0
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9E9E9E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reset", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 remember로 감싼 안정적인 객체는 불필요한 Recomposition을 방지합니다!",
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
    onRecompose: () -> Unit
) {
    SideEffect { onRecompose() }

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
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF2E7D32))
        )
    }
}

@Composable
private fun ImmutableConfigDisplay(
    config: ImmutableConfig,
    onRecompose: () -> Unit
) {
    SideEffect { onRecompose() }

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
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF1976D2))
        )
    }
}

@Composable
private fun RememberOptimizationCard() {
    var withoutRememberCount by remember { mutableIntStateOf(0) }
    var withRememberCount by remember { mutableIntStateOf(0) }
    var trigger by remember { mutableIntStateOf(0) }

    val items = listOf("Apple", "Banana", "Cherry", "Date", "Elderberry")

    // remember 없이 - 매번 새로 정렬
    val sortedWithout = items.sortedDescending()

    // remember 사용 - 캐싱
    val sortedWith = remember(items) { items.sortedDescending() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF3E0),
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
                        SideEffect { withoutRememberCount++ }

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
                            text = "Recompose: $withoutRememberCount",
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
                        SideEffect { withRememberCount++ }

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
                            text = "Recompose: $withRememberCount",
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
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE65100)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Trigger ($trigger)", color = Color.White, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        withoutRememberCount = 0
                        withRememberCount = 0
                        trigger = 0
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9E9E9E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reset", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DerivedStateOfCard() {
    var items by remember { mutableStateOf(listOf("A", "B", "C")) }
    var query by remember { mutableStateOf("") }
    var derivedCount by remember { mutableIntStateOf(0) }
    var normalCount by remember { mutableIntStateOf(0) }

    // derivedStateOf 사용 - query나 items가 변경될 때만 재계산
    val filteredDerived by remember(query, items) {
        derivedStateOf {
            derivedCount++
            items.filter { it.contains(query, ignoreCase = true) }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE1BEE7),
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
                    SideEffect { normalCount++ }

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
                            text = "derivedStateOf 계산: ${derivedCount}회",
                            fontSize = 10.sp,
                            color = Color(0xFF7B1FA2)
                        )
                        Text(
                            text = "Composable Recompose: ${normalCount}회",
                            fontSize = 10.sp,
                            color = Color(0xFF666666)
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
                    onClick = { query = if (query.isEmpty()) "A" else "" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF7B1FA2)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Toggle Query", color = Color.White, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        items = items + ('A' + items.size).toString()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9C27B0)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add Item", color = Color.White, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        items = listOf("A", "B", "C")
                        query = ""
                        derivedCount = 0
                        normalCount = 0
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9E9E9E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reset", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun LambdaStabilityCard() {
    var badLambdaCount by remember { mutableIntStateOf(0) }
    var goodLambdaCount by remember { mutableIntStateOf(0) }
    var trigger by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFB2EBF2),
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
                            onRecompose = { badLambdaCount++ }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Recompose: $badLambdaCount",
                            fontSize = 10.sp,
                            color = Color(0xFFC62828)
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
                            onRecompose = { goodLambdaCount++ }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Recompose: $goodLambdaCount",
                            fontSize = 10.sp,
                            color = Color(0xFF2E7D32)
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
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00838F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Trigger ($trigger)", color = Color.White, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        badLambdaCount = 0
                        goodLambdaCount = 0
                        trigger = 0
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9E9E9E)),
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
    onRecompose: () -> Unit
) {
    SideEffect { onRecompose() }

    Text(
        text = "onClick = { }",
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF666666)
    )
}

@Composable
private fun GoodLambdaButton(
    onClick: () -> Unit,
    onRecompose: () -> Unit
) {
    SideEffect { onRecompose() }

    Text(
        text = "remember { { } }",
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF666666)
    )
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
            color = if (backgroundColor == Color(0xFF263238)) Color(0xFFB0BEC5) else Color(0xFF333333),
            lineHeight = 14.sp
        )
    }
}

