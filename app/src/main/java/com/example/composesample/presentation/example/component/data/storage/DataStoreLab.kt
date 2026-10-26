package com.example.composesample.presentation.example.component.data.storage

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream

/**
 * DataStore 예제의 순수 부분 — 타입 DataStore 가 쓸 값·직렬화기와 결과 표현. 화면·ViewModel·단위 테스트가 함께 쓴다.
 *
 * 타입 DataStore 는 Serializer 가 파일 형식을 정한다. 이 예제는 의존성을 늘리지 않으려고 손으로 쓴 텍스트 형식을 쓰고,
 * 읽을 수 없는 내용이면 CorruptionException 을 던져 DataStore 의 손상 처리기(ReThrow / ReplaceFile)로 넘긴다.
 */

/** 타입 DataStore 에 저장할 값 */
data class DemoSettings(val count: Int = 0, val label: String = "기본값")

/** "v1" 헤더 + key=value 줄 — 첫 줄이 다르거나 count 가 숫자가 아니면 손상으로 본다 */
fun formatSettings(settings: DemoSettings): String =
    "v1\ncount=${settings.count}\nlabel=${settings.label.replace('\n', ' ')}"

fun parseSettings(text: String): DemoSettings? {
    val lines = text.lines()
    if (lines.firstOrNull() != "v1") return null
    val values = lines.drop(1).mapNotNull { line -> line.split('=', limit = 2).takeIf { it.size == 2 }?.let { it[0] to it[1] } }.toMap()
    val count = values["count"]?.toIntOrNull() ?: return null
    return DemoSettings(count, values["label"].orEmpty())
}

object DemoSettingsSerializer : Serializer<DemoSettings> {
    override val defaultValue: DemoSettings = DemoSettings()

    override suspend fun readFrom(input: InputStream): DemoSettings {
        val text = input.readBytes().toString(Charsets.UTF_8)
        return parseSettings(text) ?: throw CorruptionException("읽을 수 없는 형식: \"${escapeControl(text.take(16))}\"")
    }

    override suspend fun writeTo(t: DemoSettings, output: OutputStream) {
        output.write(formatSettings(t).toByteArray(Charsets.UTF_8))
    }
}

/** 같은 값을 여러 코루틴이 동시에 1씩 올렸을 때 — 기대값과 실제값의 차이가 잃어버린 갱신 */
data class RaceResult(val name: String, val expected: Int, val actual: Int, val millis: Long) {
    val lost: Int get() = expected - actual
}

/** 쓰기 비용 측정 한 줄 */
data class CostResult(val name: String, val writes: Int, val diskWrites: String, val millis: Long)

/** 제어 문자를 \xNN 으로 — 깨진 파일 내용(NUL 등)을 그대로 Text 에 넣으면 접근성 트리 XML 덤프가 깨진다(실측) */
fun escapeControl(text: String): String = buildString {
    text.forEach { c ->
        when {
            c == '\n' -> append(" · ")
            c.code < 0x20 || c.code == 0x7F -> append("\\x%02X".format(c.code))
            else -> append(c)
        }
    }
}

/** 예외를 원인 사슬까지 한 줄로 — FileStorage 는 직렬화기의 CorruptionException 을 파일 경로가 든 CorruptionException 으로 한 번 더 감싼다 */
fun describeFailure(throwable: Throwable): String =
    generateSequence(throwable) { it.cause }.take(4).joinToString(" ← ") { e ->
        // RuntimeException(cause) 처럼 메시지가 원인의 toString() 이면 되풀이하지 않는다
        val message = e.message?.takeUnless { it == e.cause?.toString() }
        if (message.isNullOrEmpty()) e.javaClass.simpleName else "${e.javaClass.simpleName}: ${escapeControl(message)}"
    }
