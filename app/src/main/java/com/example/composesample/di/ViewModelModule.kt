package com.example.composesample.di

import com.example.composesample.presentation.example.component.api.ApiExampleUseCaseViewModel
import com.example.composesample.presentation.example.component.api.ApiExampleViewModel
import com.example.composesample.presentation.example.component.cache.DataCacheViewModel
import com.example.composesample.presentation.legacy.movie.MovieViewModel
import com.example.composesample.presentation.legacy.sub.SubActivityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val viewModelModule: Module = module {
    viewModel { MovieViewModel(get(), get(named("api"))) }
    viewModel {
        ApiExampleViewModel(
            get(),
            get(named("post")),
            get()
        )
    }
    viewModel { ApiExampleUseCaseViewModel(get(), get()) }
    viewModel { DataCacheViewModel(get(), get()) }
    viewModel { SubActivityViewModel(get(), get()) }
}