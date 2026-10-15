package com.example.composesample.presentation.example.component.architecture.pattern.retain

/**
 * Retain API Example 참고 자료
 *
 * - 상태 수명(State lifespans) 공식 가이드: https://developer.android.com/develop/ui/compose/state-lifespans
 * - API 문서(androidx.compose.runtime.retain): https://developer.android.com/reference/kotlin/androidx/compose/runtime/retain/package-summary
 * - Compose runtime 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/compose-runtime
 * - 공식 샘플(RetainSamples.kt · RetainAndSaveSample.kt): https://github.com/androidx/androidx/tree/androidx-main/compose/runtime/runtime-retain/samples/src/main/kotlin/androidx/compose/runtime/retain/samples
 *
 * 사용하는 것 (runtime-retain 1.12.1 — Compose BOM 2026.09.00 의 전이 의존성, 실험 opt-in 없음 · sources jar 확인)
 * - `retain { }` / `retain(keys) { }` — 키는 "호출 위치(currentCompositeKeyHashCode) + 타입 해시 + keys"
 * - `RetainObserver` — onRetained · onEnteredComposition · onExitedComposition · onRetired · onUnused
 * - `RetainedEffect(key) { …; onRetire { … } }` — 키 없는 오버로드는 DeprecationLevel.ERROR
 * - `retainManagedRetainedValuesStore()` + `LocalRetainedValuesStoreProvider(store) { }` — 접히는 콘텐츠의 보존 구간
 * - `retainRetainedValuesStoreRegistry()` + `registry.LocalRetainedValuesStoreProvider(key) { }` / `clearChild(key)`
 * - `SubcomposeLayoutState.createPausedPrecomposition` — 적용되지 않은 컴포지션을 만들어 onUnused 를 재현 (ui 1.12.1 공개 API)
 *
 * 안드로이드 기본 저장소 (ui-android 1.12.1 소스)
 * - `LifecycleRetainedValuesStoreOwner` 가 ViewModel 이다 → 재생성 생존 범위가 ViewModel 과 같고, ViewModelStore 가 정리되면 dispose
 * - AndroidComposeView 가 onStop 에서 "나간 값 보존"을 시작하고, onResume 뒤 프레임 끝에 멈추며 소비되지 않은 값을 onRetired
 *   → RESUMED 상태에서 컴포지션을 떠난 retain 값(if 로 숨김·탭 전환·예제 닫기)은 그 자리에서 폐기 — remember 와 같다
 * - 직렬화하지 않으므로 프로세스 종료 뒤에는 남지 않는다 — 그 경우는 rememberSaveable / SavedStateHandle
 *
 * 실측 (SM-A725F, debug(HotSwan)·release 동일, 2026-10-15)
 * - recreate()·다크 모드: remember 새 객체·0 · rememberSaveable 값 복원 · retain/ViewModel 같은 객체·값,
 *   진행 중 2초 작업은 retain/ViewModel 에서 이어져 완료되고 remember 는 onForgotten 에서 취소
 * - 헤더 ← 로 예제를 닫고 다시 열기: retain 은 닫을 때 onExitedComposition → onRetired, 다시 열면 새 객체 /
 *   ViewModel 은 같은 객체·값 (예제 화면의 ViewModelStoreOwner 는 BlogExampleActivity)
 * - 시스템 뒤로가기(BlogExampleActivity 종료): onExitedComposition → onForgotten(remember) → onRetired → onCleared
 * - 프로세스 종료(am kill): rememberSaveable 값만 복원, 나머지는 #1·0
 * - retain 으로 보관한 객체가 RememberObserver 도 구현해도 onRemembered/onForgotten 은 오지 않는다(KDoc 과 일치)
 * - if 로 숨김(기본 저장소): onExitedComposition → onRetired, 다시 보이면 onRetained → onEnteredComposition(새 객체)
 * - ManagedRetainedValuesStore 안에서 숨김: onExitedComposition 만 · 다시 보이면 같은 객체 onEnteredComposition ·
 *   숨긴 채 recreate() 해도 같은 객체 · 숨긴 채 disableRetainingExitedValues() → onRetired
 * - retain(key) 키 변경: 옛 값 onExitedComposition → onRetired, 새 값 onRetained → onEnteredComposition
 * - 미리 컴포즈 후 cancel(): onUnused 하나만(onRetained 없음) / apply() → dispose(): 네 콜백이 순서대로
 * - 재생성: DisposableEffect 는 onDispose → 시작을 반복, RetainedEffect 는 아무것도 안 함 / 탭을 떠나면 둘 다 끝
 * - Registry + key(): 탭을 오가도 같은 객체(onExited/onEntered 만), 숨겨진 탭도 recreate() 를 넘음,
 *   clearChild("검색") → 보존 중이던 검색 객체 즉시 onRetired
 * - 기본 저장소 + key(): 탭을 바꿀 때마다 onRetired + 새 객체 — remember 와 같다
 * - Registry, key() 없음: 어느 탭에서든 같은 객체가 돌아오고 탭을 바꿔도 콜백 0 — 같은 호출 위치에서 저장소만 바뀌면
 *   retain 의 remember(key) 가 그대로 맞아 기존 홀더를 새 저장소로 옮길 뿐이다(readoptUnder)
 *
 * 주의사항
 * - Context·View 를 붙잡는 객체를 retain 하지 않는다 — runtime-retain 1.12.1 lint.jar 의 RetainLeaksContext·
 *   RetainRememberObserver·RetainUnitType·RetainingDoNotRetainType 검사, 막을 타입엔 @DoNotRetain(runtime-annotation)
 * - Closeable 을 구현해도 자동으로 close() 하지 않는다 — 정리는 RetainObserver.onRetired/onUnused 에서
 * - 저장소·레지스트리를 쓰면 빠진 키를 clearChild 로 비우지 않는 한 레지스트리가 폐기될 때까지 남는다(KDoc)
 * - navigation3 1.2.0(프로젝트)·1.3.0-alpha02 aar 에는 retain 용 NavEntryDecorator 가 없다(2026-10-15 확인)
 * - 이 앱의 예제 화면(BlogExampleActivity)은 configChanges 로 회전을 직접 처리해 회전으로는 재생성되지 않는다
 *   — 재생성 확인은 recreate()·다크 모드로
 * - 저장할 값과 다시 만들기 비싼 값을 함께 다룰 때는 rememberSaveable/rememberSerializable + retain 으로 나누고
 *   remember(saved, retained) 로 묶는다(공식 rememberAndRetainSample)
 */
