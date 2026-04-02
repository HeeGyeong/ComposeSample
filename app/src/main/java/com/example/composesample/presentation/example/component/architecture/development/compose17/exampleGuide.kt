package com.example.composesample.presentation.example.component.architecture.development.compose17

/**
 * Compose 1.7 새 기능 예제 참고 자료
 *
 * ## TextOverflow (Start / Middle Ellipsis)
 * - 공식 문서: https://developer.android.com/jetpack/compose/text/style-text
 * 핵심 개념:
 * - TextOverflow.StartEllipsis: 앞부분 생략 ("...끝 부분")
 * - TextOverflow.MiddleEllipsis: 중간 생략 ("앞...끝")
 * - 기존 TextOverflow.Ellipsis는 End 생략 (기본값)
 *
 * ## GraphicsLayer (BlendMode / ColorFilter)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/graphics/draw/modifiers
 * 핵심 개념:
 * - Modifier.graphicsLayer { blendMode = BlendMode.Screen } : GPU 블렌딩 모드
 * - Modifier.graphicsLayer { colorFilter = ColorFilter.tint(...) } : 색상 필터
 * - compositeOver(): 두 색상을 알파 블렌딩으로 합성
 *
 * ## LookaheadScope (자동 레이아웃 애니메이션)
 * - 공식 문서: https://developer.android.com/develop/ui/compose/animation/lookahead
 * 핵심 개념:
 * - LookaheadScope 내부에서 Modifier.animateBounds()로 이동/크기 변화 자동 애니메이션
 * - 별도 AnimatedVisibility 없이 레이아웃 변화를 부드럽게 처리
 *
 * ## FocusRestorer (포커스 복원)
 * - 공식 문서: https://developer.android.com/reference/kotlin/androidx/compose/ui/focus/package-summary
 * 핵심 개념:
 * - Modifier.focusRestorer(): LazyRow 등에서 포커스가 사라졌다가 돌아올 때 마지막 위치 복원
 * - TV/폴더블 기기 접근성 대응에 필수
 *
 * ## PathGraphics (Path.reverse / Path.contains)
 * - 공식 문서: https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/Path
 * 핵심 개념:
 * - Path.reverse(): 채우기 방향 반전 (도넛 모양 구멍 뚫기)
 * - Path.contains(offset): 특정 좌표가 Path 내부에 포함되는지 판별
 */
