package com.example.composesample.presentation.example.list

private const val BLOG_BASE_URL = "https://heegs.tistory.com/"

fun blogUrl(postId: Int): String = "$BLOG_BASE_URL$postId"

/** 블로그 홈 — 메인 화면 "Open Blog" 가 연다. 주소는 이 파일에만 둔다 */
fun blogHomeUrl(): String = BLOG_BASE_URL
