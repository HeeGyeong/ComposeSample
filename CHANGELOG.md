# 변경 이력 (CHANGELOG)

ComposeSample의 버전·예제 추가 변경 이력입니다. 최신순으로 기록합니다.
(요약은 `README.md`의 "최신 업데이트" 참고)

---

## 2026.06
- 아키텍처 리팩토링 및 문서/품질 개선
  - domain 모듈 순수 Kotlin(JVM) 전환 — Android/Retrofit/Gson 의존 제거
  - ExampleObject/ExampleMoveType을 domain→app `presentation.example.model` 이동
  - DataCache의 presentation→data 직접참조 제거 (UserCacheRepository 추상화)
  - UseCase `execute()` → `operator invoke()` 통일
  - ApiExampleViewModel Koin `named` 등록 명시
  - MainUIComponent Material1 → Material3 전환
  - UI/DI/Data 규칙 문서 한국어화, 전체 md 문서 최신화(DomainREADME/README/AppREADME/PendingExamples/ClaudeCodeGuide)
  - 문서 신규: `docs/README.md`(인덱스), `ARCHITECTURE.md`, `docs/KnownLimitations.md`, `LICENSE`(MIT)
  - exampleGuide.kt 30개 카테고리 보강
- 버전: ComposeBom 2026.05.00 + Material 1.11.1 업그레이드

## 2026.05
- 신규 예제 추가
  - Accessible Focus Indicator — 키보드/D-pad 포커스 시각화 4가지 패턴 + IndicationNodeFactory + DrawModifierNode 커스텀 인디케이션
  - Document Editing TextField — TextFieldState 심화(undoState/selection 조작/AnnotatedString 미리보기/멀티 커서 시뮬레이션)
  - Syntax Highlighting — AnnotatedString + 정규식 토크나이저 기반 Kotlin 코드 하이라이팅 미니 데모
  - Particle Emitter — Canvas + withFrameNanos 기반 물리 파티클 시스템(폭죽/별가루) + Canvas vs Layout 트레이드오프
  - Animations Showcase — duration/easing 슬라이더로 4섹션 동시 비교
  - Hardware-Backed Keystore — API 레벨별 보안 하드웨어 검증
  - Shared Element Debug Tooling (Compose 1.11)
  - Foundation Style API (Compose 1.11) — 단일 CompositionLocal 디자인 토큰
  - Month Picker Dial — Canvas 폴라 좌표 + 드래그 스냅
  - App Security — Cert Pinning/Secure Storage/Play Integrity 비교
  - AGSL Shader Live Tuning — RuntimeShader uniform 실시간 조절
  - Type-Safe Feature Flag — sealed 레지스트리 + 디버그 오버라이드
  - Per-Item ViewModels — LazyColumn 아이템별 ViewModelStoreOwner
  - Room FTS4 vs LIKE 검색 성능 비교
  - Room Database Indices — 단일/복합 인덱스 벤치마크
  - Multi-Table Inserts in Room — DAO 인터페이스 상속 + 트랜잭션

## 2026.04
- 신규 예제 추가: LazyStaggeredGrid 폭포수 그리드, Adaptive Layout WindowSizeClass, Custom Layout MeasurePolicy, Dynamic App Links, Screenshot Testing(Paparazzi/Roborazzi), Compose Snapshot System, Compose UI Testing, Predictive Back Gesture, Spring/Tween/Snap 애니메이션, Haptic Feedback, Stability Annotations, Rich Content in Text Input, FlowRow/FlowColumn 공식 Flexbox, Preview-only Annotation(@RequiresOptIn), Coroutine Bridges(suspendCoroutine), Compose Loaders 수학 곡선 로딩, TextField Max Length 숨겨진 버그, Kotlin Name-Based Destructuring, Material 3 Expressive SecureTextField, Modifier Order in Compose, Flow Operators(buffer/conflate/debounce/sample), Multi-Table Inserts in Room(DAO 인터페이스 상속) 등
- Compose Hot Reload(HotSwan) Gradle 플러그인 적용
- 버전: Kotlin 2.3.20 + AGP 8.13.2 + Compose BOM 2026.03.01 + Material3 1.4.0 업그레이드

## 2026.03
- 신규 예제 추가: MotionBlur, LargeContentViewer, LocalContextStrings, EmbeddedPicker Compose 통합, Rebound 리컴포지션 모니터링, Coroutine Flow Testing with Turbine, Compose Preview Internals, Remember Patterns, Startup Optimization, AnimatedContent 심화 등

## 2026.02
- 신규 예제 추가: Transition, Dial, Photo Picker, Sticker Canvas 등

## 2026.01
- UI 컴포넌트 예제 추가: Quick Setting, TopAppBar, Canvas Shapes, Responsive TabRow 등

## 2025.12
- Compose 고급 예제 추가: ButtonGroup, WithContext, Path Hit, Recomposition 등

## 2025.11
- Kotlin 패턴 예제 추가: Sealed Class Interface, 코루틴 내부 동작, Modularization 등

## 2025.10
- 예제 추가: CompositionLocal, AutoCloseable, Inline 등

## 2025.09
- 신규 예제 추가: Navigation3, Shadow API, SnapNotify, 카드 모서리 스타일 등

## 2025.08
- 신규 예제 추가: Text AutoSizing 등

## 2025.07
- 컴포넌트 패키지 구조 대분류별 정리

## 2025.06
- 버전 최신화 및 Cursor Rules mdc 파일 추가 (9개 규칙 파일)

## 2025.03
- Cursor IDE 용 원본 문서 추가

## 2025.02
- targetSDK 35 UI 대응

## 2024.12
- toml 파일 작성 및 gradle 변경

## 2024.11
- README.md 갱신, 도메인 별 README.md 작성

## 2024.08
- Version Update 및 버전 대응

## 2024.06
- Clean Architecture 구조로 전환

## 2024.04
- 메인 화면 UI/UX 개선
