package com.example.bignews.repository

import com.example.bignews.api.RetrofitInstance
import com.example.bignews.db.ArticleDatabase
import com.example.bignews.models.Article

class NewsRepository(val db: ArticleDatabase) {

    suspend fun getHeadlines(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getHeadlines(countryCode, pageNumber)
    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        RetrofitInstance.api.searchForNews(searchQuery, pageNumber)
    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)
    fun getNewspaperNews() = db.getArticleDao().getAllArticles()
    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)

}