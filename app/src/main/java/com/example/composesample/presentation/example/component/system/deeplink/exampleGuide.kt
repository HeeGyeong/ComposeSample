package com.example.composesample.presentation.example.component.system.deeplink

/**
 * System/Deeplink 예제 참고 자료
 *
 * ## DynamicAppLinksExampleUI (Android 15+ Dynamic App Links)
 * - 공식 문서: https://developer.android.com/training/app-links
 * - Digital Asset Links: https://developers.google.com/digital-asset-links/v1/getting-started
 * 핵심 개념:
 * - App Links: https 스킴 딥링크 중 서버의 assetlinks.json으로 소유권이 검증된 링크 (디스앰비규에이션 없이 앱 직행)
 * - Android 15+: 서버의 Digital Asset Links JSON을 갱신해 앱 업데이트 없이 딥링킹 동작을 실시간 제어
 * - AndroidManifest의 intent-filter(autoVerify=true) + data scheme/host 선언
 * - 검증 상태는 adb 또는 설정에서 확인 가능 (verified/none)
 */
