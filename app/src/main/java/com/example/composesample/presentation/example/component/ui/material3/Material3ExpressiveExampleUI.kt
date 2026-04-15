package com.example.composesample.presentation.example.component.ui.material3

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

@Composable
fun Material3ExpressiveExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Material 3 Expressive (1.4.0)",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { SecureTextFieldDemoCard() }
            item { OutlinedSecureTextFieldDemoCard() }
            item { ObfuscationModeComparisonCard() }
            item { CodeExampleCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun OverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Material3 1.4.0 신규 컴포넌트",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Material Design 3의 Expressive 업데이트에서 비밀번호 입력을 위한 " +
                        "SecureTextField가 stable API로 추가되었습니다. " +
                        "TextFieldState 기반으로 동작하며 3가지 난독화 모드를 제공합니다.",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val components = listOf(
                Triple("SecureTextField", "Filled 스타일 비밀번호 필드", "stable"),
                Triple("OutlinedSecureTextField", "Outlined 스타일 비밀번호 필드", "stable"),
                Triple("FloatingToolbar", "플로팅 액션 바", "alpha (1.5.0+)"),
                Triple("VerticalDragHandle", "BottomSheet 드래그 핸들", "stable")
            )
            components.forEach { (name, desc, status) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFF3EDF7), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6750A4),
                        modifier = Modifier.weight(0.35f)
                    )
                    Text(
                        text = desc,
                        fontSize = 10.sp,
                        color = Color(0xFF49454F),
                        modifier = Modifier.weight(0.45f)
                    )
                    Text(
                        text = status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (status == "stable") Color(0xFF388E3C) else Color(0xFFE65100),
                        modifier = Modifier.weight(0.2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SecureTextFieldDemoCard() {
    var selectedMode by remember { mutableIntStateOf(1) }
    val state = rememberTextFieldState()

    val currentMode = when (selectedMode) {
        0 -> TextObfuscationMode.Hidden
        1 -> TextObfuscationMode.RevealLastTyped
        else -> TextObfuscationMode.Visible
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SecureTextField (Filled)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "비밀번호 입력 전용 Filled 스타일. 난독화 모드를 선택하고 직접 입력해보세요.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 모드 선택 탭
            ObfuscationModeSelector(selectedMode = selectedMode, onModeSelected = { selectedMode = it })

            Spacer(modifier = Modifier.height(12.dp))

            SecureTextField(
                state = state,
                label = { Text("비밀번호") },
                placeholder = { Text("입력해보세요") },
                textObfuscationMode = currentMode,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            ObfuscationModeDescription(selectedMode)
        }
    }
}

@Composable
private fun OutlinedSecureTextFieldDemoCard() {
    var selectedMode by remember { mutableIntStateOf(0) }
    val state = rememberTextFieldState()

    val currentMode = when (selectedMode) {
        0 -> TextObfuscationMode.Hidden
        1 -> TextObfuscationMode.RevealLastTyped
        else -> TextObfuscationMode.Visible
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "OutlinedSecureTextField (Outlined)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Outlined 스타일 비밀번호 필드. 같은 TextObfuscationMode를 사용합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            ObfuscationModeSelector(selectedMode = selectedMode, onModeSelected = { selectedMode = it })

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedSecureTextField(
                state = state,
                label = { Text("비밀번호") },
                placeholder = { Text("입력해보세요") },
                textObfuscationMode = currentMode,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            ObfuscationModeDescription(selectedMode)
        }
    }
}

@Composable
private fun ObfuscationModeSelector(selectedMode: Int, onModeSelected: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("Hidden", "RevealLastTyped", "Visible").forEachIndexed { idx, label ->
            Box(
                modifier = Modifier
                    .background(
                        if (selectedMode == idx) Color(0xFF6750A4) else Color(0xFFE8DEF8),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onModeSelected(idx) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = if (selectedMode == idx) Color.White else Color(0xFF6750A4),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ObfuscationModeDescription(selectedMode: Int) {
    val modeDesc = when (selectedMode) {
        0 -> "Hidden — 모든 문자를 즉시 '•'로 표시. 가장 보안 강도가 높은 모드"
        1 -> "RevealLastTyped — 마지막 입력 문자만 잠깐 표시 후 '•'로 변환. 입력 확인과 보안의 균형"
        else -> "Visible — 전체 텍스트를 그대로 표시. 디버깅/테스트 용도"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3EDF7), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = modeDesc, fontSize = 11.sp, color = Color(0xFF49454F), lineHeight = 16.sp)
    }
}

@Composable
private fun ObfuscationModeComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TextObfuscationMode 비교",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val modes = listOf(
                Triple("Hidden", "\"password\" → \"••••••••\"", "최고 보안. 모든 문자 즉시 난독화"),
                Triple("RevealLastTyped", "\"passwor\" + \"d\" → \"•••••••d\" → \"••••••••\"", "실무 권장. 마지막 입력 확인 가능"),
                Triple("Visible", "\"password\" → \"password\"", "테스트/디버깅. 난독화 없음")
            )
            modes.forEach { (mode, example, desc) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = mode,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF6750A4)
                    )
                    Text(
                        text = example,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF212121)
                    )
                    Text(text = desc, fontSize = 11.sp, color = Color(0xFF757575))
                }
            }
        }
    }
}

@Composable
private fun CodeExampleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "코드 예제",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF6750A4), RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "// TextFieldState 기반 (onValueChange 아님)\n" +
                            "val state = rememberTextFieldState()\n\n" +
                            "SecureTextField(\n" +
                            "    state = state,\n" +
                            "    label = { Text(\"비밀번호\") },\n" +
                            "    textObfuscationMode =\n" +
                            "        TextObfuscationMode.RevealLastTyped,\n" +
                            "    // 커스텀 난독화 문자 (기본값 '•')\n" +
                            "    obfuscationCharacter = '●'\n" +
                            ")\n\n" +
                            "// Outlined 스타일\n" +
                            "OutlinedSecureTextField(\n" +
                            "    state = state,\n" +
                            "    label = { Text(\"비밀번호\") },\n" +
                            "    textObfuscationMode =\n" +
                            "        TextObfuscationMode.Hidden\n" +
                            ")",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF212121),
                    lineHeight = 16.sp
                )
            }
        }
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
                color = Color(0xFF6750A4)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val bullets = listOf(
                "SecureTextField는 TextFieldState 기반 — 기존 onValueChange가 아닌 상태 객체로 관리",
                "TextObfuscationMode.RevealLastTyped이 실무에서 가장 많이 사용되는 모드 (입력 확인 + 보안)",
                "obfuscationCharacter 파라미터로 난독화 문자를 커스터마이징 가능 (기본 '•')",
                "Filled(SecureTextField)와 Outlined(OutlinedSecureTextField) 두 가지 스타일 제공",
                "FloatingToolbar, HorizontalCenteredHeroCarousel 등은 alpha(1.5.0+)에서 사용 가능"
            )
            bullets.forEach { bullet ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(text = "• ", fontSize = 13.sp, color = Color(0xFF6750A4))
                    Text(text = bullet, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
                }
            }
        }
    }
}
