package com.capstone.temfore.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capstone.temfore.R
import com.capstone.temfore.data.RecommendationRepository
import com.capstone.temfore.data.WeatherRepository
import com.capstone.temfore.data.remote.response.ListRecommendItem
import com.capstone.temfore.data.remote.retrofit.ApiConfig
import com.capstone.temfore.databinding.FragmentHomeBinding
import com.capstone.temfore.ui.auth.login.LoginActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: HomeViewModel
    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var recommendationViewModel: RecommendationViewModel
    private lateinit var auth: FirebaseAuth

    private var categoryUser: String = "ayam"
    private var tempUser: Int = 0
    private var timeUser: Int = 0

    // Permission request launcher untuk meminta izin lokasi
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLastLocation() // Ambil lokasi jika izin diberikan
            } else {
                // Handle ketika izin ditolak
                Log.d(TAG, "Permission Denied")
                Toast.makeText(
                    requireContext(),
                    "Izin lokasi diperlukan untuk mengambil cuaca!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inisialisasi binding untuk fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize ViewModels
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val weatherRepository = WeatherRepository(ApiConfig.getApiService())
        val weatherViewModelFactory = WeatherViewModelFactory(weatherRepository)
        weatherViewModel = ViewModelProvider(this, weatherViewModelFactory).get(WeatherViewModel::class.java)

        recommendationViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(RecommendationViewModel::class.java)

        auth = Firebase.auth
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            // Not signed in, launch the Login activity
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            requireActivity().finish()
        }
        Log.d("HomeFragment", "Email verified: ${firebaseUser?.isEmailVerified}")

        // Inisialisasi FusedLocationProviderClient untuk mendapatkan lokasi
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Periksa dan minta izin lokasi jika belum diberikan
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLastLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }


        binding.textItTime.text = getString(R.string.title_it_hello)

        viewModel.helloMessageData.observe(viewLifecycleOwner) {
            binding.textTime.text =  it
        }
        viewModel.timeMessageData.observe(viewLifecycleOwner) {
            binding.textUserTime.text = it
        }

        getListCategory()

        binding.progressBar.visibility = View.VISIBLE

        // Observasi data cuaca dari ViewModel
        weatherViewModel.weatherData.observe(viewLifecycleOwner) { weatherResponse ->
            binding.progressBar.visibility = View.GONE

            if (weatherResponse != null) {
                // Update UI dengan data cuaca
                tempUser = weatherResponse.temperature.roundToInt()
                binding.textUserLocation.text = getString(R.string.title_user_location, weatherResponse.location)
                binding.textUserTemperature.text = getString(R.string.title_user_temperature, tempUser.toString())

                val iconUrl = weatherResponse.icon.replace("http://", "https://")
                Glide.with(this)
                    .load(iconUrl)
                    .into(binding.imageTemperature)
            } else {
                binding.userInfo.visibility = View.GONE
            }
        }

        binding.rvRecommendations.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        // Fetch food recommendations after weather data
        recommendationViewModel.recommendations.observe(viewLifecycleOwner) { result ->
            setRecommendationsData(result)
            binding.progressBarRecommendations.visibility = View.GONE

        }

        return binding.root
    }

    private fun getListCategory(){
        val recyclerView: RecyclerView = binding.rvCategory
        recyclerView.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)

        val imageList = listOf(
            R.drawable.img_category_1,
            R.drawable.img_category_2,
            R.drawable.img_category_3,
            R.drawable.img_category_4,
            R.drawable.img_category_5,
            R.drawable.img_category_6,
            R.drawable.img_category_7,
        )
        val categoryList = listOf(
            "ayam",
            "ikan",
            "kambing",
            "sapi",
            "tempe",
            "telur",
            "udang"
        )

        val adapter = CategoryAdapter(imageList, categoryList) { selectedCategory ->
            categoryUser = selectedCategory
            // Lakukan request API berdasarkan kategori yang dipilih
            binding.progressBarRecommendations.visibility = View.VISIBLE
            fetchFoodRecommendations()
        }
        recyclerView.adapter = adapter
    }

    // Ambil lokasi terakhir yang diketahui
    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Jika izin belum diberikan, tidak bisa mengambil lokasi
            return
        }

        // Mendapatkan lokasi terakhir
        Log.d(TAG, "Attempting to get location")
        fusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task: Task<Location?> ->
            if (task.isSuccessful) {
                val location = task.result
                if (location != null) {
                    Log.d(
                        TAG,
                        "Location fetched: Lat: ${location.latitude}, Lon: ${location.longitude}"
                    )
                    val latitude = location.latitude
                    val longitude = location.longitude
                    // Panggil API cuaca dengan latitude dan longitude
                    fetchWeatherByCoordinates(latitude, longitude)
                } else {
                    // Handle jika lokasi null
                    Log.d(TAG, "Location is null")
                }
            } else {
                // Handle jika gagal mendapatkan lokasi
                Log.d(TAG, "Failed to get location: ${task.exception?.message}")
            }
        }
    }

    // Ambil data cuaca berdasarkan koordinat (latitude dan longitude)
    private fun fetchWeatherByCoordinates(latitude: Double, longitude: Double) {
        Log.d(TAG, "API request Weather......................")
        weatherViewModel.fetchWeatherByCoordinates(latitude, longitude)
        fetchFoodRecommendations()
    }

    private fun fetchFoodRecommendations() {
        Log.d(TAG, "API request Recommend......................")
        recommendationViewModel.fetchRecommendations(categoryUser, tempUser, timeUser)
    }

    private fun setRecommendationsData(listEventsItem: List<ListRecommendItem>) {
        val adapter = RecommendationAdapter()
        adapter.submitList(listEventsItem)
        binding.rvRecommendations.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // Mengunci orientasi ke potret
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onPause() {
        super.onPause()
        // Kembalikan orientasi ke pengaturan default
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "HomeFragment"
    }
}
