package com.example.composesample.presentation.example.component.data.room

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room 예제(인덱스 · FTS)가 함께 쓰는 쿼리 벤치마크 도구.
 *
 * 한 번만 재서 나눈 배율은 믿을 수 없다. 0.1ms 수준의 인덱스 쿼리는 첫 실행 비용(문장 컴파일 · 페이지 캐시 적재),
 * DAO 호출의 스레드 전환, GC, 디버그 빌드의 인터프리터 같은 잡음이 측정값을 좌우한다. 실측으로는 같은 쿼리의
 * 배율이 실행마다 0.4x ~ 150x 까지 흔들렸다. 그래서 다음 세 가지를 지킨다.
 * 1) 워밍업 1회는 측정에서 뺀다
 * 2) 라운드마다 실행 순서를 한 칸씩 돌린다 — "먼저 실행된 쪽이 캐시를 데워 준다"는 순서 편향을 없앤다
 * 3) 중앙값을 쓴다 — 한 번 튀는 값에 흔들리지 않는다
 *
 * 그리고 시간은 DAO 가 아니라 **같은 SQL 을 SQLite 에 직접 실행한 시간**으로 잰다([runSql]).
 * DAO 호출 왕복에는 코루틴 스레드 전환과 Room 생성 코드 실행이 더해지는데, 디버그 빌드에서는 이것이 호출당 10ms 이상이라
 * 쿼리 자체의 차이를 덮는다. 실측(2만 행, age BETWEEN COUNT): 직접 실행 인덱스 없음 3.4ms · 단일 인덱스 0.5ms(7배)
 * vs DAO 경유 17ms · 14ms(1.2배).
 */

/** 반복 측정 결과. 시간은 SQLite 직접 실행 기준이다([runSql]). */
internal data class BenchStats(
    val medianMs: Double,
    val minMs: Double,
    val maxMs: Double,
    val runs: Int
)

/** 라운드 수. 쿼리당 워밍업 1회 + 측정 [BENCH_ROUNDS]회. */
internal const val BENCH_ROUNDS = 7

/** 배율이 이 범위(±10%) 안이면 측정 오차로 보고 "차이 없음"으로 표시한다. */
private const val SAME_SPEED_TOLERANCE = 0.1

/**
 * [queries] 를 같은 조건에서 번갈아 반복 측정해 쿼리별 [BenchStats] 를 돌려준다(입력 순서 그대로).
 */
internal suspend fun benchmarkRoundRobin(
    queries: List<suspend () -> Any?>,
    rounds: Int = BENCH_ROUNDS
): List<BenchStats> {
    // 1) 워밍업 — 측정하지 않는다
    queries.forEach { it() }

    val samples = List(queries.size) { mutableListOf<Double>() }
    repeat(rounds) { round ->
        // 2) 라운드마다 시작 위치를 한 칸씩 돌린다: (A,B,C) → (B,C,A) → (C,A,B) …
        for (offset in queries.indices) {
            val index = (round + offset) % queries.size
            val start = System.nanoTime()
            queries[index]()
            samples[index] += (System.nanoTime() - start) / 1_000_000.0
        }
    }
    // 3) 중앙값
    return samples.map { values ->
        val sorted = values.sorted()
        BenchStats(
            medianMs = sorted[sorted.size / 2],
            minMs = sorted.first(),
            maxMs = sorted.last(),
            runs = sorted.size
        )
    }
}

/**
 * SQL 을 SQLite 에 직접 실행하고 결과 행을 끝까지 읽는다(엔티티 변환 없이). 읽은 행 수를 돌려준다.
 * 블로킹 호출이므로 IO 스레드에서, 측정 루프 전체를 한 번의 withContext 로 감싸 호출마다 스레드를 옮기지 않게 한다.
 */
internal fun SupportSQLiteDatabase.runSql(sql: String, args: Array<Any?>): Int =
    query(sql, args).use { cursor ->
        var rows = 0
        while (cursor.moveToNext()) rows++
        rows
    }

/** 결과 행에 쓰는 한 줄 요약: "중앙값 1.23 ms (0.98~2.10, 7회)". */
internal fun BenchStats.summary(): String =
    "중앙값 %.2f ms (%.2f~%.2f, %d회)".format(medianMs, minMs, maxMs, runs)

/**
 * 배율 문구. 1 미만을 "0.4x 빠름"처럼 쓰지 않고, 오차 범위 안이면 차이가 없다고 말한다.
 */
internal fun speedupText(targetLabel: String, baselineLabel: String, baseline: BenchStats, target: BenchStats): String {
    if (target.medianMs <= 0.0) return "→ 측정값이 0 이라 배율을 계산할 수 없음"
    val ratio = baseline.medianMs / target.medianMs
    return when {
        ratio >= 1 + SAME_SPEED_TOLERANCE -> "→ $targetLabel 가 $baselineLabel 보다 약 %.1fx 빠름 (중앙값 기준)".format(ratio)
        ratio <= 1 - SAME_SPEED_TOLERANCE -> "→ 이번 측정에선 $targetLabel 가 $baselineLabel 보다 약 %.1fx 느림 (중앙값 기준)".format(1 / ratio)
        else -> "→ $targetLabel 와 $baselineLabel 의 차이가 ±10% 이내 — 이 데이터 규모에선 차이 없음"
    }
}
