package com.capstone.temfore.ui.home

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.temfore.data.WeatherRepository
import com.capstone.temfore.data.response.WeatherResponse
import com.capstone.temfore.utils.TimeUtils
import kotlinx.coroutines.launch

class HomeViewModel(private val weatherRepository: WeatherRepository) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData: LiveData<WeatherResponse> get() = _weatherData

//    private val _timeData = MutableLiveData<String>()
//    val timeData: LiveData<String> get() = _timeData
//
//    private val _dateData = MutableLiveData<String>()
//    val dateData: LiveData<String> get() = _dateData

    private val _timeMessageData = MutableLiveData<String>()
    val timeMessageData: LiveData<String> get() = _timeMessageData

    private val _helloMessageData = MutableLiveData<String>()
    val helloMessageData: LiveData<String> get() = _helloMessageData

    private val timeUtils = TimeUtils()

    // Update time and date every second
    private val handler = Handler(Looper.getMainLooper())

    init {
//        startRealTimeClock()
        updateTimeDisplay()
    }

    fun fetchWeatherByCoordinates(latitude: Double, longitude:Double) {
        viewModelScope.launch {
            try {
                val response = weatherRepository.getWeatherByCoordinates(latitude, longitude)
                _weatherData.postValue(response)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

//    private fun startRealTimeClock() {
//        handler.post(object : Runnable {
//            override fun run() {
//                updateTimeDisplay()
//                handler.postDelayed(this, 1000)
//            }
//        })
//    }

    private fun updateTimeDisplay() {
//        _timeData.postValue(timeUtils.getCurrentTime())
//        _dateData.postValue(timeUtils.getCurrentDate())
        _timeMessageData.postValue(timeUtils.getTimeBasedMessage())
        _helloMessageData.postValue(timeUtils.getHelloBasedMessage())
    }
}