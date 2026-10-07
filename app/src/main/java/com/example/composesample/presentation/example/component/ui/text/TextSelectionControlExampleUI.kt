package com.example.composesample.presentation.example.component.ui.text

import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.rememberSelectionState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.launch

/**
 * 텍스트 선택 제어 예제
 * - 프로젝트의 기존 `ui/text` 예제 8개는 전부 **입력(TextField)** 축이다. 이 예제는 반대쪽 —
 *   **읽기 전용 텍스트를 사용자가 선택하는 경로**와 그때 뜨는 플로팅 툴바를 다룬다.
 * - `SelectionContainer`(선택 허용) / `DisableSelection`(부분 예외) / `LocalTextToolbar` 교체(툴바 대체)
 *   세 축을 각각 실제로 눌러 볼 수 있게 구성한다.
 * - **선택된 텍스트를 코드로 읽기**: 1.12 의 `SelectionState`(`rememberSelectionState()` + `SelectionContainer(state)`)로
 *   selectedTexts 를 관찰하고 selectAll·clear·extendSelectionByWord·select(TextRange)·getSelectableTexts 를 부른다(3번 카드).
 *   1.11.4 까지는 `Selection` 과 selection/onSelectionChange 오버로드가 Kotlin `internal` 이라(바이트코드에는 public)
 *   커스텀 툴바로 복사 액션을 감싸고 클립보드를 되읽는 우회로뿐이었다(4번 카드).
 * - **그 커스텀 툴바는 기본값에서 호출되지 않는다** — Compose 1.11 이 선택 컨텍스트 메뉴를
 *   `text.contextmenu` 로 옮겼고 `ComposeFoundationFlags.isNewContextMenuEnabled` 기본값이 true 이기 때문이다.
 *   1.11.4·1.12.1 모두 실기기에서 같은 값(기본값: showMenu 0회 / 플래그를 끄면 2회, 액션은 copy·selectAll).
 * - 1.11.4 → 1.12.1 에서 바뀐 것과 그대로인 것은 5번 카드에 실측으로 정리한다.
 * - 참고 자료(URL/핵심 개념)는 같은 폴더의 exampleGuide.kt 의 "Text Selection Control" 섹션 참고.
 */

private const val SAMPLE_TEXT =
    "Compose 에서 화면에 보이는 텍스트는 기본적으로 선택할 수 없다. " +
        "선택은 컨테이너가 열어 주는 기능이며, 그 안에서 다시 일부만 잠글 수 있다."

private const val ARTICLE_BODY =
    "선택 기능을 켜면 본문뿐 아니라 그 안에 있는 모든 텍스트가 함께 딸려 온다. " +
        "그래서 복사했을 때 원하지 않는 조각이 섞이는 일이 생긴다."

// ==================== 커스텀 툴바 ====================

/** showMenu 로 들어온 요청을 그대로 담아 두는 그릇. 어떤 액션이 제공됐는지는 null 여부로 드러난다. */
private data class ToolbarRequest(
    val rect: Rect,
    val onCopy: (() -> Unit)?,
    val onPaste: (() -> Unit)?,
    val onCut: (() -> Unit)?,
    val onSelectAll: (() -> Unit)?
)

/**
 * 플랫폼 플로팅 툴바 대신 우리가 받아 보는 구현.
 *
 * `LocalTextToolbar` 를 이 구현으로 갈아끼우면 선택 매니저가 플랫폼 툴바 대신 여기를 호출하므로,
 * 툴바를 화면 안에 원하는 모양으로 그릴 수 있다.
 */
private class RecordingTextToolbar(
    private val onShow: (ToolbarRequest) -> Unit,
    private val onHide: () -> Unit
) : TextToolbar {

    /** showMenu 가 몇 번 불렸는지. 0 이면 이 툴바가 아예 쓰이지 않았다는 뜻이다. */
    var callCount: Int = 0
        private set

    override var status: TextToolbarStatus = TextToolbarStatus.Hidden
        private set

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        callCount++
        status = TextToolbarStatus.Shown
        onShow(
            ToolbarRequest(
                rect = rect,
                onCopy = onCopyRequested,
                onPaste = onPasteRequested,
                onCut = onCutRequested,
                onSelectAll = onSelectAllRequested
            )
        )
    }

    override fun hide() {
        status = TextToolbarStatus.Hidden
        onHide()
    }
}

// ==================== 화면 ====================

@Composable
fun TextSelectionControlExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "텍스트 선택 제어",
            onBackIconClicked = onBackEvent
        )

        // LazyColumn 이 아니라 Column 인 이유: Lazy 항목 안의 SelectionContainer 는 재생성 뒤 선택을 되살리지 못한다(5번 카드 실측)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BasicsCard()
            DisableSelectionCard()
            SelectionStateCard()
            CustomToolbarCard()
            VersionGapCard()
            PitfallCard()
        }
    }
}

// ==================== 1. 기본 ====================

@Composable
private fun BasicsCard() {
    SectionCard(title = "1. 기본값은 '선택 불가'다") {
        BodyText(
            "TextField 는 처음부터 선택·복사가 되지만, 화면에 그려 놓은 Text 는 길게 눌러도 아무 일도 일어나지 않는다. " +
                "선택을 열어 주는 것은 텍스트가 아니라 **감싸는 컨테이너**다. 아래 두 문단을 각각 길게 눌러 비교해 보라."
        )
        Spacer(modifier = Modifier.height(10.dp))

        LabeledBox(label = "그냥 Text — 선택되지 않는다", color = Color(0xFFECEFF1)) {
            Text(text = SAMPLE_TEXT, fontSize = 12.sp, color = Color(0xFF37474F), lineHeight = 18.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LabeledBox(label = "SelectionContainer 로 감싼 Text — 선택된다", color = Color(0xFFE8F5E9)) {
            SelectionContainer {
                Text(text = SAMPLE_TEXT, fontSize = 12.sp, color = Color(0xFF1B5E20), lineHeight = 18.sp)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        BodyText(
            "컨테이너는 자기 안의 모든 선택 가능한 텍스트를 하나의 선택 영역으로 묶는다. " +
                "여러 Text 에 걸쳐 드래그하면 그 사이가 한 번에 선택되는 것도 이 때문이다."
        )
    }
}

// ==================== 2. 부분 제외 ====================

@Composable
private fun DisableSelectionCard() {
    SectionCard(title = "2. 일부만 빼기 — DisableSelection") {
        BodyText(
            "컨테이너가 전부 열어 주는 것이 문제일 때가 있다. 본문을 복사했더니 타임스탬프나 라벨이 따라오는 경우다. " +
                "`DisableSelection` 으로 감싼 부분은 같은 컨테이너 안에 있어도 선택에서 빠진다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        LabeledBox(label = "전체를 드래그해 보라 — 회색 줄만 빠진다", color = Color(0xFFF3E5F5)) {
            SelectionContainer {
                Column {
                    DisableSelection {
                        Text(
                            text = "2026. 09. 11  ·  선택에서 제외된 메타데이터",
                            fontSize = 10.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = ARTICLE_BODY,
                        fontSize = 12.sp,
                        color = Color(0xFF4A148C),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    DisableSelection {
                        Text(
                            text = "— 광고 문구도 복사되지 않는다",
                            fontSize = 10.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        BodyText(
            "제외된 조각은 **드래그가 지나가도 하이라이트되지 않고 복사 결과에도 들어가지 않는다.** " +
                "선택을 막는 것이지 텍스트를 숨기는 것이 아니므로 화면에는 그대로 보인다."
        )
    }
}

// ==================== 3. 선택 내용 읽기 ====================

@Composable
private fun SelectionStateCard() {
    // 같은 SelectionState 를 두 컨테이너에 넘기면 error() — 컨테이너마다 하나씩 만든다
    val state = rememberSelectionState()
    // getSelectableTexts() 는 레이아웃이 끝나야 읽을 수 있어 컴포지션 중에는 부르지 않고 버튼에서 읽어 둔다
    var selectableTexts by remember { mutableStateOf<List<AnnotatedString>?>(null) }

    SectionCard(title = "3. 선택된 내용을 코드로 읽기 — SelectionState (1.12)") {
        BodyText(
            "1.12 부터 SelectionContainer 에 상태 객체를 넘길 수 있다. 사용자가 무엇을 선택했는지 상태로 관찰하고, " +
                "선택을 코드로 바꿀 수도 있다. 아래 문단을 길게 눌러 드래그하거나 버튼을 눌러 보라."
        )
        Spacer(modifier = Modifier.height(8.dp))
        CodeBlock(
            "val state = rememberSelectionState()   // rememberSaveable\n" +
                "SelectionContainer(state = state) { ... }\n\n" +
                "state.selectedTexts            // List<AnnotatedString>, 스냅샷 상태\n" +
                "state.selectAll() / clear() / extendSelectionByWord()\n" +
                "state.select(TextRange(0, 12))   // 컨테이너 전체 기준 범위\n" +
                "state.getSelectableTexts()       // 컴포지션 중 호출 금지"
        )
        Spacer(modifier = Modifier.height(8.dp))

        LabeledBox(label = "SelectionContainer(state = state) — 회색 줄은 DisableSelection", color = Color(0xFFE0F2F1)) {
            SelectionContainer(state = state) {
                Column {
                    DisableSelection {
                        Text(text = "2026. 10. 07  ·  선택에서 제외된 메타데이터", fontSize = 10.sp, color = Color(0xFF9E9E9E))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = SAMPLE_TEXT, fontSize = 12.sp, color = Color(0xFF004D40), lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = ARTICLE_BODY, fontSize = 12.sp, color = Color(0xFF004D40), lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    DisableSelection {
                        Text(text = "— 광고 문구는 선택되지 않는다", fontSize = 10.sp, color = Color(0xFF9E9E9E))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DemoButton("전체 선택", Color(0xFF00796B)) { state.selectAll() }
            DemoButton("단어 확장", Color(0xFF00897B)) { state.extendSelectionByWord() }
            DemoButton("앞 12자", Color(0xFF26A69A)) { state.select(TextRange(0, 12)) }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DemoButton("지우기", Color(0xFF546E7A)) { state.clear() }
            DemoButton("선택 가능 텍스트 읽기", Color(0xFF37474F)) { selectableTexts = state.getSelectableTexts() }
        }

        Spacer(modifier = Modifier.height(10.dp))
        val selected = state.selectedTexts
        ResultRow("selectedTexts", "${selected.size}조각 · ${selected.sumOf { it.length }}자")
        selected.forEachIndexed { index, text ->
            ResultRow("  [$index]", "\"${text.text.ellipsize(36)}\"")
        }
        ResultRow(
            "getSelectable…",
            selectableTexts?.let { texts -> "${texts.size}개 · ${texts.sumOf { it.length }}자 (DisableSelection 제외)" }
                ?: "버튼을 눌러 읽는다"
        )

        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "1.11.4 까지는 왜 못 읽었나", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
        Spacer(modifier = Modifier.height(6.dp))
        BodyText(
            "1.11.4 의 바이트코드에도 선택 상태를 넘겨받는 오버로드 `SelectionContainer(modifier, selection, onSelectionChange, children)` " +
                "와 `Selection` 클래스가 들어 있었지만, 그대로 쓰면 컴파일되지 않았다. 이 예제를 처음 만들 때 받은 오류다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        CodeBlock(
            "e: Cannot access 'data class Selection : Any': it is internal in file.\n" +
                "e: Cannot access 'fun SelectionContainer(modifier: Modifier = ...,\n" +
                "   selection: Selection?, onSelectionChange: (Selection?) -> Unit,\n" +
                "   children: ComposableFunction0<Unit>): Unit': it is internal in file."
        )
        Spacer(modifier = Modifier.height(6.dp))
        BodyText(
            "Kotlin 의 internal 은 JVM 바이트코드에서 public 으로 컴파일되므로 javap 로는 쓸 수 있을 것처럼 보인다. " +
                "1.12.1 에서도 Selection 은 여전히 internal 이고, 공개된 것은 그것을 감싼 SelectionState 다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        TableRow("확인", "javap 표기 / Kotlin 실제", isHeader = true)
        TableRow("Selection", "public final class / internal (1.12.1 도 동일)")
        TableRow("오버로드", "public static final void / internal")
        TableRow("SelectionState", "1.12 신규 — public")
    }
}

/** 결과 행이 한 줄을 넘지 않도록 자른다 */
private fun String.ellipsize(max: Int): String = if (length <= max) this else take(max) + "…"

// ==================== 4. 커스텀 툴바 ====================

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CustomToolbarCard() {
    var request by remember { mutableStateOf<ToolbarRequest?>(null) }
    var copiedText by remember { mutableStateOf<String?>(null) }
    var legacyPath by remember { mutableStateOf(false) }
    var observedCalls by remember { mutableIntStateOf(0) }
    val toolbar = remember {
        RecordingTextToolbar(
            onShow = { request = it },
            onHide = { request = null }
        )
    }
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 전역 플래그를 건드리므로 화면을 떠날 때 반드시 기본값으로 되돌린다.
    DisposableEffect(Unit) {
        onDispose { ComposeFoundationFlags.isNewContextMenuEnabled = true }
    }

    SectionCard(title = "4. 플로팅 툴바 가로채기 — 기본값에서는 안 된다") {
        BodyText(
            "선택이 끝나면 '복사/전체 선택' 메뉴가 뜬다. 오래된 문서들은 `LocalTextToolbar` 를 갈아끼우면 " +
                "그 메뉴를 대체할 수 있다고 말하는데, **이 버전에서는 그대로 하면 아무 일도 일어나지 않는다.**"
        )
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "Compose 1.11 의 foundation 은 선택 컨텍스트 메뉴를 새 시스템(`text.contextmenu`)으로 옮겼고, " +
                "그 전환은 `ComposeFoundationFlags.isNewContextMenuEnabled` 가 정한다 — **기본값은 true** 다. " +
                "true 인 동안 선택 매니저는 `LocalTextToolbar` 를 아예 호출하지 않는다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        ResultRow("플래그", ComposeFoundationFlags.isNewContextMenuEnabled.toString())
        ResultRow("showMenu 호출", "${observedCalls}회")
        ResultRow("status", toolbar.status.toString())

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DemoButton(
                text = if (legacyPath) "기본(새 경로)로 되돌리기" else "예전 경로로 전환",
                color = Color(0xFF6A1B9A)
            ) {
                legacyPath = !legacyPath
                ComposeFoundationFlags.isNewContextMenuEnabled = !legacyPath
                request = null
                copiedText = null
            }
            DemoButton(text = "측정값 새로고침", color = Color(0xFF455A64)) {
                observedCalls = toolbar.callCount
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        // 플래그를 바꾼 뒤에는 선택 기계를 새로 만들어야 새 값이 반영된다 → key 로 서브트리를 재생성한다.
        key(legacyPath) {
            LabeledBox(
                label = if (legacyPath) {
                    "예전 경로 — 길게 눌러 선택하면 아래 값이 채워진다"
                } else {
                    "기본 경로 — 길게 눌러도 showMenu 는 오지 않는다"
                },
                color = if (legacyPath) Color(0xFFFFF3E0) else Color(0xFFECEFF1)
            ) {
                CompositionLocalProvider(LocalTextToolbar provides toolbar) {
                    SelectionContainer {
                        Text(
                            text = SAMPLE_TEXT,
                            fontSize = 12.sp,
                            color = if (legacyPath) Color(0xFFE65100) else Color(0xFF37474F),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        val current = request
        if (current == null) {
            CaptionText("showMenu 가 아직 호출되지 않았다. 선택 후 \"측정값 새로고침\"을 눌러 호출 횟수를 확인해 보라.")
        } else {
            ResultRow("rect", "left=%.0f, top=%.0f".format(current.rect.left, current.rect.top))
            ResultRow(
                "제공된 액션",
                listOfNotNull(
                    current.onCopy?.let { "copy" },
                    current.onPaste?.let { "paste" },
                    current.onCut?.let { "cut" },
                    current.onSelectAll?.let { "selectAll" }
                ).joinToString(", ").ifEmpty { "(없음)" }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                current.onCopy?.let { action ->
                    DemoButton("복사 후 읽기", Color(0xFFEF6C00)) {
                        // 복사 액션을 실행한 뒤 클립보드를 되읽는다 — 1.11 까지 선택된 문자열을 얻던 우회로(1.12 는 3번 카드).
                        action()
                        scope.launch {
                            val clipData = clipboard.getClipEntry()?.clipData
                            copiedText = if (clipData != null && clipData.itemCount > 0) {
                                clipData.getItemAt(0).coerceToText(context).toString()
                            } else {
                                null
                            }
                        }
                    }
                }
                current.onSelectAll?.let { action -> DemoButton("전체 선택", Color(0xFFF9A825)) { action() } }
            }
            copiedText?.let { text ->
                Spacer(modifier = Modifier.height(6.dp))
                ResultRow("클립보드", "${text.length}자")
                CodeBlock(text.ifEmpty { "(빈 문자열)" })
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        TableRow("실측", "실기기(SM-A725F/Android 13) — 1.11.4·1.12.1 동일", isHeader = true)
        TableRow("기본값", "플래그 true — showMenu 0회, status=Hidden")
        TableRow("전환 후", "플래그 false + 서브트리 재생성 — showMenu 2회, status=Shown")
        TableRow("액션", "copy, selectAll 만 온다(읽기 전용이라 cut/paste 없음)")
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "즉 커스텀 툴바는 **받은 것만** 그릴 수 있고, 그 이전에 **호출이 오는지부터** 확인해야 한다. " +
                "1.11 까지는 이것이 \"선택된 텍스트 읽기\"의 유일한 우회로였다 — 복사 액션을 우리가 감싸고 있으므로 " +
                "실행한 뒤 클립보드를 읽으면 그 문자열이 나온다. 1.12 에서는 3번 카드의 SelectionState 가 이 일을 대신한다."
        )
        CaptionText(
            "플래그는 프로세스 전역이다. 이 카드는 화면을 떠날 때 DisposableEffect 로 기본값(true)으로 되돌린다."
        )
    }
}

// ==================== 5. 1.11.4 → 1.12.1 ====================

@Composable
private fun VersionGapCard() {
    SectionCard(title = "5. 1.11.4 → 1.12.1 에서 바뀐 것과 그대로인 것") {
        BodyText(
            "이 예제는 foundation 1.11.4 로 처음 만들었고, 2026-09-29 compose-bom 2026.09.00 상향으로 1.12.1 이 되었다. " +
                "1~4번 카드를 1.12.1 에서 같은 조작으로 다시 재고 소스를 대조한 결과다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("구분", "1.11.4 / 1.12.1", isHeader = true)
        TableRow("상태 객체", "없음(Selection 이 internal) / SelectionState + rememberSelectionState()")
        TableRow("선택 텍스트", "클립보드 우회만 가능 / selectedTexts — 조각 리스트, 스냅샷 상태")
        TableRow("전체 텍스트", "없음 / getSelectableTexts() — 함수, 컴포지션 밖에서만")
        TableRow("조작", "없음 / selectAll · clear · extendSelectionByWord · select(TextRange)")
        TableRow("재생성", "기본 컨테이너가 remember — 선택이 사라짐 / rememberSaveable — 하이라이트·조작까지 복원(Lazy 항목 안은 예외)")
        TableRow("1·2번", "길게 누르면 단어 선택 + 복사·모두 선택 메뉴 / 동일(실측)")
        TableRow("4번", "플래그 true: showMenu 0회 · false: 2회, copy·selectAll / 동일(실측)")
        Spacer(modifier = Modifier.height(10.dp))
        TableRow("재생성 실측", "3번 카드 '앞 12자' 후 다크 모드 전환(SM-A725F)", isHeader = true)
        TableRow("이 화면", "Column — 1조각 그대로, 하이라이트·핸들 복원, 단어 확장 12→14자 정상")
        TableRow("Lazy 항목", "상태를 항목 안에서 만들면 0조각(debug·release 동일)")
        TableRow("Lazy+끌어올림", "상태를 LazyColumn 밖에서 만들어 넘기면 selectedTexts 만 1조각, 하이라이트 없음·단어 확장 무반응")
        TableRow("스크롤", "Lazy 항목을 화면 밖으로 보냈다 와도 유지(포커스 항목은 고정)")
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "Saver 로그를 붙여 보니 Lazy 항목 안에서는 onStop 때 1조각을 저장한 뒤, 해체 중에 선택이 풀린 0조각이 " +
                "같은 SaveableStateHolder 맵에 다시 저장되어 복원값이 0조각이 됐다(구성 변경 재생성은 Bundle 을 직렬화하지 않고 넘긴다). " +
                "이 화면이 LazyColumn 대신 Column 을 쓰는 이유다."
        )
        Spacer(modifier = Modifier.height(10.dp))
        BodyText(
            "선택 범위를 정하는 SelectionContainer·DisableSelection 과 툴바 가로채기는 그대로이고, " +
                "\"선택된 텍스트를 코드로 읽고 조작한다\"는 축만 1.12 에서 새로 열렸다. " +
                "기본 SelectionContainer 도 내부에서 rememberSelectionState() 를 쓰므로 재생성 복원 여부는 상태를 넘기지 않아도 같다."
        )
    }
}

// ==================== 6. 함정 ====================

@Composable
private fun PitfallCard() {
    SectionCard(title = "6. 걸리는 것들") {
        PitfallRow(
            "SelectionContainer 를 크게 감싸면 다 딸려온다",
            "컨테이너 하나가 그 안의 모든 텍스트를 한 선택 영역으로 묶는다. 화면 전체를 감싸 두면 " +
                "머리말·버튼 라벨까지 복사 결과에 섞인다. 감쌀 범위를 먼저 정하고, 예외는 DisableSelection 으로 판다."
        )
        PitfallRow(
            "DisableSelection 은 숨기는 것이 아니다",
            "화면에는 그대로 보이고 스크린 리더도 읽는다. 민감한 값을 가리는 수단으로 쓰면 안 된다."
        )
        PitfallRow(
            "바이트코드의 public 을 믿으면 안 된다",
            "Kotlin 의 internal 은 JVM 에서 public 으로 컴파일된다. javap 로 보이는 Selection·controlled 오버로드는 " +
                "실제로는 internal 이라 앱에서 쓸 수 없다(3번 카드의 오류 문구)."
        )
        PitfallRow(
            "클립보드 되읽기는 공짜가 아니다",
            "Android 12+ 는 앱이 클립보드를 읽으면 사용자에게 알림 토스트를 띄우고, 사용자의 클립보드 내용도 덮어쓴다. " +
                "1.12 부터는 SelectionState.selectedTexts 로 읽으면 되므로 이 우회로를 쓸 이유가 없다."
        )
        PitfallRow(
            "SelectionState 하나에 컨테이너 하나",
            "같은 상태 객체를 두 SelectionContainer 에 넘기면 error() 로 터진다. 컨테이너마다 rememberSelectionState() 를 따로 만든다."
        )
        PitfallRow(
            "getSelectableTexts()·select() 는 컴포지션 중에 부르지 않는다",
            "레이아웃이 끝나야 결과가 나오는 함수다. 버튼·효과 안에서 부르고, 결과는 상태에 담아 그린다."
        )
        PitfallRow(
            "Lazy 항목 안의 선택은 재생성을 넘지 못한다",
            "SelectionState 는 rememberSaveable 이지만, LazyColumn 항목 안에 둔 컨테이너는 다크 모드·언어 변경 같은 재생성 뒤 " +
                "선택이 0조각으로 돌아온다. 상태만 Lazy 밖으로 끌어올리면 selectedTexts 는 남아도 화면의 선택은 끊긴다. " +
                "선택을 살려야 하는 화면은 컨테이너를 Lazy 밖(Column)에 둔다."
        )
        PitfallRow(
            "Lazy 리스트 안에서는 컴포즈된 항목만",
            "SelectionContainer 안에 LazyColumn 을 두면 selectAll 과 복사가 화면에 컴포즈된 텍스트까지만 닿는다."
        )
        PitfallRow(
            "커스텀 툴바는 받은 액션만 그릴 수 있다",
            "showMenu 의 콜백은 nullable 이다. 읽기 전용 선택이면 cut/paste 가 비어 온다. " +
                "null 인 자리를 빈 버튼으로 그려 두면 눌러도 아무 일이 없는 UI 가 된다."
        )
        PitfallRow(
            "LocalTextToolbar 교체는 기본값에서 무시된다",
            "Compose 1.11 은 선택 컨텍스트 메뉴를 text.contextmenu 로 옮겼고 " +
                "ComposeFoundationFlags.isNewContextMenuEnabled 기본값이 true 다. " +
                "예전 방식대로 툴바만 갈아끼우면 showMenu 가 한 번도 오지 않는다(실측 0회)."
        )
        PitfallRow(
            "전역 플래그를 예제가 바꾸면 원복해야 한다",
            "ComposeFoundationFlags 는 프로세스 전역 가변 상태라, 바꾼 채로 화면을 떠나면 다른 화면의 " +
                "선택 동작까지 바뀐다. 이 예제는 DisposableEffect 로 되돌린다."
        )
        PitfallRow(
            "툴바 교체 범위는 CompositionLocal 범위다",
            "LocalTextToolbar 를 provide 한 서브트리 안에서만 대체된다. 화면 전체에 적용하려면 " +
                "최상위에서 감싸야 하고, 그러면 그 화면의 TextField 선택 툴바까지 함께 바뀐다."
        )
    }
}

// ==================== 공통 요소 ====================

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun LabeledBox(
    label: String,
    color: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF616161))
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun BodyText(text: String) {
    Text(text = text, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
}

@Composable
private fun CaptionText(text: String) {
    Text(text = text, fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 16.sp)
}

@Composable
private fun CodeBlock(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF263238), RoundedCornerShape(6.dp))
            .horizontalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFECEFF1),
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun TableRow(
    first: String,
    second: String,
    isHeader: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) Color(0xFFEEEEEE) else Color.Transparent)
            .padding(vertical = 5.dp, horizontal = 6.dp)
    ) {
        Text(
            text = first,
            modifier = Modifier.width(72.dp),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF424242)
        )
        Text(
            text = second,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF616161),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.width(92.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF616161),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun PitfallRow(title: String, description: String) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
        Spacer(modifier = Modifier.height(2.dp))
        CaptionText(description)
    }
}

@Composable
private fun DemoButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 11.sp, color = Color.White)
    }
}
