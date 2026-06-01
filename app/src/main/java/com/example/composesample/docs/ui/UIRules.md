# UI 시스템 가이드

## 📋 기본 규칙
- 모든 코드는 프로젝트 루트의 `CLAUDE.md`에 정의된 코딩 컨벤션을 따릅니다.
- 아래 규칙에 따라 UI 코드와 비즈니스 로직을 분리합니다.

### 잘못된 예시
```kotlin
@Composable
fun PlayerScreen() {
    Button(onClick = {
        // 비즈니스 로직
    }) {
        Text("Button")
    }
}
```

### 올바른 예시
기능과 관련된 ViewModel 클래스 파일을 동일한 폴더 안에 생성합니다.<br>
상위 기능의 주제가 정해지면, 더 세분화하지 않고 상위 ViewModel 클래스를 사용합니다.<br>
ViewModel 클래스 파일명은 기능명 + `ViewModel` 형태로 작성합니다.<br>
핵심 개념: 상위 기능 단위 구성.<br>
"Live" 기능을 예로 들어 보겠습니다:

Live 기능<br>
├── Streaming<br>
├── Settings<br>
├── Chat<br>
└── 그 외 Live 관련 기능<br>

규칙 설명:
이렇게 하세요 ✅

```kotlin
class LiveViewModel {
    // 하나의 ViewModel이 Live 관련 모든 기능을 처리
    fun handleStreaming() { ... }
    fun handleSettings() { ... }
    fun handleChat() { ... }
}
```

이렇게 하지 마세요 ❌

```kotlin
// 하위 기능별로 ViewModel을 분리하지 않습니다
class LiveStreamViewModel { ... }
class LiveSettingViewModel { ... }
class LiveChatViewModel { ... }
```

이 규칙이 존재하는 이유:
더 나은 코드 구성
ViewModel 파편화 방지
더 쉬운 상태 관리
더 명확한 의존성 주입
기능 단위에서 단일 책임 원칙 준수
간단한 판단 기준:
스스로에게 물어보세요: "이것이 더 큰 기능의 일부인가?"
그렇다면 → 상위 기능의 ViewModel을 사용
아니라면 → 새 기능을 위한 새 ViewModel 생성


ViewModel 클래스는 의존성 주입으로 사용하고 `../di/ViewModelModule.kt` 파일에 등록합니다.<br>
의존성 주입에는 Koin 프레임워크를 사용합니다.<br>

```kotlin
val viewModelModule: Module = module {
    ...
    viewModel {
        PlayerViewModel()
    }
}

class PlayerViewModel() : ViewModel() {
    ...
    fun fetchPlayList() {
        // 비즈니스 로직
    }
}

@Composable
fun PlayerScreen(
    playerViewModel: PlayerViewModel
) {
    Button(onClick = {
        playerViewModel.fetchPlayList()
    }) {
        Text("Button")
    }
}
```

## 비즈니스 로직과 UI 코드의 분리

클린 아키텍처를 유지하고 유지보수성을 높이려면 비즈니스 로직을 UI 코드와 분리하는 것이 매우 중요합니다. 다음은 몇 가지 예시와 가이드라인입니다:

### 분리 예시

**잘못된 구현**
```kotlin
@Composable
fun UserProfileScreen() {
    Button(onClick = {
        // UI에 비즈니스 로직을 직접 작성
        fetchUserProfile()
    }) {
        Text("Load Profile")
    }
}
```

**올바른 구현**
```kotlin
class UserProfileViewModel : ViewModel() {
    fun fetchUserProfile() {
        // 비즈니스 로직
    }
}

@Composable
fun UserProfileScreen(viewModel: UserProfileViewModel = viewModel()) {
    Button(onClick = {
        viewModel.fetchUserProfile()
    }) {
        Text("Load Profile")
    }
}
```

### 가이드라인
- **ViewModel 사용**: 비즈니스 로직과 상태 관리는 항상 ViewModel에서 처리합니다.
- **UI 컴포넌트**: UI 컴포넌트는 렌더링과 사용자 상호작용에만 집중합니다.
- **의존성 주입**: Koin 또는 다른 DI 프레임워크로 ViewModel을 Composable에 주입합니다.
- **상태 관리**: 필요한 경우 상태 호이스팅(state hoisting)을 사용해 UI 상태를 Composable 외부에서 관리합니다.

이 가이드라인을 따르면 코드가 모듈화되고, 테스트 가능하며, 유지보수가 쉬워집니다.

## 상태 관리 전략

- **상태 호이스팅**: 가장 가까운 공통 상위 요소로 상태를 끌어올려 Composable 간에 상태를 공유합니다.
- **ViewModel 상태**: 구성 변경(configuration change)에도 유지되어야 하는 UI 관련 데이터는 ViewModel로 관리합니다.

## 성능 최적화 팁

- **리컴포지션**: 비용이 큰 계산은 `remember`와 `derivedStateOf`를 사용해 리컴포지션을 최소화합니다.
- **Lazy 컴포넌트**: 목록에는 `LazyColumn`과 `LazyRow`를 사용해 스크롤 성능을 향상시킵니다.
