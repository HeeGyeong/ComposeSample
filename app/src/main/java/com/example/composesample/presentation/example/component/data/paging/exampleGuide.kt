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
