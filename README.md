# ComposeSample

## 목차
- [소개](#소개)
- [개발 환경](#개발-환경)
- [주요 라이브러리](#주요-라이브러리)
- [프로젝트 구조](#프로젝트-구조)
- [Cursor Rules 설정](#cursor-rules-설정)
- [주요 컴포넌트](#주요-컴포넌트)
- [주요 기능](#주요-기능)
- [컴포넌트 예제](#컴포넌트-예제)
- [참고 사항](#참고-사항)
- [더 알아보기](#더-알아보기)

## 소개
Jetpack Compose를 학습하고 실무에 적용하면서 마주친 이슈들과 자주 사용하는 다양한 기능들을 예제로 정리한 프로젝트입니다.

Clean Architecture 기반으로 구성되어 있으며, 원하는 예제를 쉽게 찾을 수 있도록 컴포넌트를 기능별로 체계적으로 분류했습니다.

최신 변경 이력은 [CHANGELOG.md](CHANGELOG.md)에서 확인할 수 있습니다.

## 개발 환경
- Kotlin 2.4.0
- Android Studio
- AGP 8.13.2 / Gradle 8.13
- ComposeBom 2026.05.00
- Target SDK 35
- Min SDK 24
- Java 21

## 주요 라이브러리
- Room 2.8.4
- Koin 3.2.2
- WorkManager 2.9.1
- ViewModel 2.9.1
- Material 1.11.1
- Material3 1.4.0
- Lottie Compose 6.0.0
- Coil3 3.1.0

## 프로젝트 구조

```
ComposeSample
├── app
│ ├── presentation # UI 레이어 (Activity, Compose UI)
│ │ └─ example # 예제 기능 패키지
│ │   ├── component # 컴포넌트 예제
│ │   │   ├── ui # UI 컴포넌트 & 레이아웃
│ │   │   ├── interaction # 사용자 상호작용 & 제스처
│ │   │   ├── navigation # 네비게이션
│ │   │   ├── data # 데이터 관리 & 네트워크
│ │   │   ├── system # 시스템 연동 & 플랫폼
│ │   │   └── architecture # 아키텍처 & 개발 도구
│ │   ├── list # ExampleObject 목록 정의
│ │   └── model # ExampleObject 등 UI 모델
│ ├── coordinator # Coordinator 패턴 초기화
│ ├── di # 의존성 주입
│ └── util # 유틸리티 클래스
│
├── coordinator
│ └── coordinator # Coordinator 패턴 구현체
│
├── core
│ └── navigation # Coordinator 인터페이스
│
├── data
│ ├── api # API 인터페이스
│ ├── repository # Repository 구현체
│ └── db # 로컬 데이터베이스 (데이터 모델 포함)
│
└── domain
  ├── repository # Repository 인터페이스
  ├── useCase # UseCase 정의
  └── model # 도메인 모델
 
```

## AI 코딩 어시스턴트 설정

이 프로젝트는 **Cursor IDE**와 **Claude Code** 사용자 모두를 위한 **AI 코딩 어시스턴트 규칙**을 제공합니다.

### Claude Code
프로젝트 루트의 `CLAUDE.md` 파일은 아키텍처 규칙, 파일 네이밍 컨벤션, 예제 추가 방법 등을 정의하며, Claude Code 세션 시작 시 자동으로 로드됩니다.

### Cursor Rules 설정

Cursor IDE를 사용하는 개발자를 위한 **AI 코딩 어시스턴트 규칙**도 함께 제공합니다.

`.cursor/rules` 디렉터리에는 Cursor AI가 프로젝트의 아키텍처와 코딩 스타일을 자동으로 이해하고 일관된 코드를 생성할 수 있도록 9개의 mdc 파일이 포함되어 있습니다.

### 규칙 파일 구성

```
.cursor/rules/
├── data-rules.mdc                  # 데이터 클래스 구현 규칙
├── api-creation-guide.mdc          # API 생성 가이드
├── api-ui-binding.mdc              # API-UI 바인딩 규칙
├── code-style.mdc                  # Kotlin & Compose 코드 스타일
├── comprehensive-ui-guide.mdc      # 종합 UI 시스템 가이드
├── dependency-management.mdc       # Koin 의존성 주입 가이드
├── performance-optimization.mdc    # 성능 최적화 가이드
├── project-structure.mdc           # Clean Architecture 구조 가이드
└── testing-guide.mdc               # 테스트 가이드
```

### 주요 규칙 주제

- **아키텍처**: Clean Architecture + MVVM 패턴
- **UI 프레임워크**: Jetpack Compose + Material3
- **의존성 주입**: Koin 프레임워크
- **코딩 스타일**: Kotlin 네이밍 컨벤션, 한글 주석
- **데이터 클래스**: @SerializedName, @Parcelize 어노테이션 규칙
- **성능**: Compose 리컴포지션, 메모리 관리
- **테스트**: 단위 테스트, UI 테스트 커버리지

### 규칙 문서 참조

규칙 내용은 두 곳에 나뉘어 있습니다.

- **`app/src/main/java/com/example/composesample/docs/`** — 사람/Claude Code를 위한 상세 규칙 문서(`DataRules`, `DIRules`, `UIRules` 등)와 프롬프트 가이드.
- **`.cursor/rules/*.mdc`** — Cursor IDE 전용 규칙(frontmatter 포함, 영어). 이 중 `code-style`, `performance-optimization`, `project-structure`, `testing-guide` 4개는 **`.cursor/rules`에만 존재**하며 docs/ 아래에는 대응 문서가 없습니다.

두 출처는 주제별로 완전히 1:1 매핑되지 않으므로, 규칙을 확인할 때 두 곳을 모두 참고하세요. (문서 인덱스: `docs/README.md`)

## 주요 컴포넌트
- **MainActivity**: 가장 기본적인 Compose 사용 예제
- **BlogExampleActivity**: 실무에 적용 가능한 다양한 기능 구현
  - BottomSheet
  - Navigation Drawer
  - LazyColumn
  - WorkManager
  - 권한 처리
  - WebView
  - Drag & Drop
  - 그 외 다양한 실무 예제

## 주요 기능
1. **UI 컴포넌트**
   - BottomSheet, Navigation Drawer 등 다양한 UI 컴포넌트 예제
   - Compose Preview를 활용한 UI 미리보기
   - 커스텀 애니메이션과 전환 효과

2. **상태 관리**
   - ViewModel을 활용한 상태 관리
   - Compose State와 Side Effect 처리
   - LaunchedEffect, RememberCoroutineScope 활용

3. **성능 최적화**
   - LazyColumn 최적화
   - 메모리 누수 방지
   - 리컴포지션 최소화

## 컴포넌트 예제

> 아래 목록은 실제 코드를 기준으로 AI(Claude Code)가 디렉터리를 탐색해 정리한 카탈로그입니다. 새 예제가 추가될 때마다 코드와의 diff를 확인해 자동으로 갱신하고 있어(DOC-DRIFT 사이클), 사람이 손으로 나열한 것보다 기계적으로 느껴질 수 있습니다.

### **ui** - UI 컴포넌트 & 레이아웃
**layout**:
- **animation**: Compose 애니메이션, Shared Element Transition, AnimatedContent 심화(탭 전환, 카운터, 상태 전환, transitionSpec 갤러리), Spring/Tween/Snap/Keyframes 비교(물리 기반 바운스, 시간 기반 이징, 즉시 전환, 구간별 커스텀), 2D 경로 애니메이션(`ArcAnimationSpec`/`ArcMode` 호(arc), `keyframesWithSpline` 경유점 스무딩, 구간별 `using ArcMode`, `DeferredTargetAnimation` + `approachLayout` — 스펙 자체를 샘플링해 그린 경로)
- **bottomsheet**: BottomSheet, ModalBottomSheet, 커스텀 BottomSheet
- **drawer**: Navigation Drawer, Modal Drawer
- **flexbox**: FlexBox 레이아웃과 반응형 디자인, 공식 FlowRow/FlowColumn Flexbox(CSS Flexbox에서 영감을 받은 줄바꿈, maxItemsInEachRow 제한, weight 공간 분배), Flow 오버플로 제어(`maxLines`, `FlowRowOverflow.expandIndicator`/`expandOrCollapseIndicator`, `ContextualFlowRow`의 인덱스 기반 지연 컴포지션, 일반 `FlowRow`와의 컴포지션 항목 수 실측 비교)
- **header**: 스크롤 상태와 연동되는 Sticky Header
- **lazycolumn**: LazyColumn 성능 최적화, FlingBehavior 커스터마이징, targetSDK 35 대응, ReverseLazyColumn, LazyStaggeredGrid 폭포수 그리드(동적 높이, 필터링 애니메이션), LazyList `contentType` 재사용 풀 함정(아이템별 고유 contentType이 재사용 버킷을 폭증시켜 슬롯이 회수되지 않는 현상 — GC 이후 `WeakReference`로 실측)
- **pager**: ViewPager와 페이지 전환
- **topappbar**: FancyTopAppBar(Collapsing Toolbar, 다양한 스크롤 동작)
- **adaptive**: Adaptive Layout — WindowSizeClass(Compact/Medium/Expanded)를 통한 폰/태블릿/폴더블 적응형 레이아웃, Compose MediaQuery API — 윈도우 크기·폴더블 자세·포인터 정밀도·키보드 종류·시청 거리를 다루는 선언적 환경 쿼리(Compose 1.11 실험적 API)
- **custom**: Custom Layout — Layout 컴포저블과 MeasurePolicy로 직접 측정/배치하는 커스텀 레이아웃
- **grid**: Compose Grid API — non-lazy 2D 트랙 레이아웃(Compose 1.11 실험적 API): 6가지 트랙 크기 + minmax, gap, 자동/명시적 `gridItem` 배치와 span, `GridFlow` 방향, `LazyVerticalGrid`와의 실시간 비교로 Grid가 모든 자식을 컴포즈함을 확인
- **modifier**: Modifier Order — modifier 순서가 레이아웃/드로잉/히트 테스트에 미치는 영향

**media**:
- **image**: Coil 3 이미지 로딩(AsyncImage, GIF 디코딩, 캐싱과 placeholder/error 상태)
- **lottie**: Lottie 애니메이션 구현과 제어
- **picker**: Embedded Photo Picker, BottomSheet 연동과 URI 수명 관리
- **shimmer**: UI Shimmer, Text Shimmer 로딩 효과

**text**:
- 텍스트 스타일링, AutoSizing, 커스텀 TextMeasurer 렌더링
- TextOverflow(Start/Middle Ellipsis), LocalContext 문자열 안티패턴
- Rich Content in Text Input(contentReceiver를 통한 이미지/파일 붙여넣기 — 키보드/클립보드/드래그앤드롭 소스별 처리)
- TextField Max Length 숨겨진 버그(프로그래밍적 변경에는 InputTransformation이 적용되지 않는 버그 + LaunchedEffect+snapshotFlow 해결책)

**material3**:
- Material 3 Expressive(1.4.0 신규) — SecureTextField/OutlinedSecureTextField(비밀번호 입력 + 3가지 TextObfuscationMode)

**others**:
- **accessibility**: Large Content Viewer(iOS 스타일 접근성, 키보드 & 스크린 리더 지원)
- **autofill**: semantics API를 통한 Compose Autofill(`contentType` 힌트 + `LocalAutofillManager` commit/cancel)
- **button**: ButtonGroup(Material 3 Expressive)
- **canvas**: Canvas 도형과 애니메이션, Dial 컴포넌트, Motion Blur(회전하는 바퀴), Compose Loaders 수학 곡선 기반 로딩 애니메이션(Rose/Lissajous/Lemniscate/Spirograph/Cardioid/Butterfly — 6가지 곡선)
- **graphics**: New Shadow API(Compose 1.9)
- **navigation**: Navigation3 중첩 라우팅(NestedRoutesNav3)
- **notification**: SnapNotify(Snackbar 간소화 라이브러리)
- **scroll**: 커스텀 TopAppBarScrollBehavior, nested scroll, IME 인터랙티브 제어(`Modifier.imeNestedScroll()`로 스크롤 제스처를 키보드 표시/숨김 애니메이션에 연결 + `imeAnimationSource`/`imeAnimationTarget`로 실제 애니메이션 진행률을 커스텀 UI에 동기화)
- **shader**: AGSL Shader Live Tuning(API 33+ `RuntimeShader` + `graphicsLayer` renderEffect, 실시간 uniform 슬라이더와 셰이더 소스 재컴파일)
- **shapes**: CardCorners(모서리 스타일)
- **style**: Foundation Style API(Compose 1.11 실험적 API) — `Modifier.styleable` + `Style { }` DSL, 상태 변형, `animate()` 전환, 커스텀 `StyleStateKey`
- **tab**: ResponsiveTabRow(SubcomposeLayout 기반 반응형 탭)
- **visibility**: Visibility 처리 패턴

### **interaction** - 사용자 상호작용 & 제스처
- **clickevent**: 다양한 클릭 이벤트 처리와 중복 방지
- **drag**: 아이템 재정렬이 가능한 LazyColumn 드래그 앤 드롭
- **pointer**: IndirectPointerInputModifierNode 원시 트랙패드 캡처와 PointerEventType.Pan*/Scale* 표준 파이프라인 대조
- **refresh**: Pull-to-Refresh 구현과 새로고침 애니메이션
- **sticker**: 스티커 캔버스(드래그, 핀치 리사이즈, 회전, 스프링 물리, peel-off 애니메이션)
- **swipe**: Swipe to Dismiss, Material 3 SwipeToDismissBox

### **navigation** - 네비게이션
- Bottom Navigation 구현
- Navigation3(신규 네비게이션 컴포넌트)
- NestedRoutesNav3(중첩 라우팅)

### **data** - 데이터 관리 & 네트워크
- **api**: Retrofit API 호출, UseCase 패턴, 연결 끊김 처리
- **cache**: Room 로컬 데이터 캐싱과 CRUD, 실시간 검색
- **paging**: 페이징과 무한 스크롤; Paging3 `RemoteMediator` 오프라인 우선 페이징(네트워크 + DB 이중 소스, DB를 단일 진실 공급원으로 — `LoadType` REFRESH/PREPEND/APPEND 분기, RemoteKeys 테이블, `initialize()` 캐시 게이팅, `loadState.source`와 `loadState.mediator`를 별개 축으로 관찰)
- **repository**: Advanced Repository Pattern — Memory → Disk → Network 다중 소스 우선순위 해석과 캐시 채우기
- **room**: Room `@Fts4` MATCH 검색 vs `LIKE '%q%'` 전체 스캔, `@Index` 단일/복합 인덱스 쿼리 성능, DAO 인터페이스 상속 + `withTransaction`을 통한 멀티 테이블 삽입
- **sse**: Server-Sent Events와 실시간 데이터 스트리밍

### **system** - 시스템 연동 & 플랫폼
**platform**:
- **file**: 파일 선택과 SAF(Storage Access Framework) 처리
- **haptic**: Haptic Feedback(LocalHapticFeedback vs HapticFeedbackConstants 비교와 API 레벨별 지원 범위)
- **intent**: Intent 처리와 앱 간 데이터 공유
- **language**: 지역화, 시스템 언어 설정, 앱 내 언어 변경
- **powersave**: 절전 모드 감지와 배터리 최적화
- **predictiveback**: Predictive Back Gesture(Android 14+ Flow 기반 엣지 스와이프 진행률 실시간 애니메이션)
- **biometric**: Biometric Authentication(biometric-compose alpha — Compose 연동)
- **quicksettings**: Quick Settings Tile
- **shortcut**: 앱 바로가기(dynamic, static, pin)
- **version**: Android SDK 버전 처리(targetSDK 34 권한 처리)
- **webview**: WebView 구현과 JavaScript interface

**deeplink**:
- **Dynamic App Links**: 서버의 Digital Asset Links JSON을 통해 앱 업데이트 없이 실시간으로 딥링크 동작을 제어(Android 15+)

**media**:
- **ffmpeg**: 비디오/오디오 인코딩/디코딩(2025.06 기준 라이브러리 호환성 문제로 주석 처리)
- **recorder**: 오디오/비디오 녹화와 미디어 녹화 상태 관리
- **video**: Media3(ExoPlayer) 비디오 재생 — 네트워크 비디오를 위한 `PlayerView`의 Compose 연동

**background**:
- **location**: Background Location Tracking — 실제 `foregroundServiceType="location"` 서비스, 순차적 권한 처리(포그라운드 → 알림 → 백그라운드), `CoroutineWorker`와 대비해 WorkManager가 지속적인 위치 추적을 대체할 수 없는 이유 설명
- **workmanager**: 백그라운드 작업과 태스크 스케줄링

**ui**:
- **widget**: Glance 위젯(App Widget)

**others**:
- **ai**: Gemini Nano 온디바이스 추론(AICore)
- **security**: App Security 진단(인증서 피닝, Play Integrity mock), Hardware-Backed Keystore, IPC/Exported Component 보안, Screenshot Detection

### **architecture** - 아키텍처 & 개발 도구
**pattern**:
- **compositionLocal**: CompositionLocal 기초, Static/Dynamic 비교, 트리 시각화
- **coroutine**: 코루틴 기초, 내부 동작, withContext vs launch 비교
- **effect**: Side Effect 처리(LaunchedEffect, SideEffect, SnapshotFlow 등)
- **mvi**: MVI 아키텍처 패턴과 단방향 데이터 흐름
- **remember**: rememberSaveable(회전 생존), rememberUpdatedState(최신 콜백), derivedStateOf(연산 최적화) 비교
- **retain**: Compose retain API(Compose 1.10)를 통한 ViewModel 없는 상태 유지

**development**:
- **compose17**: Compose 1.7 신규 기능(Graphics Layer, Path Graphics, LookaheadScope 등)
- **concurrency**: 코루틴 내부 동작, withContext 패턴, Coroutine Bridges(suspendCoroutine/suspendCancellableCoroutine으로 콜백 기반 API를 suspend 함수로 변환)
- **coordinator**: Coordinator 패턴 구현
- **cursor**: Cursor IDE 관련 예제(AI 코딩 어시스턴트 활용)
- **di**: Koin Compiler Plugin(KSP 없이 컴파일 타임 DI 해석)
- **featureflag**: Type-Safe Feature Flag(컴파일 타임에 안전한 플래그 정의와 롤아웃 제어)
- **flow**: FlatMap vs FlatMapLatest 비교
- **init**: 초기화 로직과 상태 관리, 앱 시작 최적화(App Startup / Baseline Profile / Koin lazy 초기화)
- **internals**: How Compose Works(Composition/Layout/Draw 단계), RememberObserver와 컴포지션 생명주기(onRemembered/onForgotten/onAbandoned 실측), Composition Observer(어떤 상태가 어떤 스코프를 무효화했는지 알려주는 인과 로그, `Snapshot` 관찰 API와의 상호 보완적 커버리지 대비), Slot Tree Inspector(`parseSourceInformation`으로 `compositionData`를 순회해 각 슬롯 그룹을 함수명/파일/라인/파라미터로 해석), Recomposer 레지스트리 관찰(`Recomposer.runningRecomposers`(옵트인 불필요) + `RecomposerInfo.observe(CompositionRegistrationObserver)`로 프로세스 전역의 컴포지션 등록/해제 관찰)
- **language**: Sealed Class Interface(타입 안전 계층 구조), Name-Based Destructuring(Kotlin 2.3.20 이름 기반 구조 분해)
- **performance**: Inline Value Class(성능 최적화), Stability Annotations(@Stable/@Immutable로 불필요한 리컴포지션 방지)
- **preview**: Compose Preview 기능, @Preview 내부 동작(렌더링 파이프라인, LocalInspectionMode, MultiPreview), Preview-only Annotation(@RequiresOptIn으로 컴파일 타임에 Preview 전용 Composable 제한)
- **rebound**: 역할 기반 리컴포지션 예산 모니터링
- **strictmode**: StrictMode 정책 위반 감지(메인 스레드 디스크/네트워크 I/O, 미해제 closeable)
- **test**: UI 테스트 TDD, 리컴포지션 감지, Coroutine Flow Testing(Turbine), Screenshot Testing(Paparazzi/Roborazzi), Compose UI Testing(createComposeRule, onNodeWithTag, performClick 등 테스트 패턴 가이드)
- **tracing**: Perfetto 커스텀 트레이스 이벤트(`Trace.beginSection`/`endSection`의 스레드 페어링 함정을 실측 + `beginAsyncSection`/`endAsyncSection`·`setCounter`로 안전하게 트레이싱)
- **type**: 변수 타입 활용과 컴파일 타임 최적화

**others**:
- **lifecycle**: AutoCloseable(자동 리소스 정리)
- **modularization**: 모듈화 전략
- **navigation**: Navigation3, NestedRoutesNav3
- **state**: SnapshotFlow(State → Flow 변환), Compose Snapshot System(State<T> 내부 동작 — Snapshot 격리 모델, derivedStateOf 최적화, withMutableSnapshot을 통한 원자적 상태 변경)

### **etc.**
- 실무에서 활용 가능성이 높은 다양한 기타 예제

## 참고 사항
- 일부 예제(예: 권한 관련)는 기본 설정이 필요할 수 있습니다
- Compose 1.4.0-alpha04 이하 버전에서는 키보드 관련 이슈가 있을 수 있습니다
- 실제 앱에 필요한 기본 로직이 구현되어 있어 그대로 재사용할 수 있습니다
- 라이브러리 버전이 업데이트되면서 구현된 일부 기능이 동작하지 않을 수 있습니다
- 버전 호환성이 깨진 예제는 삭제하지 않고 전체 주석 처리하여 보존합니다
- **API 키**: Naver API 등 외부 API 키는 `local.properties`에 별도로 설정해야 합니다(`NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`)
- **Cursor Rules**: `.cursor/rules`의 mdc 파일은 Cursor IDE에서만 동작하며 다른 IDE에서는 영향을 주지 않습니다

## 더 알아보기
- **앱 설치 및 실행**: 프로젝트를 clone해서 직접 빌드/설치하면 다양한 컴포넌트와 UI 예제를 실기기에서 확인할 수 있어 더 편리합니다. 코드만으로는 파악하기 어려운 애니메이션, 제스처, 상호작용을 직접 체험해 보세요.
- **예제 설명**: 각 예제에 대한 상세 설명은 [티스토리 블로그](https://heegs.tistory.com/category/Android/Jetpack)에서 확인할 수 있습니다.
- **규칙 문서**: 상세 규칙은 `app/src/main/java/com/example/composesample/docs/`(사람/Claude용)와 `.cursor/rules/*.mdc`(Cursor 전용)에 나뉘어 있습니다. 두 출처는 일부만 매핑되므로 함께 참고하세요. 전체 문서 목록은 `docs/README.md`를 확인하세요.
- **AI 코딩 어시스턴트**: Cursor IDE 사용 시 자동으로 적용되는 규칙이 일관된 코드 생성을 돕습니다.

## 라이선스

이 프로젝트는 [MIT License](LICENSE)를 따릅니다. 학습 및 참고 목적으로 자유롭게 사용하실 수 있습니다.
