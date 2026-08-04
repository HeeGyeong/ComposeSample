package com.example.composesample.presentation.example.component.ui.layout.grid

/**
 * Compose Grid API 예제 참고 자료
 *
 * ## GridLayoutExampleUI (non-lazy 2D 트랙 레이아웃)
 * - 릴리스 블로그: https://android-developers.googleblog.com/2026/04/jetpack-compose-april-2026-updates.html
 * - Compose Foundation 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/compose-foundation
 * - CSS Grid 개념 대조: https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_grid_layout
 *
 * 핵심 개념:
 * - Grid(config = { ... }) { ... }: config 람다에서 트랙을 선언하고 content 람다에서 자식을 배치
 * - 트랙 선언은 단수형 column(...)/row(...) 또는 복수형 columns(vararg)/rows(vararg)
 * - GridTrackSize 6종 + minmax
 *   - Fixed(Dp): 고정 크기
 *   - Percentage(Float): 0.0~1.0 분수. 컨테이너 크기에 비례(100 기준이 아님)
 *   - Flex(Fr): 남은 공간을 fr 비율로 분배 (Row 의 weight 와 유사하나 트랙 단위)
 *   - Auto / MinContent / MaxContent: 콘텐츠 크기 기반
 *   - minmax(min: Dp, max: Fr): 최소 크기를 보장하면서 남으면 fr 로 확장
 * - 크기 해석 순서: 고정·콘텐츠·비율 트랙이 먼저 자리를 잡고, gap 을 차감한 뒤 남은 공간을 fr 이 분배
 * - GridFlow.Row / GridFlow.Column: 좌표를 지정하지 않은 자식을 채우는 커서의 진행 방향
 * - Modifier.gridItem(row, column, rowSpan, columnSpan, alignment)
 *   또는 IntRange 오버로드 Modifier.gridItem(rows, columns, alignment)
 * - 전부 @OptIn(ExperimentalGridApi::class) 필요
 *
 * 버전 메모:
 * - Grid API 는 Compose 1.11.0-alpha04 도입 → foundation-layout 1.11.1 에서 그대로 사용 가능
 * - 이름 붙인 영역(GridConfigurationScope.area() / Modifier.gridItem(area))은 1.12.0-beta01 신규라
 *   현재 버전에는 존재하지 않는다. 1.12 승격 이후 별도로 다룰 것
 *
 * 다른 레이아웃 예제와의 축 차이:
 * - LazyStaggeredGridExampleUI: 스크롤 격자(lazy) — 화면 밖 아이템은 컴포즈되지 않음
 * - FlowRowLayoutExampleUI: 1차원 흐름 + 자동 줄바꿈(트랙 개념 없음)
 * - CustomLayoutExampleUI: MeasurePolicy 를 직접 구현하는 저수준 경로
 *
 * 주의(함정):
 * - Grid 는 스크롤 컨테이너가 아니라 모든 자식이 컴포즈된다. 아이템 수가 데이터 양에 비례하면 Lazy 계열을 쓸 것
 * - 명시 배치는 좌표 충돌을 검증하지 않는다. 같은 셀을 두 아이템이 요구하면 오류 없이 그대로 포개진다
 * - Grid 내부는 SubcomposeLayout 기반이라 intrinsic 측정을 요구하면 예외가 발생한다
 *   (IntrinsicSize.Min/Max 로 감싸지 말 것)
 */
