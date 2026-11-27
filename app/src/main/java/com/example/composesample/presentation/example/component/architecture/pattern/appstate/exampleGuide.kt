package com.example.composesample.presentation.example.component.architecture.pattern.appstate

/**
 * AppState 예제 참고 자료 (#163)
 *
 * ## AppStateExampleUI · AppStateLab (androidx.appstate statestore · statestore-datastore · transform 1.0.0-alpha02)
 * - 공식 문서(AppState): https://developer.android.com/topic/libraries/architecture/appstate
 * - 릴리스 노트(묶음 · statestore · transform): https://developer.android.com/jetpack/androidx/releases/appstate
 *   https://developer.android.com/jetpack/androidx/releases/appstate-statestore
 *   https://developer.android.com/jetpack/androidx/releases/appstate-transform
 * - 착수 시나리오(구독 만료 시 재생 정책 — Flow flatMapLatest 판 vs transform when 분기 판, StateStore 는 자동 싱글턴이 아님 · 다른 프로세스와 공유 불가 ·
 *   동기 매핑뿐이면 derivedStateOf 로 충분): https://ardakazanci.com/posts/jetpack-appstate-explained/ (Arda Kazancı, Android Weekly #751)
 * 핵심 개념(실측: Pixel 8 API 36 에뮬레이터 debug + JVM 단위 테스트 AppStateLabTest — 두 곳 결과 같음):
 * - 버전: alpha03(2026-11-18)은 transform 이 compose runtime-saveable 1.14.0-alpha01 · savedstate 1.6.0-alpha05 를 요구해 넣는 순간 앱 전체가
 *   Compose runtime 묶음 9좌표 1.13.0 → 1.14.0-alpha01 · savedstate 5좌표 1.5.0 → 1.6.0-alpha06 · collection 1.6.0 → 1.7.0 으로 끌어올려졌다(해석 diff) →
 *   사용자 결정으로 alpha02. alpha02 의 추가 해석은 appstate 7좌표 + kotlin-reflect 2.3.20 뿐. alpha03 변경(노트): statestore-datastore 가 리플렉션을 버림
 *   (PersistToDataStore<T> 가 저장 이름 · KSerializer 를 줌, datastore-core-okio), transform 람다 안 rememberSaveable 용 SavedStateContainer, RunnableTransformScope.run
 * - 키: StateStoreKey 는 @Serializable open class 이고 equals 를 고치지 않는다 → 저장소(SnapshotStateMap) 키는 인스턴스 동일성. object 키는 다시 읽으면 3,
 *   같은 class 의 새 인스턴스로 읽으면 기본값 0 · keys 3개
 * - 자동 정리(autoClearKey): setState 가 '저장소에 그 키가 없을 때' 만 감시를 건다(Unconfined · SupervisorJob scope 의 listener 헤드리스 컴포지션).
 *   ① 먼저 getState 하면(기본값으로 키가 들어감) 감시가 안 걸려 사용자를 바꿔도 남는다 ② 비교 기준은 첫 setState 때의 트리거 값 — 손님(null)일 때 담은
 *   장바구니는 로그인(null → alice)에서 지워진다(5ms) ③ 같은 값으로 다시 쓰면 그대로이고, 정리되지 않은 감시는 Recomposer 하나로 계속 남는다
 *   (Recomposer.runningRecomposers +1 — 키마다)
 * - transform: 헤드리스 컴포지션(UnitApplier + HeadlessTransformClock — 요청하면 바로 프레임) · 기본 dispatcher Dispatchers.Default.
 *   when 분기 안 collectAsState 는 그 분기에 있을 때만 수집 — Free 로 바뀌면 수집자 1 → 0(Flow flatMapLatest 판과 같은 값 · 같은 수집자 수).
 *   읽지 않는 값(Free 인 채 화질)이 바뀌면 다시 돌지 않는다. 도착 시간은 두 판 모두 0~41ms 로 흔들려(3회) 우열 없음
 * - syncToDataStore(path, scope): ① 복원은 그 키가 저장소에 들어온 뒤(누가 getState) — 처음엔 기본값, 5ms 뒤 복원값 ② value != null 일 때만 써서
 *   로그아웃(null)이 파일에 안 남고 ③ 자동 정리로 빠진 키도 파일에서 안 지워져 다음 실행에 되살아남 ④ 되살아난 키는 getState 가 먼저 넣어 자동 정리
 *   감시 없음 ⑤ 복원 전에 쓴 값('light')을 복원이 덮음('dark') ⑥ 저장 이름 = 키 class qualifiedName(이름 · 패키지 변경 · R8 난독화에 약함 — 소스)
 *   ⑦ 같은 파일에 DataStore 를 둘 열 수 없어 실행마다 scope 를 닫고 기다린다. 파일은 {"<qualifiedName>": "<값 JSON>"} 하나
 * - alpha02 는 값 타입을 kotlin-reflect allSupertypes 로 찾는다 — 새 프로세스 첫 호출 91.6ms(에뮬), 리플렉션이 첫 저장에 섞이면 510ms · 빼면 44ms.
 *   release APK(축소 꺼짐) 30,836,977 → 31,805,233 B(+0.97MB) — appstate 7좌표 + kotlin-reflect. lint GradleDependency(alpha03 있음) 4건은 lint.xml 에서
 *   androidx.appstate 만 끈다(alpha02 고정 결정)
 * - 테스트: transform-testing 의 transformTest { TransformScope() · launchTransform · backgroundScope.launch(testDispatcher) { run() } · advance() }.
 *   JVM 에서는 android.os.Trace 를 mockkStatic 으로 막아야 한다 — 안 막으면 컴포지션이 'not mocked' 로 죽고 예외는 라이브러리 scope 의 stderr 로만 나가
 *   '안 지워짐 · 값 그대로' 로 보인다(transformTest 도 '' 그대로)
 * - #86 Molecule(architecture/pattern/molecule)과 같은 축 — UI 밖 컴포지션으로 상태를 만든다. Molecule 은 StateFlow 를 내고, transform 은 State 를 낸다
 */
