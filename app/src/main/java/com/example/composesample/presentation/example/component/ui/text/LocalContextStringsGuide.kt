package com.example.composesample.presentation.example.component.ui.text

/**
 * 📚 Why You Shouldn't Use LocalContext for Strings 학습 가이드
 *
 * 출처: https://proandroiddev.com/jetpack-compose-why-you-shouldnt-use-localcontext-for-strings-4d4c372b14ab
 *
 * Compose에서 문자열 리소스를 가져올 때 LocalContext를 사용하는 것이
 * 왜 문제인지, 그리고 올바른 대안이 무엇인지 설명합니다.
 *
 * =================================================================================================
 * 🎯 핵심 문제: LocalContext.current.getString()
 * =================================================================================================
 *
 * 많은 개발자들이 Compose에서 다음 패턴을 사용합니다:
 *
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val context = LocalContext.current
 *     val title = context.getString(R.string.screen_title)
 *     Text(text = title)
 * }
 * ```
 *
 * ❌ 이 패턴의 문제점:
 *  1. @Preview 에서 동작하지 않음 (Context가 실제 Android 환경이 아님)
 *  2. 런타임 locale 변경에 반응하지 않음 (리컴포지션 없음)
 *  3. 테스트가 어려워짐 (Context mocking 필요)
 *  4. Compose의 선언형 패러다임에 맞지 않음
 *
 * =================================================================================================
 * ✅ 올바른 방법 1: stringResource()
 * =================================================================================================
 *
 * Compose에서 제공하는 전용 함수를 사용합니다:
 *
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val title = stringResource(R.string.screen_title)
 *     Text(text = title)
 * }
 * ```
 *
 * stringResource()의 장점:
 *  - @Preview에서 올바르게 동작
 *  - locale 변경 시 자동으로 리컴포지션 트리거
 *  - Compose 테스트 환경과 완벽히 호환
 *  - 내부적으로 CompositionLocal 기반으로 구현됨
 *
 * ```kotlin
 * // 포맷 인자 지원
 * stringResource(R.string.welcome_message, username)
 *
 * // Plurals 지원
 * pluralStringResource(R.plurals.item_count, count, count)
 * ```
 *
 * =================================================================================================
 * 🔍 왜 stringResource()는 Preview에서 동작하나?
 * =================================================================================================
 *
 * `stringResource()`는 내부적으로 `LocalContext`가 아닌
 * `LocalConfiguration` + `LocalContext` 조합을 사용하고,
 * Compose 프레임워크가 제공하는 CompositionLocal로 추상화되어 있습니다.
 *
 * Preview 환경에서는 이 CompositionLocal이 적절히 초기화되지만,
 * 순수 `LocalContext.current`는 렌더링되지 않는 상황에서 Context가 없습니다.
 *
 * =================================================================================================
 * ✅ 올바른 방법 2: UiText sealed class (ViewModel 패턴)
 * =================================================================================================
 *
 * ViewModel에서 문자열이 필요한 경우가 문제입니다.
 * ViewModel이 Context를 직접 들고 있으면 테스트하기 어렵고,
 * Context 누수 위험이 있습니다.
 *
 * ```kotlin
 * // ❌ 잘못된 ViewModel 패턴
 * class MyViewModel(private val context: Context) : ViewModel() {
 *     val errorMessage = context.getString(R.string.error_network)
 * }
 * ```
 *
 * **해결책: UiText sealed class**
 *
 * ```kotlin
 * sealed class UiText {
 *     // 이미 해결된 동적 문자열 (API 응답 등)
 *     data class DynamicString(val value: String) : UiText()
 *
 *     // 아직 해결되지 않은 리소스 ID 기반 문자열
 *     class StringResource(
 *         @StringRes val id: Int,
 *         vararg val args: Any
 *     ) : UiText()
 * }
 * ```
 *
 * **ViewModel에서 사용:**
 * ```kotlin
 * class MyViewModel : ViewModel() {
 *     private val _errorMessage = MutableStateFlow<UiText?>(null)
 *     val errorMessage = _errorMessage.asStateFlow()
 *
 *     fun onNetworkError() {
 *         _errorMessage.value = UiText.StringResource(R.string.error_network)
 *     }
 *
 *     fun onApiError(message: String) {
 *         _errorMessage.value = UiText.DynamicString(message)
 *     }
 * }
 * ```
 *
 * **UI 레이어에서 해결:**
 * ```kotlin
 * @Composable
 * fun UiText.asString(): String {
 *     return when (this) {
 *         is UiText.DynamicString -> value
 *         is UiText.StringResource -> stringResource(id, *args)
 *     }
 * }
 *
 * @Composable
 * fun MyScreen(viewModel: MyViewModel = viewModel()) {
 *     val error by viewModel.errorMessage.collectAsState()
 *     error?.let {
 *         Text(text = it.asString())  // ✅ UI에서만 stringResource 호출
 *     }
 * }
 * ```
 *
 * =================================================================================================
 * ✅ 올바른 방법 3: StringResolver / StringProvider 인터페이스
 * =================================================================================================
 *
 * 테스트 가능성을 극대화하는 패턴:
 *
 * ```kotlin
 * interface StringResolver {
 *     fun resolve(@StringRes resId: Int, vararg args: Any): String
 * }
 *
 * class AndroidStringResolver(private val context: Context) : StringResolver {
 *     override fun resolve(@StringRes resId: Int, vararg args: Any): String =
 *         context.getString(resId, *args)
 * }
 *
 * class FakeStringResolver : StringResolver {
 *     override fun resolve(@StringRes resId: Int, vararg args: Any): String =
 *         "test_string_$resId"
 * }
 *
 * // ViewModel
 * class MyViewModel(private val stringResolver: StringResolver) : ViewModel() {
 *     val message = stringResolver.resolve(R.string.welcome)
 * }
 * ```
 *
 * =================================================================================================
 * ⚠️ LocalContext를 써야 할 진짜 경우
 * =================================================================================================
 *
 * LocalContext는 문자열이 아닌 실제 Context가 필요한 경우에 사용합니다:
 *
 * ```kotlin
 * @Composable
 * fun ShareButton(text: String) {
 *     val context = LocalContext.current  // ✅ 적절한 사용
 *     Button(onClick = {
 *         val intent = Intent(Intent.ACTION_SEND).apply {
 *             type = "text/plain"
 *             putExtra(Intent.EXTRA_TEXT, text)
 *         }
 *         context.startActivity(Intent.createChooser(intent, null))
 *     }) {
 *         Text("공유")
 *     }
 * }
 * ```
 *
 * LocalContext가 필요한 경우:
 *  - startActivity()
 *  - 시스템 서비스 (ClipboardManager, ConnectivityManager 등)
 *  - Toast.makeText()
 *  - 외부 라이브러리가 Context를 요구하는 경우
 *
 * =================================================================================================
 * 🚀 패턴 비교 요약
 * =================================================================================================
 *
 * | 패턴                    | Preview | Locale 변경 | 테스트 | ViewModel 가능 |
 * |------------------------|---------|-----------|-------|--------------|
 * | LocalContext.getString  | ❌      | ❌         | 어려움  | ❌ (누수 위험) |
 * | stringResource()        | ✅      | ✅         | 쉬움   | ❌ (Composable만) |
 * | UiText sealed class     | ✅      | ✅         | 쉬움   | ✅           |
 * | StringResolver 인터페이스  | ✅      | ✅         | 최고   | ✅           |
 */
object LocalContextStringsGuide
