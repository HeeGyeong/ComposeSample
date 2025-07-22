package com.example.composesample.di

import com.example.composesample.presentation.example.BlogExampleViewModel
import com.example.composesample.presentation.example.component.architecture.development.init.InitTestViewModel
import com.example.composesample.presentation.example.component.architecture.development.type.TypeExampleViewModel
import com.example.composesample.presentation.example.component.architecture.pattern.compositionLocal.CompositionLocalViewModel
import com.example.composesample.presentation.example.component.architecture.pattern.mvi.MVIExampleViewModel
import com.example.composesample.presentation.example.component.architecture.state.SnapshotFlowExampleViewModel
import com.example.composesample.presentation.example.component.data.api.ApiExampleUseCaseViewModel
import com.example.composesample.presentation.example.component.data.api.ApiExampleViewModel
import com.example.composesample.presentation.example.component.data.cache.DataCacheViewModel
import com.example.composesample.presentation.example.component.data.paging.PagingViewModel
import com.example.composesample.presentation.example.component.data.sse.SSEViewModel
import com.example.composesample.presentation.example.component.system.platform.file.SafFileSelectionViewModel
import com.example.composesample.presentation.legacy.movie.MovieViewModel
import com.example.composesample.presentation.legacy.sub.SubActivityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val viewModelModule: Module = module {
    viewModel {
        ApiExampleViewModel(
            get(),
            get(named("post")),
            get()
        )
    }

    viewModel { BlogExampleViewModel(get(), get()) }
    viewModel { MovieViewModel(get(), get(named("api"))) }
    viewModel { ApiExampleUseCaseViewModel(get(), get()) }
    viewModel { DataCacheViewModel(get(), get()) }
    viewModel { SubActivityViewModel(get(), get()) }

    viewModel { MVIExampleViewModel(get()) }
    viewModel { SSEViewModel() }
    viewModel { CompositionLocalViewModel() }
    viewModel { InitTestViewModel() }
    viewModel { PagingViewModel() }
    viewModel { TypeExampleViewModel() }
    viewModel { SafFileSelectionViewModel() }
    viewModel { SnapshotFlowExampleViewModel() }
}