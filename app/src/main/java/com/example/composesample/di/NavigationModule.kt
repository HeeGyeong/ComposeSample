package com.example.composesample.di

import com.example.core.BaseViewModel
import com.example.core.navigation.NavigationImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val navigationModule: Module = module {
    viewModel { BaseViewModel(get(), get()) }
    single { NavigationImpl(get()) }
}