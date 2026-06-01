# 알려진 제약 / 보류 항목

코드 TODO 주석과 작업 메모리에만 흩어져 있던 **의도적 보류·버전 제약·분할 예정** 항목을 사람이 읽을 수 있게 모았습니다. 각 항목은 "당장 고치지 않은 이유"를 함께 기록합니다.

---

## 버전 업그레이드 보류

### DI-01 — Koin 3.5.x 업그레이드 보류
- **현재**: Koin 3.2.2 고정 (`libs.versions.toml`)
- **이유**: 3.5.x로 올리면 ① `getViewModel(owner=)` API 변경, ② `koin-androidx-compose`의 navigation-compose 전이 의존성 제거로 다수 호출부 수정이 필요. 영향 범위가 커 별도 PR로 분리 예정.
- 관련: `libs.versions.toml`의 `TODO(DI-01)` 주석.

### biometric alpha05 고정
- **현재**: `androidx.biometric:biometric(-compose):1.4.0-alpha05`
- **이유**: alpha06+는 compileSdk 36을 요구하나 프로젝트는 compileSdk 35. 35 호환 마지막 버전인 alpha05를 채택. (alpha06+는 `biometricRequest` API가 vararg + CustomOption으로 바뀌어 호출부 수정도 필요)

### ffmpeg 주석 처리
- **현재**: `system/media/ffmpeg`의 인코딩/디코딩 예제가 전체 주석 처리됨
- **이유**: ffmpeg-kit 라이브러리의 버전 호환성 문제. 동작 불가 코드를 삭제하지 않고 주석으로 보존(프로젝트 정책: 호환 안 되는 예제는 제거 대신 주석).

---

## 구조 분할 예정 (별도 PR)

### ARCH-02 — core-dependencies 모듈별 분리
- **현재**: `config.gradle`/`core-dependencies.gradle`을 공통 적용. domain은 ARCH-04로 분리 완료했으나 `core`/`coordinator`는 여전히 공통 적용 중.
- **이유**: 모듈별 의존성 최소화는 바람직하나 적용 범위가 넓어 별도 PR로 진행 예정.

### UTIL-01 — ConstValue / ExampleRouter 분할
- **현재**: `ConstValue.kt`(상수 150+)와 `ExampleRouter.kt`(import 150+줄, when 분기 130+)가 비대.
- **이유**: 예제 추가마다 두 파일이 동시 수정되어 머지 충돌 포인트가 됨. 카테고리별 분할이 필요하나 영향이 커 별도 PR 예정.
- 관련: `ConstValue.kt`의 `TODO(UTIL-01)` 주석.

---

## 네이밍 / 컨벤션 충돌 (사용자 결정 대기)

### CONV-05 — Room Entity 네이밍 (`UserData`)
- **현재**: `data.db.UserData`가 Room `@Entity`.
- **충돌**: 일반 Android 관례는 `*Entity`/`*DTO`지만, `CLAUDE.md` 파일 네이밍 규칙 표에 `UserData.kt`가 Data 클래스 정식 예시로 명시되어 있어 프로젝트 컨벤션과 충돌. 자동 변경하지 않고 결정을 보류.

---

## Material 2 잔존 (M3 마이그레이션 진행 중)

### M3-BULK 3차 — Material2 잔존 32파일
- **현재**: 1·2차 일괄 전환(약 80파일) 완료. 잔여 32파일은 API 모델 변경이 필요해 도메인별 재작성 대상.
- **분류**: `BottomSheet*`/`ModalBottomSheet*`(상태 모델 변경), `Drawer`(→`ModalNavigationDrawer`), `TopAppBar`(Slot/scrollBehavior 차이), `Scaffold`, `Snackbar`, `BottomNavigation`(→`NavigationBar`), `ScrollableTabRow` 등.
- **이유**: 단순 import 치환으로 안 되고 API 호출부 재작성이 필요해 도메인별 PR 분할 권장. 공통 헤더 `MainUIComponent.kt`는 M3-05로 이미 전환 완료(제외).
- **의도적 잔존**: 일부 M2 예제(BottomSheet/SwipeToDismiss)는 M3 대체 예제와 비교용으로 보존 — [`ARCHITECTURE.md`](../../../../../../ARCHITECTURE.md) "교육용 예외" 참고.

---

## 문서

### 모듈 README 언어
- **현재**: `data/.../DataREADME.md`, `domain/.../DomainREADME.md`는 영어. `docs/` 규칙 3종(UI/DI/Data)은 한국어화 완료(DOC-09).
- **미결**: 모듈 README의 한국어화 여부 미정. 코드 주석은 한국어 규칙이나 모듈 README는 별개로 볼 여지가 있어 정책 결정 대기.
- **경미**: `DataREADME.md`에 ARCH-03의 `UserCacheRepository` 추상화 계층 미반영.

---

> 이 목록은 진행 상황에 따라 갱신됩니다. 항목이 해소되면 해당 섹션을 제거하거나 "완료"로 표기하세요.
