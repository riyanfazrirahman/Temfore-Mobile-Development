package com.capstone.temfore.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.temfore.utils.TimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val timeUtils: TimeUtils = TimeUtils()

    private val _timeData = MutableLiveData<String>()
    val timeData: LiveData<String> get() = _timeData

    private val _timeHoursData = MutableLiveData<String>()
    val timeHoursData: LiveData<String> get() = _timeHoursData

    private val _dateData = MutableLiveData<String>()
    val dateData: LiveData<String> get() = _dateData

    private val _timeMessageData = MutableLiveData<String>()
    val timeMessageData: LiveData<String> get() = _timeMessageData

    private val _helloMessageData = MutableLiveData<String>()
    val helloMessageData: LiveData<String> get() = _helloMessageData

    // Use viewModelScope to update time every second
    private val timeUpdateJob = viewModelScope.launch {
        while (isActive) {
            updateTimeDisplay()
            delay(1000) // Delay for 1 second
        }
    }

    init {
        updateTimeDisplay()
    }

    private fun updateTimeDisplay() {
        _timeHoursData.value = timeUtils.getCurrentTimeHours()
        _timeData.value = timeUtils.getCurrentTime()
        _dateData.value = timeUtils.getCurrentDate()
        _timeMessageData.value = timeUtils.getTimeBasedMessage()
        _helloMessageData.value = timeUtils.getHelloBasedMessage()
    }

    override fun onCleared() {
        super.onCleared()
        timeUpdateJob.cancel()  // Cancel the time update job when ViewModel is cleared
    }
}
