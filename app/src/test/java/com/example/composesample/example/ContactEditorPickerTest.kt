package com.example.composesample.example

import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Photo
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.Website
import com.example.composesample.presentation.example.component.system.platform.contacts.CUSTOM_MIME
import com.example.composesample.presentation.example.component.system.platform.contacts.DEMO_WEBSITE
import com.example.composesample.presentation.example.component.system.platform.contacts.DataRow
import com.example.composesample.presentation.example.component.system.platform.contacts.EditorAction
import com.example.composesample.presentation.example.component.system.platform.contacts.EditorRequest
import com.example.composesample.presentation.example.component.system.platform.contacts.FailureKind
import com.example.composesample.presentation.example.component.system.platform.contacts.FieldOutcome
import com.example.composesample.presentation.example.component.system.platform.contacts.PickedRow
import com.example.composesample.presentation.example.component.system.platform.contacts.checkFields
import com.example.composesample.presentation.example.component.system.platform.contacts.classify
import com.example.composesample.presentation.example.component.system.platform.contacts.demoName
import com.example.composesample.presentation.example.component.system.platform.contacts.demoPhone
import com.example.composesample.presentation.example.component.system.platform.contacts.describeFailure
import com.example.composesample.presentation.example.component.system.platform.contacts.groupPicked
import com.example.composesample.presentation.example.component.system.platform.contacts.isDemoName
import com.example.composesample.presentation.example.component.system.platform.contacts.sameName
import com.example.composesample.presentation.example.component.system.platform.contacts.writeRequestable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.UndeclaredThrowableException

/**
 * ContactEditorPickerExample 의 판정 로직 검증 — 화면 기록 행이 쓰는 함수를 그대로 부른다.
 * given : 편집기로 보낸 요청과 저장 뒤 읽은 data 행 / 피커 결과 커서 행 / 쓰기·조회 실패 예외
 * when : 남은 항목 대조 · LOOKUP_KEY 로 묶기 · 예외 분류 · maxSdkVersion 판정
 * then : 표준 밖 MIME·사진 행은 '사라짐', 3명 4행은 3명으로, 감싼 SecurityException 도 권한 실패로 가른다
 */
class ContactEditorPickerTest {

    private fun row(mime: String, data1: String?, name: String = "CS152편집기 2031", blob: Boolean = false) =
        DataRow(7, "0r7-key", name, mime, data1, blob, accountType = null, accountName = null)

    private val insert = EditorRequest(
        id = 2031, action = EditorAction.INSERT, name = demoName("편집기", 2031), phone = demoPhone(2031),
        withWebsite = true, withCustomMime = true, withPhoto = true, finishOnSave = false,
    )

    @Test
    fun `provider 가 바꾼 표시 이름도 예제 연락처로 알아보고 같은 이름으로 본다`() {
        assertEquals("CS152 편집기 2031", insert.name)
        // 편집기·provider 가 성 'CS152' + 이름 '편집기 2031' 로 나눠 다시 붙인 표시 이름
        assertTrue(isDemoName("CS152편집기 2031"))
        assertTrue(sameName("CS152편집기 2031", insert.name))
        assertFalse(isDemoName("홍길동"))
        assertFalse(sameName(null, insert.name))
    }

    @Test
    fun `Google 편집기 저장 결과 — 이름·전화·웹사이트는 남고 표준 밖 MIME 과 사진 행은 사라짐`() {
        // API 35·37 에뮬레이터 Google 연락처로 저장한 뒤 읽은 행 그대로(전화번호 형식은 편집기가 바꿀 수 있어 숫자만 비교)
        val saved = listOf(
            row(StructuredName.CONTENT_ITEM_TYPE, "CS152편집기 2031"),
            row(Phone.CONTENT_ITEM_TYPE, "010 1520 2031"),
            row(Website.CONTENT_ITEM_TYPE, DEMO_WEBSITE),
        )
        val outcomes = checkFields(insert, saved).associate { it.label to it.outcome }
        assertEquals(FieldOutcome.KEPT, outcomes["이름"])
        assertEquals(FieldOutcome.KEPT, outcomes["전화번호"])
        assertEquals(FieldOutcome.KEPT, outcomes["웹사이트(표준 행, 대조군)"])
        assertEquals(FieldOutcome.DROPPED, outcomes["표준 밖 MIME 행"])
        assertEquals(FieldOutcome.DROPPED, outcomes["사진 행"])

        val withAll = saved + row(CUSTOM_MIME, "custom 2031") + row(Photo.CONTENT_ITEM_TYPE, null, blob = true)
        assertTrue(checkFields(insert, withAll).all { it.outcome == FieldOutcome.KEPT })
    }

    @Test
    fun `기존 연락처에 번호만 더한 요청은 보내지 않은 항목을 '안 보냄' 으로 둔다`() {
        val edit = insert.copy(action = EditorAction.EDIT, name = null, withWebsite = false, withCustomMime = false, withPhoto = false)
        val outcomes = checkFields(edit, listOf(row(Phone.CONTENT_ITEM_TYPE, demoPhone(2031)))).map { it.outcome }
        assertEquals(listOf(FieldOutcome.NOT_SENT, FieldOutcome.KEPT, FieldOutcome.NOT_SENT, FieldOutcome.NOT_SENT, FieldOutcome.NOT_SENT), outcomes)
    }

    @Test
    fun `피커 결과는 필드 하나당 한 행 — 3명 4행을 LOOKUP_KEY 로 3명으로 묶는다`() {
        val rows = listOf(
            PickedRow("k1", "CS152편집기 2", Phone.CONTENT_ITEM_TYPE, "010-1520-2002"),
            PickedRow("k2", "CS152시드 가", Phone.CONTENT_ITEM_TYPE, "010-1520-9001"),
            PickedRow("k2", "CS152시드 가", Email.CONTENT_ITEM_TYPE, "ga@cs152.example"),
            PickedRow("k3", "CS152시드 나", Email.CONTENT_ITEM_TYPE, "na@cs152.example"),
        )
        val grouped = groupPicked(rows)
        assertEquals(listOf("k1", "k2", "k3"), grouped.map { it.lookupKey })
        assertEquals(listOf("010-1520-9001"), grouped[1].phones)
        assertEquals(listOf("ga@cs152.example"), grouped[1].emails)
        // 레거시 연락처 URI 결과 — MIMETYPE·DATA1 열이 없는 한 행
        val legacy = groupPicked(listOf(PickedRow("k9", "CS152시드 나", null, null)))
        assertEquals(1, legacy.size)
        assertTrue(legacy[0].phones.isEmpty() && legacy[0].emails.isEmpty())
    }

    @Test
    fun `예외는 원인 사슬로 가른다 — debug 가 감싼 SecurityException 도 권한 실패`() {
        val denied = SecurityException("Permission Denial: writing com.android.providers.contacts.ContactsProvider2 uri …\nRemote stack trace:")
        assertEquals(FailureKind.SECURITY, classify(denied))
        assertEquals(FailureKind.SECURITY, classify(UndeclaredThrowableException(denied)))
        // 감싼 예외는 건너뛰고 시스템 메시지의 첫 줄만 보인다
        assertEquals(
            "SecurityException: Permission Denial: writing com.android.providers.contacts.ContactsProvider2 uri …",
            describeFailure(UndeclaredThrowableException(denied))
        )
        val selection = UnsupportedOperationException("Querying Session Uri with selection or selection arguments is not allowed.")
        assertEquals(FailureKind.UNSUPPORTED, classify(selection))
    }

    @Test
    fun `WRITE_CONTACTS 는 maxSdkVersion 36 까지만 요청된다`() {
        assertTrue(writeRequestable(33))
        assertTrue(writeRequestable(36))
        assertFalse(writeRequestable(37))
        assertEquals("010-1520-0007", demoPhone(7))
        assertEquals("010-1520-2031", demoPhone(12031))
    }
}
