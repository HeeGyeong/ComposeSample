package com.example.composesample.presentation.example.component.system.platform.exitinfo

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * '프로세스 종료 사후 분석' 예제의 순수 로직 — 화면·ViewModel·단위 테스트가 함께 쓴다.
 *
 * 앱이 왜 죽었는지는 죽는 순간이 아니라 **다음 실행에서** ApplicationExitInfo(API 30)로 읽힌다.
 * 이 예제는 일부러 프로세스를 끝내기 전에 '무엇을 할지(실험)'와 pid 를 남겨 두고, 다음 실행에서 같은 pid 의
 * 시스템 종료 기록과 맞춰 본다. 종료 이유 이름은 절전 모드 예제의 [com.example.composesample.presentation.example.component.system.platform.powersave.ExitReason]
 * 값 미러를 함께 쓴다.
 */

/** ApplicationExitInfo.setProcessStateSummary 의 한도(javadoc: Maximum length is 128 bytes) */
const val MAX_STATE_SUMMARY_BYTES = 128

/** 일부러 프로세스를 끝내는 실험 네 가지 */
enum class ExperimentKind(val label: String, val how: String) {
    ANR("ANR", "메인 스레드를 30초 막는다 — 버튼 터치의 처리가 끝나지 않아 5초(삼성 10초) 뒤 '응답 없음' 대화상자, 앱 닫기를 누른다"),
    CRASH("크래시", "메인 스레드에서 IllegalStateException 을 던진다(앱의 기본 처리기가 먼저 기록)"),
    EXIT_SELF("System.exit", "kotlin.system.exitProcess(0)"),
    KILL_SELF("killProcess", "Process.killProcess(Process.myPid())");

    companion object {
        fun fromName(name: String): ExperimentKind? = entries.firstOrNull { it.name == name }
    }
}

/** 끝내기 직전에 남긴 실험 기록 — 다음 실행에서 같은 pid 의 시스템 기록과 맞춘다 */
data class Experiment(val kind: ExperimentKind, val pid: Int, val triggeredAt: Long, val summary: String)

/** 앱의 기본 처리기(UncaughtExceptionHandler)가 죽기 직전에 남긴 크래시 기록 */
data class CrashRecord(val pid: Int, val at: Long, val thread: String, val exception: String, val message: String)

/** ApplicationExitInfo 에서 옮긴 값 + 읽어 낸 트레이스 요약 */
data class ExitSnapshot(
    val pid: Int,
    val reason: Int,
    val status: Int,
    val timestamp: Long,
    val processName: String,
    val importance: Int,
    val pssKb: Long,
    val rssKb: Long,
    val description: String?,
    /** setProcessStateSummary 로 남긴 바이트를 UTF-8 로 읽은 것 */
    val summary: String?,
    /** getTraceInputStream 이 준 바이트 수(null = 트레이스 없음) */
    val traceBytes: Int?,
    /** ANR 트레이스의 "main" 스레드 스택 앞부분 */
    val mainStack: List<String> = emptyList()
)

// ==================== 기록 직렬화(SharedPreferences 한 줄) ====================

private const val SEP = '\u001F'

fun encodeExperiment(e: Experiment): String = listOf(e.kind.name, e.pid, e.triggeredAt, e.summary).joinToString(SEP.toString())

fun decodeExperiment(line: String): Experiment? {
    val parts = line.split(SEP)
    if (parts.size != 4) return null
    val kind = ExperimentKind.fromName(parts[0]) ?: return null
    return Experiment(kind, parts[1].toIntOrNull() ?: return null, parts[2].toLongOrNull() ?: return null, parts[3])
}

fun encodeCrash(c: CrashRecord): String = listOf(c.pid, c.at, c.thread, c.exception, c.message).joinToString(SEP.toString()) { it.toString().replace('\n', ' ') }

fun decodeCrash(line: String): CrashRecord? {
    val parts = line.split(SEP)
    if (parts.size != 5) return null
    return CrashRecord(parts[0].toIntOrNull() ?: return null, parts[1].toLongOrNull() ?: return null, parts[2], parts[3], parts[4])
}

// ==================== 대조·트레이스 ====================

/**
 * 실험에 해당하는 시스템 기록 — pid 가 같고 실험 뒤에 끝난 기록. pid 는 재사용될 수 있어 시각도 본다
 * (기록 시각은 실험보다 늦어야 한다 — 같은 ms 안의 오차만 허용).
 */
fun matchExit(experiment: Experiment, exits: List<ExitSnapshot>): ExitSnapshot? =
    exits.firstOrNull { it.pid == experiment.pid && it.timestamp >= experiment.triggeredAt - 1_000 }

/**
 * 같은 pid 에서 나중에 또 실험했다면 이 실험으로는 프로세스가 끝나지 않은 것이다
 * (ANR 대화상자에서 '기다리기'를 골라 살아남은 경우 등) — 종료 기록은 마지막 실험에만 붙인다.
 */
fun isSuperseded(experiment: Experiment, experiments: List<Experiment>): Boolean =
    experiments.any { it.pid == experiment.pid && it.triggeredAt > experiment.triggeredAt }

/**
 * ANR 트레이스(텍스트)에서 "main" 스레드 블록의 스택 줄만 앞에서부터 [maxLines] 개.
 * 형식: `"main" prio=5 tid=1 Sleeping` 다음 줄들 중 `at `·`- `·`native:` 로 시작하는 줄, 빈 줄에서 끝난다.
 */
fun mainThreadStack(trace: String, maxLines: Int = 14): List<String> {
    val lines = trace.lines()
    val start = lines.indexOfFirst { it.startsWith("\"main\"") }
    if (start < 0) return emptyList()
    return lines.drop(start + 1)
        .takeWhile { it.isNotBlank() }
        .map { it.trim() }
        .filter { it.startsWith("at ") || it.startsWith("- ") || it.startsWith("native:") }
        .take(maxLines)
}

/** 이 앱 패키지의 프레임 줄 수 — 앱 코드가 스택에 보이는지 */
fun appFrameCount(stack: List<String>, packagePrefix: String): Int = stack.count { it.startsWith("at $packagePrefix") }

/** 이 예제의 함수가 찍힌 줄 번호(1부터, 없으면 null) — debug(HotSwan)·release 에서 다르게 나올 수 있다 */
fun markerLine(stack: List<String>, marker: String): Int? = stack.indexOfFirst { it.contains(marker) }.takeIf { it >= 0 }?.plus(1)

// ==================== 상태 요약 ====================

fun utf8Size(text: String): Int = text.toByteArray(Charsets.UTF_8).size

/** 실험 직전에 남길 상태 요약 — ASCII 로 써서 128바이트 안에 넉넉히 들어가게 한다 */
fun summaryOf(kind: ExperimentKind, pid: Int, at: Long): String =
    "cs159|${kind.name}|pid=$pid|${SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(at))}"

/** 한도 실험용 문자열 — ASCII 는 글자당 1바이트, 한글은 3바이트라 같은 바이트에 글자 수가 1/3 이다 */
fun probeText(bytes: Int, korean: Boolean): String =
    if (korean) "가".repeat(bytes / 3) + "x".repeat(bytes % 3) else "x".repeat(bytes)

fun formatTime(millis: Long): String = SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(Date(millis))
