package com.example.composesample.presentation.example.component.ui.layout.adaptive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.NavigableSupportingPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.launch

/**
 * 적응형 다중 페인 — Material3 Adaptive 의 ListDetailPaneScaffold·SupportingPaneScaffold 와 ThreePaneScaffoldNavigator.
 *
 * 창 크기에서 계산한 PaneScaffoldDirective 를 그대로 쓰거나 분할 수를 강제해(폰에서 2분할, 태블릿에서 1분할)
 * 한 기기에서 두 배치를 비교하고, BackNavigationBehavior 네 가지가 뒤로가기마다 어디로 돌아가는지 본다.
 * 같은 폴더의 AdaptiveLayoutExampleUI 는 WindowSizeClass 로 목록·상세를 손수 나누는 방식이다.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AdaptivePaneScaffoldExampleUI(onBackEvent: () -> Unit) {
    var supporting by rememberSaveable { mutableStateOf(false) }
    var directiveMode by rememberSaveable { mutableStateOf(DirectiveMode.WINDOW) }
    var backMode by rememberSaveable { mutableStateOf(BackMode.SCAFFOLD_VALUE) }

    // V1(currentWindowAdaptiveInfo)은 1.3.0 에서 deprecated — L·XL 폭 클래스를 쓰는 V2 가 navigator 기본값과 같다
    val windowInfo = currentWindowAdaptiveInfoV2()
    val windowDirective = calculatePaneScaffoldDirective(windowInfo)
    val directive = directiveMode.partitions?.let { partitions ->
        windowDirective.copy(
            maxHorizontalPartitions = partitions,
            horizontalPartitionSpacerSize = if (partitions > 1) 24.dp else 0.dp
        )
    } ?: windowDirective

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        MainHeader(title = "Adaptive Pane Scaffold Example", onBackIconClicked = onBackEvent)
        ControlPanel(
            supporting = supporting,
            directiveMode = directiveMode,
            backMode = backMode,
            summary = windowSummary(windowInfo, windowDirective, directive),
            onSupporting = { supporting = it },
            onDirective = { directiveMode = it },
            onBackMode = { backMode = it }
        )
        if (supporting) {
            SupportingDemo(directive, backMode, Modifier.weight(1f))
        } else {
            ListDetailDemo(directive, backMode, Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun windowSummary(info: WindowAdaptiveInfo, fromWindow: PaneScaffoldDirective, used: PaneScaffoldDirective): String =
    "창 폭 ${widthClassName(info.windowSizeClass.minWidthDp)} → 창이 정한 분할 ${fromWindow.maxHorizontalPartitions} · 지금 쓰는 분할 ${used.maxHorizontalPartitions}"

// BackNavigationBehavior 는 생성자가 private 인 value class 다. 이 값을 '반환하는 함수'를 두면 debug(HotSwan) 컴파일이
// "has no primary constructor — cannot build dispatch return bridge" 로 실패한다(실측) → 함수 대신 표에서 꺼낸다
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private val backBehaviors: Map<BackMode, BackNavigationBehavior> = mapOf(
    BackMode.SCAFFOLD_VALUE to BackNavigationBehavior.PopUntilScaffoldValueChange,
    BackMode.CURRENT_DESTINATION to BackNavigationBehavior.PopUntilCurrentDestinationChange,
    BackMode.CONTENT to BackNavigationBehavior.PopUntilContentChange,
    BackMode.LATEST to BackNavigationBehavior.PopLatest
)

// ==================== 조작부 ====================

@Composable
private fun ControlPanel(
    supporting: Boolean,
    directiveMode: DirectiveMode,
    backMode: BackMode,
    summary: String,
    onSupporting: (Boolean) -> Unit,
    onDirective: (DirectiveMode) -> Unit,
    onBackMode: (BackMode) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFECEFF1)).padding(horizontal = 12.dp, vertical = 6.dp)) {
        ChipRow {
            Chip("List-Detail", !supporting) { onSupporting(false) }
            Chip("Supporting", supporting) { onSupporting(true) }
        }
        ChipRow {
            DirectiveMode.entries.forEach { mode -> Chip(mode.label, directiveMode == mode) { onDirective(mode) } }
        }
        ChipRow {
            BackMode.entries.forEach { mode -> Chip(mode.label, backMode == mode) { onBackMode(mode) } }
        }
        Text(text = summary, fontSize = 11.sp, color = Color(0xFF37474F), lineHeight = 15.sp)
    }
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) { content() }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(text = label, fontSize = 11.sp) })
}

@Composable
private fun StatusLine(first: String, second: String) {
    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF263238)).padding(horizontal = 12.dp, vertical = 4.dp)) {
        Text(text = first, fontSize = 11.sp, color = Color(0xFFECEFF1))
        Text(text = second, fontSize = 11.sp, color = Color(0xFFB0BEC5))
    }
}

// ==================== List-Detail ====================

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun ListDetailDemo(directive: PaneScaffoldDirective, backMode: BackMode, modifier: Modifier) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Int>(scaffoldDirective = directive)
    val scope = rememberCoroutineScope()
    val behavior = backBehaviors.getValue(backMode)
    val value = navigator.scaffoldValue
    val current = navigator.currentDestination
    val selected = current?.contentKey

    Column(modifier = modifier) {
        StatusLine(
            "List ${shortPaneValue(value[ListDetailPaneScaffoldRole.List].toString())} · " +
                "Detail ${shortPaneValue(value[ListDetailPaneScaffoldRole.Detail].toString())} · " +
                "Extra ${shortPaneValue(value[ListDetailPaneScaffoldRole.Extra].toString())}",
            "현재 ${listDetailRole(current?.pane)}${selected?.let { " · 글 $it" } ?: ""} · " +
                "canNavigateBack(${backMode.label}) = ${navigator.canNavigateBack(behavior)}"
        )
        NavigableListDetailPaneScaffold(
            navigator = navigator,
            defaultBackBehavior = behavior,
            modifier = Modifier.weight(1f),
            listPane = {
                AnimatedPane {
                    ListPane(selected = selected, backMode = backMode) { id ->
                        scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, id) }
                    }
                }
            },
            detailPane = {
                AnimatedPane {
                    DetailPane(
                        article = articleById(selected),
                        listHidden = value[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Hidden,
                        onBack = { scope.launch { navigator.navigateBack(behavior) } },
                        onExtra = { id -> scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Extra, id) } }
                    )
                }
            },
            extraPane = {
                AnimatedPane {
                    ExtraPane(article = articleById(selected)) { scope.launch { navigator.navigateBack(behavior) } }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun listDetailRole(role: ThreePaneScaffoldRole?): String = when (role) {
    ListDetailPaneScaffoldRole.List -> "List"
    ListDetailPaneScaffoldRole.Detail -> "Detail"
    ListDetailPaneScaffoldRole.Extra -> "Extra"
    else -> "없음"
}

@Composable
private fun ListPane(selected: Int?, backMode: BackMode, onSelect: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { GuideCard(backMode) }
        item { MeasuredCard() }
        items(paneArticles, key = { it.id }) { article ->
            val isSelected = article.id == selected
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(article.id) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = article.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                    Text(text = article.summary, fontSize = 11.sp, color = Color(0xFF616161), lineHeight = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun GuideCard(backMode: BackMode) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "읽는 법", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
            Bullet("글을 고르면 navigateTo(Detail, id). 창이 좁으면 상세가 목록 자리를 대신하고, 넓으면 옆에 열린다(위 상태 줄).")
            Bullet("'강제 2분할'은 폰에서도 두 페인을, '강제 1분할'은 태블릿에서도 한 페인을 보여 준다 — PaneScaffoldDirective.copy(maxHorizontalPartitions)")
            Bullet("지금 뒤로가기: ${backMode.label} — ${backMode.meaning}. 여러 글을 차례로 고른 뒤 시스템 뒤로가기로 비교한다.")
            Bullet("canNavigateBack 이 false 면 스캐폴드가 뒤로가기를 받지 않아 예제 화면 자체가 닫힌다.")
        }
    }
}

@Composable
private fun MeasuredCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "실측", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
            Bullet("Galaxy A72 세로(Compact): 창이 정한 분할 1 — 글을 고르면 List Hidden·Detail Expanded, 뒤로가기 한 번에 목록.")
            Bullet(
                "같은 폰 '강제 2분할'에서 글 1→2→3 뒤 뒤로가기: ScaffoldValueChange 는 보이는 구성이 안 바뀌어 canNavigateBack=false(예제가 닫힌다) · " +
                    "CurrentDestinationChange 는 한 번에 목록 · ContentChange 와 PopLatest 는 2 → 1 → 목록."
            )
            Bullet("글 1→2→2 처럼 같은 글이 겹치면 갈린다 — ContentChange 는 뒤로가기 한 번에 1, PopLatest 는 2(겹친 기록) → 1.")
            Bullet(
                "Pixel Tablet API 35 가로: V2 창 정보가 Large(1200dp~)라 3분할 — 고르기 전부터 List·Detail·Extra 가 모두 펼쳐져 " +
                    "기본 동작에선 뒤로가기가 스캐폴드에 걸리지 않는다. '강제 1분할'로 폰과 같은 흐름이 된다."
            )
            Bullet("Supporting: 넓은 창은 Main·Supporting 함께, 1분할은 navigateTo(Supporting) 가 본문 자리를 대신하고 뒤로가기로 돌아온다.")
        }
    }
}

@Composable
private fun DetailPane(article: PaneArticle?, listHidden: Boolean, onBack: () -> Unit, onExtra: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
        if (listHidden) {
            SmallButton("← 목록(navigateBack)", onBack)
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (article == null) {
            Text(text = "목록에서 글을 고르면 여기에 열린다.", fontSize = 13.sp, color = Color(0xFF757575))
            return@Column
        }
        Text(text = article.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = article.summary, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
        Spacer(modifier = Modifier.height(12.dp))
        SmallButton("관련 메모 열기(Extra)") { onExtra(article.id) }
    }
}

@Composable
private fun ExtraPane(article: PaneArticle?, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF3E5F5)).padding(16.dp)) {
        Text(text = "Extra 페인", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4A148C))
        Spacer(modifier = Modifier.height(6.dp))
        // 3분할 창(Large 폭)에서는 글을 고르기 전부터 Extra 페인이 펼쳐져 있다
        if (article == null) Bullet("상세에서 '관련 메모 열기'를 누르면 채워진다.") else article.related.forEach { Bullet(it) }
        Spacer(modifier = Modifier.height(8.dp))
        SmallButton("닫기(navigateBack)", onClose)
    }
}

// ==================== Supporting ====================

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun SupportingDemo(directive: PaneScaffoldDirective, backMode: BackMode, modifier: Modifier) {
    val navigator = rememberSupportingPaneScaffoldNavigator<Any>(scaffoldDirective = directive)
    val behavior = backBehaviors.getValue(backMode)
    val scope = rememberCoroutineScope()
    val value = navigator.scaffoldValue
    val supportingHidden = value[SupportingPaneScaffoldRole.Supporting] == PaneAdaptedValue.Hidden

    Column(modifier = modifier) {
        StatusLine(
            "Main ${shortPaneValue(value[SupportingPaneScaffoldRole.Main].toString())} · " +
                "Supporting ${shortPaneValue(value[SupportingPaneScaffoldRole.Supporting].toString())}",
            "canNavigateBack = ${navigator.canNavigateBack(behavior)}"
        )
        NavigableSupportingPaneScaffold(
            navigator = navigator,
            defaultBackBehavior = behavior,
            modifier = Modifier.weight(1f),
            mainPane = {
                AnimatedPane {
                    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
                        Text(text = "Main 페인", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "본문 옆에 보조 정보(메모·필터·관련 항목)를 두는 배치. 창이 넓으면 보조 페인이 처음부터 옆에 있고, " +
                                "좁으면 숨어 있다가 navigateTo(Supporting) 로 본문 자리를 대신한다.",
                            fontSize = 13.sp,
                            color = Color(0xFF424242),
                            lineHeight = 19.sp
                        )
                        if (supportingHidden) {
                            Spacer(modifier = Modifier.height(12.dp))
                            SmallButton("보조 페인 열기(navigateTo)") {
                                scope.launch { navigator.navigateTo(SupportingPaneScaffoldRole.Supporting) }
                            }
                        }
                    }
                }
            },
            supportingPane = {
                AnimatedPane {
                    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFE8F5E9)).padding(16.dp)) {
                        Text(text = "Supporting 페인", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                        Spacer(modifier = Modifier.height(6.dp))
                        Bullet("메모 1 — 창이 넓으면 본문과 함께 보인다")
                        Bullet("메모 2 — 좁은 창에서는 뒤로가기로 본문에 돌아간다")
                        if (value[SupportingPaneScaffoldRole.Main] == PaneAdaptedValue.Hidden) {
                            Spacer(modifier = Modifier.height(8.dp))
                            SmallButton("← 본문(navigateBack)") { scope.launch { navigator.navigateBack(behavior) } }
                        }
                    }
                }
            }
        )
    }
}

// ==================== 공통 UI ====================

@Composable
private fun SmallButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))) {
        Text(text = label, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
private fun Bullet(text: String) {
    Text(text = "· $text", fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp, modifier = Modifier.padding(vertical = 2.dp))
}
