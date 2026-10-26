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
- Kotlin 2.4.21
- Android Studio
- AGP 9.4.1(내장 Kotlin) / Gradle 9.8.0
- ComposeBom 2026.09.00 (Compose 1.12.1)
- Compile SDK 37
- Target SDK 35
- Min SDK 24
- Java 21

## 주요 라이브러리
- Room 2.8.5
- Koin 4.2.2
- WorkManager 2.12.0
- ViewModel 2.10.0
- Material 1.11.4
- Material3 1.4.0
- Lottie Compose 6.7.1
- Coil3 3.5.0

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
- **데이터 클래스**: @SerializedName, @Parcelize 어노테이션 규칙 · Gson 응답 모델은 nullable 필드 + 매퍼에서 기본값·필수값 처리(Gson 은 Kotlin 기본값을 건너뜀)
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
- **lazycolumn**: LazyColumn 성능 최적화, FlingBehavior 커스터마이징, targetSDK 35 대응, ReverseLazyColumn, LazyStaggeredGrid 폭포수 그리드(동적 높이, 필터링 애니메이션), LazyList `contentType` 재사용 풀 함정(아이템별 고유 contentType이 재사용 버킷을 폭증시켜 슬롯이 회수되지 않는 현상 — GC 이후 `WeakReference`로 실측), LazyList 캐시 윈도우와 노출 추적(Compose 1.12 `LazyLayoutCacheWindow` 를 `rememberLazyListState(cacheWindow)` 로 걸어 Dp·뷰포트 비율별로 미리 컴포즈(ahead)·유지(behind)되는 아이템 수 실측, 그 상태에서 `LaunchedEffect` 노출 로그가 화면 밖 아이템까지 세는 함정 vs `onVisibilityChanged`(리스트 경계 `layoutBounds` / 윈도우 기준 차이), 1.11 에서 deprecated 된 `onFirstVisible` 의 재부착 중복과 본 key 집합 대안, 무거운 아이템의 Pausable composition prefetch 분할 — 정지 화면은 예산 무제한·평균 학습 뒤 측정 패스 전락·debug HotSwan 제외)
- **pager**: ViewPager와 페이지 전환
- **topappbar**: FancyTopAppBar(Collapsing Toolbar, 다양한 스크롤 동작)
- **adaptive**: Adaptive Layout — WindowSizeClass(Compact/Medium/Expanded)를 통한 폰/태블릿/폴더블 적응형 레이아웃, Compose MediaQuery API — 윈도우 크기·폴더블 자세·포인터 정밀도·키보드 종류·시청 거리를 다루는 선언적 환경 쿼리(Compose 1.11 실험적 API) / Adaptive Pane Scaffold(Material3 Adaptive 1.3.0) — `ListDetailPaneScaffold`·`SupportingPaneScaffold` + `ThreePaneScaffoldNavigator`: 창 크기대로(`calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2())` — Compact 1 · Large 3분할)와 `PaneScaffoldDirective.copy(maxHorizontalPartitions)` 로 강제한 2·1분할 비교, `BackNavigationBehavior` 4종 실측(2분할에서 기본 PopUntilScaffoldValueChange 는 돌아갈 곳이 없어 예제가 닫힘 · CurrentDestinationChange 는 한 번에 목록 · ContentChange/PopLatest 는 글 단위, 같은 글이 겹치면 갈림)
- **custom**: Custom Layout — Layout 컴포저블과 MeasurePolicy로 직접 측정/배치하는 커스텀 레이아웃
- **grid**: Compose Grid API — non-lazy 2D 트랙 레이아웃(Compose 1.11 실험적 API): 6가지 트랙 크기 + minmax, gap, 자동/명시적 `gridItem` 배치와 span, `GridFlow` 방향, `LazyVerticalGrid`와의 실시간 비교로 Grid가 모든 자식을 컴포즈함을 확인
- **modifier**: Modifier Order — modifier 순서가 레이아웃/드로잉/히트 테스트에 미치는 영향
- **insets**: 디스플레이 컷아웃 & 인셋 — `safeDrawing`/`safeGestures`/`safeContent`가 어떤 기본 인셋의 합집합인지 바이트코드로 확인해 대조(waterfall은 safeDrawing에 없음), 컷아웃이 있는 기기에서도 창이 확장하지 않으면 `displayCutout` 인셋이 0인 것을 `layoutInDisplayCutoutMode`·edge-to-edge 토글로 실측, `consumeWindowInsets`/`recalculateWindowInsets`로 인셋 패딩이 0이 되는 과정, `WindowInsets.cutoutPath`(Compose 1.11 신규)와 `displayShape` Path 렌더링

**media**:
- **image**: Coil 3 이미지 로딩(AsyncImage, GIF 디코딩, 캐싱과 placeholder/error 상태) / 대용량 이미지 타일 딥줌 — 8000×6000 원본을 `BitmapRegionDecoder` 로 보이는 512px 타일만, 배율에 맞춘 `inSampleSize` 로 디코딩(전용 스레드·지나간 요청 취소·바이트 기준 LruCache·한 단계 성긴 개요 바닥), 한 번에 디코딩한 183MB 비트맵은 하드웨어 가속 캔버스가 그리기를 거부하는 것과 아래쪽 타일일수록 디코딩이 길어지는 JPEG 특성을 실측
- **lottie**: Lottie 애니메이션 구현과 제어
- **picker**: Embedded Photo Picker, BottomSheet 연동과 URI 수명 관리 / 시스템 Photo Picker 실물 — `PickVisualMedia`·`PickMultipleVisualMedia` 를 실제로 띄워 기기마다 고르는 단계(시스템 피커 → 시스템 앱 대체 → 문서 선택기, API 레벨이 아니라 SDK 확장 버전으로)와 실제로 뜨는 앱(`<queries>` 없으면 엉뚱한 처리 앱이 보임), 요청 옵션이 엑스트라로 가는 모양, 여러 장 최대 개수 min(생성자, 요청) 과 1·한도 초과 launch 예외(문서 선택기 기기에선 무시), 결과 플래그 READ | PERSISTABLE · 표시 이름 `<id>.jpg`, 받은 URI 권한이 받은 액티비티에 묶이는 것과 `takePersistableUriPermission`, 피커가 EXIF GPS 를 0 으로 덮는데 같은 사진을 문서 선택기로 받으면 위치가 그대로 남는 것·피커 URI `setRequireOriginal` 예외를 실측
- **shimmer**: UI Shimmer, Text Shimmer 로딩 효과, 자동 스켈레톤 로딩 감지 Modifier(`CompositionLocal`로 로딩 상태·shimmer 애니메이션을 자동 전파해 요소마다 `isLoading` 분기 없이 `Modifier.autoSkeleton()`만 붙이는 방식 — 실제 콘텐츠의 측정된 크기를 그대로 재사용, 기존 수동 스켈레톤 트리 방식과 대비)

**form**:
- **폼 상태와 검증**: 다중 필드 폼 — 검증 시점 3종(입력 즉시/포커스 이탈/제출)을 같은 규칙으로 대조하고 에러 표시 전환 횟수를 화면에서 실측, `isError`/`supportingText`와 IME 액션 기반 포커스 이동(`FocusRequester`), `derivedStateOf` 제출 게이팅의 리컴포지션 차이, 에러 문구가 필드 높이를 늘려 아래를 밀어내는 레이아웃 점프

**text**:
- 텍스트 스타일링, AutoSizing, 커스텀 TextMeasurer 렌더링
- **텍스트 선택 제어**: 읽기 전용 텍스트 선택(`SelectionContainer`/`DisableSelection`)과 컨텍스트 메뉴 — Compose 1.11 이 선택 메뉴를 `text.contextmenu` 로 옮기면서 `LocalTextToolbar` 교체가 기본값에서 동작하지 않게 된 사실을 실기기 계측으로 확인(`isNewContextMenuEnabled` 기본 true → showMenu 0회, 끄면 copy·selectAll 수신) + 1.11 시절 선택 문자열을 얻던 클립보드 우회 + Compose 1.12 `SelectionState` 실적용(`selectedTexts` 관찰, `selectAll`·`clear`·`extendSelectionByWord`·`select(TextRange)`·`getSelectableTexts`), 1~4번 카드 1.11.4 실측을 1.12.1 에서 재측정(동일), 재생성 복원은 리스트 밖에서만 온전하고 LazyColumn 항목 안에서는 0조각·반쪽 복원으로 깨지는 것을 Saver 로그로 규명
- TextOverflow(Start/Middle Ellipsis), LocalContext 문자열 안티패턴
- Rich Content in Text Input(contentReceiver를 통한 이미지/파일 붙여넣기 — 키보드/클립보드/드래그앤드롭 소스별 처리)
- TextField Max Length 숨겨진 버그(프로그래밍적 변경에는 InputTransformation이 적용되지 않는 버그 + LaunchedEffect+snapshotFlow 해결책)

**material3**:
- Material 3 Expressive(1.4.0 신규) — SecureTextField/OutlinedSecureTextField(비밀번호 입력 + TextObfuscationMode 4종) + foundation 1.12 `TextObfuscationMode.System`(시스템 '비밀번호 표시' 설정을 따름, API 37+ 터치·물리 키보드 분리)과 1.12 에서 설정을 무시하게 된 `RevealLastTyped` 를 설정 켬/끔으로 나란히 실측(M3 1.4.0 기본값이 RevealLastTyped 라 설정을 꺼도 노출), `LocalTextFieldContentObserverRegistrationExecutor` 로 관찰자 등록·해제를 백그라운드 스레드로

**others**:
- **accessibility**: Large Content Viewer(iOS 스타일 접근성, 키보드 & 스크린 리더 지원)
- **autofill**: semantics API를 통한 Compose Autofill(`contentType` 힌트 + `LocalAutofillManager` commit/cancel)
- **button**: ButtonGroup(Material 3 Expressive)
- **canvas**: Canvas 도형과 애니메이션, Dial 컴포넌트, Motion Blur(회전하는 바퀴), Compose Loaders 수학 곡선 기반 로딩 애니메이션(Rose/Lissajous/Lemniscate/Spirograph/Cardioid/Butterfly — 6가지 곡선) / Ribbon Modifier — 원을 끌며 만든 나선 경로를 `PathMeasure.getTangent` 부호로 앞뒤 구간으로 나눠 `drawContent()` 앞뒤에 번갈아 그리고 `chainPathEffect` 로 감기는 진행률을 더하는 Modifier, 원문의 `phase = 첫 구간` 대시가 구간 길이가 모두 같을 때만 맞아 시작 각을 옮기면 앞 그리기가 최대 72% 어긋나는 것(`getSegment` 분리와 대조)·`chainPathEffect` 인자 순서·진행률을 값으로 넘기면 `drawWithCache` 캐시가 프레임마다 버려지는 것을 실측
- **graphics**: New Shadow API(Compose 1.9), 다이얼로그 배경 블러(`blurBehindRadius`+`FLAG_BLUR_BEHIND` 크로스 윈도우 · `setBackgroundBlurRadius` 윈도우 배경 · `Modifier.blur` 컴포저블 세 가지를 구분하고, `isCrossWindowBlurEnabled` 를 리스너로 구독해 "코드는 성공하고 그림만 없는" 상태를 드러냄 + API 31 미만용 GraphicsLayer 캡처·소프트웨어 블러 폴백 비용 실측), Mesh Gradient(Compose 1.12 `MeshGradientPainter` — 꼭짓점 위치·색·베지어 제어점, bilinear/bicubic 대조, 꼭짓점 드래그 편집과 제어점 추론 규칙의 픽셀 단위 검증, OkLab 보간 실측, draw 단계 애니메이션 비용. 렌더러가 기본 검정 Paint 로 `drawVertices` 를 불러 Paint 색을 곱하는 기기(Galaxy A72/API 33 실측)에서는 전부 검게 나오는 문제를 판정하고 흰 Paint 소프트웨어 캔버스 호환 모드로 우회), Pixelate 모자이크 Modifier(값을 그대로 컴포즈한 채 그리기 단계에서만 가림 — 높이 기준 셀 · 셀 1개 = 픽셀 1개 비트맵 · `FilterQuality.Low` 2배 보간 · `None` 확대 · `saveLayer`+`DstIn` 페더 4단계, blockRatio 0.08/0.14/0.18/0.22 판독성, Text 마다 vs 부모 하나, 함정 3가지 — 재사용 비트맵을 안 지우면 옛 값이 누적(평균 어둡기 27 vs 66) · 레이어 없는 `DstIn` 은 가장자리를 검은 띠로 · `drawWithCache` 요소가 람다 동일성 비교라 호출부 리컴포즈마다 비트맵 재할당(`remember`·`Modifier.Node` 로 고정), 시맨틱 누출과 `clearAndSetSemantics`, API 31 미만에서 무시되는 `Modifier.blur` 대조, release 그리기 0.40~0.55ms)
- **navigation**: Navigation3 중첩 라우팅(NestedRoutesNav3)
- **notification**: SnapNotify(Snackbar 간소화 라이브러리)
- **overlay**: 좌표 기반 스포트라이트 오버레이(코치마크) — `onGloballyPositioned`로 타깃 좌표 수집 → `Popup` 전체화면 오버레이(부모 클리핑에 갇히지 않음) → `Path.op(Difference)` + `clipPath`로 스크림에 구멍 뚫기 → `animateFloatAsState`로 스텝 전환 애니메이션
- **scroll**: 커스텀 TopAppBarScrollBehavior, nested scroll, IME 인터랙티브 제어(`Modifier.imeNestedScroll()`로 스크롤 제스처를 키보드 표시/숨김 애니메이션에 연결 + `imeAnimationSource`/`imeAnimationTarget`로 실제 애니메이션 진행률을 커스텀 UI에 동기화), 커스텀 오버스크롤(`OverscrollEffect` 직접 구현으로 기본 스트레치/글로우를 고무줄로 교체 + `LocalOverscrollFactory` 로 하위 트리 일괄 적용및 null 비활성화 + `withoutVisualEffect()`/`withoutEventHandling()` 로 이벤트와 시각 효과 분리)
- **shader**: AGSL Shader Live Tuning(API 33+ `RuntimeShader` + `graphicsLayer` renderEffect, 실시간 uniform 슬라이더와 셰이더 소스 재컴파일)
- **shapes**: CardCorners(모서리 스타일)
- **style**: Foundation Style API(Compose 1.11 도입 실험적 API, 1.12 에서 스코프 인터페이스 구조로 재편) — `Modifier.styleable` + `Style { }` DSL, 상태 블록(`pressed { }` 등), `animate(spec) { }` 전환, 커스텀 `StyleStateKey`
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
- **api**: Retrofit API 호출, UseCase 패턴, 연결 끊김 처리, Ktor 고급 구성(MockEngine 위 Auth 토큰 리프레시·HttpRequestRetry 백오프 + 오리진 페일오버 — 원문의 검증 상황 10가지를 Ktor 플러그인으로 실행해 GET 만 재생·502~504 만 전환·POST 의 모호한 실패는 호출자에게·동시 실패는 실제로 쓴 오리진으로 판정·취소 시 폴백 없음·쿨다운 뒤 주 오리진 복귀를 기대값과 대조, 같은 판정을 단위 테스트로 고정), Gson 누락 필드 — 같은 응답(누락·명시적 null·키 이름 변경·낯선 값)을 일부만 기본값·전부 기본값·nullable 응답 모델 + 매퍼로 파싱해 Kotlin 기본값·non-null 이 우회되는 지점, 파싱은 성공하고 hashCode·copy()·when 등 값을 쓰는 줄에서 나는 NPE, Boolean 의 꺼짐/모름과 "Y"→false, `disableJdkUnsafe()` 의 한계, Retrofit·Ktor 컨버터 동일 결과를 실측 + 같은 응답을 kotlinx.serialization 1.12 Json 으로 — 생성자를 거쳐 누락 키는 기본값, 계약 위반은 파싱하는 줄에서 경로와 함께 실패, `coerceInputValues` 는 기본값 있는 프로퍼티의 null·모르는 enum 만, `MissingFieldException` 은 `JsonException` 이 아니라 `catch (e: JsonException)` 를 빠져나감, init 검증 실패의 `JsonDecodingException` 감싸기, 중첩 한도 400(release 401 겹 too nested · 기기 debug 는 230~240 겹 StackOverflowError), value class private 생성자의 팩토리 우회와 필드에도 걸리는 serializer, 예외 메시지에 붙는 입력 JSON(다른 필드의 카드번호)과 `exceptionsWithDebugInfo`
- **cache**: Room 로컬 데이터 캐싱과 CRUD, 실시간 검색 / Ktor HTTP 캐시 — `HttpCache` 플러그인과 MockEngine 원 서버로 max-age·no-cache·no-store·ETag·Last-Modified 응답별 서버 도달·조건부 헤더·304 를 요청 단위로 대조(304 는 앱에 200 으로 보인다), private 응답 × `isShared`, Ktor 3.6.0 신규 `clearAllCaches()`·`FileStorage(Path, SystemFileSystem)` 디스크 영속·`acceptHeaderMergeStrategy`
- **download**: 이어받기·분할 다운로드(HTTP Range) — MockEngine 가짜 서버(Range·If-Range·ETag, Range 미지원·HEAD 거부·도중 끊김·파일 교체 스위치) 위에서 HEAD/`bytes=0-0` Resolve → 연결 N개 구간 병렬 + `FileChannel.write(buf, position)` → 공유 토큰 버킷(256KiB/s 에서 연결 1·4개 같은 합계) → `.part`·`.state` 일시정지·프로세스 종료 뒤 이어받기, 재개 확인 3가지(If-Range · 재개 전 HEAD 비교 · 확인 없음) × 교체 시점 2가지에서 HEAD 비교는 확인과 요청 사이 교체를 놓쳐 v1·v2 가 섞이는 것, 200 을 206 처럼 이어 쓰면 파일이 커지고 끊긴 응답을 완료로 보면 크기는 같은 채 구멍이 남는 것(CRC 로만 잡힘)을 실측
- **outbox**: 오프라인 아웃박스 전송 — 보내기를 Room `PENDING` 행(기기에서 만든 clientId 가 기본 키)으로 먼저 저장해 말풍선을 바로 그리고 WorkManager 워커가 그 행을 보낸다. MockEngine 가짜 서버(같은 clientId 는 한 번만 저장하는 멱등 · 대화별 seq · 파일 영속, 응답 유실·503·400·401·오프라인 스위치) 위에서 응답만 잃은 뒤 같은 id 재시도는 서버 1건 · 시도마다 새 id 는 2건, 응답 유실 직후 `am kill` 해도 시스템이 새 프로세스로 워커를 띄워 같은 id 로 재전송, 일시 장애 때 대화별 체인은 순서를 지키되 뒤 메시지가 백오프만큼 기다리고 따로 걸면 서버 순서가 뒤집히는 것, 워커가 `Result.failure()` 로 끝내면 함께 매단 메시지는 정책과 무관하게 FAILED·`APPEND` 는 끝난 뒤 등록한 메시지까지 FAILED(행은 PENDING 으로 멈춤) vs 행만 FAILED + `success()`, `@Upsert` 가 고유 인덱스 충돌 뒤 기본 키로 UPDATE 해 자동 증가 키 설계에서는 중복 도착의 seq 가 조용히 버려지고 `REPLACE` 는 local_id 를 바꾸는 것을 실측
- **paging**: 페이징과 무한 스크롤; Paging3 `RemoteMediator` 오프라인 우선 페이징(네트워크 + DB 이중 소스, DB를 단일 진실 공급원으로 — `LoadType` REFRESH/PREPEND/APPEND 분기, RemoteKeys 테이블, `initialize()` 캐시 게이팅, `loadState.source`와 `loadState.mediator`를 별개 축으로 관찰)
- **repository**: Advanced Repository Pattern — Memory → Disk → Network 다중 소스 우선순위 해석과 캐시 채우기
- **room**: Room `@Fts4` MATCH 검색 vs `LIKE '%q%'` 전체 스캔, `@Index` 단일/복합 인덱스 쿼리 성능, DAO 인터페이스 상속 + `withTransaction`을 통한 멀티 테이블 삽입
- **sse**: Server-Sent Events와 실시간 데이터 스트리밍(okhttp-eventsource 5.0.0 `BackgroundEventSource` — 호출자가 close() 하면 onClosed 가 오지 않는 4.0+ 동작을 반영) / Ktor 3.6.0 SSE 클라이언트로 같은 스트림을 Flow 로 받아 콜백 방식과 대조 — 전용 스레드 유무, 종료를 정한 뒤 도착하는 이벤트(콜백 0~15개 vs Flow 0개), Job 취소·화면 이탈 시 정리, Ktor 기본 재연결 0회
- **storage**: DataStore ↔ SharedPreferences 대조 — 동시 증가 100회(SP 의 잃어버린 갱신 vs `updateData` 직렬화), 쓰기 200회 비용(apply·commit·개별/묶은 `updateData`), 손상 파일(SP 는 빈 값으로 넘어가고 DataStore 는 `CorruptionException`·`ReplaceFileCorruptionHandler` 복구), `SharedPreferencesMigration` 전체·일부 키, 같은 파일 두 인스턴스 `IllegalStateException`

### **system** - 시스템 연동 & 플랫폼
**platform**:
- **clipboard**: 클립보드 제한과 텍스트 처리 메뉴 — 창 포커스가 없으면 `getPrimaryClip()` 이 예외 없이 null(알림창만 내려 RESUMED 여도 거부, 홈은 CREATED)이고 변경 리스너도 오지 않는 것(API 31+ 는 텍스트 분류 뒤 쓰기 1회에 리스너 2회), 다른 앱 클립 읽기 알림(같은 클립·자기 앱 클립은 없음), `EXTRA_IS_SENSITIVE` 로 AOSP 복사 미리보기 가리기, 실험 전 클립 보관·복원 / PROCESS_TEXT 메뉴 항목 액티비티(기본 꺼짐·화면에서만 켜기)를 Compose·View·다른 앱 필드에서 대조 — Compose 기본 메뉴는 `startActivity` 로 열어 결과가 안 돌아오고 READONLY 가 뒤집힘, `filterTextContextMenuComponents`+`appendTextContextMenuComponents` 로 직접 연결, View 는 id 가 없으면 항목이 안 뜸, `<queries>` 없으면 다른 처리기 0개
- **contacts**: 연락처 쓰기는 편집기 인텐트로 · 읽기는 Contact Picker 로 — 다음 릴리스 타깃 시 막히는 `WRITE_CONTACTS` 직접 쓰기(`applyBatch` back-reference, 원문 Before)와 `ACTION_INSERT`·`ACTION_EDIT`·`ACTION_INSERT_OR_EDIT`(권한 없음)을 나란히: 저장했는데 `RESULT_CANCELED`(연락처 앱 첫 사용)·Samsung 피커 취소 1100 처럼 결과 코드로 저장을 못 가리는 것, `finishActivityOnSaveCompleted` 무관, `Insert.DATA` 의 표준 밖 MIME·사진 행이 Google 편집기에서 사라짐(Samsung 은 사진 표시), provider 의 이름 나누기 / API 37 Contact Picker(`ACTION_PICK_CONTACTS` — targetSdk 37 미만은 `EXTRA_USE_SYSTEM_CONTACTS_PICKER` 없으면 처리 앱 없음) 세션 URI(`0@` 사용자 id)·필드당 한 행·selection 예외·계정 열 제거, 레거시 `PickContact` 대조, URI 임시 권한이 프로세스가 아니라 받은 액티비티에 묶이는 것, `WRITE_CONTACTS` `maxSdkVersion` 으로 API 37 기기에서 쓰기 거부 미리 보기
- **display**: 디스플레이 주사율과 프레임 간격 — `Display.supportedModes`(사용자 설정에 따라 걸러지는 모드 목록)와 `preferredDisplayModeId` 창 단위 60/90Hz 요청을 `DisplayListener` 로 반영 확인(화면을 떠날 때 원복), `withFrameNanos` 2초 창으로 FPS·늦은 프레임 비율·p95/p99·최악 간격을 재고 부하(50ms 멈춤·매 프레임 13ms)로 평균 FPS 가 숨기는 끊김과 90Hz 의 줄어든 예산을 실측, Compose `preferredFrameRate` 가 API 35+ 에서만 `View.requestedFrameRate` 로 전달되는 경로(레이어가 다시 그려질 때 투표·움직이면 High 자동 투표), 지속 성능 모드, `FrameMetrics`(`Window.addOnFrameMetricsAvailableListener`) 단계별 프레임 시간 — 같은 13ms 부하를 컴포지션·`withFrameNanos`·`Modifier.layout`·`drawBehind` 에 두면 앞의 둘은 애니메이션, 뒤의 둘은 그리기 기록 단계로 잡히고 Compose 의 측정·배치는 FrameMetrics 측정·배치가 0 인 것(`dispatchDraw` 안에서 `measureAndLayout`)·`TOTAL_DURATION` 을 1/주사율이 아니라 `DEADLINE`(API 31+)과 비교해야 하는 이유를 실측
- **file**: 파일 선택과 SAF(Storage Access Framework) 처리
- **haptic**: Haptic Feedback(LocalHapticFeedback vs HapticFeedbackConstants 비교와 API 레벨별 지원 범위) + Compose 1.12 상호작용 사운드 — clickable·toggleable 이 탭마다 `LocalSoundEffect.playClickSound()` 를 자동 요청하는 기본값과 `SoundEffectOnInteraction(enabled = false)` 구역별 끄기를 구역별 "탭 수 · 요청 수"로 대조, pointerInput 제스처는 자동 재생이 없어 햅틱과 함께 직접 호출, 실제 재생 조건(시스템 터치음 + 무음·진동 모드 아님)을 실시간 표시
- **intent**: Intent 처리와 앱 간 데이터 공유
- **language**: 지역화, 시스템 언어 설정, 앱 내 언어 변경 / 시스템 앱별 언어 탭 — API 33 `LocaleManager.setApplicationLocales` 로 앱 전체 언어를 시스템에 맡기고 컨텍스트 교체 방식과 대조: 값이 바뀌면 액티비티 재생성(ViewModel·pid 유지), configuration 이 [앱 언어, 시스템 목록] 으로 합쳐져 `Locale.getDefault`·날짜 형식까지 바뀌고 `Resources.getSystem()` 도 섞인 값을 주는 것, `ja,en` 이 영어가 아니라 기본 values(한국어)로 떨어지는 이유(APK 에 라이브러리의 values-ja), ComponentActivity 에서 `AppCompatDelegate.setApplicationLocales` 가 조용히 무시되는 것, `android:localeConfig` 선언 전후 시스템 설정 "앱 언어" 화면, 시스템이 저장(프로세스 종료·재설치 유지, `pm clear` 로 초기화), `adb shell cmd locale set-app-locales` 를 실측
- **pip**: Picture-in-Picture compat(core 1.18.0) — `PictureInPictureParamsCompat`로 9개 필드를 버전 분기 없이 구성하고 `toPictureInPictureParams()`가 API 33/31/26으로 잘라내는 규칙을 플랫폼 getter 되읽기로 확인, 종횡비 허용 범위(0.418410~2.390000)와 `enterPictureInPictureMode`(RESUMED 필요) vs `setPictureInPictureParams`(정지 상태에서도 가능) 대조
- **powersave**: 절전 모드 감지와 배터리 최적화 / 백그라운드 제한 진단 탭 — 푸시·작업이 기기마다 늦거나 안 오는 이유를 앱이 스스로 읽어 판정: 앱 대기 버킷(`getAppStandbyBucket` — 앱이 앞이면 ACTIVE 라 백그라운드 때 값은 `queryEventsForSelf` 의 `STANDBY_BUCKET_CHANGED` 이력으로), 사용자의 '제한'(`isBackgroundRestricted` = 설정 '백그라운드 사용 허용' 끔), 배터리 최적화 예외('제한 없음' — 버킷도 숨은 값 5), Doze 깊은/가벼운·배터리 세이버·저전력 대기 브로드캐스트, 종료 기록(`REASON_USER_REQUESTED` 가 강제 중지·최근 앱 제거·Android 14 전 업데이트에 함께 쓰이고 설명 문자열로만 갈림)과 시작 기록(`wasForceStopped` 는 fork 시각이 이번 프로세스와 맞는 기록에서만 — API 35 에뮬레이터에서 목록 첫 기록이 지난 부팅 것이었고 부팅 직후엔 이번 실행 기록이 없었음), 설정 화면 이동(최적화 목록·앱 정보)과 adb 재현 명령
- **exitinfo**: 프로세스 종료 사후 분석 — ANR(메인 스레드 30초 막기)·크래시·`exitProcess(0)`·`Process.killProcess` 를 일으키기 직전에 종류·pid·시각을 남기고, 다음 실행에서 `getHistoricalProcessExitReasons`(API 30) 기록과 pid 로 대조: ANR = REASON_ANR(설명 'user request after error: Input dispatching timed out …'·트레이스) · 크래시 = REASON_CRASH(설명 'crash' 뿐 — 예외 클래스·메시지는 앱의 기본 처리기가 따로 기록) · exit = EXIT_SELF(status 0) · killProcess = SIGNALED(status 9), `setProcessStateSummary` 로 남긴 '하던 일'(128바이트 — 129바이트부터 IllegalArgumentException, 한글 42자), ANR 트레이스 main 스레드 스택(debug(HotSwan)는 앱 프레임 0줄 · release 는 `blockMainThreadForAnr` 가 6~7번째 줄), 입력 대기 한도 에뮬레이터 5초·Galaxy 10초
- **predictiveback**: Predictive Back Gesture(Android 14+ Flow 기반 엣지 스와이프 진행률 실시간 애니메이션)
- **process**: 멀티프로세스 앱 구조 — 같은 서비스를 기본 프로세스·`:remote`·`:isolated`(`isolatedProcess`)에 띄워 Messenger 로 왕복하며 프로세스별 PID·싱글턴·Koin 초기화 분리(`Application.onCreate` 프로세스 가드), 같은 프로세스는 통과하고 프로세스를 넘으면 `TransactionTooLargeException` 이 되는 바인더 한계(이진 탐색 경계 · 동기 1,040,384 B / oneway 520,096 B 실측), 원격 kill·크래시 후 자동 재연결과 1분 안 두 번째 크래시의 `onBindingDied`, 옛 Messenger 의 `DeadObjectException`, 격리 UID 의 권한·파일(ENOENT)·소켓(EACCES) 박탈, `getRunningAppProcesses` 프로세스 표와 PSS
- **biometric**: Biometric Authentication(biometric-compose 1.4.0-alpha07 — Compose 연동. 폴백 없음=기본 취소 버튼, `CustomOption` → `CustomFallbackSelected` 결과, 폴백 여러 개(최대 4)는 Android 16 QPR2 미만에서 첫 번째만 쓰이는 규칙까지)
- **quicksettings**: Quick Settings Tile
- **sensor**: 센서 퓨전 나침반(TYPE_ROTATION_VECTOR 방위각 파이프라인, remapCoordinateSystem 화면 회전 보정, 각도 랩어라운드를 견디는 저역통과 필터)
- **shortcut**: 앱 바로가기(dynamic, static, pin)
- **version**: Android SDK 버전 처리(targetSDK 34 권한 처리)
- **webview**: YouTube 임베드 WebView(전체 화면 커스텀 뷰) / JS ↔ 네이티브 브리지 — `addJavascriptInterface` 가 JavaBridge 스레드에서 동기로 불리는 것(JS 308ms 정지, 브리지 안 WebView 호출은 예외 → JS 오류)·인자/반환 타입 변환(`undefined`·객체 → "undefined", Int 버림, null 반환 → `undefined`, 돌려준 객체도 브리지)·`evaluateJavascript` 결과가 JSON 텍스트(예외도 null, Promise `{}`, 로드 직후엔 이전 문서)·`WebMessagePort` 대상 오리진 불일치는 조용히 미전달·dari 식 requestId 짝짓기(도착 순서 큐는 셋 다 엇갈리는데 앱 기록은 SUCCESS)·손 이스케이프 `\r` 로 응답 스크립트 SyntaxError·인터페이스가 모든 iframe·이동한 다른 오리진에 주입되고 `onPageStarted` 기반 오리진 검사는 로드 즉시 호출에 늦는 것·`removeJavascriptInterface` 는 다음 로드부터를 실측

**deeplink**:
- **Dynamic App Links**: 서버의 Digital Asset Links JSON을 통해 앱 업데이트 없이 실시간으로 딥링크 동작을 제어(Android 15+) / 실제 수신 탭 — 매니페스트의 커스텀 스킴 액티비티 두 개(`composesample://` singleTop · `composesample-standard://` standard)로 링크를 받아, `order` 가 path 가 아니라 host 로 잡히는 함정(path 만 보면 "알 수 없는 경로: 4821"), 콜드 `onCreate` 의 링크를 첫 컴포지션 전까지 보류했다가 배출(보류 칸이 없으면 빈 화면), 재생성·프로세스 종료 복원 때 `savedInstanceState` 를 안 보면 같은 링크를 다시 처리하는데 그게 마지막 웜 링크가 아니라 처음 링크라 보던 화면이 바뀌는 것, singleTop 의 `onNewIntent` vs standard 의 인스턴스 쌓임, 백스택 합성(뒤로 = 홈 vs 앱 종료), 값 검증과 잘못된 링크 화면을 실측

**media**:
- **ffmpeg**: 비디오/오디오 인코딩/디코딩(2025.06 기준 라이브러리 호환성 문제로 주석 처리)
- **recorder**: 오디오/비디오 녹화와 미디어 녹화 상태 관리
- **video**: Media3(ExoPlayer) 비디오 재생 — 네트워크 비디오를 위한 `PlayerView`의 Compose 연동

**background**:
- **location**: Background Location Tracking — 실제 `foregroundServiceType="location"` 서비스, 순차적 권한 처리(포그라운드 → 알림 → 백그라운드), `CoroutineWorker`와 대비해 WorkManager가 지속적인 위치 추적을 대체할 수 없는 이유 설명
- **workmanager**: 백그라운드 작업과 태스크 스케줄링 / 주기 작업 재등록 탭 — 앱 업데이트에서 주기를 15분 → 30분으로 바꿔 같은 이름으로 다시 등록할 때 `ExistingPeriodicWorkPolicy` KEEP·UPDATE·CANCEL_AND_REENQUEUE 를 고유 이름 셋에서 동시에 실행해 대조: KEEP 은 기존 사용자에게 새 주기·태그가 끝내 안 가고(신규 설치에선 셋이 같아 데모에서 안 보임), UPDATE 는 같은 id·generation +1·주기 기준 시각 유지로 추가 실행 없음(같은 사양이어도 등록마다 generation 상승), CANCEL_AND_REENQUEUE 는 새 id 의 첫 실행이 곧바로 돌아 앱 시작마다 등록하면 매번 실행, 실행 중 재등록 시 UPDATE 는 끊지 않고 다음 실행부터(워커 gen 0 · WorkInfo gen 1) · CANCEL_AND_REENQUEUE 는 stopReason 1 로 중단을 실측 / Worker 예외 핸들러 — work-runtime 2.11 의 `setWorkerExecutionExceptionHandler`·`setWorkerInitializationExceptionHandler` 로 워커가 예외로 죽는 사각지대를 열고, 실행 중 throw / `Result.failure()` / 생성자 throw / 생성자 시그니처 불일치 4종을 넣어 무엇이 핸들러를 깨우는지 실측 대조(넷 다 WorkInfo 는 FAILED, 생성자 예외만 `InvocationTargetException` 으로 감싸져 message 가 null) / WorkManager 이벤트 리스너 — work 2.12 의 `setExecutionEventListener`·`setScheduleEventListener` 로 작업 수명주기를 suspend 스트림으로 관찰. 정상 체인·체인 실패·실행 중 취소·예외 4개 시나리오의 콜백 순서를 타임라인으로 대조하고(체인 실패 시 실행 축은 조용하고 스케줄 축만 `onPrerequisiteFailed` 를 준다), 같은 실패를 2.11 Consumer 핸들러와 나란히 받아 호출 모델·순서 차이를 비교 / 주기 작업 실행 시각 탭 — 다음 실행이 "마지막 실행 끝 + 주기"라 실행 시각에 끌려가는 것(…:46.810)과 워커가 실행 안에서 다음 :00/:30 슬롯을 `setNextScheduleTimeOverride` + `updateWork` 로 지정해 정각(…:00.000)에 고정하는 것을 대조, 오버라이드의 최소 15분(첫 실행만 예외)·한 번만(실행 후 지워짐, 태그만 바꾼 updateWork 에는 남음, clear)·재시도에서 백오프보다 우선·flex 첫 실행(주기 − flex)·CANCEL_AND_REENQUEUE 와 함께면 IllegalArgumentException 을 WorkInfo.nextScheduleTimeMillis 와 소스 규칙 기대값으로 실측

**notification**:
- **Live Updates 알림**: Android 16 승격(promoted) 알림 — `NotificationCompat.ProgressStyle`의 세그먼트/포인트/트래커 아이콘, 설정자가 없고 세그먼트 길이 합으로 정해지는 `progressMax`, API 36 미만에서 단색 진행 막대 한 줄로 축약되는 폴백을 빌드된 Notification의 extras 실측으로 확인
- **Notification MetricStyle**: androidx.core 1.19 의 지표 알림 — `NotificationCompat.MetricStyle` + `Metric` 값 6종(FixedInt·FixedFloat·FixedText·FixedDate·FixedTime·TimeDifference)과 semantic style, 앱 도메인 값 하나에서 (API 37+) MetricStyle 과 (전 버전) content text·크로노미터 폴백을 함께 만드는 구조(생성자·TimeDifference 팩토리가 `@RequiresApi(37)`), API 37 미만에서 `apply()` 가 아무것도 그리지 않고 extras 에만 싣는 것을 앱 경로/강제 부착 extras 대조로 실측, `setCriticalMetric` 범위 미검사와 Api37Impl 의 `indexOf` 변환(범위 밖 → -1, 같은 지표 둘 → 첫 번째), 생성·빌드 시점 검증 예외, `NotificationCompat.Builder(context, notification)` 왕복 복원

**ui**:
- **widget**: Glance 위젯(App Widget)

**others**:
- **ai**: Gemini Nano 온디바이스 추론(AICore)
- **security**: App Security 진단(실제 TLS 인증서 피닝, Play Integrity mock), API 요청 서명(HMAC + 재전송 방지), Hardware-Backed Keystore, IPC/Exported Component 보안, Screenshot Detection / Security State(androidx.security:security-state 1.1.0) — 기기 SPL·커널·모듈 버전을 로컬로 읽고(`getGlobalSecurityState`·`getDeviceSecurityPatchLevel`), `createVulnerabilityReportUrl()` 로 받은 OSV 보고서를 `loadVulnerabilityReport` 에 넣어 게시 SPL·아직 없는 수정(게시 SPL 기준 `getPatchedCves` − 기기 SPL 기준)·`isDeviceFullyUpdated`·`areCvesPatched`, 업데이트 제공자 IPC(`queryAllAvailableUpdates` — GMS 1곳 응답). SYSTEM_MODULES SPL 은 감시 모듈 중 최솟값, 지원 종료 커널 계열은 비교 제외

### **architecture** - 아키텍처 & 개발 도구
**pattern**:
- **compositionLocal**: CompositionLocal 기초, Static/Dynamic 비교, 트리 시각화
- **coroutine**: 코루틴 기초, 내부 동작, withContext vs launch 비교
- **effect**: Side Effect 처리(LaunchedEffect, SideEffect, SnapshotFlow 등)
- **error**: Sealed 인터페이스 도메인 에러 처리 — 예외 던지기 대신 sealed interface를 함수 반환 타입으로 써서 실패 케이스를 시그니처에 직접 드러내고, exhaustive when으로 처리 누락을 컴파일 타임에 강제
- **mvi**: MVI 아키텍처 패턴과 단방향 데이터 흐름
- **remember**: rememberSaveable(재생성 생존), rememberSerializable(Compose 1.12 — @Serializable 데이터 클래스를 Saver 없이 저장, 손으로 쓴 Saver 가 새 필드를 놓치는 대조, SnapshotStateSetSerializer 로 mutableStateSetOf 저장, 다크 모드 재생성·프로세스 종료 후 복원 실측, 1.12.1 SnapshotStateSet Parcelable 버그와 LazyColumn 의 보이는 항목만 저장 규칙), rememberUpdatedState(최신 콜백), derivedStateOf(연산 최적화) 비교
- **retain**: Compose retain API 실물(runtime-retain 1.12.1) — 같은 카운터를 remember·rememberSaveable·retain·ViewModel 로 보관해 `Activity.recreate()`·다크 모드·헤더 ← 로 닫고 다시 열기·프로세스 종료에서 남는 것과 진행 중 작업이 재생성을 넘는지 대조, `RetainObserver` 다섯 콜백의 실제 순서(RESUMED 에서 if 로 숨기면 곧바로 onRetired — remember 와 같음, `ManagedRetainedValuesStore` 안에서는 숨겨도 같은 객체, 키 변경, 미리 컴포즈 후 cancel 의 onUnused, 재생성에서 `DisposableEffect` 만 재시작하는 `RetainedEffect`), `RetainedValuesStoreRegistry` 로 탭별 보존과 `clearChild`, `key()` 를 빠뜨리면 다른 탭의 객체가 돌아오는 함정

**development**:
- **compose17**: Compose 1.7 신규 기능(Graphics Layer, Path Graphics, LookaheadScope 등)
- **concurrency**: 코루틴 내부 동작, withContext 패턴, Coroutine Bridges(suspendCoroutine/suspendCancellableCoroutine으로 콜백 기반 API를 suspend 함수로 변환 + 콜백 대신 사람의 응답을 `CompletableDeferred` 로 기다리기 — 질문을 `StateFlow` 에 올리고 `await()` 에서 멈췄다가 다이얼로그가 `complete` 로 깨우는 구조, 원문에 없는 대기 중 취소 시 유령 다이얼로그와 `finally` 정리·identity 확인, 밀려난 요청의 `null`, 재생성에는 남고 프로세스 종료엔 사라지는 상태), 스택 트레이스 복구(suspend 경계에서 호출자 프레임이 사라지는 것을 실측 + assertion 기반 DEBUG 스위치가 안드로이드에서 꺼져 있는 이유 + 필드가 하나만 있어도 반사 복사가 스킵되는 규칙과 `CopyableThrowable` + `_COROUTINE` 경계 프레임 직접 재현), 구조적 동시성 가드레일(coroutines 1.11 이 `launch(Job())`·`launch(NonCancellable)`·`runInterruptible(Job())` 에 붙인 경고 오버로드의 구조 + 실제로 돌려 부모 취소가 닿지 않고 `coroutineScope` 가 기다리지 않으며 인터럽트가 안 되는 것을 실측, 정적 타입이 `CoroutineContext` 면 경고 없이 통과하는 빈틈과 대안, `StateFlow.onSubscription`·`SharedFlow.asFlow`·`CompletableDeferred.asDeferred` 노출 대조, 가드레일이 못 잡는 취소 경로 버그 — stdlib `runCatching`·`catch (e: Exception)` 이 `CancellationException` 을 삼켜 취소된 루프가 남은 항목을 전부 '실패'로 처리하고 끝까지 도는 것을 `runCatchingCancellable`·`ensureActive()` 와 대조, 예외를 잡는 함수에 `EXACTLY_ONCE` 계약을 붙이면 초기화 안 된 non-null `val` 이 null 로 읽히는 것, 함수 속에 숨은 `viewModelScope.launch` 때문에 `collectLatest` 가 옛 검색을 끊지 못하는 경쟁)
- **coordinator**: Coordinator 패턴 구현
- **cursor**: Cursor IDE 관련 예제(AI 코딩 어시스턴트 활용)
- **di**: Koin Compiler Plugin(KSP 없이 컴파일 타임 DI 해석)
- **featureflag**: Type-Safe Feature Flag(컴파일 타임에 안전한 플래그 정의와 롤아웃 제어)
- **flow**: FlatMap vs FlatMapLatest 비교
- **init**: 초기화 로직과 상태 관리, 앱 시작 최적화(App Startup / Baseline Profile / Koin lazy 초기화)
- **internals**: How Compose Works(Composition/Layout/Draw 단계), RememberObserver와 컴포지션 생명주기(onRemembered/onForgotten/onAbandoned 실측), Composition Observer(어떤 상태가 어떤 스코프를 무효화했는지 알려주는 인과 로그, `Snapshot` 관찰 API와의 상호 보완적 커버리지 대비), Slot Tree Inspector(`parseSourceInformation`으로 `compositionData`를 순회해 각 슬롯 그룹을 함수명/파일/라인/파라미터로 해석), Recomposer 레지스트리 관찰(`Recomposer.runningRecomposers`(옵트인 불필요) + `RecomposerInfo.observe(CompositionRegistrationObserver)`로 프로세스 전역의 컴포지션 등록/해제 관찰)
- **language**: Sealed Class Interface(타입 안전 계층 구조), Name-Based Destructuring(Kotlin 2.3.20 이름 기반 구조 분해), Kotlin 2.4 언어 기능(Collection Literals / Context Parameters) / 민감 값·불법 상태를 타입으로 막기 — 결제 시스템 패턴(Verifone, KotlinConf 2026)을 옮겨 가림 value class `Pan`(private 생성자 + toString 재정의 + `toNetwork()` 출구)이 문자열 템플릿·data class·`Pan?`·List·예외 메시지(logcat 스택 트레이스 포함)를 전부 `PAN[****1111]` 로 가리는 것과 거부 결과의 입력도 `RawPan` 으로 감싸야 하는 이유, Gson 이 그 장치를 우회하는 두 경로(직렬화는 toString 을 거치지 않아 원문 · `{"value":…}` — Pan 전용 TypeAdapter 도 value class 필드는 JVM 에서 String 이라 안 걸림 / 역직렬화는 private 생성자·검증 팩토리를 건너뜀 → nullable 응답 DTO + 팩토리), `@ExposedCopyVisibility` 의 public `copy()` 로 팩토리가 거부할 객체를 만드는 것 vs `@ConsistentCopyVisibility`(private, 2.5 부터 표시 없는 선언·바깥 copy 호출은 오류), 타임아웃을 `TimedOut` 결과로 받아 재시도 대신 조회하면 승인 2건 → 1건을 실측
- **performance**: Inline Value Class(성능 최적화), Stability Annotations(@Stable/@Immutable로 불필요한 리컴포지션 방지)
- **preview**: Compose Preview 기능, @Preview 내부 동작(렌더링 파이프라인, LocalInspectionMode, MultiPreview), Preview-only Annotation(@RequiresOptIn으로 컴파일 타임에 Preview 전용 Composable 제한)
- **rebound**: 역할 기반 리컴포지션 예산 모니터링
- **strictmode**: StrictMode 정책 위반 감지(메인 스레드 디스크/네트워크 I/O, 미해제 closeable)
- **test**: UI 테스트 TDD, 리컴포지션 감지, Coroutine Flow Testing(Turbine), Screenshot Testing(Paparazzi/Roborazzi), Preview-Driven Screenshot Testing(@Preview를 source of truth로 locale/fontScale/theme 매트릭스 파생), 스크린샷 테스트 이미지 결정론화(LocalAsyncImagePreviewHandler로 비동기 이미지 픽셀 고정), Compose UI Testing(createComposeRule, onNodeWithTag, performClick 등 테스트 패턴 가이드), WorkManager 테스트 하네스(work-testing의 TestDriver로 네트워크·충전 제약과 24시간 초기 지연을 기다리지 않고 통과, TestListenableWorkerBuilder 격리 실행 + runAttemptCount 주입으로 재시도 분기 검증) / Power-Assert(Kotlin 2.4.20) — 같은 조건을 plain·`@PowerAssert` 함수에 넣어 실패 메시지 대조, `PowerAssert.explanation` 의 `CallExplanation` 구조(인자 구간·부분식 값으로 직접 메시지 만들기), 한 번만 평가·실패 시에만 설명 생성·리플렉션 호출은 설명 null 실측, 기존 `kotlin.test` 단위 테스트에 그대로 적용, AGP 컴파일 이름 때문에 기본 `compilationFilter`(TESTS)가 안드로이드에 안 걸리는 함정과 debug HotSwan 제외
- **time**: kotlin.time 시간 API — Clock.System.now()/Instant 벽시계 vs TimeSource.Monotonic.markNow()/elapsedNow() 단조시계, measureTimedValue의 Duration 정밀도, TestTimeSource로 실시간 대기 없이 시간 전진
- **tracing**: Perfetto 커스텀 트레이스 이벤트(`Trace.beginSection`/`endSection`의 스레드 페어링 함정을 실측 + `beginAsyncSection`/`endAsyncSection`·`setCounter`로 안전하게 트레이싱)
- **type**: 변수 타입 활용과 컴파일 타임 최적화

**others**:
- **lifecycle**: AutoCloseable(자동 리소스 정리) / Composable 범위 LifecycleOwner — lifecycle 2.10 의 `rememberLifecycleOwner(maxLifecycle, parent)` 로 하위 트리에 상한 걸린 수명주기를 준다. HorizontalPager 의 현재 페이지만 RESUMED, 이웃 페이지는 STARTED 로 캡해 `repeatOnLifecycle(RESUMED)` 작업이 멈추는 것을 틱 수로 대조(상한 적용 10/0/0 vs 상한 없음 10/10/10), 상한 변경·백그라운드 왕복·컴포지션 이탈(ON_DESTROY) 이벤트 로그
- **modularization**: 모듈화 전략
- **navigation**: Navigation3, NestedRoutesNav3, NavigationEvent 디스패처(androidx.navigationevent 로 back/forward 양방향 이벤트 — DirectNavigationEventInput 으로 제스처 없이 주입해 콜백 순서를 결정론적으로 재현, 진행률은 콜백이 아니라 transitionState 로만 오는 설계, currentInfo/backInfo/forwardInfo 와 게이팅 실측 · 1.2.0 의 입력원 핸들러 상태 질의(`hasEnabled*Handlers`·`onHasEnabled*Changed`)·Owner 팩토리·fallback·뗀 입력원 무시를 실측), Nav3 SceneStrategy 바텀시트(실제 androidx.navigation3 로 `SceneStrategy` 를 구현해 `OverlayScene` 시트를 층층이 쌓기 — 시트 아래 화면은 컴포지션에 남아 Lifecycle 만 STARTED 로 캡되는 것, pop 후 `onRemove` 가 끝날 때까지 남는 퇴장 타이밍, 시트가 직접 `NavigationBackHandler` 를 가져야 back 이 새지 않는 이유 실측)
- **state**: SnapshotFlow(State → Flow 변환), Compose Snapshot System(State<T> 내부 동작 — Snapshot 격리 모델, derivedStateOf 최적화, withMutableSnapshot을 통한 원자적 상태 변경), Per-Item ViewModels(아이템별 ViewModel 스코프 — 수작업 per-key Store vs lifecycle 2.11 `rememberViewModelStoreProvider`·`rememberViewModelStoreOwner`, 구성 변경·삭제(`clearKey`)·접기·스크롤 이탈·화면 이탈별 정리 시점 실측, Provider 는 LazyColumn 밖에)

### **etc.**
- 실무에서 활용 가능성이 높은 다양한 기타 예제

## 참고 사항
- 일부 예제(예: 권한 관련)는 기본 설정이 필요할 수 있습니다
- Compose 1.4.0-alpha04 이하 버전에서는 키보드 관련 이슈가 있을 수 있습니다
- 실제 앱에 필요한 기본 로직이 구현되어 있어 그대로 재사용할 수 있습니다
- 라이브러리 버전이 업데이트되면서 구현된 일부 기능이 동작하지 않을 수 있습니다
- 버전 호환성이 깨진 예제는 삭제하지 않고 전체 주석 처리하여 보존합니다
- **API 키**: Naver API 등 외부 API 키는 `local.properties`에 별도로 설정해야 합니다(`NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`)
- **`local.properties`**: git 으로 추적하지 않습니다(`.gitignore` 대상). 클론 후 Android Studio 가 자동 생성하거나, 직접 만들어 `sdk.dir=<Android SDK 경로>` 를 넣으면 됩니다. 위 API 키도 같은 파일에 둡니다
- **Cursor Rules**: `.cursor/rules`의 mdc 파일은 Cursor IDE에서만 동작하며 다른 IDE에서는 영향을 주지 않습니다

## 더 알아보기
- **앱 설치 및 실행**: 프로젝트를 clone해서 직접 빌드/설치하면 다양한 컴포넌트와 UI 예제를 실기기에서 확인할 수 있어 더 편리합니다. 코드만으로는 파악하기 어려운 애니메이션, 제스처, 상호작용을 직접 체험해 보세요.
- **예제 설명**: 각 예제에 대한 상세 설명은 [티스토리 블로그](https://heegs.tistory.com/category/Android/Jetpack%20Compose)에서 확인할 수 있습니다.
- **규칙 문서**: 상세 규칙은 `app/src/main/java/com/example/composesample/docs/`(사람/Claude용)와 `.cursor/rules/*.mdc`(Cursor 전용)에 나뉘어 있습니다. 두 출처는 일부만 매핑되므로 함께 참고하세요. 전체 문서 목록은 `docs/README.md`를 확인하세요.
- **AI 코딩 어시스턴트**: Cursor IDE 사용 시 자동으로 적용되는 규칙이 일관된 코드 생성을 돕습니다.

## 라이선스

이 프로젝트는 [MIT License](LICENSE)를 따릅니다. 학습 및 참고 목적으로 자유롭게 사용하실 수 있습니다.
