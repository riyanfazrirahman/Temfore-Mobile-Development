package com.capstone.temfore.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.temfore.databinding.FragmentFavoriteBinding

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(layoutInflater, container, false)

        val factory: FavoriteViewModelFactory =
            FavoriteViewModelFactory.getInstance(requireActivity())
        val viewModel: FavoriteViewModel by viewModels {
            factory
        }

        val eventsAdapter = FavoriteAdapter { food ->
            viewModel.deleteFavoriteEvent(food.foodId.toInt())
        }

        viewModel.getAllFavoriteEvents().observe(viewLifecycleOwner) { favorite ->
            binding.progressBar.visibility = View.GONE
            eventsAdapter.submitList(favorite)

            // Cek apakah data kosong dan sesuaikan tampilan
            if (favorite.isEmpty()) {
                binding.ivEmptyFavorite.visibility = View.VISIBLE  // Menampilkan logo jika kosong
                binding.tvEmptyFavorite.visibility = View.VISIBLE  // Menampilkan logo jika kosong
                binding.rvFavorite.visibility = View.GONE  // Menyembunyikan RecyclerView
            } else {
                binding.ivEmptyFavorite.visibility = View.GONE  // Menyembunyikan logo
                binding.tvEmptyFavorite.visibility = View.GONE  // Menyembunyikan logo
                binding.rvFavorite.visibility = View.VISIBLE  // Menampilkan RecyclerView
            }
        }

        binding.rvFavorite.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = eventsAdapter
        }



        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

