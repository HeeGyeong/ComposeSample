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
 */
