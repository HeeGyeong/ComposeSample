package com.example.composesample.presentation.example.component.ui.canvas

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * Dial Component Guide
 *
 * 출처: https://www.sinasamaki.com/how-to-create-dials-in-jetpack-compose/
 *
 * Jetpack Compose에서 Canvas를 활용하여 Dial(다이얼) 컴포넌트를 직접 구현하는 방법을 설명합니다.
 * (원본 블로그는 ChromaDial 라이브러리를 소개하지만, 외부 라이브러리 추가 없이 직접 구현합니다.)
 *
 * === 핵심 개념 ===
 *
 * Dial은 원형 슬라이더로, 사용자가 원을 따라 값을 조절할 수 있는 UI 컴포넌트입니다.
 * 시계, 타이머, 온도 조절기 등 다양한 곳에서 사용됩니다.
 *
 * === 주요 구성 요소 ===
 *
 * 1. Track (트랙):
 *    - Dial의 원형 경로를 그리는 Arc.
 *    - `drawArc()`로 그리며, `startAngle`과 `sweepAngle`로 범위 지정.
 *    - 배경 트랙 + 활성 트랙으로 구분하여 진행 상태 표시.
 *
 * 2. Thumb (썸):
 *    - 현재 값을 나타내는 원형 핸들.
 *    - 사용자가 드래그하여 값을 변경.
 *    - 삼각함수(sin, cos)로 원 위의 위치 계산.
 *
 * 3. Tick Marks (눈금):
 *    - 일정 간격으로 표시되는 눈금선.
 *    - interval 값에 따라 스냅 포인트 역할.
 *
 * 4. Value Label (값 표시):
 *    - 현재 값을 텍스트로 표시.
 *    - Dial 중앙에 표시하는 것이 일반적.
 *
 * === Dial 파라미터 (블로그 기준) ===
 *
 * 1. degree: 현재 각도 값 (0~360 또는 그 이상).
 * 2. onDegreeChanged: 각도 변경 시 호출되는 콜백.
 * 3. onDegreeChangeFinished: 드래그 완료 시 호출 (비싼 연산용).
 * 4. startDegrees: 시작 각도 (기본 0, 12시 방향).
 * 5. sweepDegrees: 커버하는 각도 범위 (기본 360).
 * 6. interval: 스냅 간격 (기본 0, 스무스).
 *
 * === Dial Range ===
 *
 * - 기본 범위: 0º ~ 360º (한 바퀴).
 * - startDegrees로 시작점 지정 (예: 225º → 7시 반 방향에서 시작).
 * - sweepDegrees로 커버 범위 지정 (예: 270º → 3/4 원).
 * - sweepDegrees > 360이면 여러 바퀴 가능 (예: 1440º = 4바퀴).
 *
 * === Interval Snapping ===
 *
 * - interval = 0: 스무스 (기본값).
 * - interval = 30: 30도마다 스냅 포인트.
 * - interval = 360 / 60: 60분할 (시계의 분/초 단위).
 * - 스냅 시 가장 가까운 interval 배수로 값을 반올림.
 *
 * === Canvas 구현 핵심 ===
 *
 * 1. 각도 → 좌표 변환:
 *    - x = center.x + radius * cos(angleInRadians)
 *    - y = center.y + radius * sin(angleInRadians)
 *    - 12시 방향 기준: angleInRadians = Math.toRadians(degree - 90.0)
 *
 * 2. 터치 좌표 → 각도 변환:
 *    - angle = atan2(touchY - centerY, touchX - centerX)
 *    - 12시 방향 기준으로 보정: (degrees + 90 + 360) % 360
 *
 * 3. Arc 그리기:
 *    - drawArc(color, startAngle, sweepAngle, useCenter = false, style = Stroke)
 *    - startAngle은 3시 방향이 0도이므로, 12시 방향 기준으로 -90 보정.
 *
 * === 사용 시 주의사항 ===
 *
 * 1. 비싼 연산:
 *    - onDegreeChanged는 초당 여러 번 호출되므로, 디스크 저장 등은 onDegreeChangeFinished에서 처리.
 *
 * 2. 접근성:
 *    - 시각 장애인을 위해 값을 음성으로 읽어주는 기능 추가 권장.
 *    - contentDescription에 현재 값 포함.
 *
 * 3. 성능:
 *    - Canvas 리드로잉이 빈번하므로, remember로 불필요한 재계산 방지.
 *    - 무거운 drawText보다 별도 Text 컴포저블 사용이 효율적.
 *
 * === 요약 ===
 * - Canvas의 drawArc, drawCircle, drawLine으로 Dial UI 구현.
 * - 삼각함수로 각도 ↔ 좌표 변환.
 * - pointerInput으로 드래그 제스처 처리.
 * - startDegrees, sweepDegrees로 범위 제어.
 * - interval로 스냅 포인트 설정.
 */
object DialComponentGuide {
    const val GUIDE_INFO = """
        Dial Component - Jetpack Compose Canvas
        
        이 예제는 Canvas를 활용하여 Dial(원형 슬라이더) 컴포넌트를 
        직접 구현하는 방법을 보여줍니다.
        
        원본 블로그는 ChromaDial 라이브러리를 소개하지만,
        외부 라이브러리 없이 Canvas로 직접 구현합니다.
        
        핵심 요소:
        - Track: 원형 경로 (drawArc)
        - Thumb: 드래그 가능한 핸들 (drawCircle)
        - Tick Marks: 일정 간격 눈금
        - Value Label: 현재 값 표시
        
        구현된 Dial:
        1. Basic Dial: 기본 0~360도 원형 다이얼
        2. Range Dial: startDegrees, sweepDegrees로 범위 제한
        3. Snapping Dial: interval 기반 스냅 포인트
        4. Multi-Turn Dial: 360도 이상 여러 바퀴 회전
        
        주요 개념:
        - sin/cos로 원 위 좌표 계산
        - atan2로 터치 좌표 → 각도 변환
        - pointerInput으로 드래그 제스처 처리
        - drawArc, drawCircle, drawLine으로 UI 그리기
    """
}

@Preview
@Composable
fun DialComponentGuidePreview() {
    // Preview for the guide content if needed
}
