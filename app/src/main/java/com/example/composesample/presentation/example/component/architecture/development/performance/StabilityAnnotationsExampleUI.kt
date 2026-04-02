package com.example.composesample.presentation.example.component.architecture.development.performance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
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

// ===================== 데모용 데이터 클래스 =====================

// 불안정한 클래스: List<T>는 MutableList 구현체도 허용 → Compose 컴파일러가 불안정으로 판단
data class UnstableItem(val name: String, val tags: List<String>)

// @Immutable: 모든 public 프로퍼티가 불변임을 컴파일러에 약속
// Compose는 이 클래스를 사용하는 @Composable을 "스킵 가능(skippable)"으로 마킹
@Immutable
data class ImmutableItem(val name: String, val tags: List<String>)

// @Stable: equals() 결과가 안정적이고 의존 State가 변하면 리컴포지션 알림을 보장
// @Immutable보다 약한 계약 — var 프로퍼티도 허용
@Stable
class StableCounter(initialValue: Int) {
    var count by mutableIntStateOf(initialValue)
}

// ===================== 메인 UI =====================

@Composable
fun StabilityAnnotationsExampleUI(onBackEvent: () -> Unit) {
    // 부모 리컴포지션 트리거용 카운터
    var parentTrigger by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
            Text(
                text = "Stability Annotations",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InfoCard(
                    title = "Compose 안정성(Stability)이란?",
                    description = "Compose 컴파일러는 각 @Composable 파라미터의 '안정성'을 분석합니다.\n" +
                            "파라미터가 변경되지 않으면 리컴포지션을 건너뛸 수 있습니다.\n\n" +
                            "파라미터가 안정적(stable)으로 인식되려면:\n" +
                            "• 기본 타입 (Int, String, Boolean 등)\n" +
                            "• @Stable 또는 @Immutable 어노테이션이 있는 타입\n" +
                            "• Compose의 MutableState<T>\n" +
                            "• 위 조건을 만족하는 타입으로만 구성된 data class",
                    bgColor = Color(0xFFE8F5E9)
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("1. 불안정한 클래스 (항상 리컴포지션)") }

            item {
                CodeCard(
                    code = """// ❌ 불안정: List<T>는 MutableList 구현 가능 → 불안정
data class UnstableItem(
    val name: String,
    val tags: List<String>  // ← Compose가 불안정으로 판단
)

// 부모가 리컴포지션되면 이 컴포저블도 항상 리컴포지션됨
@Composable
fun UnstableChild(item: UnstableItem) { ... }"""
                )
            }

            item {
                InfoCard(
                    title = "불안정으로 판단되는 타입들",
                    description = "• List<T>, Map<K,V>, Set<T> — 구현체가 mutable일 수 있음\n" +
                            "• var 프로퍼티가 있는 일반 클래스\n" +
                            "• 인터페이스 타입 (구현 불명)\n" +
                            "• 다른 불안정 타입을 포함하는 클래스\n" +
                            "• @Stable / @Immutable 없는 외부 라이브러리 클래스",
                    bgColor = Color(0xFFFCE4EC)
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("2. @Immutable") }

            item {
                CodeCard(
                    code = """// ✅ @Immutable: 모든 public 프로퍼티가 절대 바뀌지 않음을 약속
@Immutable
data class ImmutableItem(
    val name: String,
    val tags: List<String>  // 런타임에 절대 변경되지 않겠다고 개발자가 보장
)

// 부모가 리컴포지션되어도 item이 같으면 스킵됨
@Composable
fun ImmutableChild(item: ImmutableItem) { ... }"""
                )
            }

            item {
                InfoCard(
                    title = "@Immutable 특징",
                    description = "• 가장 강한 안정성 계약 — 생성 후 절대 변경 없음\n" +
                            "• Compose 컴파일러가 이 타입을 사용하는 함수를 '스킵 가능'으로 마킹\n" +
                            "• 주의: 실제로 변경하면 런타임 버그 (컴파일러가 검증 불가)\n" +
                            "• kotlinx.collections.immutable의 ImmutableList를 사용하면\n" +
                            "  어노테이션 없이도 컴파일러가 자동으로 안정적으로 판단",
                    bgColor = Color(0xFFE3F2FD)
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("3. @Stable") }

            item {
                CodeCard(
                    code = """// ✅ @Stable: equals()가 안정적이고, State 변경 시 알림 보장
@Stable
class StableCounter(initialValue: Int) {
    // var이지만 MutableState로 감싸 → 변경 시 Compose에 알림
    var count by mutableIntStateOf(initialValue)
}

// @Stable이 붙었으므로 count가 바뀌지 않으면 스킵됨
@Composable
fun StableChild(counter: StableCounter) { ... }"""
                )
            }

            item {
                InfoCard(
                    title = "@Stable vs @Immutable 비교",
                    description = "• @Immutable: 값이 절대 변하지 않음 (더 강한 계약)\n" +
                            "• @Stable: 값이 변할 수 있지만 변경 시 Compose에 알림 보장\n" +
                            "• 둘 다 '스킵 가능'한 컴포저블을 만들어줌\n" +
                            "• 일반적으로: 순수 데이터 → @Immutable, 상태 보유 → @Stable",
                    bgColor = Color(0xFFFFF8E1)
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("4. 리컴포지션 카운터 데모") }

            item {
                InfoCard(
                    title = "데모 설명",
                    description = "아래 버튼으로 부모를 리컴포지션시킵니다.\n" +
                            "• UnstableChild: 파라미터 내용이 동일해도 항상 리컴포지션\n" +
                            "• ImmutableChild: 파라미터가 같으면 리컴포지션 스킵\n\n" +
                            "실제 스킵 여부는 Compose 컴파일러 버전과\n" +
                            "Strong Skipping Mode 설정에 따라 다를 수 있습니다.",
                    bgColor = Color(0xFFF3E5F5)
                )
            }

            item {
                Button(
                    onClick = { parentTrigger++ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                ) {
                    Text("부모 리컴포지션 트리거 (${parentTrigger}회)", color = Color.White)
                }
            }

            item {
                // parentTrigger를 읽어 부모가 리컴포지션될 때 함께 재실행되는 블록
                val trigger = parentTrigger
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 불안정 파라미터 → 항상 리컴포지션
                    UnstableChildDemo(
                        item = UnstableItem("테스트", listOf("A", "B")),
                        modifier = Modifier.weight(1f)
                    )
                    // @Immutable 파라미터 → 스킵 가능
                    ImmutableChildDemo(
                        item = ImmutableItem("테스트", listOf("A", "B")),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { HorizontalDivider() }
            item { SectionHeader("5. 실무 가이드라인") }

            item {
                CodeCard(
                    code = """// ✅ kotlinx-collections-immutable 활용 (권장)
// → 어노테이션 없이도 컴파일러가 자동으로 안정적으로 인식
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class UserState(
    val name: String,
    val tags: ImmutableList<String> = persistentListOf()
)

// ✅ data class + val 프로퍼티만 사용
// → 기본 타입만 있으면 @Immutable 없이도 안정적
@Immutable
data class SimpleData(val id: Int, val label: String)

// ⚠ 남용 금지: 실제로 불안정한 클래스에 붙이면 리컴포지션 버그 발생
// @Immutable  ← 절대 금지
// class WrongUsage { var mutable = "변할 수 있음" }"""
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// ===================== 데모용 자식 컴포저블 =====================

@Composable
private fun UnstableChildDemo(item: UnstableItem, modifier: Modifier = Modifier) {
    var recomposeCount by remember { mutableIntStateOf(0) }
    // SideEffect: 리컴포지션마다 실행 (성공적으로 커밋된 컴포지션 후)
    SideEffect { recomposeCount++ }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("UnstableChild", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "리컴포지션\n${recomposeCount}회",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828),
                fontFamily = FontFamily.Monospace
            )
            Text(item.name, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun ImmutableChildDemo(item: ImmutableItem, modifier: Modifier = Modifier) {
    var recomposeCount by remember { mutableIntStateOf(0) }
    SideEffect { recomposeCount++ }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("@ImmutableChild", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "리컴포지션\n${recomposeCount}회",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                fontFamily = FontFamily.Monospace
            )
            Text(item.name, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

// ===================== 공통 UI 컴포넌트 =====================

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1976D2),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun InfoCard(title: String, description: String, bgColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (title.isNotEmpty()) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (description.isNotEmpty()) Spacer(modifier = Modifier.height(6.dp))
            }
            if (description.isNotEmpty()) {
                Text(text = description, fontSize = 13.sp, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
private fun CodeCard(code: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = code,
            color = Color(0xFFCFD8DC),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 17.sp,
            modifier = Modifier.padding(12.dp)
        )
    }
}
