package com.example.composesample.di

// 기본 사용 모듈
val KoinModules = listOf(
    apiModule,
    viewModelModule,
    networkModule,
    ktorModule,
)

// Clean Architecture에 추가되는 Module
val CleanArchitectureAddModules = listOf(
    RepositoryModule,
    UseCaseModule,
    databaseModule
)