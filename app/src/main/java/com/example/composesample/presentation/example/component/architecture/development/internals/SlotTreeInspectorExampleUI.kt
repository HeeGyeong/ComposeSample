package com.example.composesample.presentation.example.component.architecture.development.internals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.CompositionGroup
import androidx.compose.runtime.tooling.findCompositionInstance
import androidx.compose.runtime.tooling.parseSourceInformation
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

// ============================================================================
// 슬롯 트리 순회 인프라
//
// Compose 런타임은 컴포지션 결과를 SlotTable 이라는 선형 배열에 담는다.
// CompositionData/CompositionGroup 은 그 배열을 트리처럼 읽게 해주는 공개 창구이고,
// 각 그룹의 sourceInfo 문자열을 parseSourceInformation 으로 풀면
// "이 그룹이 어느 파일 몇 번째 줄의 어떤 함수 호출인가"가 나온다.
//
// ⚠️ 이 순회는 반드시 "컴포지션이 끝난 뒤"에 해야 한다.
//    컴포지션 도중에 읽으면 지금 만들고 있는 부분이 빠진 트리가 나온다(새 슬롯 테이블이면 아예 빈 트리).
//    자세한 조건은 아래 GateMatrixCard 참조.
// ============================================================================

/** 트리 한 줄을 화면에 그리기 위해 평탄화한 결과. */
private data class SlotLine(
    val depth: Int,
    val functionName: String?,
    val sourceFile: String?,
    val lineNumber: Int?,
    val parameterNames: List<String>,
    val nodeName: String?,
    val groupSize: Int,
    val slotCount: Int,
    val isInline: Boolean
)

/** 그룹의 sourceInfo 를 풀어 함수 이름만 얻는다. 소스 정보가 없으면 null. */
@OptIn(ComposeToolingApi::class)
private fun CompositionGroup.functionNameOrNull(): String? =
    sourceInfo?.let { parseSourceInformation(it) }?.functionName

/**
 * 함수 이름으로 서브트리의 루트 그룹을 찾는다.
 *
 * CompositionGroup 이 CompositionData 를 상속하므로 이 확장 하나로 루트/하위 어디서든 재귀 탐색된다.
 * 소스 정보 수집이 꺼져 있으면 어떤 그룹도 이름을 갖지 않으므로 항상 null 이 된다.
 */
@OptIn(ComposeToolingApi::class)
private fun CompositionData.findGroupByFunctionName(name: String): CompositionGroup? {
    compositionGroups.forEach { group ->
        if (group.functionNameOrNull() == name) return group
        group.findGroupByFunctionName(name)?.let { return it }
    }
    return null
}

/** 그룹 트리를 깊이 우선으로 평탄화한다. 화면 표시용이라 깊이와 줄 수를 모두 제한한다. */
@OptIn(ComposeToolingApi::class)
private fun CompositionGroup.flattenInto(
    out: MutableList<SlotLine>,
    depth: Int,
    maxDepth: Int,
    limit: Int
) {
    if (out.size >= limit) return

    val parsed = sourceInfo?.let { parseSourceInformation(it) }
    out.add(
        SlotLine(
            depth = depth,
            functionName = parsed?.functionName,
            sourceFile = parsed?.sourceFile,
            lineNumber = parsed?.locations?.firstOrNull()?.lineNumber,
            // 파라미터 이름은 이론상 비어 있을 수 있어 null 은 걸러낸다.
            parameterNames = parsed?.parameters?.mapNotNull { it.name } ?: emptyList(),
            nodeName = node?.javaClass?.simpleName,
            groupSize = groupSize,
            slotCount = data.count(),
            isInline = parsed?.isInline == true
        )
    )

    if (depth >= maxDepth) return
    compositionGroups.forEach { it.flattenInto(out, depth + 1, maxDepth, limit) }
}

/** 이 그룹과 모든 자손 그룹의 개수. 슬롯 트리의 규모를 체감하기 위한 계측용. */
@OptIn(ComposeToolingApi::class)
private fun CompositionGroup.countRecursive(): Int {
    var total = 1
    compositionGroups.forEach { total += it.countRecursive() }
    return total
}

@OptIn(ComposeToolingApi::class)
private fun CompositionData.countAllGroups(): Int {
    var total = 0
    compositionGroups.forEach { total += it.countRecursive() }
    return total
}

// ============================================================================
// UI
// ============================================================================

@Composable
fun SlotTreeInspectorExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Slot Tree Inspector (컴포지션 구조 덤프)",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { InspectorCard() }
            item { GateMatrixCard() }
            item { SourceInfoFormatCard() }
            item { NestedCompositionCard() }
            item { LimitationCard() }
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
                text = "슬롯 트리를 직접 읽는다는 것",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00796B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Compose 는 컴포지션 결과를 SlotTable 이라는 선형 배열에 저장합니다. " +
                        "그 배열을 트리처럼 읽게 해주는 공개 창구가 CompositionData / CompositionGroup 이고, " +
                        "각 그룹이 들고 있는 sourceInfo 문자열을 parseSourceInformation 으로 풀면 " +
                        "그 그룹이 \"어느 파일 몇 번째 줄의 어떤 함수 호출\"인지가 드러납니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                code = "@OptIn(ComposeToolingApi::class)\n" +
                        "@Composable\n" +
                        "fun Inspect() {\n" +
                        "    // ① 소스 정보 수집을 켠다 (이 호출 이후에 삽입되는 그룹만 기록된다)\n" +
                        "    currentComposer.collectParameterInformation()\n" +
                        "\n" +
                        "    // ② 이 Composable 이 속한 컴포지션의 슬롯 데이터를 잡아둔다\n" +
                        "    val data = currentComposer.compositionData\n" +
                        "\n" +
                        "    Button(onClick = {\n" +
                        "        // ③ 읽기는 컴포지션이 끝난 뒤에 (여기서는 클릭 핸들러)\n" +
                        "        data.compositionGroups.forEach { group ->\n" +
                        "            val info = group.sourceInfo?.let(::parseSourceInformation)\n" +
                        "            println(\"${'$'}{info?.functionName} @ ${'$'}{info?.sourceFile}\")\n" +
                        "        }\n" +
                        "    }) { Text(\"스캔\") }\n" +
                        "}",
                borderColor = Color(0xFF00796B)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "CompositionObserver 예제와의 차이",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00796B)
            )
            Spacer(modifier = Modifier.height(6.dp))
            listOf(
                "CompositionObserver" to "이벤트 축 — \"왜 리컴포즈됐는가\"(무효화 원인)를 시간순으로 답한다",
                "Slot Tree (이 예제)" to "구조 축 — \"지금 컴포지션이 어떤 모양이고 각 그룹이 어디인가\"를 답한다"
            ).forEach { (name, note) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = name,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF00796B),
                        modifier = Modifier.weight(0.36f)
                    )
                    Text(
                        text = note,
                        fontSize = 10.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.weight(0.64f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "앞선 CompositionObserver 예제는 무효화된 스코프에 사람이 읽을 이름을 붙이지 못해 " +
                        "S1/S2 별칭과 해시로만 표기했습니다. 그 빈 곳을 메우는 것이 정확히 이 예제의 sourceInfo 해석입니다.",
                fontSize = 12.sp,
                color = Color(0xFF616161),
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * 핵심 실동작 카드.
 *
 * 이 카드는 자기 자신이 속한 컴포지션의 슬롯 트리를 스캔해, 바로 아래에서 그리고 있는
 * InspectedSampleTree 서브트리를 찾아 함수명/파일:줄/파라미터/노드로 덤프한다.
 */
@OptIn(ComposeToolingApi::class)
@Composable
private fun InspectorCard() {
    // ① 소스 정보 수집 활성화.
    //    런타임은 "그룹을 삽입하는 시점"에 sourceMarkersEnabled 가 켜져 있을 때만 sourceInfo 를 기록한다.
    //    따라서 이 호출보다 먼저 삽입된 그룹(이 카드 자신과 그 조상)에는 소스 정보가 없고,
    //    이 호출 이후에 삽입되는 아래쪽 그룹부터 기록된다.
    currentComposer.collectParameterInformation()

    // ② 이 Composable 이 속한 컴포지션의 슬롯 데이터. 참조만 잡아두고 읽기는 나중에 한다.
    val compositionData = currentComposer.compositionData

    var leafCount by remember { mutableIntStateOf(2) }
    var maxDepth by remember { mutableIntStateOf(4) }
    val lines = remember { mutableStateListOf<SlotLine>() }
    var summary by remember { mutableStateOf("아직 스캔하지 않았습니다.") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "1. 실동작 — 내 서브트리 스캔",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00796B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "아래 \"스캔 대상\"은 평범한 Composable 입니다. 잎 개수를 바꾸면 슬롯 트리가 실제로 " +
                        "달라지고, 스캔 결과의 줄 수와 그룹 크기가 따라 변합니다.",
                fontSize = 12.sp,
                color = Color(0xFF616161),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── 스캔 대상 ─────────────────────────────────────────────
            Text(
                text = "스캔 대상 (InspectedSampleTree)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00796B)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFB2DFDB), RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0F2F1), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                InspectedSampleTree(title = "샘플", leafCount = leafCount)
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { if (leafCount < 5) leafCount++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                ) { Text(text = "잎 +1", fontSize = 12.sp) }
                Button(
                    onClick = { if (leafCount > 0) leafCount-- },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                ) { Text(text = "잎 -1", fontSize = 12.sp) }
                Button(
                    onClick = { maxDepth = if (maxDepth >= 6) 2 else maxDepth + 1 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF546E7A))
                ) { Text(text = "깊이 $maxDepth", fontSize = 12.sp) }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    // ③ 읽기는 컴포지션 밖(클릭 핸들러)에서. 컴포지션 도중에 읽으면 빈 트리다.
                    lines.clear()
                    val root = compositionData.findGroupByFunctionName("InspectedSampleTree")
                    if (root == null) {
                        summary = "InspectedSampleTree 그룹을 찾지 못했습니다 " +
                                "(소스 정보 수집이 꺼져 있으면 이름을 알 수 없습니다)."
                    } else {
                        root.flattenInto(out = lines, depth = 0, maxDepth = maxDepth, limit = 60)
                        summary = "서브트리 그룹 ${root.countRecursive()}개 중 깊이 ${maxDepth}까지 " +
                                "${lines.size}줄 표시 / 이 카드가 속한 컴포지션 전체는 " +
                                "${compositionData.countAllGroups()}개 그룹"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C))
            ) { Text(text = "구조 스캔", fontSize = 13.sp) }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = summary,
                fontSize = 11.sp,
                color = Color(0xFF00695C),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "※ 인스펙터도 같은 컴포지션 안에 있습니다. 스캔 결과 표가 화면에 그려지면 그 표의 그룹까지 " +
                        "같은 트리에 들어가므로, 아무것도 바꾸지 않고 한 번 더 스캔하기만 해도 \"컴포지션 전체\" " +
                        "숫자가 눈에 띄게 커집니다. 관찰이 대상을 바꾸는 셈이라, 실무에서는 덤프를 별도 " +
                        "컴포지션이나 로그로 빼는 편이 좋습니다.",
                fontSize = 11.sp,
                color = Color(0xFF757575),
                lineHeight = 17.sp
            )

            if (lines.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFF00796B), RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    lines.forEach { line -> SlotLineRow(line) }
                }
            }
        }
    }
}

/** 스캔 결과 한 줄. 들여쓰기로 그룹 깊이를 표현한다. */
@Composable
private fun SlotLineRow(line: SlotLine) {
    val indent = "  ".repeat(line.depth)
    val name = line.functionName ?: "(익명 그룹)"
    val where = if (line.sourceFile != null && line.lineNumber != null) {
        "${line.sourceFile}:${line.lineNumber}"
    } else {
        "-"
    }
    val params = if (line.parameterNames.isEmpty()) "" else "(${line.parameterNames.joinToString(", ")})"

    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "$indent${if (line.isInline) "◇" else "◆"} $name$params",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (line.functionName != null) FontWeight.Medium else FontWeight.Normal,
            color = if (line.functionName != null) Color(0xFF00695C) else Color(0xFF9E9E9E)
        )
        Text(
            text = "$indent    $where · groupSize=${line.groupSize} · slots=${line.slotCount}" +
                    (line.nodeName?.let { " · node=$it" } ?: ""),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF9E9E9E)
        )
    }
}

/** 스캔 대상. 파라미터 이름이 sourceInfo 에 그대로 기록되므로 결과에서 (title, leafCount) 로 보인다. */
@Composable
private fun InspectedSampleTree(title: String, leafCount: Int) {
    val createdAt = remember { "remember 슬롯" }
    Column {
        Text(text = "$title · $createdAt · 잎 ${leafCount}개", fontSize = 11.sp, color = Color(0xFF00695C))
        repeat(leafCount) { index ->
            InspectedLeaf(index = index)
        }
    }
}

@Composable
private fun InspectedLeaf(index: Int) {
    Text(text = "· leaf-$index", fontSize = 11.sp, color = Color(0xFF00796B))
}

/**
 * 소스 정보가 나오기 위한 3가지 조건을 실기기 계측 그대로 재현하는 카드.
 *
 * 이 카드는 InspectorCard 와 달리 collectParameterInformation() 을 **호출하지 않는다**.
 * LazyColumn 의 각 item 은 별도의 서브컴포지션이라 컴포저 플래그가 카드마다 독립적이고,
 * 그래서 이 카드에서 스캔하면 트리는 나오지만 이름은 전부 비어 있다(표의 2행).
 */
@OptIn(ComposeToolingApi::class)
@Composable
private fun GateMatrixCard() {
    val compositionData = currentComposer.compositionData

    var extraRows by remember { mutableIntStateOf(0) }
    var afterResult by remember { mutableStateOf("아직 읽지 않았습니다.") }

    // 컴포지션 "도중"에 읽은 값. 아래에서 추가되는 행들이 이 숫자에는 반영되지 않는다
    // (실측: 행을 하나 늘려도 이 값은 그대로이고, 클릭 핸들러에서 읽은 값만 늘어난다).
    val duringPassGroups = compositionData.countAllGroups()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "2. 언제 읽는가 · 무엇을 켜야 하는가",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00796B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "슬롯 트리는 아무 때나 읽을 수 있는 자료구조가 아닙니다. 읽는 시점과 수집 플래그에 따라 " +
                        "결과가 갈립니다.",
                fontSize = 12.sp,
                color = Color(0xFF616161),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            GateRow("조건", "결과", isHeader = true)
            GateRow("컴포지션 도중", "지금 만들고 있는 부분이 빠진 트리가 보인다")
            GateRow("· 슬롯 테이블이 새것이면", "아예 빈 트리 (isEmpty=true)")
            GateRow("컴포지션 이후 + 수집 OFF", "구조는 완전하지만 sourceInfo 전부 null")
            GateRow("컴포지션 이후 + 수집 ON", "함수명·파일·줄번호·파라미터 모두 해석")

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "직접 확인 — \"행 +1\"을 누른 뒤 아래 두 값을 비교",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00796B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "행을 추가하면 이 카드의 그룹 수가 늘어납니다. 그런데 카드 본문 맨 위에서 읽은 값은 " +
                        "늘어나기 전 숫자에 머뭅니다 — 컴포지션 도중에는 지금 만들고 있는 부분이 " +
                        "슬롯 트리에서 보이지 않기 때문입니다.",
                fontSize = 12.sp,
                color = Color(0xFF616161),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "· 컴포지션 도중 읽기 → 그룹 ${duringPassGroups}개",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = afterResult,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF00695C),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { extraRows++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF546E7A))
                ) { Text(text = "행 +1", fontSize = 12.sp) }
                Button(
                    onClick = {
                        // 이 카드는 collectParameterInformation() 을 부르지 않았으므로
                        // 트리는 나오지만 이름 있는 그룹은 0개여야 한다.
                        var total = 0
                        var named = 0
                        compositionData.compositionGroups.forEach { group ->
                            fun walk(g: CompositionGroup) {
                                total++
                                if (g.sourceInfo != null) named++
                                g.compositionGroups.forEach(::walk)
                            }
                            walk(group)
                        }
                        afterResult = "· 컴포지션 이후 읽기(수집 OFF) → 그룹 ${total}개, " +
                                "그중 sourceInfo 를 가진 그룹 ${named}개"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C))
                ) { Text(text = "컴포지션 이후에 읽기", fontSize = 12.sp) }
            }

            if (extraRows > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    repeat(extraRows) { index ->
                        Text(
                            text = "추가된 행 ${index + 1}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            CodeBlock(
                code = "// 런타임이 소스 정보를 기록하는 조건 (GapComposer.sourceInformation)\n" +
                        "if (inserting && sourceMarkersEnabled) {\n" +
                        "    writer.recordGroupSourceInformation(info)\n" +
                        "}\n" +
                        "\n" +
                        "// collectParameterInformation() 이 하는 일\n" +
                        "forceRecomposeScopes = true          // 모든 스코프를 재시작 가능하게\n" +
                        "sourceMarkersEnabled = true          // 위 조건의 두 번째 항\n" +
                        "slotTable.collectSourceInformation() // 소스 정보 보관 테이블 준비",
                borderColor = Color(0xFF00796B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "기록 조건에 inserting 이 들어 있다는 점이 중요합니다. 수집을 켜기 전에 이미 삽입된 그룹은 " +
                        "나중에 켜도 소급 기록되지 않습니다. 반대로 켠 뒤에 새로 삽입되는 서브트리(예: 잎 추가)는 " +
                        "정상적으로 이름을 갖습니다.",
                fontSize = 12.sp,
                color = Color(0xFF616161),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun GateRow(condition: String, result: String, isHeader: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(
                if (isHeader) Color(0xFFB2DFDB) else Color(0xFFF5F5F5),
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = condition,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Medium,
            color = if (isHeader) Color(0xFF004D40) else Color(0xFF00695C),
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = result,
            fontSize = 10.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = if (isHeader) Color(0xFF004D40) else Color(0xFF616161),
            modifier = Modifier.weight(0.58f)
        )
    }
}

/**
 * sourceInfo 문자열 자체를 다루는 카드.
 *
 * parseSourceInformation 은 컴포지션과 무관한 순수 String 파서라, 리터럴을 그대로 넣어
 * 문법의 각 부분이 어떤 필드로 풀리는지 확인할 수 있다.
 */
@OptIn(ComposeToolingApi::class)
@Composable
private fun SourceInfoFormatCard() {
    val samples = remember {
        listOf(
            "C(Foo)N(a,b)10@100L5,*12@200L8:Foo.kt#abc123",
            "CC(InlineFoo)11@111L3:Foo.kt#abc123",
            "C(Bar)N(tint:c#ui.graphics.Color)5@50L2:Bar.kt#xy",
            "119@6296L7",
            "C(Broken"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "3. sourceInfo 문자열 문법",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00796B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "컴파일러가 그룹마다 심어 두는 sourceInfo 는 아래 형태의 압축 문자열입니다. " +
                        "parseSourceInformation 은 순수 String 파서라 아래 리터럴들이 실제로 파싱된 결과입니다.",
                fontSize = 12.sp,
                color = Color(0xFF616161),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            CodeBlock(
                code = "C(함수명)N(파라미터들)줄@오프셋L길이,...:파일.kt#패키지해시\n" +
                        "│ │       │            │                │        └ 패키지 축약 해시\n" +
                        "│ │       │            │                └ 선언 파일\n" +
                        "│ │       │            └ 호출 지점들(여러 개, * 는 반복 호출)\n" +
                        "│ │       └ N(...) 파라미터 이름 (수집을 켰을 때만)\n" +
                        "│ └ C = 호출 그룹 / CC = 인라인 호출 그룹\n" +
                        "└ 앞에 C 가 없으면 이름 없는 그룹(줄 정보만)",
                borderColor = Color(0xFF00796B)
            )
            Spacer(modifier = Modifier.height(12.dp))

            samples.forEach { raw ->
                val parsed = parseSourceInformation(raw)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = raw,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF00695C)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (parsed == null) {
                            "→ null (파싱 실패 시 예외가 아니라 null 을 돌려준다)"
                        } else {
                            "→ fn=${parsed.functionName ?: "-"} · file=${parsed.sourceFile ?: "-"} · " +
                                    "inline=${parsed.isInline} · " +
                                    "lines=${parsed.locations.joinToString { "${it.lineNumber}${if (it.isRepeatable) "*" else ""}" }} · " +
                                    "params=${
                                        if (parsed.parameters.isEmpty()) "-"
                                        else parsed.parameters.joinToString { p ->
                                            p.name + (p.inlineClass?.let { ":$it" } ?: "")
                                        }
                                    }"
                        },
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (parsed == null) Color(0xFFD32F2F) else Color(0xFF616161),
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "⚠️ 줄 번호는 함수 선언 줄이 아니라 그 함수 안에서 실행되는 Composable 호출 지점을 가리킵니다. " +
                        "문자열의 숫자는 0부터 세고 파싱 결과는 1부터 세므로 정확히 1 차이가 납니다.",
                fontSize = 12.sp,
                color = Color(0xFF616161),
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * 중첩 컴포지션 축.
 *
 * 이 예제의 카드들은 LazyColumn 의 item 안에 있고, LazyColumn 은 SubcomposeLayout 기반이라
 * 각 item 이 별도의 서브컴포지션이다. 그래서 여기서 얻는 CompositionInstance 는 부모를 갖는다.
 */
@OptIn(ComposeToolingApi::class)
@Composable
private fun NestedCompositionCard() {
    val compositionData = currentComposer.compositionData
    var result by remember { mutableStateOf("아직 확인하지 않았습니다.") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "4. 중첩 컴포지션 — 이 카드의 부모는 누구인가",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00796B)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "화면 전체가 하나의 컴포지션인 것은 아닙니다. LazyColumn 은 SubcomposeLayout 기반이라 " +
                        "각 item 이 자기만의 서브컴포지션을 갖습니다. 그래서 카드 안에서 얻은 compositionData 는 " +
                        "화면 전체가 아니라 \"이 item\"의 트리이고, findCompositionInstance() 로 부모 컴포지션의 존재를 확인할 수 있습니다.",
                fontSize = 12.sp,
                color = Color(0xFF616161),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val instance = compositionData.findCompositionInstance()
                    result = if (instance == null) {
                        "CompositionInstance 를 얻지 못했습니다."
                    } else {
                        "· 부모 컴포지션 존재: ${instance.parent != null}\n" +
                                "· 이 컴포지션의 그룹 수: ${compositionData.countAllGroups()}개\n" +
                                "· 루트 그룹 수: ${compositionData.compositionGroups.count()}개"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00695C))
            ) { Text(text = "컴포지션 관계 확인", fontSize = 13.sp) }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = result,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF00695C),
                lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            CodeBlock(
                code = "val instance = compositionData.findCompositionInstance()\n" +
                        "instance?.parent          // 부모 컴포지션 (최상위면 null)\n" +
                        "instance?.data            // 이 컴포지션의 CompositionData\n" +
                        "instance?.findContextGroup()  // 부모 트리에서 이 컴포지션이 붙은 그룹",
                borderColor = Color(0xFF00796B)
            )
        }
    }
}

@Composable
private fun LimitationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "5. 실무로 가져갈 때의 제약",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
            Spacer(modifier = Modifier.height(10.dp))

            listOf(
                "수집은 공짜가 아니다" to
                        "collectParameterInformation() 은 sourceMarkersEnabled 뿐 아니라 forceRecomposeScopes 도 켭니다. " +
                        "모든 스코프가 재시작 가능해져 스킵 최적화가 약해지므로, 디버그 빌드에서 필요할 때만 켜야 합니다.",
                "컴포지션 도중에는 읽을 수 없다" to
                        "컴포지션이 진행 중일 때 읽으면 지금 만들고 있는 부분이 빠진 트리가 나오고, 슬롯 테이블이 새것이면 " +
                        "아예 빈 트리가 나옵니다. 예외를 던지지 않고 조용히 그럴듯한 값을 돌려주므로 가장 속기 쉬운 함정입니다.",
                "릴리스 빌드에서는 이름이 사라질 수 있다" to
                        "소스 정보는 디버깅용 메타데이터입니다. 릴리스 최적화로 제거되면 트리 구조만 남고 함수명은 비어 있게 됩니다.",
                "ui-tooling-data 의 asTree() 는 쓰지 말 것" to
                        "더 편한 Group 계층 API 가 ui-tooling-data 에 있지만 그 아티팩트는 디버그 전용이라 " +
                        "릴리스 빌드가 깨집니다. 이 예제가 쓰는 API 는 runtime 자체에 있어 안전합니다.",
                "ComposeStackTrace 계열은 호출 불가" to
                        "같은 tooling 패키지의 ComposeStackTrace / attachComposeStackTrace 는 바이트코드상 public 이지만 " +
                        "Kotlin internal 이라 앱 코드에서 호출할 수 없습니다."
            ).forEach { (title, body) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = body,
                        fontSize = 11.sp,
                        color = Color(0xFF616161),
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "정리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D40)
            )
            Spacer(modifier = Modifier.height(10.dp))

            listOf(
                "currentComposer.compositionData 는 지금 이 Composable 이 속한 컴포지션의 슬롯 트리다",
                "CompositionGroup 은 CompositionData 를 상속하므로 재귀 순회가 한 함수로 끝난다",
                "그룹에 이름을 붙이는 것은 sourceInfo 문자열이고, 해석은 parseSourceInformation 이 한다",
                "이름을 보려면 collectParameterInformation() 을 그룹 삽입보다 먼저 호출해야 한다",
                "읽기는 반드시 컴포지션 이후에 — 도중에 읽으면 만들고 있는 부분이 빠진 트리가 나온다",
                "LazyColumn 의 item 은 각각 별도 서브컴포지션이라 부모 컴포지션을 갖는다"
            ).forEachIndexed { index, text ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(
                        text = "${index + 1}.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00796B),
                        modifier = Modifier.width(20.dp)
                    )
                    Text(
                        text = text,
                        fontSize = 12.sp,
                        color = Color(0xFF424242),
                        lineHeight = 18.sp
                    )
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
            .horizontalScroll(rememberScrollState())
    ) {
        Text(
            text = code,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF212121),
            lineHeight = 15.sp
        )
    }
}
