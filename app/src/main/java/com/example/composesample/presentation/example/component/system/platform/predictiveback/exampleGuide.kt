package com.example.composesample.presentation.example.component.system.platform.predictiveback

/**
 * System/Platform/PredictiveBack 예제 참고 자료
 *
 * ## PredictiveBackExampleUI (Predictive Back Gesture, Android 14+)
 * - 공식 문서: https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture
 * - Compose 연동: https://developer.android.com/develop/ui/compose/system/predictive-back
 * 핵심 개념:
 * - PredictiveBackHandler { progress: Flow<BackEventCompat> -> } 로 뒤로가기 진행률 수신
 * - progress.collect 로 스와이프 진행률(0f~1f)에 따라 실시간 애니메이션
 * - try/finally: 제스처 완료 시 commit, 취소 시 CancellationException 처리로 원복
 * - manifest android:enableOnBackInvokedCallback="true" 필요
 */
