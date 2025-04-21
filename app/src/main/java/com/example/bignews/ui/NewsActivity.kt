package com.example.bignews.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.bignews.R
import com.example.bignews.databinding.ActivityNewsBinding
import com.example.bignews.db.ArticleDatabase
import com.example.bignews.repository.NewsRepository
import com.example.bignews.ui.NewsViewModel
import com.example.bignews.ui.NewsViewModelProviderFactory
import androidx.navigation.ui.setupWithNavController

class NewsActivity : AppCompatActivity() {

    lateinit var newsViewModel: NewsViewModel
    lateinit var binding: ActivityNewsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newsRepository = NewsRepository(ArticleDatabase(this))
        val viewModelProviderFactory = NewsViewModelProviderFactory(application, newsRepository)
        newsViewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

    }
}