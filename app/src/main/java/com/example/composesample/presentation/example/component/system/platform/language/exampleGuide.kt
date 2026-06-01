package com.example.composesample.presentation.example.component.system.platform.language

/**
 * System/Platform/Language 예제 참고 자료
 *
 * ## LanguageSettingExampleUI / LocalLanguageChangeExampleUI (다국어 & 앱 내 언어 변경)
 * - 공식 문서(Per-app languages): https://developer.android.com/guide/topics/resources/app-languages
 * 핵심 개념:
 * - AppCompatDelegate.setApplicationLocales(LocaleListCompat) 로 앱 단위 언어 변경 (API 33+ 시스템 연동, 미만은 AndroidX 백포트)
 * - res/xml/locales_config.xml 선언으로 시스템 설정의 앱별 언어 노출
 * - Geocoder: API 33+ getFromLocation 콜백 버전을 suspendCancellableCoroutine 으로 suspend 변환 (DEP-16)
 * - minSdk 24 기준 obsolete SDK 분기 제거 (deprecated configuration.locale 미사용)
 */
