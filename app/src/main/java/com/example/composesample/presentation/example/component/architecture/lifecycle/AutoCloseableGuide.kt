package com.example.composesample.presentation.example.component.architecture.lifecycle

/**
 * Automatic Resource Cleanup in Jetpack ViewModels using AutoCloseable Guide
 * 
 * 출처: https://www.paleblueapps.com/rockandnull/automatic-resource-cleanup-android-viewmodel-autocloseable/
 * 
 * === ViewModel에서 AutoCloseable을 활용한 자동 리소스 정리 ===
 * 
 * 1. 전통적인 리소스 정리 방식의 문제점
 *    - onCleared() 오버라이드 필요: 모든 ViewModel에서 반복적으로 구현
 *    - 보일러플레이트 코드: 각 서비스마다 cleanup() 호출 코드 작성
 *    - 누락 가능성: 새로운 서비스 추가 시 cleanup 코드 작성 잊기 쉬움
 *    - 코드 중복: 동일한 패턴이 여러 ViewModel에 반복
 * 
 * 2. AutoCloseable 패턴의 이점
 *    - 자동화: ViewModel 소멸 시 자동으로 close() 호출
 *    - 깔끔한 코드: onCleared() 오버라이드 불필요
 *    - 타입 안전성: 인터페이스 계약으로 close() 구현 강제
 *    - 메모리 누수 방지: 리소스 정리 누락 가능성 제거
 * 
 * 3. AutoCloseable 인터페이스 구현
 *    - Service 인터페이스에 AutoCloseable 상속
 *    - close() 메서드에서 리소스 정리 로직 구현
 *    - CoroutineScope 취소, 구독 해제, 연결 종료 등
 *    - 멱등성 보장: 여러 번 호출되어도 안전하게 동작
 * 
 * 4. ViewModel 생성자에 전달
 *    - ViewModel 슈퍼클래스 생성자에 AutoCloseable 서비스들 전달
 *    - 프레임워크가 자동으로 close() 호출 관리
 *    - 여러 서비스를 동시에 전달 가능
 *    - 순서 보장: 전달된 순서의 역순으로 close() 호출
 * 
 * 5. 실제 구현 예시
 *    - ItemsService: 아이템 관리 서비스 (코루틴 스코프 정리)
 *    - CustomersService: 고객 관리 서비스 (데이터베이스 연결 종료)
 *    - InvoiceService: 인보이스 서비스 (네트워크 리스너 해제)
 *    - 각 서비스는 독립적으로 close() 구현
 * 
 * 6. CoroutineScope 관리
 *    - SupervisorJob: 하나의 Job 실패가 전체에 영향 주지 않음
 *    - Dispatchers 지정: 적절한 스레드에서 작업 실행
 *    - scope.cancel(): close() 메서드에서 스코프 취소
 *    - 자식 코루틴 자동 취소: 스코프 취소 시 모든 자식 취소
 * 
 * 7. 메모리 누수 방지
 *    - 코루틴 취소: 백그라운드 작업 중단
 *    - Flow 구독 해제: 불필요한 데이터 스트림 중단
 *    - 리스너 해제: 이벤트 리스너 및 콜백 정리
 *    - 네트워크 연결 종료: 활성 연결 정리
 * 
 * 8. BaseViewModel 없이 구현
 *    - 기존: BaseViewModel에서 공통 로직 처리
 *    - 새로운 방식: ViewModel 생성자로 AutoCloseable 전달
 *    - 상속 계층 단순화: 불필요한 베이스 클래스 제거
 *    - 조합 우선: 상속 대신 조합으로 기능 구성
 * 
 * 9. 테스트 용이성
 *    - Mock 서비스: AutoCloseable 구현한 Mock 객체 주입
 *    - close() 호출 검증: ViewModel 정리 시 호출 확인
 *    - 독립적 테스트: 각 서비스를 독립적으로 테스트
 *    - 리소스 정리 확인: 테스트 후 리소스 정리 보장
 * 
 * 10. 실제 사용 시나리오
 *     - 데이터베이스 연결: Room, SQLite 연결 정리
 *     - 네트워크 클라이언트: WebSocket, HTTP 연결 종료
 *     - 센서 리스너: 센서 등록 해제
 *     - 위치 업데이트: 위치 리스너 제거
 *     - 미디어 플레이어: 오디오/비디오 리소스 해제
 * 
 * 11. 모범 사례
 *     - 명확한 책임: 각 서비스는 자신의 리소스만 정리
 *     - 멱등성: close()를 여러 번 호출해도 안전
 *     - 로깅: 리소스 정리 시 로그 출력으로 추적 가능
 *     - 예외 처리: close()에서 예외 발생 시 다른 리소스에 영향 없도록
 * 
 * 12. Dependency Injection과 통합
 *     - Hilt: @Provides 메서드에서 AutoCloseable 서비스 제공
 *     - Koin: factory { } 블록에서 AutoCloseable 서비스 생성
 *     - Manual DI: 직접 서비스 인스턴스 생성 및 전달
 *     - 생명주기 관리: DI 컨테이너가 아닌 ViewModel이 관리
 * 
 * 13. 주의사항
 *     - 순서 의존성: 서비스 간 종료 순서가 중요한 경우 고려
 *     - 중첩 리소스: 한 서비스가 다른 서비스에 의존하는 경우 처리
 *     - 예외 전파: close()에서 예외 발생 시 처리 방법
 *     - 타이밍: close()는 메인 스레드에서 호출됨
 * 
 * 14. 성능 이점
 *     - 즉시 정리: ViewModel 소멸 시 즉시 리소스 해제
 *     - 메모리 효율: 불필요한 리소스를 빠르게 해제
 *     - 배터리 절약: 백그라운드 작업 중단으로 배터리 절약
 *     - CPU 사용 감소: 불필요한 연산 중단
 * 
 * 15. 디버깅 팁
 *     - 로그 추가: 각 close() 메서드에 로그 출력
 *     - LeakCanary: 메모리 누수 감지 도구 활용
 *     - Strict Mode: 리소스 누수 경고 활성화
 *     - Profile 도구: Android Profiler로 메모리 사용 추적
 */

object AutoCloseableGuide {
    const val GUIDE_INFO = """
        Automatic Resource Cleanup in ViewModel
        
        핵심 개념:
        - AutoCloseable 인터페이스: 자동 리소스 정리
        - ViewModel 생성자: AutoCloseable 서비스 전달
        - 자동 close() 호출: ViewModel 소멸 시 프레임워크가 처리
        - onCleared() 불필요: 보일러플레이트 코드 제거
        
        주요 이점:
        - 메모리 누수 방지: 자동 리소스 정리
        - 깔끔한 코드: 반복적인 cleanup 코드 제거
        - 타입 안전성: 인터페이스로 계약 강제
        - 테스트 용이성: Mock 객체 주입 간편
        
        블로그: https://www.paleblueapps.com/rockandnull/automatic-resource-cleanup-android-viewmodel-autocloseable/
    """
    
    const val TRADITIONAL_APPROACH = """
        === 전통적인 방식 (Before) ===
        
        class MyViewModel(
            private val itemsService: ItemsService,
            private val customersService: CustomersService
        ) : ViewModel() {
            
            override fun onCleared() {
                super.onCleared()
                // 각 서비스마다 수동으로 정리
                itemsService.cleanup()
                customersService.cleanup()
            }
        }
        
        문제점:
        - onCleared() 오버라이드 필요
        - 각 서비스마다 cleanup() 호출 코드 작성
        - 새로운 서비스 추가 시 정리 코드 누락 가능
        - 모든 ViewModel에 반복되는 패턴
    """
    
    const val AUTOCLOSEABLE_APPROACH = """
        === AutoCloseable 방식 (After) ===
        
        // 1. Service에 AutoCloseable 구현
        interface ItemsService : AutoCloseable {
            val items: Flow<List<Item>>
            suspend fun getItems(): List<Item>
        }
        
        class RealItemsService : ItemsService {
            private val scope = CoroutineScope(
                SupervisorJob() + Dispatchers.Default
            )
            
            override fun close() {
                scope.cancel()
                println("ItemsService closed")
            }
        }
        
        // 2. ViewModel 생성자에 전달
        class HomeViewModel(
            private val itemsService: ItemsService,
            private val customersService: CustomersService,
            private val invoiceService: InvoiceService
        ) : ViewModel(
            itemsService,
            customersService,
            invoiceService
        ) {
            // onCleared() 오버라이드 불필요!
            // 프레임워크가 자동으로 close() 호출
        }
        
        이점:
        - onCleared() 오버라이드 불필요
        - 자동으로 모든 서비스의 close() 호출
        - 새로운 서비스 추가 시 생성자에만 추가
        - 깔끔하고 유지보수하기 쉬운 코드
    """
    
    const val IMPLEMENTATION_DETAILS = """
        === 구현 세부사항 ===
        
        1. AutoCloseable 인터페이스:
           - Kotlin/Java 표준 인터페이스
           - close() 메서드 하나만 정의
           - try-with-resources와 호환
        
        2. ViewModel 생성자:
           - vararg closeables: AutoCloseable?
           - 여러 AutoCloseable 객체 전달 가능
           - null 허용 (선택적 리소스)
        
        3. close() 호출 순서:
           - 전달된 순서의 역순으로 호출
           - 마지막 서비스부터 첫 번째 서비스 순
           - 의존성 고려한 안전한 종료
        
        4. 예외 처리:
           - 한 서비스의 close() 실패해도 계속 진행
           - 모든 서비스의 close() 시도 보장
           - 예외는 로그로 기록
    """
    
    const val REAL_WORLD_EXAMPLES = """
        === 실제 사용 예시 ===
        
        1. 데이터베이스 서비스:
           class DatabaseService : AutoCloseable {
               private val db = Room.databaseBuilder(...)
               
               override fun close() {
                   db.close()
                   println("Database closed")
               }
           }
        
        2. 네트워크 서비스:
           class NetworkService : AutoCloseable {
               private val client = OkHttpClient()
               private val webSocket: WebSocket?
               
               override fun close() {
                   webSocket?.close(1000, "ViewModel cleared")
                   client.dispatcher.executorService.shutdown()
                   println("Network connections closed")
               }
           }
        
        3. 센서 서비스:
           class SensorService : AutoCloseable {
               private val sensorManager: SensorManager
               private val listener: SensorEventListener
               
               override fun close() {
                   sensorManager.unregisterListener(listener)
                   println("Sensor listener unregistered")
               }
           }
        
        4. 위치 서비스:
           class LocationService : AutoCloseable {
               private val locationManager: LocationManager
               private val callback: LocationCallback
               
               override fun close() {
                   locationManager.removeUpdates(callback)
                   println("Location updates stopped")
               }
           }
    """
    
    const val BEST_PRACTICES = """
        === 모범 사례 ===
        
        1. 멱등성 보장:
           private var isClosed = false
           
           override fun close() {
               if (isClosed) return
               isClosed = true
               // 실제 정리 로직
           }
        
        2. 로깅 추가:
           override fun close() {
               Log.d(TAG, "Closing service...")
               // 정리 로직
               Log.d(TAG, "Service closed successfully")
           }
        
        3. 예외 처리:
           override fun close() {
               try {
                   scope.cancel()
               } catch (e: Exception) {
                   Log.e(TAG, "Error closing service", e)
               }
           }
        
        4. 순서 고려:
           // 의존하는 서비스를 먼저 전달
           ViewModel(
               networkService,  // 먼저 종료
               databaseService  // 나중에 종료 (역순)
           )
        
        5. 테스트 지원:
           class MockItemsService : ItemsService {
               var closeCalled = false
               
               override fun close() {
                   closeCalled = true
               }
           }
    """
}
