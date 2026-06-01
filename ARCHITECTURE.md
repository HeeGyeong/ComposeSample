# ARCHITECTURE.md — 아키텍처 의사결정 기록

ComposeSample의 아키텍처 구조와 **"왜 이렇게 설계했는가"**를 기록합니다.
규칙(어떻게 작성하는가)은 `CLAUDE.md`, API 나열은 모듈별 README를 참고하세요.

---

## 모듈 구조

```
ComposeSample
├── app         # UI 레이어 (Compose, ViewModel, DI 모듈, 예제)
├── data        # 데이터 레이어 (Repository 구현, API, Room)
├── domain      # 도메인 레이어 (Repository 인터페이스, UseCase, Model) — 순수 Kotlin(JVM)
├── Core        # Navigation 인터페이스
└── Coordinator # Coordinator 패턴 구현
```

## 레이어 의존 규칙

| 레이어 | 의존 허용 | 금지 |
|--------|-----------|------|
| domain | 없음 (순수 Kotlin) | Android 프레임워크, Retrofit, Gson |
| data | domain | presentation |
| app(presentation) | domain, data | - |

- 의존성 주입은 Koin으로 런타임에 연결하며, 컴파일 의존은 위 표를 벗어나지 않습니다.
- `Coordinator`/`Core`는 화면 전환 추상화를 위한 보조 모듈입니다.

---

## 주요 아키텍처 의사결정 (ARCH 리팩토링)

### ARCH-01 / ARCH-02 — domain에서 Retrofit/Gson 제거
- **결정**: domain의 Repository 인터페이스가 `retrofit2.Call`을 반환하던 것을 `suspend fun`이 도메인 타입을 반환하도록 전환. `MovieResponse`/`PostData`는 프로퍼티명을 JSON 키와 맞춰 `@SerializedName`(Gson) 의존을 제거.
- **이유**: 프레임워크 타입이 도메인 계약에 누출되면 domain이 네트워크 구현에 묶여 순수성·테스트 용이성이 깨짐.

### ARCH-03 — presentation → data 직접 참조 제거 (DataCache)
- **결정**: `DataCacheViewModel`이 `RoomSingleton`을 직접 참조하던 것을 `UserCacheRepository`(인터페이스) + `UserCacheRepositoryImpl`(ExampleDao 위임) 추상화로 분리하고 `RepositoryModule`에 등록.
- **트레이드오프**: `UserData`가 Room `@Entity`라 순수 Kotlin domain에 둘 수 없어, **추상화 인터페이스를 domain이 아닌 data 레이어에 배치**했습니다(도메인 모델 매핑은 범위에서 제외). 레이어 규칙(presentation은 data 직접참조 대신 추상화 의존)은 준수합니다.

### ARCH-04 — domain 모듈을 순수 Kotlin(JVM)로 전환
- **결정**: domain을 `com.android.library` + `kotlin.android`에서 `kotlin("jvm")`으로 전환. config.gradle/core-dependencies.gradle 적용을 제거해 Room/Retrofit/Ktor 전이 의존을 차단.
- **이유**: domain은 외부 의존이 0(suspend/List만 사용)임을 확인 후, 빌드 시스템 수준에서 프레임워크 의존이 **물리적으로 불가능**하도록 강제. 순수 jvm 미지원인 androidTest 템플릿은 제거하고 `testImplementation(junit)`만 유지.

### ARCH-05 — UI 전용 모델을 domain→app으로 이동
- **결정**: `ExampleObject`/`ExampleMoveType`을 `domain.model`에서 app `presentation.example.model`로 이동(참조 8개 파일 import 갱신).
- **이유**: 예제 목록 항목·이동 타입은 비즈니스 모델이 아니라 **UI/앱 전용 개념**. domain에는 순수 비즈니스 모델만 남깁니다.

### 보조 결정
- **DI-04**: `ApiExampleViewModel`의 Koin 등록을 positional이 아닌 `named()` 인자(application/postApiInterface/ktorClient)로 명시 → 인자 순서 오인 리스크 제거.
- **CONV-04**: UseCase 호출 규약을 `execute()`에서 `operator fun invoke()`로 통일.

---

## 의도적으로 남겨둔 예외 (교육용)

리팩토링 대상에서 **일부러 제외**한 코드가 있습니다. "왜 안 고쳤는가"를 명확히 합니다.

- **`ApiExampleViewModel` ↔ `ApiExampleUseCaseViewModel`**: 직접 API 호출 vs UseCase 경유를 비교하는 **대조쌍 예제**(KDoc 명시). 한쪽만 정리하면 비교 의도가 사라지므로 유지.
- **`legacy/` 영역**(MovieViewModel/SubActivityViewModel/SubUI 등): 과거 패턴을 보여주는 학습 자료로 의도적 보존. `legacy` 패키지에 격리.
- **Material 2 시연 예제**(BottomSheet M2, SwipeToDismiss M2 등): M3 대체 예제와 **나란히 비교**하기 위한 의도적 잔존(`@Suppress("DEPRECATION")` + 주석으로 명시).

---

## 알려진 제약 / 보류

버전 업그레이드 보류, 분할 예정 항목 등은 [`docs/KnownLimitations.md`](app/src/main/java/com/example/composesample/docs/KnownLimitations.md)를 참고하세요.
