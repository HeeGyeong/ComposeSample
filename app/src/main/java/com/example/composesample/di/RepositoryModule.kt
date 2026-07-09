package com.example.composesample.di

import com.example.data.repository.ArticleRepositoryImpl
import com.example.data.repository.MVIExampleRepositoryImpl
import com.example.data.repository.PostRepositoryImpl
import com.example.data.repository.UserCacheRepository
import com.example.data.repository.UserCacheRepositoryImpl
import com.example.data.repository.dataSource.PostDataSource
import com.example.data.repository.dataSourceImpl.PostDataSourceImpl
import com.example.domain.repository.ArticleRepository
import com.example.domain.repository.MVIExampleRepository
import com.example.domain.repository.PostRepository
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule: Module = module {
    single<PostDataSource> { PostDataSourceImpl(get(named("post"))) }
    single<PostRepository> { PostRepositoryImpl(get()) }
    single<MVIExampleRepository> { MVIExampleRepositoryImpl() }
    // DataCache 예제: ExampleDao(databaseModule 제공)를 주입해 Room 접근을 캡슐화
    single<UserCacheRepository> { UserCacheRepositoryImpl(get()) }
    // AdvancedRepositoryPattern 예제: Memory→Disk→Network 우선순위 해석 (싱글턴으로 캐시 상태 유지)
    single<ArticleRepository> { ArticleRepositoryImpl() }
}