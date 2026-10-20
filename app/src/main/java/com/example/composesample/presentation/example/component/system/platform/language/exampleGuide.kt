package com.example.composesample.presentation.example.component.system.platform.language

/**
 * System/Platform/Language 예제 참고 자료
 *
 * ## LanguageSettingExampleUI / LocalLanguageChangeExampleUI (다국어 & 앱 내 언어 변경)
 * - 공식 문서(Per-app languages): https://developer.android.com/guide/topics/resources/app-languages
 * - LocaleManager: https://developer.android.com/reference/android/app/LocaleManager
 * - LocaleConfig: https://developer.android.com/reference/android/app/LocaleConfig
 * - Settings.ACTION_APP_LOCALE_SETTINGS: https://developer.android.com/reference/android/provider/Settings#ACTION_APP_LOCALE_SETTINGS
 * - AppCompatDelegate.setApplicationLocales: https://developer.android.com/reference/androidx/appcompat/app/AppCompatDelegate#setApplicationLocales(androidx.core.os.LocaleListCompat)
 * - LocaleManagerCompat(core): https://developer.android.com/reference/androidx/core/app/LocaleManagerCompat
 * - 출처(#140, Android Weekly #748): One Maestro flow in every language, on iOS and Android — https://shotfleet.com/guides/maestro-one-flow-every-language
 *   ("Android: per-app locales (API 33)" 와 pm clear 함정만 — Maestro 도구 자체는 범위 밖)
 * 핵심 개념:
 * - LanguageSettingExampleUI: 현재 configuration.locales[0] 의 국가·언어·코드 표시 + Settings.ACTION_LOCALE_SETTINGS 로
 *   시스템(기기 전체) 언어 설정 화면 열기 + 현재 위치를 Geocoder 로 주소 변환
 * - LocalLanguageChangeExampleUI 첫 탭 '컨텍스트 교체' · LanguageTestActivity: util/LanguageManager 가 선택을 SharedPreferences 에
 *   저장하고 Configuration.setLocale + createConfigurationContext 로 '그 화면용 컨텍스트'만 만든다 → 그 컨텍스트로 getString 한
 *   문자열만 바뀌고 앱의 다른 화면·Locale.getDefault·날짜 형식·시스템 설정은 그대로다
 * - LocalLanguageChangeExampleUI 둘째 탭 '시스템 앱 언어'(AppLocaleTab.kt, #140): API 33 LocaleManager.setApplicationLocales 로
 *   앱 전체 언어를 시스템에 맡긴다. 실측(Galaxy A72 API 33 debug·release, 에뮬레이터 API 35 en-US · API 30):
 *   - 값이 바뀌면 앱의 액티비티가 재생성된다(wm_relaunch, BlogExampleActivity 는 configChanges 에 locale 없음).
 *     ViewModel·pid 는 그대로, 같은 값을 다시 넣으면 재생성 없음. 호출 직후 getApplicationLocales() 는 새 값이지만 configuration 은 이전 값
 *   - configuration.locales 는 [앱 언어…, 시스템 목록…] 으로 합쳐지고 Locale.getDefault·DateFormat 도 바뀐다.
 *     Resources.getSystem() 도 이 합쳐진 목록을 준다 → 기기 언어는 LocaleManager.systemLocales 로 읽는다
 *   - 리소스 선택: ja 는 values-ja 가 없어 기본 values(한국어)로 떨어진다(영어 기기에서도). ja,en 도 한국어 — APK 에 라이브러리의
 *     values-ja 가 있어(ResourcesImpl 이 APK 의 로케일 목록에서 첫 일치를 고름) ja 가 선택된다. APK 에 없는 fy 는 fy,en 이면
 *     en 이 맨 앞으로 올라온다. androidResources.localeFilters 로 ko·en 만 남긴 임시 빌드에서는 ja,en → en(이 앱 설정은 바꾸지 않음)
 *   - AppCompatDelegate.setApplicationLocales(API 33+)는 살아 있는 AppCompatDelegate 에서 컨텍스트를 얻는데 이 앱은 ComponentActivity
 *     뿐이라 조용히 아무것도 안 한다(appcompat 1.8.0 getLocaleManagerForApplication). getApplicationLocales() 도 늘 [] →
 *     읽기는 LocaleManagerCompat. API 33 미만은 메모리 값([en])만 바뀌고 configuration 은 그대로(적용 대상 = AppCompatActivity,
 *     저장 = 매니페스트 autoStoreLocales 서비스)
 *   - android:localeConfig(res/xml/locales_config.xml, ko·en): 선언 전에는 ACTION_APP_LOCALE_SETTINGS 설정 화면이 뜨자마자 닫혔고,
 *     선언 뒤에는 '시스템 기본값'·추천·모든 언어 목록이 나온다. 목록은 설정 화면용일 뿐 LocaleManager 로는 목록 밖 ja·fy 도 들어간다.
 *     AGP androidResources.generateLocaleConfig = true + res/resources.properties(unqualifiedResLocale)로 자동 생성할 수도 있다.
 *     API 34+ 는 LocaleManager.setOverrideLocaleConfig 로 앱이 목록을 런타임에 바꿀 수 있다(이 예제는 33 기기라 다루지 않음)
 *   - 저장은 시스템: 프로세스 종료 뒤 새 pid 로도 그 언어로 시작, install -r 은 유지, pm clear·삭제 뒤 설치는 [] (API 35 에뮬레이터)
 *   - 밖에서: adb shell cmd locale set-app-locales <pkg> --locales en / get-app-locales <pkg> (앱이 떠 있으면 즉시 재생성)
 *   - 두 방식을 섞으면: 시스템 언어가 en 이어도 첫 탭은 저장값(한국어)으로 그린다 — 헤더 제목이 탭마다 다른 언어
 * - Geocoder: API 33+ getFromLocation 콜백 버전을 suspendCancellableCoroutine 으로 suspend 변환 (DEP-16)
 * - minSdk 24 기준 obsolete SDK 분기 제거 (deprecated configuration.locale 미사용)
 */
