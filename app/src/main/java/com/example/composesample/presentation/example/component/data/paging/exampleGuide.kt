package com.example.composesample.presentation.example.component.data.paging

/**
 * Data/Paging 예제 참고 자료
 *
 * ## PagingExampleUI (Paging 3 + 무한 스크롤)
 * - 공식 문서: https://developer.android.com/topic/libraries/architecture/paging/v3-overview
 * - Compose 연동: https://developer.android.com/jetpack/compose/lists#large-datasets
 * 핵심 개념:
 * - PagingSource: load(params) 에서 페이지 키 기반으로 데이터 청크 반환 (LoadResult.Page/Error)
 * - Pager + PagingConfig(pageSize): Flow<PagingData<T>> 생성
 * - collectAsLazyPagingItems(): LazyColumn에서 items(lazyPagingItems) 로 소비
 * - loadState: refresh/append/prepend 상태로 로딩 인디케이터·에러 재시도 UI 구성
 */

/**
 * RemoteMediatorExampleUI (Paging 3 + Room 오프라인 우선 페이징) 참고 자료
 * - 공식 가이드: https://developer.android.com/topic/libraries/architecture/paging/v3-network-db
 * - RemoteMediator API: https://developer.android.com/reference/kotlin/androidx/paging/RemoteMediator
 * - Room Paging 통합: https://developer.android.com/topic/libraries/architecture/paging/v3-paged-data#room
 *
 * 핵심 개념
 * - 단일 진실 공급원(Single Source of Truth): 화면은 DB 만 본다.
 *   PagingSource 는 Room 이 생성하고(@Query 반환 타입을 PagingSource 로 선언),
 *   RemoteMediator 는 화면이 아니라 DB 에 write 한다 → DB 변경이 PagingSource 를 invalidate 해 화면에 반영
 * - RemoteMediator.load(loadType, state) 의 3분기
 *   · REFRESH : 최초 로드 또는 refresh() — 보통 기존 캐시를 지우고 첫 페이지부터 다시 채운다.
 *               앵커(state.anchorPosition)에서 페이지를 역산하는 변형도 있지만,
 *               캐시 전량 삭제와 함께 쓰면 새로고침 후 목록이 중간 페이지부터 시작하므로
 *               "지우지 않고 해당 구간만 갱신"하는 구현과 짝을 이뤄야 한다
 *   · PREPEND : 목록 앞쪽 경계 도달 — 앞 페이지를 받을 수 없으면 Success(endOfPaginationReached = true)
 *   · APPEND  : 목록 뒤쪽 경계 도달 — 다음 페이지를 받아 DB 에 append
 * - MediatorResult 는 Success(endOfPaginationReached) / Error(throwable) 두 가지.
 *   Error 를 돌려줘도 DB 는 그대로이므로 캐시된 항목은 계속 화면에 남는다(오프라인 우선의 실체)
 * - RemoteKeys 테이블이 필요한 이유: DB 만 읽는 PagingSource 는 "다음에 네트워크 몇 페이지를 받을지"를
 *   알 수 없다. 그래서 아이템별 prev/next 페이지 키를 별도 테이블에 저장하고
 *   경계 아이템(state.firstItemOrNull()/lastItemOrNull())의 키로 다음 요청 페이지를 역산한다
 * - 아이템과 키는 반드시 같은 트랜잭션(withTransaction)에서 갱신해야 한다
 *   — 부분 실패 시 "아이템은 있는데 키가 없어 더 못 받는" 상태가 생긴다
 * - initialize(): Paging 시작 시 1회 호출되어 초기 REFRESH 강제 여부를 결정
 *   · LAUNCH_INITIAL_REFRESH(기본) : 캐시를 무시하고 네트워크부터
 *   · SKIP_INITIAL_REFRESH         : 캐시를 그대로 쓰고 스크롤 시에만 APPEND
 *   보통 캐시 타임스탬프(MAX(createdAt))와 유효시간을 비교해 분기한다
 * - loadState 가 두 축으로 나뉜다: loadState.source(DB PagingSource) / loadState.mediator(네트워크).
 *   RemoteMediator 를 쓰지 않는 Pager 에서는 mediator 가 null 이다.
 *   네트워크 실패 시 mediator 만 Error 가 되고 source 는 NotLoading 을 유지한다
 * - Pager 의 remoteMediator 파라미터를 쓰려면 @OptIn(ExperimentalPagingApi::class) 가 필요하다
 * - Room DAO 가 PagingSource 를 반환하려면 androidx.room:room-paging 의존성이 필요하다
 *   (없으면 KSP 가 "Cannot find required type element ... LimitOffsetPagingSource" 로 실패)
 * - PagingConfig.initialLoadSize 기본값은 pageSize * 3 이라 첫 로드가 3페이지를 요청한다.
 *   네트워크 페이지와 DB 페이지를 1:1 로 관찰하려면 pageSize 와 같게 맞춘다
 */
