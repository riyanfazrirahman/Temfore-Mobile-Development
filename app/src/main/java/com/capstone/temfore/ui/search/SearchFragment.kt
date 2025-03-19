package com.capstone.temfore.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.temfore.data.remote.response.ListRecommendItem
import com.capstone.temfore.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    // This property is only valid between onCreateView and
    // onDestroyView.
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchViewModel: SearchViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        searchViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(SearchViewModel::class.java)

        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireActivity())
        // Fetch food recommendations after weather data

        with(binding) {
            searchView.setupWithSearchBar(searchBar)
            searchView
                .editText
                .setOnEditorActionListener { _, _, _ ->
                    searchBar.setText(searchView.text)
                    searchView.hide()
                    Toast.makeText(requireActivity(), searchView.text, Toast.LENGTH_SHORT).show()
                    val query = searchView.text.toString()
                    if (query.isNotEmpty()) {
                        fetchFoodSearch(query)
                    } else {
                        Toast.makeText(requireActivity(), "Please enter a search term", Toast.LENGTH_SHORT).show()
                    }
                    false
                }
        }
        searchViewModel.searchFood.observe(viewLifecycleOwner) { result ->
            setSearchData(result)
        }

        searchViewModel.isLoading.observe(requireActivity()) {
            showLoading(it)
        }

        return binding.root
    }

    private fun fetchFoodSearch(query:String) {
        Log.d(TAG, "API request Recommend......................")
        searchViewModel.searchFoodByTitle(query)
    }

    private fun setSearchData(listEventsItem: List<ListRecommendItem>) {
        val adapter = SearchAdapter()
        adapter.submitList(listEventsItem)
        binding.rvSearchResults.adapter = adapter

        // Cek apakah data kosong dan sesuaikan tampilan
        if (listEventsItem.isEmpty()) {
            binding.ivEmptySearch.visibility = View.VISIBLE  // Menampilkan logo jika kosong
            binding.tvEmptySearch.visibility = View.VISIBLE  // Menampilkan logo jika kosong
            binding.rvSearchResults.visibility = View.GONE  // Menyembunyikan RecyclerView
        } else {
            binding.ivEmptySearch.visibility = View.GONE  // Menyembunyikan logo
            binding.tvEmptySearch.visibility = View.GONE  // Menyembunyikan logo
            binding.rvSearchResults.visibility = View.VISIBLE  // Menampilkan RecyclerView
        }
    }

    private fun showLoading(isLoading: Boolean) {
        _binding?.let { binding ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        } ?: run {
            Log.e("SearchFragment", "showLoading dipanggil setelah view dihancurkan")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val TAG = SearchFragment::class.java.simpleName
    }
}