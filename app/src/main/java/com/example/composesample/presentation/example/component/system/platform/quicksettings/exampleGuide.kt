package com.example.composesample.presentation.example.component.system.platform.quicksettings

/**
 * System/Platform/QuickSettings 예제 참고 자료
 *
 * ## QuickSettingsTileExampleUI (Quick Settings Tile 마이크로 인터랙션 패턴: Counter/Toggle/Timer/QuickAction/Memo)
 * - 출처: https://proandroiddev.com/the-micro-interaction-pattern-using-quick-settings-tiles-for-instant-input-57db9e52c458
 * 핵심 개념:
 * - TileService(onTileAdded/onStartListening/onClick 등)를 상속해 알림 패널에서 앱을 열지 않고 즉시 기능 실행
 * - Tile.STATE_ACTIVE/INACTIVE/UNAVAILABLE 상태를 설정한 뒤 반드시 updateTile() 호출해야 반영
 * - Android 14+부터 startActivityAndCollapse(Intent)가 deprecated → PendingIntent로 감싸 전달 필요
 * - AndroidManifest에 BIND_QUICK_SETTINGS_TILE 권한 + QS_TILE intent-filter + exported="true" 필수
 */
