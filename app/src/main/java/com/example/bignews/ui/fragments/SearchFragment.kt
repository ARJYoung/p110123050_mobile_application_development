package com.example.bignews.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.widget.AbsListView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bignews.R
import com.example.bignews.adapters.NewsAdaptor
import com.example.bignews.databinding.FragmentSearchBinding
import com.example.bignews.ui.NewsActivity
import com.example.bignews.ui.NewsViewModel
import com.example.bignews.util.Constants
import com.example.bignews.util.Constants.Companion.SEARCH_NEWS_TIME_DELAY
import com.example.bignews.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.widget.addTextChangedListener


class SearchFragment : Fragment(R.layout.fragment_search) {

    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdaptor: NewsAdaptor
    lateinit var retryButton: Button
    lateinit var errorText: TextView
    lateinit var itemSearchError: CardView
    lateinit var binding: FragmentSearchBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSearchBinding.bind(view)

        itemSearchError = view.findViewById(R.id.itemSearchError)

        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_error, null)

        retryButton = view.findViewById(R.id.retryButton)
        errorText = view.findViewById(R.id.errorText)

        newsViewModel = (activity as NewsActivity).newsViewModel
        setupSearchRecycler()

        newsAdaptor.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_searchFragment_to_articleFragment, bundle)
        }
        var job: Job? = null
        binding.searchEdit.addTextChangedListener() { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()) {
                        newsViewModel.searchNews(editable.toString())
                    }
                }
            }
        }
        newsViewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success<*> -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdaptor.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = newsViewModel.searchNewsPage == totalPages
                        if (isLastPage) {
                            binding.recyclerSearch.setPadding(0, 0, 0, 0)
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity, "An error occurred: $message", Toast.LENGTH_LONG)
                            .show()
                        showErrorMessage(message)
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }

            }
        })

        retryButton.setOnClickListener {
            if (binding.searchEdit.text.toString().isNotEmpty()) {
                newsViewModel.searchNews(binding.searchEdit.text.toString())
            } else {
                hideErrorMessage()
            }
        }
    }

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false

    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideErrorMessage() {
        itemSearchError.visibility = View.INVISIBLE
        isError = false
    }

    private fun showErrorMessage(message: String) {
        itemSearchError.visibility = View.VISIBLE
        errorText.text = message
        isError = true
    }

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoErrors = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate =
                isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                newsViewModel.searchNews(binding.searchEdit.text.toString())
                isScrolling = false
            }
        }


        fun onScrollStateChanged(recyclerView: RecyclerView, newsState: SavedState, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun setupSearchRecycler() {
        newsAdaptor = NewsAdaptor()
        binding.recyclerSearch.apply {
            adapter = newsAdaptor
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchFragment.scrollListener)
        }
    }
}