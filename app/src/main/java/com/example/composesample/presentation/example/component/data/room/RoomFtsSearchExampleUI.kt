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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.system.measureNanoTime
import kotlinx.coroutines.launch

@Composable
fun RoomFtsSearchExampleUI(onBackEvent: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val db = remember { FtsSearchDatabase.create(context) }
    val docDao = remember { db.docDao() }
    val ftsDao = remember { db.docFtsDao() }

    var seedSize by remember { mutableStateOf("5000") }
    var query by remember { mutableStateOf("kotlin") }
    var rowCount by remember { mutableStateOf(0) }
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
                "Room FTS4 vs LIKE 검색 성능 비교",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        SectionCardFts(title = "개념 요약") {
            Text(
                """
                • LIKE '%query%' : 양쪽 와일드카드 — 인덱스 미사용 → 전체 행 스캔(O(N))
                • FTS4 MATCH    : 가상 테이블의 역색인(inverted index)을 통한 토큰 매칭(O(logN))
                  - prefix 매칭: 'kotl*' → kotlin, kotlinx 등 어두 일치
                  - 기본 simple tokenizer 는 공백/구두점 기준 분리
                • contentEntity 지정 시 외부 테이블과 자동 동기화도 가능
                  (본 예제는 양쪽에 직접 insert 하여 비교를 명확히 함)
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
                            likeResult = runSearch { docDao.searchLike(query.trim(), LIMIT) }
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
                            matchResult = runSearch { ftsDao.searchMatch(query.trim(), LIMIT) }
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
            if (l != null && m != null && m.elapsedMs > 0) {
                Spacer(Modifier.height(8.dp))
                val ratio = (l.elapsedMs / m.elapsedMs)
                Text(
                    "→ FTS MATCH 가 LIKE 보다 약 %.1fx 빠름".format(ratio),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
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
    val elapsedMs: Double,
    val rows: List<DocEntity>
)

private suspend inline fun runSearch(crossinline block: suspend () -> List<DocEntity>): SearchResult {
    var rows: List<DocEntity> = emptyList()
    val nanos = measureNanoTime {
        rows = block()
    }
    return SearchResult(elapsedMs = nanos / 1_000_000.0, rows = rows)
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
                "%.2f ms · ${result.rows.size}건".format(result.elapsedMs),
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
