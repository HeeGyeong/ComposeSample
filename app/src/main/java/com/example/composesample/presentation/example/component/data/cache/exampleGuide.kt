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
 */
