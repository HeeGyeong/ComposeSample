package com.example.composesample.presentation.example.component.ui.layout.grid

/**
 * Compose Grid API 예제 참고 자료
 *
 * ## GridLayoutExampleUI (non-lazy 2D 트랙 레이아웃)
 * - 릴리스 블로그: https://android-developers.googleblog.com/2026/04/jetpack-compose-april-2026-updates.html
 * - Compose Foundation 릴리스 노트: https://developer.android.com/jetpack/androidx/releases/compose-foundation
 * - CSS Grid 개념 대조: https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_grid_layout
 * - CSS grid-template-areas(이름 붙인 영역의 원형): https://developer.mozilla.org/en-US/docs/Web/CSS/grid-template-areas
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
 * - 행·열 번호는 **1부터**(1.11.0 정식부터 그대로, 1.11.0·1.11.1·1.11.4·1.12.1 sources 확인). 0 은 GridIndexUnspecified —
 *   오류 없이 그 축만 자동 배치가 된다. 음수는 끝에서부터(-1 = 마지막 트랙, 선언한 트랙 기준)
 *   ⚠️ 이 예제의 배치 카드는 2026-10-27 까지 0부터 썼다: 헤더(0,0,span 3)는 자동 배치로 우연히 맞았지만
 *   Side(rows 1..2, columns 0..0)는 '1행 고정·열 자동'이 돼 암시적 4번째 열로, A(1,1)는 헤더 자리로 가 헤더를 덮었다(에뮬레이터 실측)
 * - 전부 @OptIn(ExperimentalGridApi::class) 필요
 *
 * 버전 메모:
 * - Grid API 는 Compose 1.11.0-alpha04 도입(@ExperimentalGridApi) — 예제 작성 당시 foundation-layout 1.11.1
 * - 이름 붙인 영역(GridConfigurationScope.area(areaId, row, column, rowSpan, columnSpan) / area(areaId, rows, columns) +
 *   Modifier.gridItem(areaId, alignment))은 1.12.0-beta01 신규로, 프로젝트의 foundation-layout 1.12.1 에 있다(@ExperimentalGridApi).
 *   GridAreaCards.kt 의 두 카드가 다룬다(#161)
 *
 * 이름 붙인 영역(1.12.1 Grid.kt 소스 기준):
 * - areaId 는 equals/hashCode 가 안정적인 값(enum·String·data class·object). config 의 area 는 id → 칸(16비트씩 묶은 Long) 맵에
 *   넣을 뿐이라 같은 id 를 두 번 정의하면 뒤의 것이 이긴다. row·column 둘 다 GridIndexUnspecified 면 IllegalArgumentException
 * - 1차원 영역: 한 축을 비워 두면(area("Tags", row = 1)) 그 행 안에서 빈 열을 차례로 찾는다 — 선언한 열을 넘으면
 *   다음 행으로 줄바꿈하지 않고 암시 열(Auto)을 만든다. 1차원·명시 배치는 자동 배치 커서를 움직이지 않는다
 * - 없는 id 는 조용히 자동 배치(1×1)로 떨어진다(문서: "silently fall back"). 범위 밖 음수 인덱스도 자동 배치
 * - 영역은 자리를 예약하지 않는다: 자식은 선언 순서대로 처리되고 명시(영역) 배치는 차 있어도 그 칸에 놓인다 —
 *   앞서 선언된 자동 배치 자식이 그 칸을 먼저 가져가면 영역 자식이 그 위에 포개진다
 * - 그리는 순서: 칸을 정한 뒤 자식을 (행, 열)로 안정 정렬해서 place 한다 → 시작 칸이 다르면 위치가 z 순서를 정한다.
 *   KDoc 의 "items declared later draw on top, mirroring Box" 는 시작 칸이 같을 때만 맞는다(같은 칸은 안정 정렬이라 선언 순서)
 * - 암시 트랙은 GridTrackSize.Auto
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
