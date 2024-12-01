package com.example.composesample.di

import com.example.data.repository.MVIExampleRepositoryImpl
import com.example.data.repository.PostRepositoryImpl
import com.example.data.repository.dataSource.PostDataSource
import com.example.data.repository.dataSourceImpl.PostDataSourceImpl
import com.example.domain.repository.MVIExampleRepository
import com.example.domain.repository.PostRepository
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val RepositoryModule: Module = module {
    single<PostDataSource> { PostDataSourceImpl(get(named("post"))) }
    single<PostRepository> { PostRepositoryImpl(get()) }
    single<MVIExampleRepository> { MVIExampleRepositoryImpl() }
}