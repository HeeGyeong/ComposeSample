package com.example.composesample.module

import com.example.composesample.example.ui.api.ApiExampleViewModel
import com.example.composesample.movie.MovieViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val viewModelModule: Module = module {
    viewModel {
        MovieViewModel(
            get(),
            get(named("api")),
        )
    }

    viewModel {
        ApiExampleViewModel(
            get(),
            get(named("post"))
        )
    }
}