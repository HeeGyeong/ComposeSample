package com.example.composesample.presentation.example.component.architecture.pattern.remember

/**
 * Remember Patterns 참고 자료
 *
 * - rememberSaveable 공식 문서:
 *   https://developer.android.com/develop/ui/compose/state#restore-ui-state
 *
 * - rememberUpdatedState 공식 문서:
 *   https://developer.android.com/develop/ui/compose/side-effects#rememberupdatedstate
 *
 * - derivedStateOf 공식 문서:
 *   https://developer.android.com/develop/ui/compose/side-effects#derivedstateof
 *
 * 핵심 개념:
 * - rememberSaveable: remember와 달리 액티비티 재생성·프로세스 재시작 후에도 상태 유지 (Bundle 직렬화).
 *   이 앱의 예제 화면(BlogExampleActivity)은 configChanges 로 회전을 직접 처리해 회전으로는 재생성되지 않는다 — 다크 모드·언어 전환으로 확인
 * - rememberUpdatedState: LaunchedEffect 등 오래 실행되는 이펙트 안에서 항상 최신 람다/값을 참조
 * - derivedStateOf: 다른 State로부터 계산된 값을 메모이제이션. key 없이 remember { derivedStateOf { } } 형태로 사용
 *
 * ## rememberSerializable (2번 섹션, 2026-10-08 — runtime-saveable 1.12.1 · savedstate 1.5.0 · kotlinx-serialization 1.11.0)
 * - rememberSerializable 공식 레퍼런스: https://developer.android.com/reference/kotlin/androidx/compose/runtime/saveable/package-summary
 * - savedstate 릴리스 노트(SnapshotStateSetSerializer 등): https://developer.android.com/jetpack/androidx/releases/savedstate
 * - kotlinx-serialization 플러그인: https://kotlinlang.org/docs/serialization.html
 * 핵심 개념(sources jar 기준):
 * - `androidx.compose.runtime.saveable.rememberSerializable` — public, 실험 아님. 오버로드 4개: 값 / MutableState 각각 reified
 *   (`serializersModule.serializer<T>()`) 와 serializer 직접 전달(`serializer =` / `stateSerializer =`). 내부는
 *   serializableSaver + rememberSaveable — kotlinx-serialization 으로 SavedState(Bundle)에 인코딩하므로 Saver 를 쓰지 않는다
 * - 직접 만든 클래스는 @Serializable + 컴파일러 플러그인(org.jetbrains.kotlin.plugin.serialization = Kotlin 버전),
 *   기본 타입은 내장 serializer. 런타임 kotlinx-serialization-core 1.11.0 은 savedstate 1.5.0 전이와 같은 버전으로 명시 선언(해석 변화 0)
 * - `canBeSaved`(기본 Saver 가 받는가): @Serializable 데이터 클래스 false · `mutableStateSetOf()` true(Android 의 SnapshotStateSet 은 Parcelable)
 * - ⚠️ **Compose runtime 1.12.1 SnapshotStateSet.writeToParcel 버그** — `writeInt(size)` 뒤 `if (iterator.hasNext()) writeValue(next())`
 *   로 첫 원소만 쓴다(1.13.0-alpha03 에서 while 로 수정, SnapshotStateList 는 정상). 원소 2개 이상인 집합을 rememberSaveable 로
 *   저장하면 Bundle 이 실제로 직렬화될 때(프로세스 종료 후 복원) 어긋난다. 실측(SM-A725F, 원소 2개 → 홈 → am kill → 복귀):
 *   release 는 [빨강, null] 로 파랑 유실 + 뒤 항목들까지 Parcel "consumed X bytes, but Y expected" 경고, debug 는 BadParcelableException
 *   → HotSwan 이 삼켜 빈 화면. 원소 1개와 다크 모드 재생성(같은 프로세스 — Bundle 을 직렬화하지 않음)은 멀쩡했다.
 *   그래서 예제는 `rememberSerializable(serializer = SnapshotStateSetSerializer(String.serializer())) { mutableStateSetOf() }` 로만 저장한다
 * - ⚠️ **LazyColumn 항목의 saveable 상태는 재생성 순간 보이는 항목만 저장된다** — foundation LazySaveableStateHolder 가
 *   performSave 때 컴포즈되지 않은 항목의 상태를 지운다(KDoc: TransactionTooLargeException 대비). 실측: 1번 섹션 카운터 2 를 화면 밖에
 *   둔 채 재생성 → 0, 보이는 채 재생성 → 2. 스크롤로 벗어났다 돌아오는 것(같은 컴포지션)은 유지된다
 * - 2번 섹션 실측(SM-A725F, debug·release 동일): 필터를 compose · 2000 · PRICE_LOW · tags [new, sale] 로 바꾸고 집합 [빨강, 파랑] · Int 3 →
 *   다크 모드 재생성과 프로세스 종료 후 복귀 모두 B(rememberSerializable) 전 필드 · 집합 · Int 복원, A(손으로 쓴 Saver — tags 누락)는
 *   tags 만 "없음". Parcel 경고 0, 크래시 0. HotSwan SKIPPED 변화 0(생성된 SearchFilter$$serializer 는 HotSwan 아래서도 정상)
 */
