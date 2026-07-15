package com.example.composesample.presentation.example.component.system.media.video

/**
 * Media3 비디오 재생 (ExoPlayer) 예제 참고 자료
 *
 * ## 출처
 * - How we rewrote Reddit's video player on Android — Alexey Bykov
 * - Android Weekly #735
 *
 * ## 공식 문서 / 권장 자료
 * - Media3 개요: https://developer.android.com/media/media3
 * - Media3 ExoPlayer: https://developer.android.com/media/media3/exoplayer
 * - Compose 에 PlayerView 통합: https://developer.android.com/media/media3/exoplayer/hello-world
 * - AndroidView (Compose 상호운용): https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/views-in-compose
 *
 * ## 핵심 개념 요약
 * - androidx.media3 는 구버전 ExoPlayer(com.google.android.exoplayer2)를 흡수 통합한 Jetpack 공식 미디어 라이브러리
 * - media3-exoplayer(재생 엔진) / media3-ui(PlayerView) / media3-common(MediaItem·Player) 으로 모듈이 분리되어 필요한 것만 선택적으로 추가
 * - Compose 에는 PlayerView 를 AndroidView 로 임베딩하고, ExoPlayer 인스턴스는 remember 로 1회만 생성해 리컴포지션마다 재생성되지 않게 함
 * - Player.Listener(onIsPlayingChanged/onPlaybackStateChanged)로 재생 상태를 구독, 진행률(position/duration)은 콜백이 아닌 폴링으로 조회
 * - 네이티브 리소스이므로 화면 이탈 시 release(), 앱 백그라운드 전환(ON_STOP) 시 pause() 처리가 필수
 *
 * ## 본 예제 구현 메모
 * - 원문(Reddit 앱 비디오 플레이어 리팩터링 회고록)은 그대로 이식할 수 없어, Media3 Compose 통합의 핵심 패턴(AndroidView+PlayerView,
 *   상태 추적, 생명주기 관리)만 재구성해 시연
 * - 샘플 비디오는 Google 공개 테스트 스트림(BigBuckBunny.mp4)을 사용해 실제 네트워크 재생을 시연(네트워크 없으면 버퍼링 상태로 표시)
 * - 화면 이탈 시 리스너 해제 + player.release() 는 DisposableEffect 로, PlayerView 쪽 참조 해제는 AndroidView 의 onRelease 로 분리
 *   (WebViewIssueUI 의 AndroidView + onRelease 리소스 정리 관례와 동일)
 * - 앱 백그라운드 전환 시 자동 일시정지는 프로젝트 공용 유틸 `OnLifecycleEvent`(LifecycleUtil.kt)로 ON_STOP 을 구독해 실동작 시연
 */
