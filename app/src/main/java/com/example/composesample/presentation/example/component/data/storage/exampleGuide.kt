package com.example.composesample.presentation.example.component.data.storage

/**
 * DataStore 예제 참고 자료
 *
 * ## DataStoreExampleUI (DataStore 1.3.0 ↔ SharedPreferences)
 * - 공식 문서: https://developer.android.com/topic/libraries/architecture/datastore
 * - 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/datastore
 * - SharedPreferences.Editor apply/commit: https://developer.android.com/reference/android/content/SharedPreferences.Editor
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
 */
