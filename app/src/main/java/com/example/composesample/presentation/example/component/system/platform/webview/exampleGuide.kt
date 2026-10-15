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
 * - 이 예제는 JS ↔ 네이티브 브리지를 다루지 않는다 — 아래 WebViewBridgeExampleUI 참고
 *
 * ## WebViewBridgeExampleUI (WebView JS ↔ 네이티브 브리지)
 * - 출처: easyhooon/dari(Android Weekly #748) — WebView 브리지 메시지 인스펙터. 인스펙터가 아니라 그 샘플이 쓰는 브리지 API·
 *   프로토콜(request(handler, requestId, data) / onBridgeResponse, MessageStatus)만 가져왔다: https://github.com/easyhooon/dari
 * - 공식 문서: https://developer.android.com/develop/ui/views/layout/webapps/native-api-access-jsbridge
 * - 공식 문서(보안 위험): https://developer.android.com/privacy-and-security/risks/insecure-webview-native-bridges
 * - API: https://developer.android.com/reference/android/webkit/WebView#addJavascriptInterface(java.lang.Object,%20java.lang.String)
 * - API: https://developer.android.com/reference/android/webkit/WebView#evaluateJavascript(java.lang.String,%20android.webkit.ValueCallback%3Cjava.lang.String%3E)
 * - API: https://developer.android.com/reference/android/webkit/WebView#postWebMessage(android.webkit.WebMessage,%20android.net.Uri)
 * - androidx.webkit addWebMessageListener(이 프로젝트에 없는 의존성): https://developer.android.com/reference/androidx/webkit/WebViewCompat#addWebMessageListener(android.webkit.WebView,java.lang.String,java.util.Set%3Cjava.lang.String%3E,androidx.webkit.WebViewCompat.WebMessageListener)
 * 핵심 개념(실측: SM-A725F API 33 · Android System WebView 154, debug·release 동일):
 * - addJavascriptInterface + @JavascriptInterface: 웹 → 앱. JavaBridge 스레드에서 불리고 JS 는 반환까지 멈춘다(slow(300) → 308ms).
 *   브리지 안에서 WebView 메서드를 부르면 RuntimeException("A WebView method was called on thread 'JavaBridge'") →
 *   앱은 죽지 않고 JS 가 "Java exception was raised during method invocation" 을 받는다. View.post 로 메인에 넘긴다
 * - 인터페이스는 로드 전에 붙인다(다음 로드부터 보임). removeJavascriptInterface 도 다음 로드부터 — 지금 페이지에서는 호출이 그대로 닿는다
 * - 타입 변환: String 매개변수에 undefined·객체·배열 → 문자열 "undefined", null → null / Int 는 소수점 버림, 문자열·불리언·null → 0 /
 *   Boolean 은 true 외엔 false / 2^53+1 은 JS 숫자라 이미 9007199254740992. 반환 null String → undefined, IntArray → undefined,
 *   List·객체 → {} 이지만 그 객체의 @JavascriptInterface 메서드는 불린다(돌려준 객체도 브리지). 어노테이션 없는 메서드·getClass 는 안 보임,
 *   인자 수가 틀리면 "Method not found" → 값은 JSON 문자열 하나로 주고받는다
 * - evaluateJavascript: 앱 → 웹. 결과 콜백은 메인, 결과는 JSON 텍스트(문자열은 따옴표째). undefined·NaN·throw·ReferenceError 는 모두 "null",
 *   Promise·DOM 노드는 "{}", new Date(0) 은 "0.0". loadDataWithBaseURL 직후 실행하면 이전 문서에서 돈다 → onPageFinished 뒤에.
 *   Promise 값은 then 에서 브리지로 돌려받는다(그 호출은 JavaBridge 스레드)
 * - createWebMessageChannel + postWebMessage(API 23): 메인 프레임에 포트를 넘긴다. 대상 오리진이 페이지 오리진과 같거나 "*" 일 때만 전달되고,
 *   다르면 앱 쪽 오류 없이 사라진다(페이지 콘솔에만 "target origin … does not match"). 포트 콜백은 메인, 페이지의 event.origin 은 ""
 * - requestId 짝짓기(dari 샘플 모양): 응답이 B(200)·C(400)·A(600) 순으로 와도 id 로 짝지으면 각자 자기 응답, 도착 순서 큐는 셋 다 엇갈리는데
 *   앱 기록은 셋 다 SUCCESS — 어긋남은 웹에서만 보인다. 앱 상태(IN_PROGRESS·SUCCESS·ERROR)와 웹 수신(응답 함수가 true 반환)은 따로 센다
 * - 응답을 JS 코드에 끼워 넣기: 역슬래시·'·\n 만 바꾸는 손 이스케이프는 일반 텍스트의 \r 을 그대로 남겨 SyntaxError → 앱은 SUCCESS, 웹은 시간 초과.
 *   JSON 문자열 표기(Gson.toJson(String))는 날 줄바꿈이 없고 ' < > 도 \u 로 바꾼다. dari 샘플은 JSONObject.toString() 만 넘겨 이 경로를 타지 않는다
 * - 주입 범위: 메인 프레임·srcdoc·sandbox·data: iframe 모두 같은 객체를 부르고, 다른 오리진으로 이동한 페이지도 부른다. 앱 메서드는 호출한 프레임을 모른다.
 *   onPageStarted 로 적어 둔 최상위 URL 로 판정하면 로드 즉시 부른 호출이 콜백보다 먼저 와 이전 페이지 기준으로 판정된다(우회·오거부, 3회 동일)
 *   → 신뢰 콘텐츠만 띄우고, 보낸 프레임 오리진이 필요하면 androidx.webkit addWebMessageListener(allowedOriginRules · sourceOrigin · isMainFrame)
 * - 구현 메모: WebView 는 ViewModel 에 두지 않고 카드 수명 홀더가 가진다. WebViewClient·WebMessageCallback 은 이름 있는 클래스(HotSwan ⑰),
 *   브리지 콜백의 화면 상태 쓰기는 Handler 로 메인에. onRelease: removeJavascriptInterface → stopLoading → about:blank → removeAllViews → destroy
 */
