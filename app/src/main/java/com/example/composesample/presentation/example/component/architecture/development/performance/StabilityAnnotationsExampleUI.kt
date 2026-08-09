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

            item {
                InfoCard(
                    title = "먼저 알아둘 것 — Strong Skipping Mode",
                    description = "Kotlin 2.0.20 컴파일러부터 Strong Skipping이 기본 활성화됩니다.\n" +
                            "(이 프로젝트는 Kotlin 2.4.0이므로 켜져 있습니다)\n\n" +
                            "그래서 '불안정한 파라미터를 받으면 스킵이 아예 불가능'하지 않습니다.\n" +
                            "불안정 파라미터를 받는 컴포저블도 skippable로 컴파일되고,\n" +
                            "달라지는 것은 '무엇으로 비교하는가'입니다.\n\n" +
                            "• 안정(stable) 파라미터 → equals() 구조 비교\n" +
                            "• 불안정(unstable) 파라미터 → === 인스턴스 동일성 비교\n\n" +
                            "즉 불안정 타입은 내용이 같아도 .copy()/.toList() 등으로\n" +
                            "새 인스턴스를 만들면 리컴포지션됩니다. 5번 섹션에서 직접 확인합니다.",
                    bgColor = Color(0xFFE0F7FA)
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("1. 불안정한 클래스 (인스턴스가 바뀌면 리컴포지션)") }

            item {
                CodeCard(
                    code = """// ❌ 불안정: List<T>는 MutableList 구현 가능 → 불안정
data class UnstableItem(
    val name: String,
    val tags: List<String>  // ← Compose가 불안정으로 판단
)

// Strong Skipping 덕분에 이 함수도 skippable 로 컴파일된다.
// 다만 item 은 equals() 가 아니라 === 로 비교되므로,
// 내용이 같아도 새 인스턴스를 넘기면 리컴포지션된다.
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
                            "두 카드 모두 매번 새 인스턴스를 만들어 넘깁니다.\n\n" +
                            "• UnstableChild: === 비교 → 새 인스턴스라 실패 → 리컴포지션\n" +
                            "• ImmutableChild: equals() 비교 → 내용이 같아 성공 → 스킵\n\n" +
                            "여기서 차이를 만드는 것은 '불안정하냐'가 아니라\n" +
                            "'무엇으로 비교하느냐'입니다. 5번 섹션에서 이를 분리해 확인합니다.",
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
                    // 불안정 파라미터 → === 비교. 매번 새 인스턴스라 실패 → 리컴포지션
                    UnstableChildDemo(
                        label = "UnstableChild",
                        item = UnstableItem("테스트", listOf("A", "B")),
                        modifier = Modifier.weight(1f)
                    )
                    // @Immutable 파라미터 → equals() 비교. 내용이 같아 성공 → 스킵
                    ImmutableChildDemo(
                        item = ImmutableItem("테스트", listOf("A", "B")),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { HorizontalDivider() }
            item { SectionHeader("5. 실증: 불안정 파라미터는 ===로 비교된다") }

            item {
                InfoCard(
                    title = "같은 불안정 타입, 갈리는 결과",
                    description = "아래 두 카드는 똑같은 UnstableItem(불안정)을 받습니다.\n" +
                            "다른 점은 인스턴스를 매번 새로 만드느냐 하나뿐입니다.\n\n" +
                            "• 왼쪽: 매번 새 인스턴스 → === 실패 → 리컴포지션\n" +
                            "• 오른쪽: remember로 고정 → === 성공 → 스킵\n\n" +
                            "오른쪽이 1회에서 멈춘다면, '불안정 = 항상 리컴포지션'이\n" +
                            "틀린 설명이라는 증거입니다. 정확한 규칙은\n" +
                            "'불안정 = 내용이 아니라 인스턴스로 비교'입니다.",
                    bgColor = Color(0xFFE0F7FA)
                )
            }

            item {
                // remember로 고정한 인스턴스 — 부모가 리컴포지션돼도 같은 객체가 전달된다
                val fixedItem = remember { UnstableItem("고정", listOf("A", "B")) }
                Column {
                    // parentTrigger를 실제로 읽어야 이 item이 부모와 함께 리컴포지션된다
                    Text(
                        text = "부모 리컴포지션 ${parentTrigger}회 기준",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // 매번 새 인스턴스 생성 → === 비교 실패
                        UnstableChildDemo(
                            label = "새 인스턴스",
                            item = UnstableItem("고정", listOf("A", "B")),
                            modifier = Modifier.weight(1f)
                        )
                        // remember로 고정된 동일 인스턴스 → === 비교 성공 → 스킵
                        UnstableChildDemo(
                            label = "remember 고정",
                            item = fixedItem,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                CodeCard(
                    code = """// 두 호출의 차이는 '인스턴스를 새로 만드느냐'뿐이다
val fixedItem = remember { UnstableItem("고정", listOf("A", "B")) }

UnstableChild(UnstableItem("고정", listOf("A", "B")))  // 매번 새 인스턴스 → 리컴포지션
UnstableChild(fixedItem)                                // 같은 인스턴스   → 스킵

// 컴파일러 리포트로도 확인할 수 있다 (composables.txt)
// restartable skippable fun UnstableChildDemo(
//   stable label: String        ← stable 표시 있음 = equals() 비교
//   item: UnstableItem          ← stable 표시 없음 = 불안정(=== 비교)
//   stable modifier: Modifier?  ← stable 표시 있음 = equals() 비교
// )
// ↑ skippable 이라는 점에 주목. 불안정 파라미터가 있어도 스킵 자체는 가능하다."""
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("6. 실무 가이드라인") }

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
// class WrongUsage { var mutable = "변할 수 있음" }

// ✅ Strong Skipping 시대의 추가 원칙: 인스턴스를 불필요하게 새로 만들지 말 것
// 불안정 타입은 ===로 비교되므로, 내용이 같아도 아래는 매번 리컴포지션된다
// state.copy(items = state.items.toList())  ← 새 List 인스턴스 = 스킵 실패
// → ImmutableList로 바꾸거나(equals 비교), 인스턴스를 그대로 재사용할 것"""
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// ===================== 데모용 자식 컴포저블 =====================

@Composable
private fun UnstableChildDemo(
    label: String,
    item: UnstableItem,
    modifier: Modifier = Modifier
) {
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
            Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
