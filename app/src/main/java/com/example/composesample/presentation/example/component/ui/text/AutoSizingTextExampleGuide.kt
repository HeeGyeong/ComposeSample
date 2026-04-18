package com.example.composesample.presentation.example.component.ui.text

/**
 * 📚 Auto-sizing Text with BasicText 실전 예제 학습 가이드
 * 
 * 이 문서는 AutoSizingTextExampleUI의 상세한 학습 가이드를 제공합니다.
 * 
 * =================================================================================================
 * 🎯 이 예제로 학습할 수 있는 내용
 * =================================================================================================
 * 
 * 1. BasicText의 autoSize 기능을 활용한 동적 텍스트 크기 조절
 * 2. TextAutoSize.StepBased()의 다양한 파라미터 활용법
 * 3. maxFontSize, minFontSize를 통한 크기 제한 설정
 * 4. softWrap, maxLines와 autoSize의 조합 활용
 * 5. TextOverflow.Ellipsis와 autoSize의 협력 관계
 * 6. onTextLayout 콜백을 통한 텍스트 레이아웃 정보 수집
 * 7. 실용적인 UI 컴포넌트에서의 autoSize 적용 사례
 * 
 * =================================================================================================
 * 🔍 핵심 개념: BasicText autoSize
 * =================================================================================================
 * 
 * 📝 autoSize란?
 * - Compose BOM 2025.04.01 이상에서 제공되는 BasicText의 새로운 기능
 * - 텍스트가 주어진 컨테이너 공간에 맞게 자동으로 폰트 크기를 조절
 * - 반응형 UI, 다국어 지원, 동적 콘텐츠 표시에 매우 유용
 * - Text 컴포넌트가 아닌 BasicText에서만 지원
 * 
 * 🏗️ autoSize 동작 원리:
 * 1. 설정된 fontSize를 시작점으로 사용
 * 2. 텍스트가 컨테이너보다 작으면 → 폰트 크기를 늘려서 공간을 최대한 활용
 * 3. 텍스트가 컨테이너보다 크면 → 폰트 크기를 줄여서 컨테이너에 맞춤
 * 4. minFontSize, maxFontSize 범위 내에서 조절
 * 
 * 🚀 주요 장점:
 * - 반응형 UI: 다양한 화면 크기에 자동 대응
 * - 다국어 지원: 언어별 텍스트 길이 차이 자동 처리
 * - 동적 콘텐츠: 런타임에 변경되는 텍스트 자동 최적화
 * - 일관된 디자인: 컨테이너를 최대한 활용하여 균형 잡힌 UI
 * 
 * =================================================================================================
 * 🎨 기능별 상세 가이드
 * =================================================================================================
 * 
 * 📋 기본 autoSize 사용법
 * ────────────────────────────────────────────────────────────────────────────────────────────
 * 🎯 핵심 구조:
 * ```kotlin
 * BasicText(
 *     text = "Auto Resizing Text",
 *     style = TextStyle(fontSize = 24.sp),
 *     autoSize = TextAutoSize.StepBased(),
 *     color = { Color.Black }
 * )
 * ```
 * 
 * 💡 주의사항:
 * - 반드시 명확한 크기 제약이 있는 컨테이너에서 사용해야 함
 * - width 또는 height가 무제한이면 autoSize가 작동하지 않음
 * - fontSize는 autoSize의 "시작점"으로 사용됨
 * 
 * 🔧 컨테이너 제약 예시:
 * ```kotlin
 * Box(
 *     modifier = Modifier
 *         .size(120.dp) // 명확한 크기 제약
 *         .padding(8.dp)
 * ) {
 *     BasicText(
 *         text = "Short Text",
 *         autoSize = TextAutoSize.StepBased()
 *     )
 * }
 * ```
 * 
 * 📋 최대 폰트 크기 제한 (maxFontSize)
 * ────────────────────────────────────────────────────────────────────────────────────────────
 * 🎯 사용 목적:
 * - 텍스트가 너무 커지는 것을 방지
 * - 디자인 일관성 유지
 * - 가독성 확보
 * 
 * 💡 구현 패턴:
 * ```kotlin
 * BasicText(
 *     text = "Auto Resizing Text but with max size limit",
 *     style = TextStyle(fontSize = 32.sp),
 *     autoSize = TextAutoSize.StepBased(
 *         maxFontSize = 16.sp // 최대 16sp로 제한
 *     ),
 *     color = { Color.Green }
 * )
 * ```
 * 
 * 🔧 적용 케이스:
 * - 카드 제목: 일정한 크기 이상 커지지 않도록
 * - 버튼 텍스트: 버튼 높이에 맞는 적절한 크기 유지
 * - 짧은 텍스트: 과도하게 큰 폰트 방지
 * 
 * 📋 최소 폰트 크기 제한과 Ellipsis
 * ────────────────────────────────────────────────────────────────────────────────────────────
 * 🎯 핵심 개념:
 * - minFontSize 이하로 줄어들 수 없을 때 TextOverflow.Ellipsis 활용
 * - 가독성을 위한 최소 폰트 크기 보장
 * - 긴 텍스트의 우아한 처리
 * 
 * 💡 구현 전략:
 * ```kotlin
 * BasicText(
 *     text = "Very long text that cannot fit in small space...",
 *     style = TextStyle(fontSize = 32.sp),
 *     autoSize = TextAutoSize.StepBased(
 *         minFontSize = 10.sp // 10sp 이하로는 줄어들지 않음
 *     ),
 *     overflow = TextOverflow.Ellipsis, // 최소 크기에서도 안 맞으면 ... 표시
 *     color = { Color.Red }
 * )
 * ```
 * 
 * 🔧 동작 순서:
 * 1. autoSize가 텍스트 크기를 점진적으로 줄임
 * 2. minFontSize(10.sp)에 도달
 * 3. 여전히 컨테이너를 벗어나면 TextOverflow.Ellipsis 적용
 * 4. "Very long text that cann..." 형태로 표시
 * 
 * 📋 SoftWrap과 autoSize 조합
 * ────────────────────────────────────────────────────────────────────────────────────────────
 * 🎯 SoftWrap 역할:
 * - true: 텍스트가 길면 여러 줄로 자동 줄바꿈
 * - false: 텍스트를 한 줄로 유지, 필요시 잘림
 * 
 * 💡 autoSize와의 상호작용:
 * ```kotlin
 * var softWrapEnabled by remember { mutableStateOf(true) }
 * 
 * BasicText(
 *     text = "Long text with soft wrap control - toggle to see difference",
 *     style = TextStyle(fontSize = 24.sp),
 *     autoSize = TextAutoSize.StepBased(maxFontSize = 16.sp),
 *     softWrap = softWrapEnabled
 * )
 * ```
 * 
 * 🔧 동작 차이:
 * - SoftWrap = true: 여러 줄로 나뉘어지면서 폰트 크기 조절
 * - SoftWrap = false: 한 줄 유지하면서 폰트 크기를 더 작게 조절
 * 
 * 📋 MaxLines 제한과 autoSize
 * ────────────────────────────────────────────────────────────────────────────────────────────
 * 🎯 maxLines의 효과:
 * - 텍스트가 표시될 수 있는 최대 줄 수 제한
 * - autoSize가 제한된 줄 수 내에서 최적화 수행
 * 
 * 💡 일반적인 사용 패턴:
 * ```kotlin
 * BasicText(
 *     text = "Very long text that should be limited to single line",
 *     style = TextStyle(fontSize = 24.sp),
 *     autoSize = TextAutoSize.StepBased(maxFontSize = 16.sp),
 *     maxLines = 1, // 한 줄로 제한
 *     overflow = TextOverflow.Ellipsis
 * )
 * ```
 * 
 * 🔧 적용 시나리오:
 * - 제목이나 헤더: maxLines = 1
 * - 요약 텍스트: maxLines = 2 또는 3
 * - 카드 설명: maxLines = 2~4
 * 
 * 📋 onTextLayout 콜백 활용
 * ────────────────────────────────────────────────────────────────────────────────────────────
 * 🎯 onTextLayout의 역할:
 * - 텍스트 레이아웃 완료 후 호출되는 콜백
 * - 실제 텍스트 크기, 줄 수, 폰트 크기 등 정보 제공
 * - 디버깅 및 동적 UI 조정에 활용
 * 
 * 💡 구현 예시:
 * ```kotlin
 * var measuredWidth by remember { mutableStateOf(0) }
 * var measuredHeight by remember { mutableStateOf(0) }
 * 
 * BasicText(
 *     text = "Auto Resizing Text with layout callback",
 *     style = TextStyle(fontSize = 24.sp),
 *     autoSize = TextAutoSize.StepBased(maxFontSize = 18.sp),
 *     onTextLayout = { layoutResult ->
 *         measuredWidth = layoutResult.size.width
 *         measuredHeight = layoutResult.size.height
 *         // layoutResult.lineCount, layoutResult.didOverflowWidth 등 추가 정보 활용 가능
 *     }
 * )
 * 
 * Text("측정된 크기: ${measuredWidth}px × ${measuredHeight}px")
 * ```
 * 
 * 🔧 활용 방안:
 * - 성능 모니터링: 텍스트 렌더링 시간 측정
 * - 동적 레이아웃: 텍스트 크기에 따른 UI 조정
 * - 접근성: 실제 폰트 크기 정보 제공
 * - 디버깅: autoSize 동작 확인
 * 
 * =================================================================================================
 * 🛠️ 실용적인 활용 사례
 * =================================================================================================
 * 
 * 📋 카드 제목 최적화
 * ────────────────────────────────────────────────────────────────────────────────────────────
 * 💡 문제 상황:
 * - 카드 제목 길이가 다양함 (짧은 제목 vs 긴 제목)
 * - 고정 폰트 크기로는 공간 활용이 비효율적
 * - 다국어 지원 시 제목 길이 예측 불가
 * 
 * 🔧 autoSize 해결책:
 * ```kotlin
 * Card(
 *     modifier = Modifier
 *         .fillMaxWidth()
 *         .height(60.dp)
 * ) {
 *     Box(
 *         modifier = Modifier
 *             .fillMaxSize()
 *             .padding(8.dp),
 *         contentAlignment = Alignment.Center
 *     ) {
 *         BasicText(
 *             text = "카드 제목이 자동으로 크기 조절됩니다 - 매우 긴 제목도 자동으로 맞춰집니다",
 *             style = TextStyle(fontSize = 20.sp),
 *             autoSize = TextAutoSize.StepBased(maxFontSize = 16.sp),
 *             color = { Color.White }
 *         )
 *     }
 * }
 * ```
 * 
 * 📋 버튼 텍스트 최적화
 * ────────────────────────────────────────────────────────────────────────────────────────────
 * 💡 문제 상황:
 * - 버튼 텍스트가 넘치거나 너무 작게 표시
 * - 다국어에서 버튼 텍스트 길이 차이
 * - 동적으로 변경되는 버튼 라벨
 * 
 * 🔧 autoSize 해결책:
 * ```kotlin
 * Button(
 *     onClick = { },
 *     modifier = Modifier
 *         .fillMaxWidth()
 *         .height(48.dp)
 * ) {
 *     BasicText(
 *         text = "버튼 텍스트도 자동 크기 조절 - 매우 긴 버튼 텍스트입니다",
 *         style = TextStyle(fontSize = 18.sp),
 *         autoSize = TextAutoSize.StepBased(maxFontSize = 14.sp),
 *         color = { Color.Black }
 *     )
 * }
 * ```
 * 
 * =================================================================================================
 * ⚠️  주의사항 및 제한사항
 * =================================================================================================
 * 
 * 🚨 필수 요구사항:
 * 1. **Compose BOM 버전**: 2025.04.01 이상 필요
 * 2. **컨테이너 제약**: 반드시 명확한 크기 제한이 있어야 함
 * 3. **BasicText 전용**: Text 컴포넌트에서는 지원하지 않음
 * 
 * 🔧 성능 고려사항:
 * - autoSize는 텍스트 레이아웃 계산을 여러 번 수행할 수 있음
 * - 복잡한 텍스트나 많은 autoSize 컴포넌트 사용 시 성능 영향 가능
 * - 캐싱 및 최적화 고려 필요
 * 
 * 💡 디자인 가이드라인:
 * - maxFontSize, minFontSize 적절히 설정하여 가독성 확보
 * - 브랜드 가이드라인에 맞는 폰트 크기 범위 설정
 * - 접근성을 위한 최소 폰트 크기 준수 (일반적으로 12sp 이상)
 * 
 * 🔍 디버깅 팁:
 * - onTextLayout 콜백으로 실제 적용된 폰트 크기 확인
 * - 컨테이너 경계를 시각적으로 표시하여 제약 확인
 * - 다양한 텍스트 길이로 테스트 수행
 * 
 * =================================================================================================
 * 🎓 학습 순서 권장사항
 * =================================================================================================
 * 
 * 1. **기본 개념 이해**: autoSize가 없는 일반 Text와 비교
 * 2. **단순한 예제**: 고정 크기 Box에서 기본 autoSize 테스트
 * 3. **제한 설정**: maxFontSize, minFontSize 파라미터 실험
 * 4. **복합 기능**: softWrap, maxLines, overflow와 조합 테스트
 * 5. **실제 적용**: 카드, 버튼 등 실제 UI 컴포넌트에 적용
 * 6. **성능 최적화**: onTextLayout 콜백 활용 및 성능 측정
 * 
 * 💡 추가 학습 자료:
 * - Compose 공식 문서의 BasicText autoSize 섹션
 * - Material Design의 텍스트 크기 가이드라인
 * - 접근성 관련 폰트 크기 권장사항
 */
object AutoSizingTextExampleGuide 