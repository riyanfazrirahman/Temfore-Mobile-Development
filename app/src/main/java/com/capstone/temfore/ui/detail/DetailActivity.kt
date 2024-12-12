package com.capstone.temfore.ui.detail

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.capstone.temfore.R
import com.capstone.temfore.data.remote.response.ListRecommendItem
import com.capstone.temfore.data.remote.retrofit.ApiConfig
import com.capstone.temfore.databinding.ActivityDetailBinding
import com.capstone.temfore.ui.favorite.FavoriteViewModel
import com.capstone.temfore.ui.favorite.FavoriteViewModelFactory
import com.capstone.temfore.utils.TextFormat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val viewModel by viewModels<FavoriteViewModel> {
        FavoriteViewModelFactory.getInstance(application)
    }
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idFood = intent.getStringExtra("FOOD_ID")?.toIntOrNull()
        Log.d(TAG, "Received FOOD_ID: $idFood - ${idFood?.javaClass?.simpleName}")

        if (idFood != null) {
            getEventDetails(idFood)
            cekStatusiFavorite(idFood)
            binding.ivFavorite.setOnClickListener {
                setEventFavorite(isFavorite, idFood)
            }
        } else {
            Log.e(TAG, "FOOD ID is null")
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.ivBack.setOnClickListener {
            onSupportNavigateUp()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        finish()
        return super.onSupportNavigateUp()
    }

    private fun cekStatusiFavorite(idEvent: Int) {
        Log.d(TAG, "Cek icon favorite event")
        return viewModel.isFavoriteEvent(idEvent).observe(this) { favorite ->
            Log.d(TAG, "Received isFavorite status: $isFavorite")
            if (favorite == false) {
                binding.ivFavorite.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.ic_favorite_border_24dp)
                )
                Log.d(TAG, "isFavorite status: $favorite")
                isFavorite = favorite
            } else {
                binding.ivFavorite.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_baseline_favorite_24
                    )
                )
                Log.d(TAG, "isFavorite status: $favorite")
                isFavorite = favorite
            }
        }
    }

    private fun setEventFavorite(isFavorite: Boolean, idEvent: Int) {
        if (!isFavorite) {
            viewModel.addFavoriteEvent(idEvent)
            Toast.makeText(this, "Add Favorite", Toast.LENGTH_SHORT).show()
            binding.ivFavorite.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.ic_baseline_favorite_24)
            )
            Log.d(TAG, "Update isFavorite status: $isFavorite")
        } else {
            viewModel.deleteFavoriteEvent(idEvent)
            Toast.makeText(this, "Remove Favorite", Toast.LENGTH_SHORT).show()
            binding.ivFavorite.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_baseline_favorite_24
                )
            )
            Log.d(TAG, "Update isFavorite status: $isFavorite")
        }
    }


    private fun getEventDetails(id: Int) {
        showLoading(true)
        val client = ApiConfig.getApiService().getFoodDetail(id)

        client.enqueue(object : Callback<ListRecommendItem> {
            override fun onResponse(
                call: Call<ListRecommendItem>,
                response: Response<ListRecommendItem>,
            ) {
                showLoading(false)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val food = responseBody
                    if (food != null) {
                        setEventDetailData(food)
                    } else {
                        Log.e(TAG, "Event data is missing or incomplete")
                        Toast.makeText(
                            this@DetailActivity,
                            "Event data is missing or incomplete",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else {
                    Log.e(TAG, "Response failed: ${response.message()}")
                    Toast.makeText(
                        this@DetailActivity,
                        "Failed to load event details",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<ListRecommendItem>, t: Throwable) {
                showLoading(false)
                Log.e(TAG, "Network call failed: ${t.message}")
                Toast.makeText(
                    this@DetailActivity,
                    "Failed to load event details. Please check your connection.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        })
    }

    private fun setEventDetailData(food: ListRecommendItem) {
        binding.tvFoodTemp.text = TextFormat.formatTemperatureMessage(food.tempCold)
        binding.tvFoodType.text =  TextFormat.formatType(food.type)
        binding.tvFoodTitle.text = food.title
        binding.tvFoodIngredients.text = TextFormat.formatIngredients(food.ingredients)
        binding.tvFoodSteps.text = TextFormat.formatSteps(food.steps)

        val imgCategory = TextFormat.getCategoryImage(food.category.toString())
        Glide.with(binding.foodCategory.context)
            .load(imgCategory)
            .into(binding.foodCategory)

        Glide.with(binding.ivFoodImage.context)
            .load(food.imageUrl)
            .into(binding.ivFoodImage)
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }


    companion object {
        private val TAG = DetailActivity::class.java.simpleName
    }
}