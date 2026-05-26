package com.example.composesample.presentation.example.component.data.room

/**
 * Multi-Table Inserts in Room 참고 자료
 * - 출처: https://androidessence.com/multi-table-inserts
 *
 * 핵심 개념
 * - DAO 인터페이스 상속(BaseInsertDao)으로 @Insert 공통 로직을 재사용
 *   → Room KSP는 default 구현이 있는 suspend 함수에 대해 abstract @Insert를 자동 생성
 * - @Transaction 으로 다중 테이블 insert를 원자적으로 묶어
 *   부분 실패 시 전체 롤백되도록 보장
 * - Composite DAO 패턴: 여러 자식 DAO를 추상 프로퍼티로 노출하고
 *   @Transaction 함수에서 자식 DAO의 default insert 를 순차 호출
 */

/**
 * Room FTS4 vs LIKE 검색 성능 비교 참고 자료
 * - 출처: https://medium.com/@valentinerutto/effective-search-in-room-fts-vs-like-f2225f1d528b
 *
 * 핵심 개념
 * - @Fts4 가상 테이블은 토큰 단위 역색인(inverted index)을 자동 생성
 *   → MATCH 연산자로 어두 일치(prefix: 'kotl*')·다중 토큰 검색을 빠르게 수행
 * - LIKE '%query%' 는 양쪽 와일드카드로 SQLite 가 인덱스를 활용할 수 없어
 *   행 수에 비례한 전체 스캔(O(N))이 발생
 * - contentEntity 를 지정하면 원본 테이블 변경이 FTS 테이블에 자동 반영되지만,
 *   본 예제는 비교 단순화를 위해 양쪽에 직접 insert 한다
 * - 토크나이저: 기본 simple(공백/구두점) · porter(어간 추출) · unicode61 등 선택 가능
 *   (한국어처럼 어절 분리가 어려운 언어는 별도 토크나이저 또는 ngram 전략 필요)
 */

/**
 * Room Database Indices 참고 자료
 * - 출처: Oğuzhan Aslan — Accelerate Android Room Queries with Database Indices (Android Weekly #728)
 *
 * 핵심 개념
 * - @Entity(indices = [Index(value = ["age"])]) 로 단일 컬럼 B-Tree 인덱스 생성
 *   → 등호/범위(BETWEEN, <, >) 조회를 전체 스캔(O(N)) 대신 인덱스 스캔으로 가속
 * - 복합 인덱스 @Index(["city", "age"]) 는 컬럼 순서가 곧 정렬 우선순위
 *   → leftmost prefix 규칙: (city) 또는 (city, age) 조건만 인덱스를 활용하며,
 *     age 단독 조건은 선행 컬럼(city)을 만족하지 못해 인덱스를 타지 못함
 *   → city 등호 + age 정렬 쿼리는 조회와 ORDER BY 를 모두 인덱스로 처리(정렬 비용 제거)
 * - @Index(unique = true) 로 유니크 제약을 인덱스로 강제할 수 있음
 * - 트레이드오프: 인덱스는 읽기를 빠르게 하지만 INSERT/UPDATE 시 인덱스 갱신 비용과
 *   추가 저장 공간이 발생 → 실제 조회 패턴(WHERE/ORDER BY/JOIN 컬럼)에 맞춰 선택적으로 추가
 */
