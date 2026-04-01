# ComposeSample

## 목차
- [프로젝트 소개](#프로젝트-소개)
- [개발 환경](#개발-환경)
- [주요 라이브러리](#주요-라이브러리)
- [프로젝트 구조](#프로젝트-구조)
- [Cursor Rules 설정](#cursor-rules-설정)
- [주요 컴포넌트](#주요-컴포넌트)
- [주요 기능](#주요-기능)
- [컴포넌트 예제](#컴포넌트-예제)
- [주의사항](#주의사항)
- [더 자세한 내용](#더-자세한-내용)

## 프로젝트 소개
Jetpack Compose를 공부하고 실무에 적용하면서 발생했던 이슈와 자주 사용되는 다양한 기능들의 샘플을 모아둔 프로젝트입니다.

Clean Architecture 기반으로 구성되어 있으며, 컴포넌트들은 기능별로 체계적으로 분류되어 있어 원하는 예제를 쉽게 찾을 수 있습니다.

- **최신 업데이트**
  - 2026.04: 신규 예제 추가 (LazyStaggeredGrid 폭포수 그리드 등)
  - 2026.03: 신규 예제 추가 (MotionBlur, LargeContentViewer, LocalContextStrings, EmbeddedPicker Compose 통합, Rebound 리컴포지션 모니터링, Coroutine Flow Testing with Turbine, Compose Preview Internals, Remember Patterns, Startup Optimization, AnimatedContent 심화 등)
  - 2026.02: 신규 예제 추가 (Transition, Dial, Photo Picker, Sticker Canvas 등)
  - 2026.01: UI 컴포넌트 예제 추가 (Quick Setting, TopAppBar, Canvas Shapes, Responsive TabRow 등)
  - 2025.12: Compose 고급 예제 추가 (ButtonGroup, WithContext, Path Hit, Recomposition 등)
  - 2025.11: Kotlin 패턴 예제 추가 (Sealed Class Interface, 코루틴 내부 동작, Modularization 등)
  - 2025.10: 예제 추가 (CompositionLocal, AutoCloseable, Inline 등)
  - 2025.09: 신규 예제 추가 (Navigation3, Shadow API, SnapNotify, 카드 모서리 스타일 등)
  - 2025.08: 신규 예제 추가 (Text AutoSizing 등)
  - 2025.07: 컴포넌트 패키지 구조 대분류별 정리
  - 2025.06: 버전 최신화 및 Cursor Rules mdc 파일 추가 (9개 규칙 파일)
  - 2025.03: Cursor IDE 용 원본 문서 추가
  - 2025.02: targetSDK 35 UI 대응
  - 2024.12: toml 파일 작성 및 gradle 변경
  - 2024.11: README.md 갱신, 도메인 별 READMD.md 작성
  - 2024.08: Version Update 및 버전 대응
  - 2024.06: Clean Architecture 구조로 전환
  - 2024.04: 메인 화면 UI/UX 개선

## 개발 환경
- Kotlin 2.1.0
- Android Studio
- ComposeBom 2025.01.00
- Target SDK 35
- Min SDK 24
- Java 17

## 주요 라이브러리
- Room 2.6.1
- Koin 3.2.2
- WorkManager 2.9.1
- ViewModel 2.9.1
- Material 1.7.4
- Material3 1.3.2
- Lottie Compose 6.0.0
- Coil Compose 2.5.0

## 프로젝트 구조

```
ComposeSample
├── app
│ ├── presentation # UI 레이어 (Activity, Compose UI)
│ │ ├─ example # example feature package
│ │ │ └─ component # 컴포넌트 예제
│ │ │   ├── ui # UI 컴포넌트 & 레이아웃
│ │ │   ├── interaction # 사용자 상호작용 & 제스처
│ │ │   ├── navigation # 내비게이션
│ │ │   ├── data # 데이터 관리 & 네트워크
│ │ │   ├── system # 시스템 통합 & 플랫폼
│ │ │   └── architecture # 아키텍처 & 개발 도구
│ │ └─ legacy  # legacy feature package
│ ├── coordinator # Coordinator pattern Initializer
│ ├── di # 의존성 주입
│ ├── util # 유틸리티 클래스
│ └── model # UI 모델 클래스
│
├── Coordinator
│ └── coordinator # Coordinator pattern Initializer
│
├── Core
│ └── navigation # Coordinator interface
│
├── data
│ ├── api # API 인터페이스
│ ├── repository # Repository 구현체
│ ├── db # 로컬 데이터베이스
│ └── model # Data 모델
│
└── domain
  ├── repository # Repository 인터페이스
  ├── usecase # UseCase 정의
  └── model # 도메인 모델
 
```

## AI 코딩 어시스턴트 설정

이 프로젝트는 **Cursor IDE** 및 **Claude Code** 사용자를 위한 **AI 코딩 어시스턴트 규칙**을 제공합니다.

### Claude Code
프로젝트 루트의 `CLAUDE.md` 파일에 아키텍처 규칙, 파일 네이밍 컨벤션, 예제 추가 방법 등이 정의되어 있어 Claude Code 세션 시작 시 자동으로 로드됩니다.

### Cursor Rules 설정

Cursor IDE를 사용하는 개발자들을 위한 **AI 코딩 어시스턴트 규칙**도 제공됩니다.

`.cursor/rules` 디렉토리에 9개의 mdc 파일이 포함되어 있어, Cursor AI가 프로젝트의 아키텍처와 코딩 스타일을 자동으로 이해하고 일관된 코드를 생성할 수 있도록 도와줍니다.

### Rules 파일 구성

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
└── testing-guide.mdc               # 테스팅 가이드
```

### 주요 규칙 내용

- **아키텍처**: Clean Architecture + MVVM 패턴
- **UI 프레임워크**: Jetpack Compose + Material3
- **의존성 주입**: Koin 프레임워크
- **코딩 스타일**: Kotlin 네이밍 컨벤션, 한국어 주석
- **데이터 클래스**: @SerializedName, @Parcelize 어노테이션 규칙
- **성능 최적화**: Compose 리컴포지션, 메모리 관리
- **테스팅**: 단위 테스트, UI 테스트 커버리지

### 원본 문서 참조

상세한 규칙 내용은 `app/src/main/java/com/example/composesample/docs/` 폴더의 원본 문서에서 확인할 수 있습니다.

## 주요 컴포넌트
- **MainActivity**: 가장 기본적인 Compose 사용 예제
- **BlogExampleActivity**: 실무에 적용 가능한 다양한 기능 구현 예제
  - BottomSheet
  - Navigation Drawer
  - LazyColumn
  - WorkManager
  - Permission 처리
  - WebView
  - Drag & Drop
  - 등 다양한 실무 예제 포함

## 주요 기능
1. **UI 컴포넌트**
   - BottomSheet, Navigation Drawer 등 다양한 UI 컴포넌트 예제
   - Compose Preview를 활용한 UI 미리보기
   - 커스텀 애니메이션 및 전환 효과

2. **상태 관리**
   - ViewModel을 활용한 상태 관리
   - Compose의 State 및 Side Effect 처리
   - LaunchedEffect, RememberCoroutineScope 활용

3. **성능 최적화**
   - LazyColumn 최적화
   - 메모리 누수 방지
   - 리컴포지션 최소화

## 컴포넌트 예제

### **ui** - UI 컴포넌트 & 레이아웃
**layout**:
- **animation**: Compose 애니메이션, Shared Element Transition, AnimatedContent 심화 (탭 전환·카운터·상태 전환·transitionSpec 갤러리)
- **bottomsheet**: BottomSheet, ModalBottomSheet, 커스텀 BottomSheet
- **drawer**: Navigation Drawer, Modal Drawer
- **flexbox**: FlexBox 레이아웃과 반응형 디자인
- **header**: Sticky Header와 스크롤 상태 연동
- **lazycolumn**: LazyColumn 성능 최적화, FlingBehavior 커스터마이징, targetSDK 35 대응, LazyStaggeredGrid 폭포수 그리드 (동적 높이, 필터링 애니메이션)
- **pager**: ViewPager와 페이지 전환
- **topappbar**: FancyTopAppBar (Collapsing Toolbar, 다양한 스크롤 동작)

**media**:
- **lottie**: Lottie 애니메이션 구현과 제어
- **picker**: Embedded Photo Picker, BottomSheet 통합 및 URI 수명 관리
- **shimmer**: UI Shimmer, Text Shimmer 로딩 효과

**text**:
- 텍스트 스타일링, AutoSizing, 커스텀 TextMeasurer 렌더링
- TextOverflow (Start/Middle Ellipsis), LocalContext 문자열 안티패턴

**기타**:
- **accessibility**: Large Content Viewer (iOS 스타일 접근성, 키보드·스크린 리더 지원)
- **button**: ButtonGroup (Material 3 Expressive)
- **canvas**: Canvas 도형 & 애니메이션, Dial 컴포넌트, Motion Blur (스피닝 휠)
- **graphics**: New Shadow API (Compose 1.9)
- **navigation**: Navigation3 중첩 라우팅 (NestedRoutesNav3)
- **notification**: SnapNotify (Snackbar 간소화 라이브러리)
- **scroll**: 커스텀 TopAppBarScrollBehavior, 중첩 스크롤
- **shapes**: CardCorners (모서리 스타일)
- **tab**: ResponsiveTabRow (SubcomposeLayout 반응형 탭)
- **visibility**: Visibility 처리 패턴

### **interaction** - 사용자 상호작용 & 제스처
- **clickevent**: 다양한 클릭 이벤트 처리와 중복 방지
- **drag**: LazyColumn 드래그 앤 드롭과 아이템 위치 변경
- **refresh**: Pull-to-Refresh 구현과 새로고침 애니메이션
- **sticker**: 스티커 캔버스 (드래그, 핀치 리사이즈, 회전, 스프링 물리, 필오프 애니메이션)
- **swipe**: Swipe to Dismiss, Material 3 SwipeToDismissBox

### **navigation** - 내비게이션
- Bottom Navigation 구현
- Navigation3 (새 Navigation 컴포넌트)
- NestedRoutesNav3 (중첩 라우팅)

### **data** - 데이터 관리 & 네트워크
- **api**: Retrofit API 호출, UseCase 패턴, 연결 해제 처리
- **cache**: Room 로컬 데이터 캐싱과 CRUD, 실시간 검색
- **paging**: 페이징 처리와 무한 스크롤
- **sse**: Server-Sent Events와 실시간 데이터 스트리밍

### **system** - 시스템 통합 & 플랫폼
**platform**:
- **file**: 파일 선택과 SAF(Storage Access Framework) 처리
- **intent**: Intent 처리와 앱 간 데이터 공유
- **language**: 다국어 처리, 시스템 언어 설정, 앱 내 언어 변경
- **powersave**: 절전 모드 감지와 배터리 최적화
- **quicksettings**: 빠른 설정 타일 (Quick Settings Tile)
- **shortcut**: 앱 숏컷 (dynamic, static, pin)
- **version**: Android SDK 버전 대응 (targetSDK 34 권한 처리)
- **webview**: WebView 구현과 JavaScript 인터페이스

**media**:
- **ffmpeg**: 비디오/오디오 인코딩/디코딩 (2025.06 기준 라이브러리 호환성 문제로 주석처리)
- **recorder**: 오디오/비디오 녹화와 미디어 레코딩 상태 관리

**background**:
- **workmanager**: 백그라운드 작업과 작업 스케줄링

**ui**:
- **widget**: Glance 위젯 (App Widget)

### **architecture** - 아키텍처 & 개발 도구
**pattern**:
- **compositionLocal**: CompositionLocal 기본, Static/Dynamic 비교, 트리 시각화
- **coroutine**: 코루틴 기초, 내부 동작 원리, withContext vs launch 비교
- **effect**: Side Effect 처리 (LaunchedEffect, SideEffect, SnapshotFlow 등)
- **mvi**: MVI 아키텍처 패턴과 단방향 데이터 흐름
- **remember**: rememberSaveable(회전 생존), rememberUpdatedState(콜백 최신화), derivedStateOf(계산 최적화) 비교
- **retain**: Compose retain API로 ViewModel 없이 상태 보존 (Compose 1.10)

**development**:
- **compose17**: Compose 1.7 신기능 (Graphics Layer, Path Graphics, LookaheadScope 등)
- **concurrency**: 코루틴 내부 동작 원리, withContext 패턴
- **coordinator**: Coordinator Pattern 구현
- **cursor**: Cursor IDE 관련 예제 (AI 코딩 어시스턴트 활용)
- **flow**: FlatMap vs FlatMapLatest 비교
- **init**: 초기화 로직과 상태 관리, 앱 시작 속도 최적화 (App Startup / Baseline Profile / Koin 지연 초기화)
- **language**: Sealed Class Interface (타입 안전 계층)
- **performance**: Inline Value Class (성능 최적화)
- **preview**: Compose Preview 기능, @Preview 내부 동작 원리 (렌더링 파이프라인, LocalInspectionMode, MultiPreview)
- **rebound**: 역할 기반 리컴포지션 예산 모니터링
- **test**: UI 테스트 TDD, Recomposition 감지, Coroutine Flow Testing (Turbine)
- **type**: 변수 타입 활용과 컴파일 타임 최적화

**기타**:
- **lifecycle**: AutoCloseable (자동 리소스 정리)
- **modularization**: 모듈화 전략
- **navigation**: Navigation3, NestedRoutesNav3
- **state**: SnapshotFlow (State → Flow 변환)

### **etc.**
- 그 외 실무에 사용할 법한 다양한 예제 작성


## 주의사항
- 일부 예제(예: Permission 관련)는 기본 설정이 필요할 수 있습니다
- Compose 버전 1.4.0-alpha04 이하에서는 키보드 관련 이슈가 있을 수 있습니다
- 실제 앱에서 필요한 기본 로직들은 그대로 활용 가능하도록 구현되어 있습니다
- Library version 최신화에 따라 구현된 기능들이 동작하지 않을 수 있습니다.
- Version 호환이 되지 않는 예제는 제거하지 않고 전체 주석 처리로 남겨두었습니다.
- **API 키**: Naver API 등 외부 API 키는 `local.properties`에 별도 설정 필요합니다. (`NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`)
- **Cursor Rules**: `.cursor/rules` 디렉토리의 mdc 파일들은 Cursor IDE에서만 동작하며, 다른 IDE에서는 영향을 주지 않습니다.

## 더 자세한 내용
- **앱 설치 및 실행**: 프로젝트를 클론하여 직접 앱을 빌드하고 설치하면 다양한 컴포넌트와 UI 예제들을 실제 디바이스에서 확인할 수 있어 더욱 편리합니다. 코드만으로는 파악하기 어려운 애니메이션, 제스처, 인터랙션 등을 직접 확인해보세요.
- **예제 설명**: 각 예제에 대한 자세한 설명은 [Tistory Blog](https://heegs.tistory.com/category/Android/Jetpack)에서 확인하실 수 있습니다.
- **Cursor Rules**: 프로젝트의 아키텍처와 코딩 규칙은 `app/src/main/java/com/example/composesample/docs/` 폴더의 원본 문서에서 확인할 수 있습니다.
- **AI 코딩 어시스턴트**: Cursor IDE 사용 시 자동으로 적용되는 규칙들이 일관된 코드 생성을 도와줍니다.
