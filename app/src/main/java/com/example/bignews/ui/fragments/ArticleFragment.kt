package com.example.bignews.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.navigation.fragment.navArgs
import com.example.bignews.R
import com.example.bignews.databinding.FragmentArticleBinding
import com.example.bignews.ui.NewsActivity
import com.example.bignews.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlin.getValue

class ArticleFragment : Fragment(R.layout.fragment_article) {
    lateinit var newsViewModel: NewsViewModel
    val args: ArticleFragmentArgs by navArgs()
    lateinit var binding: FragmentArticleBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentArticleBinding.bind(view)

        newsViewModel = (activity as NewsActivity).newsViewModel
        val article = args.article
        binding.webView.apply {
            webViewClient = WebViewClient()
            article.url?.let { loadUrl(it) }
        }

        binding.fab.setOnClickListener {
            newsViewModel.addToNewspaper(article)
            Snackbar.make(view, "Article saved successfully", Snackbar.LENGTH_SHORT).show()
        }
    }
}