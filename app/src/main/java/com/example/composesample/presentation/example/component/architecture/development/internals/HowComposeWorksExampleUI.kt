package com.example.composesample.presentation.example.component.architecture.development.internals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

// 리컴포지션(컴포지션) 횟수를 세는 plain holder — snapshot state 가 아니므로 읽고 써도 추가 무효화를 일으키지 않음.
// Composable body 가 다시 실행될 때마다 value 가 증가하고, skip 되면 증가하지 않아 read-tracking 시연에 사용한다.
private class CompositionCounter {
    var value = 0
}

@Composable
fun HowComposeWorksExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "How Compose Works",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { CompilerTransformCard() }
            item { SlotTableCard() }
            item { SnapshotReadTrackingCard() }
            item { LayoutPipelineCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun ConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Compose는 어떻게 동작하는가",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Jetpack Compose는 우리가 작성한 선언형 @Composable 코드를 4단계 파이프라인으로 실제 화면에 그립니다. " +
                        "이 예제는 각 단계를 개념 + 코드 블록 + 동등 시뮬레이션으로 보여줍니다. " +
                        "내부 런타임 클래스(Composer/SlotTable 실물)를 직접 호출하지 않고, 같은 원리를 안전하게 재현합니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val phases = listOf(
                Triple("① 컴파일러 변환", "Compose Compiler", "@Composable 에 \$composer·group 삽입"),
                Triple("② SlotTable", "Composition 저장", "그룹/슬롯에 결과 저장·위치 기반 재사용"),
                Triple("③ Snapshot 읽기 추적", "Recomposition", "state 읽은 Composable 만 무효화"),
                Triple("④ Layout Pipeline", "measure→place→draw", "측정·배치·그리기 3단계")
            )
            phases.forEach { (label, phase, note) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.32f))
                    Text(text = phase, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF1976D2), modifier = Modifier.weight(0.32f))
                    Text(text = note, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.36f))
                }
            }
        }
    }
}

@Composable
private fun CompilerTransformCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "① 컴파일러 변환 (@Composable → \$composer)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Compose Compiler 플러그인은 모든 @Composable 함수에 숨은 Composer 파라미터를 추가하고, " +
                        "함수 본문을 startRestartGroup/endRestartGroup 그룹으로 감쌉니다. " +
                        "이 '그룹 키'가 SlotTable 에서 각 호출의 위치(identity)를 식별합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "// 우리가 작성한 코드\n" +
                        "@Composable\n" +
                        "fun Greeting(name: String) {\n" +
                        "    Text(\"Hello, \$name\")\n" +
                        "}\n\n" +
                        "// 컴파일러가 대략 이렇게 변환 (개념적)\n" +
                        "fun Greeting(name: String, \$composer: Composer, \$changed: Int) {\n" +
                        "    \$composer.startRestartGroup(0xABCD) // 그룹 키 = 호출 위치\n" +
                        "    if (\$changed and 0b1011 != 0b1010 ||\n" +
                        "        !\$composer.skipping) {\n" +
                        "        Text(\"Hello, \$name\", \$composer, 0)\n" +
                        "    } else {\n" +
                        "        \$composer.skipToGroupEnd() // 입력 불변 → 스킵\n" +
                        "    }\n" +
                        "    \$composer.endRestartGroup()?.updateScope { c, _ ->\n" +
                        "        Greeting(name, c, \$changed or 1) // 리컴포즈 람다\n" +
                        "    }\n" +
                        "}",
                borderColor = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "핵심: 함수는 UI '객체'를 반환하지 않고, Composer 에 '내가 무엇을 그릴지'를 방출(emit)합니다. " +
                        "\$changed 비트마스크로 인자 변경 여부를 알아 skip 여부를 판단합니다.",
                fontSize = 11.sp,
                color = Color(0xFF424242),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun SlotTableCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "② SlotTable (컴포지션 저장소)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "컴포지션의 결과(그룹 구조 · remember 값 · 노드)는 SlotTable 이라는 선형 배열(gap buffer)에 저장됩니다. " +
                        "리컴포지션 때 Compose 는 그룹 키로 같은 위치를 찾아 슬롯을 재사용하고, 바뀐 부분만 갱신합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // SlotTable 의 그룹/슬롯 레이아웃을 개념적으로 시각화
            val rows = listOf(
                Triple("group #0", "Greeting", "키=호출위치 · 자식 그룹 보유"),
                Triple("  slot 0", "remember{}", "기억된 값 캐시"),
                Triple("  slot 1", "\"Hello\"", "Text 파라미터"),
                Triple("group #1", "LayoutNode", "방출된 UI 노드 참조")
            )
            rows.forEach { (cell, content, note) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .background(Color(0xFFEDE7F6), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = cell, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, color = Color(0xFF5E35B1), modifier = Modifier.weight(0.28f))
                    Text(text = content, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF4527A0), modifier = Modifier.weight(0.3f))
                    Text(text = note, fontSize = 10.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.42f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "이 위치 기반 식별 때문에 if/for 안에서 Composable 을 조건부로 호출하면 그룹 위치가 어긋나 " +
                        "엉뚱한 슬롯이 재사용될 수 있고, 이를 방지하려고 LazyColumn 등에 key 를 부여합니다.",
                fontSize = 11.sp,
                color = Color(0xFF424242),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun SnapshotReadTrackingCard() {
    // 두 개의 독립 state 를 두고, 각각을 읽는 자식만 리컴포즈됨을 실제 컴포지션 횟수로 보여준다(read-tracking 실측).
    var counterA by remember { mutableIntStateOf(0) }
    var counterB by remember { mutableIntStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "③ Snapshot 읽기 추적 (read-tracking)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "MutableState 를 읽는 동안 Compose 는 '이 Composable 이 이 state 를 읽었다'를 스냅샷 시스템에 기록합니다. " +
                        "값이 바뀌면 그 state 를 읽은 Composable 만 무효화(리컴포즈)됩니다. 아래에서 A 만 올리면 B 카드의 " +
                        "컴포지션 횟수는 그대로임을 확인하세요.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // counterA 만 읽는 자식 / counterB 만 읽는 자식 — 안정(Int) 파라미터라 변경된 쪽만 리컴포즈된다.
            TrackedCounter(label = "Counter A", value = counterA, accent = Color(0xFF1976D2))
            Spacer(modifier = Modifier.height(8.dp))
            TrackedCounter(label = "Counter B", value = counterB, accent = Color(0xFFEF6C00))
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { counterA++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text(text = "A 증가", color = Color.White, fontSize = 12.sp)
                }
                Button(
                    onClick = { counterB++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF6C00))
                ) {
                    Text(text = "B 증가", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun TrackedCounter(label: String, value: Int, accent: Color) {
    // CompositionCounter 는 snapshot state 가 아니라 plain 객체 — body 가 재실행될 때만 증가한다.
    // 이 자식의 인자(value)가 바뀌어 리컴포즈될 때만 횟수가 오르고, 다른 카운터만 바뀌면 skip 되어 그대로 유지된다.
    val recomposition = remember { CompositionCounter() }
    recomposition.value++

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$label = $value", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = accent)
        Text(
            text = "이 카드 컴포지션 ${recomposition.value}회",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = accent
        )
    }
}

@Composable
private fun LayoutPipelineCard() {
    var selectedPhase by remember { mutableIntStateOf(0) }
    val phaseLabels = listOf("Measure", "Place", "Draw")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "④ Layout Pipeline (measure → place → draw)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "컴포지션이 LayoutNode 트리를 만들면, 화면에 그려지기까지 3단계를 거칩니다. " +
                        "각 단계를 눌러 무슨 일이 일어나는지 확인하세요.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                phaseLabels.forEachIndexed { index, label ->
                    Button(
                        onClick = { selectedPhase = index },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPhase == index) Color(0xFF1976D2) else Color(0xFFE0E0E0)
                        )
                    ) {
                        Text(
                            text = label,
                            color = if (selectedPhase == index) Color.White else Color(0xFF424242),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            when (selectedPhase) {
                0 -> PhaseDetail(
                    title = "1. Measure (측정)",
                    desc = "부모가 자식에게 Constraints(min/max 폭·높이)를 내려주고, 자식은 자신의 크기를 정해 Placeable 을 반환합니다. " +
                            "각 노드는 한 번만 측정되는 것을 원칙으로 하며(단일 패스), intrinsic 측정은 예외입니다.",
                    code = "@Composable\n" +
                            "fun MyLayout(content: @Composable () -> Unit) {\n" +
                            "    Layout(content) { measurables, constraints ->\n" +
                            "        // 1) 자식 측정\n" +
                            "        val placeables = measurables.map {\n" +
                            "            it.measure(constraints)\n" +
                            "        }\n" +
                            "        // 2) 내 크기 결정\n" +
                            "        layout(constraints.maxWidth, h) { ... }\n" +
                            "    }\n" +
                            "}"
                )
                1 -> PhaseDetail(
                    title = "2. Place (배치)",
                    desc = "측정으로 얻은 Placeable 을 부모 좌표계의 (x, y) 위치에 배치합니다. " +
                            "placeRelative 는 LTR/RTL 방향을 자동 반영합니다. 위치만 바뀌는 애니메이션은 측정을 건너뛰어 저렴합니다.",
                    code = "layout(width, height) {\n" +
                            "    // measure 단계의 placeables 를 배치\n" +
                            "    var y = 0\n" +
                            "    placeables.forEach { placeable ->\n" +
                            "        placeable.placeRelative(x = 0, y = y)\n" +
                            "        y += placeable.height\n" +
                            "    }\n" +
                            "}"
                )
                2 -> PhaseDetail(
                    title = "3. Draw (그리기)",
                    desc = "배치된 노드를 Canvas 에 실제로 그립니다. Modifier.drawBehind/drawWithContent 나 graphicsLayer 로 " +
                            "이 단계만 따로 갱신할 수 있어, 색·알파 변경은 리컴포지션/재측정 없이 draw 만 다시 돕니다.",
                    code = "Modifier.drawBehind {\n" +
                            "    drawRect(color = Color.Blue)\n" +
                            "}\n\n" +
                            "// graphicsLayer 로 draw 단계만 갱신 (저렴)\n" +
                            "Modifier.graphicsLayer { alpha = animatedAlpha }"
                )
            }
        }
    }
}

@Composable
private fun PhaseDetail(title: String, desc: String, code: String) {
    Column {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = desc, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
        Spacer(modifier = Modifier.height(10.dp))
        CodeBlock(code = code, borderColor = Color(0xFF1976D2))
    }
}

@Composable
private fun SummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "핵심 정리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val bullets = listOf(
                "Compose Compiler 가 @Composable 에 Composer 파라미터와 startRestartGroup/endRestartGroup 을 삽입한다",
                "함수는 UI 객체를 반환하지 않고 Composer 에 emit 한다 — \$changed 비트마스크로 skip 여부를 판단",
                "컴포지션 결과는 SlotTable 에 그룹/슬롯으로 저장되고, 그룹 키(호출 위치)로 재사용된다",
                "Snapshot 시스템이 state 읽기를 추적해, 값이 바뀌면 그 state 를 읽은 Composable 만 리컴포즈된다",
                "LayoutNode 는 measure → place → draw 3단계를 거치며, graphicsLayer/drawBehind 로 draw 단계만 저렴하게 갱신 가능"
            )
            bullets.forEach { bullet ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(text = "• ", fontSize = 13.sp, color = Color(0xFF1976D2))
                    Text(text = bullet, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String, borderColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF212121),
            lineHeight = 16.sp
        )
    }
}
