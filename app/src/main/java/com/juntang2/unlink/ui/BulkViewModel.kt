package com.juntang2.unlink.ui

import android.content.Context
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
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

@HiltViewModel
class BulkViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _bulkInput = MutableStateFlow("")
    val bulkInput: StateFlow<String> = _bulkInput

    private val _results = MutableStateFlow<List<BulkResult>>(emptyList())
    val results: StateFlow<List<BulkResult>> = _results

    private val _isCleaning = MutableStateFlow(false)
    val isCleaning: StateFlow<Boolean> = _isCleaning

    private val _exportStatus = MutableStateFlow<String?>(null)
    val exportStatus: StateFlow<String?> = _exportStatus

    fun setBulkInput(text: String) {
        _bulkInput.value = text
    }

    fun cleanBulk() {
        viewModelScope.launch {
            _isCleaning.value = true
            val text = _bulkInput.value
            val urls = URLParser.extractUrls(text)
            val settings = settingsRepository.getSettings().first()
            val customKeepRules = settingsRepository.getCustomKeepRules().first()
            val customRemoveRules = settingsRepository.getCustomRemoveRules().first()

            val cleanedResults = withContext(Dispatchers.Default) {
                urls.map { url ->
                    val cleaned = ParameterFilter.cleanUrl(
                        url = url,
                        aggressiveMode = settings.aggressiveMode,
                        keepAffiliate = settings.keepAffiliate,
                        customKeepRules = customKeepRules,
                        customRemoveRules = customRemoveRules
                    )
                    BulkResult(original = url, cleaned = cleaned)
                }
            }

            _results.value = cleanedResults
            _isCleaning.value = false

            cleanedResults.forEach { result ->
                if (result.cleaned.isNotEmpty()) {
                    val history = URLHistory(
                        originalUrl = result.original,
                        cleanedUrl = result.cleaned,
                        isFavorite = false
                    )
                    historyRepository.insertHistory(history)
                }
            }
        }
    }

    fun exportCsv(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, "cleaned_links.csv")
                val writer = FileWriter(file)
                writer.append("Original URL,Cleaned URL,Timestamp,Domain\n")
                _results.value.forEach {
                    val domain = URLParser.getDomain(it.cleaned)
                    writer.append("\"${it.original}\",\"${it.cleaned}\",${System.currentTimeMillis()},\"$domain\"\n")
                }
                writer.flush()
                writer.close()
                _exportStatus.value = "Exported to CSV successfully"
            } catch (e: Exception) {
                _exportStatus.value = "Export failed: Storage unavailable"
            }
        }
    }

    fun exportJson(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, "cleaned_links.json")
                val writer = FileWriter(file)
                writer.append("[\n")
                _results.value.forEachIndexed { index, it ->
                    val domain = URLParser.getDomain(it.cleaned)
                    writer.append("  {\n")
                    writer.append("    \"original\": \"${it.original}\",\n")
                    writer.append("    \"cleaned\": \"${it.cleaned}\",\n")
                    writer.append("    \"timestamp\": ${System.currentTimeMillis()},\n")
                    writer.append("    \"domain\": \"$domain\"\n")
                    writer.append("  }")
                    if (index < _results.value.size - 1) writer.append(",")
                    writer.append("\n")
                }
                writer.append("]")
                writer.flush()
                writer.close()
                _exportStatus.value = "Exported to JSON successfully"
            } catch (e: Exception) {
                _exportStatus.value = "Export failed: Storage unavailable"
            }
        }
    }

    fun clearStatus() {
        _exportStatus.value = null
    }

    fun clearResults() {
        _results.value = emptyList()
        _bulkInput.value = ""
    }
}

data class BulkResult(
    val original: String,
    val cleaned: String
)
