package com.example.composesample.presentation.example.component.system.deeplink

/**
 * System/Deeplink 예제 참고 자료
 *
 * ## DynamicAppLinksExampleUI (Android 15+ Dynamic App Links)
 * - 공식 문서: https://developer.android.com/training/app-links
 * - Digital Asset Links: https://developers.google.com/digital-asset-links/v1/getting-started
 * - Google Developers Blog (2025-10, 화면의 "출처 및 공식 문서" 카드가 가리키는 원문): https://android-developers.googleblog.com/2025/10/dynamic-app-links-elevating-your.html
 * 핵심 개념:
 * - App Links: https 스킴 딥링크 중 서버의 assetlinks.json으로 소유권이 검증된 링크 (디스앰비규에이션 없이 앱 직행)
 * - Android 15+: 서버의 Digital Asset Links JSON을 갱신해 앱 업데이트 없이 딥링킹 동작을 실시간 제어
 * - AndroidManifest의 intent-filter(autoVerify=true) + data scheme/host 선언
 * - 검증 상태는 adb 또는 설정에서 확인 가능 (verified/none)
 *
 * ## '실제 수신' 탭 (DeepLinkReceiveTab · DeepLinkDemoActivity · DeepLinkCore)
 * - 출처: KMP Bits, "Box, Box: Deep Links in Kotlin Multiplatform"(Android Weekly #749) — KMP 틀은 빼고 Android 수신 절반만:
 *   https://medium.com/@kmpbits/box-box-deep-links-in-kotlin-multiplatform-kmp-bits-23eea749127d
 * - 앱 콘텐츠 딥링크 만들기: https://developer.android.com/training/app-links/deep-linking
 * - activity launchMode(singleTop·standard): https://developer.android.com/guide/topics/manifest/activity-element#lmode
 * - 작업과 백스택: https://developer.android.com/guide/components/activities/tasks-and-back-stack
 * 핵심 개념(실측: Galaxy A72 API 33 debug·release, JVM 단위 테스트 DeepLinkCoreTest 5건):
 * - 다른 탭은 규칙 매칭 시뮬레이션이고 앱 매니페스트에 받는 쪽이 없었다 → 커스텀 스킴 액티비티 두 개(composesample:// singleTop ·
 *   composesample-standard:// standard). 액티비티는 문자열만 넘기고 해석·백스택·보류는 순수 Kotlin(DeepLinkCore)
 * - host 함정: composesample://order/4821 은 order 가 host, path 는 /4821 (java.net.URI·android.net.Uri 동일) → https 처럼 path 만 보면
 *   '알 수 없는 경로: 4821'. 스킴별로 host 를 앞에 붙여 두 형식을 같은 목적지로
 * - 콜드(onCreate) vs 웜(onNewIntent): singleTop 은 맨 위에 있을 때 같은 인스턴스로, standard 는 같은 task 에 새 인스턴스가 쌓여
 *   인스턴스마다 백스택이 따로 — 위 인스턴스를 비우면 아래 인스턴스의 이전 화면이 다시 나온다. adb am start 로 떠 있는 동안 보내도 onNewIntent
 * - 재생성: 회복된 액티비티에도 같은 intent 가 붙어 있다 → savedInstanceState 가 null 일 때만 처리. 안 하면 백스택에 한 번 더 쌓이는데,
 *   다시 처리되는 것은 마지막 웜 링크가 아니라 처음 링크다(onNewIntent 에서 setIntent 를 하지 않으면 getIntent 는 처음 intent).
 *   다크 모드 전환·HOME → am kill → 최근 앱 복원(새 pid) 모두 같은 규칙, 백스택은 rememberSaveable 로 복원
 * - 준비 전 링크: onCreate 는 setContent 보다 먼저라 백스택을 쥔 화면이 없다 → 보류 칸에 두고 첫 컴포지션(DisposableEffect)에서 배출.
 *   보류 칸이 없으면 콜드 링크가 버려져 빈 화면
 * - 백스택 합성: 콜드면 뒤에 아무것도 없다 → [홈, 목적지] 로 쌓아 뒤로 = 홈. 합성하지 않으면 뒤로 한 번에 액티비티가 닫힌다
 * - 커스텀 스킴은 누구나 보낼 수 있다 → 값 검증(주문 번호 숫자 1~9자리 등), 모르는 링크는 추측하지 말고 '잘못된 링크'로
 * - App Links(https autoVerify)는 도메인에 assetlinks.json 을 올려야 해서 이 탭은 다루지 않는다
 */
