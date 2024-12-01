package com.example.composesample.di

import com.example.domain.useCase.FetchDataUseCase
import com.example.domain.useCase.GetPostUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val UseCaseModule: Module = module {
    single { GetPostUseCase(get()) }
    single { FetchDataUseCase(get()) }
}