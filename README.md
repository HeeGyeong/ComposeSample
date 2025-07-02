# ComposeSample

## 목차
- [프로젝트 소개](#프로젝트-소개)
- [개발 환경](#개발-환경)
- [주요 라이브러리](#주요-라이브러리)
- [프로젝트 구조](#프로젝트-구조)
- [주요 컴포넌트](#주요-컴포넌트)
- [주요 기능](#주요-기능)
- [컴포넌트 예제](#컴포넌트-예제)
- [주의사항](#주의사항)
- [더 자세한 내용](#더-자세한-내용)

## 프로젝트 소개
Jetpack Compose를 공부하고 실무에 적용하면서 발생했던 이슈와 자주 사용되는 다양한 기능들의 샘플을 모아둔 프로젝트입니다.

Clean Architecture 기반으로 구성되어 있으며, 컴포넌트들은 기능별로 체계적으로 분류되어 있어 원하는 예제를 쉽게 찾을 수 있습니다.

- **최신 업데이트**
  - 2025.07: 컴포넌트 패키지 구조 대분류별 정리
  - 2025.06: 버전 최신화 및 cursorrules mdc 파일 추가
  - 2025.03: Cursor IDE 용 md 파일 추가
  - 2025.02: targetSDK 35 UI 대응
  - 2024.12: toml 파일 작성 및 gradle 변경
  - 2024.11: README.md 갱신, 도메인 별 READMD.md 작성
  - 2024.08: Version Update 및 버전 대응
  - 2024.06: Clean Architecture 구조로 전환
  - 2024.04: 메인 화면 UI/UX 개선

## 개발 환경
- Kotlin 1.9.22
- Android Studio
- ComposeBom 2025.01.00
- Target SDK 34
- Min SDK 24
- Java 17

## 주요 라이브러리
- Room 2.6.1
- Koin 3.2.2
- WorkManager 2.9.1
- ViewModel 2.8.6
- Material 1.7.4
- Material3 1.1.0
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

### 주요 컴포넌트
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
- **animation**: Compose 애니메이션과 전환 효과
- **bottomsheet**: BottomSheet와 ModalBottomSheet 구현, 커스텀 BottomSheet
- **drawer**: Navigation Drawer와 Modal Drawer 구현
- **flexbox**: FlexBox 레이아웃과 반응형 디자인
- **header**: Sticky Header와 스크롤 상태 연동
- **lazycolumn**: LazyColumn 성능 최적화, FlingBehavior 커스터마이징, targetSDK 35 대응
- **pager**: ViewPager 구현과 페이지 전환

**media**:
- **lottie**: Lottie 애니메이션 구현과 제어
- **shimmer**: 로딩 상태 Shimmer 효과와 커스텀 애니메이션

**text**:
- 텍스트 스타일링과 다양한 표현 방식

### **interaction** - 사용자 상호작용 & 제스처
- **clickevent**: 다양한 클릭 이벤트 처리
- **drag**: LazyColumn 드래그 앤 드롭과 아이템 위치 변경
- **refresh**: Pull-to-Refresh 구현과 새로고침 애니메이션
- **swipe**: Swipe to Dismiss와 스와이프 제스처 처리

### **navigation** - 내비게이션
- Bottom Navigation 구현
- Navigation Component 활용

### **data** - 데이터 관리 & 네트워크
- **api**: Retrofit API 호출과 UseCase 패턴, 네트워크 연결 상태 모니터링
- **cache**: Room 로컬 데이터 캐싱과 CRUD, 실시간 검색 기능
- **paging**: 페이징 처리와 무한 스크롤
- **sse**: Server-Sent Events와 실시간 데이터 스트리밍

### **system** - 시스템 통합 & 플랫폼
**platform**:
- **file**: 파일 선택과 SAF(Storage Access Framework) 처리
- **intent**: Intent 처리와 앱 간 데이터 공유, 딥링크
- **language**: 다국어 처리와 언어 설정
- **powersave**: 절전 모드 감지와 배터리 최적화
- **shortcut**: 안드로이드 shortcuts (dynamic, static)
- **version**: Android SDK 버전 대응과 호환성 처리
- **webview**: WebView 구현과 JavaScript 인터페이스

**media**:
- **ffmpeg**: 비디오/오디오 인코딩/디코딩 (2025.06 기준 라이브러리 호환성 문제로 주석처리)
- **recorder**: 오디오/비디오 녹화와 미디어 레코딩 상태 관리

**background**:
- **workmanager**: 백그라운드 작업과 작업 스케줄링

### **architecture** - 아키텍처 & 개발 도구
**pattern**:
- **compositionLocal**: CompositionLocal을 사용한 데이터 공유
- **coroutine**: 코루틴 기반 비동기 처리
- **effect**: Compose Side Effect 처리 (LaunchedEffect, SideEffect 등)
- **mvi**: MVI 아키텍처 패턴과 단방향 데이터 흐름

**development**:
- **compose17**: Compose 1.7 신기능들 (Graphics Layer, Path Graphics 등)
- **coordinator**: Coordinator Pattern 구현
- **cursor**: Cursor IDE 관련 예제
- **init**: 초기화 로직과 상태 관리
- **preview**: Compose Preview 기능과 다양한 옵션
- **test**: UI 테스트 예제
- **type**: 타입 안전성과 컴파일 타임 최적화

**etc.** 그 외 실무에 사용할 법한 다양한 예제 작성


## 주의사항
- 일부 예제(예: Permission 관련)는 기본 설정이 필요할 수 있습니다
- Compose 버전 1.4.0-alpha04 이하에서는 키보드 관련 이슈가 있을 수 있습니다
- 실제 앱에서 필요한 기본 로직들은 그대로 활용 가능하도록 구현되어 있습니다
- Library version 최신화에 따라 구현된 기능들이 동작하지 않을 수 있습니다.
- Version 호환이 되지 않는 예제는 제거하지 않고 전체 주석 처리로 남겨두었습니다.

## 더 자세한 내용
각 예제에 대한 자세한 설명은 [Tistory Blog](https://heegs.tistory.com/category/Android/Jetpack)에서 확인하실 수 있습니다.
