package com.example.composesample.presentation.example.component.ui.shapes

/**
 * Shapes 예제 참고 자료
 *
 * ## CardCornersExampleUI (Card 모서리 커스터마이징)
 * 핵심 개념:
 * - 4가지 기본 모서리 타입: Rounded(RoundedCornerShape), Sharp(RectangleShape), Cut(CutCornerShape), Concave(오목 — Custom Path/Canvas 없이는 오버레이로 시뮬레이션만 가능)
 * - RoundedCornerShape(topStart, topEnd, bottomStart, bottomEnd)로 모서리별 크기를 개별 지정해 혼합 스타일(정보 카드 상단만 둥글게, 대각선 스타일 등) 구현
 * - 인터랙티브 에디터: 모서리별 타입(Rounded/Sharp)과 크기(0-32dp, Slider)를 remember 상태로 관리해 실시간으로 Shape 재생성
 * - 실무 활용: 프로필 카드/알림 패널/액션 버튼마다 서로 다른 모서리 조합을 적용해 시각적으로 차별화
 *
 * ## PolygonShapeExampleUI (#148, Compose 1.13 PolygonShape · MorphPolygonShape)
 * 참고 자료:
 * - foundation 릴리스 노트(1.13.0-alpha02 도입 · beta01 API 재편 · 1.13.0 정식): https://developer.android.com/jetpack/androidx/releases/compose-foundation
 * - Compose November '26 릴리스 블로그(원문 사례 = 아바타 → 원 모핑): https://developer.android.com/blog/posts/jetpack-compose-november-2026-release
 * - TJ Dahunsi "Material is the metaphor"(Android Weekly #749 — graphics-shapes 를 직접 Path 로 바꾸던 코드):
 *   https://www.tunjid.com/articles/material-is-the-metaphor-pushing-the-limits-of-building-with-material-design-cross-platform-with-jetpack-compose-6acf75b3c23999c5f99f8d5b
 * 핵심 개념(foundation 1.13.0 sources · 실측, opt-in 없음):
 * - 팩토리: PolygonShape.regularPolygon · star · pill · pillStar · circle · rectangle · absoluteRectangle, 꼭짓점 직접은 PolygonShape { polygon(…) }.
 *   CornerRounding.dp(반경, smoothing) / fraction(비율, smoothing) / Unrounded. fraction 의 기준은 정다각형 · 별 = 반지름(짧은 변 ÷ 2),
 *   사각형 = 짧은 변 전체, 직접 준 꼭짓점 = 그 범위의 짧은 변(toRoundedPolygonRounding).
 * - PolygonShape 는 상태를 가진 인스턴스다: 마지막 크기 · 방향 · 밀도의 다각형과 Outline 하나만 캐시한다. MorphPolygonShape 는
 *   양 끝의 해석된 다각형이 같은 인스턴스일 때만 Morph 를 재사용한다(KDoc: 요소마다 인스턴스 하나).
 * - MorphPolygonShape(start, end) { progress } 의 람다와 transform { } 블록은 외곽선을 만들 때 실행된다. clip(레이어 매개변수) ·
 *   background · border 는 외곽선을 만드는 동안의 상태 읽기를 관찰하므로 리컴포지션 없이 그 단계만 다시 한다.
 * - @RememberInComposition: PolygonShape { } · transform · copy · MorphPolygonShape 를 remember 없이 컴포지션에서 부르면 lint
 *   RememberInComposition(Error).
 * - transform 은 제자리 변경이고 마지막 블록 하나만 남는다. 상태 쓰기가 아니라서 클릭에서 부르면 아무것도 다시 그리지 않고
 *   (Background 는 크기 · shape 가 같으면 지난 외곽선을 쓴다), 크기가 바뀌거나 다시 붙을 때 그 인스턴스를 쓰는 곳이 전부 바뀐다. copy() 는 따로.
 * - toPolygonShape(): RoundedCornerShape · CutCornerShape(+ Absolute) → PolygonShape. 3차 곡선 근사라 픽셀까지 같지 않다(KDoc).
 * - 크기 0: PolygonShape 계열은 폭이나 높이가 0 이면 graphics-shapes 의 RoundedPolygon 검사에서 IllegalArgumentException
 *   ("Can't get the direction of a 0-length vector" · "Polygons must have at least 3 vertices" · "Star radii must both be greater than 0" ·
 *   "Pill shapes must have positive width and height"). RoundedCornerShape 는 괜찮다. 1.14.0-alpha01 노트에도 수정 없음.
 * - Modifier.border 새 구현(ComposeFoundationFlags.isNewBorderImplementationEnabled, 기본 true — Modifier.border 를 부를 때 읽는다):
 *   일반 외곽선은 단색 · 그라데이션이면 ImageBitmap, RuntimeShader 같은 ShaderBrush 면 GraphicsLayer 에 그린다. 옛 구현은 일반 외곽선을
 *   소프트웨어 비트맵에만 그려 RuntimeShader 에서 던진다. 1.14.0-alpha01 은 API 28+ 의 일반 도형을 모두 GraphicsLayer 로.
 * 실측(Pixel Tablet API 35 에뮬, release 는 디버그 키 서명):
 * - 모핑 3방식(각 700ms 두 번 오가기): 리컴포지션 ① 값으로 · 새 도형 = 프레임마다(48~50), ② 람다 · 레이아웃 크기 = 0, ③ 람다 · scale = 0.
 *   프레임당 외곽선 시간 release ③ 13~16µs, ① 은 ③ 의 15~37배, ② 는 49~65배(② 는 프레임당 외곽선 1.8번).
 * - 계측 테스트: 진행률 4번 → ③ 리컴포지션 0 · 외곽선 4번 · 그림 바뀜, ① 리컴포지션 +4. 상태 transform 36° → 리컴포지션 0 · 외곽선 1번.
 *   공유 인스턴스를 클릭처럼 transform → 다시 만든 외곽선 0 · 픽셀 그대로(색을 바꿔 다시 그려도 그대로), 다시 붙이면 공유 A · B 만 회전, copy C 그대로.
 * - 캐시(1,000 프레임 데운 뒤 1,000 프레임, 96px · 320px 칸): release 한 인스턴스 59µs/프레임 vs 크기마다 copy() 0.09µs,
 *   매 프레임 회전이면 89.5µs vs 35.2µs.
 * - 크기 0: 0×0 · 240×0 에서 toPolygonShape · star · regularPolygon · circle · pill · rectangle · MorphPolygonShape 모두 던짐,
 *   PolygonShape { } 는 0×0 만(세 꼭짓점이 겹칠 때), 240×1 은 모두 그림. 계측 테스트: 높이 0 칸의 background 가 던짐, clip 은 배치에서.
 *   ZeroSafeShape(크기 0 이면 Outline.Rectangle) 로 감싸면 0 에서 자라는 칸도 그린다.
 * - toPolygonShape 픽셀 차(240px, 소프트웨어 캔버스): RoundedCornerShape 8.dp 113px · 24.dp 346px · 50% 930px · 섞음 261px · Cut 16.dp 278px
 *   (최대 채널 차 12~104). 하드웨어 캡처(계측 프로브) 24.dp 288px · 최대 60.
 * - border: 옛 구현 + 별 + RuntimeShader → IllegalArgumentException "Software rendering doesn't support RuntimeShader", 옛 구현 + 둥근 사각형
 *   (단순 · 모서리별) + RuntimeShader 는 그림, 새 구현 + 별 + RuntimeShader 는 그림. 단색 테두리는 새 · 옛 구현 픽셀 차 0.
 * - HotSwan: 재는 경로(PolygonShapeLabKt · CountingShape · MorphLabState)를 debug 재작성에서 뺐다 — ComposeHotReloadGuide.md.
 */
