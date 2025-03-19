package com.capstone.temfore.ui.search

import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.temfore.data.remote.response.ListRecommendItem
import com.capstone.temfore.data.remote.response.RecommendResponse
import com.capstone.temfore.data.remote.retrofit.ApiConfig
import com.capstone.temfore.ui.home.RecommendationViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException

class SearchViewModel : ViewModel() {

    private val _searchFood = MutableLiveData<List<ListRecommendItem>>()
    val searchFood: LiveData<List<ListRecommendItem>> get() = _searchFood

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun searchFoodByTitle(query: String) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().searchFood(query)
        client.enqueue(object : Callback<RecommendResponse> {
            override fun onResponse(
                call: Call<RecommendResponse>,
                response: Response<RecommendResponse>
            ) {
                _isLoading.value = false
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    _searchFood.value = response.body()?.hasilPencarian
                } else {
                    Log.e(TAG, "onFailure: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<RecommendResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "onFailure: ${t.message}")
                if (t is SocketTimeoutException) {
                    Log.e(RecommendationViewModel.TAG, "Timeout occurred, please check your internet connection.")
                    // Bisa menampilkan pesan kepada pengguna
                }
            }
        })
    }

    companion object {
        private const val TAG = "SearchViewModel"
    }

}