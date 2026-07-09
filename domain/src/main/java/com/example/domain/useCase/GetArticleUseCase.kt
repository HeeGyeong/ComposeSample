package com.example.domain.useCase

import com.example.domain.repository.ArticleRepository

class GetArticleUseCase(
    private val articleRepository: ArticleRepository
) {
    suspend operator fun invoke(id: String, forceRefresh: Boolean = false) =
        articleRepository.getArticle(id, forceRefresh)
}
