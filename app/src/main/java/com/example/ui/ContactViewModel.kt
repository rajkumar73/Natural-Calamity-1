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
import com.example.data.GeminiClient
import com.example.data.SearchSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

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

    private val _twoWaySyncUrl = MutableStateFlow(
        sharedPrefs.getString("two_way_sync_url", "") ?: ""
    )
    val twoWaySyncUrl: StateFlow<String> = _twoWaySyncUrl.asStateFlow()

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

    private val _appNameMr = MutableStateFlow(
        sharedPrefs.getString("app_name_mr", "आपत्ती व्यवस्थापन संपर्क") ?: "आपत्ती व्यवस्थापन संपर्क"
    )
    val appNameMr: StateFlow<String> = _appNameMr.asStateFlow()

    private val _appNameEn = MutableStateFlow(
        sharedPrefs.getString("app_name_en", "Disaster Response Contacts") ?: "Disaster Response Contacts"
    )
    val appNameEn: StateFlow<String> = _appNameEn.asStateFlow()

    fun updateAppName(marathi: String, english: String) {
        val mr = marathi.trim().ifBlank { "आपत्ती व्यवस्थापन संपर्क" }
        val en = english.trim().ifBlank { "Disaster Response Contacts" }
        _appNameMr.value = mr
        _appNameEn.value = en
        sharedPrefs.edit()
            .putString("app_name_mr", mr)
            .putString("app_name_en", en)
            .apply()
    }

    private val _liveAlertMr = MutableStateFlow(
        sharedPrefs.getString("live_alert_mr", "मुसळधार पावसाचा इशारा! नदीकाठच्या नागरिकांनी सतर्कता बाळगावी व पूर आल्यास सुरक्षित स्थळी स्थलांतर करावे.") ?: "मुसळधार पावसाचा इशारा! नदीकाठच्या नागरिकांनी सतर्कता बाळगावी व पूर आल्यास सुरक्षित स्थळी स्थलांतर करावे."
    )
    val liveAlertMr: StateFlow<String> = _liveAlertMr.asStateFlow()

    private val _liveAlertEn = MutableStateFlow(
        sharedPrefs.getString("live_alert_en", "Heavy rainfall warning! Riverbank residents should stay highly alert and move to safer places in case of flooding.") ?: "Heavy rainfall warning! Riverbank residents should stay highly alert and move to safer places in case of flooding."
    )
    val liveAlertEn: StateFlow<String> = _liveAlertEn.asStateFlow()

    private val _isLiveAlertDismissed = MutableStateFlow(
        sharedPrefs.getBoolean("live_alert_dismissed", false)
    )
    val isLiveAlertDismissed: StateFlow<Boolean> = _isLiveAlertDismissed.asStateFlow()

    fun updateLiveAlert(marathi: String, english: String) {
        val mr = marathi.trim()
        val en = english.trim()
        _liveAlertMr.value = mr
        _liveAlertEn.value = en
        _isLiveAlertDismissed.value = false
        sharedPrefs.edit()
            .putString("live_alert_mr", mr)
            .putString("live_alert_en", en)
            .putBoolean("live_alert_dismissed", false)
            .apply()
    }

    fun dismissLiveAlert() {
        _isLiveAlertDismissed.value = true
        sharedPrefs.edit()
            .putBoolean("live_alert_dismissed", true)
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

    private val _isChatbotEnabled = MutableStateFlow(
        sharedPrefs.getBoolean("is_chatbot_enabled", true)
    )
    val isChatbotEnabled: StateFlow<Boolean> = _isChatbotEnabled.asStateFlow()

    fun setChatbotEnabled(enabled: Boolean) {
        _isChatbotEnabled.value = enabled
        sharedPrefs.edit().putBoolean("is_chatbot_enabled", enabled).apply()
    }

    private val _isLiveAlertEnabled = MutableStateFlow(
        sharedPrefs.getBoolean("is_live_alert_enabled", true)
    )
    val isLiveAlertEnabled: StateFlow<Boolean> = _isLiveAlertEnabled.asStateFlow()

    fun setLiveAlertEnabled(enabled: Boolean) {
        _isLiveAlertEnabled.value = enabled
        sharedPrefs.edit().putBoolean("is_live_alert_enabled", enabled).apply()
    }

    private val _selectedThemeCode = MutableStateFlow(
        sharedPrefs.getString("selected_theme_code", "DEFAULT") ?: "DEFAULT"
    )
    val selectedThemeCode: StateFlow<String> = _selectedThemeCode.asStateFlow()

    fun setSelectedThemeCode(code: String) {
        _selectedThemeCode.value = code
        sharedPrefs.edit().putString("selected_theme_code", code).apply()
    }

    private val _weatherTemp = MutableStateFlow(
        sharedPrefs.getString("weather_temp", "29°C") ?: "29°C"
    )
    val weatherTemp: StateFlow<String> = _weatherTemp.asStateFlow()

    private val _weatherStatusMr = MutableStateFlow(
        sharedPrefs.getString("weather_status_mr", "सतत पाऊस (Heavy Rain)") ?: "सतत पाऊस (Heavy Rain)"
    )
    val weatherStatusMr: StateFlow<String> = _weatherStatusMr.asStateFlow()

    private val _weatherStatusEn = MutableStateFlow(
        sharedPrefs.getString("weather_status_en", "Continuous Heavy Rain") ?: "Continuous Heavy Rain"
    )
    val weatherStatusEn: StateFlow<String> = _weatherStatusEn.asStateFlow()

    private val _floodLevelMr = MutableStateFlow(
        sharedPrefs.getString("flood_level_mr", "धोक्याच्या पातळीखाली (Normal)") ?: "धोक्याच्या पातळीखाली (Normal)"
    )
    val floodLevelMr: StateFlow<String> = _floodLevelMr.asStateFlow()

    private val _floodLevelEn = MutableStateFlow(
        sharedPrefs.getString("flood_level_en", "Below Danger Level (Safe)") ?: "Below Danger Level (Safe)"
    )
    val floodLevelEn: StateFlow<String> = _floodLevelEn.asStateFlow()

    private val _floodProgress = MutableStateFlow(
        sharedPrefs.getFloat("flood_progress", 0.45f)
    )
    val floodProgress: StateFlow<Float> = _floodProgress.asStateFlow()

    fun updateWeatherData(
        temp: String,
        statusMr: String,
        statusEn: String,
        floodMr: String,
        floodEn: String,
        progress: Float
    ) {
        _weatherTemp.value = temp
        _weatherStatusMr.value = statusMr
        _weatherStatusEn.value = statusEn
        _floodLevelMr.value = floodMr
        _floodLevelEn.value = floodEn
        _floodProgress.value = progress

        sharedPrefs.edit().apply {
            putString("weather_temp", temp)
            putString("weather_status_mr", statusMr)
            putString("weather_status_en", statusEn)
            putString("flood_level_mr", floodMr)
            putString("flood_level_en", floodEn)
            putFloat("flood_progress", progress)
            apply()
        }
    }

    private val _favoriteContactIds = MutableStateFlow<Set<Int>>(
        sharedPrefs.getStringSet("favorite_ids", emptySet())
            ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    )
    val favoriteContactIds: StateFlow<Set<Int>> = _favoriteContactIds.asStateFlow()

    fun toggleFavorite(contactId: Int) {
        val current = _favoriteContactIds.value.toMutableSet()
        if (current.contains(contactId)) {
            current.remove(contactId)
        } else {
            current.add(contactId)
        }
        _favoriteContactIds.value = current
        sharedPrefs.edit()
            .putStringSet("favorite_ids", current.map { it.toString() }.toSet())
            .apply()
    }

    // --- GROUP CHAT / DISCUSSION BOARD STATE & FUNCTIONS ---
    private val _userProfileName = MutableStateFlow(sharedPrefs.getString("user_profile_name", "") ?: "")
    val userProfileName: StateFlow<String> = _userProfileName.asStateFlow()

    private val _userProfileRole = MutableStateFlow(sharedPrefs.getString("user_profile_role", "") ?: "")
    val userProfileRole: StateFlow<String> = _userProfileRole.asStateFlow()

    private val _groupMessages = MutableStateFlow<List<GroupChatMessage>>(emptyList())
    val groupMessages: StateFlow<List<GroupChatMessage>> = _groupMessages.asStateFlow()

    init {
        loadGroupMessages()
    }

    fun saveUserProfile(name: String, role: String) {
        _userProfileName.value = name
        _userProfileRole.value = role
        sharedPrefs.edit()
            .putString("user_profile_name", name)
            .putString("user_profile_role", role)
            .apply()
    }

    private fun loadGroupMessages() {
        val serialized = sharedPrefs.getString("group_messages_json", "") ?: ""
        if (serialized.isBlank()) {
            val defaultList = listOf(
                GroupChatMessage(
                    id = "init_1",
                    senderName = "मंगळवेढा आपत्कालीन नियंत्रण कक्ष",
                    senderRole = "प्रशासन (Admin)",
                    message = "सर्व ग्रामसेवक, पोलीस पाटील आणि नागरिकांना विनंती आहे की, पुराच्या पाण्यावर आणि धरणाच्या विसर्गावर बारीक लक्ष ठेवावे.",
                    timestamp = "09:15 AM",
                    isSelf = false
                ),
                GroupChatMessage(
                    id = "init_2",
                    senderName = "डॉ. सतीश शिंदे",
                    senderRole = "वैद्यकीय अधिकारी (Medical Officer)",
                    message = "सिद्धापूर आणि नदीकाठच्या सर्व आरोग्य उपकेंद्रांवर औषधसाठा आणि सर्पदंशावरील लस (Anti-Snake Venom) उपलब्ध आहे.",
                    timestamp = "09:42 AM",
                    isSelf = false
                ),
                GroupChatMessage(
                    id = "init_3",
                    senderName = "विजय साळुंखे",
                    senderRole = "सरपंच (Sarpanch), सिद्धापूर",
                    message = "सिद्धापूर गावातील नदीकाठच्या ७ कुटुंबांना सुरक्षित स्थळी स्थलांतरित करण्यात आले आहे. प्राथमिक शाळेत राहण्याची व्यवस्था केली आहे.",
                    timestamp = "10:05 AM",
                    isSelf = false
                ),
                GroupChatMessage(
                    id = "init_4",
                    senderName = "अमोल कुलकर्णी",
                    senderRole = "आपत्कालीन स्वयंसेवक (Rescue)",
                    message = "आमची ५ स्वयंसेवकांची टीम लाईफ जॅकेट आणि दोरीसह सज्ज आहे. कोणत्याही मदतीसाठी आम्हाला तात्काळ संपर्क करा.",
                    timestamp = "10:20 AM",
                    isSelf = false
                )
            )
            _groupMessages.value = defaultList
            saveGroupMessagesToPrefs(defaultList)
        } else {
            try {
                val messages = mutableListOf<GroupChatMessage>()
                val parts = serialized.split("||||")
                for (part in parts) {
                    if (part.isBlank()) continue
                    val fields = part.split("####")
                    if (fields.size >= 6) {
                        messages.add(
                            GroupChatMessage(
                                id = fields[0],
                                senderName = fields[1],
                                senderRole = fields[2],
                                message = fields[3],
                                timestamp = fields[4],
                                isSelf = fields[5] == "true"
                            )
                        )
                    }
                }
                _groupMessages.value = messages
            } catch (e: Exception) {
                _groupMessages.value = emptyList()
            }
        }
    }

    private fun saveGroupMessagesToPrefs(list: List<GroupChatMessage>) {
        val builder = StringBuilder()
        list.forEachIndexed { idx, msg ->
            builder.append(msg.id).append("####")
            builder.append(msg.senderName).append("####")
            builder.append(msg.senderRole).append("####")
            builder.append(msg.message).append("####")
            builder.append(msg.timestamp).append("####")
            builder.append(if (msg.isSelf) "true" else "false")
            if (idx < list.size - 1) {
                builder.append("||||")
            }
        }
        sharedPrefs.edit().putString("group_messages_json", builder.toString()).apply()
    }

    fun sendGroupMessage(messageText: String) {
        if (messageText.isBlank()) return
        val currentName = _userProfileName.value.ifBlank { "नागरिक (Anonymous)" }
        val currentRole = _userProfileRole.value.ifBlank { "नागरिक (Citizen)" }
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        val currentTimeString = sdf.format(java.util.Date())

        val userMsg = GroupChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = currentName,
            senderRole = currentRole,
            message = messageText,
            timestamp = currentTimeString,
            isSelf = true
        )

        val updated = _groupMessages.value.toMutableList()
        updated.add(userMsg)
        _groupMessages.value = updated
        saveGroupMessagesToPrefs(updated)

        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            val systemInstruction = """
                You are a simulated participant in the Taluka Emergency Discussion Group ("आपत्कालीन चर्चा गट"). 
                Your name is "मंगळवेढा आपत्कालीन समन्वयक" (Taluka Coordinator) or another helpful responder from the Taluka level like a rescue officer or senior volunteer.
                You are replying to a chat message posted by a user in the group. 
                The user name is "$currentName" and their role is "$currentRole".
                They just posted: "$messageText".
                
                Provide a highly realistic, supportive, and action-oriented reply in Marathi (or English if the user typed in English).
                Keep the response brief and extremely professional (maximum 1-2 sentences), behaving as a real person coordinating emergency help in Mangalwedha.
                For example: "धन्यवाद $currentName. आम्ही या समस्येची नोंद नियंत्रण कक्षात घेतली आहे आणि मदत पथक पाठवत आहोत." or another relevant response.
                Avoid any promotional language, just write the message text directly.
            """.trimIndent()

            try {
                val response = GeminiClient.getChatResponse(messageText, systemInstruction)
                val replyText = response.text
                
                val responderMsg = GroupChatMessage(
                    id = "msg_sim_${System.currentTimeMillis()}",
                    senderName = "मंगळवेढा समन्वयक (AI)",
                    senderRole = "समन्वयक (Emergency Coordinator)",
                    message = replyText,
                    timestamp = sdf.format(java.util.Date()),
                    isSelf = false
                )
                
                val finalUpdated = _groupMessages.value.toMutableList()
                finalUpdated.add(responderMsg)
                _groupMessages.value = finalUpdated
                saveGroupMessagesToPrefs(finalUpdated)
            } catch (e: Exception) {
                val staticReplies = listOf(
                    "आपली माहिती नियंत्रण कक्षाकडे पाठवण्यात आली आहे. धन्यवाद!",
                    "मदत गट अलर्टवर आहे. काही तातडीची मदत लागल्यास कळवा.",
                    "सर्व गावांचे समन्वयक या ग्रुपवर सक्रिय आहेत. आम्ही परिस्थितीवर लक्ष ठेवून आहोत."
                )
                val replyText = staticReplies.random()
                val responderMsg = GroupChatMessage(
                    id = "msg_sim_static_${System.currentTimeMillis()}",
                    senderName = "आपत्कालीन स्वयंसेवक",
                    senderRole = "समन्वयक (Volunteer)",
                    message = replyText,
                    timestamp = sdf.format(java.util.Date()),
                    isSelf = false
                )
                val finalUpdated = _groupMessages.value.toMutableList()
                finalUpdated.add(responderMsg)
                _groupMessages.value = finalUpdated
                saveGroupMessagesToPrefs(finalUpdated)
            }
        }
    }

    fun clearGroupMessages() {
        sharedPrefs.edit().remove("group_messages_json").apply()
        loadGroupMessages()
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
        _googleSheetsUrl.value = url
        sharedPrefs.edit().putString("google_sheets_url", url).apply()
    }

    fun updateTwoWaySyncUrl(url: String) {
        _twoWaySyncUrl.value = url.trim()
        sharedPrefs.edit().putString("two_way_sync_url", url.trim()).apply()
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

    val allContactsFlow: StateFlow<List<EmergencyContact>> = repository.allContacts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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
        // Show all approved contacts, and also show newly added pending contacts (isPending is true but updateForContactId is null)
        // so that they can be used immediately in an emergency before admin approval.
        // Keep pending deletions in the list so they aren't deleted immediately from public view without administrator approval.
        var list = contacts.filter { !it.isPending || it.updateForContactId == null }

        // Filter by Scope (Taluka Level vs Village Level)
        if (scope == "TALUKA") {
            list = list.filter { isTalukaLevel(it) }
        } else if (scope == "VILLAGE") {
            if (village == "ALL") {
                list = list.filter { !isTalukaLevel(it) }
            }
        }

        // Filter by village
        if (village != "ALL") {
            list = list.filter {
                isTalukaLevel(it) ||
                it.villageOrAreaMr.equals(village, ignoreCase = true) ||
                it.villageOrAreaEn.equals(village, ignoreCase = true)
            }
        }

        // Filter by category
        if (category != "ALL") {
            list = list.filter {
                it.category.equals(category, ignoreCase = true)
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

    val pendingContacts: StateFlow<List<EmergencyContact>> = repository.allContacts.map { contacts ->
        contacts.filter { it.isPending || it.isPendingDelete }
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

    fun pushContactsToGoogleSheet() {
        if (_syncStatus.value == "SYNCING") return
        val url = _twoWaySyncUrl.value
        if (url.isBlank()) {
            viewModelScope.launch {
                _syncStatus.value = "FAILED"
                _uiMessage.emit("टू-वे सिंक लिंक उपलब्ध नाही. कृपया ती सेटिंग्समध्ये प्रविष्ट करा.")
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _syncStatus.value = "SYNCING"
            try {
                val contactsList = repository.allContacts.first()
                    .distinctBy { "${it.nameMr.trim()}_${it.phone.trim()}_${it.category.trim()}_${it.villageOrAreaMr.trim()}" }
                if (contactsList.isEmpty()) {
                    throw Exception("डेटाबेसमध्ये कोणताही संपर्क उपलब्ध नाही.")
                }

                val jsonBuilder = StringBuilder()
                jsonBuilder.append("[")
                contactsList.forEachIndexed { index, contact ->
                    val escapeJson = { s: String ->
                        s.replace("\\", "\\\\")
                         .replace("\"", "\\\"")
                         .replace("\n", "\\n")
                         .replace("\r", "")
                         .replace("\t", "\\t")
                    }
                    jsonBuilder.append("{")
                    jsonBuilder.append("\"id\":${contact.id},")
                    jsonBuilder.append("\"nameMr\":\"${escapeJson(contact.nameMr)}\",")
                    jsonBuilder.append("\"nameEn\":\"${escapeJson(contact.nameEn)}\",")
                    jsonBuilder.append("\"phone\":\"${escapeJson(contact.phone)}\",")
                    jsonBuilder.append("\"phoneAlt\":\"${escapeJson(contact.phoneAlt)}\",")
                    jsonBuilder.append("\"category\":\"${escapeJson(contact.category)}\",")
                    jsonBuilder.append("\"designationMr\":\"${escapeJson(contact.designationMr)}\",")
                    jsonBuilder.append("\"designationEn\":\"${escapeJson(contact.designationEn)}\",")
                    jsonBuilder.append("\"villageOrAreaMr\":\"${escapeJson(contact.villageOrAreaMr)}\",")
                    jsonBuilder.append("\"villageOrAreaEn\":\"${escapeJson(contact.villageOrAreaEn)}\",")
                    jsonBuilder.append("\"isDefault\":${contact.isDefault},")
                    jsonBuilder.append("\"notes\":\"${escapeJson(contact.notes)}\"")
                    jsonBuilder.append("}")
                    if (index < contactsList.size - 1) {
                        jsonBuilder.append(",")
                    }
                }
                jsonBuilder.append("]")
                val jsonPayload = jsonBuilder.toString()

                val client = OkHttpClient()
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val body = jsonPayload.toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw Exception("HTTP Error: ${response.code}")
                }

                val responseBody = response.body?.string() ?: ""
                if (responseBody.contains("error")) {
                    throw Exception("Apps Script Error: $responseBody")
                }

                _syncStatus.value = "SUCCEEDED"
                _uiMessage.emit("गूगल शीटवर सर्व संपर्क यशस्वीरित्या पाठवले (Upload) गेले!")
            } catch (e: Exception) {
                _syncStatus.value = "FAILED"
                _uiMessage.emit("अपलोड अपयशी: ${e.localizedMessage}")
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
        notes: String = "",
        isAdmin: Boolean = false
    ) {
        viewModelScope.launch {
            if (nameMr.isBlank()) {
                _uiMessage.emit("कृपया निदान नाव प्रविष्ट करा")
                return@launch
            }

            if (isAdmin) {
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
                    notes = notes.trim(),
                    isLocal = true,
                    isPending = false
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
            } else {
                // Non-admin user saves/edits contact -> creates a pending contact
                try {
                    if (id == 0) {
                        // Requesting to add a new contact
                        val contact = EmergencyContact(
                            id = 0,
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
                            notes = notes.trim(),
                            isLocal = true,
                            isPending = true, // Awaiting admin approval
                            updateForContactId = null
                        )
                        repository.insert(contact)
                        _uiMessage.emit("नवीन संपर्क विनंती पाठवली आहे! प्रशासकीय मंजुरीनंतर तो यादीत दिसेल.")
                    } else {
                        // Requesting to update an existing contact -> Create a duplicate record with isPending=true and updateForContactId = id
                        val contact = EmergencyContact(
                            id = 0,
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
                            notes = notes.trim(),
                            isLocal = true,
                            isPending = true, // Awaiting admin approval
                            updateForContactId = id
                        )
                        repository.insert(contact)
                        _uiMessage.emit("संपर्क बदलाची विनंती पाठवली आहे! प्रशासकीय मंजुरीनंतर बदल अद्ययावत होईल.")
                    }
                } catch (e: Exception) {
                    _uiMessage.emit("विनंती पाठवताना चूक झाली: ${e.localizedMessage}")
                }
            }
        }
    }

    fun deleteContact(contact: EmergencyContact, isAdmin: Boolean = false) {
        viewModelScope.launch {
            try {
                if (isAdmin) {
                    if (contact.isPendingDelete) {
                        // Rejects a deletion request (clicks "Reject")
                        repository.update(contact.copy(isPendingDelete = false))
                        _uiMessage.emit("हटवण्याची विनंती नाकारली! संपर्क यादीत कायम राहील.")
                    } else if (contact.isPending && contact.updateForContactId != null) {
                        // Rejects an edit/update request (clicks "Reject") -> delete the pending edit contact record
                        repository.delete(contact)
                        _uiMessage.emit("संपर्क बदल विनंती नाकारली आणि काढली!")
                    } else {
                        // Direct hard delete of a contact
                        repository.delete(contact)
                        _uiMessage.emit("संपर्क काढला गेला!")
                    }
                } else {
                    // Standard user requests deletion -> soft delete (mark isPendingDelete = true)
                    repository.update(contact.copy(isPendingDelete = true))
                    _uiMessage.emit("संपर्क हटवण्याची विनंती प्रशासकाकडे पाठवली गेली आहे!")
                }
            } catch (e: Exception) {
                _uiMessage.emit("क्रिया अयशस्वी: ${e.localizedMessage}")
            }
        }
    }

    fun verifyContact(contact: EmergencyContact) {
        viewModelScope.launch {
            try {
                if (contact.isPendingDelete) {
                    // Approved deletion request
                    repository.delete(contact)
                    _uiMessage.emit("संपर्क यशस्वीरित्या डिलीट केला! (Contact deleted successfully!)")
                } else if (contact.isPending && contact.updateForContactId != null) {
                    // Approved update/edit request -> apply the updates to the original contact, then delete the pending copy
                    val originalContactId = contact.updateForContactId
                    val updatedOriginal = contact.copy(
                        id = originalContactId,
                        isPending = false,
                        updateForContactId = null,
                        isLocal = true
                    )
                    repository.update(updatedOriginal)
                    repository.delete(contact)
                    _uiMessage.emit("संपर्क बदल यशस्वीरित्या मंजूर केला! (Contact update approved!)")
                } else {
                    // Approved standard/volunteer request
                    repository.update(contact.copy(isPending = false, isLocal = true))
                    _uiMessage.emit("संपर्क यशस्वीरित्या मंजूर केला आणि समाविष्ट केला! (Contact verified & approved!)")
                }
            } catch (e: Exception) {
                _uiMessage.emit("मंजूर करताना चूक झाली: ${e.localizedMessage}")
            }
        }
    }

    fun registerVolunteer(
        nameMr: String, nameEn: String,
        phone: String, phoneAlt: String,
        category: String,
        designationMr: String, designationEn: String,
        villageOrAreaMr: String, villageOrAreaEn: String,
        notes: String,
        publishDirectly: Boolean = true
    ) {
        viewModelScope.launch {
            val contact = EmergencyContact(
                nameMr = nameMr.trim(),
                nameEn = nameEn.trim().ifBlank { nameMr.trim() },
                phone = phone.trim(),
                phoneAlt = phoneAlt.trim(),
                category = category,
                designationMr = designationMr.trim(),
                designationEn = designationEn.trim(),
                villageOrAreaMr = villageOrAreaMr.trim(),
                villageOrAreaEn = villageOrAreaEn.trim(),
                isDefault = false,
                notes = notes.trim(),
                isPending = !publishDirectly,
                isLocal = true
            )
            try {
                repository.insert(contact)
                if (publishDirectly) {
                    _uiMessage.emit("आपली नोंदणी यशस्वी झाली आणि ती थेट यादीत समाविष्ट केली गेली! (Volunteer registered & published!)")
                } else {
                    _uiMessage.emit("आपली नोंदणी यशस्वी झाली! प्रशासकीय पडताळणीनंतर नाव यादीत दिसेल.")
                }
            } catch (e: Exception) {
                _uiMessage.emit("नोंदणी करताना चूक झाली: ${e.localizedMessage}")
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

    // --- Chatbot Support ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "नमस्कार! मी आपला तालुका आपत्कालीन मार्गदर्शक आणि AI सहाय्यक आहे. मी आपल्याला संपर्क शोधण्यासाठी किंवा आपत्कालीन परिस्थितीत काय करावे याबद्दल मदत करू शकतो. विचारण्यासाठी खाली टाईप करा किंवा माइक वर बोलून विचारा! \n\n(Hello! I am your Taluka Emergency Directory AI Assistant. I can help you search contacts or guide you on what to do during emergencies. Type below or use the mic to ask!)",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(ChatMessage(text = userText, isUser = true))
        _chatMessages.value = currentList

        _isChatLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val contactsList = filteredContacts.value
            val contactsContext = if (contactsList.isEmpty()) {
                "सध्या निर्देशिकेत कोणतेही संपर्क उपलब्ध नाहीत."
            } else {
                "येथे तालुक्यातील आपत्कालीन संपर्काची यादी आहे:\n" +
                contactsList.joinToString("\n") { contact ->
                    "- नाव: ${contact.nameMr} (${contact.nameEn}), पद: ${contact.designationMr} (${contact.designationEn}), गाव/क्षेत्र: ${contact.villageOrAreaMr} (${contact.villageOrAreaEn}), फोन: ${contact.phone} ${if (contact.phoneAlt.isNotEmpty()) ", ${contact.phoneAlt}" else ""}, वर्गवारी: ${contact.category}, शेरा: ${contact.notes}"
                }
            }

            val systemInstruction = """
                आपण 'तालुका आपत्कालीन निर्देशिका' (Taluka Emergency Directory) चे अधिकृत AI सहाय्यक आहात. 
                वापरकर्त्यांना आपत्कालीन संपर्क क्रमांक शोधण्यात, डॉक्टर्स, पोलीस, प्रशासन, अग्निशामक दल, स्वयंसेवक यांची माहिती मिळवण्यात आणि आपत्ती व्यवस्थापन किंवा प्रथमोपचार (First Aid) यावर मार्गदर्शन करणे हे आपले मुख्य काम आहे.
                
                महत्त्वाचे (Google Search):
                आपल्याकडे गुगल सर्च (Google Search Grounding) चे टूल उपलब्ध आहे. जर वापरकर्त्याने हवामान (Weather), पूर किंवा आपत्तीच्या ताज्या बातम्या (Disaster News/Updates/Warnings), सामान्य प्रथमोपचार (First Aid), किंवा इतर कोणतीही माहिती विचारली जी यादीत उपलब्ध नाही, तर आपण गुगल सर्चचा वापर करून इंटरनेटवरून थेट ताजी, अचूक माहिती मिळवून वापरकर्त्याला मराठीत दिली पाहिजे.
                
                नियम:
                १. वापरकर्त्याच्या प्रश्नाचे उत्तर मराठीत किंवा इंग्रजीत द्या (मराठीला प्राधान्य द्या).
                २. जर वापरकर्त्याने एखाद्या स्थानिक अधिकाऱ्याचा, नावाचा किंवा गावाचा संपर्क मागितला, तर खाली दिलेल्या संपर्कांच्या यादीतून अचूक नाव, गाव आणि मुख्य फोन नंबर शोधा आणि द्या.
                ३. स्थानिक आपत्कालीन संपर्कासाठी स्वतःचे खोटे फोन नंबर किंवा संपर्क बनवू नका. जर ती स्थानिक माहिती निर्देशिकेत नसेल, तर सांगा की ती यादीत उपलब्ध नाही, आणि आपण गुगल सर्च वापरून त्या संबंधित इतर मदत किंवा अधिकृत ऑनलाईन माहिती देण्याचा प्रयत्न करा.
                ४. हवामान अंदाज, आपत्ती व्यवस्थापन मार्गदर्शक तत्त्वे, प्रथमोपचार टिप्स, किंवा इतर ऑनलाईन प्रश्नांसाठी आवर्जून गुगल सर्चचा वापर करून ताजी माहिती मराठीत मुद्देसूद आणि सोप्या शब्दांत द्या.
                ५. प्रथमोपचार किंवा सुरक्षेबद्दल विचारल्यास सोपे आणि व्यावहारिक मराठीत मुद्देसूद माहिती द्या (उदा. साप चावल्यावर काय करावे, पुराच्या वेळी काय काळजी घ्यावी).
                
                येथे तालुक्यातील अधिकृत आणि अद्ययावत संपर्काची यादी आहे:
                $contactsContext
            """.trimIndent()

            val reply = GeminiClient.getChatResponse(userText, systemInstruction)

            withContext(Dispatchers.Main) {
                val updatedList = _chatMessages.value.toMutableList()
                updatedList.add(
                    ChatMessage(
                        text = reply.text,
                        isUser = false,
                        searchQueries = reply.searchQueries,
                        sources = reply.sources
                    )
                )
                _chatMessages.value = updatedList
                _isChatLoading.value = false
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                text = "नमस्कार! मी आपला तालुका आपत्कालीन मार्गदर्शक आणि AI सहाय्यक आहे. मी आपल्याला संपर्क शोधण्यासाठी किंवा आपत्कालीन परिस्थितीत काय करावे याबद्दल मदत करू शकतो. विचारण्यासाठी खाली टाईप करा किंवा माइक वर बोलून विचारा! \n\n(Hello! I am your Taluka Emergency Directory AI Assistant. I can help you search contacts or guide you on what to do during emergencies. Type below or use the mic to ask!)",
                isUser = false
            )
        )
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val searchQueries: List<String>? = null,
    val sources: List<SearchSource>? = null
)

data class GroupChatMessage(
    val id: String,
    val senderName: String,
    val senderRole: String,
    val message: String,
    val timestamp: String,
    val isSelf: Boolean
)

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
