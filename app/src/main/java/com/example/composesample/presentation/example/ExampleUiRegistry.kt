package com.example.composesample.presentation.example

import android.os.Build
import androidx.compose.runtime.Composable
import com.example.composesample.presentation.example.component.architecture.development.compose17.FocusRestorerExampleUI
import com.example.composesample.presentation.example.component.architecture.development.compose17.GraphicsLayerExampleUI
import com.example.composesample.presentation.example.component.architecture.development.compose17.LookaheadScopeExampleUI
import com.example.composesample.presentation.example.component.architecture.development.compose17.PathGraphicsExampleUI
import com.example.composesample.presentation.example.component.architecture.development.compose17.TextOverflowExampleUI
import com.example.composesample.presentation.example.component.architecture.development.coordinator.CoordinatorExampleUI
import com.example.composesample.presentation.example.component.architecture.development.cursor.CursorIDEExampleUI
import com.example.composesample.presentation.example.component.architecture.development.di.KoinCompilerPluginExampleUI
import com.example.composesample.presentation.example.component.architecture.development.featureflag.FeatureFlagExampleUI
import com.example.composesample.presentation.example.component.architecture.development.init.InitTestExampleUI
import com.example.composesample.presentation.example.component.architecture.development.init.StartupOptimizationExampleUI
import com.example.composesample.presentation.example.component.architecture.development.preview.PreviewInternalsExampleUI
import com.example.composesample.presentation.example.component.architecture.development.preview.PreviewOnlyAnnotationExampleUI
import com.example.composesample.presentation.example.component.architecture.development.rebound.ReboundExampleUI
import com.example.composesample.presentation.example.component.architecture.development.test.TurbineFlowTestExampleUI
import com.example.composesample.presentation.example.component.architecture.development.test.RecompositionTestExampleUI
import com.example.composesample.presentation.example.component.architecture.development.test.ScreenshotTestingExampleUI
import com.example.composesample.presentation.example.component.architecture.development.test.PreviewDrivenScreenshotExampleUI
import com.example.composesample.presentation.example.component.architecture.development.test.UITestExampleUI
import com.example.composesample.presentation.example.component.system.platform.quicksettings.QuickSettingsTileExampleUI
import com.example.composesample.presentation.example.component.architecture.development.type.TypeExampleUI
import com.example.composesample.presentation.example.component.architecture.pattern.compositionLocal.CompositionLocalExampleUI
import com.example.composesample.presentation.example.component.architecture.pattern.coroutine.CoroutineExampleUI
import com.example.composesample.presentation.example.component.architecture.pattern.effect.SideEffectExampleUI
import com.example.composesample.presentation.example.component.architecture.pattern.mvi.MVIExampleUI
import com.example.composesample.presentation.example.component.data.api.ApiDisconnectExampleUI
import com.example.composesample.presentation.example.component.data.api.KtorAdvancedConfigExampleUI
import com.example.composesample.presentation.example.component.data.api.KtorExampleUI
import com.example.composesample.presentation.example.component.data.cache.DataCacheExampleUI
import com.example.composesample.presentation.example.component.data.paging.PagingExampleUI
import com.example.composesample.presentation.example.component.data.repository.AdvancedRepositoryPatternExampleUI
import com.example.composesample.presentation.example.component.data.room.MultiTableInsertExampleUI
import com.example.composesample.presentation.example.component.data.room.RoomFtsSearchExampleUI
import com.example.composesample.presentation.example.component.data.room.RoomIndexExampleUI
import com.example.composesample.presentation.example.component.data.sse.SSEExampleUI
import com.example.composesample.presentation.example.component.interaction.clickevent.ClickEventUI
import com.example.composesample.presentation.example.component.interaction.drag.DragAndDropExampleUI
import com.example.composesample.presentation.example.component.interaction.refresh.PullToRefreshUI
import com.example.composesample.presentation.example.component.interaction.sticker.StickerCanvasExampleUI
import com.example.composesample.presentation.example.component.interaction.swipe.SwipeToDismissM3ExampleUI
import com.example.composesample.presentation.example.component.interaction.swipe.SwipeToDismissUI
import com.example.composesample.presentation.example.component.system.ai.GeminiNanoExampleUI
import com.example.composesample.presentation.example.component.system.deeplink.DynamicAppLinksExampleUI
import com.example.composesample.presentation.example.component.system.platform.biometric.BiometricAuthExampleUI
import com.example.composesample.presentation.example.component.system.security.AppSecurityExampleUI
import com.example.composesample.presentation.example.component.system.security.HardwareKeystoreExampleUI
import com.example.composesample.presentation.example.component.system.security.ScreenshotDetectionExampleUI
import com.example.composesample.presentation.example.component.system.platform.haptic.HapticFeedbackExampleUI
import com.example.composesample.presentation.example.component.system.platform.predictiveback.PredictiveBackExampleUI
import com.example.composesample.presentation.example.component.system.background.workmanager.WorkManagerUI
import com.example.composesample.presentation.example.component.system.media.recorder.AudioRecorderUI
import com.example.composesample.presentation.example.component.system.media.video.Media3VideoPlayerExampleUI
import com.example.composesample.presentation.example.component.system.platform.file.SafFileSelectionUI
import com.example.composesample.presentation.example.component.system.platform.intent.PassingIntentDataExampleUI
import com.example.composesample.presentation.example.component.system.platform.language.LanguageSettingExampleUI
import com.example.composesample.presentation.example.component.system.platform.language.LocalLanguageChangeExampleUI
import com.example.composesample.presentation.example.component.system.platform.powersave.PowerSaveModeExampleUI
import com.example.composesample.presentation.example.component.system.platform.shortcut.ShortcutExampleUI
import com.example.composesample.presentation.example.component.system.platform.version.TargetSDK34ExampleUI
import com.example.composesample.presentation.example.component.system.platform.webview.WebViewIssueUI
import com.example.composesample.presentation.example.component.system.ui.widget.GlanceWidgetExampleUI
import com.example.composesample.presentation.example.component.ui.autofill.AutofillExampleUI
import com.example.composesample.presentation.example.component.ui.accessibility.AccessibleFocusIndicatorExampleUI
import com.example.composesample.presentation.example.component.ui.accessibility.LargeContentViewerExampleUI
import com.example.composesample.presentation.example.component.ui.button.ButtonGroupExampleUI
import com.example.composesample.presentation.example.component.ui.canvas.CanvasShapesExampleUI
import com.example.composesample.presentation.example.component.ui.canvas.ComposeLoadersExampleUI
import com.example.composesample.presentation.example.component.ui.canvas.DialComponentExampleUI
import com.example.composesample.presentation.example.component.ui.canvas.MonthPickerDialExampleUI
import com.example.composesample.presentation.example.component.ui.canvas.ParticleEmitterExampleUI
import com.example.composesample.presentation.example.component.ui.canvas.FreehandDrawingExampleUI
import com.example.composesample.presentation.example.component.ui.canvas.MotionBlurExampleUI
import com.example.composesample.presentation.example.component.ui.graphics.NewShadowApiExampleUI
import com.example.composesample.presentation.example.component.ui.layout.animation.AnimatedContentExampleUI
import com.example.composesample.presentation.example.component.ui.layout.animation.AnimationsShowcaseExampleUI
import com.example.composesample.presentation.example.component.ui.layout.animation.SpringTweenSnapExampleUI
import com.example.composesample.presentation.example.component.ui.layout.animation.AnimationExampleUI
import com.example.composesample.presentation.example.component.ui.layout.animation.SharedElementDebugToolingExampleUI
import com.example.composesample.presentation.example.component.ui.layout.animation.SharedElementTransitionExampleUI
import com.example.composesample.presentation.example.component.ui.layout.bottomsheet.BottomSheetUI
import com.example.composesample.presentation.example.component.ui.layout.bottomsheet.CustomBottomSheetUI
import com.example.composesample.presentation.example.component.ui.layout.bottomsheet.ModalBottomSheetUI
import com.example.composesample.presentation.example.component.ui.layout.drawer.ModalDrawerExampleUI
import com.example.composesample.presentation.example.component.ui.layout.drawer.ScaffoldDrawerExampleUI
import com.example.composesample.presentation.example.component.ui.layout.flexbox.FlexBoxUI
import com.example.composesample.presentation.example.component.ui.layout.flexbox.FlowRowLayoutExampleUI
import com.example.composesample.presentation.example.component.ui.material3.Material3ExpressiveExampleUI
import com.example.composesample.presentation.example.component.ui.shader.AgslShaderTuningExampleUI
import com.example.composesample.presentation.example.component.ui.style.FoundationStyleApiExampleUI
import com.example.composesample.presentation.example.component.ui.layout.header.StickyHeaderExampleUI
import com.example.composesample.presentation.example.component.ui.layout.lazycolumn.LazyColumnFlingBehaviorExampleUI
import com.example.composesample.presentation.example.component.ui.layout.lazycolumn.LazyColumnIssueUI
import com.example.composesample.presentation.example.component.ui.layout.lazycolumn.LazyStaggeredGridExampleUI
import com.example.composesample.presentation.example.component.ui.layout.lazycolumn.ReverseLazyColumnExampleUI
import com.example.composesample.presentation.example.component.ui.layout.pager.PullScreenPagerUI
import com.example.composesample.presentation.example.component.ui.layout.topappbar.FancyTopAppBarExampleUI
import com.example.composesample.presentation.example.component.ui.media.lottie.LottieExampleUI
import com.example.composesample.presentation.example.component.ui.media.picker.EmbeddedPhotoPickerExampleUI
import com.example.composesample.presentation.example.component.ui.media.picker.EmbeddedPickerComposeExampleUI
import com.example.composesample.presentation.example.component.ui.media.shimmer.ShimmerExampleUI
import com.example.composesample.presentation.example.component.ui.media.shimmer.TextShimmerExampleUI
import com.example.composesample.presentation.example.component.ui.navigation.NestedRoutesNav3ExampleUI
import com.example.composesample.presentation.example.component.ui.notification.SnapNotifyExampleUI
import com.example.composesample.presentation.example.component.ui.scroll.CustomScrollBehaviorExampleUI
import com.example.composesample.presentation.example.component.ui.scroll.NestedScrollingExampleUI
import com.example.composesample.presentation.example.component.ui.shapes.CardCornersExampleUI
import com.example.composesample.presentation.example.component.ui.tab.ResponsiveTabRowExampleUI
import com.example.composesample.presentation.example.component.ui.layout.adaptive.AdaptiveLayoutExampleUI
import com.example.composesample.presentation.example.component.ui.layout.custom.CustomLayoutExampleUI
import com.example.composesample.presentation.example.component.ui.layout.modifier.ModifierOrderExampleUI
import com.example.composesample.presentation.example.component.ui.text.AutoSizingTextExampleUI
import com.example.composesample.presentation.example.component.ui.text.CustomTextRenderingExampleUI
import com.example.composesample.presentation.example.component.ui.text.LocalContextStringsExampleUI
import com.example.composesample.presentation.example.component.ui.text.RichContentTextInputExampleUI
import com.example.composesample.presentation.example.component.ui.text.TextFieldMaxLengthExampleUI
import com.example.composesample.presentation.example.component.ui.text.DocumentEditingTextFieldExampleUI
import com.example.composesample.presentation.example.component.ui.text.SyntaxHighlightingExampleUI
import com.example.composesample.presentation.example.component.ui.text.TextStyleUI
import com.example.composesample.presentation.example.component.ui.visibility.VisibilityExampleUI
import com.example.composesample.presentation.example.component.architecture.development.performance.InlineValueClassExampleUI
import com.example.composesample.presentation.example.component.architecture.development.internals.HowComposeWorksExampleUI
import com.example.composesample.presentation.example.component.architecture.development.internals.RememberObserverExampleUI
import com.example.composesample.presentation.example.component.ui.media.image.Coil3ImageExampleUI
import com.example.composesample.presentation.example.component.architecture.development.language.Kotlin24FeaturesExampleUI
import com.example.composesample.presentation.example.component.architecture.development.language.NameBasedDestructuringExampleUI
import com.example.composesample.presentation.example.component.architecture.development.language.SealedClassInterfaceExampleUI
import com.example.composesample.presentation.example.component.architecture.development.concurrency.CoroutinesInternalsExampleUI
import com.example.composesample.presentation.example.component.architecture.development.concurrency.WithContextExampleUI
import com.example.composesample.presentation.example.component.architecture.development.concurrency.CoroutineBridgesExampleUI
import com.example.composesample.presentation.example.component.architecture.development.concurrency.RaceConditionExampleUI
import com.example.composesample.presentation.example.component.architecture.development.concurrency.SelectExpressionExampleUI
import com.example.composesample.presentation.example.component.architecture.development.strictmode.StrictModeExampleUI
import com.example.composesample.presentation.example.component.architecture.development.flow.FlatMapExampleUI
import com.example.composesample.presentation.example.component.architecture.development.flow.FlowBatchingExampleUI
import com.example.composesample.presentation.example.component.architecture.development.flow.FlowOperatorsExampleUI
import com.example.composesample.presentation.example.component.architecture.navigation.Nav3SavedStateHandleExampleUI
import com.example.composesample.presentation.example.component.architecture.navigation.Nav3ViewModelScopeExampleUI
import com.example.composesample.presentation.example.component.architecture.navigation.Navigation3ExampleUI
import com.example.composesample.presentation.example.component.architecture.modularization.ModularizationExampleUI
import com.example.composesample.presentation.example.component.architecture.lifecycle.AutoCloseableExampleUI
import com.example.composesample.presentation.example.component.architecture.pattern.compositionLocal.StaticDynamicCompositionLocalExampleUI
import com.example.composesample.presentation.example.component.architecture.pattern.compositionLocal.tree.CompositionLocalTreeExampleUI
import com.example.composesample.presentation.example.component.architecture.pattern.remember.RememberPatternsExampleUI
import com.example.composesample.presentation.example.component.architecture.pattern.retain.RetainApiExampleUI
import com.example.composesample.presentation.example.component.architecture.development.compose17.Compose17FeaturesExampleUI
import com.example.composesample.presentation.example.component.architecture.development.performance.StabilityAnnotationsExampleUI
import com.example.composesample.presentation.example.component.architecture.development.test.ComposeTestingExampleUI
import com.example.composesample.presentation.example.component.architecture.state.ComposeSnapshotExampleUI
import com.example.composesample.presentation.example.component.architecture.state.PerItemViewModelExampleUI
import com.example.composesample.presentation.example.component.architecture.state.SnapshotFlowExampleUI
import com.example.composesample.util.ConstValue.AgslShaderTuningExample
import com.example.composesample.util.ConstValue.AnimatedContentExample
import com.example.composesample.util.ConstValue.AppSecurityExample
import com.example.composesample.util.ConstValue.HardwareKeystoreExample
import com.example.composesample.util.ConstValue.ScreenshotDetectionExample
import com.example.composesample.util.ConstValue.AnimationExample
import com.example.composesample.util.ConstValue.ApiDisconnectExample
import com.example.composesample.util.ConstValue.AutoCloseableExample
import com.example.composesample.util.ConstValue.AutofillExample
import com.example.composesample.util.ConstValue.AutoSizingTextExample
import com.example.composesample.util.ConstValue.AudioRecorderExample
import com.example.composesample.util.ConstValue.BottomSheetExample
import com.example.composesample.util.ConstValue.ButtonGroupExample
import com.example.composesample.util.ConstValue.CanvasShapesExample
import com.example.composesample.util.ConstValue.CardCornersExample
import com.example.composesample.util.ConstValue.ClickEventExample
import com.example.composesample.util.ConstValue.CompositionLocalExample
import com.example.composesample.util.ConstValue.CompositionLocalTreeExample
import com.example.composesample.util.ConstValue.CoordinatorExample
import com.example.composesample.util.ConstValue.CoroutineExample
import com.example.composesample.util.ConstValue.CoroutinesInternalsExample
import com.example.composesample.util.ConstValue.CursorIDEExample
import com.example.composesample.util.ConstValue.CustomBottomSheetExample
import com.example.composesample.util.ConstValue.CustomScrollBehaviorExample
import com.example.composesample.util.ConstValue.CustomTextRenderingExample
import com.example.composesample.util.ConstValue.AdvancedRepositoryPatternExample
import com.example.composesample.util.ConstValue.Media3VideoPlayerExample
import com.example.composesample.util.ConstValue.DataCacheExample
import com.example.composesample.util.ConstValue.DialComponentExample
import com.example.composesample.util.ConstValue.DragAndDropExample
import com.example.composesample.util.ConstValue.EmbeddedPhotoPickerExample
import com.example.composesample.util.ConstValue.EmbeddedPickerComposeExample
import com.example.composesample.util.ConstValue.FancyTopAppBarExample
import com.example.composesample.util.ConstValue.FlatMapExample
import com.example.composesample.util.ConstValue.FlowBatchingExample
import com.example.composesample.util.ConstValue.FlowOperatorsExample
import com.example.composesample.util.ConstValue.FlexBoxLayoutExample
import com.example.composesample.util.ConstValue.FlingBehaviorExample
import com.example.composesample.util.ConstValue.FocusRestorerExample
import com.example.composesample.util.ConstValue.GeminiNanoExample
import com.example.composesample.util.ConstValue.GlanceWidgetExample
import com.example.composesample.util.ConstValue.GraphicsLayerExample
import com.example.composesample.util.ConstValue.InitTestExample
import com.example.composesample.util.ConstValue.InlineValueClassExample
import com.example.composesample.util.ConstValue.KoinCompilerPluginExample
import com.example.composesample.util.ConstValue.FeatureFlagExample
import com.example.composesample.util.ConstValue.KtorAdvancedConfigExample
import com.example.composesample.util.ConstValue.KtorExample
import com.example.composesample.util.ConstValue.AccessibleFocusIndicatorExample
import com.example.composesample.util.ConstValue.LargeContentViewerExample
import com.example.composesample.util.ConstValue.LanguageSettingExample
import com.example.composesample.util.ConstValue.LazyColumnExample
import com.example.composesample.util.ConstValue.LazyStaggeredGridExample
import com.example.composesample.util.ConstValue.LocalContextStringsExample
import com.example.composesample.util.ConstValue.LocalLanguageChangeExample
import com.example.composesample.util.ConstValue.LookaheadScopeExample
import com.example.composesample.util.ConstValue.LottieExample
import com.example.composesample.util.ConstValue.MVIExample
import com.example.composesample.util.ConstValue.Material3ExpressiveExample
import com.example.composesample.util.ConstValue.FoundationStyleApiExample
import com.example.composesample.util.ConstValue.ModalBottomSheetExample
import com.example.composesample.util.ConstValue.MonthPickerDialExample
import com.example.composesample.util.ConstValue.ParticleEmitterExample
import com.example.composesample.util.ConstValue.FreehandDrawingExample
import com.example.composesample.util.ConstValue.ModalDrawExample
import com.example.composesample.util.ConstValue.ModularizationExample
import com.example.composesample.util.ConstValue.MotionBlurExample
import com.example.composesample.util.ConstValue.Nav3SavedStateHandleExample
import com.example.composesample.util.ConstValue.Nav3ViewModelScopeExample
import com.example.composesample.util.ConstValue.Navigation3Example
import com.example.composesample.util.ConstValue.NestedRoutesNav3Example
import com.example.composesample.util.ConstValue.NestedScrollingExample
import com.example.composesample.util.ConstValue.NewShadowApiExample
import com.example.composesample.util.ConstValue.MultiTableInsertExample
import com.example.composesample.util.ConstValue.RoomFtsSearchExample
import com.example.composesample.util.ConstValue.RoomIndexExample
import com.example.composesample.util.ConstValue.PagingExample
import com.example.composesample.util.ConstValue.PassingIntentDataExample
import com.example.composesample.util.ConstValue.PathGraphicsExample
import com.example.composesample.util.ConstValue.PowerSaveModeExample
import com.example.composesample.util.ConstValue.PullScreenPager
import com.example.composesample.util.ConstValue.PullToRefreshExample
import com.example.composesample.util.ConstValue.QuickSettingsTileExample
import com.example.composesample.util.ConstValue.PreviewInternalsExample
import com.example.composesample.util.ConstValue.StartupOptimizationExample
import com.example.composesample.util.ConstValue.RememberPatternsExample
import com.example.composesample.util.ConstValue.ReboundExample
import com.example.composesample.util.ConstValue.TurbineFlowTestExample
import com.example.composesample.util.ConstValue.RecompositionTestExample
import com.example.composesample.util.ConstValue.ResponsiveTabRowExample
import com.example.composesample.util.ConstValue.RetainApiExample
import com.example.composesample.util.ConstValue.ReverseLazyColumnExample
import com.example.composesample.util.ConstValue.SSEExample
import com.example.composesample.util.ConstValue.SafFileExample
import com.example.composesample.util.ConstValue.ScaffoldDrawExample
import com.example.composesample.util.ConstValue.Kotlin24FeaturesExample
import com.example.composesample.util.ConstValue.HowComposeWorksExample
import com.example.composesample.util.ConstValue.RememberObserverExample
import com.example.composesample.util.ConstValue.Coil3ImageExample
import com.example.composesample.util.ConstValue.NameBasedDestructuringExample
import com.example.composesample.util.ConstValue.SealedClassInterfaceExample
import com.example.composesample.util.ConstValue.SharedElementDebugToolingExample
import com.example.composesample.util.ConstValue.SharedElementTransitionExample
import com.example.composesample.util.ConstValue.ShortcutExample
import com.example.composesample.util.ConstValue.SideEffectExample
import com.example.composesample.util.ConstValue.SnapNotifyExample
import com.example.composesample.util.ConstValue.SnapshotFlowExample
import com.example.composesample.util.ConstValue.StaticDynamicCompositionLocalExample
import com.example.composesample.util.ConstValue.StickerCanvasExample
import com.example.composesample.util.ConstValue.StickyHeaderExample
import com.example.composesample.util.ConstValue.SwipeToDismissExample
import com.example.composesample.util.ConstValue.SwipeToDismissM3Example
import com.example.composesample.util.ConstValue.TargetSDK34PermissionExample
import com.example.composesample.util.ConstValue.TestExample
import com.example.composesample.util.ConstValue.TextOverflowExample
import com.example.composesample.util.ConstValue.TextShimmerExample
import com.example.composesample.util.ConstValue.TextStyleExample
import com.example.composesample.util.ConstValue.TypeExample
import com.example.composesample.util.ConstValue.UIShimmerExample
import com.example.composesample.util.ConstValue.VisibilityExample
import com.example.composesample.util.ConstValue.WebViewIssueExample
import com.example.composesample.util.ConstValue.WithContextExample
import com.example.composesample.util.ConstValue.CoroutineBridgesExample
import com.example.composesample.util.ConstValue.RaceConditionExample
import com.example.composesample.util.ConstValue.SelectExpressionExample
import com.example.composesample.util.ConstValue.StrictModeExample
import com.example.composesample.util.ConstValue.WorkManagerExample
import com.example.composesample.util.ConstValue.DynamicAppLinksExample
import com.example.composesample.util.ConstValue.BiometricAuthExample
import com.example.composesample.util.ConstValue.HapticFeedbackExample
import com.example.composesample.util.ConstValue.StabilityAnnotationsExample
import com.example.composesample.util.ConstValue.PredictiveBackExample
import com.example.composesample.util.ConstValue.SpringTweenSnapExample
import com.example.composesample.util.ConstValue.AnimationsShowcaseExample
import com.example.composesample.util.ConstValue.AdaptiveLayoutExample
import com.example.composesample.util.ConstValue.Compose17FeaturesExample
import com.example.composesample.util.ConstValue.ComposeLoadersExample
import com.example.composesample.util.ConstValue.FlowRowLayoutExample
import com.example.composesample.util.ConstValue.PreviewOnlyAnnotationExample
import com.example.composesample.util.ConstValue.RichContentTextInputExample
import com.example.composesample.util.ConstValue.TextFieldMaxLengthExample
import com.example.composesample.util.ConstValue.DocumentEditingTextFieldExample
import com.example.composesample.util.ConstValue.SyntaxHighlightingExample
import com.example.composesample.util.ConstValue.ComposeSnapshotExample
import com.example.composesample.util.ConstValue.PerItemViewModelExample
import com.example.composesample.util.ConstValue.ComposeTestingExample
import com.example.composesample.util.ConstValue.CustomLayoutExample
import com.example.composesample.util.ConstValue.ModifierOrderExample
import com.example.composesample.util.ConstValue.ScreenshotTestingExample
import com.example.composesample.util.ConstValue.PreviewDrivenScreenshotExample

/**
 * 예제 UI 디스패치 레지스트리 (UTIL-01).
 *
 * 기존 ExampleRouter 의 146분기 when 모놀리식을 레지스트리(Map)로 분리한 것.
 * - 키: ConstValue 예제 타입 문자열
 * - 값: onBackEvent 를 받아 해당 예제 화면을 그리는 @Composable 디스패처
 *
 * 예제를 추가할 때는 이 맵에 한 줄(키 to UI 람다)만 등록하면 라우팅이 연결된다.
 * (ConstValue 상수 추가 · Examples20XX 등록 · UI 파일 생성 절차는 기존과 동일)
 *
 * ExampleMoveType.ACTIVITY(BottomNavigation)·EMPTY 분기는 화면 구성이 아니라
 * Activity 시작/상태 초기화이므로 ExampleRouter 에 그대로 둔다.
 *
 * SSE / ReverseLazyColumn 의 SDK 게이팅, SafFile 의 파라미터명(onBackButtonClick) 등
 * 기존 동작은 그대로 보존한다.
 */
val exampleUiRegistry: Map<String, @Composable (onBackEvent: () -> Unit) -> Unit> = mapOf(
    LazyColumnExample to { onBackEvent -> LazyColumnIssueUI(onBackEvent) },
    LazyStaggeredGridExample to { onBackEvent -> LazyStaggeredGridExampleUI(onBackEvent) },
    ClickEventExample to { onBackEvent -> ClickEventUI(onBackEvent) },
    FlexBoxLayoutExample to { onBackEvent -> FlexBoxUI(onBackEvent) },
    FlowRowLayoutExample to { onBackEvent -> FlowRowLayoutExampleUI(onBackEvent) },
    ComposeLoadersExample to { onBackEvent -> ComposeLoadersExampleUI(onBackEvent) },
    WebViewIssueExample to { onBackEvent -> WebViewIssueUI(onBackEvent) },
    TextStyleExample to { onBackEvent -> TextStyleUI(onBackEvent) },
    AudioRecorderExample to { onBackEvent -> AudioRecorderUI(onBackEvent) },
    Media3VideoPlayerExample to { onBackEvent -> Media3VideoPlayerExampleUI(onBackEvent) },
    WorkManagerExample to { onBackEvent -> WorkManagerUI(onBackEvent) },
    PullToRefreshExample to { onBackEvent -> PullToRefreshUI(onBackEvent) },
    PullScreenPager to { onBackEvent -> PullScreenPagerUI(onBackEvent) },
    FlingBehaviorExample to { onBackEvent -> LazyColumnFlingBehaviorExampleUI(onBackEvent) },
    BottomSheetExample to { onBackEvent -> BottomSheetUI(onBackEvent) },
    ModalBottomSheetExample to { onBackEvent -> ModalBottomSheetUI(onBackEvent) },
    CustomBottomSheetExample to { onBackEvent -> CustomBottomSheetUI(onBackEvent) },
    ScaffoldDrawExample to { onBackEvent -> ScaffoldDrawerExampleUI(onBackEvent) },
    ModalDrawExample to { onBackEvent -> ModalDrawerExampleUI(onBackEvent) },
    SwipeToDismissExample to { onBackEvent -> SwipeToDismissUI(onBackEvent) },
    SwipeToDismissM3Example to { onBackEvent -> SwipeToDismissM3ExampleUI(onBackEvent) },
    SideEffectExample to { onBackEvent -> SideEffectExampleUI(onBackEvent) },
    DataCacheExample to { onBackEvent -> DataCacheExampleUI(onBackEvent) },
    AdvancedRepositoryPatternExample to { onBackEvent -> AdvancedRepositoryPatternExampleUI(onBackEvent) },
    ApiDisconnectExample to { onBackEvent -> ApiDisconnectExampleUI(onBackEvent) },
    PowerSaveModeExample to { onBackEvent -> PowerSaveModeExampleUI(onBackEvent) },
    DragAndDropExample to { onBackEvent -> DragAndDropExampleUI(onBackEvent) },
    TargetSDK34PermissionExample to { onBackEvent -> TargetSDK34ExampleUI(onBackEvent) },
    PassingIntentDataExample to { onBackEvent -> PassingIntentDataExampleUI(onBackEvent) },
    LottieExample to { onBackEvent -> LottieExampleUI(onBackEvent) },
    StickyHeaderExample to { onBackEvent -> StickyHeaderExampleUI(onBackEvent) },
    CursorIDEExample to { onBackEvent -> CursorIDEExampleUI(onBackEvent) },
    KtorExample to { onBackEvent -> KtorExampleUI(onBackEvent) },
    KtorAdvancedConfigExample to { onBackEvent -> KtorAdvancedConfigExampleUI(onBackEvent) },
    AnimationExample to { onBackEvent -> AnimationExampleUI(onBackEvent) },
    AnimatedContentExample to { onBackEvent -> AnimatedContentExampleUI(onBackEvent) },
    SharedElementTransitionExample to { onBackEvent -> SharedElementTransitionExampleUI(onBackEvent) },
    SharedElementDebugToolingExample to { onBackEvent -> SharedElementDebugToolingExampleUI(onBackEvent) },
    SSEExample to { onBackEvent ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SSEExampleUI(onBackEvent)
        }
    },
    MVIExample to { onBackEvent -> MVIExampleUI(onBackEvent) },
    CoordinatorExample to { onBackEvent -> CoordinatorExampleUI(onBackEvent) },
    TestExample to { onBackEvent -> UITestExampleUI(onBackEvent) },
    ReverseLazyColumnExample to { onBackEvent ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ReverseLazyColumnExampleUI(onBackEvent)
        }
    },
    CoroutineExample to { onBackEvent -> CoroutineExampleUI(onBackEvent) },
    UIShimmerExample to { onBackEvent -> ShimmerExampleUI(onBackEvent) },
    TextShimmerExample to { onBackEvent -> TextShimmerExampleUI(onBackEvent) },
    CompositionLocalExample to { onBackEvent -> CompositionLocalExampleUI(onBackEvent) },
    InitTestExample to { onBackEvent -> InitTestExampleUI(onBackEvent) },
    ShortcutExample to { onBackEvent -> ShortcutExampleUI(onBackEvent) },
    PagingExample to { onBackEvent -> PagingExampleUI(onBackEvent) },
    MultiTableInsertExample to { onBackEvent -> MultiTableInsertExampleUI(onBackEvent) },
    RoomFtsSearchExample to { onBackEvent -> RoomFtsSearchExampleUI(onBackEvent) },
    RoomIndexExample to { onBackEvent -> RoomIndexExampleUI(onBackEvent) },
    TypeExample to { onBackEvent -> TypeExampleUI(onBackEvent) },
    SafFileExample to { onBackEvent -> SafFileSelectionUI(onBackButtonClick = onBackEvent) },
    LanguageSettingExample to { onBackEvent -> LanguageSettingExampleUI(onBackEvent) },
    LocalLanguageChangeExample to { onBackEvent -> LocalLanguageChangeExampleUI(onBackEvent) },
    TextOverflowExample to { onBackEvent -> TextOverflowExampleUI(onBackEvent) },
    GraphicsLayerExample to { onBackEvent -> GraphicsLayerExampleUI(onBackEvent) },
    AgslShaderTuningExample to { onBackEvent -> AgslShaderTuningExampleUI(onBackEvent) },
    LookaheadScopeExample to { onBackEvent -> LookaheadScopeExampleUI(onBackEvent) },
    FocusRestorerExample to { onBackEvent -> FocusRestorerExampleUI(onBackEvent) },
    PathGraphicsExample to { onBackEvent -> PathGraphicsExampleUI(onBackEvent) },
    NestedScrollingExample to { onBackEvent -> NestedScrollingExampleUI(onBackEvent) },
    CustomScrollBehaviorExample to { onBackEvent -> CustomScrollBehaviorExampleUI(onBackEvent) },
    StickerCanvasExample to { onBackEvent -> StickerCanvasExampleUI(onBackEvent) },
    AccessibleFocusIndicatorExample to { onBackEvent -> AccessibleFocusIndicatorExampleUI(onBackEvent) },
    LargeContentViewerExample to { onBackEvent -> LargeContentViewerExampleUI(onBackEvent) },
    MotionBlurExample to { onBackEvent -> MotionBlurExampleUI(onBackEvent) },
    LocalContextStringsExample to { onBackEvent -> LocalContextStringsExampleUI(onBackEvent) },
    EmbeddedPickerComposeExample to { onBackEvent -> EmbeddedPickerComposeExampleUI(onBackEvent) },
    SnapshotFlowExample to { onBackEvent -> SnapshotFlowExampleUI(onBackEvent) },
    GlanceWidgetExample to { onBackEvent -> GlanceWidgetExampleUI(onBackEvent) },
    AutoSizingTextExample to { onBackEvent -> AutoSizingTextExampleUI(onBackEvent) },
    RichContentTextInputExample to { onBackEvent -> RichContentTextInputExampleUI(onBackEvent) },
    TextFieldMaxLengthExample to { onBackEvent -> TextFieldMaxLengthExampleUI(onBackEvent) },
    SyntaxHighlightingExample to { onBackEvent -> SyntaxHighlightingExampleUI(onBackEvent) },
    DocumentEditingTextFieldExample to { onBackEvent -> DocumentEditingTextFieldExampleUI(onBackEvent) },
    Material3ExpressiveExample to { onBackEvent -> Material3ExpressiveExampleUI(onBackEvent) },
    FoundationStyleApiExample to { onBackEvent -> FoundationStyleApiExampleUI(onBackEvent) },
    NestedRoutesNav3Example to { onBackEvent -> NestedRoutesNav3ExampleUI(onBackEvent) },
    NewShadowApiExample to { onBackEvent -> NewShadowApiExampleUI(onBackEvent) },
    SnapNotifyExample to { onBackEvent -> SnapNotifyExampleUI(onBackEvent) },
    CardCornersExample to { onBackEvent -> CardCornersExampleUI(onBackEvent) },
    AutoCloseableExample to { onBackEvent -> AutoCloseableExampleUI(onBackEvent) },
    StaticDynamicCompositionLocalExample to { onBackEvent -> StaticDynamicCompositionLocalExampleUI(onBackEvent) },
    CompositionLocalTreeExample to { onBackEvent -> CompositionLocalTreeExampleUI(onBackEvent) },
    RetainApiExample to { onBackEvent -> RetainApiExampleUI(onBackEvent) },
    InlineValueClassExample to { onBackEvent -> InlineValueClassExampleUI(onBackEvent) },
    SealedClassInterfaceExample to { onBackEvent -> SealedClassInterfaceExampleUI(onBackEvent) },
    NameBasedDestructuringExample to { onBackEvent -> NameBasedDestructuringExampleUI(onBackEvent) },
    Kotlin24FeaturesExample to { onBackEvent -> Kotlin24FeaturesExampleUI(onBackEvent) },
    HowComposeWorksExample to { onBackEvent -> HowComposeWorksExampleUI(onBackEvent) },
    RememberObserverExample to { onBackEvent -> RememberObserverExampleUI(onBackEvent) },
    Coil3ImageExample to { onBackEvent -> Coil3ImageExampleUI(onBackEvent) },
    CoroutinesInternalsExample to { onBackEvent -> CoroutinesInternalsExampleUI(onBackEvent) },
    FlatMapExample to { onBackEvent -> FlatMapExampleUI(onBackEvent) },
    FlowOperatorsExample to { onBackEvent -> FlowOperatorsExampleUI(onBackEvent) },
    FlowBatchingExample to { onBackEvent -> FlowBatchingExampleUI(onBackEvent) },
    Navigation3Example to { onBackEvent -> Navigation3ExampleUI(onBackEvent) },
    Nav3ViewModelScopeExample to { onBackEvent -> Nav3ViewModelScopeExampleUI(onBackEvent) },
    Nav3SavedStateHandleExample to { onBackEvent -> Nav3SavedStateHandleExampleUI(onBackEvent) },
    ModularizationExample to { onBackEvent -> ModularizationExampleUI(onBackEvent) },
    WithContextExample to { onBackEvent -> WithContextExampleUI(onBackEvent) },
    CoroutineBridgesExample to { onBackEvent -> CoroutineBridgesExampleUI(onBackEvent) },
    RaceConditionExample to { onBackEvent -> RaceConditionExampleUI(onBackEvent) },
    SelectExpressionExample to { onBackEvent -> SelectExpressionExampleUI(onBackEvent) },
    StrictModeExample to { onBackEvent -> StrictModeExampleUI(onBackEvent) },
    ButtonGroupExample to { onBackEvent -> ButtonGroupExampleUI(onBackEvent) },
    VisibilityExample to { onBackEvent -> VisibilityExampleUI(onBackEvent) },
    RecompositionTestExample to { onBackEvent -> RecompositionTestExampleUI(onBackEvent) },
    ReboundExample to { onBackEvent -> ReboundExampleUI(onBackEvent) },
    TurbineFlowTestExample to { onBackEvent -> TurbineFlowTestExampleUI(onBackEvent) },
    PreviewInternalsExample to { onBackEvent -> PreviewInternalsExampleUI(onBackEvent) },
    PreviewOnlyAnnotationExample to { onBackEvent -> PreviewOnlyAnnotationExampleUI(onBackEvent) },
    StartupOptimizationExample to { onBackEvent -> StartupOptimizationExampleUI(onBackEvent) },
    AppSecurityExample to { onBackEvent -> AppSecurityExampleUI(onBackEvent) },
    HardwareKeystoreExample to { onBackEvent -> HardwareKeystoreExampleUI(onBackEvent) },
    ScreenshotDetectionExample to { onBackEvent -> ScreenshotDetectionExampleUI(onBackEvent) },
    RememberPatternsExample to { onBackEvent -> RememberPatternsExampleUI(onBackEvent) },
    QuickSettingsTileExample to { onBackEvent -> QuickSettingsTileExampleUI(onBackEvent) },
    FancyTopAppBarExample to { onBackEvent -> FancyTopAppBarExampleUI(onBackEvent) },
    CanvasShapesExample to { onBackEvent -> CanvasShapesExampleUI(onBackEvent) },
    DialComponentExample to { onBackEvent -> DialComponentExampleUI(onBackEvent) },
    MonthPickerDialExample to { onBackEvent -> MonthPickerDialExampleUI(onBackEvent) },
    ParticleEmitterExample to { onBackEvent -> ParticleEmitterExampleUI(onBackEvent) },
    FreehandDrawingExample to { onBackEvent -> FreehandDrawingExampleUI(onBackEvent) },
    EmbeddedPhotoPickerExample to { onBackEvent -> EmbeddedPhotoPickerExampleUI(onBackEvent) },
    ResponsiveTabRowExample to { onBackEvent -> ResponsiveTabRowExampleUI(onBackEvent) },
    CustomTextRenderingExample to { onBackEvent -> CustomTextRenderingExampleUI(onBackEvent) },
    DynamicAppLinksExample to { onBackEvent -> DynamicAppLinksExampleUI(onBackEvent) },
    GeminiNanoExample to { onBackEvent -> GeminiNanoExampleUI(onBackEvent) },
    AdaptiveLayoutExample to { onBackEvent -> AdaptiveLayoutExampleUI(onBackEvent) },
    CustomLayoutExample to { onBackEvent -> CustomLayoutExampleUI(onBackEvent) },
    ModifierOrderExample to { onBackEvent -> ModifierOrderExampleUI(onBackEvent) },
    ScreenshotTestingExample to { onBackEvent -> ScreenshotTestingExampleUI(onBackEvent) },
    PreviewDrivenScreenshotExample to { onBackEvent -> PreviewDrivenScreenshotExampleUI(onBackEvent) },
    Compose17FeaturesExample to { onBackEvent -> Compose17FeaturesExampleUI(onBackEvent) },
    ComposeSnapshotExample to { onBackEvent -> ComposeSnapshotExampleUI(onBackEvent) },
    PerItemViewModelExample to { onBackEvent -> PerItemViewModelExampleUI(onBackEvent) },
    ComposeTestingExample to { onBackEvent -> ComposeTestingExampleUI(onBackEvent) },
    PredictiveBackExample to { onBackEvent -> PredictiveBackExampleUI(onBackEvent) },
    SpringTweenSnapExample to { onBackEvent -> SpringTweenSnapExampleUI(onBackEvent) },
    AnimationsShowcaseExample to { onBackEvent -> AnimationsShowcaseExampleUI(onBackEvent) },
    HapticFeedbackExample to { onBackEvent -> HapticFeedbackExampleUI(onBackEvent) },
    BiometricAuthExample to { onBackEvent -> BiometricAuthExampleUI(onBackEvent) },
    StabilityAnnotationsExample to { onBackEvent -> StabilityAnnotationsExampleUI(onBackEvent) },
    KoinCompilerPluginExample to { onBackEvent -> KoinCompilerPluginExampleUI(onBackEvent) },
    FeatureFlagExample to { onBackEvent -> FeatureFlagExampleUI(onBackEvent) },
    AutofillExample to { onBackEvent -> AutofillExampleUI(onBackEvent) },
)
