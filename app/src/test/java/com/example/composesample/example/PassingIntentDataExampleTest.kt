package com.example.composesample.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.composesample.presentation.example.component.system.platform.intent.ParcelableDataClass
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * given : 테스트를 위한 사전 준비를 설정하는 단계
 * when : 실제로 테스트하고자 하는 동작을 실행하는 단계
 * then : 실행 결과를 검증하는 단계
 */
class PassingIntentDataExampleTest {
    // Context 모킹을 위한 변수
    private lateinit var mockContext: Context
    // Intent 캡처를 위한 슬롯 변수
    private lateinit var intentSlot: CapturingSlot<Intent>

    @Before
    fun setup() {
        // Context 모킹 생성 (relaxed = true로 미구현 메서드도 null 반환)
        mockContext = mockk(relaxed = true)
        intentSlot = slot()
        every { mockContext.startActivity(capture(intentSlot)) } just Runs
    }

    @Test
    fun `Type 1 - Intent에 데이터가 올바르게 전달되는지 확인`() {
        // Given
        val intent = mockk<Intent>(relaxed = true)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().setFlags(any()) } returns intent

        // When
        intent.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("BooleanData", true)
            putExtra("NumberData", 100)
            putExtra("StringData", "String Data")
            putExtra(
                "ParcelableData", ParcelableDataClass(
                    name = "Parcelable Data",
                    age = 10,
                    isMale = true,
                    address = "Address Data"
                )
            )
        }
        mockContext.startActivity(intent)

        // Then
        verify {
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("BooleanData", true)
            intent.putExtra("NumberData", 100)
            intent.putExtra("StringData", "String Data")
            intent.putExtra("ParcelableData", any<ParcelableDataClass>())
        }
    }

    @Test
    fun `Type 2 - Bundle을 통해 데이터가 올바르게 전달되는지 확인`() {
        // Given
        val intent = mockk<Intent>(relaxed = true)
        val bundle = mockk<Bundle>(relaxed = true)
        mockkConstructor(Intent::class)
        mockkStatic(Bundle::class)
        every { anyConstructed<Intent>().setFlags(any()) } returns intent

        // When
        bundle.apply {
            putBoolean("BooleanData", true)
            putInt("NumberData", 100)
            putString("StringData", "String Data")
            putParcelable(
                "ParcelableData", ParcelableDataClass(
                    name = "Parcelable Data",
                    age = 10,
                    isMale = true,
                    address = "Address Data"
                )
            )
        }

        intent.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("BundleData", bundle)
        }
        mockContext.startActivity(intent)

        // Then
        verify {
            bundle.putBoolean("BooleanData", true)
            bundle.putInt("NumberData", 100)
            bundle.putString("StringData", "String Data")
            bundle.putParcelable("ParcelableData", any<ParcelableDataClass>())

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("BundleData", bundle)
        }
    }
}