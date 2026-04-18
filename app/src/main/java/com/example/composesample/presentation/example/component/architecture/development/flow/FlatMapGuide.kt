package com.example.composesample.presentation.example.component.architecture.development.flow

/**
 * Understanding flatMap vs flatMapLatest: A Deep Dive Guide
 * 
 * Flow 변환 연산자 flatMap과 flatMapLatest의 심층 분석 가이드
 * 
 * 출처: https://proandroiddev.com/understanding-flatmap-vs-flatmaplatest-a-deep-dive-061994b7ffc4
 * 
 * === FlatMapExampleUI에서 다루는 내용 ===
 * 
 * 이 예제는 Flow의 변환 연산자를 시각적으로 비교합니다:
 * 
 * 주요 데모:
 * - flatMap: 모든 내부 Flow를 동시에 수집
 * - flatMapLatest: 최신 Flow만 수집하고 이전 것은 취소
 * - flatMapConcat: 순차적으로 내부 Flow를 수집
 * - flatMapMerge: 병렬로 내부 Flow를 수집 (동시성 제어)
 * - 실전 사용 사례: 검색, 자동완성, 실시간 업데이트
 * 
 * === flatMap 개요 ===
 * 
 * 1. 개념
 *    - Flow<T>를 Flow<Flow<R>>로 변환한 후 평탄화(flatten)
 *    - 각 원본 값이 새로운 Flow를 생성
 *    - 모든 내부 Flow를 수집
 * 
 * 2. 기본 문법
 *    flow {
 *        emit(1)
 *        emit(2)
 *    }.flatMapConcat { value ->
 *        flow {
 *            emit("$value-A")
 *            delay(100)
 *            emit("$value-B")
 *        }
 *    }
 *    // 결과: 1-A, 1-B, 2-A, 2-B (순차적)
 * 
 * 3. flatMap의 종류
 *    - flatMapConcat: 순차 처리
 *    - flatMapMerge: 병렬 처리 (동시성 제어)
 *    - flatMapLatest: 최신 값만 처리
 * 
 * === flatMapConcat ===
 * 
 * 1. 동작 방식
 *    - 각 내부 Flow를 순차적으로 수집
 *    - 이전 Flow가 완료되어야 다음 Flow 시작
 *    - 순서가 보장됨
 * 
 * 2. 예제
 *    flow {
 *        emit("User1")
 *        emit("User2")
 *        emit("User3")
 *    }.flatMapConcat { userId ->
 *        fetchUserDetails(userId)  // 각각 1초 소요
 *    }
 *    // 총 소요 시간: 3초 (순차적)
 * 
 * 3. 사용 시나리오
 *    - 순서가 중요한 경우
 *    - 이전 작업의 결과가 다음 작업에 필요한 경우
 *    - 데이터 일관성이 중요한 경우
 * 
 * 4. 장점
 *    ✓ 순서 보장
 *    ✓ 예측 가능한 동작
 *    ✓ 리소스 사용 제어
 * 
 * 5. 단점
 *    ✗ 느린 처리 속도
 *    ✗ 병렬 처리 불가
 *    ✗ 지연 시간 누적
 * 
 * === flatMapMerge ===
 * 
 * 1. 동작 방식
 *    - 여러 내부 Flow를 동시에 수집
 *    - concurrency 파라미터로 동시성 제어
 *    - 순서는 보장되지 않음
 * 
 * 2. 예제
 *    flow {
 *        emit("User1")
 *        emit("User2")
 *        emit("User3")
 *    }.flatMapMerge(concurrency = 3) { userId ->
 *        fetchUserDetails(userId)  // 각각 1초 소요
 *    }
 *    // 총 소요 시간: ~1초 (병렬)
 * 
 * 3. 사용 시나리오
 *    - 순서가 중요하지 않은 경우
 *    - 빠른 처리가 필요한 경우
 *    - 독립적인 작업들
 * 
 * 4. concurrency 파라미터
 *    flatMapMerge(concurrency = 2) { ... }
 *    - DEFAULT_CONCURRENCY: 16 (기본값)
 *    - 동시에 실행될 최대 Flow 개수
 *    - 리소스 사용량 조절
 * 
 * 5. 장점
 *    ✓ 빠른 처리 속도
 *    ✓ 병렬 실행
 *    ✓ 효율적인 리소스 활용
 * 
 * 6. 단점
 *    ✗ 순서 미보장
 *    ✗ 높은 리소스 사용
 *    ✗ 복잡한 디버깅
 * 
 * === flatMapLatest ===
 * 
 * 1. 동작 방식
 *    - 새로운 값이 방출되면 이전 Flow 취소
 *    - 항상 최신 Flow만 활성 상태
 *    - switchMap과 동일한 개념
 * 
 * 2. 예제
 *    searchQueryFlow
 *        .debounce(300)
 *        .flatMapLatest { query ->
 *            searchRepository.search(query)
 *        }
 *    // 새로운 검색어 입력 시 이전 검색 취소
 * 
 * 3. 사용 시나리오
 *    - 검색 기능 (Search as you type)
 *    - 자동완성
 *    - 실시간 필터링
 *    - 탭/버튼 전환
 * 
 * 4. 장점
 *    ✓ 불필요한 작업 취소
 *    ✓ 리소스 절약
 *    ✓ 최신 결과만 표시
 *    ✓ 네트워크 요청 최소화
 * 
 * 5. 단점
 *    ✗ 중간 결과 손실 가능
 *    ✗ 빠른 변경 시 결과 없을 수 있음
 *    ✗ 완료 보장 안 됨
 * 
 * 6. 내부 동작
 *    fun <T, R> Flow<T>.flatMapLatest(
 *        transform: suspend (T) -> Flow<R>
 *    ): Flow<R> = map(transform).flattenLatest()
 *    
 *    - map으로 Flow<Flow<R>> 생성
 *    - flattenLatest로 평탄화
 *    - 새 Flow 시작 시 이전 Flow 취소
 * 
 * === 실전 비교: flatMapConcat vs flatMapMerge vs flatMapLatest ===
 * 
 * 검색 시나리오:
 * 
 * 1. flatMapConcat 사용 시
 *    사용자가 "H" → "He" → "Hel" 입력
 *    
 *    - "H" 검색 시작 (1초)
 *    - "H" 검색 완료
 *    - "He" 검색 시작 (1초)
 *    - "He" 검색 완료
 *    - "Hel" 검색 시작 (1초)
 *    - "Hel" 검색 완료
 *    
 *    총 시간: 3초
 *    문제: 사용자가 "Hel"의 결과를 기다리는 동안 "H", "He"의 결과도 기다림
 * 
 * 2. flatMapMerge 사용 시
 *    사용자가 "H" → "He" → "Hel" 입력
 *    
 *    - "H" 검색 시작
 *    - "He" 검색 시작
 *    - "Hel" 검색 시작
 *    - 모든 검색이 병렬로 진행
 *    - 완료 순서: 불확실
 *    
 *    총 시간: ~1초
 *    문제: "H"의 결과가 "Hel"보다 늦게 도착하면 잘못된 결과 표시
 * 
 * 3. flatMapLatest 사용 시 (정답!)
 *    사용자가 "H" → "He" → "Hel" 입력
 *    
 *    - "H" 검색 시작
 *    - "He" 입력 → "H" 검색 취소
 *    - "He" 검색 시작
 *    - "Hel" 입력 → "He" 검색 취소
 *    - "Hel" 검색 시작
 *    - "Hel" 검색 완료
 *    
 *    총 시간: ~1초
 *    결과: 최신 검색어("Hel")의 결과만 표시
 * 
 * === 성능 비교 ===
 * 
 * 시나리오: 3개의 요청, 각 요청 1초 소요
 * 
 * flatMapConcat:
 * ├─ Request 1 (1초) ────────┐
 * │                          ↓
 * ├─ Request 2 (1초) ────────┐
 * │                          ↓
 * └─ Request 3 (1초) ────────┘
 * 총 시간: 3초
 * 
 * flatMapMerge(concurrency = 3):
 * ├─ Request 1 (1초) ─┐
 * ├─ Request 2 (1초) ─┤
 * └─ Request 3 (1초) ─┘
 * 총 시간: ~1초
 * 
 * flatMapLatest:
 * ├─ Request 1 시작 → 취소
 * ├─ Request 2 시작 → 취소
 * └─ Request 3 (1초) ────────┘
 * 총 시간: ~1초
 * 결과: Request 3만 완료
 * 
 * === 실전 사용 사례 ===
 * 
 * 1. 검색 기능 (flatMapLatest)
 *    textField
 *        .asFlow()
 *        .debounce(300)  // 300ms 대기
 *        .filter { it.length >= 2 }  // 최소 2글자
 *        .distinctUntilChanged()  // 중복 제거
 *        .flatMapLatest { query ->
 *            searchRepository.search(query)
 *        }
 *        .catch { emit(emptyList()) }
 *        .collect { results ->
 *            updateUI(results)
 *        }
 *    
 *    왜 flatMapLatest?
 *    - 사용자가 빠르게 입력할 때 불필요한 검색 취소
 *    - 항상 최신 검색어의 결과만 표시
 *    - 네트워크 요청 최소화
 * 
 * 2. 탭 전환 (flatMapLatest)
 *    selectedTabFlow
 *        .flatMapLatest { tab ->
 *            when (tab) {
 *                Tab.HOME -> homeRepository.getHomeData()
 *                Tab.PROFILE -> profileRepository.getProfile()
 *                Tab.SETTINGS -> settingsRepository.getSettings()
 *            }
 *        }
 *        .collect { data ->
 *            updateUI(data)
 *        }
 *    
 *    왜 flatMapLatest?
 *    - 탭 전환 시 이전 탭의 데이터 로딩 취소
 *    - 현재 탭의 데이터만 로딩
 * 
 * 3. 여러 사용자 데이터 병렬 로딩 (flatMapMerge)
 *    userIdsFlow
 *        .flatMapMerge(concurrency = 5) { userId ->
 *            userRepository.getUserDetails(userId)
 *        }
 *        .collect { userDetail ->
 *            addToList(userDetail)
 *        }
 *    
 *    왜 flatMapMerge?
 *    - 여러 사용자의 데이터를 동시에 로딩
 *    - 순서가 중요하지 않음
 *    - concurrency로 동시 요청 수 제어
 * 
 * 4. 순차적 데이터 처리 (flatMapConcat)
 *    transactionsFlow
 *        .flatMapConcat { transaction ->
 *            processTransaction(transaction)
 *                .flatMapConcat { result ->
 *                    updateDatabase(result)
 *                }
 *        }
 *        .collect { finalResult ->
 *            updateUI(finalResult)
 *        }
 *    
 *    왜 flatMapConcat?
 *    - 트랜잭션은 순서대로 처리되어야 함
 *    - 이전 트랜잭션이 완료되어야 다음 시작
 *    - 데이터 일관성 보장
 * 
 * 5. 실시간 위치 업데이트 (flatMapLatest)
 *    locationUpdatesFlow
 *        .flatMapLatest { location ->
 *            nearbyPlacesRepository.getNearbyPlaces(location)
 *        }
 *        .collect { places ->
 *            updateMap(places)
 *        }
 *    
 *    왜 flatMapLatest?
 *    - 위치가 변경되면 이전 위치의 장소 검색 취소
 *    - 현재 위치의 장소만 표시
 * 
 * === 선택 가이드 ===
 * 
 * flatMapConcat을 선택:
 * ✓ 순서가 중요한 경우
 * ✓ 이전 작업의 결과가 필요한 경우
 * ✓ 데이터 일관성이 중요한 경우
 * ✓ 트랜잭션 처리
 * ✓ 순차적 워크플로우
 * 
 * flatMapMerge를 선택:
 * ✓ 순서가 중요하지 않은 경우
 * ✓ 빠른 처리가 필요한 경우
 * ✓ 독립적인 작업들
 * ✓ 병렬 API 호출
 * ✓ 대량 데이터 처리
 * 
 * flatMapLatest를 선택:
 * ✓ 최신 결과만 필요한 경우
 * ✓ 검색/필터링
 * ✓ 탭/화면 전환
 * ✓ 실시간 업데이트
 * ✓ 자동완성
 * ✓ 사용자 입력 추적
 * 
 * === 일반적인 실수 ===
 * 
 * 1. flatMapMerge를 검색에 사용
 *    ❌ 잘못된 예:
 *    searchQuery.flatMapMerge { query ->
 *        searchApi(query)
 *    }
 *    
 *    문제: 이전 검색이 취소되지 않아 순서가 뒤섞임
 *    
 *    ✓ 올바른 예:
 *    searchQuery.flatMapLatest { query ->
 *        searchApi(query)
 *    }
 * 
 * 2. flatMapConcat을 독립적 작업에 사용
 *    ❌ 잘못된 예:
 *    userIds.flatMapConcat { id ->
 *        fetchUser(id)  // 각각 1초
 *    }
 *    // 10명이면 10초 소요
 *    
 *    ✓ 올바른 예:
 *    userIds.flatMapMerge(concurrency = 5) { id ->
 *        fetchUser(id)
 *    }
 *    // 10명이어도 ~2초 소요
 * 
 * 3. flatMapLatest를 모든 데이터가 필요한 경우에 사용
 *    ❌ 잘못된 예:
 *    pageNumbers.flatMapLatest { page ->
 *        fetchPage(page)
 *    }
 *    // 마지막 페이지만 로드됨
 *    
 *    ✓ 올바른 예:
 *    pageNumbers.flatMapConcat { page ->
 *        fetchPage(page)
 *    }
 *    // 모든 페이지 순차적으로 로드
 * 
 * === debounce와 함께 사용 ===
 * 
 * 검색 최적화:
 * searchQuery
 *     .debounce(300)  // 입력 후 300ms 대기
 *     .filter { it.length >= 2 }
 *     .distinctUntilChanged()  // 동일한 값 필터링
 *     .flatMapLatest { query ->
 *         if (query.isEmpty()) {
 *             flowOf(emptyList())
 *         } else {
 *             searchRepository.search(query)
 *                 .catch { emit(emptyList()) }
 *         }
 *     }
 * 
 * 각 단계의 역할:
 * 1. debounce: 사용자가 입력을 멈출 때까지 대기
 * 2. filter: 너무 짧은 검색어 제외
 * 3. distinctUntilChanged: 중복 검색 방지
 * 4. flatMapLatest: 이전 검색 취소
 * 5. catch: 에러 처리
 * 
 * === 성능 최적화 팁 ===
 * 
 * 1. concurrency 조절
 *    flatMapMerge(concurrency = 3) { ... }
 *    - 너무 높으면: 리소스 과다 사용
 *    - 너무 낮으면: 성능 저하
 *    - 권장: API 제한, 네트워크 상황 고려
 * 
 * 2. 취소 가능한 작업
 *    flow {
 *        // 무거운 작업
 *        ensureActive()  // 취소 체크
 *        emit(result)
 *    }
 * 
 * 3. 에러 처리
 *    .flatMapLatest { query ->
 *        searchApi(query)
 *            .retry(3)  // 3회 재시도
 *            .catch { emit(emptyList()) }
 *    }
 * 
 * 4. 캐싱
 *    .flatMapLatest { query ->
 *        cache[query] ?: searchApi(query).also {
 *            cache[query] = it
 *        }
 *    }
 * 
 * === 테스트 ===
 * 
 * flatMapLatest 테스트:
 * @Test
 * fun `flatMapLatest cancels previous flow`() = runTest {
 *     val flow = flow {
 *         emit(1)
 *         delay(100)
 *         emit(2)
 *     }.flatMapLatest { value ->
 *         flow {
 *             emit("$value-start")
 *             delay(200)
 *             emit("$value-end")
 *         }
 *     }
 *     
 *     val results = flow.toList()
 *     
 *     // 1-start 후 바로 2-start로 전환
 *     // 1-end는 취소되어 방출 안 됨
 *     assertEquals(
 *         listOf("1-start", "2-start", "2-end"),
 *         results
 *     )
 * }
 * 
 * === 요약 ===
 * 
 * flatMapConcat:
 * - 순차 처리
 * - 순서 보장
 * - 느림
 * - 사용: 순서가 중요한 작업
 * 
 * flatMapMerge:
 * - 병렬 처리
 * - 순서 미보장
 * - 빠름
 * - 사용: 독립적인 작업들
 * 
 * flatMapLatest:
 * - 최신 것만 처리
 * - 이전 것 취소
 * - 효율적
 * - 사용: 검색, 필터링, 전환
 */

object FlatMapGuide {
    const val GUIDE_INFO = """
        flatMap vs flatMapLatest
        
        핵심 차이:
        - flatMapConcat: 순차 처리, 순서 보장
        - flatMapMerge: 병렬 처리, 빠름
        - flatMapLatest: 최신만 처리, 이전 취소
        
        주요 사용처:
        - 검색: flatMapLatest
        - 병렬 로딩: flatMapMerge
        - 순차 처리: flatMapConcat
        
        출처: https://proandroiddev.com/understanding-flatmap-vs-flatmaplatest-a-deep-dive-061994b7ffc4
    """
}
