package com.example.composesample.presentation.example.component.architecture.state

/**
 * Compose State / Snapshot System 참고 자료
 *
 * --- Compose Snapshot System ---
 * - 공식 문서: https://developer.android.com/develop/ui/compose/state
 * - Snapshot: Compose의 상태 격리 메커니즘 (git처럼 State를 복사해 독립적으로 수정)
 * - withMutableSnapshot { }: 여러 State를 원자적으로 변경 (중간 리컴포지션 발생 없음)
 * - derivedStateOf { }: 의존 State가 변경될 때만 재계산 (과도한 리컴포지션 방지)
 * - Snapshot.takeSnapshot(): Composition 외부(Worker Thread 등)에서 상태를 안전하게 읽는 법
 *
 * Snapshot 계층 구조:
 * - GlobalSnapshot: 전체 앱의 최상위 Snapshot, 리컴포지션 스케줄러가 여기서 변경을 감지
 * - MutableSnapshot: apply() 호출 시 부모 Snapshot에 변경사항을 병합
 * - 충돌(conflict): 같은 State를 두 Snapshot이 동시에 수정 시 mergePolicy로 해결
 *
 * --- SnapshotFlow ---
 * - snapshotFlow { state.value }: Snapshot 읽기를 Flow로 변환
 * - distinctUntilChanged()가 기본 내장되어 동일 값 중복 방출 없음
 * - LaunchedEffect 안에서 collect 하여 Side Effect 처리에 활용
 * - 공식 문서: https://developer.android.com/develop/ui/compose/side-effects#snapshotFlow
 *
 * 핵심 개념:
 * - State<T>는 내부적으로 StateStateRecord를 통해 Snapshot별 값을 관리
 * - Composition은 매 프레임 GlobalSnapshot을 통해 변경된 State를 감지하고 리컴포지션을 예약
 * - derivedStateOf는 내부적으로 DerivedSnapshotState로 구현되며, 읽기 시점에 의존성을 등록
 *
 * --- Per-Item ViewModels in Compose ---
 * - 참고: https://saurabharora.dev/posts/per-item-viewmodels-in-compose/
 * - 핵심: LazyColumn 의 각 아이템에 독립 ViewModel 스코프를 부여
 * - 패턴: CompositionLocalProvider(LocalViewModelStoreOwner provides itemOwner) 로 store 교체
 *         → viewModel() 호출은 그대로 두고 store 출처만 갈아끼움
 * - 메모리 정리: DisposableEffect onDispose 에서 store.clear() 로 ViewModel.onCleared() 호출
 *
 * --- (보강 #121) lifecycle 2.11 공식 per-key API ---
 * - 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/lifecycle (2.11.0-alpha02 도입, beta01 에서 키 유무 오버로드 분리)
 * - `rememberViewModelStoreProvider(parent = LocalViewModelStoreOwner.current)` + `rememberViewModelStoreOwner(key, provider)` →
 *   `CompositionLocalProvider(LocalViewModelStoreOwner provides owner) { viewModel { … } }`.
 *   `ViewModelStoreProvider` 는 lifecycle-viewmodel-savedstate, 두 remember 함수는 lifecycle-viewmodel-compose 에 있다
 * - 동작(소스 + 실측): Provider 는 부모 Store 안에 상태 보관용 ViewModel 을 두고(키 없으면 호출 위치의 composite key hash 로),
 *   키별 Store 를 그 안에 모은다 → **구성 변경에서 살아남는다**. owner 는 토큰만 잡았다 놓고 clearKey 는 부르지 않는다 →
 *   **아이템을 데이터에서 지워도 자동으로 비워지지 않는다**(clearKey 를 직접). Provider 가 컴포지션을 떠날 때 부모 수명주기가
 *   CREATED 이상이면 clearAllKeys(), 재생성 중(DESTROYED)이면 건너뛴다
 * - 실측(Galaxy A72, 실제 앱 + 계측 프로브):
 *   · 액티비티 재생성(다크 모드 전환 2회): 수작업 전부 새 인스턴스 / 공식 같은 인스턴스·카운트 유지
 *   · 아이템 삭제: 수작업 즉시 onCleared / 공식은 clearKey 없으면 남고(화면을 떠날 때 정리), 있으면 즉시 정리
 *   · 접기(컴포지션 이탈)·스크롤로 섹션이 화면 밖에 나감: 수작업 비워짐 → 새 인스턴스 / 공식 유지
 *   · 화면 이탈: 공식도 clearAllKeys 로 전부 정리(지운 채 남아 있던 것 포함)
 * - 함정(실측):
 *   · 수작업 구현의 forEach 에 key() 가 없으면 중간 아이템을 지울 때 그 뒤 아이템들의 store 까지 비워진다(기존 구현 버그 → key() 로 수정)
 *   · Provider 를 스크롤로 사라지는 lazy 항목 안에 두면 그 항목이 빠질 때 clearAllKeys 로 전부 비워진다 → LazyColumn·Pager 밖에 둔다
 *   · 계측 테스트에서 재생성 후 setContent 를 늦게 부르면 저장 상태 복원이 안 돼 composite key hash 가 바뀌고 키 없는 Provider 가
 *     이전 상태를 못 찾는다(rememberSaveable 도 초기화) — 실제 앱처럼 onCreate 안에서 setContent 해야 정상 측정
 * - 이 비교를 화면에서 하려고 BlogExampleActivity 의 열린 예제 상태를 rememberSaveable 로 바꿨다(EX-CONFIG-RESET-01, `25235a1e`) —
 *   그 전에는 구성 변경 때 예제가 닫히고 목록으로 돌아갔다
 *
 * --- derivedStateOf 의 숨은 비용 (#176) ---
 * - 원문: Jaewoong Eum, "Recalculated, not recomposed: what does one tap cost in a Compose paywall?"(RevenueCat, Android Weekly #753)
 *   https://www.revenuecat.com/blog/engineering/android-paywall-improve
 * - 공식 문서(derivedStateOf): https://developer.android.com/develop/ui/compose/side-effects#derivedstateof
 * - 소스(androidx-main): https://github.com/androidx/androidx/blob/androidx-main/compose/runtime/runtime/src/commonMain/kotlin/androidx/compose/runtime/DerivedState.kt
 *   · https://github.com/androidx/androidx/blob/androidx-main/compose/runtime/runtime/src/commonMain/kotlin/androidx/compose/runtime/RecomposeScopeImpl.kt
 *   — 아래 규칙은 프로젝트가 쓰는 runtime-android 1.12.1 의 sources jar 로 확인했다(줄 번호는 1.12.1 기준)
 * - 규칙(1.12.1 소스):
 *   · 의존성 = 계산이 실제로 읽은 상태. DerivedSnapshotState 가 계산을 Snapshot.observe 읽기 관찰자 아래에서 돌려 newDependencies 에 모은다
 *     → 람다로 넘기고 부르지 않으면 그 람다가 읽을 상태는 의존성에 없다
 *   · 리컴포지션 직전 RecomposeScopeImpl.checkDerivedStateChanged: `policy ?: structuralEqualityPolicy()` 로
 *     currentRecord.currentValue(무효면 그 자리에서 재계산)와 지난 값을 비교 — 건너뛰려면 계산해야 한다
 *   · 정책 없음: 재계산 결과가 같아도 새 레코드(DerivedState.kt 246~259행 — `policy?.equivalent(…) == true` 일 때만 기존 레코드 재사용)
 *     → 위층 derivedStateOf 의 readableHash(의존 레코드 identityHashCode · snapshotId)가 바뀌어 위층도 재계산
 *   · 캐시는 인스턴스마다(validSnapshotId · writeCount, 바뀌었으면 resultHash 대조) — 한 인스턴스를 여러 스코프가 읽으면 탭당 1 회
 * - 화면 모형: 플랜 카드 2 · 연간 안내 1(선택 의존) + 선택과 무관한 텍스트 N. 칸마다 기반 1(오버라이드 해석) + 속성 4(visible · 강조 · 굵기 · fontFamily).
 *   킬 스위치 3 — 조건 입력 미리 읽기 · 기반 structuralEqualityPolicy · 폰트 equals(AndroidFont 하위 클래스, 로더 호출을 셈).
 *   예상식 expectedTapCost: 해석 = 3 + (미리 읽기면 N) · 속성 = 4 × (3 + 정책 없을 때 미리 읽은 N) · 리컴포지션 = 3 + (equals 없음이면 속성을 다시 계산한 N)
 *   · 폰트 로드 = equals 없음이면 리컴포지션 수, 있으면 0
 * - 실측(JVM 단위 테스트 — 실제 ControlledComposition 을 Recomposer 와 같은 순서로 한 단계씩): 8 구성 × 두 방향이 모두 예상식과 같다(폰트 로드는
 *   레이아웃이라 JVM 에서 0). 한 인스턴스를 5 스코프가 읽으면 1 회 · 스코프마다 인스턴스면 5 회 · 결과가 같으면 리컴포지션 0.
 *   정책 없는 기반 위 6 층: 위층 6 회 재계산 / structuralEqualityPolicy: 0 회
 * - 실측(기기 — Pixel 4 API 30 에뮬레이터 debug · release, Pixel Tablet API 35 에뮬레이터 debug): 칸 20 · 100 · 300 × 킬 스위치 8 구성 ×
 *   두 방향 = 실행마다 48 탭, 세 실행 144 탭이 모두 예상식과 같고 세 실행의 결과가 한 줄도 다르지 않다. 칸 300 기준:
 *   · 고친 코드(람다 · 정책 없음 · equals): 해석 3 · 속성 12 · 리컴포지션 3 · 폰트 로드 0
 *   · 미리 읽기만 되살림: 303 · 1,212 · 3 · 0 — 리컴포지션은 그대로 3 인데 계산은 칸 수에 비례(원문 '308 해석, 리컴포지션 0' 의 모양)
 *   · 미리 읽기 + 기반 structuralEqualityPolicy: 303 · 12 · 3 · 0 — 위층 재계산만 사라진다
 *   · 미리 읽기 + equals 없음(원문의 두 수정을 모두 끈 상태): 303 · 1,212 · 303 · 303
 *   · 람다 + equals 없음: 3 · 12 · 3 · 3 — 고친 뒤에는 equals 결함이 탭 비용에 거의 안 보인다(원문 193 ms vs 184 ms 와 같은 결론)
 * - 원문 주장(이 앱에서 재지 않음): 300 텍스트 페이월 탭 700 ms → 184 ms · 오버라이드 해석 308 → 0 · 텍스트 리컴포지션 0(전부 건너뜀),
 *   89 컴포넌트 템플릿 해석 99 → 22, 폰트 equals(#4245)·람다(#4383) 둘 다 끄면 833 ms · 텍스트 리컴포지션 300 · 폰트 로드 300.
 *   시간은 Android 15 에뮬레이터 debug 빌드 · 중앙값 — 같은 계열 빌드가 Galaxy S23 190 ms, Android 14 에뮬레이터 112 ms 로 기기 비교는 의미 없다.
 *   waitForIdle 은 프레임 단위라 해상도 16.7 ms, 바닥 두 프레임(약 33 ms)
 * - 함정:
 *   · JVM 단위 테스트에서 컴포지션을 돌리려면 android.os.Trace 를 목으로 바꾼다(runtime-android 가 beginSection 을 부르고 android.jar 에는 구현이 없다)
 *   · recompose 는 컴포지션의 읽기 · 쓰기 관찰자를 건 스냅숏 안에서 — 관찰자 없이 돌리면 다시 그린 스코프가 구독을 잃어 다음 변화를 놓친다
 *   · recompose() 가 false 를 돌려주면 applyChanges 를 부르지 않는다 — 부르면 "calling recordModificationsOf and applyChanges concurrently" 오류
 *   · 카운터를 스냅숏 상태로 두면 세는 일이 쓰기가 되어 리컴포지션을 만든다 → 평범한 필드 + 리컴포지션은 SideEffect 로
 *   · equals 없는 FontFamily 를 두 텍스트가 함께 쓰면 탭당 로드가 리컴포지션 수보다 많을 수 있다: FontFamilyResolver 의 TypefaceRequestCache ·
 *     AsyncTypefaceCache 는 LruCache(16)(ui-text 1.12.1 바이트코드)이고, 다시 측정할 노드는 DepthSortedSet 이 깊이 → LayoutNode.hashCode 순으로
 *     꺼내 같은 깊이의 텍스트 순서가 실행마다 다르다 → 두 텍스트 사이에 다른 패밀리가 16 개 넘게 해석되면 두 번째도 미스.
 *     첫 구현(플랜 카드 이름 · 가격이 한 패밀리)에서 한 실행은 칸 20 · 100 · 300 탭당 +1 · +1 · +2, 같은 구성만 연 실행은 +0 →
 *     로더 스택(PROBE)으로 같은 인스턴스가 측정 단계에서 두 번 로드된 것을 확인하고 가격 텍스트를 기본 폰트로 바꿨다
 *   · 기존 Compose Snapshot 예제의 'counter > 5 일 때만 재계산' 문구는 틀렸다(counter 가 바뀔 때마다 재계산) → 이번에 정정
 */
