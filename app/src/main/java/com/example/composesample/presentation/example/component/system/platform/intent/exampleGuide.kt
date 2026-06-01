package com.example.composesample.presentation.example.component.system.platform.intent

/**
 * System/Platform/Intent 예제 참고 자료
 *
 * ## PassingIntentDataExampleUI / PassingIntentDataActivity (Intent 데이터 전달)
 * - 공식 문서: https://developer.android.com/guide/components/intents-filters
 * - Parcelable: https://developer.android.com/reference/android/os/Parcelable
 * 핵심 개념:
 * - @Parcelize 로 Parcelable 자동 구현 → Intent.putExtra 로 객체 전달
 * - API 33+ type-safe 추출: Bundle.getParcelable(key, Class) / BundleCompat.getParcelable (DEP-13)
 * - 암시적 Intent(ACTION_SEND 등)로 앱 간 데이터 공유
 * - ParcelableDataModel: @Parcelize data class 전달 예시
 */
