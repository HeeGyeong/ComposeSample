package com.example.composesample.presentation.example.component.system.platform.shortcut

/**
 * System/Platform/Shortcut 예제 참고 자료
 *
 * ## ShortcutExampleUI (앱 숏컷 — dynamic / static / pin)
 * - 공식 문서: https://developer.android.com/develop/ui/views/launch/shortcuts
 * 핵심 개념:
 * - Dynamic Shortcut: ShortcutManager.dynamicShortcuts 로 런타임에 추가/갱신 (API 25+)
 * - Static Shortcut: res/xml/shortcuts.xml 로 정적 선언
 * - Pinned Shortcut: requestPinShortcut()로 홈 화면 고정 요청 (API 26+)
 * - minSdk 24 호환: Build.VERSION.SDK_INT 가드로 미지원 단말 early return (ShortcutUtil)
 */
