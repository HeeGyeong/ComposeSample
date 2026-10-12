package com.example.composesample.presentation.example.component.system.platform.webview

/**
 * System/Platform/WebView 예제 참고 자료
 *
 * ## WebViewIssueExampleUI (WebView 로 YouTube 임베드 재생)
 * - 공식 문서: https://developer.android.com/develop/ui/views/layout/webapps/webview
 * 핵심 개념:
 * - AndroidView(factory = { WebView(it) }) 로 Compose에 WebView 통합, update 에서는 URL 이 바뀐 경우에만 loadUrl
 * - 임베드 재생에 필요한 설정: settings.javaScriptEnabled · domStorageEnabled · mediaPlaybackRequiresUserGesture = false
 * - watch?v= · youtu.be 주소를 /embed/ 주소로 바꿔 로드한다(getEmbedYoutubeUrl)
 * - 전체 화면: WebChromeClient.onShowCustomView 로 받은 뷰를 WindowManager 에 붙이고 onHideCustomView 에서 뗀다
 * - webViewClient = WebViewClient() 기본 구현 — 페이지 안 링크를 외부 브라우저 대신 이 WebView 에서 연다(URL 가로채기 override 없음)
 * - 메모리 누수 방지: onRelease 에서 stopLoading → loadUrl("about:blank") → removeAllViews → destroy (LIFECYCLE-01)
 * - 이 예제는 JS ↔ 네이티브 브리지(addJavascriptInterface · evaluateJavascript · WebMessagePort)를 다루지 않는다
 */
