package com.example.composesample.di

val KoinModules = listOf(
    apiModule,
    viewModelModule,
    networkModule,

    // CleanArchitecture에 추가되는 Module
    RepositoryModule,
    UseCaseModule
)