# 📚 docs 문서 인덱스

ComposeSample 프로젝트의 규칙·가이드 문서 모음입니다. 대상 독자와 용도별로 정리했습니다.

## 규칙 문서 (사람 / Claude Code용)

| 문서 | 내용 | 대응 `.cursor/rules` |
|------|------|----------------------|
| [`ui/UIRules.md`](ui/UIRules.md) | UI 시스템 가이드 — ViewModel/UI 분리, 상위 기능 단위 ViewModel, 상태 관리, 성능 최적화 | `comprehensive-ui-guide.mdc` |
| [`di/DIRules.md`](di/DIRules.md) | DI 아키텍처 — Koin 모듈 사양, named 한정자, 스코프, 에러 처리 | `dependency-management.mdc` |
| [`data/DataRules.md`](data/DataRules.md) | 데이터 클래스 — 네이밍, @SerializedName/@Parcelize, 널 가능성·기본값 | `data-rules.mdc` |

## 프롬프트 가이드 (AI 코드 생성용)

| 문서 | 내용 | 대응 `.cursor/rules` |
|------|------|----------------------|
| [`prompt/CreateAPIGuide.md`](prompt/CreateAPIGuide.md) | API 생성 프롬프트 가이드 | `api-creation-guide.mdc` |
| [`prompt/CreateAPIAndUIBindingGuide.md`](prompt/CreateAPIAndUIBindingGuide.md) | API + UI 바인딩 생성 가이드 | `api-ui-binding.mdc` |
| [`prompt/CreateUICodeSnippet.md`](prompt/CreateUICodeSnippet.md) | UI 코드 생성 스니펫 | (대응 없음) |

## 도구 / 운영 가이드

| 문서 | 내용 |
|------|------|
| [`claudecode/ClaudeCodeGuide.md`](claudecode/ClaudeCodeGuide.md) | Claude Code 활용 — CLAUDE.md/Memory/Hooks/프롬프트 패턴 |
| [`devtools/ComposeHotReloadGuide.md`](devtools/ComposeHotReloadGuide.md) | Compose Hot Reload(HotSwan) 설치·동작·버전 요구사항 |
| [`pending/PendingExamples.md`](pending/PendingExamples.md) | (아카이브) Android Weekly #723 예제 후보 검토 기록 |

## 프로젝트 루트 문서

| 문서 | 내용 |
|------|------|
| [`/CLAUDE.md`](../../../../../../../CLAUDE.md) | Claude Code 프로젝트 가이드 — 기술 스택, 예제 추가 4단계, 워크플로우 규칙 |
| [`/README.md`](../../../../../../../README.md) | 프로젝트 소개, 개발 환경, 컴포넌트 예제 목록 |
| `/ARCHITECTURE.md` | 아키텍처 의사결정 기록 (레이어 규칙, ARCH 리팩토링 배경) |
| `/CHANGELOG.md` | 버전·예제 추가 변경 이력 |
| `/docs/KnownLimitations.md`(이 폴더) | 알려진 제약·보류 항목 (버전 업그레이드 보류 등) |

## `.cursor/rules`와의 관계

`.cursor/rules/*.mdc`는 **Cursor IDE 전용** 규칙 파일(영어, frontmatter 포함)입니다. 위 표의 docs 문서와 토픽이 일부만 1:1 대응하며, 다음 4개는 **`.cursor/rules`에만 존재**하고 docs/ 대응 문서가 없습니다:

- `code-style.mdc` — Kotlin & Compose 코드 스타일
- `performance-optimization.mdc` — 성능 최적화
- `project-structure.mdc` — Clean Architecture 구조
- `testing-guide.mdc` — 테스팅 가이드

규칙을 확인할 때는 두 위치를 함께 참고하세요.
