package com.example.composesample.presentation.example.component.system.platform.language

/**
 * System/Platform/Language 예제 참고 자료
 *
 * ## LanguageSettingExampleUI / LocalLanguageChangeExampleUI (다국어 & 앱 내 언어 변경)
 * - 공식 문서(Per-app languages — 이 예제와 다른 시스템 방식, 아래 대조 참고): https://developer.android.com/guide/topics/resources/app-languages
 * 핵심 개념:
 * - LanguageSettingExampleUI: 현재 configuration.locales[0] 의 국가·언어·코드 표시 + Settings.ACTION_LOCALE_SETTINGS 로
 *   시스템(기기 전체) 언어 설정 화면 열기 + 현재 위치를 Geocoder 로 주소 변환
 * - LocalLanguageChangeExampleUI · LanguageTestActivity: util/LanguageManager 가 선택을 SharedPreferences 에 저장하고
 *   Configuration.setLocale + createConfigurationContext 로 '그 화면용 컨텍스트'만 만든다 → 그 컨텍스트로 getString 한 문자열만
 *   바뀌고 앱의 다른 화면·시스템 설정은 그대로다
 * - 대조(이 예제에는 없음): 시스템 per-app language 는 API 33 LocaleManager.setApplicationLocales
 *   (AppCompat 의 AppCompatDelegate.setApplicationLocales 는 33 미만 백포트) + 매니페스트 localeConfig 로 앱 전체 언어를 바꾸고
 *   시스템 설정의 '앱 언어'에 노출한다
 * - Geocoder: API 33+ getFromLocation 콜백 버전을 suspendCancellableCoroutine 으로 suspend 변환 (DEP-16)
 * - minSdk 24 기준 obsolete SDK 분기 제거 (deprecated configuration.locale 미사용)
 */
