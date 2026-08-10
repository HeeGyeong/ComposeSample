package com.example.composesample.presentation.example.component.data.paging

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.composesample.presentation.MainHeader
import org.koin.androidx.compose.koinViewModel

@Composable
fun RemoteMediatorExampleUI(onBackEvent: () -> Unit) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val viewModel = koinViewModel<RemoteMediatorViewModel>(owner = viewModelStoreOwner)

    val pagingItems = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val cachedCount by viewModel.cachedCount.collectAsStateWithLifecycle()
    val eventLog by viewModel.eventLog.collectAsStateWithLifecycle()

    val loadState = pagingItems.loadState
    val mediatorRefresh = loadState.mediator?.refresh
    val mediatorAppend = loadState.mediator?.append

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        MainHeader(
            title = "RemoteMediator 오프라인 우선 페이징",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                SectionCardRm(title = "개념 요약") {
                    Text(
                        """
                        • 단일 진실 공급원 : 화면은 항상 DB(Room)만 본다. 네트워크는 화면에 직접 닿지 않는다
                        • PagingSource     : Room 이 생성. DB 를 읽고, DB 가 바뀌면 스스로 invalidate
                        • RemoteMediator   : DB 데이터가 모자랄 때만 호출 → 네트워크에서 받아 DB 에 write
                        • LoadType 3분기   : REFRESH(처음/새로고침) · PREPEND(앞) · APPEND(뒤)
                        • RemoteKeys 테이블: DB 만 읽는 PagingSource 는 "다음 네트워크 페이지"를 모른다
                                             → 아이템별 prev/next 페이지 키를 따로 저장해 경계에서 조회
                        • initialize()     : 캐시가 신선하면 SKIP_INITIAL_REFRESH 로 네트워크를 건너뛴다
                        """.trimIndent(),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            item {
                SectionCardRm(title = "제어") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isOffline) Color(0xFFEF5350) else Color(0xFF66BB6A),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                if (isOffline) "네트워크 OFF" else "네트워크 ON",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.padding(6.dp))
                        Text(
                            "DB 캐시 ${cachedCount}건",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "네트워크를 끈 뒤 아래 목록을 끝까지 스크롤해 보면, 새 페이지는 못 받아도 " +
                            "이미 DB 에 있는 항목은 그대로 보인다. 이것이 오프라인 우선이다.",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.toggleOffline() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (isOffline) "네트워크 켜기" else "네트워크 끄기") }

                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { pagingItems.refresh() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("새로고침 (LoadType.REFRESH)") }

                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { viewModel.recreatePager() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Pager 재생성 — initialize() 분기 관찰") }

                    Spacer(Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { viewModel.clearCache() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("DB 캐시 비우기") }
                }
            }

            item {
                SectionCardRm(title = "LoadState — source(DB) vs mediator(네트워크)") {
                    Text(
                        "두 축이 따로 관측된다는 점이 RemoteMediator 를 쓸 때의 핵심 차이다. " +
                            "네트워크가 끊기면 mediator 만 Error 가 되고 source 는 정상이다.",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Spacer(Modifier.height(8.dp))
                    LoadStateRow("source.refresh", loadState.source.refresh, Color(0xFF42A5F5))
                    LoadStateRow("source.append", loadState.source.append, Color(0xFF42A5F5))
                    LoadStateRow("mediator.refresh", mediatorRefresh, Color(0xFFAB47BC))
                    LoadStateRow("mediator.append", mediatorAppend, Color(0xFFAB47BC))

                    val mediatorError = (mediatorRefresh as? LoadState.Error)
                        ?: (mediatorAppend as? LoadState.Error)
                    if (mediatorError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "네트워크 오류: ${mediatorError.error.message}",
                            fontSize = 12.sp,
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { pagingItems.retry() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("retry()") }
                    }
                }
            }

            item {
                SectionCardRm(title = "이벤트 로그 (RemoteMediator 호출 순서)") {
                    if (eventLog.isEmpty()) {
                        Text("아직 기록 없음", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF212121), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            eventLog.forEach { line ->
                                Text(
                                    line,
                                    fontSize = 10.sp,
                                    color = Color(0xFFB2FF59),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = { viewModel.clearLog() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("로그 비우기") }
                    }
                }
            }

            item {
                Text(
                    "목록 (DB 에서 읽음 · 총 ${pagingItems.itemCount}건 로드)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(
                count = pagingItems.itemCount,
                // peek 은 get 과 달리 로드를 트리거하지 않아 key 계산에 안전하다
                key = { index -> pagingItems.peek(index)?.id ?: index }
            ) { index ->
                pagingItems[index]?.let { article ->
                    ArticleRow(article)
                    HorizontalDivider()
                }
            }

            when (val append = loadState.append) {
                is LoadState.Loading -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }

                is LoadState.NotLoading -> if (append.endOfPaginationReached) {
                    item {
                        Text(
                            "— 마지막 페이지 (endOfPaginationReached = true) —",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }

                is LoadState.Error -> item {
                    Text(
                        "추가 로드 실패 — 탭하면 retry()",
                        fontSize = 12.sp,
                        color = Color(0xFFC62828),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ArticleRow(article: ArticleEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFF7986CB), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                "p${article.page}",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.padding(6.dp))
        Column {
            Text(article.title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("by ${article.author}", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun LoadStateRow(label: String, state: LoadState?, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .background(color, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.padding(4.dp))
        Text(
            text = describeLoadState(state),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = when (state) {
                is LoadState.Error -> Color(0xFFC62828)
                is LoadState.Loading -> Color(0xFF1565C0)
                else -> Color.DarkGray
            }
        )
    }
}

private fun describeLoadState(state: LoadState?): String = when (state) {
    // mediator 는 RemoteMediator 를 쓰지 않는 Pager 에서 null 이다
    null -> "null (RemoteMediator 미사용)"
    is LoadState.Loading -> "Loading"
    is LoadState.Error -> "Error(${state.error::class.simpleName})"
    is LoadState.NotLoading -> "NotLoading(end=${state.endOfPaginationReached})"
}

@Composable
private fun SectionCardRm(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.padding(top = 4.dp)) { content() }
        }
    }
}
