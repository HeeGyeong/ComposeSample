package com.example.composesample.presentation.example.component.architecture.pattern.compositionLocal

/**
 * Static vs Dynamic CompositionLocal Guide
 * 
 * 출처: https://proandroiddev.com/jetpack-compose-static-vs-dynamic-compositionlocals-its-not-about-change-frequency-81f56b3dd991
 * 
 * === CompositionLocal의 Static vs Dynamic 이해하기 ===
 * 
 * 1. 핵심 오해: "변경 빈도"가 아니다!
 *    - ❌ 잘못된 이해: "자주 변경되면 Dynamic, 안 변경되면 Static"
 *    - ✅ 올바른 이해: "리컴포지션 스코프의 차이"
 *    - 변경 빈도는 선택 기준이 아님
 *    - 리컴포지션 전파 방식의 차이가 핵심
 * 
 * 2. staticCompositionLocalOf의 특징
 *    - 값 변경 시: Provider 이하 전체 리컴포지션
 *    - 읽기 추적: CompositionLocal을 읽는 부분을 추적하지 않음
 *    - 사용 시기: 거의 변경되지 않거나, 전체 리컴포지션이 필요한 경우
 *    - 예시: 테마 설정, 언어 설정, 전역 구성
 * 
 * 3. compositionLocalOf의 특징 (Dynamic)
 *    - 값 변경 시: 실제로 읽는 컴포저블만 리컴포지션
 *    - 읽기 추적: 각 읽기 위치를 정밀하게 추적
 *    - 사용 시기: 값이 자주 변경되고, 부분 리컴포지션이 중요한 경우
 *    - 예시: 현재 사용자 데이터, UI 상태, 동적 설정
 * 
 * 4. 리컴포지션 스코프 차이 시각화
 *    Static:
 *      CompositionLocalProvider (변경 감지)
 *        └─> 전체 하위 트리 리컴포지션
 *              ├─> Component A (리컴포지션 ○)
 *              ├─> Component B (리컴포지션 ○)
 *              └─> Component C (리컴포지션 ○)
 *    
 *    Dynamic:
 *      CompositionLocalProvider (변경 감지)
 *        └─> 읽는 컴포저블만 리컴포지션
 *              ├─> Component A (읽음, 리컴포지션 ○)
 *              ├─> Component B (안 읽음, 리컴포지션 ✗)
 *              └─> Component C (읽음, 리컴포지션 ○)
 * 
 * 5. 성능 영향 분석
 *    Static:
 *      - 초기 비용: 낮음 (읽기 추적 오버헤드 없음)
 *      - 변경 비용: 높음 (전체 하위 트리 리컴포지션)
 *      - 메모리: 적음 (추적 정보 저장 불필요)
 *    
 *    Dynamic:
 *      - 초기 비용: 약간 높음 (읽기 추적 설정)
 *      - 변경 비용: 낮음 (필요한 부분만 리컴포지션)
 *      - 메모리: 약간 많음 (읽기 위치 추적 정보)
 * 
 * 6. 실제 사용 사례
 *    Static 추천:
 *      - MaterialTheme: 앱 전체 테마
 *      - LocalConfiguration: 화면 구성 정보
 *      - LocalContext: Android Context
 *      - LocalDensity: 화면 밀도
 *      - 앱 시작 시 설정되고 거의 변경 안 됨
 *    
 *    Dynamic 추천:
 *      - LocalUser: 현재 사용자 정보
 *      - LocalContentAlpha: 콘텐츠 투명도
 *      - LocalTextStyle: 텍스트 스타일
 *      - UI 인터랙션에 따라 자주 변경됨
 * 
 * 7. 선택 기준 가이드라인
 *    Static을 선택하는 경우:
 *      ✓ 값이 앱 라이프사이클 동안 한두 번만 변경
 *      ✓ 변경 시 전체 UI 갱신이 자연스러운 경우
 *      ✓ 거의 모든 하위 컴포저블이 값을 사용
 *      ✓ 초기 성능 최적화가 중요
 *    
 *    Dynamic을 선택하는 경우:
 *      ✓ 값이 사용자 인터랙션에 따라 자주 변경
 *      ✓ 일부 컴포저블만 값을 사용
 *      ✓ 부분 리컴포지션이 성능에 중요
 *      ✓ 읽기 위치가 동적으로 변할 수 있음
 * 
 * 8. 실수하기 쉬운 패턴
 *    ❌ 잘못된 패턴 1: 자주 변경되는 값을 Static으로
 *       val LocalCounter = staticCompositionLocalOf { 0 }
 *       // 카운터가 증가할 때마다 전체 트리 리컴포지션!
 *    
 *    ❌ 잘못된 패턴 2: 거의 안 변경되는 값을 Dynamic으로
 *       val LocalAppName = compositionLocalOf { "MyApp" }
 *       // 불필요한 읽기 추적 오버헤드
 *    
 *    ✅ 올바른 패턴: 변경 패턴에 맞는 선택
 *       val LocalTheme = staticCompositionLocalOf { LightTheme }
 *       val LocalUserState = compositionLocalOf { UserState() }
 * 
 * 9. 마이그레이션 전략
 *    Static → Dynamic으로 변경이 필요한 경우:
 *      1. 성능 프로파일링으로 병목 지점 확인
 *      2. 값 변경 빈도 측정
 *      3. 하위 컴포저블 중 실제 사용 비율 확인
 *      4. 단계적 마이그레이션 (한 번에 하나씩)
 *      5. 변경 후 성능 비교
 *    
 *    Dynamic → Static으로 변경이 필요한 경우:
 *      1. 실제 변경 빈도 확인 (예상보다 낮을 수 있음)
 *      2. 추적 오버헤드 측정
 *      3. 전체 리컴포지션 비용 평가
 *      4. A/B 테스트로 실제 성능 비교
 * 
 * 10. 디버깅 팁
 *     - Layout Inspector: 리컴포지션 횟수 확인
 *     - Composition Tracing: 리컴포지션 원인 추적
 *     - 로그 추가: Provider 값 변경 시점 기록
 *     - 성능 프로파일러: 실제 성능 영향 측정
 * 
 * 11. 조합 패턴
 *     Static + Dynamic 혼합 사용:
 *       val LocalTheme = staticCompositionLocalOf { Theme() }
 *       val LocalThemeColors = compositionLocalOf { Colors() }
 *       
 *       // Theme는 거의 변경 안 됨 (Static)
 *       // Colors는 다크모드 전환 등으로 자주 변경 (Dynamic)
 * 
 * 12. 테스트 고려사항
 *     - Preview에서: 기본값 설정 중요
 *     - Unit Test: Provider 없이도 동작하도록 기본값 제공
 *     - Integration Test: 값 변경 시 올바른 리컴포지션 확인
 *     - Performance Test: 예상한 성능 특성 검증
 * 
 * 13. 메모리 프로파일링
 *     Static:
 *       - Snapshot 생성 비용: 낮음
 *       - 추적 데이터: 없음
 *       - 총 메모리 사용: 기본적
 *     
 *     Dynamic:
 *       - Snapshot 생성 비용: 중간
 *       - 추적 데이터: 읽기 위치마다 저장
 *       - 총 메모리 사용: 추적 오버헤드 포함
 * 
 * 14. Compose 컴파일러 최적화
 *     - Static: 컴파일 타임에 더 많은 최적화 가능
 *     - Dynamic: 런타임 추적으로 정밀한 무효화
 *     - Stability: 안정적인 타입일수록 최적화 향상
 * 
 * 15. 실전 베스트 프랙티스
 *     ✓ 기본은 Dynamic: 확신이 없으면 compositionLocalOf 사용
 *     ✓ 프로파일링 우선: 추측이 아닌 측정으로 결정
 *     ✓ 문서화: 왜 Static/Dynamic을 선택했는지 주석 작성
 *     ✓ 단순하게: 복잡한 최적화보다 명확한 코드 우선
 *     ✓ 모니터링: 프로덕션에서 실제 성능 추적
 * 
 * 16. 중첩된 Provider 패턴
 *     부모 Provider가 Static일 때:
 *       - 부모 값 변경 시 전체 하위 트리 리컴포지션
 *       - 자식 Provider가 Dynamic이어도 영향받음
 *       - 자식까지 전부 무효화됨
 *     
 *     부모 Provider가 Dynamic일 때:
 *       - 부모 값 변경 시 읽는 부분만 리컴포지션
 *       - 자식 Provider는 독립적으로 동작
 *       - 최적화된 리컴포지션 가능
 *     
 *     설계 가이드:
 *       ✓ Static은 최상위에 배치 (Theme, Context)
 *       ✓ Dynamic은 하위에 배치 (State, Data)
 *       ✓ 중첩 깊이를 최소화
 * 
 * 17. 조건부 읽기와 Lazy Subscription
 *     compositionLocalOf의 특별한 특징:
 *       - .current 호출 시점에 구독 시작
 *       - if문 안에서만 읽으면 조건부 구독
 *       - false 브랜치에서는 값 변경에 반응 안 함
 *     
 *     예시:
 *       val value = if (shouldRead) {
 *           LocalData.current  // 이 브랜치일 때만 구독
 *       } else {
 *           defaultValue  // 이 브랜치에서는 구독 안 함
 *       }
 *     
 *     활용:
 *       ✓ 조건부 기능에서 불필요한 리컴포지션 방지
 *       ✓ 성능 최적화
 *       ✓ 동적 UI 상태 관리
 */

object StaticDynamicCompositionLocalGuide {
    const val GUIDE_INFO = """
        Static vs Dynamic CompositionLocal
        
        핵심 개념:
        - Static: 전체 하위 트리 리컴포지션
        - Dynamic: 읽는 컴포저블만 리컴포지션
        - 선택 기준: 변경 빈도가 아닌 리컴포지션 패턴
        - Static: 거의 변경 안 됨 + 전체 사용
        - Dynamic: 자주 변경 + 부분 사용
        
        성능 트레이드오프:
        - Static: 초기 빠름, 변경 시 느림
        - Dynamic: 초기 약간 느림, 변경 시 빠름
        
        블로그: https://proandroiddev.com/jetpack-compose-static-vs-dynamic-compositionlocals-its-not-about-change-frequency-81f56b3dd991
    """
    
    const val STATIC_CHARACTERISTICS = """
        === staticCompositionLocalOf 특징 ===
        
        1. 리컴포지션 동작:
           - 값 변경 시 Provider 이하 전체 리컴포지션
           - 읽기 위치 추적하지 않음
           - 모든 하위 컴포저블 무효화
        
        2. 성능 프로필:
           - 초기 비용: 낮음 (추적 오버헤드 없음)
           - 변경 비용: 높음 (전체 리컴포지션)
           - 메모리: 적음
        
        3. 사용 시기:
           ✓ 앱 라이프사이클 동안 1-2번만 변경
           ✓ 거의 모든 하위 컴포저블이 사용
           ✓ 변경 시 전체 UI 갱신이 자연스러움
           ✓ MaterialTheme, Context 등
        
        4. 예시 코드:
           val LocalTheme = staticCompositionLocalOf { LightTheme }
           
           CompositionLocalProvider(LocalTheme provides DarkTheme) {
               // 테마 변경 시 이 블록 전체 리컴포지션
               AppContent()
           }
    """
    
    const val DYNAMIC_CHARACTERISTICS = """
        === compositionLocalOf 특징 (Dynamic) ===
        
        1. 리컴포지션 동작:
           - 값 변경 시 실제로 읽는 곳만 리컴포지션
           - 각 읽기 위치를 정밀하게 추적
           - 부분 무효화로 성능 최적화
        
        2. 성능 프로필:
           - 초기 비용: 약간 높음 (추적 설정)
           - 변경 비용: 낮음 (부분 리컴포지션)
           - 메모리: 약간 많음 (추적 데이터)
        
        3. 사용 시기:
           ✓ 사용자 인터랙션으로 자주 변경
           ✓ 일부 컴포저블만 값 사용
           ✓ 부분 리컴포지션이 성능에 중요
           ✓ 사용자 데이터, UI 상태 등
        
        4. 예시 코드:
           val LocalUser = compositionLocalOf { User() }
           
           CompositionLocalProvider(LocalUser provides currentUser) {
               // 사용자 변경 시 LocalUser.current를
               // 읽는 컴포저블만 리컴포지션
               UserProfile()
               Settings()
           }
    """
    
    const val DECISION_GUIDE = """
        === 선택 가이드라인 ===
        
        Static을 선택하세요:
        1. 값이 거의 변경되지 않음 (앱당 1-2회)
        2. 대부분의 하위 컴포저블이 값 사용
        3. 변경 시 전체 UI 새로고침이 자연스러움
        4. 예: Theme, Configuration, Context
        
        Dynamic을 선택하세요:
        1. 값이 자주 변경됨 (사용자 인터랙션)
        2. 일부 컴포저블만 값 사용
        3. 부분 업데이트가 성능에 중요
        4. 예: User state, UI state, Alpha
        
        확신이 없다면:
        → Dynamic(compositionLocalOf) 사용 권장
        → 프로파일링 후 필요시 Static으로 최적화
    """
    
    const val COMMON_MISTAKES = """
        === 흔한 실수들 ===
        
        ❌ 실수 1: 자주 변경되는 값을 Static으로
           val LocalCounter = staticCompositionLocalOf { 0 }
           
           // 카운터 증가할 때마다 전체 트리 리컴포지션!
           // 성능 저하 발생
        
        ❌ 실수 2: 거의 안 변경되는 값을 Dynamic으로
           val LocalAppConfig = compositionLocalOf { Config() }
           
           // 불필요한 읽기 추적 오버헤드
           // 메모리 낭비
        
        ❌ 실수 3: 변경 빈도만 고려
           "이 값은 자주 변경되니까 Dynamic"
           → 잘못된 접근!
           → 리컴포지션 패턴도 함께 고려해야 함
        
        ✅ 올바른 패턴:
           // 테마: 거의 안 변경 + 전체 사용
           val LocalTheme = staticCompositionLocalOf { Theme() }
           
           // 사용자: 자주 변경 + 부분 사용
           val LocalUser = compositionLocalOf { User() }
    """
    
    const val PERFORMANCE_TIPS = """
        === 성능 최적화 팁 ===
        
        1. 프로파일링 우선:
           - Layout Inspector로 리컴포지션 횟수 확인
           - 실제 측정 데이터로 결정
           - 추측보다 측정
        
        2. 계층 구조 최적화:
           - 자주 변경되는 값은 하위에 배치
           - Static은 상위, Dynamic은 하위
           - 변경 범위 최소화
        
        3. 혼합 사용:
           val LocalTheme = staticCompositionLocalOf { }
           val LocalColors = compositionLocalOf { }
           
           // Theme은 Static, Colors는 Dynamic
           // 다크모드 전환 시 Colors만 업데이트
        
        4. 기본값 설정:
           - Preview와 테스트를 위해 의미 있는 기본값
           - null 대신 실제 기본 객체 제공
           - 안전한 폴백 보장
    """
    
    const val NESTED_PROVIDER_GUIDE = """
        === 중첩된 Provider 패턴 ===
        
        1. Static 부모의 영향:
           CompositionLocalProvider(LocalStaticTheme provides theme) {
               CompositionLocalProvider(LocalDynamicUser provides user) {
                   Content()  // theme 변경 시 전부 리컴포지션
               }
           }
           
           - Static 부모 변경 → 전체 하위 트리 무효화
           - 자식이 Dynamic이어도 소용없음
           - 성능에 큰 영향
        
        2. Dynamic 부모의 독립성:
           CompositionLocalProvider(LocalDynamicUser provides user) {
               CompositionLocalProvider(LocalDynamicTheme provides theme) {
                   Content()  // user/theme 각각 독립적
               }
           }
           
           - 각 Provider가 독립적으로 동작
           - 읽는 부분만 리컴포지션
           - 최적화된 성능
        
        3. 설계 원칙:
           ✓ Static을 최상위에 배치 (앱 전역 설정)
           ✓ Dynamic을 하위에 배치 (화면별 상태)
           ✓ 중첩 깊이 최소화
           ✓ Provider 계층 구조 문서화
    """
    
    const val CONDITIONAL_READING_GUIDE = """
        === 조건부 읽기와 Lazy Subscription ===
        
        1. Lazy Subscription 동작:
           @Composable
           fun ConditionalReader(enabled: Boolean) {
               val data = if (enabled) {
                   LocalData.current  // enabled=true일 때만 구독
               } else {
                   null  // enabled=false면 구독 안 함
               }
           }
           
           - .current 호출 시점에 구독 시작
           - 조건문 안에서만 호출하면 조건부 구독
           - 불필요한 리컴포지션 방지
        
        2. 실전 활용 사례:
           // 권한 기반 데이터 접근
           val userData = if (hasPermission) {
               LocalUserData.current
           } else {
               null
           }
           
           // 기능 플래그
           val feature = if (isFeatureEnabled) {
               LocalFeature.current
           } else {
               defaultFeature
           }
        
        3. 주의사항:
           ✗ 잘못된 패턴:
             val data = LocalData.current
             if (enabled) { use(data) }
             // data를 무조건 읽음 → 항상 구독됨
           
           ✓ 올바른 패턴:
             if (enabled) {
                 val data = LocalData.current
                 use(data)
             }
             // enabled=true일 때만 구독
        
        4. 성능 이점:
           - 조건부 기능의 불필요한 리컴포지션 방지
           - 권한/플래그 기반 UI에서 유용
           - 메모리 사용 최적화
    """
    
    const val EXAMPLE_REFERENCE = """
        === 실전 예제 참고 ===
        
        이 가이드의 개념들은 다음 예제에서 시각적으로 확인할 수 있습니다:
        
        1. StaticDynamicCompositionLocalExampleUI
           - ControlPanel: Static/Dynamic 카운터 제어
           - StaticCompositionLocalDemo: 전체 리컴포지션 동작
           - DynamicCompositionLocalDemo: 부분 리컴포지션 동작
           - NestedProviderDemo: 중첩된 Provider 리컴포지션 전파
           - ConditionalReadingDemo: 조건부 읽기 lazy subscription
        
        2. 예제에서 확인 가능한 내용:
           ✓ Static과 Dynamic의 리컴포지션 횟수 차이
           ✓ Reader vs Non-Reader 컴포넌트 동작
           ✓ 중첩 Provider의 리컴포지션 전파
           ✓ 조건부 읽기 시 구독 on/off
        
        3. 실험 방법:
           - Static Counter 증가 → 모든 Static 컴포넌트 리컴포지션
           - Dynamic Counter 증가 → Reader만 리컴포지션
           - 중첩 예제에서 부모 영향 확인
           - 조건부 읽기 토글로 lazy subscription 확인
    """
    
    const val EXAMPLE_IMPLEMENTATION_DETAILS = """
        === 예제 구현 상세 설명 ===
        
        파일: StaticDynamicCompositionLocalExampleUI.kt
        
        1. CompositionLocal 선언
           val LocalStaticCounter = staticCompositionLocalOf { 0 }
           val LocalDynamicCounter = compositionLocalOf { 0 }
           val LocalNestedValue = compositionLocalOf { 0 }
           
           - LocalStaticCounter: Static 데모용 카운터
           - LocalDynamicCounter: Dynamic 데모용 카운터
           - LocalNestedValue: 중첩 예제용 Dynamic 카운터
        
        2. ControlPanel 구현
           기능:
           - Static/Dynamic 카운터 값 표시
           - +1 버튼: 각 카운터 증가
           - Reset 버튼: 카운터를 0으로 리셋
           - 경고 메시지: 리컴포지션 동작 차이 설명
           
           역할:
           - 사용자가 직접 카운터를 조작하여 리컴포지션 관찰
           - Static/Dynamic 동작 차이를 실시간 확인
        
        3. StaticCompositionLocalDemo 구현
           구조:
           - StaticReaderComponent (A, C): LocalStaticCounter.current 읽음
           - StaticNonReaderComponent (B): 값을 읽지 않음
           
           동작:
           - Static Counter 증가 시 A, B, C 모두 리컴포지션
           - LaunchedEffect로 리컴포지션 횟수 카운트
           - DisposableEffect로 Non-Reader도 리컴포지션됨을 증명
           
           핵심 구현:
           StaticNonReaderComponent에서:
           - LocalStaticCounter.current 호출 안 함
           - 하지만 currentRecomposeScope + DisposableEffect로
             부모 Provider 변경 시 리컴포지션됨을 감지
           - 이것이 Static의 전체 무효화 특성
        
        4. DynamicCompositionLocalDemo 구현
           구조:
           - DynamicReaderComponent (A, C): LocalDynamicCounter.current 읽음
           - DynamicNonReaderComponent (B): 값을 읽지 않음
           
           동작:
           - Dynamic Counter 증가 시 A, C만 리컴포지션
           - B는 값을 읽지 않으므로 리컴포지션 안 됨
           - LaunchedEffect로 리컴포지션 횟수 카운트
           
           핵심 구현:
           DynamicNonReaderComponent에서:
           - LocalDynamicCounter.current 호출 안 함
           - DisposableEffect 사용 안 함
           - 단순히 LaunchedEffect(Unit)로 초기 카운트만
           - Dynamic은 읽기 위치를 추적하므로 리컴포지션 안 됨
        
        5. NestedProviderDemo 구현
           시나리오 1: Static 부모 + Dynamic 자식
           CompositionLocalProvider(LocalStaticCounter provides staticCounter) {
               CompositionLocalProvider(LocalNestedValue provides dynamicCounter) {
                   NestedChild(isStatic = true)
               }
           }
           
           동작:
           - Static Counter 증가 시
           - Static 부모가 전체 하위 트리 무효화
           - Dynamic 자식도 같이 리컴포지션됨
           
           시나리오 2: Dynamic 부모 + Dynamic 자식
           CompositionLocalProvider(LocalDynamicCounter provides dynamicCounter) {
               CompositionLocalProvider(LocalNestedValue provides staticCounter) {
                   NestedChild(isStatic = false)
               }
           }
           
           동작:
           - Dynamic Counter 증가 시
           - 실제로 값을 읽는 NestedChild만 리컴포지션
           - 부분 무효화로 최적화됨
           
           NestedChild 구현:
           - 부모와 중첩 값을 모두 읽음
           - LaunchedEffect(parentCounter, nestedValue)로 추적
           - 리컴포지션 횟수를 표시하여 차이 시각화
        
        6. ConditionalReadingDemo 구현
           상태:
           var showValue by remember { mutableStateOf(false) }
           
           ConditionalReader 구현:
           val displayText = if (showValue) {
               val counter = LocalDynamicCounter.current  // 조건부 구독
               "📖 Counter: ${'$'}counter"
           } else {
               "🚫 값 읽지 않음"  // 구독 안 함
           }
           
           핵심 동작:
           - showValue = false일 때:
             * .current 호출 안 함 → 구독 안 됨
             * Dynamic Counter 증가해도 리컴포지션 안 됨
           
           - showValue = true일 때:
             * .current 호출 → 구독 시작
             * Dynamic Counter 증가 시 리컴포지션됨
           
           - LaunchedEffect로 각 브랜치별 카운트 추적
           - 토글 버튼으로 showValue 전환하며 lazy subscription 확인
        
        7. RecompositionIndicator 구현
           시각적 피드백:
           - label: 컴포넌트 이름 표시
           - value: 읽은 counter 값 또는 "-"
           - recomposeCount: 리컴포지션 횟수 표시
           - color: 타입별 색상 구분 (Static: 빨강, Dynamic: 초록)
           - reads: 값을 읽는지 여부 (읽지 않으면 회색)
           
           애니메이션:
           - animateFloatAsState로 리컴포지션 시 scale 효과
           - Spring 애니메이션으로 부드러운 시각적 피드백
        
        8. 리컴포지션 카운팅 기법
           LaunchedEffect 활용:
           var recomposeCount by remember { mutableStateOf(0) }
           LaunchedEffect(counter) {
               recomposeCount++
           }
           
           - counter 값이 변경될 때마다 LaunchedEffect 재실행
           - recomposeCount 증가로 리컴포지션 횟수 추적
           - remember로 카운트 상태 유지
           
           DisposableEffect 활용 (Static Non-Reader):
           val currentComposition = currentRecomposeScope
           DisposableEffect(currentComposition) {
               recomposeCount++
               onDispose { }
           }
           
           - currentRecomposeScope로 현재 컴포지션 스코프 캡처
           - 부모 리컴포지션 시 DisposableEffect 재실행
           - 값을 읽지 않아도 리컴포지션 감지 가능
        
        9. 색상 체계
           Static 관련: 빨강 계열
           - Primary: Color(0xFFD32F2F)
           - Light: Color(0xFFE57373)
           - Background: Color(0xFFFFEBEE)
           
           Dynamic 관련: 초록 계열
           - Primary: Color(0xFF4CAF50)
           - Light: Color(0xFF81C784)
           - Background: Color(0xFFE8F5E8)
           
           중첩 예제: 노랑 계열
           - Primary: Color(0xFFF57F17)
           - Background: Color(0xFFFFF9C4)
           
           조건부 읽기: 파랑 계열
           - Primary: Color(0xFF0277BD)
           - Background: Color(0xFFE1F5FE)
           
           Non-Reader: 회색
           - Color(0xFF9E9E9E)
        
        10. UI 레이아웃 구조
            LazyColumn {
                item { ControlPanel }          // 제어판
                item { StaticDemo }            // Static 예제
                item { DynamicDemo }           // Dynamic 예제
                item { NestedProviderDemo }    // 중첩 예제
                item { ConditionalReadingDemo } // 조건부 읽기 예제
            }
            
            - 각 예제가 독립적인 Card로 구성
            - 16.dp 간격으로 시각적 구분
            - 스크롤 가능한 레이아웃
    """
}

