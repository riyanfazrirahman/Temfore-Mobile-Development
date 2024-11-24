package com.capstone.tempore.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.capstone.tempore.R
import com.capstone.tempore.data.WeatherRepository
import com.capstone.tempore.data.retrofit.ApiConfig
import com.capstone.tempore.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import kotlin.math.roundToInt


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: HomeViewModel


    // Permission request launcher untuk meminta izin lokasi
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLastLocation() // Ambil lokasi jika izin diberikan
            } else {
                // Handle ketika izin ditolak
                Log.d(TAG, "Permission Denied")
                Toast.makeText(requireContext(), "Izin lokasi diperlukan untuk mengambil cuaca!", Toast.LENGTH_SHORT).show()
            }
        }

    // Binding untuk akses elemen tampilan
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inisialisasi binding untuk fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

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

        // Inisialisasi WeatherRepository untuk mengambil data cuaca
        val weatherRepository = WeatherRepository(ApiConfig.getApiService())

        // Gunakan factory untuk membuat ViewModel
        val viewModelFactory = WeatherViewModelFactory(weatherRepository)
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]

        // Observasi data cuaca dari ViewModel
        viewModel.weatherData.observe(viewLifecycleOwner) { weatherResponse ->
            binding.progressBar.visibility = View.GONE

            if (weatherResponse != null) {
                // Update UI dengan data cuaca
                binding.textUserLocation.text = getString(R.string.title_user_location, weatherResponse.location)
                binding.textUserTemperature.text = getString(R.string.title_user_temperature, weatherResponse.temperature.roundToInt().toString())

                // Dapatkan ikon cuaca
                val iconUrl = if (weatherResponse.icon.startsWith("http://")) {
                    weatherResponse.icon.replace("http://", "https://")
                } else {
                    weatherResponse.icon
                }

                Glide.with(this)
                    .load(iconUrl)
                    .into(binding.imageTemperature)

                Log.d(TAG, "Loading image from URL: ${weatherResponse.icon}")

                // Observasi pesan waktu dari ViewModel
                viewModel.timeMessageData.observe(requireActivity()) { message ->
                    binding.textUserTime.text = getString(R.string.title_user_time, message)
                }

                // Observasi pesan waktu dari ViewModel
                viewModel.helloMessageData.observe(requireActivity()) { message ->
                    binding.textTime.text = getString(R.string.title_user_hello, message)
                }

            } else {
                // Jika data tidak tersedia, tampilkan pesan error
                binding.textUserLocation.text = getString(R.string.data_not_available)
                binding.textUserTemperature.text = getString(R.string.data_not_available)
            }
        }

//        // Observasi data waktu dari ViewModel
//        viewModel.timeData.observe(requireActivity()) { time ->
//            binding.timeTextView.text = time
//        }
//
//        // Observasi data tanggal dari ViewModel
//        viewModel.dateData.observe(requireActivity()) { date ->
//            binding.dateTextView.text = date
//        }

        binding.progressBar.visibility = View.VISIBLE

        val textView: TextView = binding.textHome
        viewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
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
        Log.d(TAG, "API request...........")
        viewModel.fetchWeatherByCoordinates(latitude, longitude)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "HomeFragment"
    }
}
