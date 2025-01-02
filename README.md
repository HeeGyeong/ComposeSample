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

- **최신 업데이트**
  - 2024.12: toml 파일 작성 및 gradle 변경
  - 2024.11: README.md 갱신, 도메인 별 READMD.md 작성
  - 2024.08: Version Update 및 버전 대응
  - 2024.06: Clean Architecture 구조로 전환
  - 2024.04: 메인 화면 UI/UX 개선

## 개발 환경
- Kotlin 1.9.0
- Android Studio
- Compose 1.5.2
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
│ │ ├─ example # example feature pacakge
│ │ └─ legacy  # legacy feature pacakge
│ ├── coordinator # Coordinator pattern Initializer
│ ├── di # 의존성 주입
│ ├── util # 유틸리티 클래스
│ └── model # UI 모델 클래스
│
├── Coordinator
│ └── coordinator # Coordinator pattern Initializer
│
├── Core
│ └── navigatrion # Coordinator interface
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
1. **api**: 
- Retrofit을 사용한 API 호출과 UseCase 패턴 구현
- 네트워크 연결 상태 모니터링 및 에러 처리

2. **bottomsheet**: 
- BottomSheet와 ModalBottomSheet 구현
- 애니메이션과 상태 관리를 포함한 커스텀 BottomSheet

3. **cache**: 
- Room을 사용한 로컬 데이터 캐싱과 CRUD 구현
- 실시간 검색 기능이 포함된 데이터 관리

4. **drag**: 
- LazyColumn에서의 드래그 앤 드롭 기능
- 아이템 위치 변경 및 애니메이션 처리

5. **drawer**: 
- Navigation Drawer 구현
- Modal Drawer와 스크린 전환 예제

6. **effect**: 
- Compose의 Side Effect 처리
- LaunchedEffect, SideEffect 등 효과 관리

7. **ffmpeg**: 
- FFmpeg를 사용한 비디오/오디오 처리
- 미디어 파일 인코딩/디코딩 예제

8. **flexbox**: 
- FlexBox 레이아웃 구현
- 동적 크기 조절 및 반응형 레이아웃

9. **header**: 
- Sticky Header 구현
- 스크롤에 따른 헤더 상태 변경

10. **intent**: 
- 안드로이드 Intent 처리
- 앱 간 데이터 공유 및 딥링크 처리

11. **lazycolumn**: 
- LazyColumn 성능 최적화
- FlingBehavior 커스터마이징

12. **lottie**: 
- Lottie 애니메이션 구현
- 애니메이션 제어 및 상호작용

13. **navigation**: 
- Bottom Navigation 구현
- Navigation Component 활용

14. **pager**: 
- ViewPager 구현
- 페이지 전환 및 인디케이터

15. **powersave**: 
- 절전 모드 감지 및 대응
- 배터리 최적화 처리

16. **preview**: 
- Compose Preview 기능
- 다양한 Preview 옵션 활용

17. **recorder**: 
- 오디오/비디오 녹화 기능
- 미디어 레코딩 상태 관리

18. **refresh**: 
- Pull-to-Refresh 구현
- 새로고침 상태 및 애니메이션

19. **shimmer**: 
- 로딩 상태 Shimmer 효과
- 커스텀 Shimmer 애니메이션

20. **swipe**: 
- Swipe to Dismiss 구현
- 스와이프 제스처 처리

21. **text**: 
- 텍스트 스타일링
- 다양한 텍스트 표현 방식

22. **version**: 
- Android SDK 버전 대응
- 버전별 기능 처리

23. **webview**: 
- WebView 구현 및 설정
- JavaScript 인터페이스 처리

24. **workmanager**: 
- WorkManager 백그라운드 작업
- 작업 스케줄링 관리

25. **ktor**: 
- Ktor 클라이언트를 사용한 네트워크 통신
- 코루틴 기반의 비동기 처리

26. **mvi**: 
- MVI 아키텍처 패턴 구현
- 단방향 데이터 흐름과 상태 관리

27. **sse**: 
- Server-Sent Events 구현
- 실시간 데이터 스트리밍 처리

## 주의사항
- 일부 예제(예: Permission 관련)는 기본 설정이 필요할 수 있습니다
- Compose 버전 1.4.0-alpha04 이하에서는 키보드 관련 이슈가 있을 수 있습니다
- 실제 앱에서 필요한 기본 로직들은 그대로 활용 가능하도록 구현되어 있습니다

## 더 자세한 내용
각 예제에 대한 자세한 설명은 [Tistory Blog](https://heegs.tistory.com/category/Android/Jetpack)에서 확인하실 수 있습니다.
