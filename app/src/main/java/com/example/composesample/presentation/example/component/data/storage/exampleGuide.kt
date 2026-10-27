package com.example.composesample.presentation.example.component.data.storage

/**
 * DataStore 예제 참고 자료
 *
 * ## DataStoreExampleUI (DataStore 1.3.0 ↔ SharedPreferences)
 * - 공식 문서: https://developer.android.com/topic/libraries/architecture/datastore
 * - 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/datastore
 * - SharedPreferences.Editor apply/commit: https://developer.android.com/reference/android/content/SharedPreferences.Editor
 * - MultiProcessDataStoreFactory: https://developer.android.com/reference/kotlin/androidx/datastore/core/MultiProcessDataStoreFactory
 * - AOSP QueuedWork(Android 13): https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android13-release/core/java/android/app/QueuedWork.java
 *
 * ### 핵심 개념 (1.3.0 소스 jar 기준)
 * - 쓰기는 `updateData(transform)` 하나 — transform 들은 쓰기 actor 에서 차례로 돌고, 앞 결과가 다음 입력이다.
 *   그래서 동시에 +1 을 해도 갱신을 잃지 않는다. 대신 쓰기마다 임시 파일 → fsync → 이름 바꾸기라 여러 값은 한 transform 으로 묶는다
 * - 손상: Serializer.readFrom 이 던진 CorruptionException 을 FileStorage 가 `CorruptionException("Corruption in file $file", 원인)` 으로
 *   감싸 올린다. 기본 처리기는 `ReThrowCorruptionHandler`(파일은 손대지 않음) — `ReplaceFileCorruptionHandler { 기본값 }` 은
 *   쓰기 잠금 안에서 파일이 아직 깨져 있는지 다시 읽어 본 뒤 덮어쓴다
 * - 파일당 인스턴스 하나: `FileStorage.createConnection()` 이 프로세스 전역 activeFiles 집합을 검사해
 *   "There are multiple DataStores active for the same file" IllegalStateException. 생성 시점이 아니라 첫 읽기·쓰기 때 검사하고,
 *   scope 의 Job 이 끝나야(쓰기 actor onComplete → connection.close) 집합에서 빠진다
 * - `DataStore.Builder(storage, context)` — context 에 Job 이 있어야 한다(`DataStoreImpl` init 의 check). setCorruptionHandler·addMigrations
 * - `SharedPreferencesMigration(context, name, keysToMigrate)` — 첫 읽기 전에 돈다. 전체 이관이면 SP 를 비우고 비면 파일까지 지운다
 *   (`deleteSharedPreferences`). 일부 키면 옮긴 키만 SP 에서 지운다. 이미 DataStore 에 있는 키는 덮지 않는다
 * - SharedPreferences: 각 호출은 스레드 안전하지만 getX → putX 조합은 원자적이지 않다. apply 는 메모리를 즉시 바꾸고 디스크는 QueuedWork 로 미룬다
 *
 * ### 프로세스 경계 (7·8번 카드, #160)
 * - FileStorage 기본 coordinator(`SingleProcessCoordinator`): 잠금은 프로세스 안 Mutex, 버전은 프로세스 안 AtomicInt, updateNotifications = 빈 Flow.
 *   쓰기(transformAndWrite)는 잠금 안에서 파일을 다시 읽고 쓰지만 다른 프로세스와는 엇갈린다. 읽기는 버전이 그대로면 캐시를 돌려준다
 *   → 다른 프로세스의 쓰기를 영영 보지 못한다(자기 쓰기 전까지). 쓰기는 `파일.tmp` 에 쓴 뒤 이름 바꾸기라 두 프로세스가 같은 .tmp 를 두고
 *   엇갈리면 "Unable to rename … .tmp … multiple instances of DataStore" IOException(원인 NoSuchFileException)
 * - `MultiProcessDataStoreFactory.create(serializer, …, produceFile)`: coordinator 가 `MultiProcessCoordinator` —
 *   `파일.lock` 에 FileChannel 배타 잠금(+ 프로세스 안 Mutex), `파일.version` 공유 카운터(native mmap, libdatastore_shared_counter),
 *   updateNotifications 는 파일 감시(MulticastFileObserver) → 다른 프로세스의 쓰기가 data 로 온다. 파일당 하나 규칙은 프로세스마다 그대로
 * - SharedPreferences 는 프로세스마다 메모리 Map 을 따로 두고(MODE_MULTI_PROCESS 는 deprecated), 쓰기는 파일 전체를 덮는다.
 *   변경 리스너는 같은 프로세스의 변경만 받는다
 * - QueuedWork(AOSP 13): apply 의 디스크 쓰기는 100ms(DELAY) 뒤 전용 스레드에서. `waitToFinish()` 는 남은 일을 **호출한 스레드에서** 처리하고
 *   끝나기를 기다린다 — ActivityThread 가 handleStopActivity(onStop·상태 저장 뒤) · handleServiceArgs(onStartCommand 뒤) ·
 *   handleStopService 에서 메인 스레드로 부른다(handlePauseActivity 는 Honeycomb 이전 앱만). 같은 파일의 중간 apply 는 건너뛰고 마지막 것만 쓴다
 * - 떠난 화면의 stop 은 ActivityTaskSupervisor 의 stopping 목록에 들어갔다가 다음 화면이 idle 을 알릴 때(또는 IDLE_TIMEOUT 10초) 처리된다
 */
