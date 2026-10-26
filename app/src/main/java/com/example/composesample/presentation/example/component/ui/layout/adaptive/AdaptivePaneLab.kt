package com.example.composesample.presentation.example.component.ui.layout.adaptive

/**
 * 적응형 다중 페인 예제의 순수 부분 — 표본 데이터와 표시용 이름. 화면·단위 테스트가 함께 쓴다.
 */

/** 목록·상세에 보일 표본 글 */
data class PaneArticle(val id: Int, val title: String, val summary: String, val related: List<String>)

val paneArticles: List<PaneArticle> = (1..12).map { n ->
    PaneArticle(
        id = n,
        title = "글 $n — ${listOf("레이아웃", "상태", "내비게이션", "접근성")[n % 4]}",
        summary = "목록에서 고르면 상세 페인이 열린다. 창이 넓으면 목록 옆, 좁으면 목록 자리를 대신한다(글 $n).",
        related = listOf("관련 메모 ${n}-A", "관련 메모 ${n}-B")
    )
}

fun articleById(id: Int?): PaneArticle? = paneArticles.firstOrNull { it.id == id }

/** 페인 배치 지시 — 창 크기대로 계산하거나, 폰·태블릿에서 반대 상태를 강제해 두 상태를 한 기기에서 비교한다 */
enum class DirectiveMode(val label: String, val partitions: Int?) {
    WINDOW("창 크기대로", null),
    FORCE_TWO("강제 2분할", 2),
    FORCE_ONE("강제 1분할", 1)
}

/** BackNavigationBehavior 네 가지 — 화면이 라이브러리 값으로 바꾼다 */
enum class BackMode(val label: String, val meaning: String) {
    SCAFFOLD_VALUE("PopUntilScaffoldValueChange", "보이는 페인 구성이 바뀔 때까지 꺼낸다(기본값)"),
    CURRENT_DESTINATION("PopUntilCurrentDestinationChange", "현재 페인 역할이 바뀔 때까지 꺼낸다"),
    CONTENT("PopUntilContentChange", "역할이나 내용(content key)이 바뀔 때까지 꺼낸다"),
    LATEST("PopLatest", "기록 하나만 꺼낸다")
}

/** PaneAdaptedValue.toString() 을 짧게 — "PaneAdaptedValue[Expanded]" → "Expanded" */
fun shortPaneValue(raw: String): String =
    Regex("""PaneAdaptedValue\[(.+)]""").find(raw)?.groupValues?.get(1) ?: raw

/** WindowSizeClass.minWidthDp 하한을 폭 클래스 이름으로 — Compact 는 0dp 부터라 숫자만 보면 헷갈린다 */
fun widthClassName(minWidthDp: Int): String = when {
    minWidthDp >= 1600 -> "XLarge(1600dp~)"
    minWidthDp >= 1200 -> "Large(1200dp~)"
    minWidthDp >= 840 -> "Expanded(840dp~)"
    minWidthDp >= 600 -> "Medium(600dp~)"
    else -> "Compact(~600dp)"
}
