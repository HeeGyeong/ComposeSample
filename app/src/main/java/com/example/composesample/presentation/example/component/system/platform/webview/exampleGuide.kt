package com.example.composesample.presentation.example.component.system.platform.webview

/**
 * System/Platform/WebView 예제 참고 자료
 *
 * ## WebViewIssueUI (WebView 구현 & JavaScript 인터페이스)
 * - 공식 문서: https://developer.android.com/develop/ui/views/layout/webapps/webview
 * 핵심 개념:
 * - AndroidView(factory = { WebView(it) }) 로 Compose에 WebView 통합
 * - settings.javaScriptEnabled / addJavascriptInterface 로 JS ↔ 네이티브 브릿지
 * - WebViewClient.shouldOverrideUrlLoading 으로 URL 로딩 가로채기
 * - 메모리 누수 방지: onRelease 에서 stopLoading → loadUrl("about:blank") → removeAllViews → destroy (LIFECYCLE-01)
 */
