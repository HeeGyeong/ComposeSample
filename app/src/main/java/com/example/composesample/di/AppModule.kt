package com.example.composesample.di

import com.example.composesample.data.repository.MVIExampleRepository
import com.example.composesample.data.repository.MVIExampleRepositoryImpl
import com.example.composesample.domain.usecase.FetchDataUseCase
import com.example.composesample.presentation.example.component.mvi.MVIExampleViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<MVIExampleRepository> { MVIExampleRepositoryImpl() }
    single { FetchDataUseCase(get()) }
    viewModel { MVIExampleViewModel(get()) }
} 