package com.example.composesample.example

import com.example.composesample.presentation.example.component.architecture.development.language.ClosedCopyAuthorization
import com.example.composesample.presentation.example.component.architecture.development.language.OpenCopyAuthorization
import com.example.composesample.presentation.example.component.architecture.development.language.Pan
import com.example.composesample.presentation.example.component.architecture.development.language.ParsedPan
import com.example.composesample.presentation.example.component.architecture.development.language.TEST_PAN
import com.example.composesample.presentation.example.component.architecture.development.language.CopyVisibilityProbe
import com.example.composesample.presentation.example.component.architecture.development.language.decodeRows
import com.example.composesample.presentation.example.component.architecture.development.language.maskingRows
import com.example.composesample.presentation.example.component.architecture.development.language.passesLuhn
import com.example.composesample.presentation.example.component.architecture.development.language.serializationRows
import com.example.composesample.presentation.example.component.architecture.development.language.timeoutAsException
import com.example.composesample.presentation.example.component.architecture.development.language.timeoutAsOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SensitiveTypesExample 의 실험 로직 검증 — 화면이 보여주는 행을 그대로 만든다.
 * given : 결제 테스트 카드번호(4111…)와 가림 value class Pan
 * when : 문자열로 만들기 / Gson 직렬화·역직렬화 / copy() / 타임아웃 처리
 * then : 어디서 가려지고 어디서 원문이 새는가, 검증이 어디서 우회되는가
 */
class SensitiveTypesTest {

    @Test
    fun `팩토리는 길이·숫자·체크 디지트를 순서대로 검사한다`() {
        assertTrue(passesLuhn(TEST_PAN))
        assertFalse(passesLuhn("4111111111111112"))
        assertTrue(Pan.parse(TEST_PAN) is ParsedPan.Valid)
        assertTrue(Pan.parse("4111 1111") is ParsedPan.Invalid.WrongLength)
        assertTrue(Pan.parse("41111111111111ab") is ParsedPan.Invalid.NotNumeric)
        assertTrue(Pan.parse("4111111111111112") is ParsedPan.Invalid.BadChecksum)
    }

    @Test
    fun `문자열이 되는 경로는 전부 가려지고 원문은 toNetwork 한 곳에서만 나온다`() {
        val rows = maskingRows()
        val leaking = rows.filter { it.leaks }.map { it.code }

        assertEquals(listOf("pan.toNetwork()"), leaking)
        // 원문이 나오는 유일한 줄은 의도된 출구로 표시된다
        assertTrue(rows.single { it.leaks }.intended)
        assertEquals("카드 PAN[****1111] 처리", rows[0].result)
        assertEquals("CardAuthRequest(pan=PAN[****1111], amountMinor=1200)", rows[1].result)
        assertEquals("[PAN[****1111]]", rows[3].result)
        // 거부 결과도 원문 타입(RawPan)으로 감싸 가린다
        assertEquals("BadChecksum(raw=RAW[****1112])", rows[5].result)
    }

    @Test
    fun `Gson 직렬화는 toString 을 거치지 않고 Pan 어댑터도 data class 필드에는 안 걸린다`() {
        val rows = serializationRows().associate { it.code to it.result }

        assertEquals("""{"pan":"4111111111111111","amountMinor":1200}""", rows["gson.toJson(request)"])
        assertEquals("""{"value":"4111111111111111"}""", rows["gson.toJson(pan)"])
        // 단독 Pan 에는 어댑터가 걸린다
        assertEquals("\"PAN[****1111]\"", rows["어댑터 등록 · toJson(pan)"])
        // 함정: 필드는 JVM 에서 String 이라 String 어댑터가 쓰인다
        assertEquals("""{"pan":"4111111111111111","amountMinor":1200}""", rows["어댑터 등록 · toJson(request)"])
        assertEquals("""{"pan":"4111111111111111","amount_minor":1200}""", rows["경계 DTO · toJson(request.toDto())"])
    }

    @Test
    fun `Gson 역직렬화는 팩토리를 건너뛰고 응답 DTO 와 팩토리를 거치면 거부된다`() {
        val rows = decodeRows().associateBy { it.raw }

        assertEquals("PAN[****11ab] 생성 · 재검사 거부(숫자 아님)", rows.getValue("41111111111111ab").direct)
        // 어댑터를 등록해도 필드에는 안 걸려 똑같이 들어온다
        assertEquals("PAN[****11ab] 생성 · 재검사 거부(숫자 아님)", rows.getValue("41111111111111ab").withAdapter)
        assertEquals("거부(숫자 아님)", rows.getValue("41111111111111ab").viaFactory)
        assertEquals("거부(길이)", rows.getValue("4111 1111").viaFactory)
        assertEquals("거부(체크 디지트)", rows.getValue("4111111111111112").viaFactory)
        assertEquals("통과", rows.getValue(TEST_PAN).viaFactory)
    }

    @Test
    fun `ExposedCopyVisibility 는 copy 가 public 이라 검증 없는 객체를 만들고 ConsistentCopyVisibility 는 막는다`() {
        assertEquals("public final", CopyVisibilityProbe.modifiers(OpenCopyAuthorization::class.java))
        assertEquals("private final", CopyVisibilityProbe.modifiers(ClosedCopyAuthorization::class.java))

        val open = OpenCopyAuthorization.approve("000001", 1_200)!!
        val closed = ClosedCopyAuthorization.approve("000001", 1_200)!!
        assertEquals("만들어짐: OpenCopyAuthorization(stan=000001, amountMinor=-500)", CopyVisibilityProbe.forgeAmount(open))
        assertEquals("막힘: IllegalAccessException", CopyVisibilityProbe.forgeAmount(closed))
        // 팩토리로는 같은 값을 만들 수 없다
        assertEquals(null, OpenCopyAuthorization.approve("000001", -500))
    }

    @Test
    fun `타임아웃을 예외로 잡고 재시도하면 두 번 승인되고 결과로 다루면 조회로 한 번만`() {
        val asException = timeoutAsException()
        assertEquals(2, asException.approvedAtAcquirer)
        assertEquals("STAN 000101 → GatewayTimeoutException", asException.steps[0])

        val asOutcome = timeoutAsOutcome()
        assertEquals(1, asOutcome.approvedAtAcquirer)
        assertEquals("재시도 대신 조회(000201) → Approved(authCode=AUTH-1)", asOutcome.steps[1])
    }
}
