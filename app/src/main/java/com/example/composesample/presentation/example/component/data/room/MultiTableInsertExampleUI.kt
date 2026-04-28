package com.example.composesample.presentation.example.component.data.room

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun MultiTableInsertExampleUI(onBackEvent: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 예제용 in-memory DB — Composition 동안만 유지
    val db = remember { MultiTableInsertDatabase.create(context) }
    val repo = remember { MultiTableInsertRepository(db) }

    var snapshot by remember { mutableStateOf<TableSnapshot?>(null) }
    var lastResult by remember { mutableStateOf<String?>(null) }
    var counter by remember { mutableStateOf(0) }

    // 초기 스냅샷 로드
    LaunchedEffect(Unit) { snapshot = repo.snapshot() }

    val refresh: suspend () -> Unit = { snapshot = repo.snapshot() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // 상단 바
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Multi-Table Inserts in Room",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 설명
        SectionCard(title = "패턴 요약") {
            Text(
                """
                • BaseInsertDao<T> 인터페이스로 @Insert 시그니처를 공통화
                  → 자식 DAO 4개 (Author/Post/Tag/PostTag) 가 보일러플레이트 없이 insert/insertAll 재사용
                • db.withTransaction { ... } 으로 다중 테이블 insert 를 원자적으로 묶음
                  → 트랜잭션 도중 예외 발생 시 모든 테이블 변경 롤백
                • In-memory DB 사용 (앱 재시작 시 초기화)
                """.trimIndent(),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // 카운트 표시
        SectionCard(title = "테이블 상태") {
            val s = snapshot
            if (s == null) {
                Text("로딩 중…", fontSize = 13.sp)
            } else {
                Text("Author : ${s.authorCount}", fontSize = 13.sp)
                Text("Post   : ${s.postCount}", fontSize = 13.sp)
                Text("Tag    : ${s.tagCount}", fontSize = 13.sp)
                Text("PostTag: ${s.postTagCount}", fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                if (s.posts.isNotEmpty()) {
                    Text("최근 Post:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    s.posts.take(3).forEach {
                        Text("  #${it.id} authorId=${it.authorId} \"${it.title}\"", fontSize = 11.sp)
                    }
                }
            }
        }

        // 액션 버튼
        SectionCard(title = "액션") {
            Button(
                onClick = {
                    scope.launch {
                        counter += 1
                        val author = AuthorEntity(
                            name = "Author #$counter",
                            email = "author$counter@example.com"
                        )
                        val post = PostEntity(
                            authorId = 0, // Repository 에서 실제 authorId 로 교체됨
                            title = "Post Title #$counter",
                            body = "Body of post #$counter"
                        )
                        val tags = listOf(
                            TagEntity(label = "kotlin-$counter"),
                            TagEntity(label = "room-$counter"),
                            TagEntity(label = "compose-$counter")
                        )
                        runCatching {
                            repo.publishPost(author, post, tags)
                        }.onSuccess { result ->
                            lastResult = "✅ 성공: authorId=${result.authorId}, " +
                                "postId=${result.postId}, tagIds=${result.tagIds}"
                        }.onFailure { e ->
                            lastResult = "❌ 실패: ${e.message}"
                        }
                        refresh()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Multi-Table Insert (Author + Post + 3 Tags + CrossRef)") }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        counter += 1
                        val author = AuthorEntity(
                            name = "FailAuthor #$counter",
                            email = "fail$counter@example.com"
                        )
                        val post = PostEntity(
                            authorId = 0,
                            title = "Will be rolled back #$counter",
                            body = ""
                        )
                        runCatching {
                            repo.publishPost(
                                author = author,
                                post = post,
                                tags = listOf(TagEntity(label = "rollback-$counter")),
                                forceFailure = true
                            )
                        }.onSuccess {
                            lastResult = "예상과 다름: 성공해선 안 됨"
                        }.onFailure { e ->
                            lastResult = "✅ 롤백 검증: \"${e.message}\" — author/post 모두 미반영"
                        }
                        refresh()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE57373))
            ) { Text("의도적 실패 → 트랜잭션 롤백 검증") }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        repo.clearAll()
                        lastResult = "🧹 모든 테이블 초기화"
                        refresh()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9E9E9E))
            ) { Text("모두 삭제") }
        }

        // 결과
        SectionCard(title = "마지막 실행 결과") {
            Text(lastResult ?: "—", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp
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
