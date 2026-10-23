package com.example.composesample.example

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import com.example.composesample.presentation.example.component.ui.media.picker.ACTION_PICK_IMAGES
import com.example.composesample.presentation.example.component.ui.media.picker.FailureKind
import com.example.composesample.presentation.example.component.ui.media.picker.GpsReading
import com.example.composesample.presentation.example.component.ui.media.picker.GpsState
import com.example.composesample.presentation.example.component.ui.media.picker.PickerTier
import com.example.composesample.presentation.example.component.ui.media.picker.classify
import com.example.composesample.presentation.example.component.ui.media.picker.describeFailure
import com.example.composesample.presentation.example.component.ui.media.picker.formatLatLong
import com.example.composesample.presentation.example.component.ui.media.picker.gpsState
import com.example.composesample.presentation.example.component.ui.media.picker.grantFlagsText
import com.example.composesample.presentation.example.component.ui.media.picker.shortExtraKey
import com.example.composesample.presentation.example.component.ui.media.picker.tierOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.FileNotFoundException
import java.lang.reflect.UndeclaredThrowableException

/**
 * SystemPhotoPickerExample 의 판정 로직 검증 — 화면이 쓰는 함수를 그대로 부른다.
 * given : contract 가 만든 인텐트 액션 / 피커 결과 플래그 / 피커 URI 로 읽은 EXIF GPS / 읽기 실패 예외
 * when : 단계 판정 · 플래그 표시 · GPS 상태 판정 · 예외 분류
 * then : 세 단계만 알아보고, 0 으로 지워진 GPS 는 '태그 없음' 과 다르게, 감싼 FileNotFoundException 도 그대로 가른다
 */
class SystemPhotoPickerTest {

    @Test
    fun `contract 가 고르는 단계는 액션 세 가지뿐이다`() {
        assertEquals("android.provider.action.PICK_IMAGES", ACTION_PICK_IMAGES)
        assertEquals(PickerTier.SYSTEM, tierOf(ACTION_PICK_IMAGES))
        assertEquals(PickerTier.SYSTEM_FALLBACK, tierOf(PickVisualMedia.ACTION_SYSTEM_FALLBACK_PICK_IMAGES))
        assertEquals(PickerTier.OPEN_DOCUMENT, tierOf(Intent.ACTION_OPEN_DOCUMENT))
        // 1.13.0 에 상수로만 남은 GMS 액션은 createIntent 가 만들지 않는다
        assertNull(tierOf("com.google.android.gms.provider.action.PICK_IMAGES"))
    }

    @Test
    fun `엑스트라 키는 접두어를 떼어 보인다`() {
        assertEquals("PICK_IMAGES_MAX", shortExtraKey("android.provider.extra.PICK_IMAGES_MAX"))
        assertEquals("PICK_IMAGES_IN_ORDER", shortExtraKey(PickVisualMedia.EXTRA_SYSTEM_FALLBACK_PICK_IMAGES_IN_ORDER))
        assertEquals("ALLOW_MULTIPLE", shortExtraKey(Intent.EXTRA_ALLOW_MULTIPLE))
        assertEquals("custom.KEY", shortExtraKey("custom.KEY"))
    }

    @Test
    fun `피커 결과 플래그 — 읽기와 영구 가능만 붙어 온다`() {
        // API 35 에뮬레이터 시스템 피커 결과 그대로
        val picker = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        assertEquals("READ | PERSISTABLE", grantFlagsText(picker))
        assertEquals("없음", grantFlagsText(0))
    }

    @Test
    fun `GPS — 피커가 0 으로 지운 값은 태그 없음과 다르게 판정한다`() {
        val original = GpsReading("37/1,33/1,5954/100", "N", "126/1,58/1,4066/100", "E", 37.56654 to 126.97796)
        assertEquals(GpsState.PRESENT, gpsState(original))
        assertEquals("37.56654, 126.97796", formatLatLong(original.latLong))

        // 피커 URI 로 읽은 같은 사진: 태그 자리는 남고 값·방향 글자만 0 으로(API 35 에뮬레이터 실측)
        val redacted = GpsReading("0/1,0/1,0/1", "", "0/1,0/1,0/1", "", 0.0 to 0.0)
        assertEquals(GpsState.ZEROED, gpsState(redacted))
        // getLatLong 이 false 를 돌려줘도(값 해석 실패) 태그가 있으면 지워진 쪽
        assertEquals(GpsState.ZEROED, gpsState(redacted.copy(latLong = null)))

        assertEquals(GpsState.ABSENT, gpsState(GpsReading(null, null, null, null, null)))
        assertEquals(GpsState.UNREADABLE, gpsState(null))
        assertEquals("없음", formatLatLong(null))
    }

    @Test
    fun `예외는 원인 사슬로 가른다 — debug 가 감싼 FileNotFoundException 도 그대로`() {
        val denied = SecurityException("Calling uid ( 10210 ) does not have permission to access picker uri: content://media/picker/0/com.android.providers.media.photopicker/media/1000000023")
        assertEquals(FailureKind.SECURITY, classify(denied))
        val missing = FileNotFoundException("No item at content://media/picker/0/…/media/1000000023")
        assertEquals(FailureKind.NOT_FOUND, classify(UndeclaredThrowableException(missing)))
        assertEquals("FileNotFoundException: No item at content://media/picker/0/…/media/1000000023", describeFailure(UndeclaredThrowableException(missing)))

        // 여러 장 한도 위반은 launch 안의 createIntent 에서 — 메시지는 activity 1.13.0 그대로
        val limit = IllegalArgumentException("Max items must be greater than 1 and lesser than or equal to MediaStore.getPickImagesMaxLimit()")
        assertEquals(FailureKind.ILLEGAL_ARGUMENT, classify(limit))
    }
}
