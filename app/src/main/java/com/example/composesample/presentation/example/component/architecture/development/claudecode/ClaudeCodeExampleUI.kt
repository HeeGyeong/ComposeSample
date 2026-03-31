package com.example.composesample.presentation.example.component.architecture.development.claudecode

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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

@Composable
fun ClaudeCodeExampleUI(onBackEvent: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("CLAUDE.md", "Memory", "Hooks", "팁")

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
                Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
            Text(
                text = "Claude Code 활용",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider()

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                when (selectedTab) {
                    0 -> ClaudeMdTab()
                    1 -> MemoryTab()
                    2 -> HooksTab()
                    3 -> TipsTab()
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────
// 탭 1: CLAUDE.md
// ─────────────────────────────────────────────────────────

@Composable
private fun ClaudeMdTab() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoCard(
            title = "CLAUDE.md 란?",
            color = Color(0xFFE3F2FD)
        ) {
            BodyText("프로젝트 루트에 위치한 마크다운 파일로, Claude Code 세션 시작 시 자동으로 로드됩니다.")
            Spacer(modifier = Modifier.height(8.dp))
            BodyText("별도 지시 없이도 프로젝트의 코딩 컨벤션, 아키텍처 규칙, 파일 구조를 Claude가 항상 인지한 채로 작업합니다.")
        }

        InfoCard(title = "이 프로젝트의 CLAUDE.md 구성", color = Color(0xFFF8F9FA)) {
            FeatureRow("기술 스택", "Kotlin 2.1.0 / ComposeBom 2025.01.00 / Target SDK 35")
            FeatureRow("모듈 구조", "app / data / domain / Core / Coordinator")
            FeatureRow("예제 추가 방법", "4단계 워크플로우 (ConstValue → ExampleObject → UI → Router)")
            FeatureRow("파일 네이밍", "*ExampleUI.kt / exampleGuide.kt 컨벤션")
            FeatureRow("빌드 제약", "CLI 빌드 불가, Android Studio 전용")
            FeatureRow("커밋 컨벤션", "한국어 + feat/fix/refactor/chore/docs 접두어")
        }

        InfoCard(title = "CLAUDE.md 작성 팁", color = Color(0xFFFFF9C4)) {
            CodeBlock(
                """# 프로젝트명

## 기술 스택
- 언어, 프레임워크, 주요 라이브러리

## 파일 네이밍 컨벤션
- 규칙 명시

## 새 기능 추가 방법
- 단계별 워크플로우

## 빌드 환경 제약
- 빌드 불가 조건 등

## 커밋 메시지 컨벤션
- 언어, 접두어 규칙"""
            )
            Spacer(modifier = Modifier.height(8.dp))
            BodyText("✅ 구체적인 예시 코드 포함  ✅ 하지 말아야 할 것도 명시  ✅ 빌드 제약 반드시 기재")
        }
    }
}

// ─────────────────────────────────────────────────────────
// 탭 2: Memory
// ─────────────────────────────────────────────────────────

@Composable
private fun MemoryTab() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoCard(title = "Auto Memory 시스템", color = Color(0xFFE3F2FD)) {
            BodyText("대화 간 지속되는 파일 기반 메모리 시스템입니다.")
            BodyText("~/.claude/projects/<project>/memory/ 에 저장됩니다.")
        }

        InfoCard(title = "메모리 타입 4가지", color = Color(0xFFF8F9FA)) {
            MemoryTypeRow(
                type = "user",
                color = Color(0xFF1E88E5),
                desc = "사용자 역할, 선호도, 지식 수준 — 응답 방식 맞춤화에 활용"
            )
            Spacer(modifier = Modifier.height(8.dp))
            MemoryTypeRow(
                type = "feedback",
                color = Color(0xFF43A047),
                desc = "작업 방식 교정 및 검증 — \"하지 마\" / \"이 방식 맞아\" 모두 저장"
            )
            Spacer(modifier = Modifier.height(8.dp))
            MemoryTypeRow(
                type = "project",
                color = Color(0xFFF57C00),
                desc = "진행 중인 작업, 마감, 결정 사항 — 빠르게 변하는 상태 추적"
            )
            Spacer(modifier = Modifier.height(8.dp))
            MemoryTypeRow(
                type = "reference",
                color = Color(0xFF7B1FA2),
                desc = "외부 시스템 위치 정보 — Linear 프로젝트, Slack 채널, 대시보드 URL 등"
            )
        }

        InfoCard(title = "이 프로젝트의 메모리 활용 예시", color = Color(0xFFFFF9C4)) {
            CodeBlock(
                """---
name: 추가 예정 예제 목록
type: project
---

## 1. LazyStaggeredGrid ← 즉시 진행 가능
- 카테고리: ui/layout/lazycolumn
- 상태: 대기 중

## 2. Adaptive Layout
- 카테고리: ui/layout
..."""
            )
            Spacer(modifier = Modifier.height(8.dp))
            BodyText("예제 추가 작업 사이에도 우선순위 목록이 유지되어 이어서 작업 가능합니다.")
        }

        InfoCard(title = "저장하지 말아야 할 것", color = Color(0xFFFFEBEE)) {
            BulletItem("코드 패턴, 파일 경로 — 코드에서 직접 확인 가능")
            BulletItem("git 이력 — git log/blame이 정확함")
            BulletItem("디버깅 해결책 — 코드에 반영되어 있음")
            BulletItem("임시 작업 상태 — 세션 내 Tasks 도구 활용")
        }
    }
}

// ─────────────────────────────────────────────────────────
// 탭 3: Hooks
// ─────────────────────────────────────────────────────────

@Composable
private fun HooksTab() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoCard(title = "Hooks 란?", color = Color(0xFFE3F2FD)) {
            BodyText("도구 실행 전후 또는 세션 이벤트에 자동으로 실행되는 셸 커맨드입니다.")
            BodyText("~/.claude/settings.json 또는 .claude/settings.json 에 설정합니다.")
        }

        InfoCard(title = "주요 Hook 이벤트", color = Color(0xFFF8F9FA)) {
            HookEventRow("PreToolUse", "도구 실행 전 — 검증, 차단 가능")
            HookEventRow("PostToolUse", "도구 실행 후 — 포맷터, 린터 자동 실행")
            HookEventRow("Stop", "Claude 응답 완료 시 — 알림, 로그 기록")
            HookEventRow("SessionStart", "세션 시작 시 — 환경 초기화")
            HookEventRow("PreCompact", "컨텍스트 압축 전 — 보존할 내용 확인")
        }

        InfoCard(title = "활용 예시", color = Color(0xFFFFF9C4)) {
            SubLabel("파일 저장 후 자동 포맷 (PostToolUse)")
            CodeBlock(
                """"PostToolUse": [{
  "matcher": "Write|Edit",
  "hooks": [{
    "type": "command",
    "command": "ktlint --format \$FILE"
  }]
}]"""
            )
            Spacer(modifier = Modifier.height(8.dp))
            SubLabel("Bash 커맨드 자동 허용 (settings.json)")
            CodeBlock(
                """"permissions": {
  "allow": [
    "Bash(git:*)",
    "Bash(./gradlew:*)"
  ],
  "defaultMode": "acceptEdits"
}"""
            )
        }

        InfoCard(title = "이 프로젝트 설정", color = Color(0xFFF8F9FA)) {
            FeatureRow("attribution.commit", "\"\" — Co-Authored-By 서명 제거")
            FeatureRow("defaultMode", "acceptEdits — 파일 편집 자동 승인")
            FeatureRow("allow", "Bash(git:*), Bash(ssh:*) — 자동 허용")
        }
    }
}

// ─────────────────────────────────────────────────────────
// 탭 4: 팁
// ─────────────────────────────────────────────────────────

@Composable
private fun TipsTab() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoCard(title = "Cursor vs Claude Code", color = Color(0xFFE3F2FD)) {
            CompareRow("규칙 파일", ".cursorrules / .mdc", "CLAUDE.md (자동 로드)")
            CompareRow("메모리", "없음 (세션 단절)", "파일 기반 Auto Memory")
            CompareRow("Hooks", "없음", "PreToolUse / PostToolUse 등")
            CompareRow("권한 제어", "없음", "settings.json allow/deny")
            CompareRow("컨텍스트", "IDE 내 파일 참조", "CLI + 전체 파일시스템 접근")
        }

        InfoCard(title = "이 프로젝트에서 효과적인 프롬프트 패턴", color = Color(0xFFF8F9FA)) {
            TipItem(
                tag = "예제 추가",
                text = "\"X 예제를 추가하기 전에 계획을 먼저 세워주세요\"\n→ 기존 패턴 파악 후 일관된 구현 가능"
            )
            Spacer(modifier = Modifier.height(8.dp))
            TipItem(
                tag = "작업 전 확인",
                text = "\"작업 전에 기존 관련 파일을 읽어보세요\"\n→ 중복 구현이나 컨벤션 불일치 방지"
            )
            Spacer(modifier = Modifier.height(8.dp))
            TipItem(
                tag = "메모리 활용",
                text = "\"이 목록을 메모리에 저장해주세요\"\n→ 다음 세션에서도 우선순위 유지"
            )
            Spacer(modifier = Modifier.height(8.dp))
            TipItem(
                tag = "커밋 제어",
                text = "\"빌드 확인 후 커밋/푸시까지 진행해주세요\"\n→ CLI 빌드 불가 프로젝트에선 별도 확인 필요"
            )
        }

        InfoCard(title = "주의사항", color = Color(0xFFFFEBEE)) {
            BulletItem("CLI 빌드 불가 → Android Studio에서 직접 빌드 검증 필요")
            BulletItem("CLAUDE.md가 없으면 프로젝트 규칙을 매번 설명해야 함")
            BulletItem("메모리는 점-인-타임 스냅샷 — 코드 변경과 달라질 수 있음")
            BulletItem("민감한 정보(API 키 등)는 메모리에 저장하지 말 것")
        }
    }
}

// ─────────────────────────────────────────────────────────
// 공통 컴포넌트
// ─────────────────────────────────────────────────────────

@Composable
private fun InfoCard(
    title: String,
    color: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun BodyText(text: String) {
    Text(text, fontSize = 13.sp, color = Color.DarkGray, lineHeight = 19.sp)
}

@Composable
private fun SubLabel(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun CodeBlock(code: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(code, fontSize = 11.sp, color = Color(0xFFD4D4D4), fontFamily = FontFamily.Monospace, lineHeight = 16.sp)
    }
}

@Composable
private fun FeatureRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray, modifier = Modifier.weight(1f))
        Text(value, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1.6f))
    }
}

@Composable
private fun BulletItem(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("• ", fontSize = 13.sp, color = Color.DarkGray)
        Text(text, fontSize = 13.sp, color = Color.DarkGray, lineHeight = 18.sp)
    }
}

@Composable
private fun MemoryTypeRow(type: String, color: Color, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = type,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .background(color, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Text(desc, fontSize = 12.sp, color = Color.DarkGray, lineHeight = 17.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun HookEventRow(event: String, desc: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(event, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E88E5), modifier = Modifier.weight(1f), fontFamily = FontFamily.Monospace)
        Text(desc, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.weight(1.5f))
    }
}

@Composable
private fun CompareRow(feature: String, cursor: String, claude: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
        Text(feature, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray, modifier = Modifier.weight(0.9f))
        Text(cursor, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
        Text(claude, fontSize = 11.sp, color = Color(0xFF1E88E5), modifier = Modifier.weight(1.1f))
    }
}

@Composable
private fun TipItem(tag: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = tag,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Text(text, fontSize = 12.sp, color = Color.DarkGray, lineHeight = 17.sp, modifier = Modifier.weight(1f))
    }
}
