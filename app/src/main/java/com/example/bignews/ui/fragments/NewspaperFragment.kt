package com.example.bignews.ui.fragments

import com.example.bignews.R
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bignews.adapters.NewsAdaptor
import com.example.bignews.databinding.FragmentNewspaper1Binding
import com.example.bignews.ui.NewsActivity
import com.example.bignews.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar

class NewspaperFragment : Fragment(R.layout.fragment_newspaper1) {

    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdaptor: NewsAdaptor
    lateinit var binding: FragmentNewspaper1Binding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNewspaper1Binding.bind(view)

        newsViewModel = (activity as NewsActivity).newsViewModel
        setupNewspaperRecyclerView()

        newsAdaptor.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_favouritesFragment_to_articleFragment, bundle)
        }
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override  fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                //return true
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                return true
            }

           override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article= newsAdaptor.differ.currentList[position]
                newsViewModel.deleteArticle(article)
               Snackbar.make(view, "Successfully deleted article", Snackbar.LENGTH_LONG).apply {
                   setAction("Undo") {
                       newsViewModel.saveArticle(article)
                       newsViewModel.addToNewspaper(article)
                    }
                    show()
                }
               }
        }
        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.recyclerNewspaper)
        }
        newsViewModel.getNewspaperNews().observe(viewLifecycleOwner, Observer { articles ->
            newsAdaptor.differ.submitList(articles)
        })
    }

    private fun setupNewspaperRecyclerView() {
        newsAdaptor = NewsAdaptor()
        binding.recyclerNewspaper.apply {
            adapter = newsAdaptor
            layoutManager = LinearLayoutManager(activity)
        }
    }
}

private fun NewsViewModel.saveArticle(article: com.example.bignews.models.Article) {}
