package com.example.composesample.presentation.example.component.system.background.workmanager

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkInfo
import java.util.UUID
import kotlin.math.abs

/**
 * 주기 작업 재등록 정책 탭의 순수 로직 — 화면·ViewModel·단위 테스트가 함께 쓴다.
 *
 * 같은 이름의 주기 작업을 "앱 업데이트에서 사양을 바꿔" 다시 등록할 때 정책마다 무엇이 바뀌는지를
 * WorkInfo 스냅샷과 워커가 남긴 실행 기록으로 판정한다. WorkManager 를 부르는 코드는 여기에 두지 않는다.
 */

/** 정책 하나 = 고유 이름 하나. 세 정책을 서로 다른 이름에서 동시에 돌려 나란히 비교한다. */
enum class PolicyLane(
    val label: String,
    val policy: ExistingPeriodicWorkPolicy,
    val uniqueName: String
) {
    KEEP("KEEP", ExistingPeriodicWorkPolicy.KEEP, "periodic-policy-keep"),
    UPDATE("UPDATE", ExistingPeriodicWorkPolicy.UPDATE, "periodic-policy-update"),
    CANCEL_AND_REENQUEUE(
        "CANCEL_AND_REENQUEUE",
        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
        "periodic-policy-cancel"
    );

    companion object {
        fun fromUniqueName(name: String?): PolicyLane? = entries.firstOrNull { it.uniqueName == name }
    }
}

/** 앱 버전별 작업 사양. v1 앱은 15분으로 등록했고, 업데이트한 v2 앱은 30분으로 바꿨다고 가정한다. */
enum class PeriodicSpec(val label: String, val intervalMinutes: Long) {
    V1("v1", 15),
    V2("v2", 30);

    /** WorkInfo 에는 inputData 가 없어서(완료 전에는 outputData 도 비어 있다) 사양을 태그로도 싣는다 */
    val tag: String get() = "$SPEC_TAG_PREFIX$label"

    companion object {
        fun fromLabel(label: String?): PeriodicSpec? = entries.firstOrNull { it.label == label }
        fun fromTags(tags: Set<String>): PeriodicSpec? = entries.firstOrNull { it.tag in tags }
    }
}

const val SPEC_TAG_PREFIX = "periodic-policy-spec:"

enum class RunOutcome(val label: String) {
    RUNNING("실행 중"),
    FINISHED("완료"),
    STOPPED("중단")
}

/** 워커가 doWork 안에서 남기는 실행 기록 한 건 */
data class RunRecord(
    val lane: PolicyLane,
    val workId: UUID,
    /** 워커가 받은 WorkerParameters.generation — UPDATE 직후 실행 중인 워커는 WorkInfo 보다 이전 세대일 수 있다 */
    val generation: Int,
    /** 워커가 받은 inputData 의 사양 — 이번 실행이 어느 버전의 요청으로 돌았는가 */
    val spec: PeriodicSpec,
    val startedAt: Long,
    val endedAt: Long? = null,
    val outcome: RunOutcome = RunOutcome.RUNNING,
    /** API 31+ 에서만 읽힌다(ListenableWorker.getStopReason) */
    val stopReason: Int? = null
)

/** 한 시점의 WorkInfo 에서 비교에 필요한 값만 뽑은 것 */
data class LaneSnapshot(
    val lane: PolicyLane,
    /** "v1 첫 실행 뒤" 처럼 시나리오 안의 시점 이름 */
    val label: String,
    val takenAt: Long,
    /** 같은 고유 이름으로 조회된 WorkInfo 개수 */
    val infoCount: Int,
    val workId: UUID?,
    val state: WorkInfo.State?,
    val generation: Int?,
    val intervalMillis: Long?,
    /** ENQUEUED 일 때만 값이 있다(그 밖의 상태는 WorkInfo 가 Long.MAX_VALUE 를 준다) */
    val nextScheduleAt: Long?,
    /** 태그로 본 사양 — UPDATE 는 태그도 새 요청의 것으로 바꾼다 */
    val spec: PeriodicSpec?
)

/** 즉시 실행 몫이 끝나 다음 실행이 '주기 뒤'로 밀려났는가 — 시나리오가 다음 단계로 넘어가는 조건 */
fun LaneSnapshot.isSettled(now: Long, runs: List<RunRecord>): Boolean {
    val next = nextScheduleAt ?: return false
    return state == WorkInfo.State.ENQUEUED &&
        next - now > SETTLE_MARGIN_MILLIS &&
        runs.none { it.lane == lane && it.outcome == RunOutcome.RUNNING }
}

/**
 * 다음 실행 시각 − 주기 = 주기를 세기 시작한 시각.
 *
 * 주기 작업은 한 번 돌고 나면 끝난 시각(lastEnqueueTime)부터 다시 센다. 그 시각과 가장 가까운 실행 끝을
 * 실행 기록에서 찾아 "다음 실행이 어느 실행을 기준으로 잡혔는지"를 밝힌다. 아직 한 번도 안 돌았으면 null.
 */
fun LaneSnapshot.periodBaseRun(runs: List<RunRecord>): RunRecord? {
    val next = nextScheduleAt ?: return null
    val interval = intervalMillis ?: return null
    val base = next - interval
    return runs
        .filter { it.lane == lane && it.endedAt != null }
        .minByOrNull { abs(it.endedAt!! - base) }
        ?.takeIf { abs(it.endedAt!! - base) <= BASE_MATCH_TOLERANCE_MILLIS }
}

/** 한 정책의 시나리오 결과를 첫 스냅샷 ↔ 마지막 스냅샷 + 실행 기록으로 요약한 것 */
data class LaneVerdict(
    val lane: PolicyLane,
    val distinctIds: Int,
    val firstIntervalMinutes: Long?,
    val lastIntervalMinutes: Long?,
    val firstGeneration: Int?,
    val lastGeneration: Int?,
    val runsBySpec: Map<PeriodicSpec, Int>,
    val stoppedRuns: Int,
    val baseRun: RunRecord?
) {
    val idChanged: Boolean get() = distinctIds > 1
}

fun verdictOf(lane: PolicyLane, snapshots: List<LaneSnapshot>, runs: List<RunRecord>): LaneVerdict? {
    val mine = snapshots.filter { it.lane == lane }
    val first = mine.firstOrNull() ?: return null
    val last = mine.last()
    val laneRuns = runs.filter { it.lane == lane }
    return LaneVerdict(
        lane = lane,
        // 시점마다 본 id 와 실행 기록의 id 를 합쳐 센다 — 사이에 새 id 가 생겼다 사라져도 놓치지 않게
        distinctIds = (mine.mapNotNull { it.workId } + laneRuns.map { it.workId }).toSet().size,
        firstIntervalMinutes = first.intervalMillis?.let { it / 60_000 },
        lastIntervalMinutes = last.intervalMillis?.let { it / 60_000 },
        firstGeneration = first.generation,
        lastGeneration = last.generation,
        runsBySpec = laneRuns.groupingBy { it.spec }.eachCount(),
        stoppedRuns = laneRuns.count { it.outcome == RunOutcome.STOPPED },
        baseRun = last.periodBaseRun(laneRuns)
    )
}

/** 결과 블록 맨 위의 한 줄 요약 */
fun LaneVerdict.summary(): String {
    val id = if (idChanged) "id 바뀜(${distinctIds}개)" else "id 같음"
    val interval = when {
        firstIntervalMinutes == null || lastIntervalMinutes == null -> "주기 ?"
        firstIntervalMinutes == lastIntervalMinutes -> "주기 ${lastIntervalMinutes}분"
        else -> "주기 ${firstIntervalMinutes}→${lastIntervalMinutes}분"
    }
    val generation = if (firstGeneration == lastGeneration) "gen $lastGeneration" else "gen $firstGeneration→$lastGeneration"
    val runs = PeriodicSpec.entries
        .filter { (runsBySpec[it] ?: 0) > 0 }
        .joinToString(", ") { "${it.label} ${runsBySpec[it]}회" }
        .ifEmpty { "0회" }
    val stopped = if (stoppedRuns > 0) " (중단 ${stoppedRuns}회)" else ""
    val base = baseRun?.let { " · 다음 실행 기준 = ${it.spec.label} 실행 끝" } ?: ""
    return "$id · $interval · $generation · 실행 $runs$stopped$base"
}

/** "14분 59초 뒤" — 다음 실행까지 남은 시간 */
fun formatRemaining(millis: Long): String {
    if (millis <= 0) return "지금 실행 가능"
    val totalSeconds = millis / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "${minutes}분 ${seconds}초 뒤" else "${seconds}초 뒤"
}

fun UUID.short(): String = toString().take(8)

/** 다음 실행이 이만큼보다 멀면 '즉시 실행 몫은 끝났다'고 본다(가장 짧은 주기 15분보다 훨씬 작게) */
const val SETTLE_MARGIN_MILLIS = 60_000L

/** lastEnqueueTime 은 워커가 끝난 직후 WorkManager 가 기록한다 — 워커가 남긴 끝 시각과의 차이 허용치 */
const val BASE_MATCH_TOLERANCE_MILLIS = 3_000L
