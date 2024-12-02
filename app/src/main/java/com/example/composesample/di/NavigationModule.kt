package com.example.composesample.di

import com.example.composesample.coordinator.Coordinator
import com.example.core.BaseViewModel
import com.example.core.navigation.NavigationInterface
import com.example.core.navigation.Navigation
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val navigationInterfaceModule: Module = module {
    single<NavigationInterface> { Coordinator() }
    singleOf(::Navigation)
    viewModel { BaseViewModel(get(), get()) }
}