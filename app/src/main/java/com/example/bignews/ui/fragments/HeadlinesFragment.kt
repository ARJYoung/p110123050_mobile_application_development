package com.example.bignews.ui.fragments

import com.example.bignews.R
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bignews.adapters.NewsAdaptor
import com.example.bignews.ui.NewsActivity
import com.example.bignews.ui.NewsViewModel
import com.example.bignews.util.Constants
import com.example.bignews.util.Resource
import org.w3c.dom.Text
import com.example.bignews.databinding.FragmentHeadlinesBinding
import android.widget.TextView


class HeadlinesFragment : Fragment(R.layout.fragment_headlines) {
    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdaptor: NewsAdaptor
    lateinit var retryButton: Button
    lateinit var errorText: Text
    lateinit var itemHeadlinesError: Error
    lateinit var binding: FragmentHeadlinesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHeadlinesBinding.bind(view)

        val itemHeadlinesError: TextView = view.findViewById(R.id.itemHeadlinesError)

        val inflater =
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_error, null)

        retryButton = view.findViewById(R.id.retryButton)
        errorText = view.findViewById(R.id.errorText)

        newsViewModel = (activity as NewsActivity).newsViewModel
        setupHeadlinesRecycler()

        newsAdaptor.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_headlinesFragment_to_articleFragment, bundle)

        }

        newsViewModel.headlines.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success<*> -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        //newsAdaptor.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = newsViewModel.headlinesPage == totalPages
                        if (isLastPage) {
                            binding.recyclerHeadlines.setPadding(0, 0, 0, 0)
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(activity, "An error occured: $message", Toast.LENGTH_LONG)
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
            newsViewModel.getHeadlines("uk")
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

        (itemHeadlinesError as TextView)!!.visibility = View.VISIBLE
        isError = false
    }

    private fun showErrorMessage(message: String) {
        (itemHeadlinesError as? TextView)?.visibility = View.INVISIBLE
    }

    val scrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
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
                newsViewModel.getHeadlines("us")
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> {
                    isScrolling = false
                    // Perform actions when scrolling stops
                    println("Scrolling stopped")
                }

                RecyclerView.SCROLL_STATE_DRAGGING -> {
                    isScrolling = true
                    println("Currently dragging")
                    // Perform actions while dragging
                }

                RecyclerView.SCROLL_STATE_SETTLING -> {
                    println("Settling after drag/fling")
                    // Perform actions while settling
                }
            }
        }
    }

    private fun setupHeadlinesRecycler() {
        newsAdaptor = NewsAdaptor() // Now it's assigning to the class property
        binding.recyclerHeadlines.apply {
            adapter = newsAdaptor
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@HeadlinesFragment.scrollListener)
        }
    }
}




