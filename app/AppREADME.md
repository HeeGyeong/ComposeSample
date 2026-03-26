# ComposeSample App 모듈

## 개요
Jetpack Compose를 활용한 UI 구현 예제와 다양한 기능 구현 사례를 포함하는 모듈입니다. Clean Architecture 원칙에 따라 설계되었습니다.

## 프로젝트 구조
```
app
├── Application       # Koin 초기화를 위한 Application 클래스
├── db                # 로컬 데이터베이스
├── di                # 의존성 주입 모듈
├── presentation      # UI 레이어 (Activity, Compose UI)
│   ├── example       # 예제 화면 모음
│   │   ├── component # 카테고리별 예제 컴포넌트
│   │   │   ├── ui              # UI 컴포넌트 예제
│   │   │   ├── interaction     # 인터랙션 예제
│   │   │   ├── data            # 데이터 처리 예제
│   │   │   ├── system          # 시스템/플랫폼 API 예제
│   │   │   └── architecture    # 아키텍처 패턴 예제
│   │   └── list      # 예제 목록 (연도별 분류)
│   └── legacy        # 이전 방식으로 구현된 화면
└── util              # 유틸리티 클래스
```

## 주요 화면
- **MainActivity**: 예제 코드와 레거시 코드로 이동하는 메인 화면
- **BlogExampleActivity**: 다양한 기능 구현 예제 목록 화면

## 예제 카테고리

### UI 컴포넌트 (`component/ui`)
| 패키지 | 내용 |
|--------|------|
| `accessibility` | 접근성 지원 (Large Content Viewer, TalkBack 등) |
| `button` | 버튼 스타일 및 인터랙션 |
| `canvas` | Canvas 도형 그리기, 애니메이션, 텍스트 렌더링 |
| `graphics` | 모션 블러, RenderEffect 등 그래픽 효과 |
| `layout/animation` | AnimatedVisibility, AnimatedContent, Shared Element Transition |
| `layout/bottomsheet` | ModalBottomSheet, Collapsed BottomSheet |
| `layout/drawer` | 사이드 Drawer 메뉴 |
| `layout/flexbox` | FlexBox 기반 유동 레이아웃 |
| `layout/header` | Fancy TopAppBar, 스크롤 연동 헤더 |
| `layout/lazycolumn` | LazyColumn 목록, Paging |
| `layout/pager` | HorizontalPager, VerticalPager |
| `layout/topappbar` | Custom TopAppBarScrollBehavior |
| `media/image` | 이미지 로딩 및 처리 |
| `media/lottie` | Lottie 애니메이션 |
| `media/picker` | Photo Picker, Embedded Photo Picker |
| `media/shimmer` | 로딩 Shimmer 효과 |
| `navigation` | Bottom Navigation, Custom Navigation |
| `notification` | 알림 구현 |
| `scroll` | 커스텀 스크롤 동작 |
| `shapes` | 커스텀 도형 및 클리핑 |
| `tab` | Responsive TabRow (SubcomposeLayout) |
| `text` | 텍스트 스타일링 |
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
| `api` | Retrofit API 통신, 네트워크 상태 감지 |
| `cache` | 캐시 전략 |
| `paging` | Paging 3 구현 |
| `sse` | Server-Sent Events |

### 시스템 / 플랫폼 API (`component/system`)
| 패키지 | 내용 |
|--------|------|
| `background/workmanager` | WorkManager 백그라운드 작업 |
| `deeplink` | 딥링크 처리 |
| `media/ffmpeg` | FFmpeg 미디어 처리 |
| `media/recorder` | 오디오/비디오 녹화 |
| `platform/file` | 파일 시스템 접근 |
| `platform/intent` | Intent 활용 |
| `platform/language` | 앱 내 언어 변경 |
| `platform/powersave` | 절전 모드 대응 |
| `platform/quicksettings` | 빠른 설정 타일 |
| `platform/shortcut` | 앱 단축키 |
| `platform/version` | API 버전별 분기 처리 |
| `platform/webview` | WebView 연동 |
| `ui/widget` | 홈 화면 위젯 |

### 아키텍처 패턴 (`component/architecture`)
| 패키지 | 내용 |
|--------|------|
| `lifecycle` | Lifecycle 인식 컴포넌트 |
| `modularization` | 멀티 모듈 구조 예제 |
| `navigation` | Navigation Component 통합 |
| `state` | 상태 관리 패턴 |
| `pattern/compositionLocal` | CompositionLocal 데이터 흐름, Tree 시각화 |
| `pattern/coroutine` | SnapshotFlow, 코루틴 Compose 통합 |
| `pattern/effect` | SideEffect, LaunchedEffect, rememberCoroutineScope |
| `pattern/mvi` | MVI 아키텍처 패턴 |
| `pattern/retain` | Compose 1.10 Retain API (ViewModel 대체) |
| `development/compose17` | Compose 최신 기능 |
| `development/concurrency` | 동시성 처리 패턴 |
| `development/coordinator` | Coordinator 패턴 화면 전환 |
| `development/flow` | Flow 테스트 (Turbine) |
| `development/init` | 앱 초기화 최적화 |
| `development/language` | Kotlin 언어 기능 예제 |
| `development/performance` | 성능 최적화, Rebound 리컴포지션 모니터링 |
| `development/preview` | @Preview 인터널, PreviewParameter 고급 활용 |
| `development/rebound` | 리컴포지션 예산 할당 및 감지 |
| `development/test` | 코루틴 Flow 테스트 (Turbine) |
| `development/type` | 타입 시스템 예제 (Inline Value Class 등) |

## 의존성 주입
Koin을 사용합니다. `di` 패키지에 모듈을 정의하고 `InjectModules.kt`에서 전체 모듈을 등록합니다.

## 예제 추가 방법
1. `ConstValue.kt`에 상수 추가
2. `component/` 하위 해당 카테고리에 UI 파일 및 Guide 파일 생성
3. `ExampleRouter.kt`에 라우팅 추가
4. `Examples20XX.kt`에 `ExampleObject` 항목 추가

자세한 규칙은 `CLAUDE.md` 및 `docs/` 폴더 참고.

## 주의사항
- Compose 버전과 Kotlin 버전 호환성 확인 필수
- 필요한 권한은 `AndroidManifest.xml`에 선언
- 신규 라이브러리 추가 시 팀 협의 필요
- Domain 레이어는 순수 Kotlin만 사용
- Presentation 레이어는 Compose만 사용
- 레이어 간 명확한 분리 유지
