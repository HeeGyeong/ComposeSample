package com.example.composesample.presentation.example.component.ui.shapes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Card Corners in Jetpack Compose - Complete Implementation Guide
 * 
 * === CardCornersExampleUI.kt 기반 실제 구현 가이드 ===
 * 
 * 1. 기본 Corner 타입들 (BasicCornerTypesCard)
 *    - Convex (Rounded): RoundedCornerShape(16.dp) - 둥근 모서리
 *    - Sharp (90°): RectangleShape - 직각 모서리  
 *    - Cut (Diagonal): CutCornerShape(16.dp) - 잘린 모서리
 *    - Concave (Inward): 시뮬레이션 구현 (실제로는 Custom Path 필요)
 * 
 * 2. 혼합 Corner 스타일 (MixedCornersCard)
 *    - 실제 RoundedCornerShape 매개변수 활용
 *    - 개별 모서리 제어: topStart, topEnd, bottomStart, bottomEnd
 *    - 구현된 예시:
 *      * 정보 카드: topStart=16dp, topEnd=16dp, bottom=0dp
 *      * 대각선 스타일: topStart=20dp, bottomEnd=20dp, others=0dp
 *      * 복합 스타일: 상단 RoundedCornerShape + 하단 CutCornerShape
 * 
 * 3. 인터랙티브 Corner 에디터 (InteractiveCornerCard)
 *    - 4개 모서리 개별 실시간 제어 시스템
 *    - 각 모서리별 상태 관리:
 *      * 타입: Rounded/Sharp
 *      * 크기: 0-32dp (Slider 제어)
 *    - 동적 Shape 생성:
 *      RoundedCornerShape(
 *        topStart = if (type == "Sharp") 0.dp else size.dp,
 *        ...
 *      )
 * 
 * 4. 실제 활용 사례 (RealWorldExamplesCard)
 *    - 프로필 카드: topStart=20dp, topEnd=4dp, bottomStart=4dp, bottomEnd=20dp
 *    - 알림 패널: CutCornerShape(topEnd=16dp, bottomStart=16dp)
 *    - 액션 버튼: RoundedCornerShape(topStart=24dp, bottomEnd=24dp)
 *    - 정보 카드: RoundedCornerShape(topStart=16dp, topEnd=16dp)
 * 
 * 5. 핵심 구현 원리
 *    - 시뮬레이션이 아닌 실제 Compose Shape 사용
 *    - remember를 활용한 상태 관리
 *    - 실시간 Shape 업데이트 지원
 *    - Material Design 완벽 호환
 * 
 * 6. 컴포넌트 구조
 *    - CornerTypeExample: 기본 타입 시각화
 *    - ConcaveCornerExample: 시뮬레이션 구현
 *    - MixedCornerExampleCard: 실제 혼합 모서리
 *    - CornerControl: 개별 모서리 제어 UI
 *    - RealWorldExample: 실무 활용 예시
 */

object CardCornersGuide {
    const val GUIDE_INFO = """
        Card Corners in Jetpack Compose - CardCornersExampleUI 기반 가이드
        
        실제 구현된 4가지 주요 컴포넌트:
        - BasicCornerTypesCard: 4가지 기본 Corner 타입 시각화
        - MixedCornersCard: 실제 RoundedCornerShape 매개변수 활용
        - InteractiveCornerCard: 실시간 4개 모서리 개별 제어
        - RealWorldExamplesCard: 실무 활용 사례 및 구현 예시
        
        핵심 구현 특징:
        - 시뮬레이션이 아닌 실제 Compose Shape 사용
        - remember 상태 관리로 실시간 업데이트 지원
        - 각 모서리별 독립적 제어: topStart, topEnd, bottomStart, bottomEnd
        - Material Design과 완벽 호환되는 네이티브 구현
    """
    
    const val CORNER_TYPES = """
        === BasicCornerTypesCard 구현 분석 ===
        
        1. Convex (Rounded) - 둥근 모서리
           - 구현: RoundedCornerShape(16.dp)
           - 색상: Color(0xFF4CAF50)
           - 특징: 가장 일반적이고 친근한 모서리, Material Design 권장
           
        2. Sharp (90°) - 직각 모서리  
           - 구현: RectangleShape
           - 색상: Color(0xFF2196F3)
           - 특징: 90도 직각, 클래식하고 정돈된 느낌
           
        3. Cut (Diagonal) - 잘린 모서리
           - 구현: CutCornerShape(16.dp)
           - 색상: Color(0xFFFF9800)
           - 특징: 대각선으로 잘린 모서리, 모던하고 기하학적
           
        4. Concave (Inward) - 오목한 모서리 (시뮬레이션)
           - 구현: ConcaveCornerExample 컴포넌트
           - 색상: Color(0xFFE91E63)
           - 방법: 기본 Box + 모서리에 원형 오버레이로 시뮬레이션
           - 실제로는 Custom Path나 Canvas 구현 필요
    """
    
    const val IMPLEMENTATION_EXAMPLE = """
        === MixedCornersCard 실제 구현 예시 ===
        
        // 1. 정보 카드 스타일 (상단만 둥근 모서리)
        MixedCornerExampleCard(
            title = "정보 카드 스타일",
            description = "상단: 둥근 모서리, 하단: 직각 모서리",
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = 0.dp,
                bottomEnd = 0.dp
            ),
            color = Color(0xFF4CAF50)
        )
        
        // 2. 대각선 스타일 (좌상단, 우하단만 둥근)
        MixedCornerExampleCard(
            title = "대각선 스타일",
            description = "좌상단과 우하단만 둥근 모서리",
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 0.dp,
                bottomStart = 0.dp,
                bottomEnd = 20.dp
            ),
            color = Color(0xFFFF9800)
        )
        
        // 3. 복합 스타일 (상단 둥근 + 하단 잘린)
        MixedCornerExampleCard(
            title = "잘린 모서리 조합",
            description = "상단: 둥근, 하단: 잘린 모서리",
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = Color(0xFFE91E63),
            bottomShape = CutCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
        )
    """
    
    const val INTERACTIVE_CORNER_IMPLEMENTATION = """
        === InteractiveCornerCard 구현 분석 ===
        
        1. 상태 관리:
           - 각 모서리별 크기: var topStartSize by remember { mutableStateOf(16f) }
           - 각 모서리별 타입: var topStartType by remember { mutableStateOf("Rounded") }
           - 총 8개의 상태 변수로 4개 모서리 독립 제어
           
        2. 동적 Shape 생성:
           val dynamicShape = RoundedCornerShape(
               topStart = if (topStartType == "Sharp") 0.dp else topStartSize.dp,
               topEnd = if (topEndType == "Sharp") 0.dp else topEndSize.dp,
               bottomStart = if (bottomStartType == "Sharp") 0.dp else bottomStartSize.dp,
               bottomEnd = if (bottomEndType == "Sharp") 0.dp else bottomEndSize.dp
           )
           
        3. CornerControl 컴포넌트:
           - 각 모서리별 타입 선택 버튼 (Rounded/Sharp)
           - Slider를 통한 실시간 크기 조절 (0-32dp)
           - Surface로 감싸서 시각적 구분
           
        4. 실시간 프리뷰:
           - 설정 변경 시 즉시 카드 모양 반영
           - "LIVE DEMO" 텍스트로 실시간 특성 강조
    """
    
    const val REAL_WORLD_EXAMPLES = """
        === RealWorldExamplesCard 구현 분석 ===
        
        1. 프로필 카드 (사용자 정보를 담는 개성 있는 카드):
           - Shape: RoundedCornerShape(topStart=20dp, topEnd=4dp, bottomStart=4dp, bottomEnd=20dp)
           - 색상: Color(0xFF2196F3)
           - 아이콘: Icons.Filled.AccountCircle
           
        2. 알림 패널 (중요도에 따른 시각적 차별화):
           - Shape: CutCornerShape(topStart=0dp, topEnd=16dp, bottomStart=16dp, bottomEnd=0dp)
           - 색상: Color(0xFFFF9800)
           - 아이콘: Icons.Filled.Notifications
           
        3. 액션 버튼 (동적이고 모던한 버튼 디자인):
           - Shape: RoundedCornerShape(topStart=24dp, bottomEnd=24dp)
           - 색상: Color(0xFF4CAF50)
           - 아이콘: Icons.Filled.Settings
           
        4. 정보 카드 (콘텐츠 유형별 브랜딩):
           - Shape: RoundedCornerShape(topStart=16dp, topEnd=16dp)
           - 색상: Color(0xFF9C27B0)
           - 아이콘: Icons.Filled.Info
           
        각 사례는 RealWorldExample 컴포넌트로 구현되어 실제 사용 가능한 코드 제공
    """
}


/**
 * CardCornersExampleUI.kt 기반 실제 구현 가이드
 *
 * 이 가이드는 실제 동작하는 CardCornersExampleUI 예제를 기반으로 작성되었습니다.
 */
@Composable
private fun CardCornersImplementationGuide() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📋 CardCornersExampleUI 구현 가이드",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B1FA2)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "실제 구현된 4개 주요 컴포넌트와 핵심 기능들:",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ImplementationItem(
                    title = "BasicCornerTypesCard",
                    description = "4가지 기본 Corner 타입 (Rounded, Sharp, Cut, Concave)",
                    feature = "기본 타입",
                    color = Color(0xFF4CAF50)
                )

                ImplementationItem(
                    title = "MixedCornersCard",
                    description = "실제 RoundedCornerShape 매개변수로 혼합 스타일 구현",
                    feature = "혼합 스타일",
                    color = Color(0xFF2196F3)
                )

                ImplementationItem(
                    title = "InteractiveCornerCard",
                    description = "4개 모서리 개별 실시간 제어 시스템",
                    feature = "인터랙티브",
                    color = Color(0xFFFF9800)
                )

                ImplementationItem(
                    title = "RealWorldExamplesCard",
                    description = "프로필, 알림, 버튼 등 실무 활용 사례",
                    feature = "실무 예시",
                    color = Color(0xFFE91E63)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF7B1FA2).copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "🎯 구현 특징:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF7B1FA2)
                    )
                    Text(
                        text = "• 시뮬레이션이 아닌 실제 Compose Shape 사용\n• remember 상태 관리로 실시간 업데이트\n• 각 모서리별 독립적 제어 지원\n• Material Design 완벽 호환",
                        fontSize = 11.sp,
                        color = Color(0xFF7B1FA2).copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ImplementationItem(
    title: String,
    description: String,
    feature: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF7B1FA2)
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Text(
                text = feature,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 10.sp,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

