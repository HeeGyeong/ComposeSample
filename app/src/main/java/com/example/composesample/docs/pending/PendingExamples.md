# 추가 예정 예제 리스트 (Android Weekly #723 기준) — ✅ 전 항목 완료(아카이브)

> 작성일: 2026-04-20
> 출처: https://androidweekly.net/issues/issue-723
> **상태: 이 문서의 항목은 모두 처리(완료/제외)되었습니다. 현재 대기 예제 관리는 Claude Code 메모리(`project_pending_examples.md`)로 이관되어 있으며, 본 문서는 #723 검토 기록으로 보존합니다.**

Android Weekly #723을 검토하여 선정했던 5개 항목의 최종 처리 결과입니다.

---

## 🔴 우선순위 높음

### 1. Navigation 3 ViewModel Scope ✅ 완료 (커밋: 5b9b954a)
- **카테고리**: `architecture/navigation/`
- **파일명**: `Nav3ViewModelScopeExampleUI.kt`
- **내용**: Nav2 Auto-Scope vs Nav3 기본 동작 vs NavKey 단위 Store 매핑 복원 — 순수 Compose 상태로 시뮬레이션(Nav3 alpha 의존성 회피)
- **출처**: medium.com/@domen.lanisnik (Issue #723)

### 2. Koin Compiler Plugin (Compile Safety) ✅ 완료 (커밋: a1582247)
- **카테고리**: `architecture/development/di/`
- **파일명**: `KoinCompilerPluginExampleUI.kt`
- **내용**: 수동 DSL vs 애노테이션(@Module/@Single/@Factory/@KoinViewModel/@Named) 비교, KSP 생성 코드 개념 미리보기, 점진적 전환 전략
- **출처**: blog.insert-koin.io (Issue #723)
- **관련 문서**: `docs/di/DIRules.md`

---

## 🟡 우선순위 중간

### 3. Gemini Nano On-Device AI (ML Kit GenAI) ✅ 완료 (커밋: 2f4db6b4)
- **카테고리**: `system/ai/`
- **파일명**: `GeminiNanoExampleUI.kt`
- **내용**: Feature Availability 4상태 시뮬레이션 + Summarization Mock + Nano↔Cloud 하이브리드 라우팅 코드 (실기기 의존성 회피 위해 Mock 응답)
- **출처**: medium.com/@Y4583L (Issue #723)

### 4. Halogen — Runtime Material 3 Theme Generation ❌ 제외
- **사유**: 외부 라이브러리 + AI API 키 의존. Foundation Style API(#23)가 디자인 토큰을 자체 구현으로 커버하여 제외.
- **출처**: github.com/himattm/halogen (Issue #723)

### 5. Airbnb Month Picker Dial (ChromaDial 기반) ✅ 완료 (커밋: 474cae52)
- **카테고리**: `ui/canvas/`
- **파일명**: `MonthPickerDialExampleUI.kt`
- **내용**: Canvas 폴라 좌표(cos/sin) 배치 + atan2 각도 계산 + detectDragGestures + spring 스냅 애니메이션 + snapshotFlow 선택 동기화
- **출처**: sinasamaki.com (Issue #723)

---

## 진행 원칙 (보존)

1. 우선순위 순서대로 진행
2. 각 예제 완료 시 CLAUDE.md "새 예제 추가 방법" 4단계 준수
3. 완료 후 빌드 검증 → 커밋 → 푸시
4. 완료 항목은 `project_pending_examples.md` 메모리에 ✅ 표시 + 커밋 해시 기록

> 이후 신규 후보(Android Weekly #724~ 이상)는 본 파일이 아니라 메모리 `project_pending_examples.md`에서 관리합니다.
