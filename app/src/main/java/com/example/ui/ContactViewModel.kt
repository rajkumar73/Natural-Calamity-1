package com.example.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.EmergencyContact
import com.example.data.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class ContactViewModel(
    application: Application,
    private val repository: ContactRepository
) : AndroidViewModel(application) {

     private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val sharedPrefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _googleSheetsUrl = MutableStateFlow(
        sharedPrefs.getString("google_sheets_url", "") ?: ""
    )
    val googleSheetsUrl: StateFlow<String> = _googleSheetsUrl.asStateFlow()

    private val _talukaName = MutableStateFlow(
        sharedPrefs.getString("taluka_name", "मंगळवेढा") ?: "मंगळवेढा"
    )
    val talukaName: StateFlow<String> = _talukaName.asStateFlow()

    private val _talukaNameEn = MutableStateFlow(
        sharedPrefs.getString("taluka_name_en", "Mangalwedha") ?: "Mangalwedha"
    )
    val talukaNameEn: StateFlow<String> = _talukaNameEn.asStateFlow()

    fun updateTalukaName(marathi: String, english: String) {
        val mr = marathi.trim().ifBlank { "मंगळवेढा" }
        val en = english.trim().ifBlank { "Mangalwedha" }
        _talukaName.value = mr
        _talukaNameEn.value = en
        sharedPrefs.edit()
            .putString("taluka_name", mr)
            .putString("taluka_name_en", en)
            .apply()
    }

    private val _disablePrepopulate = MutableStateFlow(
        sharedPrefs.getBoolean("disable_prepopulate", false)
    )
    val disablePrepopulate: StateFlow<Boolean> = _disablePrepopulate.asStateFlow()

    fun setDisablePrepopulate(disabled: Boolean) {
        _disablePrepopulate.value = disabled
        sharedPrefs.edit().putBoolean("disable_prepopulate", disabled).apply()
    }

    fun clearAllContacts() {
        viewModelScope.launch {
            try {
                setDisablePrepopulate(true)
                repository.clearAllContacts()
                _uiMessage.emit("सर्व जुने रेकॉर्ड्स डिलीट झाले! आता वरून गुगल शीट सिंक केल्यावर फक्त तुमचा डेटा दिसेल.")
            } catch (e: Exception) {
                _uiMessage.emit("डेटा पुसताना चूक झाली: ${e.localizedMessage}")
            }
        }
    }

    fun convertToCsvUrl(inputUrl: String): String {
        val trimmed = inputUrl.trim()
        if (trimmed.isBlank()) return ""
        if (trimmed.contains("/pub?") && trimmed.contains("output=csv")) {
            return trimmed
        }
        if (trimmed.contains("/export?") && trimmed.contains("format=csv")) {
            return trimmed
        }
        // Match standard google spreadsheet sharing/edit url
        val regex = "https://docs\\.google\\.com/spreadsheets/d/([^/]+)".toRegex()
        val matchResult = regex.find(trimmed)
        if (matchResult != null) {
            val spreadsheetId = matchResult.groupValues[1]
            return "https://docs.google.com/spreadsheets/d/$spreadsheetId/export?format=csv"
        }
        return trimmed
    }

    fun updateGoogleSheetsUrl(url: String) {
        val converted = convertToCsvUrl(url)
        _googleSheetsUrl.value = converted
        sharedPrefs.edit().putString("google_sheets_url", converted).apply()
    }

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedVillage = MutableStateFlow(sharedPrefs.getString("selected_village", "ALL") ?: "ALL")
    val selectedVillage: StateFlow<String> = _selectedVillage.asStateFlow()

    // Scope Selection filter: "ALL", "TALUKA", "VILLAGE"
    private val _selectedScope = MutableStateFlow("ALL")
    val selectedScope: StateFlow<String> = _selectedScope.asStateFlow()

    // Automatic Offline & Online Connectivity Engine
    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Sync state for background sync indicator
    private val _syncStatus = MutableStateFlow("IDLE") // IDLE, SYNCING, SUCCEEDED, FAILED
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _lastSyncedTime = MutableStateFlow("नुकतेच ऑफलाइन (Cached)")
    val lastSyncedTime: StateFlow<String> = _lastSyncedTime.asStateFlow()

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    // Dynamically query unique villages from existing items in Room database
    val availableVillages: StateFlow<List<String>> = repository.allContacts.map { contacts ->
        val villages = contacts.map { it.villageOrAreaMr.trim() }
            .filter { it.isNotBlank() && !it.contains("सर्व") && !it.contains("All") && !it.contains("तालुका") }
            .distinct()
            .sorted()
        listOf("ALL") + villages
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("ALL")
    )

    private fun isTalukaLevel(contact: EmergencyContact): Boolean {
        val village = contact.villageOrAreaMr.trim()
        val villageEn = contact.villageOrAreaEn.trim()
        return village.contains("सर्व") || village.contains("तालुका") || 
               villageEn.contains("All") || villageEn.contains("Taluka") || 
               village.isEmpty() || village == "Tehsil Office (HQ)" || village == "सर्व तालुका विभाग"
    }

    // High performance reactive in-memory search and filter on database outputs
    val filteredContacts: StateFlow<List<EmergencyContact>> = combine(
        repository.allContacts,
        _searchQuery,
        _selectedCategory,
        _selectedVillage,
        _selectedScope
    ) { contacts, query, category, village, scope ->
        var list = contacts

        // Filter by Scope (Taluka Level vs Village Level)
        if (scope == "TALUKA") {
            list = list.filter { isTalukaLevel(it) }
        } else if (scope == "VILLAGE") {
            list = list.filter { !isTalukaLevel(it) }
        }

        // Filter by village
        if (village != "ALL") {
            list = list.filter {
                it.villageOrAreaMr.equals(village, ignoreCase = true) ||
                it.villageOrAreaEn.equals(village, ignoreCase = true)
            }
        }

        // Filter by category
        if (category != "ALL") {
            list = list.filter {
                val mappedGroup = when (it.category.uppercase()) {
                    "POLICE", "MEDICAL", "FIRE", "RESCUE" -> "EMERG_SERVICES"
                    "ADMIN" -> "ADMIN_OFFICERS"
                    else -> "LOCAL_REPS"
                }
                mappedGroup == category
            }
        }

        // Search in Marathi & English names, designations, and phones
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.nameMr.lowercase().contains(q) ||
                it.nameEn.lowercase().contains(q) ||
                it.designationMr.lowercase().contains(q) ||
                it.designationEn.lowercase().contains(q) ||
                it.villageOrAreaMr.lowercase().contains(q) ||
                it.villageOrAreaEn.lowercase().contains(q) ||
                it.phone.contains(q) ||
                it.phoneAlt.contains(q)
            }
        }
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            try {
                val disablePopulate = sharedPrefs.getBoolean("disable_prepopulate", false)
                if (!disablePopulate) {
                    repository.prePopulateIfEmpty()
                }
            } catch (e: Exception) {
                _uiMessage.emit("माहिती जोडताना चूक झाली: ${e.localizedMessage}")
            }
        }
        monitorNetworkAndAutoSync()
    }

    private fun monitorNetworkAndAutoSync() {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager == null) {
            _isOnline.value = true
            return
        }

        // Initial setup
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val initiallyOnline = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        _isOnline.value = initiallyOnline
        if (initiallyOnline) {
            _lastSyncedTime.value = "आज (सिंक्रोनाइझ झाले)"
        }

        try {
            val builder = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            
            connectivityManager.registerNetworkCallback(builder, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnline.value = true
                    triggerBackgroundSync()
                }

                override fun onLost(network: Network) {
                    _isOnline.value = false
                }
            })
        } catch (e: Exception) {
            _isOnline.value = true
        }
    }

    fun triggerBackgroundSync() {
        if (_syncStatus.value == "SYNCING") return
        var url = _googleSheetsUrl.value
        if (url.isBlank()) {
            viewModelScope.launch {
                _syncStatus.value = "FAILED"
                _uiMessage.emit("गुगल शीट सिंक लिंक उपलब्ध नाही. कृपया सेटिंग्जमध्ये लिंक प्रविष्ट करा.")
            }
            return
        }

        // Auto-convert standard Google sheet browser links prior to syncing
        url = convertToCsvUrl(url)

        viewModelScope.launch(Dispatchers.IO) {
            _syncStatus.value = "SYNCING"
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw Exception("HTTP Error: ${response.code}")
                }
                var csvData = response.body?.string() ?: ""
                
                // Clean the CSV from UTF-8 BOM characters that Google Sheets exports
                csvData = csvData.replace("\uFEFF", "").trim()
                
                if (csvData.isBlank()) {
                    throw Exception("गुगल शीट रिकामी आहे किंवा वाचता येत नाही.")
                }

                val contacts = parseContactsCsv(csvData)
                if (contacts.isEmpty()) {
                    throw Exception("शीटमध्ये योग्य संपर्क माहिती सापडली नाही. कॉलमची नावे तपासा. (कॉलम: nameMr आवश्यक आहे)")
                }

                repository.importContactsFromSheet(contacts)
                setDisablePrepopulate(true)

                val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                val timeString = sdf.format(java.util.Date())
                _lastSyncedTime.value = "आज $timeString"
                _syncStatus.value = "SUCCEEDED"
                _uiMessage.emit("गूगल शीटमधून ${contacts.size} संपर्क यशस्वीरित्या सिंक्रोनाइझ झाले!")
            } catch (e: Exception) {
                _syncStatus.value = "FAILED"
                _uiMessage.emit("सिंक्रोनाइझेशन अपयशी: ${e.localizedMessage}")
            }
        }
    }

    fun importLocalContactsCsv(csvText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _syncStatus.value = "SYNCING"
            try {
                val cleanedCsvText = csvText.replace("\uFEFF", "").trim()
                if (cleanedCsvText.isBlank()) {
                    throw Exception("निवडलेली फाईल रिकामी आहे.")
                }
                val contacts = parseContactsCsv(cleanedCsvText)
                if (contacts.isEmpty()) {
                    throw Exception("योग्य स्वरूप मिळाले नाही. फाईल मधील हेडिंग तपासा.")
                }
                repository.importContactsFromSheet(contacts)
                setDisablePrepopulate(true)
                _syncStatus.value = "SUCCEEDED"
                _uiMessage.emit("स्थानिक फाईलवरून ${contacts.size} संपर्क यशस्वीरित्या आयात केले!")
            } catch (e: Exception) {
                _syncStatus.value = "FAILED"
                _uiMessage.emit("आयात अपयशी: ${e.localizedMessage}")
            }
        }
    }

    private fun parseContactsCsv(csvText: String): List<EmergencyContact> {
        val list = mutableListOf<EmergencyContact>()
        val lines = csvText.lines()
        if (lines.isEmpty()) return list

        val headerLine = lines.firstOrNull() ?: return list
        val headers = parseCsvLine(headerLine).map { it.trim().lowercase() }
        
        // Use flexible fallbacks for headers to match even if the user slightly modifies column names in Marathi/English
        val nameMrIdx = headers.indexOfFirst { it.contains("namemr") || it.contains("नाव") || it.contains("name mr") }
        val nameEnIdx = headers.indexOfFirst { it.contains("nameen") || it.contains("नाव इंग्रजी") || it.contains("name en") }
        val phoneIdx = headers.indexOfFirst { it.contains("phone") || it.contains("फोन") || it.contains("मोबाइल") || it.contains("mobile") || it.contains("संपर्क") }
        val phoneAltIdx = headers.indexOfFirst { it.contains("phonealt") || it.contains("फोन २") || it.contains("phone alt") || it.contains("दुसरा फोन") }
        val categoryIdx = headers.indexOfFirst { it.contains("category") || it.contains("गट") || it.contains("प्रवर्ग") || it.contains("वर्ग") }
        val designationMrIdx = headers.indexOfFirst { it.contains("designationmr") || it.contains("पद") || it.contains("designation mr") }
        val designationEnIdx = headers.indexOfFirst { it.contains("designationen") || it.contains("पद इंग्रजी") || it.contains("designation en") }
        val villageOrAreaMrIdx = headers.indexOfFirst { it.contains("villageorareamr") || it.contains("गाव") || it.contains("village") || it.contains("पत्ता") }
        val villageOrAreaEnIdx = headers.indexOfFirst { it.contains("villageorareaen") || it.contains("गाव इंग्रजी") || it.contains("village en") }
        val isDefaultIdx = headers.indexOfFirst { it.contains("isdefault") || it.contains("शासकीय") || it.contains("default") }
        val notesIdx = headers.indexOfFirst { it.contains("notes") || it.contains("नोंद") || it.contains("माहिती") || it.contains("notes mr") }

        // Must have at least nameMr (Column A)
        val hasHeaders = nameMrIdx != -1

        for (i in 1 until lines.size) {
            val line = lines[i]
            if (line.isBlank()) continue
            val fields = parseCsvLine(line)
            if (fields.isEmpty()) continue

            val nameMr = if (hasHeaders && nameMrIdx < fields.size) fields[nameMrIdx].trim() else fields.getOrNull(0)?.trim() ?: ""
            val nameEn = if (hasHeaders && nameEnIdx < fields.size && nameEnIdx != -1) fields[nameEnIdx].trim() else fields.getOrNull(1)?.trim() ?: ""
            val phone = if (hasHeaders && phoneIdx < fields.size && phoneIdx != -1) fields[phoneIdx].trim() else if (hasHeaders) "" else fields.getOrNull(2)?.trim() ?: ""
            
            if (nameMr.isBlank()) continue

            val phoneAlt = if (hasHeaders && phoneAltIdx < fields.size && phoneAltIdx != -1) fields[phoneAltIdx].trim() else fields.getOrNull(3)?.trim() ?: ""
            val category = if (hasHeaders && categoryIdx < fields.size && categoryIdx != -1) fields[categoryIdx].trim().uppercase() else fields.getOrNull(4)?.trim()?.uppercase() ?: "RESCUE"
            val designationMr = if (hasHeaders && designationMrIdx < fields.size && designationMrIdx != -1) fields[designationMrIdx].trim() else fields.getOrNull(5)?.trim() ?: ""
            val designationEn = if (hasHeaders && designationEnIdx < fields.size && designationEnIdx != -1) fields[designationEnIdx].trim() else fields.getOrNull(6)?.trim() ?: ""
            val villageOrAreaMr = if (hasHeaders && villageOrAreaMrIdx < fields.size && villageOrAreaMrIdx != -1) fields[villageOrAreaMrIdx].trim() else fields.getOrNull(7)?.trim() ?: ""
            val villageOrAreaEn = if (hasHeaders && villageOrAreaEnIdx < fields.size && villageOrAreaEnIdx != -1) fields[villageOrAreaEnIdx].trim() else fields.getOrNull(8)?.trim() ?: ""
            val isDefaultStr = if (hasHeaders && isDefaultIdx < fields.size && isDefaultIdx != -1) fields[isDefaultIdx].trim() else fields.getOrNull(9)?.trim() ?: ""
            val isDefault = isDefaultStr.equals("true", ignoreCase = true) || isDefaultStr == "1" || isDefaultStr.contains("होय", ignoreCase = true)
            val notes = if (hasHeaders && notesIdx < fields.size && notesIdx != -1) fields[notesIdx].trim() else fields.getOrNull(10)?.trim() ?: ""

            list.add(
                EmergencyContact(
                    nameMr = nameMr,
                    nameEn = nameEn,
                    phone = phone,
                    phoneAlt = phoneAlt,
                    category = category,
                    designationMr = designationMr,
                    designationEn = designationEn,
                    villageOrAreaMr = villageOrAreaMr,
                    villageOrAreaEn = villageOrAreaEn,
                    isDefault = isDefault,
                    notes = notes
                )
            )
        }
        return list
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                inQuotes = !inQuotes
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().replace("\"", ""))
                current = StringBuilder()
            } else {
                current.append(c)
            }
            i++
        }
        result.add(current.toString().replace("\"", ""))
        return result
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectVillage(village: String) {
        _selectedVillage.value = village
        sharedPrefs.edit().putString("selected_village", village).apply()
    }

    fun selectScope(scope: String) {
        _selectedScope.value = scope
    }

    fun saveContact(
        id: Int = 0,
        nameMr: String,
        nameEn: String,
        phone: String,
        phoneAlt: String = "",
        category: String,
        designationMr: String,
        designationEn: String,
        villageOrAreaMr: String,
        villageOrAreaEn: String,
        isDefault: Boolean = false,
        notes: String = ""
    ) {
        viewModelScope.launch {
            if (nameMr.isBlank()) {
                _uiMessage.emit("कृपया निदान नाव प्रविष्ट करा")
                return@launch
            }

            val contact = EmergencyContact(
                id = id,
                nameMr = nameMr.trim(),
                nameEn = nameEn.trim().ifBlank { nameMr.trim() },
                phone = phone.trim(),
                phoneAlt = phoneAlt.trim(),
                category = category,
                designationMr = designationMr.trim(),
                designationEn = designationEn.trim(),
                villageOrAreaMr = villageOrAreaMr.trim(),
                villageOrAreaEn = villageOrAreaEn.trim(),
                isDefault = isDefault,
                notes = notes.trim()
            )

            try {
                if (id == 0) {
                    repository.insert(contact)
                    _uiMessage.emit("नवीन संपर्क यशस्वीरित्या जोडला गेला!")
                } else {
                    repository.update(contact)
                    _uiMessage.emit("संपर्क यशस्वीरित्या अद्ययावत केला!")
                }
            } catch (e: Exception) {
                _uiMessage.emit("साठवताना चूक झाली: ${e.localizedMessage}")
            }
        }
    }

    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch {
            try {
                repository.delete(contact)
                _uiMessage.emit("संपर्क काढला गेला!")
            } catch (e: Exception) {
                _uiMessage.emit("काढताना चूक झाली: ${e.localizedMessage}")
            }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            try {
                setDisablePrepopulate(false)
                repository.resetToDefault()
                _uiMessage.emit("मूळ संपर्क यादी यशस्वीरित्या पुनर्संचयित केली!")
            } catch (e: Exception) {
                _uiMessage.emit("पुनर्संचयित करण्यात अडचण: ${e.localizedMessage}")
            }
        }
    }
}

class ContactViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            val repository = ContactRepository(database.contactDao())
            @Suppress("UNCHECKED_CAST")
            return ContactViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
