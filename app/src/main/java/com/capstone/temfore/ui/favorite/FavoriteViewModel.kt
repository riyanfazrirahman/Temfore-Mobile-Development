package com.capstone.temfore.ui.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.capstone.temfore.data.RecommendationRepository
import com.capstone.temfore.data.Result
import com.capstone.temfore.data.local.entity.FavoriteFood
import com.capstone.temfore.data.remote.response.ListRecommendItem
import com.capstone.temfore.data.remote.response.RecommendResponse

class FavoriteViewModel(private val recommendationRepository: RecommendationRepository) : ViewModel() {

    fun isFavoriteEvent(eventId: Int): LiveData<Boolean> {
        return recommendationRepository.isFoodFavorite(eventId)
    }

    fun deleteFavoriteEvent(eventId: Int) {
        recommendationRepository.removeFavoriteFoodById(eventId)
    }

    fun addFavoriteEvent(eventId: Int): LiveData<Result<ListRecommendItem>> {
        return recommendationRepository.insertFavoriteFood(eventId)
    }

    fun getAllFavoriteEvents(): LiveData<List<FavoriteFood>> {
        return recommendationRepository.getAllFavoriteFood()
    }
}