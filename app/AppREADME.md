# ComposeSample App 모듈

## 개요
Jetpack Compose를 활용한 UI 구현 예제와 다양한 기능 구현 사례를 포함하는 모듈입니다. Clean Architecture 원칙에 따라 설계되었습니다.

## 프로젝트 구조
```
app
├── application       # Koin 초기화를 위한 Application 클래스
├── coordinator       # Coordinator 패턴 Initializer
├── di                # 의존성 주입 모듈
├── docs              # 규칙/가이드 원본 문서 (Cursor Rules 원본)
├── presentation      # UI 레이어 (Activity, Compose UI)
│   ├── example       # 예제 화면 모음
│   │   ├── component # 카테고리별 예제 컴포넌트
│   │   │   ├── ui              # UI 컴포넌트 예제
│   │   │   ├── interaction     # 인터랙션 예제
│   │   │   ├── data            # 데이터 처리 예제
│   │   │   ├── system          # 시스템/플랫폼 API 예제
│   │   │   └── architecture    # 아키텍처 패턴 예제
│   │   ├── list      # 예제 목록 (연도별 분류)
│   │   └── model     # 앱 전용 모델 (ExampleObject/ExampleMoveType, ARCH-05로 domain→app 이동)
│   └── legacy        # 이전 방식으로 구현된 화면
└── util              # 유틸리티 클래스
```
> 로컬 데이터베이스(Room)는 app이 아니라 `data` 모듈에 위치합니다.

## 주요 화면
- **MainActivity**: 예제 코드와 레거시 코드로 이동하는 메인 화면
- **BlogExampleActivity**: 다양한 기능 구현 예제 목록 화면

## 예제 카테고리

### UI 컴포넌트 (`component/ui`)
| 패키지 | 내용 |
|--------|------|
| `accessibility` | 접근성 지원 (Large Content Viewer, Accessible Focus Indicator) |
| `button` | 버튼 스타일 및 인터랙션 (ButtonGroup) |
| `canvas` | Canvas 도형/애니메이션, Dial, Compose Loaders 수학 곡선, Particle Emitter, Month Picker Dial |
| `graphics` | 모션 블러, New Shadow API(Compose 1.9) 등 그래픽 효과 |
| `shader` | AGSL Shader Live Tuning (RuntimeShader uniform 실시간 조절) |
| `style` | Foundation Style API (단일 CompositionLocal 디자인 토큰, Compose 1.11) |
| `material3` | Material 3 Expressive (SecureTextField 등 1.4.0 신규 컴포넌트) |
| `layout/animation` | AnimatedVisibility/AnimatedContent, Shared Element(+Debug Tooling), Spring/Tween/Snap, Animations Showcase |
| `layout/adaptive` | Adaptive Layout (WindowSizeClass 적응형) |
| `layout/custom` | Custom Layout (Layout composable + MeasurePolicy) |
| `layout/bottomsheet` | ModalBottomSheet, Collapsed BottomSheet |
| `layout/drawer` | 사이드 Drawer 메뉴 |
| `layout/flexbox` | FlowRow/FlowColumn 공식 Flexbox |
| `layout/header` | Fancy TopAppBar, 스크롤 연동 헤더 |
| `layout/lazycolumn` | LazyColumn 최적화, ReverseLazyColumn, LazyStaggeredGrid |
| `layout/modifier` | Modifier Order in Compose |
| `layout/pager` | HorizontalPager, VerticalPager |
| `layout/topappbar` | Custom TopAppBarScrollBehavior |
| `media/image` | 이미지 로딩 및 처리 |
| `media/lottie` | Lottie 애니메이션 |
| `media/picker` | Photo Picker, Embedded Photo Picker |
| `media/shimmer` | 로딩 Shimmer 효과 |
| `navigation` | Navigation3 중첩 라우팅 (NestedRoutesNav3) |
| `notification` | SnapNotify (Snackbar 간소화) |
| `scroll` | 커스텀 스크롤 동작, 중첩 스크롤 |
| `shapes` | CardCorners (모서리 스타일) |
| `tab` | Responsive TabRow (SubcomposeLayout) |
| `text` | 텍스트 스타일링/AutoSizing, Rich Content, TextField Max Length, Document Editing, Syntax Highlighting |
| `visibility` | 조건부 표시/숨김 |

### 인터랙션 (`component/interaction`)
| 패키지 | 내용 |
|--------|------|
| `clickevent` | 클릭 이벤트 처리 패턴 |
| `drag` | 드래그 제스처 |
| `refresh` | Pull-to-Refresh |
| `sticker` | 드래그·핀치·회전·스프링 물리 스티커 캔버스 |
| `swipe` | Swipe to Dismiss (Material 3) |

### 데이터 처리 (`component/data`)
| 패키지 | 내용 |
|--------|------|
| `api` | Retrofit/Ktor API 통신, UseCase 패턴, 네트워크 상태 감지 |
| `cache` | Room 로컬 캐싱·CRUD·실시간 검색 (UserCacheRepository 추상화) |
| `paging` | Paging 3 구현, 무한 스크롤 |
| `room` | Room 심화 — FTS4 vs LIKE 검색, Database Indices, Multi-Table Inserts |
| `sse` | Server-Sent Events |

### 시스템 / 플랫폼 API (`component/system`)
| 패키지 | 내용 |
|--------|------|
| `ai` | Gemini Nano 온디바이스 AI (ML Kit GenAI, Mock 시뮬레이션) |
| `security` | App Security (Cert Pinning/Secure Storage/Play Integrity), Hardware-Backed Keystore |
| `background/workmanager` | WorkManager 백그라운드 작업 |
| `deeplink` | 딥링크 처리, Dynamic App Links (Android 15+) |
| `media/ffmpeg` | FFmpeg 미디어 처리 (라이브러리 호환성 이슈로 주석 처리) |
| `media/recorder` | 오디오/비디오 녹화 |
| `platform/biometric` | 생체 인증 (BiometricPrompt, Compose) |
| `platform/file` | 파일 시스템 접근 (SAF) |
| `platform/haptic` | Haptic Feedback (API 레벨별 지원 비교) |
| `platform/intent` | Intent 활용 |
| `platform/language` | 앱 내 언어 변경 |
| `platform/powersave` | 절전 모드 대응 |
| `platform/predictiveback` | Predictive Back Gesture (Android 14+) |
| `platform/quicksettings` | 빠른 설정 타일 |
| `platform/shortcut` | 앱 단축키 (dynamic/static/pin) |
| `platform/version` | API 버전별 분기 처리 |
| `platform/webview` | WebView 연동 |
| `ui/widget` | Glance 홈 화면 위젯 |

### 아키텍처 패턴 (`component/architecture`)
| 패키지 | 내용 |
|--------|------|
| `lifecycle` | AutoCloseable (자동 리소스 정리) 등 Lifecycle 인식 컴포넌트 |
| `modularization` | 멀티 모듈 구조 예제 |
| `navigation` | Navigation3, NestedRoutesNav3 |
| `state` | SnapshotFlow, Compose Snapshot System, Per-Item ViewModels |
| `pattern/compositionLocal` | CompositionLocal 데이터 흐름, Static/Dynamic 비교, Tree 시각화 |
| `pattern/coroutine` | 코루틴 기초·내부 동작, withContext vs launch |
| `pattern/effect` | SideEffect, LaunchedEffect, SnapshotFlow, rememberCoroutineScope |
| `pattern/mvi` | MVI 아키텍처 패턴 (단방향 데이터 흐름) |
| `pattern/remember` | rememberSaveable/rememberUpdatedState/derivedStateOf 비교 |
| `pattern/retain` | Compose 1.10 Retain API (ViewModel 없이 상태 보존) |
| `development/compose17` | Compose 1.7 신기능 (Graphics Layer, LookaheadScope 등) |
| `development/concurrency` | 동시성 처리, Coroutine Bridges (suspendCoroutine) |
| `development/coordinator` | Coordinator 패턴 화면 전환 |
| `development/cursor` | Cursor IDE / AI 코딩 어시스턴트 활용 예제 |
| `development/di` | Koin Compiler Plugin (애노테이션 기반 컴파일 안전성) |
| `development/featureflag` | Type-Safe Feature Flag (sealed 레지스트리 + 디버그 오버라이드) |
| `development/flow` | FlatMap vs FlatMapLatest 비교 |
| `development/init` | 앱 초기화/시작 속도 최적화 (App Startup / Baseline Profile) |
| `development/language` | Sealed Class Interface, Name-Based Destructuring |
| `development/performance` | Inline Value Class, Stability Annotations |
| `development/preview` | @Preview 인터널, Preview-only Annotation(@RequiresOptIn) |
| `development/rebound` | 리컴포지션 예산 할당 및 감지 |
| `development/test` | UI 테스트 TDD, Recomposition 감지, Coroutine Flow(Turbine), Screenshot(Paparazzi/Roborazzi), Compose UI Testing |
| `development/type` | 타입 시스템 예제 (변수 타입/컴파일 타임 최적화) |

## 의존성 주입
Koin을 사용합니다. `di` 패키지에 모듈을 정의하고 `InjectModules.kt`에서 전체 모듈을 등록합니다.

## 예제 추가 방법
1. `ConstValue.kt`에 상수 추가
2. `Examples20XX.kt`에 `ExampleObject` 항목 추가
3. `component/` 하위 해당 카테고리에 UI 파일(`*ExampleUI.kt`) 및 `exampleGuide.kt` 생성
4. `ExampleRouter.kt`에 라우팅 추가

자세한 규칙은 `CLAUDE.md`("새 예제 추가 방법" 4단계) 및 `docs/` 폴더 참고.

## 주의사항
- Compose 버전과 Kotlin 버전 호환성 확인 필수
- 필요한 권한은 `AndroidManifest.xml`에 선언
- 신규 라이브러리 추가 시 팀 협의 필요
- Domain 레이어는 순수 Kotlin만 사용
- Presentation 레이어는 Compose만 사용
- 레이어 간 명확한 분리 유지
