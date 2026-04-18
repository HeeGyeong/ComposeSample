package com.example.composesample.presentation.example.component.ui.notification

/**
 * Compose Snackbar: 실무 활용 가이드
 * 
 * === Compose Snackbar 실전 활용법 ===
 * 
 * 1. 기본 구조와 설정
 *    - SnackbarHostState: Snackbar 상태 관리
 *    - rememberCoroutineScope: 코루틴 스코프 관리
 *    - Scaffold + SnackbarHost: UI 구조 설정
 *    - showSnackbar: 메시지 표시 함수
 * 
 * 2. 핵심 구현 패턴
 *    - 상태 호이스팅: 상위 컴포넌트에서 상태 관리
 *    - 코루틴 활용: 비동기 메시지 처리
 *    - 액션 결과 처리: SnackbarResult로 사용자 반응 감지
 *    - 자동 큐잉: 여러 메시지 순차 처리
 * 
 * 3. 실제 사용 사례
 *    - 폼 검증: 입력 오류 시 즉시 피드백
 *    - 네트워크 작업: 로딩/성공/실패 상태 알림
 *    - 쇼핑카트: 상품 추가/제거 확인 및 실행취소
 *    - 파일 작업: 삭제 확인 및 복구 기능
 *    - 설정 변경: 저장 완료 알림
 * 
 * 4. 인터랙티브 기능
 *    - 액션 버튼: 사용자 추가 행동 유도
 *    - 실행취소: 되돌리기 기능 제공
 *    - 재시도: 실패한 작업 재실행
 *    - 상세보기: 추가 정보 제공
 * 
 * 5. UX 향상 기법
 *    - 이모지 활용: 직관적인 메시지 전달
 *    - 컨텍스트 정보: 현재 상태 표시 (예: 장바구니 개수)
 *    - 진행 상태: 로딩 인디케이터와 함께 사용
 *    - 피드백 체인: 액션 결과에 따른 연속 피드백
 * 
 * 6. 아키텍처 패턴
 *    - 컴포넌트 분리: 재사용 가능한 카드 구조
 *    - 상태 관리: 로컬 상태와 전역 상태 구분
 *    - 이벤트 처리: 사용자 액션에 따른 적절한 응답
 *    - 코루틴 스코프: 생명주기 인식 비동기 처리
 * 
 * 7. 성능 고려사항
 *    - remember 활용: 불필요한 재생성 방지
 *    - 코루틴 취소: 메모리 누수 방지
 *    - 상태 최적화: 필요한 상태만 관리
 *    - 리컴포지션 최소화: 적절한 상태 분리
 * 
 * 8. 테스트 가능한 설계
 *    - 순수 함수: 부작용 최소화
 *    - 상태 분리: 테스트하기 쉬운 구조
 *    - 의존성 주입: 모킹 가능한 설계
 *    - 단위 테스트: 개별 기능 검증
 */

object ComposeSnackbarGuide {
    const val GUIDE_INFO = """
        Compose Snackbar: 실무 활용 가이드
        
        핵심 구성 요소:
        - SnackbarHostState: 상태 관리
        - rememberCoroutineScope: 코루틴 스코프
        - Scaffold + SnackbarHost: UI 구조
        - showSnackbar: 메시지 표시 함수
        - SnackbarResult: 사용자 액션 결과 처리
        - 이모지 활용: 직관적 UX 향상
        - 인터랙티브 기능: 액션 버튼과 피드백 체인
        
        실제 프로덕션 환경에서 검증된 패턴들을 제공합니다.
    """
    
    const val IMPLEMENTATION_PATTERNS = """
        === 기본 구현 패턴 ===
        
        1. 기본 설정:
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            // UI 내용
        }
        
        2. 기본 메시지 표시:
        scope.launch {
            snackbarHostState.showSnackbar("기본 메시지입니다")
        }
        
        3. 액션 버튼 포함:
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "액션이 포함된 메시지입니다",
                actionLabel = "실행"
            )
            if (result == SnackbarResult.ActionPerformed) {
                // 액션 처리
                snackbarHostState.showSnackbar("액션이 실행되었습니다!")
            }
        }
        
        4. 실행취소 패턴:
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "🗑️ 파일이 삭제되었습니다",
                actionLabel = "실행취소"
            )
            if (result == SnackbarResult.ActionPerformed) {
                // 되돌리기 로직
                snackbarHostState.showSnackbar("✅ 파일 삭제가 취소되었습니다")
            }
        }
    """
    
    const val USE_CASES = """
        === 실제 사용 사례들 ===
        
        1. 폼 검증 및 제출
           scope.launch {
               if (formData.email.contains("@")) {
                   snackbarHostState.showSnackbar("✅ 회원가입이 완료되었습니다!")
               } else {
                   snackbarHostState.showSnackbar("❌ 올바른 이메일 주소를 입력해주세요")
               }
           }
        
        2. 쇼핑카트 작업 (실행취소 포함)
           scope.launch {
               val result = snackbarHostState.showSnackbar(
                   message = "🛒 상품이 장바구니에 추가되었습니다",
                   actionLabel = "보기"
               )
               if (result == SnackbarResult.ActionPerformed) {
                   snackbarHostState.showSnackbar("👀 장바구니를 확인하세요!")
               }
           }
        
        3. 네트워크 작업 (로딩 → 완료)
           scope.launch {
               snackbarHostState.showSnackbar("⏳ 데이터를 불러오는 중...")
               delay(3000) // 실제로는 API 호출
               snackbarHostState.showSnackbar("✅ 데이터 로드가 완료되었습니다")
           }
        
        4. 파일 작업 (삭제 → 실행취소)
           scope.launch {
               val result = snackbarHostState.showSnackbar(
                   message = "🗑️ 파일이 삭제되었습니다",
                   actionLabel = "실행취소"
               )
               if (result == SnackbarResult.ActionPerformed) {
                   // 복구 로직 실행
                   snackbarHostState.showSnackbar("✅ 파일 삭제가 취소되었습니다")
               }
           }
    """
    
    const val ARCHITECTURE_GUIDELINES = """
        === 아키텍처 패턴 ===
        
        1. 상태 호이스팅 패턴:
           @Composable
           fun MyScreen() {
               val snackbarHostState = remember { SnackbarHostState() }
               
               Scaffold(
                   snackbarHost = { SnackbarHost(snackbarHostState) }
               ) { paddingValues ->
                   MyContent(
                       onShowSnackbar = { message ->
                           scope.launch {
                               snackbarHostState.showSnackbar(message)
                           }
                       }
                   )
               }
           }
        
        2. 컴포넌트 분리 패턴:
           @Composable
           private fun BasicUsageCard(snackbarHostState: SnackbarHostState) {
               val scope = rememberCoroutineScope()
               // 카드 내용과 Snackbar 로직
           }
        
        3. 실행취소 패턴:
           @Composable
           private fun InteractiveCard(snackbarHostState: SnackbarHostState) {
               var undoCount by remember { mutableStateOf(0) }
               val scope = rememberCoroutineScope()
               
               Button(
                   onClick = {
                       scope.launch {
                           val result = snackbarHostState.showSnackbar(
                               message = "작업이 실행되었습니다",
                               actionLabel = "실행취소"
                           )
                           if (result == SnackbarResult.ActionPerformed) {
                               undoCount++
                               // 되돌리기 로직
                           }
                       }
                   }
               ) { Text("실행") }
           }
        
        4. 상태 관리 패턴:
           @Composable
           fun RealWorldUseCasesCard(
               formData: FormData,
               onFormDataChange: (FormData) -> Unit,
               cartItems: Int,
               onCartChange: (Int) -> Unit,
               snackbarHostState: SnackbarHostState
           ) {
               // 상태와 Snackbar를 함께 관리
           }
    """
}
