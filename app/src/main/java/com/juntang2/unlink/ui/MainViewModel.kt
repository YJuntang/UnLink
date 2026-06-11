package com.juntang2.unlink.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juntang2.unlink.core.model.URLHistory
import com.juntang2.unlink.core.repository.HistoryRepository
import com.juntang2.unlink.core.repository.SettingsRepository
import com.juntang2.unlink.common.util.URLParser
import com.juntang2.unlink.common.util.ParameterFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _inputUrl = MutableStateFlow("")
    val inputUrl: StateFlow<String> = _inputUrl

    private val _cleanedUrl = MutableStateFlow("")
    val cleanedUrl: StateFlow<String> = _cleanedUrl

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isResolving = MutableStateFlow(false)
    val isResolving: StateFlow<Boolean> = _isResolving

    fun setInputUrl(url: String) {
        _inputUrl.value = url
        _error.value = null
        if (url.trim().isEmpty()) {
            _cleanedUrl.value = ""
        }
    }

    fun cleanUrl() {
        val input = _inputUrl.value
        if (!URLParser.isValidUrl(input)) {
            _error.value = "Invalid URL format"
            return
        }

        viewModelScope.launch {
            _isResolving.value = true
            val settings = settingsRepository.getSettings().first()
            val customKeepRules = settingsRepository.getCustomKeepRules().first()
            val customRemoveRules = settingsRepository.getCustomRemoveRules().first()
            var urlToClean = URLParser.normalizeUrl(input)

            if (settings.expandShortUrl) {
                try {
                    urlToClean = expandUrlWithOkHttp(urlToClean)
                } catch (e: Exception) {
                    _error.value = "Timeout / resolution failed"
                }
            }

            val cleaned = ParameterFilter.cleanUrl(
                url = urlToClean,
                aggressiveMode = settings.aggressiveMode,
                keepAffiliate = settings.keepAffiliate,
                customKeepRules = customKeepRules,
                customRemoveRules = customRemoveRules
            )

            _cleanedUrl.value = cleaned
            _isResolving.value = false

            if (cleaned.isNotEmpty()) {
                val history = URLHistory(
                    originalUrl = input,
                    cleanedUrl = cleaned,
                    isFavorite = false
                )
                historyRepository.insertHistory(history)
            }
        }
    }

    private suspend fun expandUrlWithOkHttp(url: String): String = withContext(Dispatchers.IO) {
        var currentUrl = url
        val visited = mutableSetOf<String>()
        var redirects = 0
        val maxRedirects = 5

        while (redirects < maxRedirects) {
            visited.add(currentUrl)
            val request = Request.Builder().url(currentUrl).head().build()
            val client = okHttpClient.newBuilder().followRedirects(false).build()
            val response = client.newCall(request).execute()
            
            if (response.code in 300..399) {
                val location = response.header("Location")
                if (location != null) {
                    val nextUrl = URLParser.normalizeUrl(location)
                    if (visited.contains(nextUrl)) {
                        response.close()
                        break
                    }
                    currentUrl = nextUrl
                    redirects++
                    response.close()
                } else {
                    response.close()
                    break
                }
            } else {
                response.close()
                break
            }
        }
        currentUrl
    }
}
