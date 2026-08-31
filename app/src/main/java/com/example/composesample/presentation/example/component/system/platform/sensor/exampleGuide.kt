package com.example.composesample.presentation.example.component.system.platform.sensor

/**
 * System/Platform/Sensor 예제 참고 자료
 *
 * ## SensorFusionCompassExampleUI (센서 퓨전 나침반)
 * - 센서 개요: https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview
 * - 위치 관련 센서(방위각·회전 벡터): https://developer.android.com/develop/sensors-and-location/sensors/sensors_position
 * - SensorManager: https://developer.android.com/reference/android/hardware/SensorManager
 * - 출처(Android Weekly #742): MBCompass — 오픈소스 Compose 나침반 앱
 *
 * 핵심 개념:
 * - TYPE_ROTATION_VECTOR 는 가속도계 + 자기장 + 자이로를 플랫폼이 합성(센서 퓨전)해 주는 가상 센서다.
 *   가속도계와 자기장을 직접 구독해 getRotationMatrix() 로 합치는 방식보다 떨림·간섭에 강하다.
 * - TYPE_GAME_ROTATION_VECTOR 는 자기장을 빼서 자기 간섭이 없는 대신 북쪽 기준이 없다 → 나침반에는 쓸 수 없다.
 * - 방위각 파이프라인 4단계:
 *   ① onSensorChanged(event.values) → ② getRotationMatrixFromVector(R, v)(반환값 없는 void)
 *   → ③ remapCoordinateSystem(R, X, Y, outR) → ④ getOrientation(outR, o) 의 o[0](라디안, -π~π)
 * - event.values 길이는 기기마다 다르다. 정의는 3~4개(x·y·z·w)지만 SM-A725F(Android 13, qualcomm
 *   "Rotation Vector Non-wakeup")는 5개를 실어 보낸다. getRotationMatrixFromVector 는 앞 3~4개만 읽으므로
 *   원본을 그대로 넘겨도 되고, 실측상 5개 원본과 앞 4개만 잘라낸 배열의 회전 행렬이 완전히 동일했다.
 * - 센서 좌표계는 화면이 아니라 기기의 자연 방향에 고정돼 있다. 화면 회전(Surface.ROTATION_*)에 따라
 *   축을 되돌리지 않으면 가로 모드에서 방위각이 90° 어긋난다.
 *   ROTATION_0(X, Y) / ROTATION_90(Y, -X) / ROTATION_180(-X, -Y) / ROTATION_270(-Y, X)
 * - 화면 회전 값은 ContextCompat.getDisplayOrDefault(context).rotation 으로 얻는다
 *   (deprecated 된 WindowManager.defaultDisplay 를 피하면서 minSdk 24 에서도 동작).
 * - 저역통과 필터의 랩어라운드 함정: previous + α × (raw − previous) 를 각도에 그대로 쓰면
 *   359° → 3° 구간에서 차이가 −356 이 되어 바늘이 반대 방향으로 한 바퀴 돈다.
 *   해법 ① 차이를 -180~180 으로 정규화: ((raw - previous + 540) % 360) - 180
 *   해법 ② sin/cos 성분을 각각 필터링한 뒤 atan2 로 복원
 * - onAccuracyChanged 의 SENSOR_STATUS_ACCURACY_* 는 자기 센서 보정 상태다.
 *   LOW/UNRELIABLE 이면 값 자체가 틀어져 있으므로 8자 캘리브레이션을 사용자에게 안내해야 한다.
 * - 여기서 얻는 방위각은 자북(magnetic north) 기준이다. 진북(true north)으로 바꾸려면
 *   GeomagneticField(위도, 경도, 고도, 시각).getDeclination() 만큼 보정한다.
 * - 리스너는 DisposableEffect 의 onDispose 에서 반드시 unregisterListener 로 해제한다(배터리 소모).
 * - ⚠️ 에뮬레이터는 회전 벡터 센서가 없거나 합성값만 제공하는 경우가 많아 실기기 확인이 필요하다.
 */
