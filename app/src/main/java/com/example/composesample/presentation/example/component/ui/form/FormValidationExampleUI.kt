package com.example.composesample.presentation.example.component.ui.form

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.flow.collectLatest

/**
 * Compose 폼 상태·검증 예제
 * - 프로젝트의 기존 텍스트 예제 8개는 전부 "필드 하나"가 축이다(최대 길이·구문 강조·리치 콘텐츠 등).
 *   이 예제는 그 위 계층인 **여러 필드가 하나의 폼으로 묶일 때** 생기는 문제만 다룬다.
 * - 검증 시점(입력 즉시 / 포커스 이탈 / 제출)이 사용자 경험을 어떻게 바꾸는지 같은 규칙으로 나란히 대조하고,
 *   그 차이를 "에러 표시가 몇 번 뒤집혔는가"로 화면에서 실측한다.
 * - 라이브러리 없이 순수 Compose(TextFieldState + material3)로만 구성한다.
 * - 참고 URL 과 개념 정리는 같은 폴더의 exampleGuide.kt 참조
 */

// ==================== 검증 규칙 (UI 밖에서 단독 검증 가능한 순수 함수) ====================

/** 필드 하나의 검증 결과. 에러가 없으면 null 을 담는다. */
internal data class FieldError(val message: String)

internal fun validateName(value: String): FieldError? = when {
    value.isBlank() -> FieldError("이름을 입력해 주세요")
    value.length < 2 -> FieldError("2자 이상 입력해 주세요")
    else -> null
}

/**
 * 이메일 형식 검사.
 *
 * 정규식 하나로 RFC 를 만족시키려는 시도는 하지 않는다 — 폼 검증의 목적은 오타를 잡는 것이지
 * 주소의 유효성을 증명하는 것이 아니다(진짜 확인은 인증 메일 발송이다).
 */
internal fun validateEmail(value: String): FieldError? = when {
    value.isBlank() -> FieldError("이메일을 입력해 주세요")
    !value.contains('@') -> FieldError("@ 가 없습니다")
    value.substringAfterLast('@').let { it.isEmpty() || !it.contains('.') } ->
        FieldError("도메인 형식을 확인해 주세요")
    else -> null
}

internal fun validatePassword(value: String): FieldError? = when {
    value.length < 8 -> FieldError("8자 이상이어야 합니다")
    value.none { it.isDigit() } -> FieldError("숫자를 1개 이상 포함해 주세요")
    else -> null
}

internal fun validateConfirm(password: String, confirm: String): FieldError? = when {
    confirm.isEmpty() -> FieldError("한 번 더 입력해 주세요")
    confirm != password -> FieldError("비밀번호가 일치하지 않습니다")
    else -> null
}

/** 검증을 언제 수행할지 */
internal enum class ValidationTiming(val label: String, val description: String) {
    EAGER("입력 즉시", "글자마다 검사 — 다 치기도 전에 빨개진다"),
    ON_BLUR("포커스 이탈", "필드를 벗어날 때 검사 — 입력 중에는 조용하다"),
    ON_SUBMIT("제출 시", "제출을 눌러야 검사 — 어디가 틀렸는지 늦게 안다")
}

// ==================== 화면 ====================

@Composable
fun FormValidationExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "폼 상태와 검증",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { FormConceptCard() }
            item { TimingComparisonCard() }
            item { RealFormCard() }
            item { SubmitGatingCard() }
            item { LayoutJumpCard() }
            item { FormPitfallCard() }
        }
    }
}

// ==================== 1. 개념 ====================

@Composable
private fun FormConceptCard() {
    FormSectionCard(title = "1. 필드 N개가 아니라 폼 1개") {
        BodyText(
            "필드마다 상태를 따로 들고 있으면 \"제출 가능한가\"를 물을 곳이 없어진다. " +
                "필드 값은 각자 갖되(TextFieldState), **에러와 제출 가능 여부는 폼 단위로 계산**하는 것이 " +
                "이 예제가 다루는 구조다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        TableRow("검증 시점", "성격", isHeader = true)
        ValidationTiming.entries.forEach { timing ->
            TableRow(timing.label, timing.description)
        }

        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "정답은 하나가 아니다. 실무에서 가장 흔한 조합은 \"처음에는 포커스 이탈, 한 번 에러가 난 " +
                "뒤부터는 입력 즉시\"인데, 2번 카드에서 세 방식을 같은 규칙으로 나란히 놓고 비교한다."
        )
    }
}

// ==================== 2. 검증 시점 대조 ====================

@Composable
private fun TimingComparisonCard() {
    FormSectionCard(title = "2. 같은 규칙, 다른 시점 (실측)") {
        BodyText(
            "세 필드 모두 이메일 규칙이 같다. 다른 것은 검사 시점뿐이다. " +
                "\"a\" → \"a@\" → \"a@b\" → \"a@b.com\" 처럼 천천히 입력해 보면 차이가 바로 드러난다."
        )
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            "아래 '에러 전환' 은 에러 표시가 켜지거나 꺼진 횟수다. 입력 즉시 검증이 사용자에게 " +
                "몇 번이나 빨간 줄을 보여주는지 그대로 센다."
        )
        Spacer(modifier = Modifier.height(12.dp))

        ValidationTiming.entries.forEach { timing ->
            TimingField(timing = timing)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TimingField(timing: ValidationTiming) {
    val state = rememberTextFieldState()
    var error by remember { mutableStateOf<FieldError?>(null) }
    var touched by remember { mutableStateOf(false) }
    var flipCount by remember { mutableIntStateOf(0) }

    // 에러 표시가 뒤집힌 횟수를 센다. 표시 여부가 바뀔 때만 증가한다.
    val showError = error != null
    var lastShown by remember { mutableStateOf(false) }
    SideEffect {
        if (lastShown != showError) {
            lastShown = showError
            flipCount++
        }
    }

    // 입력 즉시 검증: 텍스트가 바뀔 때마다 규칙을 돌린다.
    if (timing == ValidationTiming.EAGER) {
        LaunchedEffectOnText(state) { text -> error = validateEmail(text) }
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = timing.label,
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF37474F)
            )
            Text(
                text = "에러 전환 ${flipCount}회",
                fontSize = 11.sp,
                color = Color(0xFF757575)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    // 포커스 이탈 검증: 한 번이라도 들어갔다 나온 필드만 검사한다.
                    if (timing == ValidationTiming.ON_BLUR) {
                        if (focusState.isFocused) {
                            touched = true
                        } else if (touched) {
                            error = validateEmail(state.text.toString())
                        }
                    }
                },
            label = { Text("이메일") },
            isError = showError,
            supportingText = { Text(error?.message ?: "example@domain.com") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )
        if (timing == ValidationTiming.ON_SUBMIT) {
            Spacer(modifier = Modifier.height(6.dp))
            DemoButton(text = "이 필드만 제출 검사", color = Color(0xFF546E7A)) {
                error = validateEmail(state.text.toString())
            }
        }
    }
}

/** TextFieldState 의 텍스트 변화를 구독한다. onValueChange 가 없는 API 라 snapshotFlow 를 쓴다. */
@Composable
private fun LaunchedEffectOnText(state: TextFieldState, onText: (String) -> Unit) {
    androidx.compose.runtime.LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }
            .collectLatest { text -> onText(text) }
    }
}

// ==================== 3. 실제 폼 ====================

@Composable
private fun RealFormCard() {
    val nameState = rememberTextFieldState()
    val emailState = rememberTextFieldState()
    val passwordState = rememberTextFieldState()
    val confirmState = rememberTextFieldState()

    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val confirmFocus = remember { FocusRequester() }

    var submitted by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("아직 제출하지 않음") }

    val name = nameState.text.toString()
    val email = emailState.text.toString()
    val password = passwordState.text.toString()
    val confirm = confirmState.text.toString()

    val nameError = if (submitted) validateName(name) else null
    val emailError = if (submitted) validateEmail(email) else null
    val passwordError = if (submitted) validatePassword(password) else null
    val confirmError = if (submitted) validateConfirm(password, confirm) else null

    FormSectionCard(title = "3. 4개 필드를 하나의 폼으로") {
        BodyText(
            "IME 액션으로 다음 필드로 넘어가고(마지막은 Done), 제출을 누른 뒤부터 에러를 표시한다. " +
                "비밀번호 확인처럼 **다른 필드에 의존하는 규칙**은 필드 단위가 아니라 폼 단위로만 계산할 수 있다."
        )
        Spacer(modifier = Modifier.height(12.dp))

        FormField(
            state = nameState,
            label = "이름",
            error = nameError,
            hint = "2자 이상",
            imeAction = ImeAction.Next,
            onNext = { emailFocus.requestFocus() }
        )
        FormField(
            state = emailState,
            label = "이메일",
            error = emailError,
            hint = "example@domain.com",
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            focusRequester = emailFocus,
            onNext = { passwordFocus.requestFocus() }
        )
        FormField(
            state = passwordState,
            label = "비밀번호",
            error = passwordError,
            hint = "8자 이상, 숫자 포함",
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
            focusRequester = passwordFocus,
            onNext = { confirmFocus.requestFocus() }
        )
        FormField(
            state = confirmState,
            label = "비밀번호 확인",
            error = confirmError,
            hint = "위와 동일하게",
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            focusRequester = confirmFocus,
            onNext = { }
        )

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(text = "제출", color = Color(0xFF1976D2)) {
                submitted = true
                val errors = listOfNotNull(
                    validateName(name),
                    validateEmail(email),
                    validatePassword(password),
                    validateConfirm(password, confirm)
                )
                result = if (errors.isEmpty()) {
                    "통과 — 서버로 보낼 수 있는 상태"
                } else {
                    "실패 ${errors.size}건: " + errors.joinToString(" / ") { it.message }
                }
            }
            DemoButton(text = "초기화", color = Color(0xFF757575)) {
                listOf(nameState, emailState, passwordState, confirmState).forEach { it.clearText() }
                submitted = false
                result = "아직 제출하지 않음"
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        ResultRow("결과", result)
    }
}

@Composable
private fun FormField(
    state: TextFieldState,
    label: String,
    error: FieldError?,
    hint: String,
    imeAction: ImeAction,
    onNext: () -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    focusRequester: FocusRequester? = null
) {
    OutlinedTextField(
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .let { if (focusRequester != null) it.focusRequester(focusRequester) else it },
        label = { Text(label) },
        isError = error != null,
        supportingText = { Text(error?.message ?: hint) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        onKeyboardAction = { onNext() }
    )
    Spacer(modifier = Modifier.height(4.dp))
}

// ==================== 4. 제출 게이팅 ====================

@Composable
private fun SubmitGatingCard() {
    val state = rememberTextFieldState()
    val text = state.text.toString()

    // 매 입력마다 계산되는 값 — 이 값을 읽는 컴포저블은 글자마다 리컴포지션된다.
    val plainEnabled = text.length >= 8

    // 결과가 바뀔 때만 알리는 값 — 8자 경계를 넘는 순간에만 리컴포지션된다.
    val derivedEnabled by remember {
        derivedStateOf { state.text.length >= 8 }
    }

    FormSectionCard(title = "4. 제출 버튼은 왜 derivedStateOf 인가 (실측)") {
        BodyText(
            "\"8자 이상이면 활성화\"는 글자를 칠 때마다 다시 계산되지만, **결과가 바뀌는 순간은 몇 번 안 된다.** " +
                "아래 두 버튼은 같은 조건을 쓰지만 조건을 읽는 방식만 다르고, 각자 자기 리컴포지션 횟수를 센다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("8자 이상 입력") },
            supportingText = { Text("현재 ${text.length}자") }
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CountingButton(label = "매번 계산", enabled = plainEnabled)
            CountingButton(label = "derivedStateOf", enabled = derivedEnabled)
        }
        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "왼쪽은 글자마다, 오른쪽은 활성/비활성이 실제로 뒤집힐 때만 늘어난다. " +
                "버튼 하나면 차이가 사소하지만, 이 값을 읽는 것이 폼 전체를 감싼 컨테이너라면 이야기가 달라진다."
        )
    }
}

@Composable
private fun CountingButton(label: String, enabled: Boolean) {
    var recomposeCount by remember { mutableIntStateOf(0) }
    SideEffect { recomposeCount++ }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = { },
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = label, fontSize = 12.sp, color = Color.White)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "리컴포지션 ${recomposeCount}회", fontSize = 11.sp, color = Color(0xFF757575))
    }
}

// ==================== 5. 레이아웃 점프 ====================

@Composable
private fun LayoutJumpCard() {
    val state = rememberTextFieldState()
    var showError by remember { mutableStateOf(false) }
    var measuredHeight by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    FormSectionCard(title = "5. 에러 문구가 아래 필드를 밀어낸다 (실측)") {
        BodyText(
            "supportingText 를 에러일 때만 넣으면, 에러가 뜨는 순간 필드 높이가 늘어나 아래 내용이 통째로 밀린다. " +
                "아래 버튼으로 에러를 켰다 꺼 보면 높이 숫자가 바뀌는 것을 볼 수 있다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.onGloballyPositioned { coordinates ->
                measuredHeight = with(density) { coordinates.size.height.toDp().value.toInt() }
            }
        ) {
            OutlinedTextField(
                state = state,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("에러 토글 대상") },
                isError = showError,
                // 에러일 때만 슬롯을 채우는 형태 — 높이가 변한다
                supportingText = if (showError) {
                    { Text("이 문구가 나타나면서 높이가 늘어난다") }
                } else null
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(text = if (showError) "에러 끄기" else "에러 켜기", color = Color(0xFF546E7A)) {
                showError = !showError
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        ResultRow("측정 높이", "${measuredHeight}dp")

        Spacer(modifier = Modifier.height(10.dp))
        BodyText(
            "해법은 두 가지다 — ① 3번 카드처럼 **supportingText 를 항상 채우고**(에러가 없으면 도움말을 넣는다) " +
                "높이를 고정하거나 ② 에러 영역의 높이를 미리 확보한다. 이 예제의 3번 카드는 ①을 쓴다."
        )
    }
}

// ==================== 6. 함정 ====================

@Composable
private fun FormPitfallCard() {
    FormSectionCard(title = "6. 걸리는 것들") {
        PitfallRow(
            "빈 폼에 에러부터 띄우지 않기",
            "화면에 들어오자마자 \"이름을 입력해 주세요\"가 빨갛게 떠 있으면 실패로 보인다. " +
                "제출 전이거나 아직 만지지 않은 필드는 조용해야 한다(2·3번 카드의 touched/submitted 플래그)."
        )
        PitfallRow(
            "TextFieldState 에는 onValueChange 가 없다",
            "값 변화를 구독하려면 snapshotFlow { state.text } 를 쓴다. 컴포지션 안에서 state.text 를 " +
                "직접 읽으면 그 컴포저블이 글자마다 리컴포지션된다는 점도 함께 고려해야 한다."
        )
        PitfallRow(
            "다른 필드에 의존하는 규칙",
            "비밀번호 확인은 자기 값만으로 판단할 수 없다. 이런 규칙은 필드가 아니라 폼이 계산해야 하며, " +
                "비밀번호를 고치면 확인 필드의 에러도 다시 계산돼야 한다."
        )
        PitfallRow(
            "IME 액션과 포커스 이동은 별개다",
            "KeyboardOptions(imeAction = ImeAction.Next) 는 키보드의 버튼 모양만 바꾼다. " +
                "실제로 다음 필드로 가려면 onKeyboardAction 에서 FocusRequester.requestFocus() 를 불러야 한다."
        )
        PitfallRow(
            "검증 규칙은 UI 밖에 두기",
            "이 파일의 validateEmail/validatePassword 는 컴포저블이 아닌 순수 함수다. " +
                "그래야 화면 없이 단위 테스트할 수 있고, ViewModel 로 옮길 때도 그대로 따라간다."
        )
        PitfallRow(
            "이메일 정규식으로 유효성을 증명하려 하지 않기",
            "폼 검증의 목적은 오타를 잡는 것이다. 주소가 실제로 존재하는지는 인증 메일로만 확인된다."
        )
    }
}

// ==================== 공통 요소 ====================

@Composable
private fun FormSectionCard(
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
private fun BodyText(text: String) {
    Text(text = text, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
}

@Composable
private fun CaptionText(text: String) {
    Text(text = text, fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 16.sp)
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
            modifier = Modifier.width(84.dp),
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
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(
            text = label,
            modifier = Modifier.width(84.dp),
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
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = Color.White)
    }
}
