package com.example.composesample.example

import com.example.composesample.presentation.example.component.ui.media.picker.GPS_TAG_NAMES
import com.example.composesample.presentation.example.component.ui.media.picker.GpsReading
import com.example.composesample.presentation.example.component.ui.media.picker.OrientationTransform
import com.example.composesample.presentation.example.component.ui.media.picker.displayedSize
import com.example.composesample.presentation.example.component.ui.media.picker.naiveLocationText
import com.example.composesample.presentation.example.component.ui.media.picker.orientationText
import com.example.composesample.presentation.example.component.ui.media.picker.orientationTransform
import com.example.composesample.presentation.example.component.ui.media.picker.pngExifCrcOk
import com.example.composesample.presentation.example.component.ui.media.picker.printable
import com.example.composesample.presentation.example.component.ui.media.picker.sampleSizeFor
import com.example.composesample.presentation.example.component.ui.media.picker.usableLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.zip.CRC32

/**
 * SystemPhotoPickerExample 6번 카드(올리기 전 위치 정리)의 판정 로직 검증 — 화면이 쓰는 함수를 그대로 부른다.
 * given : 피커가 0 으로 덮은 GPS / 원본 GPS / 지울 GPS 태그 이름 / EXIF Orientation / 원본 픽셀 크기
 * when : 올릴 위치 판정 · 태그 이름 대조 · 방향 변환 · inSampleSize 계산
 * then : 0 위치는 getLatLong 이 true 여도 '위치 없음', 방향 6 은 가로·세로가 바뀌고, 재인코딩 상한은 2의 거듭제곱으로 맞춘다
 */
class LocationCleanupLabTest {

    @Test
    fun `0 으로 덮인 GPS 는 getLatLong 이 true 여도 올릴 위치가 없다`() {
        // 피커 URI 로 읽은 사진: 태그 자리는 남고 값·방향 글자만 0 으로(API 35 에뮬레이터 실측, 5번 카드)
        val redacted = GpsReading("0/1,0/1,0/1", "", "0/1,0/1,0/1", "", 0.0 to 0.0)
        assertEquals("true (0.00000, 0.00000)", naiveLocationText(redacted))
        assertNull(usableLocation(redacted))

        val original = GpsReading("37/1,33/1,5954/100", "N", "126/1,58/1,4066/100", "E", 37.56654 to 126.97796)
        assertEquals(37.56654 to 126.97796, usableLocation(original))

        // 태그를 지운 사본·재인코딩한 사본
        val absent = GpsReading(null, null, null, null, null)
        assertEquals("false", naiveLocationText(absent))
        assertNull(usableLocation(absent))
        assertNull(usableLocation(null))
    }

    @Test
    fun `BYTE 태그의 제어 문자는 xNN 으로 보인다`() {
        // 원본 GPSVersionID 2.2.0.0 과 피커가 0 으로 덮은 값(NUL 네 글자)
        assertEquals("\\x02\\x02\\x00\\x00", printable("\u0002\u0002\u0000\u0000"))
        assertEquals("\\x00\\x00\\x00\\x00", printable("\u0000\u0000\u0000\u0000"))
        assertEquals("0/1,0/1,0/1", printable("0/1,0/1,0/1"))
    }

    @Test
    fun `PNG eXIf CRC — 피커처럼 GPS 바이트만 0 으로 바꾸면 CRC 가 어긋난다`() {
        val exif = "MM".toByteArray() + byteArrayOf(0, 42, 0, 0, 0, 8) + "GPS 37/1,33/1".toByteArray()
        val png = png(listOf("IHDR" to ByteArray(13), "eXIf" to exif, "IDAT" to ByteArray(4), "IEND" to ByteArray(0)))
        assertTrue(pngExifCrcOk(png)!!)

        // eXIf 데이터 안 한 바이트를 0 으로(청크 CRC 는 그대로) — API 35 피커가 돌려준 PNG 와 같은 모양
        val redacted = png.copyOf().also { it[8 + 25 + 8 + 10] = 0 }
        assertFalse(pngExifCrcOk(redacted)!!)

        // eXIf 청크가 없는 PNG(Bitmap.compress) · PNG 가 아닌 파일
        assertNull(pngExifCrcOk(png(listOf("IHDR" to ByteArray(13), "IEND" to ByteArray(0)))))
        assertNull(pngExifCrcOk(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE1.toByte())))
    }

    private fun png(chunks: List<Pair<String, ByteArray>>): ByteArray = ByteArrayOutputStream().apply {
        write(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
        chunks.forEach { (type, data) ->
            val typeBytes = type.toByteArray(Charsets.US_ASCII)
            write(ByteBuffer.allocate(4).putInt(data.size).array())
            write(typeBytes)
            write(data)
            val crc = CRC32().apply { update(typeBytes); update(data) }.value
            write(ByteBuffer.allocate(4).putInt(crc.toInt()).array())
        }
    }.toByteArray()

    @Test
    fun `지울 GPS 태그 이름은 프레임워크 TAG_GPS 상수 31개와 같다`() {
        val framework = listOf(
            android.media.ExifInterface.TAG_GPS_ALTITUDE, android.media.ExifInterface.TAG_GPS_ALTITUDE_REF,
            android.media.ExifInterface.TAG_GPS_AREA_INFORMATION, android.media.ExifInterface.TAG_GPS_DOP,
            android.media.ExifInterface.TAG_GPS_DATESTAMP, android.media.ExifInterface.TAG_GPS_DEST_BEARING,
            android.media.ExifInterface.TAG_GPS_DEST_BEARING_REF, android.media.ExifInterface.TAG_GPS_DEST_DISTANCE,
            android.media.ExifInterface.TAG_GPS_DEST_DISTANCE_REF, android.media.ExifInterface.TAG_GPS_DEST_LATITUDE,
            android.media.ExifInterface.TAG_GPS_DEST_LATITUDE_REF, android.media.ExifInterface.TAG_GPS_DEST_LONGITUDE,
            android.media.ExifInterface.TAG_GPS_DEST_LONGITUDE_REF, android.media.ExifInterface.TAG_GPS_DIFFERENTIAL,
            android.media.ExifInterface.TAG_GPS_IMG_DIRECTION, android.media.ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
            android.media.ExifInterface.TAG_GPS_LATITUDE, android.media.ExifInterface.TAG_GPS_LATITUDE_REF,
            android.media.ExifInterface.TAG_GPS_LONGITUDE, android.media.ExifInterface.TAG_GPS_LONGITUDE_REF,
            android.media.ExifInterface.TAG_GPS_MAP_DATUM, android.media.ExifInterface.TAG_GPS_MEASURE_MODE,
            android.media.ExifInterface.TAG_GPS_PROCESSING_METHOD, android.media.ExifInterface.TAG_GPS_SATELLITES,
            android.media.ExifInterface.TAG_GPS_SPEED, android.media.ExifInterface.TAG_GPS_SPEED_REF,
            android.media.ExifInterface.TAG_GPS_STATUS, android.media.ExifInterface.TAG_GPS_TIMESTAMP,
            android.media.ExifInterface.TAG_GPS_TRACK, android.media.ExifInterface.TAG_GPS_TRACK_REF,
            android.media.ExifInterface.TAG_GPS_VERSION_ID,
        )
        assertEquals(31, GPS_TAG_NAMES.size)
        assertEquals(framework.toSet(), GPS_TAG_NAMES.toSet())
    }

    @Test
    fun `Orientation — 뒤집은 다음 돌리고, 90도 270도면 가로 세로가 바뀐다`() {
        // androidx ExifInterface getRotationDegrees · isFlipped 와 같은 표
        assertEquals(OrientationTransform(90, false), orientationTransform(6))
        assertEquals(OrientationTransform(270, false), orientationTransform(8))
        assertEquals(OrientationTransform(270, true), orientationTransform(5))
        assertEquals(OrientationTransform(90, true), orientationTransform(7))
        assertEquals(OrientationTransform(180, true), orientationTransform(4))
        assertEquals(OrientationTransform(0, false), orientationTransform(null))
        assertEquals(OrientationTransform(0, false), orientationTransform(0))

        // 데모 사진: 1600×1200 으로 저장 + Orientation 6 → 뷰어에는 1200×1600 세로
        assertEquals(1200 to 1600, displayedSize(1600, 1200, 6))
        // 재인코딩해 태그를 잃으면 저장된 그대로 가로
        assertEquals(1600 to 1200, displayedSize(1600, 1200, null))

        assertEquals("6 (시계 90°)", orientationText(6))
        assertEquals("5 (좌우 반전 + 시계 270°)", orientationText(5))
        assertEquals("1 (그대로)", orientationText(1))
        // 프레임워크 ExifInterface 는 Orientation 태그가 없으면 0 을 채워 돌려준다(addDefaultValuesForCompatibility)
        assertEquals("0 (정의 안 됨 · 태그가 없어도 프레임워크가 이 값을 채운다)", orientationText(0))
        assertEquals("태그 없음(= 그대로)", orientationText(null))
    }

    @Test
    fun `inSampleSize — 긴 변이 상한 이하가 되는 가장 작은 2의 거듭제곱`() {
        assertEquals(1, sampleSizeFor(1600, 1200, 4096))
        // 50MP(8160×6120) 원본은 반으로 줄여 디코딩
        assertEquals(2, sampleSizeFor(8160, 6120, 4096))
        // 미리보기: 1600/4 = 400 은 240 을 넘고 1600/8 = 200 은 넘지 않는다
        assertEquals(8, sampleSizeFor(1600, 1200, 240))
        assertEquals(1, sampleSizeFor(0, 0, 240))
    }
}
