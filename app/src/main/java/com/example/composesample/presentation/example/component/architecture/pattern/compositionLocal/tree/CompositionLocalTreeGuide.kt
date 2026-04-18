package com.example.composesample.presentation.example.component.architecture.pattern.compositionLocal.tree

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

/**
 * Visualizing CompositionLocal in the Composition Tree Guide
 *
 * 출처: https://dev.to/bansalayush/visualizing-compositionlocal-in-the-composition-tree-2jkg
 *
 * Compose가 컴포저블을 실행하면 내부적으로 Composition Tree가 만들어집니다.
 * CompositionLocal은 이 트리의 노드에 데이터를 "부착"하는 방식으로 동작하며,
 * 데이터를 자식에게 복사하지 않고 부모 방향으로 룩업하여 찾습니다.
 *
 * === 1. Composition Tree 구조 ===
 *
 * 코드:                          트리:
 * @Composable
 * fun App() {                    [App]
 *     Screen()        ──►          │
 * }                             [Screen]
 *                                  │
 * @Composable                   [Card]
 * fun Screen() {                   │
 *     Card()                    [Text]
 * }
 *
 * === 2. CompositionLocal 데이터 부착 ===
 *
 * CompositionLocalProvider를 사용하면 해당 노드에 "locals map"이 부착됩니다.
 *
 * CompositionLocalProvider(LocalTheme provides Dark) {
 *     Screen()
 * }
 *
 * 결과:
 *   [App]           ← locals: { }
 *     │
 *   [Provider]      ← locals: { LocalTheme → Dark }  ✓ 여기에 부착
 *     │
 *   [Screen]        ← locals: { }  (부모에서 상속)
 *     │
 *   [Card]          ← locals: { }  (부모에서 상속)
 *
 * === 3. 룩업 메커니즘 (트리를 올라가며 탐색) ===
 *
 * LocalTheme.current 호출 시 Compose는 현재 노드에서 위로 올라가며 탐색합니다.
 *
 *   [App]        ← 4. 여기도 없으면? 기본값 사용
 *     │
 *   [Screen]     ← 3. 찾았다! LocalTheme → Dark ✓ 반환
 *     │
 *   [Card]       ← 2. 여기에 없네, 부모를 보자...
 *     │
 *   [Text]       ← 1. 시작: "LocalTheme가 필요해"
 *
 * === 4. 섀도잉 (Nested Providers) ===
 *
 * 중첩된 Provider에서 같은 키를 다시 제공하면 자식은 더 가까운 값을 사용합니다.
 *
 *   CompositionLocalProvider(LocalTheme provides Dark) {
 *       Screen()  // LocalTheme.current → Dark
 *       CompositionLocalProvider(LocalTheme provides Light) {
 *           AnotherScreen()  // LocalTheme.current → Light (섀도잉)
 *       }
 *   }
 *
 * === 5. Slot Table (내부 데이터 구조) ===
 *
 * Compose는 Slot Table이라는 평탄화된 배열로 트리를 관리합니다.
 *
 *   Index │ Node      │ Locals Map            │ Parent
 *   ──────┼───────────┼───────────────────────┼────────
 *     0   │ App       │ { }                   │ null
 *     1   │ Provider  │ { LocalTheme → Dark } │ 0
 *     2   │ Screen    │ { }                   │ 1
 *
 * === 6. 리컴포지션 스코프 ===
 *
 * compositionLocalOf 사용 시, 값을 실제로 읽는 노드만 리컴포지션됩니다.
 *
 *   Provider(LocalCounter → 1 → 2)
 *     │
 *   [Screen]   ← 리컴포지션 안 됨 (읽지 않음)
 *     │
 *   [Counter]  ← 리컴포지션! ✓ (LocalCounter.current 읽음)
 *     │
 *   [Label]    ← 리컴포지션 안 됨 (읽지 않음)
 *
 * === 핵심 인사이트 ===
 * - 데이터는 아래로 복사되지 않는다. 한 노드에 부착되고 룩업이 위로 올라간다.
 * - Provider는 트리의 특정 노드에 key-value 맵을 부착한다.
 * - 자식은 부모의 locals를 "상속"하는 것이 아니라 룩업 시 부모를 탐색한다.
 * - compositionLocalOf는 실제로 값을 읽는 컴포저블만 리컴포지션한다.
 */
object CompositionLocalTreeGuide {
    const val GUIDE_INFO = """
        Visualizing CompositionLocal in the Composition Tree
        
        Compose가 빌드하는 Composition Tree에서 
        CompositionLocal이 어떻게 동작하는지 시각화합니다.
        
        핵심 개념:
        - 각 노드는 선택적 "locals map"을 가질 수 있음
        - Provider가 노드에 key-value 쌍을 부착
        - .current 호출 시 트리를 위로 탐색 (lookup)
        - 중첩 Provider는 값을 섀도잉
        - 데이터는 아래로 복사되지 않음
        
        시연 내용:
        1. Tree Structure: Composition 트리 구조 시각화
        2. Lookup Walk: 트리 위로 올라가는 룩업 애니메이션
        3. Shadowing: 중첩 Provider의 값 섀도잉 시각화
        4. Recomposition: 값 변경 시 리컴포지션 범위 시각화
    """
}

@Preview
@Composable
fun CompositionLocalTreeGuidePreview() {
    // Preview for the guide content if needed
}
