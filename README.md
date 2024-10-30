# ComposeSample

## 프로젝트 소개
Jetpack Compose를 공부하고 실무에 적용하면서 발생했던 이슈와 자주 사용되는 다양한 기능들의 샘플을 모아둔 프로젝트입니다.

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

ComposeSample

├── app

├── data

└── domain


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

## 주의사항
- 일부 예제(예: Permission 관련)는 기본 설정이 필요할 수 있습니다
- Compose 버전 1.4.0-alpha04 이하에서는 키보드 관련 이슈가 있을 수 있습니다
- 실제 앱에서 필요한 기본 로직들은 그대로 활용 가능하도록 구현되어 있습니다

## 더 자세한 내용
각 예제에 대한 자세한 설명은 [Tistory Blog](https://heegs.tistory.com/category/Android/Jetpack)에서 확인하실 수 있습니다.