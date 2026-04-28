package com.example.composesample.presentation.example.component.data.room

/**
 * Multi-Table Inserts in Room 참고 자료
 * - 출처: https://androidessence.com/multi-table-inserts
 *
 * 핵심 개념
 * - DAO 인터페이스 상속(BaseInsertDao)으로 @Insert 공통 로직을 재사용
 *   → Room KSP는 default 구현이 있는 suspend 함수에 대해 abstract @Insert를 자동 생성
 * - @Transaction 으로 다중 테이블 insert를 원자적으로 묶어
 *   부분 실패 시 전체 롤백되도록 보장
 * - Composite DAO 패턴: 여러 자식 DAO를 추상 프로퍼티로 노출하고
 *   @Transaction 함수에서 자식 DAO의 default insert 를 순차 호출
 */
