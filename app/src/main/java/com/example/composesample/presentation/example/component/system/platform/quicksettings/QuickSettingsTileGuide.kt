package com.example.composesample.presentation.example.component.system.platform.quicksettings

/**
 * The Micro-Interaction Pattern: Using Quick Settings Tiles for Instant Input
 *
 * 출처: https://proandroiddev.com/the-micro-interaction-pattern-using-quick-settings-tiles-for-instant-input-57db9e52c458
 *
 * === 개요 ===
 *
 * Quick Settings Tile은 Android 7.0 (API 24)부터 도입된 기능으로,
 * 사용자가 알림 패널을 내려서 빠르게 앱 기능에 접근할 수 있게 해줍니다.
 *
 * 마이크로 인터랙션 패턴:
 * - 앱을 열지 않고도 빠르게 특정 기능 실행
 * - 토글 형태의 on/off 기능
 * - 카운터, 타이머 등의 즉각적인 입력
 *
 * === TileService 기본 구조 ===
 *
 * ```kotlin
 * @RequiresApi(Build.VERSION_CODES.N)
 * class MyTileService : TileService() {
 *
 *     override fun onTileAdded() {
 *         // 타일이 Quick Settings 패널에 추가될 때
 *     }
 *
 *     override fun onTileRemoved() {
 *         // 타일이 Quick Settings 패널에서 제거될 때
 *     }
 *
 *     override fun onStartListening() {
 *         // 타일이 보이기 시작할 때 (상태 업데이트)
 *     }
 *
 *     override fun onStopListening() {
 *         // 타일이 더 이상 보이지 않을 때
 *     }
 *
 *     override fun onClick() {
 *         // 타일 클릭 시 동작
 *     }
 * }
 * ```
 *
 * === Tile 상태 ===
 *
 * 1. Tile.STATE_ACTIVE
 *    - 활성화 상태 (파란색/강조색)
 *    - 기능이 켜져 있음을 나타냄
 *
 * 2. Tile.STATE_INACTIVE
 *    - 비활성화 상태 (회색)
 *    - 기능이 꺼져 있음을 나타냄
 *
 * 3. Tile.STATE_UNAVAILABLE
 *    - 사용 불가 상태
 *    - 클릭해도 반응하지 않음
 *
 * === 타일 속성 설정 ===
 *
 * ```kotlin
 * qsTile?.apply {
 *     state = Tile.STATE_ACTIVE
 *     label = "My Tile"
 *     subtitle = "Subtitle text"  // API 29+
 *     icon = Icon.createWithResource(context, R.drawable.ic_tile)
 *     contentDescription = "Accessibility description"
 *     updateTile()  // 변경사항 적용
 * }
 * ```
 *
 * === AndroidManifest.xml 등록 ===
 *
 * ```xml
 * <service
 *     android:name=".MyTileService"
 *     android:label="@string/tile_label"
 *     android:icon="@drawable/ic_tile"
 *     android:permission="android.permission.BIND_QUICK_SETTINGS_TILE"
 *     android:exported="true">
 *     <intent-filter>
 *         <action android:name="android.service.quicksettings.action.QS_TILE" />
 *     </intent-filter>
 *     <meta-data
 *         android:name="android.service.quicksettings.ACTIVE_TILE"
 *         android:value="true" />
 * </service>
 * ```
 *
 * === 마이크로 인터랙션 패턴 예시 ===
 *
 * 1. 카운터 타일 (CounterTileService)
 *    - 클릭할 때마다 값 증가
 *    - StateFlow로 앱과 상태 공유
 *    - 앱에서 리셋 가능
 *
 * 2. 토글 타일 (ToggleTileService)
 *    - on/off 상태 전환
 *    - STATE_ACTIVE / STATE_INACTIVE 활용
 *    - 앱에서 실시간 상태 확인
 *
 * 3. 타이머 타일 (TimerTileService)
 *    - 클릭으로 시작/정지 토글
 *    - Handler로 1초마다 카운트
 *    - 앱에서 시작/정지/리셋 제어
 *
 * 4. 퀵 액션 타일 (QuickActionTileService)
 *    - 클릭 시 즉시 액션 실행
 *    - 실행 횟수 및 마지막 실행 시간 기록
 *    - 앱에서 카운트 리셋 가능
 *
 * 5. 메모 타일 (MemoTileService)
 *    - 클릭 시 메모 입력 Activity 실행
 *    - Android 14+: PendingIntent 사용 필수
 *    - StateFlow로 메모 목록 앱과 공유
 *
 * === Android 14+ Activity 실행 ===
 *
 * Android 14(API 34)부터 startActivityAndCollapse(Intent)가 deprecated되어
 * PendingIntent를 사용해야 합니다.
 *
 * ```kotlin
 * override fun onClick() {
 *     val intent = Intent(this, MyActivity::class.java).apply {
 *         addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
 *     }
 *
 *     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
 *         val pendingIntent = PendingIntent.getActivity(
 *             this, 0, intent,
 *             PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
 *         )
 *         startActivityAndCollapse(pendingIntent)
 *     } else {
 *         @Suppress("DEPRECATION")
 *         startActivityAndCollapse(intent)
 *     }
 * }
 * ```
 *
 * === Active Tile vs Inactive Tile ===
 *
 * Active Tile (META_DATA ACTIVE_TILE = true):
 * - 시스템이 타일 상태를 자주 확인
 * - 실시간 업데이트가 필요한 경우 사용
 * - 배터리 소모 증가
 *
 * Inactive Tile (기본):
 * - 사용자가 패널을 열 때만 업데이트
 * - 대부분의 경우 권장
 *
 * === 프로그래매틱 타일 추가 (API 33+) ===
 *
 * ```kotlin
 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
 *     val statusBarManager = getSystemService(StatusBarManager::class.java)
 *     statusBarManager.requestAddTileService(
 *         ComponentName(this, MyTileService::class.java),
 *         "My Tile",
 *         Icon.createWithResource(this, R.drawable.ic_tile),
 *         executor
 *     ) { resultCode ->
 *         // 결과 처리
 *     }
 * }
 * ```
 *
 * === 주의사항 ===
 *
 * 1. 권한
 *    - BIND_QUICK_SETTINGS_TILE 권한 필수
 *    - exported="true" 설정 필요
 *
 * 2. 생명주기
 *    - Service이므로 메인 스레드에서 실행
 *    - 오래 걸리는 작업은 별도 스레드에서 처리
 *
 * 3. UI 업데이트
 *    - updateTile() 호출 필수
 *    - 호출하지 않으면 변경사항 반영 안됨
 *
 * 4. 잠금 화면
 *    - 잠금 화면에서는 제한된 동작만 가능
 *    - unlockAndRun()으로 잠금 해제 후 실행 가능
 *
 * 5. Android 14+ Activity 실행
 *    - startActivityAndCollapse(Intent) 사용 불가
 *    - PendingIntent로 감싸서 전달 필수
 *
 * === 요약 ===
 *
 * Quick Settings Tile은 앱 기능에 빠르게 접근할 수 있는 강력한 방법입니다.
 * 마이크로 인터랙션 패턴을 적용하면 사용자 경험을 크게 향상시킬 수 있습니다.
 *
 * 핵심 포인트:
 * - TileService 상속
 * - onClick()에서 동작 구현
 * - qsTile 속성 설정 후 updateTile() 호출
 * - AndroidManifest.xml에 서비스 등록
 * - Android 14+에서는 PendingIntent 사용
 */

object QuickSettingsTileGuide {
    const val GUIDE_INFO = """
        Quick Settings Tile - 마이크로 인터랙션 패턴
        
        구현된 타일 예제:
        1. CounterTileService - 클릭할 때마다 값 증가
        2. ToggleTileService - on/off 상태 전환
        3. TimerTileService - 시작/정지 제어
        4. QuickActionTileService - 즉시 액션 실행
        5. MemoTileService - 메모 입력 Activity 실행
        
        핵심 개념:
        - TileService: 빠른 설정 타일 서비스
        - Tile 상태: ACTIVE, INACTIVE, UNAVAILABLE
        - onClick(): 타일 클릭 시 동작
        - updateTile(): 상태 변경 적용
        
        Android 14+ 주의사항:
        - startActivityAndCollapse(Intent) 사용 불가
        - PendingIntent로 감싸서 전달 필수
        
        필수 설정:
        - BIND_QUICK_SETTINGS_TILE 권한
        - QS_TILE intent-filter
        - exported="true"
        
        출처: https://proandroiddev.com/the-micro-interaction-pattern-using-quick-settings-tiles-for-instant-input-57db9e52c458
    """
}
