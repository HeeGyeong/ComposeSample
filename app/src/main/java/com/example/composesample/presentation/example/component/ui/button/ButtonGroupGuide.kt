package com.example.composesample.presentation.example.component.ui.button

/**
 * Jetpack Compose: ButtonGroup (Material 3 Expressive) Guide
 *
 * 출처: https://joebirch.co/android/exploring-jetpack-compose-buttongroup/
 *
 * === ButtonGroupExampleUI에서 다루는 내용 ===
 *
 * 이 예제는 Material 3 Expressive의 ButtonGroup 컴포넌트를 시뮬레이션으로 시각적으로 보여줍니다:
 *
 * 주요 데모:
 * 1. BasicButtonGroupCard()
 *    - 클릭 가능한 버튼 그룹 (clickableItem 시뮬레이션)
 *    - 확장 애니메이션 (expandedRatio) 효과
 *    - 오버플로우 인디케이터 표시
 *    - SimulatedClickableButton으로 구현
 *
 * 2. SingleSelectCard()
 *    - 단일 선택 토글 버튼 (toggleableItem 시뮬레이션)
 *    - 하나만 선택 가능한 토글 그룹
 *    - 아이콘 변경 애니메이션 (outlined ↔ filled)
 *    - SimulatedToggleButton으로 구현
 *
 * 3. MultiSelectCard()
 *    - 다중 선택 토글 버튼
 *    - 여러 개 동시 선택 가능
 *    - Check 아이콘으로 선택 상태 표시
 *    - SimulatedMultiToggleButton으로 구현
 *
 * 4. IconOnlyButtonGroupCard()
 *    - 아이콘 전용 버튼 그룹
 *    - 텍스트 없이 아이콘만 표시
 *    - 원형 버튼 스타일
 *    - IconOnlyButton으로 구현
 *
 * 5. ExpandedRatioCard()
 *    - expandedRatio 파라미터 비교 데모
 *    - 0f (비활성), 0.5f (50%), 1f (100%) 비교
 *    - 버튼 클릭 시 확장 비율 시각화
 *    - ExpandableButton으로 구현
 *
 * 6. ConnectedButtonGroupCard()
 *    - 연결된 세그먼트 컨트롤 스타일
 *    - SegmentedButton 대체 컴포넌트
 *    - 위치별 Shape 차별화 (Leading/Middle/Trailing)
 *    - FlowRow로 여러 줄 배치 예시
 *    - ConnectedSegmentButton으로 구현
 *
 * === ButtonGroup이란? ===
 *
 * Material 3 Expressive 업데이트의 일부로 소개된 새로운 컴포넌트입니다.
 * 버튼들을 수평으로 배치하며, 애니메이션과 오버플로우 처리를 기본 지원합니다.
 *
 * 주요 특징:
 * - 자동 너비 애니메이션 (누를 때 확장)
 * - 오버플로우 메뉴 지원
 * - Single/Multi 선택 토글 지원
 * - 연결된 버튼 스타일 지원
 *
 * Note: 현재 Experimental API입니다.
 * @OptIn(ExperimentalMaterial3ExpressiveApi::class)
 *
 * === ButtonGroup 파라미터 ===
 *
 * @Composable
 * fun ButtonGroup(
 *     overflowIndicator: @Composable (ButtonGroupMenuState) -> Unit,
 *     modifier: Modifier = Modifier,
 *     expandedRatio: Float = ButtonGroupDefaults.ExpandedRatio,
 *     horizontalArrangement: Arrangement.Horizontal = ButtonGroupDefaults.HorizontalArrangement,
 *     verticalAlignment: Alignment.Vertical = Alignment.Top,
 *     content: ButtonGroupScope.() -> Unit,
 * )
 *
 * 파라미터 설명:
 * - overflowIndicator: 오버플로우 시 표시할 컴포저블
 * - expandedRatio: 눌렀을 때 확장 비율 (0f = 비활성화)
 * - horizontalArrangement: 수평 정렬
 * - verticalAlignment: 수직 정렬
 * - content: ButtonGroupScope 내의 버튼들
 *
 * === ButtonGroupScope 함수 ===
 *
 * 1. clickableItem()
 *    - 일반 클릭 가능한 버튼
 *    - onClick, label 제공
 *
 * 2. toggleableItem()
 *    - 토글 가능한 버튼
 *    - checked, onCheckedChange 제공
 *    - Single/Multi 선택 구현에 사용
 *
 * 3. customItem()
 *    - 커스텀 버튼 구현
 *    - 완전한 자유도
 *
 * === 사용 예제 ===
 *
 * 1. 기본 ButtonGroup
 *    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
 *    @Composable
 *    fun BasicButtonGroup() {
 *        ButtonGroup(
 *            overflowIndicator = { menuState ->
 *                FilledIconButton(
 *                    onClick = {
 *                        if (menuState.isExpanded) menuState.dismiss()
 *                        else menuState.show()
 *                    }
 *                ) {
 *                    Icon(Icons.Filled.MoreVert, "More")
 *                }
 *            }
 *        ) {
 *            for (i in 0 until 5) {
 *                clickableItem(onClick = {}, label = "Button $i")
 *            }
 *        }
 *    }
 *
 * 2. Single-Select 토글
 *    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
 *    @Composable
 *    fun SingleSelectButtonGroup() {
 *        val options = listOf("Work", "Food", "Coffee")
 *        var selectedIndex by remember { mutableIntStateOf(0) }
 *
 *        ButtonGroup(
 *            overflowIndicator = {}
 *        ) {
 *            options.forEachIndexed { index, label ->
 *                toggleableItem(
 *                    checked = selectedIndex == index,
 *                    onCheckedChange = { selectedIndex = index },
 *                    label = label
 *                )
 *            }
 *        }
 *    }
 *
 * 3. Multi-Select 토글
 *    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
 *    @Composable
 *    fun MultiSelectButtonGroup() {
 *        val options = listOf("Work", "Food", "Coffee")
 *        val checked = remember { mutableStateListOf(false, false, false) }
 *
 *        ButtonGroup(
 *            overflowIndicator = {}
 *        ) {
 *            options.forEachIndexed { index, label ->
 *                toggleableItem(
 *                    checked = checked[index],
 *                    onCheckedChange = { checked[index] = it },
 *                    label = label
 *                )
 *            }
 *        }
 *    }
 *
 * 4. Connected ButtonGroup (세그먼트 컨트롤)
 *    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
 *    @Composable
 *    fun ConnectedButtonGroup() {
 *        val options = listOf("Work", "Food", "Coffee")
 *        var selectedIndex by remember { mutableIntStateOf(0) }
 *
 *        Row(
 *            horizontalArrangement = Arrangement.spacedBy(
 *                ButtonGroupDefaults.ConnectedSpaceBetween
 *            )
 *        ) {
 *            options.forEachIndexed { index, label ->
 *                ToggleButton(
 *                    checked = selectedIndex == index,
 *                    onCheckedChange = { selectedIndex = index },
 *                    shapes = when (index) {
 *                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
 *                        options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
 *                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
 *                    }
 *                ) {
 *                    Text(label)
 *                }
 *            }
 *        }
 *    }
 *
 * === 오버플로우 처리 ===
 *
 * ButtonGroupMenuState:
 * - isExpanded: 메뉴 확장 여부
 * - show(): 메뉴 표시
 * - dismiss(): 메뉴 닫기
 *
 * overflowIndicator = { menuState ->
 *     FilledIconButton(
 *         onClick = {
 *             if (menuState.isExpanded) menuState.dismiss()
 *             else menuState.show()
 *         }
 *     ) {
 *         Icon(Icons.Filled.MoreVert, "More options")
 *     }
 * }
 *
 * === expandedRatio 커스터마이징 ===
 *
 * 버튼을 눌렀을 때 확장 비율 조절:
 * - 0f: 확장 애니메이션 비활성화
 * - 0.5f: 50% 확장
 * - 1f: 2배로 확장 (원래 너비의 200%)
 * - 기본값: ButtonGroupDefaults.ExpandedRatio
 *
 * ButtonGroup(
 *     expandedRatio = 0.5f,  // 50% 확장
 *     overflowIndicator = { /* ... */ }
 * ) {
 *     // content
 * }
 *
 * === Connected Button Shapes ===
 *
 * ButtonGroupDefaults에서 제공하는 Shape:
 * - connectedLeadingButtonShapes(): 첫 번째 버튼 (왼쪽 둥근 모서리)
 * - connectedMiddleButtonShapes(): 중간 버튼 (직사각형)
 * - connectedTrailingButtonShapes(): 마지막 버튼 (오른쪽 둥근 모서리)
 *
 * 선택된 버튼은 항상 완전히 둥근 Shape 사용
 *
 * === FlowRow와 함께 사용 ===
 *
 * 버튼이 많아 한 줄에 안 들어갈 때:
 *
 * FlowRow(
 *     horizontalArrangement = Arrangement.spacedBy(
 *         ButtonGroupDefaults.ConnectedSpaceBetween
 *     ),
 *     verticalArrangement = Arrangement.spacedBy(2.dp)
 * ) {
 *     options.forEachIndexed { index, label ->
 *         ToggleButton(
 *             // ...
 *         )
 *     }
 * }
 *
 * === 주의사항 ===
 *
 * 1. Experimental API
 *    - @OptIn(ExperimentalMaterial3ExpressiveApi::class) 필요
 *    - API가 변경될 수 있음
 *
 * 2. Material 3 Expressive
 *    - Material 3의 확장 버전
 *    - 더 풍부한 애니메이션과 인터랙션
 *    - 최신 라이브러리 버전 필요
 *
 * 3. SegmentedButton 대체
 *    - 기존 SegmentedButton은 deprecated
 *    - Connected ButtonGroup으로 대체
 *
 * === 요약 ===
 *
 * ButtonGroup 특징:
 * - 수평 버튼 그룹 레이아웃
 * - 자동 확장 애니메이션
 * - 오버플로우 메뉴 지원
 * - Single/Multi 선택 토글
 * - Connected 스타일 지원
 *
 * 사용 시기:
 * - 관련된 버튼들을 그룹화할 때
 * - 세그먼트 컨트롤이 필요할 때
 * - 토글 선택이 필요할 때
 * - Material 3 Expressive 디자인 적용 시
 */

object ButtonGroupGuide {
    const val GUIDE_INFO = """
        ButtonGroup (Material 3 Expressive)
        
        핵심 기능:
        - 수평 버튼 그룹 레이아웃
        - 자동 확장 애니메이션 (expandedRatio)
        - 오버플로우 메뉴 지원
        - Single/Multi 선택 토글
        - Connected 스타일 지원 (SegmentedButton 대체)
        
        ButtonGroupScope:
        - clickableItem(): 클릭 버튼
        - toggleableItem(): 토글 버튼 (Single/Multi select)
        - customItem(): 커스텀 버튼
        
        Connected Shapes:
        - connectedLeadingButtonShapes(): 첫 번째 버튼
        - connectedMiddleButtonShapes(): 중간 버튼
        - connectedTrailingButtonShapes(): 마지막 버튼
        
        구현 컴포넌트:
        - SimulatedClickableButton: 클릭 버튼 시뮬레이션
        - SimulatedToggleButton: 단일 선택 토글 시뮬레이션
        - SimulatedMultiToggleButton: 다중 선택 토글 시뮬레이션
        - IconOnlyButton: 아이콘 전용 버튼
        - ExpandableButton: 확장 비율 조절 버튼
        - RatioSelectorButton: 비율 선택 버튼
        - ConnectedSegmentButton: 연결된 세그먼트 버튼
        - SegmentPosition enum: LEADING, MIDDLE, TRAILING
        
        Note: @OptIn(ExperimentalMaterial3ExpressiveApi::class) 필요
        
        출처: https://joebirch.co/android/exploring-jetpack-compose-buttongroup/
    """
}
