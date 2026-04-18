package com.example.composesample.presentation.example.component.system.ui.widget

/**
 * 📚 Glance Widget 실전 예제 학습 가이드
 * 
 * 이 문서는 GlanceWidgetExampleUI, StreaksWidget, StreaksWidgetReceiver의 상세한 학습 가이드를 제공합니다.
 * 
 * =================================================================================================
 * 🎯 이 예제로 학습할 수 있는 내용
 * =================================================================================================
 * 
 * 1. Jetpack Glance를 사용한 Android Widget 개발
 * 2. GlanceAppWidget과 GlanceAppWidgetReceiver의 역할과 구현
 * 3. Compose UI와 Glance UI의 차이점과 활용법
 * 4. Widget 라이프사이클 관리 및 데이터 업데이트
 * 5. Android Manifest 설정 및 Widget Provider 등록
 * 
 * =================================================================================================
 * 🔍 핵심 개념: Jetpack Glance
 * =================================================================================================
 * 
 * 📝 Glance란?
 * - Google에서 개발한 Jetpack 라이브러리로, Compose 스타일로 Android Widget을 작성할 수 있게 해줌
 * - 기존 RemoteViews 방식을 대체하여 더 직관적이고 유지보수하기 쉬운 Widget 개발 가능
 * - Compose UI와 유사한 선언형 API를 제공하지만 Widget에 특화된 제약사항과 기능들을 포함
 * 
 * 🏗️ Glance 아키텍처:
 * 1. GlanceAppWidget: Widget의 UI 로직과 데이터 처리
 * 2. GlanceAppWidgetReceiver: Widget의 라이프사이클 이벤트 처리
 * 3. Glance Composables: Widget UI 구성 요소들
 * 4. GlanceModifier: Widget 전용 Modifier 시스템
 * 
 * 🚀 주요 장점:
 * - Compose와 유사한 선언형 UI 작성
 * - 타입 안전성과 컴파일 타임 검증
 * - 기존 RemoteViews보다 간단한 구조
 * - 코드 재사용성과 유지보수성 향상
 * 
 * =================================================================================================
 * 🎨 컴포넌트별 상세 가이드
 * =================================================================================================
 * 
 * 📋 GlanceAppWidget - Widget 핵심 로직
 * ────────────────────────────────────────────────────────────────────────────────────────────
 * 🎯 역할과 책임:
 *    - Widget의 UI 구성과 데이터 처리 담당
 *    - provideGlance() 메서드에서 Widget 콘텐츠 제공
 *    - 데이터 소스와 UI 바인딩 관리
 * 
 * 💡 핵심 구현 패턴:
 * ```kotlin
 * class StreaksWidget : GlanceAppWidget() {
 *     override suspend fun provideGlance(context: Context, id: GlanceId) {
 *         // 1. 데이터 로딩
 *         val data = loadWidgetData(context)
 *         
 *         // 2. UI 제공
 *         provideContent {
 *             GlanceTheme {
 *                 WidgetContent(data = data)
 *             }
 *         }
 *     }
 * }
 * ```
 * 
 * 🔧 데이터 처리 전략:
 * - 비동기 데이터 로딩: suspend 함수로 데이터 안전하게 처리
 * - 캐싱 및 최적화: 불필요한 네트워크 호출 방지
 * - 에러 핸들링: 데이터 로딩 실패 시 fallback UI 제공
 * - 상태 관리: Widget별 개별 상태 유지
 * 
 * 📋 GlanceAppWidgetReceiver - 라이프사이클 관리
 * ────────────────────────────────────────────────────────────────────────────────────────────
 * 🎯 핵심 기능:
 *    - Widget의 생성, 업데이트, 삭제 이벤트 처리
 *    - System broadcast 수신 및 Widget 상태 동기화
 *    - GlanceAppWidget 인스턴스와 연결점 역할
 * 
 * 💡 구현 패턴:
 * ```kotlin
 * class StreaksWidgetReceiver : GlanceAppWidgetReceiver() {
 *     override val glanceAppWidget: GlanceAppWidget = StreaksWidget()
 *     
 *     // 추가 커스터마이징이 필요한 경우
 *     override fun onUpdate(...) {
 *         // 커스텀 업데이트 로직
 *         super.onUpdate(...)
 *     }
 * }
 * ```
 * 
 * 🔧 라이프사이클 이벤트:
 * - onEnabled(): 첫 번째 Widget 인스턴스 생성
 * - onUpdate(): Widget 업데이트 필요 시
 * - onDeleted(): Widget 인스턴스 삭제
 * - onDisabled(): 마지막 Widget 인스턴스 삭제
 * 
 * 📋 Glance UI Components - Widget 전용 UI
 * ────────────────────────────────────────────────────────────────────────────────────────────
 * 🎯 주요 컴포넌트:
 *    - Text: 텍스트 표시 (제한된 스타일링)
 *    - Image: 이미지 표시 (리소스, URL, Bitmap)
 *    - Button: 클릭 가능한 버튼
 *    - LazyColumn/LazyRow: 스크롤 가능한 리스트
 *    - Box, Column, Row: 레이아웃 컨테이너
 * 
 * 💡 Glance vs Compose 차이점:
 * ```kotlin
 * // Compose UI
 * Text(
 *     text = "Hello",
 *     style = TextStyle(fontSize = 16.sp),
 *     modifier = Modifier.padding(8.dp)
 * )
 * 
 * // Glance UI
 * Text(
 *     text = "Hello",
 *     style = TextStyle(fontSize = 16.sp),
 *     modifier = GlanceModifier.padding(8.dp)
 * )
 * ```
 * 
 * 🔧 제약사항과 한계:
 * - 제한된 Composable 세트만 사용 가능
 * - 복잡한 애니메이션 불가
 * - 일부 Modifier만 지원
 * - Custom Drawing 제한적
 * 
 * =================================================================================================
 * 🚀 실무 적용 시나리오
 * =================================================================================================
 * 
 * ✅ 날씨 위젯:
 * ```kotlin
 * class WeatherWidget : GlanceAppWidget() {
 *     override suspend fun provideGlance(context: Context, id: GlanceId) {
 *         val weatherData = weatherRepository.getCurrentWeather()
 *         
 *         provideContent {
 *             WeatherContent(
 *                 temperature = weatherData.temperature,
 *                 condition = weatherData.condition,
 *                 location = weatherData.location
 *             )
 *         }
 *     }
 * }
 * ```
 * 
 * ✅ 할 일 목록 위젯:
 * ```kotlin
 * @Composable
 * fun TodoListWidget(todos: List<TodoItem>) {
 *     LazyColumn {
 *         items(todos.take(5)) { todo ->
 *             Row(modifier = GlanceModifier.fillMaxWidth()) {
 *                 Text(
 *                     text = todo.title,
 *                     modifier = GlanceModifier.defaultWeight()
 *                 )
 *                 Image(
 *                     provider = ImageProvider(
 *                         if (todo.completed) R.drawable.ic_check 
 *                         else R.drawable.ic_unchecked
 *                     ),
 *                     contentDescription = null
 *                 )
 *             }
 *         }
 *     }
 * }
 * ```
 * 
 * ✅ 미디어 컨트롤 위젯:
 * ```kotlin
 * @Composable
 * fun MediaControlWidget(mediaState: MediaState) {
 *     Column {
 *         Text(text = mediaState.title)
 *         Text(text = mediaState.artist)
 *         
 *         Row {
 *             Image(
 *                 provider = ImageProvider(R.drawable.ic_previous),
 *                 contentDescription = "Previous",
 *                 modifier = GlanceModifier.clickable(
 *                     actionRunCallback<PreviousTrackAction>()
 *                 )
 *             )
 *             Image(
 *                 provider = ImageProvider(
 *                     if (mediaState.isPlaying) R.drawable.ic_pause 
 *                     else R.drawable.ic_play
 *                 ),
 *                 contentDescription = "Play/Pause",
 *                 modifier = GlanceModifier.clickable(
 *                     actionRunCallback<PlayPauseAction>()
 *                 )
 *             )
 *             Image(
 *                 provider = ImageProvider(R.drawable.ic_next),
 *                 contentDescription = "Next",
 *                 modifier = GlanceModifier.clickable(
 *                     actionRunCallback<NextTrackAction>()
 *                 )
 *             )
 *         }
 *     }
 * }
 * ```
 * 
 * =================================================================================================
 * 🔧 고급 활용 패턴
 * =================================================================================================
 * 
 * 🎯 데이터 동기화 전략:
 * ```kotlin
 * class SmartWidget : GlanceAppWidget() {
 *     override suspend fun provideGlance(context: Context, id: GlanceId) {
 *         // 캐시된 데이터 우선 표시
 *         val cachedData = dataCache.get(id)
 *         if (cachedData != null) {
 *             provideContent { WidgetContent(cachedData) }
 *         }
 *         
 *         // 백그라운드에서 최신 데이터 로드
 *         try {
 *             val freshData = dataRepository.fetchLatest()
 *             dataCache.put(id, freshData)
 *             
 *             // UI 업데이트
 *             provideContent { WidgetContent(freshData) }
 *         } catch (e: Exception) {
 *             // 에러 처리 - 캐시된 데이터 유지
 *             provideContent { 
 *                 ErrorContent("데이터를 불러올 수 없습니다")
 *             }
 *         }
 *     }
 * }
 * ```
 * 
 * 🎯 사용자 인터랙션 처리:
 * ```kotlin
 * @Composable
 * fun InteractiveWidget() {
 *     Column {
 *         Button(
 *             text = "앱 열기",
 *             onClick = actionStartActivity<MainActivity>()
 *         )
 *         
 *         Button(
 *             text = "설정",
 *             onClick = actionStartActivity(
 *                 Intent(context, SettingsActivity::class.java)
 *             )
 *         )
 *         
 *         Button(
 *             text = "새로고침",
 *             onClick = actionRunCallback<RefreshAction>()
 *         )
 *     }
 * }
 * 
 * class RefreshAction : ActionCallback {
 *     override suspend fun onAction(
 *         context: Context,
 *         glanceId: GlanceId,
 *         parameters: ActionParameters
 *     ) {
 *         // 데이터 새로고침 로직
 *         StreaksWidget().update(context, glanceId)
 *     }
 * }
 * ```
 * 
 * 🎯 다중 크기 지원:
 * ```kotlin
 * @Composable
 * fun ResponsiveWidget(size: DpSize) {
 *     when {
 *         size.width < 120.dp -> CompactWidget()
 *         size.width < 200.dp -> MediumWidget()
 *         else -> ExpandedWidget()
 *     }
 * }
 * ```
 * 
 * =================================================================================================
 * 🔧 Android Manifest 설정 가이드
 * =================================================================================================
 * 
 * 📋 필수 Manifest 등록:
 * ```xml
 * <!-- AndroidManifest.xml -->
 * <receiver 
 *     android:name=".StreaksWidgetReceiver"
 *     android:exported="true">
 *     <intent-filter>
 *         <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
 *     </intent-filter>
 *     <meta-data 
 *         android:name="android.appwidget.provider"
 *         android:resource="@xml/streaks_widget_info" />
 * </receiver>
 * ```
 * 
 * 📋 Widget 메타데이터 설정:
 * ```xml
 * <!-- res/xml/streaks_widget_info.xml -->
 * <?xml version="1.0" encoding="utf-8"?>
 * <appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:description="@string/widget_description"
 *     android:initialKeyguardLayout="@layout/widget_loading"
 *     android:initialLayout="@layout/widget_loading"
 *     android:minWidth="250dp"
 *     android:minHeight="110dp"
 *     android:targetCellWidth="4"
 *     android:targetCellHeight="2"
 *     android:previewImage="@drawable/widget_preview"
 *     android:resizeMode="horizontal|vertical"
 *     android:updatePeriodMillis="1800000"
 *     android:widgetCategory="home_screen" />
 * ```
 * 
 * 📋 권한 설정:
 * ```xml
 * <!-- 네트워크 데이터 필요한 경우 -->
 * <uses-permission android:name="android.permission.INTERNET" />
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 * 
 * <!-- 위치 기반 위젯인 경우 -->
 * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 * ```
 * 
 * =================================================================================================
 * 💡 성능 및 최적화 전략
 * =================================================================================================
 * 
 * ⚡ 배터리 효율성:
 * ```kotlin
 * class BatteryOptimizedWidget : GlanceAppWidget() {
 *     override suspend fun provideGlance(context: Context, id: GlanceId) {
 *         // 화면이 켜져있을 때만 업데이트
 *         if (!context.isScreenOn()) {
 *             return // 배터리 절약
 *         }
 *         
 *         // 필수 데이터만 로드
 *         val minimalData = loadEssentialData()
 *         provideContent { MinimalWidget(minimalData) }
 *     }
 * }
 * ```
 * 
 * ⚡ 메모리 최적화:
 * ```kotlin
 * // 큰 이미지는 적절한 크기로 리사이징
 * val scaledBitmap = Bitmap.createScaledBitmap(
 *     originalBitmap,
 *     widgetWidth,
 *     widgetHeight,
 *     true
 * )
 * 
 * // 메모리 해제
 * originalBitmap.recycle()
 * ```
 * 
 * ⚡ 네트워크 최적화:
 * ```kotlin
 * class NetworkOptimizedWidget : GlanceAppWidget() {
 *     override suspend fun provideGlance(context: Context, id: GlanceId) {
 *         // WiFi 연결 시에만 대용량 데이터 로드
 *         val networkInfo = context.getNetworkInfo()
 *         
 *         val data = when {
 *             networkInfo.isWiFi -> loadFullData()
 *             networkInfo.isMobile -> loadLightData()
 *             else -> getCachedData()
 *         }
 *         
 *         provideContent { AdaptiveWidget(data) }
 *     }
 * }
 * ```
 * 
 * =================================================================================================
 * 🎨 UX 디자인 가이드라인
 * =================================================================================================
 * 
 * 🎭 Widget 디자인 원칙:
 * - 한눈에 파악 가능한 정보
 * - 터치하기 쉬운 충분한 버튼 크기 (최소 48dp)
 * - 명확한 시각적 피드백
 * - 브랜드 일관성 유지
 * 
 * 🌈 색상 및 테마:
 * ```kotlin
 * @Composable
 * fun ThemedWidget() {
 *     // 시스템 다크모드 감지
 *     val isDarkTheme = LocalContext.current.isDarkTheme()
 *     
 *     val backgroundColor = if (isDarkTheme) {
 *         ColorProvider(Color(0xFF1E1E1E))
 *     } else {
 *         ColorProvider(Color.White)
 *     }
 *     
 *     val textColor = if (isDarkTheme) {
 *         ColorProvider(Color.White)
 *     } else {
 *         ColorProvider(Color.Black)
 *     }
 *     
 *     Box(
 *         modifier = GlanceModifier.background(backgroundColor)
 *     ) {
 *         Text(
 *             text = "Hello World",
 *             style = TextStyle(color = textColor)
 *         )
 *     }
 * }
 * ```
 * 
 * 🎨 다양한 화면 크기 대응:
 * ```kotlin
 * @Composable
 * fun ResponsiveDesign(size: DpSize) {
 *     when {
 *         size.width >= 300.dp && size.height >= 200.dp -> {
 *             // 대형 위젯 - 상세 정보 표시
 *             DetailedWidget()
 *         }
 *         size.width >= 180.dp -> {
 *             // 중형 위젯 - 핵심 정보만
 *             CompactWidget()
 *         }
 *         else -> {
 *             // 소형 위젯 - 최소 정보
 *             MinimalWidget()
 *         }
 *     }
 * }
 * ```
 * 
 * =================================================================================================
 * 🔍 디버깅 및 테스트 가이드
 * =================================================================================================
 * 
 * 🧪 Widget 테스트 전략:
 * ```kotlin
 * @RunWith(AndroidJUnit4::class)
 * class WidgetTest {
 *     @Test
 *     fun testWidgetContent() = runTest {
 *         val context = InstrumentationRegistry.getInstrumentation().context
 *         val glanceId = GlanceId("test_widget")
 *         
 *         val widget = StreaksWidget()
 *         
 *         // Widget 콘텐츠 테스트
 *         widget.provideGlance(context, glanceId)
 *         
 *         // 검증 로직
 *     }
 * }
 * ```
 * 
 * 🔧 디버깅 도구:
 * ```kotlin
 * class DebuggableWidget : GlanceAppWidget() {
 *     override suspend fun provideGlance(context: Context, id: GlanceId) {
 *         Log.d("Widget", "Updating widget: $id")
 *         
 *         try {
 *             val data = loadData()
 *             Log.d("Widget", "Data loaded: $data")
 *             
 *             provideContent { 
 *                 WidgetContent(data)
 *             }
 *         } catch (e: Exception) {
 *             Log.e("Widget", "Error updating widget", e)
 *             provideContent { 
 *                 ErrorWidget(e.message ?: "Unknown error")
 *             }
 *         }
 *     }
 * }
 * ```
 * 
 * =================================================================================================
 * 🚨 주의사항 및 제한사항
 * =================================================================================================
 * 
 * ⚠️ Glance 제약사항:
 * - 제한된 Composable API (Custom drawing 불가)
 * - 복잡한 상태 관리 어려움
 * - 애니메이션 지원 제한적
 * - 일부 Modifier 미지원
 * 
 * ⚠️ Android 시스템 제약:
 * - Widget 업데이트 빈도 제한 (최소 30분)
 * - 메모리 사용량 제한
 * - 네트워크 작업 시간 제한
 * - 백그라운드 실행 제약
 * 
 * ⚠️ 성능 고려사항:
 * - 큰 이미지나 복잡한 UI 피하기
 * - 과도한 네트워크 요청 방지
 * - 사용자 인터랙션 응답성 보장
 * - 배터리 소모 최소화
 * 
 * =================================================================================================
 * 🔀 위젯 크기별 구현 전략 및 차이점 분석
 * =================================================================================================
 * 
 * 📊 구현된 4가지 위젯 비교:
 * 
 * 1️⃣ **StreaksWidget (원본 - 크기 가변형)**
 * ────────────────────────────────────────────────────────────────────────────────────────────
 * 🎯 특징:
 * - LocalSize.current를 활용한 동적 크기 감지
 * - when 조건문으로 3가지 레이아웃 자동 선택
 * - resizeMode="horizontal|vertical" (사용자가 크기 조정 가능)
 * - 하나의 위젯으로 모든 크기 대응
 * 
 * 💡 핵심 구현:
 * ```kotlin
 * @Composable
 * private fun StreakContent(content: WeeklyPostingStreak) {
 *     val size = LocalSize.current
 *     
 *     when {
 *         size.width >= 320.dp && size.height >= 120.dp -> LargeLayout()
 *         size.width >= 180.dp && size.height >= 120.dp -> MediumLayout()
 *         else -> SmallLayout()
 *     }
 * }
 * ```
 * 
 * ✅ 장점:
 * - 하나의 위젯으로 모든 크기 처리
 * - 사용자가 원하는 크기로 자유 조정
 * - 코드 중복 최소화
 * - 유지보수 용이
 * 
 * ❌ 단점:
 * - 크기 감지 로직의 복잡성
 * - 디바이스/런처별 크기 감지 차이
 * - 예상치 못한 레이아웃 전환 가능성
 * 
 * 2️⃣ **SmallWidget (1x1 고정형)**
 * ────────────────────────────────────────────────────────────────────────────────────────────
 * 🎯 특징:
 * - 1x1 크기로 완전 고정 (80dp x 80dp)
 * - resizeMode="none" (크기 변경 불가)
 * - 최소한의 정보만 표시 (아이콘 + 시간)
 * - 빨간색 배경으로 시각적 구분
 * 
 * 💡 구현 전략:
 * ```kotlin
 * // small_widget_info.xml
 * android:minWidth="80dp"
 * android:minHeight="80dp"
 * android:targetCellWidth="1"
 * android:targetCellHeight="1"
 * android:resizeMode="none"  // 크기 고정!
 * ```
 * 
 * 3️⃣ **MediumWidget (2x2 고정형)**
 * ────────────────────────────────────────────────────────────────────────────────────────────
 * 🎯 특징:
 * - 2x2 크기로 완전 고정 (180dp x 120dp)
 * - 세로 중심 레이아웃
 * - 적당한 정보량 (아이콘 + 제목 + 정보 + 버튼)
 * - 초록색 배경으로 시각적 구분
 * 
 * 4️⃣ **LargeWidget (3x2+ 고정형)**
 * ────────────────────────────────────────────────────────────────────────────────────────────
 * 🎯 특징:
 * - 3x2+ 크기로 완전 고정 (280dp x 120dp)
 * - 좌우 분할 레이아웃 (대시보드 스타일)
 * - 풍부한 정보 표시
 * - 파란색 배경으로 시각적 구분
 * 
 * =================================================================================================
 * 🔧 크기 변경 제한의 기술적 이유
 * =================================================================================================
 * 
 * ❓ **왜 새로 추가한 3개 위젯은 크기 변경이 안 될까?**
 * 
 * 🔐 **android:resizeMode 속성의 차이**
 * ```xml
 * <!-- 기존 StreaksWidget (크기 변경 가능) -->
 * <appwidget-provider>
 *     android:resizeMode="horizontal|vertical"  ✅ 크기 조정 허용
 *     android:minResizeWidth="180dp"
 *     android:minResizeHeight="40dp"
 * </appwidget-provider>
 * 
 * <!-- 새로운 고정 위젯들 (크기 변경 불가) -->
 * <appwidget-provider>
 *     android:resizeMode="none"  ❌ 크기 조정 금지
 * </appwidget-provider>
 * ```
 * 
 * 🎯 **설계 의도:**
 * - **일관된 UX**: 각 위젯이 특정 크기에 최적화된 UI 제공
 * - **예측 가능성**: 사용자가 선택한 크기 그대로 유지
 * - **단순성**: 복잡한 크기 감지 로직 불필요
 * - **성능**: 크기별 최적화된 레이아웃으로 더 나은 성능
 * 
 * 📊 **크기별 최적화 전략:**
 * ```kotlin
 * // 1x1: 최소한의 정보
 * Column(centerAlignment) {
 *     Icon(large)
 *     Label(small)
 *     Time(tiny)
 * }
 * 
 * // 2x2: 균형잡힌 정보
 * Column(centerAlignment) {
 *     Icon(medium)
 *     Title(medium)
 *     Details(small)
 *     Button(standard)
 * }
 * 
 * // 3x2+: 풍부한 정보
 * Row {
 *     Column(details) // 좌측
 *     Column(actions) // 우측
 * }
 * ```
 * 
 * 🔄 **크기 변경을 허용하려면:**
 * ```xml
 * <!-- 크기 변경 허용 설정 -->
 * <appwidget-provider>
 *     android:resizeMode="horizontal|vertical"
 *     android:minWidth="80dp"
 *     android:minHeight="80dp"
 *     android:minResizeWidth="80dp"
 *     android:minResizeHeight="80dp"
 *     android:maxWidth="400dp"
 *     android:maxHeight="300dp"
 * </appwidget-provider>
 * ```
 * 
 * 💡 **실무 권장사항:**
 * - **고정 크기**: 특정 용도로 최적화된 위젯 (시계, 상태 표시 등)
 * - **가변 크기**: 범용적이고 유연한 위젯 (날씨, 뉴스 등)
 * - **하이브리드**: 최소/최대 크기 제한으로 적절한 범위 내에서만 조정 허용
 * 
 * =================================================================================================
 * 🎨 위젯 선택 가이드라인
 * =================================================================================================
 * 
 * 📋 **언제 어떤 위젯을 사용할까?**
 * 
 * 🎯 **1x1 SmallWidget 사용 시점:**
 * - 상태 표시기 (온라인/오프라인, 알림 개수)
 * - 간단한 토글 버튼
 * - 앱 런처 역할
 * - 배터리 절약이 중요한 경우
 * 
 * 🎯 **2x2 MediumWidget 사용 시점:**
 * - 날씨 정보 (온도 + 상태)
 * - 시계 + 날짜 조합
 * - 음악 플레이어 컨트롤
 * - 가장 일반적인 위젯 크기
 * 
 * 🎯 **3x2+ LargeWidget 사용 시점:**
 * - 뉴스 헤드라인 여러 개
 * - 캘린더 일정 목록
 * - 대시보드형 정보 표시
 * - 복잡한 데이터 시각화
 * 
 * 🎯 **가변 크기 StreaksWidget 사용 시점:**
 * - 사용자 맞춤형 위젯
 * - 다양한 디바이스 대응 필요
 * - 하나의 위젯으로 여러 용도 지원
 * - 개발 리소스 절약이 중요한 경우
 * 
 * =================================================================================================
 * 💻 실습 과제
 * =================================================================================================
 * 
 * 🎯 초급 과제:
 * 1. 현재 시간을 표시하는 간단한 시계 위젯 (1x1 크기 활용)
 * 2. 클릭 시 앱을 여는 로고 위젯 (SmallWidget 기반)
 * 3. 배터리 상태 표시 위젯 (아이콘 + 퍼센트)
 * 
 * 🎯 중급 과제:
 * 1. RSS 피드를 표시하는 뉴스 위젯 (MediumWidget 활용)
 * 2. 사용자 위치 기반 날씨 위젯 (2x2 최적화)
 * 3. 할 일 목록 표시 및 체크 기능 위젯
 * 4. 고정 크기 vs 가변 크기 위젯 비교 구현
 * 
 * 🎯 고급 과제:
 * 1. 다중 크기 지원하는 대시보드 위젯 (StreaksWidget 방식)
 * 2. 실시간 주식 정보 표시 위젯 (LargeWidget 활용)
 * 3. 미디어 플레이어 컨트롤 위젯 (크기별 다른 컨트롤)
 * 4. 캘린더 일정 표시 위젯 (3x2+ 대시보드형)
 * 5. 4개 위젯 통합 관리 시스템 구현
 * 
 * 🎯 실전 응용 과제:
 * 1. 사용자 설정에 따라 위젯 크기 자동 선택하는 스마트 위젯
 * 2. 컨텍스트(시간, 위치, 상황)에 따라 레이아웃 변경하는 적응형 위젯
 * 3. 위젯 크기별 성능 최적화와 배터리 효율성 비교 분석
 * 
 * =================================================================================================
 * 🔮 향후 발전 방향
 * =================================================================================================
 * 
 * 🚀 예상되는 개선사항:
 * - 더 많은 Composable 지원
 * - 향상된 애니메이션 기능  
 * - 더 유연한 상태 관리
 * - 성능 최적화 개선
 * - 크기 감지 API 안정화 (LocalSize 개선)
 * - 더 정교한 반응형 레이아웃 지원
 * 
 * 💡 활용 확장 가능성:
 * - Wear OS 위젯 지원
 * - 자동차 대시보드 통합  
 * - IoT 기기 제어 위젯
 * - AR/VR 환경 지원
 * - 폴더블 디바이스 적응형 위젯
 * - AI 기반 스마트 위젯 크기 추천
 * 
 * 📈 **구현된 4가지 위젯의 발전 방향:**
 * - **SmallWidget**: 상태 표시 전문 위젯으로 진화
 * - **MediumWidget**: 표준 정보 위젯의 템플릿 역할
 * - **LargeWidget**: 복잡한 대시보드 위젯의 기반
 * - **StreaksWidget**: 범용 적응형 위젯의 레퍼런스
 */
object GlanceWidgetExampleGuide 