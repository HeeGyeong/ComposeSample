package com.example.composesample.presentation.example.component.architecture.pattern.retain

/**
 * Retain API Example 참고 자료
 *
 * 핵심 개념:
 * - RetainedObject: Compose의 remember와 달리 ViewModel 생명주기에 종속되어 화면 회전 후에도 인스턴스 유지
 * - rememberRetained: 컴포저블에서 RetainedObject를 간편하게 사용하기 위한 헬퍼
 * - 사용 시기: 재생성 비용이 높거나 연결 상태를 유지해야 하는 객체 (네트워크, 스트림 등)
 */
object RetainApiGuide {
    val GUIDE_INFO = """
        RetainedObject는 Compose의 remember와 달리
        화면 회전 후에도 인스턴스를 유지합니다.

        • remember: 컴포지션이 살아있는 동안만 유지
        • rememberRetained: ViewModel 생명주기 동안 유지

        재생성 비용이 높거나 연결 상태를 유지해야 하는
        객체(네트워크, 스트림 등)에 적합합니다.
    """
}
