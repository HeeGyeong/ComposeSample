package com.example.domain.useCase

import com.example.domain.repository.PostRepository

class GetPostUseCase(
    private val postRepository: PostRepository
) {
    operator fun invoke() = postRepository.getPosts()
}