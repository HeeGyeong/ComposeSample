package com.example.composesample.presentation.example.list

private const val BLOG_BASE_URL = "https://heegs.tistory.com/"

fun blogUrl(postId: Int): String = "$BLOG_BASE_URL$postId"
