package com.example.composesample.presentation.example.component.ui.graphics

/**
 * Graphics 예제 참고 자료
 *
 * ## NewShadowApiExampleUI (향상된 Shadow 효과)
 * - 별도 외부 출처 없음(Kotlin 2.1.0 + Compose 1.9.0 환경 자체 정리 노트)
 *
 * ### 핵심 개념
 * - `Modifier.shadow()`의 ambientColor/spotColor로 elevation 기반 그림자에 색상 제어 추가, clip=false로 경계 밖 확장 허용
 * - `drawBehind` + `inset()`으로 인너 섀도우·뉴모피즘(밝은/어두운 이중 그림자) 등 커스텀 그림자 직접 구현
 * - 실시간 속성 제어: radius(elevation 0~30dp)/spread(추가 레이어)/offset/alpha를 슬라이더로 조절
 * - animateFloatAsState + spring()으로 터치 시 elevation·alpha·scale 동시 애니메이션
 * - 주의: 모디파이어 순서(shadow→background), 그림자 잘림 방지 패딩, 과도한 그림자로 인한 성능 저하 회피
 */
