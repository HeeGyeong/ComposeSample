package com.example.composesample.presentation.example.component.ui.autofill

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader

/**
 * Compose Autofill (semantics contentType API) 예제
 *
 * 핵심:
 * - `Modifier.semantics { contentType = ContentType.Username }` 로 TextField 에 자동완성 힌트를 부여
 * - `ContentType.Username + ContentType.Password` 처럼 + 연산자로 여러 힌트 조합 가능
 * - `LocalAutofillManager.current` 의 commit() 으로 입력값 저장 제안, cancel() 로 진행 중 세션 취소
 *
 * 참고 자료는 같은 패키지의 exampleGuide.kt 참고
 */
@Composable
fun AutofillExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(title = "Compose Autofill (semantics API)", onBackIconClicked = onBackEvent)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ConceptCard() }
            item { LoginFormCard() }
            item { RegistrationFormCard() }
            item { AvailabilityCard() }
            item { ApiNotesCard() }
        }
    }
}

/**
 * 개념 설명 카드
 */
@Composable
private fun ConceptCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Compose Autofill 이란?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "OS 의 자동완성 서비스(예: Google Autofill, 비밀번호 관리자)가 어떤 필드에 " +
                        "무엇을 채워야 할지 알 수 있도록, 각 TextField 에 의미(semantics)로 " +
                        "'이 필드는 사용자명/비밀번호/이메일이다' 라는 힌트를 부여하는 기능입니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "핵심 API",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.weight(0.32f)
                )
                Text(
                    text = "Modifier.semantics { contentType = ContentType.Username }\n" +
                            "+ LocalAutofillManager 로 commit()/cancel() 트리거",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF424242),
                    lineHeight = 16.sp,
                    modifier = Modifier.weight(0.68f)
                )
            }
        }
    }
}

/**
 * 로그인 폼 — Username / Password 힌트 + commit() 트리거
 */
@Composable
private fun LoginFormCard() {
    // LocalAutofillManager 는 자동완성 미지원 환경에서 null 일 수 있음
    val autofillManager = LocalAutofillManager.current

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var lastAction by remember { mutableStateOf("대기 중") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "① 로그인 폼 (Username + Password)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "각 필드에 contentType 힌트를 부여했습니다. 로그인 성공 시 commit() 으로 " +
                        "저장을 제안하고, 초기화 시 cancel() 로 진행 중 세션을 취소합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("아이디") },
                singleLine = true,
                // 자동완성 힌트: 기존 사용자명
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentType = ContentType.Username }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                // 자동완성 힌트: 기존 비밀번호
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentType = ContentType.Password }
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        // 로그인 성공 시 입력값 저장을 자동완성 서비스에 제안
                        autofillManager?.commit()
                        lastAction = "commit() 호출 — 저장 제안"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) {
                    Text(text = "로그인 (commit)", color = Color.White, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = {
                        username = ""
                        password = ""
                        // 진행 중인 자동완성 세션 취소
                        autofillManager?.cancel()
                        lastAction = "cancel() 호출 — 세션 취소"
                    }
                ) {
                    Text(text = "초기화 (cancel)", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            StatusRow(
                label = "AutofillManager",
                value = if (autofillManager != null) "사용 가능" else "사용 불가(null)",
                valueColor = if (autofillManager != null) Color(0xFF388E3C) else Color(0xFFD32F2F)
            )
            StatusRow(label = "마지막 동작", value = lastAction, valueColor = Color(0xFF1976D2))
        }
    }
}

/**
 * 회원가입 폼 — NewUsername / NewPassword / EmailAddress / PostalCode 조합 힌트
 */
@Composable
private fun RegistrationFormCard() {
    val autofillManager = LocalAutofillManager.current

    var newId by remember { mutableStateOf("") }
    var newPw by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var postal by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "② 회원가입 폼 (New 계열 + Email + PostalCode)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "신규 가입에는 NewUsername / NewPassword 를 사용하면 자동완성 서비스가 " +
                        "강력한 비밀번호 생성을 제안할 수 있습니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = newId,
                onValueChange = { newId = it },
                label = { Text("새 아이디") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentType = ContentType.NewUsername }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = newPw,
                onValueChange = { newPw = it },
                label = { Text("새 비밀번호") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentType = ContentType.NewPassword }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("이메일") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentType = ContentType.EmailAddress }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = postal,
                onValueChange = { postal = it },
                label = { Text("우편번호") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentType = ContentType.PostalCode }
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { autofillManager?.commit() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
            ) {
                Text(text = "가입 완료 (commit)", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

/**
 * 가용성 안내 카드 — 실제 자동완성 동작은 OS/단말 의존
 */
@Composable
private fun AvailabilityCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "실제 동작 확인 방법",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val notes = listOf(
                "자동완성 UI(저장/채우기 제안)는 OS 의 자동완성 서비스가 활성화되어 있어야 나타남 — 설정 > 비밀번호·패스키 및 자동 완성 에서 서비스 지정 필요",
                "에뮬레이터/단말에 자동완성 서비스가 없으면 LocalAutofillManager 가 null 일 수 있고, 힌트만 부여될 뿐 UI 는 표시되지 않음",
                "힌트 부여 자체는 OS 와 무관하게 항상 안전 — 지원 환경에서만 추가 기능이 동작하는 점진적 향상(progressive enhancement) 패턴",
                "commit() 은 폼 제출 성공 직후 호출해 '이 자격증명을 저장할까요?' 제안을 띄움. cancel() 은 사용자가 폼을 떠나거나 초기화할 때 진행 세션을 정리"
            )
            notes.forEach { note ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = "• ", fontSize = 13.sp, color = Color(0xFFE65100))
                    Text(
                        text = note,
                        fontSize = 12.sp,
                        color = Color(0xFF424242),
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

/**
 * API 메모 카드 — semantics contentType vs Modifier.contentType, 조합
 */
@Composable
private fun ApiNotesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "API 메모",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(12.dp))

            CodeBlock(
                title = "단일 힌트",
                code = "OutlinedTextField(\n" +
                        "    value = id,\n" +
                        "    onValueChange = { id = it },\n" +
                        "    modifier = Modifier.semantics {\n" +
                        "        contentType = ContentType.Username\n" +
                        "    }\n" +
                        ")",
                borderColor = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.height(12.dp))
            CodeBlock(
                title = "여러 힌트 조합 (+ 연산자)",
                code = "// 하나의 필드가 사용자명도/이메일도 될 수 있을 때\n" +
                        "Modifier.semantics {\n" +
                        "    contentType = ContentType.Username +\n" +
                        "        ContentType.EmailAddress\n" +
                        "}",
                borderColor = Color(0xFF388E3C)
            )
            Spacer(modifier = Modifier.height(12.dp))
            CodeBlock(
                title = "commit / cancel",
                code = "val autofill = LocalAutofillManager.current\n" +
                        "\n" +
                        "// 로그인/가입 성공 → 저장 제안\n" +
                        "autofill?.commit()\n" +
                        "\n" +
                        "// 폼 이탈/초기화 → 세션 취소\n" +
                        "autofill?.cancel()",
                borderColor = Color(0xFF6A1B9A)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "참고: Compose Foundation 1.8+ 에서 자동완성이 stable 로 승격되었습니다. " +
                        "semantics { contentType = ... } 방식이 표준이며, 일부 버전에서는 " +
                        "Modifier.contentType(...) 단축 형태도 제공됩니다.",
                fontSize = 11.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun CodeBlock(title: String, code: String, borderColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = borderColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = code,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF212121),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun StatusRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF757575))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}
