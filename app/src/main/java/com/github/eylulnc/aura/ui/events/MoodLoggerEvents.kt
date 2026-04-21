package com.github.eylulnc.aura.ui.events

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object MoodLoggerEvents {
    private val _openRequest = MutableStateFlow(0L)
    val openRequest: StateFlow<Long> = _openRequest.asStateFlow()

    fun requestOpen() {
        _openRequest.value = System.currentTimeMillis()
    }
}
