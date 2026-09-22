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
fun RoomIndexExampleUI(onBackEvent: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val db = remember { RoomIndexDatabase.create(context) }
    val noIndexDao = remember { db.noIndexDao() }
    val singleDao = remember { db.singleIndexDao() }
    val compositeDao = remember { db.compositeIndexDao() }

    var seedSize by remember { mutableStateOf("20000") }
    var rowCount by remember { mutableIntStateOf(0) }

    // age 범위 조회(min~max) 벤치마크 결과
    var ageNoIndex by remember { mutableStateOf<BenchResult?>(null) }
    var ageSingle by remember { mutableStateOf<BenchResult?>(null) }
    var ageComposite by remember { mutableStateOf<BenchResult?>(null) }

    // city 등호 + age 정렬 조회 벤치마크 결과
    var cityNoIndex by remember { mutableStateOf<BenchResult?>(null) }
    var citySingle by remember { mutableStateOf<BenchResult?>(null) }
    var cityComposite by remember { mutableStateOf<BenchResult?>(null) }

    var status by remember { mutableStateOf("준비 — 시드를 먼저 생성하세요") }

    LaunchedEffect(Unit) {
        rowCount = noIndexDao.count()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        MainHeader(
            title = "Room 인덱스 성능 비교",
            onBackIconClicked = onBackEvent
        )

        SectionCardIdx(title = "개념 요약") {
            Text(
                """
                • 인덱스 없음   : WHERE/ORDER BY 시 전체 행 스캔(O(N))
                • 단일 인덱스   : @Index(["age"]) → age 범위/등호 조회를 B-Tree 로 가속
                • 복합 인덱스   : @Index(["city","age"]) → 컬럼 순서 = 정렬 우선순위
                  - leftmost prefix 규칙: (city) 또는 (city, age) 조건만 인덱스로 '탐색'
                  - age 단독 조건은 탐색(SEARCH) 불가 → 대신 COUNT 처럼 인덱스
                    컬럼만 필요하면 인덱스 전체를 훑는 커버링 SCAN(테이블 스캔보단 빠름)
                  - city 등호 + age 정렬은 조회·정렬 모두 인덱스로 처리
                • 트레이드오프: 인덱스는 읽기를 빠르게 하지만 INSERT/UPDATE 비용과
                  저장 공간이 증가 → 조회 패턴에 맞춰 선택적으로 추가
                """.trimIndent(),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        SectionCardIdx(title = "측정 방법") {
            Text(
                "시간은 DAO 가 아니라 같은 SQL 을 SQLite 에 직접 실행해 잰다 — 워밍업 1회 후 세 테이블을 번갈아 " +
                    "${BENCH_ROUNDS}회씩 돌린 중앙값. DAO 호출 왕복에는 코루틴 스레드 전환과 Room 코드 실행이 더해져 " +
                    "디버그 빌드에서 호출당 10ms 이상이 섞이고 인덱스 차이가 가려진다.",
                fontSize = 11.sp,
                color = Color.DarkGray
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "디버그 빌드는 측정 코드 자체도 HotSwan 인터프리터로 돌아 호출당 약 1ms 가 더해진다 — 그래서 배율이 " +
                    "실제보다 작게(약 2배) 나온다. 같은 SQL 을 인터프리터 밖에서 재면 인덱스 없음 3.5ms · 단일 0.5ms(약 7배).",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        SectionCardIdx(title = "시드 데이터") {
            Text("현재 행 수(각 테이블): $rowCount", fontSize = 13.sp)
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
                        val n = seedSize.toIntOrNull()?.coerceIn(100, 100_000) ?: 20_000
                        status = "시드 생성 중… (n=$n) — 세 테이블에 동일 데이터 삽입"
                        noIndexDao.clear()
                        singleDao.clear()
                        compositeDao.clear()
                        // 세 테이블에 동일한 데이터를 삽입 → 인덱스 유무만 차이
                        for (i in 0 until n) {
                            // 배치로 나눠 삽입 (메모리 부담 완화)
                            if (i % BATCH == 0) {
                                val end = minOf(i + BATCH, n)
                                noIndexDao.insertAll((i until end).map { genNoIndex(it) })
                                singleDao.insertAll((i until end).map { genSingle(it) })
                                compositeDao.insertAll((i until end).map { genComposite(it) })
                            }
                        }
                        rowCount = noIndexDao.count()
                        ageNoIndex = null; ageSingle = null; ageComposite = null
                        cityNoIndex = null; citySingle = null; cityComposite = null
                        status = "시드 생성 완료 — 벤치마크 실행 가능"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("시드 생성 / 재생성") }
        }

        SectionCardIdx(title = "벤치마크 ① age BETWEEN 30 AND 40 (범위 조회)") {
            Text(
                "단일 인덱스(age)는 범위를 바로 탐색(SEARCH)해 가장 빠르다. 복합 (city,age)는 leftmost 가 city 라 " +
                    "age 로 탐색은 못 하고 인덱스 전체를 훑는다(커버링 SCAN) — 테이블 스캔보다는 빠르다",
                fontSize = 11.sp,
                color = Color.DarkGray
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        if (rowCount == 0) {
                            status = "시드를 먼저 생성하세요"; return@launch
                        }
                        status = "age 범위 조회 측정 중… (워밍업 1회 + ${BENCH_ROUNDS}회 반복)"
                        val results = runBenchSet(
                            db, SQL_COUNT_AGE_RANGE, arrayOf(30, 40),
                            { noIndexDao.countByAgeRange(30, 40) },
                            { singleDao.countByAgeRange(30, 40) },
                            { compositeDao.countByAgeRange(30, 40) }
                        )
                        ageNoIndex = results[0]; ageSingle = results[1]; ageComposite = results[2]
                        status = "age 범위 조회 벤치마크 완료"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("age 범위 조회 실행") }
            Spacer(Modifier.height(8.dp))
            BenchRow("인덱스 없음", ageNoIndex, Color(0xFFEF5350))
            BenchRow("단일 (age)", ageSingle, Color(0xFF42A5F5))
            BenchRow("복합 (city,age)", ageComposite, Color(0xFFFFA726))
            SpeedupHint(baseline = ageNoIndex, best = ageSingle, bestLabel = "단일 인덱스")
        }

        SectionCardIdx(title = "벤치마크 ② city='Seoul' ORDER BY age (등호+정렬)") {
            Text(
                "복합 인덱스(city,age)는 city 로 탐색하고 age 순서 그대로 읽어 정렬이 필요 없다. 단일(age)은 age 순서로 " +
                    "훑으며 city 를 거르다 LIMIT 에서 멈춰, 이 데이터(도시 10개)에선 복합과 비슷하게 나온다. 인덱스 없음은 전체 스캔 + 정렬용 임시 B-Tree",
                fontSize = 11.sp,
                color = Color.DarkGray
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        if (rowCount == 0) {
                            status = "시드를 먼저 생성하세요"; return@launch
                        }
                        status = "city 조회 측정 중… (워밍업 1회 + ${BENCH_ROUNDS}회 반복)"
                        val results = runBenchSet(
                            db, SQL_FIND_BY_CITY, arrayOf("Seoul", LIMIT),
                            { noIndexDao.findByCity("Seoul", LIMIT).size },
                            { singleDao.findByCity("Seoul", LIMIT).size },
                            { compositeDao.findByCity("Seoul", LIMIT).size }
                        )
                        cityNoIndex = results[0]; citySingle = results[1]; cityComposite = results[2]
                        status = "city 조회 벤치마크 완료"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("city 등호+정렬 조회 실행") }
            Spacer(Modifier.height(8.dp))
            BenchRow("인덱스 없음", cityNoIndex, Color(0xFFEF5350))
            BenchRow("단일 (age)", citySingle, Color(0xFF42A5F5))
            BenchRow("복합 (city,age)", cityComposite, Color(0xFFFFA726))
            SpeedupHint(baseline = cityNoIndex, best = cityComposite, bestLabel = "복합 인덱스")
        }

        SectionCardIdx(title = "상태") {
            Text(status, fontSize = 12.sp, color = Color.DarkGray)
        }

        Spacer(Modifier.height(24.dp))
    }
}

private const val LIMIT = 100
private const val BATCH = 2_000

private data class BenchResult(
    val stats: BenchStats,
    val resultValue: Int
)

// DAO 의 @Query 와 같은 SQL — 세 테이블은 이름만 다르다(%s). 벤치마크는 이 SQL 을 SQLite 에 직접 실행해 잰다.
private const val SQL_COUNT_AGE_RANGE = "SELECT COUNT(*) FROM %s WHERE age BETWEEN ? AND ?"
private const val SQL_FIND_BY_CITY = "SELECT * FROM %s WHERE city = ? ORDER BY age ASC LIMIT ?"
private val BENCH_TABLES = listOf("idx_no_index", "idx_single", "idx_composite")

/**
 * 결과 건수는 DAO 로 읽고(세 테이블이 같은 답을 내는지 확인), 시간은 같은 SQL 을 SQLite 에 직접 실행해
 * 번갈아 반복 측정한다(RoomBenchmark.kt). 측정 루프 전체를 IO 스레드 한 번 안에서 돌린다.
 */
private suspend fun runBenchSet(
    db: RoomIndexDatabase,
    sqlTemplate: String,
    args: Array<Any?>,
    vararg daoQueries: suspend () -> Int
): List<BenchResult> {
    val values = daoQueries.map { it() }
    val stats = withContext(Dispatchers.IO) {
        val sqlite = db.openHelper.readableDatabase
        benchmarkRoundRobin(BENCH_TABLES.map { table -> suspend { sqlite.runSql(sqlTemplate.format(table), args) } })
    }
    return stats.mapIndexed { i, s -> BenchResult(stats = s, resultValue = values[i]) }
}

@Composable
private fun BenchRow(label: String, result: BenchResult?, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
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
                "${result.stats.summary()} · 결과 ${result.resultValue}건",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun SpeedupHint(baseline: BenchResult?, best: BenchResult?, bestLabel: String) {
    if (baseline != null && best != null) {
        Spacer(Modifier.height(8.dp))
        Text(
            speedupText(bestLabel, "인덱스 없음", baseline.stats, best.stats),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
    }
}

// 시드 데이터 생성 — 세 엔티티가 동일 값을 갖도록 공통 규칙 사용
private val CITIES = listOf("Seoul", "Busan", "Incheon", "Daegu", "Gwangju", "Daejeon")
private fun cityOf(i: Int) = CITIES[i % CITIES.size]
private fun ageOf(i: Int) = 20 + (i % 50)            // 20~69
private fun nameOf(i: Int) = "user_$i"

private fun genNoIndex(i: Int) =
    NoIndexEntity(name = nameOf(i), age = ageOf(i), city = cityOf(i))

private fun genSingle(i: Int) =
    SingleIndexEntity(name = nameOf(i), age = ageOf(i), city = cityOf(i))

private fun genComposite(i: Int) =
    CompositeIndexEntity(name = nameOf(i), age = ageOf(i), city = cityOf(i))

@Composable
private fun SectionCardIdx(
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
