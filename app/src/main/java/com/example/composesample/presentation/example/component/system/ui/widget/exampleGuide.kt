package com.example.composesample.presentation.example.component.system.ui.widget

/**
 * System/UI/Widget 예제 참고 자료
 *
 * ## GlanceWidgetExampleUI (Jetpack Glance 위젯 4종 비교: StreaksWidget/Small/Medium/LargeWidget)
 * 핵심 개념:
 * - Jetpack Glance는 Compose 스타일 선언형 API로 RemoteViews 없이 홈스크린 위젯을 작성하는 라이브러리(GlanceAppWidget + GlanceAppWidgetReceiver + GlanceModifier)
 * - StreaksWidget 은 크기 조절이 되지만(resizeMode="horizontal|vertical", 기본 2x1) 레이아웃은 하나다 — 크기별로 바꾸려면
 *   sizeMode = SizeMode.Responsive(setOf(DpSize…)) 나 SizeMode.Exact 를 두고 LocalSize.current 로 분기한다.
 *   이 예제는 그 대신 크기를 고정한 위젯 3종(Small/Medium/Large)으로 크기별 UI 를 나눈다
 * - SmallWidget/MediumWidget/LargeWidget은 각각 1x1·2x2·3x2+ 고정 크기(resizeMode="none")로 용도별 최적화된 UI 제공
 * - 위젯 갱신은 GlanceAppWidget.update(context, glanceId)로 트리거, 시스템 최소 업데이트 주기(약 30분)·제한된 Composable/Modifier 세트 등 제약 고려 필요
 */
