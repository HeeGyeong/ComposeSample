package com.example.composesample.presentation.example.component.ui.dynamic

/**
 * A2UI 예제(#120)의 '가짜 에이전트' 대본 — LLM 이 만들었다고 치고 미리 써 둔 A2UI v0.9 메시지들.
 * 네트워크 · Gemini 없이 렌더러만 보인다. Android 클래스가 없어 JVM 단위 테스트로 JSON 모양을 검증한다.
 *
 * 메시지 하나 = JSON 객체 하나 = 봉투 하나(createSurface · updateComponents · updateDataModel · deleteSurface 중 하나) + "version".
 * 컴포넌트는 {"id", "component": 타입 이름, …속성} 의 평평한 목록이고, 자식은 id 로 가리킨다. 루트 id 는 "root".
 * 값은 리터럴이거나 데이터 모델 경로({"path": "/form/name"}) — 경로 값이 아직 없으면 그 컴포넌트는 준비 중(로딩)이다.
 */
internal object A2uiDemoScript {
    const val PROTOCOL_VERSION = "v0.9"
    const val SURFACE_ID = "cafe"

    /** 기본 카탈로그 id — A2uiBasicCatalogV1.CatalogId 와 같아야 한다(단위 테스트로 확인) */
    const val BASIC_CATALOG_ID = "https://a2ui.org/specification/v0_9/catalogs/basic/catalog.json"

    /** 버튼이 서버(에이전트)로 보내는 이벤트 이름 */
    const val ORDER_EVENT = "order"

    /** 대본 한 줄 — 화면에 보일 이름과 보낼 JSON */
    data class Step(val label: String, val json: String)

    val steps: List<Step> = listOf(
        Step(
            "1. createSurface — 그릴 자리(surface)를 연다",
            """{"version":"$PROTOCOL_VERSION","createSurface":{"surfaceId":"$SURFACE_ID","catalogId":"$BASIC_CATALOG_ID"}}"""
        ),
        Step(
            "2. updateComponents — 컴포넌트 트리(값은 아직 없음)",
            """{"version":"$PROTOCOL_VERSION","updateComponents":{"surfaceId":"$SURFACE_ID","components":[""" +
                """{"id":"root","component":"Column","children":["title","menu","name","cups","takeout","order","status"]},""" +
                """{"id":"title","component":"Text","text":"오늘의 커피 주문","variant":"h3"},""" +
                """{"id":"menu","component":"Card","child":"menuText"},""" +
                """{"id":"menuText","component":"Text","text":"에티오피아 예가체프 · 4,500원"},""" +
                """{"id":"name","component":"TextField","label":"주문자 이름","value":{"path":"/form/name"}},""" +
                """{"id":"cups","component":"Slider","label":"잔 수","min":1,"max":5,"value":{"path":"/form/cups"}},""" +
                """{"id":"takeout","component":"CheckBox","label":"포장","value":{"path":"/form/takeout"}},""" +
                """{"id":"order","component":"Button","child":"orderLabel","variant":"primary",""" +
                """"action":{"event":{"name":"$ORDER_EVENT","context":{"name":{"path":"/form/name"},""" +
                """"cups":{"path":"/form/cups"},"takeout":{"path":"/form/takeout"}}}}},""" +
                """{"id":"orderLabel","component":"Text","text":"주문하기"},""" +
                """{"id":"status","component":"Text","text":{"path":"/status"},"variant":"caption"}""" +
                """]}}"""
        ),
        Step(
            "3. updateDataModel — 값이 들어오면 준비 중이던 컴포넌트가 그려진다",
            """{"version":"$PROTOCOL_VERSION","updateDataModel":{"surfaceId":"$SURFACE_ID","path":"/","value":""" +
                """{"form":{"name":"","cups":1,"takeout":false},"status":"주문 전 — 이름을 넣고 주문하기"}}}"""
        ),
        Step(
            "4. updateComponents — 같은 surface 의 트리를 바꾼다(안내 카드 추가)",
            """{"version":"$PROTOCOL_VERSION","updateComponents":{"surfaceId":"$SURFACE_ID","components":[""" +
                """{"id":"root","component":"Column","children":["title","menu","name","cups","takeout","order","status","line","tip"]},""" +
                """{"id":"line","component":"Divider"},""" +
                """{"id":"tip","component":"Card","child":"tipText"},""" +
                """{"id":"tipText","component":"Text","text":"오후 3시 전 주문은 원두 쿠폰 1장","variant":"caption"}""" +
                """]}}"""
        ),
        Step(
            "5. deleteSurface — surface 를 닫는다",
            """{"version":"$PROTOCOL_VERSION","deleteSurface":{"surfaceId":"$SURFACE_ID"}}"""
        )
    )

    /** 일부러 틀린 메시지 — 앱이 죽지 않고 어떻게 거절되는지 본다 */
    data class BadCase(val label: String, val json: String)

    val badCases: List<BadCase> = listOf(
        BadCase("깨진 JSON", """{"version":"$PROTOCOL_VERSION","createSurface":"""),
        BadCase(
            "지원하지 않는 버전(v0.8)",
            """{"version":"v0.8","createSurface":{"surfaceId":"$SURFACE_ID","catalogId":"$BASIC_CATALOG_ID"}}"""
        ),
        BadCase(
            "id 없는 컴포넌트",
            """{"version":"$PROTOCOL_VERSION","updateComponents":{"surfaceId":"$SURFACE_ID","components":[{"component":"Text","text":"x"}]}}"""
        ),
        BadCase(
            "카탈로그에 없는 타입(Carousel)",
            """{"version":"$PROTOCOL_VERSION","updateComponents":{"surfaceId":"$SURFACE_ID","components":[""" +
                """{"id":"root","component":"Carousel","children":[]}]}}"""
        )
    )

    /**
     * 주문 이벤트를 받은 가짜 에이전트의 답 — 상태 줄 하나를 바꾸는 updateDataModel.
     * 이벤트 문맥 값은 데이터 모델에서 풀린 값이다(이름 String · 잔 수 Number · 포장 Boolean). 모르는 모양이면 안전한 기본값.
     */
    fun orderReply(context: Map<String, Any?>): String {
        val name = (context["name"] as? String)?.trim().orEmpty().ifEmpty { "이름 없음" }
        val cups = (context["cups"] as? Number)?.toInt()?.coerceIn(1, 5) ?: 1
        val place = if (context["takeout"] == true) "포장" else "매장"
        val status = "주문 접수: $name · ${cups}잔 · $place"
        return """{"version":"$PROTOCOL_VERSION","updateDataModel":{"surfaceId":"$SURFACE_ID","path":"/status","value":"${jsonEscape(status)}"}}"""
    }

    /** JSON 문자열 안에 넣을 값 — 따옴표 · 역슬래시 · 제어 문자를 이스케이프 */
    fun jsonEscape(text: String): String = buildString {
        text.forEach { c ->
            when {
                c == '"' -> append("\\\"")
                c == '\\' -> append("\\\\")
                c == '\n' -> append("\\n")
                c == '\r' -> append("\\r")
                c == '\t' -> append("\\t")
                c < ' ' -> append("\\u").append(c.code.toString(16).padStart(4, '0'))
                else -> append(c)
            }
        }
    }
}
