package com.example.composesample.presentation.example

import android.content.Intent
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.example.component.architecture.development.compose17.FocusRestorerExampleUI
import com.example.composesample.presentation.example.component.architecture.development.compose17.GraphicsLayerExampleUI
import com.example.composesample.presentation.example.component.architecture.development.compose17.LookaheadScopeExampleUI
import com.example.composesample.presentation.example.component.architecture.development.compose17.PathGraphicsExampleUI
import com.example.composesample.presentation.example.component.architecture.development.compose17.TextOverflowExampleUI
import com.example.composesample.presentation.example.component.architecture.development.coordinator.CoordinatorExampleUI
import com.example.composesample.presentation.example.component.architecture.development.cursor.CursorIDEExampleUI
import com.example.composesample.presentation.example.component.architecture.development.di.KoinCompilerPluginExampleUI
import com.example.composesample.presentation.example.component.architecture.development.init.InitTestExampleUI
import com.example.composesample.presentation.example.component.architecture.development.init.StartupOptimizationExampleUI
import com.example.composesample.presentation.example.component.architecture.development.preview.PreviewInternalsExampleUI
import com.example.composesample.presentation.example.component.architecture.development.preview.PreviewOnlyAnnotationExampleUI
import com.example.composesample.presentation.example.component.architecture.development.rebound.ReboundExampleUI
import com.example.composesample.presentation.example.component.architecture.development.test.TurbineFlowTestExampleUI
import com.example.composesample.presentation.example.component.architecture.development.test.RecompositionTestExampleUI
import com.example.composesample.presentation.example.component.architecture.development.test.ScreenshotTestingExampleUI
import com.example.composesample.presentation.example.component.architecture.development.test.UITestExampleUI
import com.example.composesample.presentation.example.component.system.platform.quicksettings.QuickSettingsTileExampleUI
import com.example.composesample.presentation.example.component.architecture.development.type.TypeExampleUI
import com.example.composesample.presentation.example.component.architecture.pattern.compositionLocal.CompositionLocalExampleUI
import com.example.composesample.presentation.example.component.architecture.pattern.coroutine.CoroutineExampleUI
import com.example.composesample.presentation.example.component.architecture.pattern.effect.SideEffectExampleUI
import com.example.composesample.presentation.example.component.architecture.pattern.mvi.MVIExampleUI
import com.example.composesample.presentation.example.component.data.api.ApiDisconnectExampleUI
import com.example.composesample.presentation.example.component.data.api.KtorExampleUI
import com.example.composesample.presentation.example.component.data.cache.DataCacheExampleUI
import com.example.composesample.presentation.example.component.data.paging.PagingExampleUI
import com.example.composesample.presentation.example.component.data.room.MultiTableInsertExampleUI
import com.example.composesample.presentation.example.component.data.sse.SSEExampleUI
import com.example.composesample.presentation.example.component.interaction.clickevent.ClickEventUI
import com.example.composesample.presentation.example.component.interaction.drag.DragAndDropExampleUI
import com.example.composesample.presentation.example.component.interaction.refresh.PullToRefreshUI
import com.example.composesample.presentation.example.component.interaction.sticker.StickerCanvasExampleUI
import com.example.composesample.presentation.example.component.interaction.swipe.SwipeToDismissM3ExampleUI
import com.example.composesample.presentation.example.component.interaction.swipe.SwipeToDismissUI
import com.example.composesample.presentation.example.component.navigation.BottomNavigationActivity
import com.example.composesample.presentation.example.component.system.ai.GeminiNanoExampleUI
import com.example.composesample.presentation.example.component.system.deeplink.DynamicAppLinksExampleUI
import com.example.composesample.presentation.example.component.system.platform.biometric.BiometricAuthExampleUI
import com.example.composesample.presentation.example.component.system.platform.haptic.HapticFeedbackExampleUI
import com.example.composesample.presentation.example.component.system.platform.predictiveback.PredictiveBackExampleUI
import com.example.composesample.presentation.example.component.system.background.workmanager.WorkManagerUI
import com.example.composesample.presentation.example.component.system.media.recorder.AudioRecorderUI
import com.example.composesample.presentation.example.component.system.platform.file.SafFileSelectionUI
import com.example.composesample.presentation.example.component.system.platform.intent.PassingIntentDataExampleUI
import com.example.composesample.presentation.example.component.system.platform.language.LanguageSettingExampleUI
import com.example.composesample.presentation.example.component.system.platform.language.LocalLanguageChangeExampleUI
import com.example.composesample.presentation.example.component.system.platform.powersave.PowerSaveModeExampleUI
import com.example.composesample.presentation.example.component.system.platform.shortcut.ShortcutExampleUI
import com.example.composesample.presentation.example.component.system.platform.version.TargetSDK34ExampleUI
import com.example.composesample.presentation.example.component.system.platform.webview.WebViewIssueUI
import com.example.composesample.presentation.example.component.system.ui.widget.GlanceWidgetExampleUI
import com.example.composesample.presentation.example.component.ui.accessibility.LargeContentViewerExampleUI
import com.example.composesample.presentation.example.component.ui.button.ButtonGroupExampleUI
import com.example.composesample.presentation.example.component.ui.canvas.CanvasShapesExampleUI
import com.example.composesample.presentation.example.component.ui.canvas.ComposeLoadersExampleUI
import com.example.composesample.presentation.example.component.ui.canvas.DialComponentExampleUI
import com.example.composesample.presentation.example.component.ui.canvas.MonthPickerDialExampleUI
import com.example.composesample.presentation.example.component.ui.canvas.MotionBlurExampleUI
import com.example.composesample.presentation.example.component.ui.graphics.NewShadowApiExampleUI
import com.example.composesample.presentation.example.component.ui.layout.animation.AnimatedContentExampleUI
import com.example.composesample.presentation.example.component.ui.layout.animation.SpringTweenSnapExampleUI
import com.example.composesample.presentation.example.component.ui.layout.animation.AnimationExampleUI
import com.example.composesample.presentation.example.component.ui.layout.animation.SharedElementTransitionExampleUI
import com.example.composesample.presentation.example.component.ui.layout.bottomsheet.BottomSheetUI
import com.example.composesample.presentation.example.component.ui.layout.bottomsheet.CustomBottomSheetUI
import com.example.composesample.presentation.example.component.ui.layout.bottomsheet.ModalBottomSheetUI
import com.example.composesample.presentation.example.component.ui.layout.drawer.ModalDrawerExampleUI
import com.example.composesample.presentation.example.component.ui.layout.drawer.ScaffoldDrawerExampleUI
import com.example.composesample.presentation.example.component.ui.layout.flexbox.FlexBoxUI
import com.example.composesample.presentation.example.component.ui.layout.flexbox.FlowRowLayoutExampleUI
import com.example.composesample.presentation.example.component.ui.material3.Material3ExpressiveExampleUI
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
import com.example.composesample.presentation.example.component.ui.text.TextStyleUI
import com.example.composesample.presentation.example.component.ui.visibility.VisibilityExampleUI
import com.example.composesample.presentation.example.component.architecture.development.performance.InlineValueClassExampleUI
import com.example.composesample.presentation.example.component.architecture.development.language.NameBasedDestructuringExampleUI
import com.example.composesample.presentation.example.component.architecture.development.language.SealedClassInterfaceExampleUI
import com.example.composesample.presentation.example.component.architecture.development.concurrency.CoroutinesInternalsExampleUI
import com.example.composesample.presentation.example.component.architecture.development.concurrency.WithContextExampleUI
import com.example.composesample.presentation.example.component.architecture.development.concurrency.CoroutineBridgesExampleUI
import com.example.composesample.presentation.example.component.architecture.development.flow.FlatMapExampleUI
import com.example.composesample.presentation.example.component.architecture.development.flow.FlowOperatorsExampleUI
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
import com.example.composesample.presentation.example.component.architecture.state.SnapshotFlowExampleUI
import com.example.composesample.util.ConstValue.AnimatedContentExample
import com.example.composesample.util.ConstValue.AnimationExample
import com.example.composesample.util.ConstValue.ApiDisconnectExample
import com.example.composesample.util.ConstValue.AutoCloseableExample
import com.example.composesample.util.ConstValue.AutoSizingTextExample
import com.example.composesample.util.ConstValue.AudioRecorderExample
import com.example.composesample.util.ConstValue.BottomNavigationExample
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
import com.example.composesample.util.ConstValue.DataCacheExample
import com.example.composesample.util.ConstValue.DialComponentExample
import com.example.composesample.util.ConstValue.DragAndDropExample
import com.example.composesample.util.ConstValue.EmbeddedPhotoPickerExample
import com.example.composesample.util.ConstValue.EmbeddedPickerComposeExample
import com.example.composesample.util.ConstValue.FancyTopAppBarExample
import com.example.composesample.util.ConstValue.FlatMapExample
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
import com.example.composesample.util.ConstValue.KtorExample
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
import com.example.composesample.util.ConstValue.ModalBottomSheetExample
import com.example.composesample.util.ConstValue.MonthPickerDialExample
import com.example.composesample.util.ConstValue.ModalDrawExample
import com.example.composesample.util.ConstValue.ModularizationExample
import com.example.composesample.util.ConstValue.MotionBlurExample
import com.example.composesample.util.ConstValue.Nav3ViewModelScopeExample
import com.example.composesample.util.ConstValue.Navigation3Example
import com.example.composesample.util.ConstValue.NestedRoutesNav3Example
import com.example.composesample.util.ConstValue.NestedScrollingExample
import com.example.composesample.util.ConstValue.NewShadowApiExample
import com.example.composesample.util.ConstValue.MultiTableInsertExample
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
import com.example.composesample.util.ConstValue.NameBasedDestructuringExample
import com.example.composesample.util.ConstValue.SealedClassInterfaceExample
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
import com.example.composesample.util.ConstValue.WorkManagerExample
import com.example.composesample.util.ConstValue.DynamicAppLinksExample
import com.example.composesample.util.ConstValue.BiometricAuthExample
import com.example.composesample.util.ConstValue.HapticFeedbackExample
import com.example.composesample.util.ConstValue.StabilityAnnotationsExample
import com.example.composesample.util.ConstValue.PredictiveBackExample
import com.example.composesample.util.ConstValue.SpringTweenSnapExample
import com.example.composesample.util.ConstValue.AdaptiveLayoutExample
import com.example.composesample.util.ConstValue.Compose17FeaturesExample
import com.example.composesample.util.ConstValue.ComposeLoadersExample
import com.example.composesample.util.ConstValue.FlowRowLayoutExample
import com.example.composesample.util.ConstValue.PreviewOnlyAnnotationExample
import com.example.composesample.util.ConstValue.RichContentTextInputExample
import com.example.composesample.util.ConstValue.TextFieldMaxLengthExample
import com.example.composesample.util.ConstValue.ComposeSnapshotExample
import com.example.composesample.util.ConstValue.ComposeTestingExample
import com.example.composesample.util.ConstValue.CustomLayoutExample
import com.example.composesample.util.ConstValue.ModifierOrderExample
import com.example.composesample.util.ConstValue.ScreenshotTestingExample
import com.example.domain.model.ExampleMoveType

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ExampleCaseUI(
    exampleType: MutableState<String>,
    exampleMoveType: MutableState<ExampleMoveType>,
    onBackEvent: () -> Unit
) {
    val context = LocalContext.current

    when (exampleMoveType.value) {
        ExampleMoveType.UI -> {
            AnimatedVisibility(
                visible = exampleType.value.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color.DarkGray)
                        .animateEnterExit(
                            enter = slideInVertically(
                                initialOffsetY = { height -> height },
                                animationSpec = tween()
                            ),
                            exit = slideOutVertically(
                                targetOffsetY = { height -> height },
                                animationSpec = tween(durationMillis = AnimationConstants.DefaultDurationMillis / 2)
                            )
                        )
                ) {
                    when (exampleType.value) {
                        LazyColumnExample -> {
                            LazyColumnIssueUI(onBackEvent)
                        }

                        LazyStaggeredGridExample -> {
                            LazyStaggeredGridExampleUI(onBackEvent)
                        }

                        ClickEventExample -> {
                            ClickEventUI(onBackEvent)
                        }

                        FlexBoxLayoutExample -> {
                            FlexBoxUI(onBackEvent)
                        }

                        FlowRowLayoutExample -> {
                            FlowRowLayoutExampleUI(onBackEvent)
                        }

                        ComposeLoadersExample -> {
                            ComposeLoadersExampleUI(onBackEvent)
                        }

                        WebViewIssueExample -> {
                            WebViewIssueUI(onBackEvent)
                        }

                        TextStyleExample -> {
                            TextStyleUI(onBackEvent)
                        }

                        AudioRecorderExample -> {
                            AudioRecorderUI(onBackEvent)
                        }

                        WorkManagerExample -> {
                            WorkManagerUI(onBackEvent)
                        }

                        PullToRefreshExample -> {
                            PullToRefreshUI(onBackEvent)
                        }

                        PullScreenPager -> {
                            PullScreenPagerUI(onBackEvent)
                        }

                        FlingBehaviorExample -> {
                            LazyColumnFlingBehaviorExampleUI(onBackEvent)
                        }

                        BottomSheetExample -> {
                            BottomSheetUI(onBackEvent)
                        }

                        ModalBottomSheetExample -> {
                            ModalBottomSheetUI(onBackEvent)
                        }

                        CustomBottomSheetExample -> {
                            CustomBottomSheetUI(onBackEvent)
                        }

                        ScaffoldDrawExample -> {
                            ScaffoldDrawerExampleUI(onBackEvent)
                        }

                        ModalDrawExample -> {
                            ModalDrawerExampleUI(onBackEvent)
                        }

                        SwipeToDismissExample -> {
                            SwipeToDismissUI(onBackEvent)
                        }

                        SwipeToDismissM3Example -> {
                            SwipeToDismissM3ExampleUI(onBackEvent)
                        }

                        SideEffectExample -> {
                            SideEffectExampleUI(onBackEvent)
                        }

                        DataCacheExample -> {
                            DataCacheExampleUI(onBackEvent)
                        }

                        ApiDisconnectExample -> {
                            ApiDisconnectExampleUI(onBackEvent)
                        }

                        PowerSaveModeExample -> {
                            PowerSaveModeExampleUI(onBackEvent)
                        }

                        DragAndDropExample -> {
                            DragAndDropExampleUI(onBackEvent)
                        }

                        TargetSDK34PermissionExample -> {
                            TargetSDK34ExampleUI(onBackEvent)
                        }

                        PassingIntentDataExample -> {
                            PassingIntentDataExampleUI(onBackEvent)
                        }

                        LottieExample -> {
                            LottieExampleUI(onBackEvent)
                        }

                        StickyHeaderExample -> {
                            StickyHeaderExampleUI(onBackEvent)
                        }

                        CursorIDEExample -> {
                            CursorIDEExampleUI(onBackEvent)
                        }

                        KtorExample -> {
                            KtorExampleUI(onBackEvent)
                        }

                        AnimationExample -> {
                            AnimationExampleUI(onBackEvent)
                        }

                        AnimatedContentExample -> {
                            AnimatedContentExampleUI(onBackEvent)
                        }

                        SharedElementTransitionExample -> {
                            SharedElementTransitionExampleUI(onBackEvent)
                        }

                        SSEExample -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                SSEExampleUI(onBackEvent)
                            }
                        }

                        MVIExample -> {
                            MVIExampleUI(onBackEvent)
                        }

                        CoordinatorExample -> {
                            CoordinatorExampleUI(onBackEvent)
                        }

                        TestExample -> {
                            UITestExampleUI(onBackEvent)
                        }

                        ReverseLazyColumnExample -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                ReverseLazyColumnExampleUI(onBackEvent)
                            }
                        }

                        CoroutineExample -> {
                            CoroutineExampleUI(onBackEvent)
                        }

                        UIShimmerExample -> {
                            ShimmerExampleUI(onBackEvent)
                        }

                        TextShimmerExample -> {
                            TextShimmerExampleUI(onBackEvent)
                        }

                        CompositionLocalExample -> {
                            CompositionLocalExampleUI(onBackEvent)
                        }

                        InitTestExample -> {
                            InitTestExampleUI(onBackEvent)
                        }

                        ShortcutExample -> {
                            ShortcutExampleUI(onBackEvent)
                        }

                        PagingExample -> {
                            PagingExampleUI(onBackEvent)
                        }

                        MultiTableInsertExample -> {
                            MultiTableInsertExampleUI(onBackEvent)
                        }

                        TypeExample -> {
                            TypeExampleUI(onBackEvent)
                        }

                        SafFileExample -> {
                            SafFileSelectionUI(onBackButtonClick = onBackEvent)
                        }

                        LanguageSettingExample -> {
                            LanguageSettingExampleUI(onBackEvent)
                        }

                        LocalLanguageChangeExample -> {
                            LocalLanguageChangeExampleUI(onBackEvent)
                        }

                        TextOverflowExample -> {
                            TextOverflowExampleUI(onBackEvent)
                        }

                        GraphicsLayerExample -> {
                            GraphicsLayerExampleUI(onBackEvent)
                        }

                        LookaheadScopeExample -> {
                            LookaheadScopeExampleUI(onBackEvent)
                        }

                        FocusRestorerExample -> {
                            FocusRestorerExampleUI(onBackEvent)
                        }

                        PathGraphicsExample -> {
                            PathGraphicsExampleUI(onBackEvent)
                        }

                        NestedScrollingExample -> {
                            NestedScrollingExampleUI(onBackEvent)
                        }

                        CustomScrollBehaviorExample -> {
                            CustomScrollBehaviorExampleUI(onBackEvent)
                        }

                        StickerCanvasExample -> {
                            StickerCanvasExampleUI(onBackEvent)
                        }

                        LargeContentViewerExample -> {
                            LargeContentViewerExampleUI(onBackEvent)
                        }

                        MotionBlurExample -> {
                            MotionBlurExampleUI(onBackEvent)
                        }

                        LocalContextStringsExample -> {
                            LocalContextStringsExampleUI(onBackEvent)
                        }

                        EmbeddedPickerComposeExample -> {
                            EmbeddedPickerComposeExampleUI(onBackEvent)
                        }

                        SnapshotFlowExample -> {
                            SnapshotFlowExampleUI(onBackEvent)
                        }

                        GlanceWidgetExample -> {
                            GlanceWidgetExampleUI(onBackEvent)
                        }

                        AutoSizingTextExample -> {
                            AutoSizingTextExampleUI(onBackEvent)
                        }

                        RichContentTextInputExample -> {
                            RichContentTextInputExampleUI(onBackEvent)
                        }

                        TextFieldMaxLengthExample -> {
                            TextFieldMaxLengthExampleUI(onBackEvent)
                        }

                        Material3ExpressiveExample -> {
                            Material3ExpressiveExampleUI(onBackEvent)
                        }

                        NestedRoutesNav3Example -> {
                            NestedRoutesNav3ExampleUI(onBackEvent)
                        }

                        NewShadowApiExample -> {
                            NewShadowApiExampleUI(onBackEvent)
                        }

                        SnapNotifyExample -> {
                            SnapNotifyExampleUI(onBackEvent)
                        }

                        CardCornersExample -> {
                            CardCornersExampleUI(onBackEvent)
                        }

                        AutoCloseableExample -> {
                            AutoCloseableExampleUI(onBackEvent)
                        }

                        StaticDynamicCompositionLocalExample -> {
                            StaticDynamicCompositionLocalExampleUI(onBackEvent)
                        }

                        CompositionLocalTreeExample -> {
                            CompositionLocalTreeExampleUI(onBackEvent)
                        }

                        RetainApiExample -> {
                            RetainApiExampleUI(onBackEvent)
                        }

                        InlineValueClassExample -> {
                            InlineValueClassExampleUI(onBackEvent)
                        }

                        SealedClassInterfaceExample -> {
                            SealedClassInterfaceExampleUI(onBackEvent)
                        }

                        NameBasedDestructuringExample -> {
                            NameBasedDestructuringExampleUI(onBackEvent)
                        }

                        CoroutinesInternalsExample -> {
                            CoroutinesInternalsExampleUI(onBackEvent)
                        }

                        FlatMapExample -> {
                            FlatMapExampleUI(onBackEvent)
                        }

                        FlowOperatorsExample -> {
                            FlowOperatorsExampleUI(onBackEvent)
                        }

                        Navigation3Example -> {
                            Navigation3ExampleUI(onBackEvent)
                        }

                        Nav3ViewModelScopeExample -> {
                            Nav3ViewModelScopeExampleUI(onBackEvent)
                        }

                        ModularizationExample -> {
                            ModularizationExampleUI(onBackEvent)
                        }

                        WithContextExample -> {
                            WithContextExampleUI(onBackEvent)
                        }

                        CoroutineBridgesExample -> {
                            CoroutineBridgesExampleUI(onBackEvent)
                        }

                        ButtonGroupExample -> {
                            ButtonGroupExampleUI(onBackEvent)
                        }

                        VisibilityExample -> {
                            VisibilityExampleUI(onBackEvent)
                        }

                        RecompositionTestExample -> {
                            RecompositionTestExampleUI(onBackEvent)
                        }

                        ReboundExample -> {
                            ReboundExampleUI(onBackEvent)
                        }

                        TurbineFlowTestExample -> {
                            TurbineFlowTestExampleUI(onBackEvent)
                        }

                        PreviewInternalsExample -> {
                            PreviewInternalsExampleUI(onBackEvent)
                        }

                        PreviewOnlyAnnotationExample -> {
                            PreviewOnlyAnnotationExampleUI(onBackEvent)
                        }

                        StartupOptimizationExample -> {
                            StartupOptimizationExampleUI(onBackEvent)
                        }

                        RememberPatternsExample -> {
                            RememberPatternsExampleUI(onBackEvent)
                        }

                        QuickSettingsTileExample -> {
                            QuickSettingsTileExampleUI(onBackEvent)
                        }

                        FancyTopAppBarExample -> {
                            FancyTopAppBarExampleUI(onBackEvent)
                        }

                        CanvasShapesExample -> {
                            CanvasShapesExampleUI(onBackEvent)
                        }

                        DialComponentExample -> {
                            DialComponentExampleUI(onBackEvent)
                        }

                        MonthPickerDialExample -> {
                            MonthPickerDialExampleUI(onBackEvent)
                        }

                        EmbeddedPhotoPickerExample -> {
                            EmbeddedPhotoPickerExampleUI(onBackEvent)
                        }

                        ResponsiveTabRowExample -> {
                            ResponsiveTabRowExampleUI(onBackEvent)
                        }

                        CustomTextRenderingExample -> {
                            CustomTextRenderingExampleUI(onBackEvent)
                        }

                        DynamicAppLinksExample -> {
                            DynamicAppLinksExampleUI(onBackEvent)
                        }

                        GeminiNanoExample -> {
                            GeminiNanoExampleUI(onBackEvent)
                        }

                        AdaptiveLayoutExample -> {
                            AdaptiveLayoutExampleUI(onBackEvent)
                        }

                        CustomLayoutExample -> {
                            CustomLayoutExampleUI(onBackEvent)
                        }

                        ModifierOrderExample -> {
                            ModifierOrderExampleUI(onBackEvent)
                        }

                        ScreenshotTestingExample -> {
                            ScreenshotTestingExampleUI(onBackEvent)
                        }

                        Compose17FeaturesExample -> {
                            Compose17FeaturesExampleUI(onBackEvent)
                        }

                        ComposeSnapshotExample -> {
                            ComposeSnapshotExampleUI(onBackEvent)
                        }

                        ComposeTestingExample -> {
                            ComposeTestingExampleUI(onBackEvent)
                        }

                        PredictiveBackExample -> {
                            PredictiveBackExampleUI(onBackEvent)
                        }

                        SpringTweenSnapExample -> {
                            SpringTweenSnapExampleUI(onBackEvent)
                        }

                        HapticFeedbackExample -> {
                            HapticFeedbackExampleUI(onBackEvent)
                        }

                        BiometricAuthExample -> {
                            BiometricAuthExampleUI(onBackEvent)
                        }

                        StabilityAnnotationsExample -> {
                            StabilityAnnotationsExampleUI(onBackEvent)
                        }

                        KoinCompilerPluginExample -> {
                            KoinCompilerPluginExampleUI(onBackEvent)
                        }

                        else -> {
                            Text(
                                text = "Dummy",
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }

        ExampleMoveType.ACTIVITY -> {
            when (exampleType.value) {
                BottomNavigationExample -> {
                    val intent =
                        Intent(
                            context,
                            BottomNavigationActivity::class.java
                        ).apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
                    context.startActivity(intent)
                }

                else -> {}
            }

            exampleMoveType.value = ExampleMoveType.EMPTY
        }

        ExampleMoveType.EMPTY -> {
            Unit
        }
    }
}
