package com.example.composesample.presentation.example.component.data.cache

/**
 * Data/Cache 예제 참고 자료
 *
 * ## DataCacheExampleUI (Room 로컬 캐싱 + 실시간 검색)
 * - 공식 문서: https://developer.android.com/training/data-storage/room
 * - Flow 쿼리: https://developer.android.com/training/data-storage/room/async-queries
 * 핵심 개념:
 * - Room @Dao의 Flow 반환 쿼리로 DB 변경을 실시간 구독 (insert/update/delete 시 자동 재방출)
 * - CRUD + startsWith 검색을 collectAsStateWithLifecycle로 UI 반영
 * - ARCH-03: ViewModel이 RoomSingleton을 직접 참조하지 않고 UserCacheRepository(추상화)에 의존
 *   - UserData가 Room @Entity라 순수 Kotlin domain에 못 둠 → 추상화 인터페이스를 data 레이어에 배치
 *   - presentation → data 직접참조 제거 (레이어 규칙 준수, ARCHITECTURE.md 참고)
 *
 * ## KtorHttpCacheExampleUI (Ktor 클라이언트 HttpCache — 3.6.0)
 * - 공식 문서(Caching): https://ktor.io/docs/client-caching.html
 * - Ktor 3.6.0 변경 이력: https://github.com/ktorio/ktor/blob/main/CHANGELOG.md
 * - HTTP 캐싱 의미론(RFC 9111): https://www.rfc-editor.org/rfc/rfc9111
 *
 * ### 핵심 개념 (3.6.0 jar javap + JVM·실기기 실측)
 * - 설치: `install(HttpCache) { isShared; publicStorage(CacheStorage); privateStorage(CacheStorage) }`.
 *   저장소 기본값은 메모리(`CacheStorage.Unlimited()`), 끄려면 `CacheStorage.Disabled`.
 *   ⚠️ Config 에 같은 이름의 deprecated 프로퍼티(`publicStorage: HttpCacheStorage`, 옛 인터페이스)가 있어
 *   블록 안에서 바깥의 같은 이름 변수를 쓰면 그쪽으로 해석돼 컴파일 오류가 난다 → `this@Outer.publicStorage` 로 명시
 * - **3.6.0 신규(3.5.2 jar 와 대조)**: `HttpCache.clearAllCaches()`(suspend) · `CacheStorage.clear()` 기본 구현 ·
 *   `FileStorage(Path, FileSystem = SystemFileSystem, dispatcher)`(kotlinx-io) · `ContentNegotiationConfig.acceptHeaderMergeStrategy`
 *   (`ContentTypeMergeStrategy.Default` / `SkipIfPresent`). 제거된 API 는 0
 * - 기존 JVM 전용 `FileStorage(File)` 은 3.6.0 에서 `Path(file.path)` 로 바꿔 새 함수에 위임한다(바이트코드) →
 *   두 입구 모두 `CachingCacheStorage`(메모리 앞단 + `FileCacheStorage`)를 돌려준다(실측). URL 하나당 해시 이름 파일 하나
 * - 이벤트 `HttpCache.HttpResponseFromCache` 는 **신선한 캐시 적중과 304 재검증 모두**에서 난다.
 *   서버가 304 를 돌려줘도 **앱은 캐시 본문이 담긴 200 을 받는다** — 네트워크 사용량은 엔진/서버 쪽 기록으로만 셀 수 있다
 * - 응답 헤더별 동작(MockEngine 원 서버, 같은 URL 반복):
 *   `max-age=N` 은 N초 동안 서버에 가지 않고, 만료 뒤 `If-None-Match` 로 재검증 → 304 · `no-cache` 는 매번 재검증(저장은 함) ·
 *   `no-store` 는 저장 안 함(조건부 헤더 없음, 이벤트 0) · Cache-Control 없이 ETag 만 있으면 매번 재검증 ·
 *   ETag 가 없으면 `Last-Modified` 로 `If-Modified-Since` · 서버 콘텐츠가 바뀌면 재검증에서 200 + 새 ETag 로 교체
 * - `Cache-Control: private` 는 비공유 캐시(isShared = false)에서 **privateStorage** 로, 공유 캐시(isShared = true)에서는 저장하지 않는다.
 *   public `max-age` 는 publicStorage. 저장소 건수는 넘겨준 CacheStorage 의 `findAll(url)` 로 직접 확인할 수 있다
 * - `clearAllCaches()` 는 public·private 저장소를 모두 비운다. FileStorage 면 파일도 지워지고 디렉터리는 남는다
 * - 메모리 저장소는 클라이언트(=프로세스)와 함께 사라진다. 새 클라이언트가 같은 디렉터리의 FileStorage 를 쓰면 첫 요청부터 디스크에서 적중한다
 * - Accept 병합: ContentNegotiation(gson) 만 설치하면 `application/json`. `accept(text/plain)` 을 직접 지정하면
 *   Default 는 `text/plain` + `application/json` 두 값을, SkipIfPresent 는 `text/plain` 하나만 보낸다
 * - kotlinx-io-core 는 Ktor 전이(0.9.1)로 이미 들어와 있지만 앱 코드가 `Path`/`SystemFileSystem` 을 직접 쓰므로
 *   같은 버전으로 명시 선언했다(해석 결과 변화 0 — 클래스패스 4종 대조)
 */
