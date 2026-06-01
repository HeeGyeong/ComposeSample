package com.example.composesample.presentation.example.component.system.media.recorder

/**
 * System/Media/Recorder 예제 참고 자료
 *
 * ## AudioRecorderUI / AudioRecorderUtil (오디오 녹음 & 미디어 상태 관리)
 * - 공식 문서: https://developer.android.com/media/platform/mediarecorder
 * 핵심 개념:
 * - MediaRecorder 설정 순서: setAudioSource → setOutputFormat → setAudioEncoder → setOutputFile → prepare → start
 * - API 31(S)+ MediaRecorder(context) 생성자 사용, 미만은 no-arg 생성자 + @Suppress("DEPRECATION") (DEP-12)
 * - 리소스 정리: DisposableEffect 로 화면 이탈 시 MediaRecorder/MediaPlayer release (LIFECYCLE-02)
 * - 녹음/재생 상태를 State로 관리해 버튼 활성화·진행 표시 제어
 */
