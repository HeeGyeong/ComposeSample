# Claude Code 활용 가이드

이 프로젝트에서 Claude Code를 효과적으로 사용하기 위한 가이드입니다.

---

## CLAUDE.md

프로젝트 루트에 위치한 마크다운 파일로, Claude Code 세션 시작 시 자동으로 로드됩니다.
별도 지시 없이도 프로젝트의 코딩 컨벤션, 아키텍처 규칙, 파일 구조를 Claude가 항상 인지한 채로 작업합니다.

### 이 프로젝트의 CLAUDE.md 구성 항목

| 항목 | 내용 |
|------|------|
| 기술 스택 | Kotlin 2.1.0 / ComposeBom 2025.01.00 / Target SDK 35 |
| 모듈 구조 | app / data / domain / Core / Coordinator |
| 예제 추가 방법 | 4단계 워크플로우 (ConstValue → ExampleObject → UI → Router) |
| 파일 네이밍 | `*ExampleUI.kt` / `exampleGuide.kt` 컨벤션 |
| 빌드 제약 | CLI 빌드 불가, Android Studio 전용 |
| 커밋 컨벤션 | 한국어 + feat/fix/refactor/chore/docs 접두어 |

### CLAUDE.md 작성 팁

```markdown
# 프로젝트명

## 기술 스택
- 언어, 프레임워크, 주요 라이브러리

## 파일 네이밍 컨벤션
- 규칙 명시

## 새 기능 추가 방법
- 단계별 워크플로우

## 빌드 환경 제약
- CLI 빌드 불가 조건 등

## 커밋 메시지 컨벤션
- 언어, 접두어 규칙
```

- ✅ 구체적인 예시 코드 포함
- ✅ 하지 말아야 할 것도 명시
- ✅ 빌드 제약 반드시 기재

---

## Memory 시스템

대화 간 지속되는 파일 기반 메모리 시스템입니다.
`~/.claude/projects/<project>/memory/` 에 저장됩니다.

### 메모리 타입 4가지

| 타입 | 용도 |
|------|------|
| `user` | 사용자 역할, 선호도, 지식 수준 — 응답 방식 맞춤화 |
| `feedback` | 작업 방식 교정 및 검증 — "하지 마" / "이 방식 맞아" 모두 저장 |
| `project` | 진행 중인 작업, 마감, 결정 사항 — 빠르게 변하는 상태 추적 |
| `reference` | 외부 시스템 위치 정보 — Linear, Slack, 대시보드 URL 등 |

### 이 프로젝트의 메모리 활용 예시

```markdown
---
name: 추가 예정 예제 목록
type: project
---

## 1. LazyStaggeredGrid ← 즉시 진행 가능
- 카테고리: ui/layout/lazycolumn
- 상태: 대기 중
```

예제 추가 작업 사이에도 우선순위 목록이 유지되어 이어서 작업 가능합니다.

### 저장하지 말아야 할 것

- 코드 패턴, 파일 경로 — 코드에서 직접 확인 가능
- git 이력 — `git log` / `git blame`이 정확함
- 디버깅 해결책 — 코드에 반영되어 있음
- 임시 작업 상태 — 세션 내 Tasks 도구 활용

---

## Hooks

도구 실행 전후 또는 세션 이벤트에 자동으로 실행되는 셸 커맨드입니다.
`~/.claude/settings.json` 또는 `.claude/settings.json` 에 설정합니다.

### 주요 Hook 이벤트

| 이벤트 | 용도 |
|--------|------|
| `PreToolUse` | 도구 실행 전 — 검증, 차단 가능 |
| `PostToolUse` | 도구 실행 후 — 포맷터, 린터 자동 실행 |
| `Stop` | Claude 응답 완료 시 — 알림, 로그 기록 |
| `SessionStart` | 세션 시작 시 — 환경 초기화 |
| `PreCompact` | 컨텍스트 압축 전 — 보존할 내용 확인 |

### 파일 저장 후 자동 포맷 예시

```json
"PostToolUse": [{
  "matcher": "Write|Edit",
  "hooks": [{
    "type": "command",
    "command": "ktlint --format $FILE"
  }]
}]
```

### 이 프로젝트 settings.json 설정

| 항목 | 값 |
|------|----|
| `attribution.commit` | `""` — Co-Authored-By 서명 제거 |
| `defaultMode` | `acceptEdits` — 파일 편집 자동 승인 |
| `allow` | `Bash(git:*)`, `Bash(ssh:*)` — 자동 허용 |

---

## Cursor vs Claude Code 비교

| 항목 | Cursor | Claude Code |
|------|--------|-------------|
| 규칙 파일 | `.cursorrules` / `.mdc` | `CLAUDE.md` (자동 로드) |
| 메모리 | 없음 (세션 단절) | 파일 기반 Auto Memory |
| Hooks | 없음 | PreToolUse / PostToolUse 등 |
| 권한 제어 | 없음 | settings.json allow/deny |
| 컨텍스트 | IDE 내 파일 참조 | CLI + 전체 파일시스템 접근 |

---

## 이 프로젝트에서 효과적인 프롬프트 패턴

### 예제 추가
```
"X 예제를 추가하기 전에 계획을 먼저 세워주세요"
→ 기존 패턴 파악 후 일관된 구현 가능
```

### 작업 전 확인
```
"작업 전에 기존 관련 파일을 읽어보세요"
→ 중복 구현이나 컨벤션 불일치 방지
```

### 메모리 활용
```
"이 목록을 메모리에 저장해주세요"
→ 다음 세션에서도 우선순위 유지
```

### 커밋 제어
```
"빌드 확인 후 커밋/푸시까지 진행해주세요"
→ CLI 빌드 불가 프로젝트에선 별도 확인 필요
```

---

## 주의사항

- CLI 빌드 불가 → Android Studio에서 직접 빌드 검증 필요
- CLAUDE.md가 없으면 프로젝트 규칙을 매번 설명해야 함
- 메모리는 점-인-타임 스냅샷 — 코드 변경과 달라질 수 있음
- 민감한 정보(API 키 등)는 메모리에 저장하지 말 것
