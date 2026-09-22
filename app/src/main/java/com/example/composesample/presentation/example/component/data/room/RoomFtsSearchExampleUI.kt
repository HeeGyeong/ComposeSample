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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RoomFtsSearchExampleUI(onBackEvent: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val db = remember { FtsSearchDatabase.create(context) }
    val docDao = remember { db.docDao() }
    val ftsDao = remember { db.docFtsDao() }

    var seedSize by remember { mutableStateOf("5000") }
    var query by remember { mutableStateOf("kotlin") }
    var rowCount by remember { mutableIntStateOf(0) }
    var likeResult by remember { mutableStateOf<SearchResult?>(null) }
    var matchResult by remember { mutableStateOf<SearchResult?>(null) }
    var status by remember { mutableStateOf("준비 — 시드를 먼저 생성하세요") }

    // 최초 진입 시 카운트 동기화
    LaunchedEffect(Unit) {
        rowCount = docDao.count()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // 상단 바
        MainHeader(
            title = "Room FTS4 vs LIKE 검색",
            onBackIconClicked = onBackEvent
        )

        SectionCardFts(title = "개념 요약") {
            Text(
                """
                • LIKE '%query%' : 양쪽 와일드카드 — 인덱스 미사용 → 전체 행 스캔(O(N))
                • FTS4 MATCH    : 가상 테이블의 역색인(inverted index)을 통한 토큰 매칭(O(logN))
                  - prefix 매칭: 'kotl*' → kotlin, kotlinx 등 어두 일치
                  - 기본 simple tokenizer 는 공백/구두점 기준 분리
                • contentEntity 지정 시 외부 테이블과 자동 동기화도 가능
                  (본 예제는 양쪽에 직접 insert 하여 비교를 명확히 함)
                • LIMIT 의 함정: 'kotlin' 처럼 흔한 단어는 LIKE 도 앞에서부터
                  100건을 금방 채워 둘의 차이가 거의 없다. 드물거나 없는 단어는
                  LIKE 가 끝까지 훑어야 해 FTS 가 훨씬 빠르다(없는 단어 실측 약 10~20배)
                """.trimIndent(),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        SectionCardFts(title = "시드 데이터") {
            Text("현재 행 수: $rowCount", fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = seedSize,
                onValueChange = { seedSize = it.filter { c -> c.isDigit() }.take(6) },
                label = { Text("시드 행 수 (최대 100000)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        val n = seedSize.toIntOrNull()?.coerceIn(100, 100_000) ?: 5000
                        status = "시드 생성 중… (n=$n)"
                        docDao.clear()
                        ftsDao.clear()
                        val docs = generateDocs(n)
                        // 일반 테이블 insert → 반환된 rowId 로 FTS 테이블에 동일 매핑 삽입
                        val ids = docDao.insertAll(docs)
                        val ftsRows = ids.mapIndexed { idx, id ->
                            DocFtsEntity(rowid = id, title = docs[idx].title, body = docs[idx].body)
                        }
                        ftsDao.insertAll(ftsRows)
                        rowCount = docDao.count()
                        likeResult = null
                        matchResult = null
                        status = "시드 생성 완료 — 검색 가능"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("시드 생성 / 재생성") }
        }

        SectionCardFts(title = "검색 실행") {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("검색어 (FTS 는 'kotl*' 같은 prefix 지원)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        scope.launch {
                            if (rowCount == 0) {
                                status = "시드를 먼저 생성하세요"
                                return@launch
                            }
                            val q = query.trim()
                            status = "LIKE 측정 중… (워밍업 1회 + ${BENCH_ROUNDS}회 반복)"
                            likeResult = runSearch(db, q, SQL_LIKE, arrayOf(q, q, LIMIT)) { docDao.searchLike(q, LIMIT) }
                            status = "LIKE 완료"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                ) { Text("LIKE 검색") }
                Spacer(Modifier.padding(4.dp))
                Button(
                    onClick = {
                        scope.launch {
                            if (rowCount == 0) {
                                status = "시드를 먼저 생성하세요"
                                return@launch
                            }
                            val q = query.trim()
                            status = "MATCH 측정 중… (워밍업 1회 + ${BENCH_ROUNDS}회 반복)"
                            matchResult = runSearch(db, q, SQL_MATCH, arrayOf(q, LIMIT)) { ftsDao.searchMatch(q, LIMIT) }
                            status = "MATCH 완료"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
                ) { Text("FTS MATCH 검색") }
            }
            Spacer(Modifier.height(8.dp))
            Text(status, fontSize = 12.sp, color = Color.DarkGray)
        }

        SectionCardFts(title = "결과 비교") {
            ResultRow("LIKE", likeResult, Color(0xFFEF5350))
            Spacer(Modifier.height(6.dp))
            ResultRow("MATCH", matchResult, Color(0xFF42A5F5))
            val l = likeResult
            val m = matchResult
            if (l != null && m != null) {
                Spacer(Modifier.height(8.dp))
                // 검색어가 다른 두 측정을 나누면 아무 의미가 없다 → 같은 검색어일 때만 배율을 낸다
                if (l.query == m.query) {
                    Text(
                        speedupText("FTS MATCH", "LIKE", l.stats, m.stats),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                } else {
                    Text(
                        "→ 검색어가 달라 비교하지 않음 (LIKE '${l.query}' · MATCH '${m.query}') — 같은 검색어로 둘 다 실행하세요",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }

        SectionCardFts(title = "검색 결과 (상위 5건, LIKE)") {
            val rows = likeResult?.rows
            if (rows.isNullOrEmpty()) {
                Text("—", fontSize = 12.sp)
            } else {
                rows.take(5).forEach {
                    Text("• #${it.id} ${it.title}", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        SectionCardFts(title = "검색 결과 (상위 5건, MATCH)") {
            val rows = matchResult?.rows
            if (rows.isNullOrEmpty()) {
                Text("—", fontSize = 12.sp)
            } else {
                rows.take(5).forEach {
                    Text("• #${it.id} ${it.title}", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

private const val LIMIT = 100

private data class SearchResult(
    val query: String,
    val stats: BenchStats,
    val rows: List<DocEntity>
)

// DAO 의 @Query 와 같은 SQL. 벤치마크는 이 SQL 을 SQLite 에 직접 실행해 잰다(RoomBenchmark.kt 참고).
private const val SQL_LIKE =
    "SELECT * FROM fts_doc WHERE title LIKE '%' || ? || '%' OR body LIKE '%' || ? || '%' ORDER BY id ASC LIMIT ?"
private const val SQL_MATCH =
    "SELECT rowid AS id, title, body FROM fts_doc_fts WHERE fts_doc_fts MATCH ? ORDER BY rowid ASC LIMIT ?"

/**
 * 결과 행은 DAO 로 한 번 읽고, 시간은 같은 SQL 을 SQLite 에 직접 실행해 워밍업 + 반복 측정의 중앙값으로 잰다.
 * 측정 루프 전체를 IO 스레드 한 번 안에서 돌린다.
 */
private suspend fun runSearch(
    db: FtsSearchDatabase,
    query: String,
    sql: String,
    args: Array<Any?>,
    daoQuery: suspend () -> List<DocEntity>
): SearchResult {
    val rows = daoQuery()
    val stats = withContext(Dispatchers.IO) {
        val sqlite = db.openHelper.readableDatabase
        benchmarkRoundRobin(listOf(suspend { sqlite.runSql(sql, args) })).first()
    }
    return SearchResult(query = query, stats = stats, rows = rows)
}

@Composable
private fun ResultRow(label: String, result: SearchResult?, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .background(color, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.padding(4.dp))
        if (result == null) {
            Text("미실행", fontSize = 12.sp, color = Color.Gray)
        } else {
            Text(
                "${result.stats.summary()} · ${result.rows.size}건",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// 시드 문서 생성 — 토큰 기반 검색이 의미를 갖도록 단어 조합으로 만든다
private fun generateDocs(n: Int): List<DocEntity> {
    val titles = listOf(
        "Kotlin", "Compose", "Coroutine", "Flow", "Room",
        "Jetpack", "Android", "WorkManager", "Hilt", "Navigation",
        "Material", "Modifier", "State", "Side Effect", "Recomposition"
    )
    val verbs = listOf("learn", "explore", "optimize", "debug", "compare", "migrate", "test")
    val nouns = listOf(
        "pattern", "performance", "architecture", "library", "snippet",
        "tutorial", "deep dive", "case study", "internals", "best practice"
    )
    return List(n) { i ->
        val t = titles[i % titles.size]
        val v = verbs[(i / 3) % verbs.size]
        val nw = nouns[(i / 7) % nouns.size]
        DocEntity(
            title = "$t $nw #$i",
            body = "How to $v $t for $nw in Android Jetpack — index $i with various keywords like kotlinx, jetbrains, lifecycle, viewmodel."
        )
    }
}

@Composable
private fun SectionCardFts(
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
