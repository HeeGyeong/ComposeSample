package com.example.composesample.module

import com.example.composesample.movie.MovieViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val viewModelModule: Module = module {
    viewModel { MovieViewModel(get(), get()) }
}