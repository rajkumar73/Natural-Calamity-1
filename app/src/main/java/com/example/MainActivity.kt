package com.example

import android.content.Context
import android.app.Application
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.ContactViewModel
import com.example.ui.ContactViewModelFactory
import com.example.ui.ChatbotScreen
import com.example.ui.GroupChatScreen
import com.example.ui.theme.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    
    // Initialize our ContactViewModel
    val contactViewModel: ContactViewModel = viewModel(
        factory = ContactViewModelFactory(application)
    )

    // Collect states
    val contacts by contactViewModel.filteredContacts.collectAsStateWithLifecycle()
    val allContactsList by contactViewModel.allContactsFlow.collectAsStateWithLifecycle()
    val searchQuery by contactViewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by contactViewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedVillage by contactViewModel.selectedVillage.collectAsStateWithLifecycle()
    val availableVillages by contactViewModel.availableVillages.collectAsStateWithLifecycle()
    val selectedScope by contactViewModel.selectedScope.collectAsStateWithLifecycle()
    val isOnline by contactViewModel.isOnline.collectAsStateWithLifecycle()
    val syncStatus by contactViewModel.syncStatus.collectAsStateWithLifecycle()
    val lastSyncedTime by contactViewModel.lastSyncedTime.collectAsStateWithLifecycle()
    val googleSheetsUrl by contactViewModel.googleSheetsUrl.collectAsStateWithLifecycle()
    val twoWaySyncUrl by contactViewModel.twoWaySyncUrl.collectAsStateWithLifecycle()
    val talukaName by contactViewModel.talukaName.collectAsStateWithLifecycle()
    val talukaNameEn by contactViewModel.talukaNameEn.collectAsStateWithLifecycle()
    val appNameMr by contactViewModel.appNameMr.collectAsStateWithLifecycle()
    val appNameEn by contactViewModel.appNameEn.collectAsStateWithLifecycle()
    val liveAlertMr by contactViewModel.liveAlertMr.collectAsStateWithLifecycle()
    val liveAlertEn by contactViewModel.liveAlertEn.collectAsStateWithLifecycle()
    val isLiveAlertDismissed by contactViewModel.isLiveAlertDismissed.collectAsStateWithLifecycle()
    val pendingContacts by contactViewModel.pendingContacts.collectAsStateWithLifecycle()
    val isChatbotEnabled by contactViewModel.isChatbotEnabled.collectAsStateWithLifecycle()
    val isLiveAlertEnabled by contactViewModel.isLiveAlertEnabled.collectAsStateWithLifecycle()
    val selectedThemeCode by contactViewModel.selectedThemeCode.collectAsStateWithLifecycle()

    // Dynamically update theme color when selectedThemeCode changes
    LaunchedEffect(selectedThemeCode) {
        GBPrimaryState.value = when (selectedThemeCode) {
            "TEAL" -> Color(0xFF0D9488)      // Emerald Ocean / Teal
            "RED" -> Color(0xFFFF2D55)       // Emergency Crimson Alert
            "PURPLE" -> Color(0xFF8B5CF6)    // Premium Cosmic Indigo
            "ORANGE" -> Color(0xFFFF6B00)    // Sunset Safety Orange
            "DARK" -> Color(0xFF475569)      // Charcoal Slate Minimal
            else -> Color(0xFF2563EB)        // Classic Royal Blue
        }
    }

    val weatherTemp by contactViewModel.weatherTemp.collectAsStateWithLifecycle()
    val weatherStatusMr by contactViewModel.weatherStatusMr.collectAsStateWithLifecycle()
    val weatherStatusEn by contactViewModel.weatherStatusEn.collectAsStateWithLifecycle()
    val floodLevelMr by contactViewModel.floodLevelMr.collectAsStateWithLifecycle()
    val floodLevelEn by contactViewModel.floodLevelEn.collectAsStateWithLifecycle()
    val floodProgress by contactViewModel.floodProgress.collectAsStateWithLifecycle()
    val favoriteContactIds by contactViewModel.favoriteContactIds.collectAsStateWithLifecycle()

    // State parameters for UI flow
    var isMarathi by remember { mutableStateOf(true) }
    var activeTab by remember { mutableStateOf("contacts") }
    var currentHelpSectionExpanded by remember { mutableStateOf(false) }
    var googleSheetsSectionExpanded by remember { mutableStateOf(false) }
    var expandedContactId by remember { mutableStateOf<Int?>(null) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showVolunteerRegistrationDialog by remember { mutableStateOf(false) }
    var isAdminUnlocked by remember { mutableStateOf(false) }
    var adminPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showTalukaEditDialogSystem by remember { mutableStateOf(false) }
    var showAppNameEditDialogSystem by remember { mutableStateOf(false) }
    var showLiveAlertEditDialogSystem by remember { mutableStateOf(false) }

    var pendingPhoneToCall by remember { mutableStateOf<String?>(null) }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val phone = pendingPhoneToCall
        if (phone != null) {
            try {
                if (isGranted) {
                    val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone"))
                    context.startActivity(callIntent)
                } else {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    context.startActivity(dialIntent)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "कॉल करता आला नाही", Toast.LENGTH_SHORT).show()
            }
            pendingPhoneToCall = null
        }
    }

    val triggerDirectCall: (String) -> Unit = { phone ->
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CALL_PHONE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone"))
                context.startActivity(callIntent)
            } catch (e: Exception) {
                try {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    context.startActivity(dialIntent)
                } catch (ex: Exception) {
                    Toast.makeText(context, "कॉल करता आला नाही", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            pendingPhoneToCall = phone
            callPermissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
        }
    }

    // Speech recognizer launcher for Voice Search
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (spokenText != null) {
                contactViewModel.updateSearchQuery(spokenText)
            }
        }
    }

    val startVoiceSearch = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isMarathi) "mr-IN" else "en-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, if (isMarathi) "संपर्क शोधण्यासाठी नाव, गाव किंवा पद बोला..." else "Speak name, village or post to search...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, if (isMarathi) "आपल्या डिव्हाइसवर स्पीच रिकग्निशन उपलब्ध नाही." else "Speech recognition not available on your device.", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher to download/save the Google Sheets CSV Template document natively
    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val csvHeaders = "nameMr,nameEn,phone,phoneAlt,category,designationMr,designationEn,villageOrAreaMr,villageOrAreaEn,isDefault,notes\n"
                    val csvRows = listOf(
                        "तहसीलदार तथा तालुका दंडाधिकारी,Tehsildar & Taluka Magistrate,02342-221011,9422000000,ADMIN,तालुका प्रमुख समन्वयक,Taluka Chief Coordinator,तालुका (सर्व),Taluka (All),TRUE,पूर व अतिवृष्टी समन्वय प्रमुख अधिकारी.",
                        "प्राथमिक आरोग्य केंद्र कोकरुड,Primary Health Center Kokrud,02342-224050,9876543210,MEDICAL,वैद्यकीय अधिकारी,Medical Officer in Charge,कोकरुड,Kokrud,TRUE,२४ तास आपत्कालीन वैद्यकीय सुविधा आणि रुग्णवाहिका उपलब्ध.",
                        "स्थानिक आपत्ती मदत स्वयंसेवक,Local Rescue Volunteer,9988776655,,RESCUE,नाविक व पोहणारे समन्वयक,Rescue Swimmer Coordinator,कोकरुड,Kokrud,TRUE,कोकरुड घाट व वारणा नदी पात्रात तातडीच्या मदतीसाठी.",
                        "महावितरण शाखा कार्यालय,MSEDCL Substation Office,1912,18002333435,UTILITY,कनिष्ठ अभियंता,Junior Engineer,शिराळा,Shirala,TRUE,वीज पुरवठा खंडित झाल्यास किंवा खांब पडल्यास तक्रार नोंदणी."
                    ).joinToString("\n")
                    outputStream.write((csvHeaders + csvRows).toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "गूगल शीट नमुना (CSV) यशस्वीरित्या साठवला गेला!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "फाइल सेव्ह करताना त्रुटी आली: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher to export all current actual contacts to local CSV
    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val csvHeaders = "\uFEFFnameMr,nameEn,phone,phoneAlt,category,designationMr,designationEn,villageOrAreaMr,villageOrAreaEn,isDefault,notes\n"
                    val csvRows = contacts.map { contact ->
                        val escape = { s: String ->
                            if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
                                "\"${s.replace("\"", "\"\"")}\""
                            } else s
                        }
                        "${escape(contact.nameMr)},${escape(contact.nameEn)},${escape(contact.phone)},${escape(contact.phoneAlt)},${escape(contact.category)},${escape(contact.designationMr)},${escape(contact.designationEn)},${escape(contact.villageOrAreaMr)},${escape(contact.villageOrAreaEn)},${contact.isDefault},${escape(contact.notes)}"
                    }.joinToString("\n")
                    outputStream.write((csvHeaders + csvRows).toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "संपर्क यादी (CSV) स्वरूपात यशस्वीरित्या सेव्ह झाली!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "त्रुटी: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher to select and imports a local CSV file
    val importCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    val csvText = String(bytes, Charsets.UTF_8)
                    contactViewModel.importLocalContactsCsv(csvText)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "वाचताना त्रुटी आली: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // Form Dialog state
    var showFormDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<EmergencyContact?>(null) }

    // Toast and system triggers
    LaunchedEffect(key1 = true) {
        contactViewModel.uiMessage.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_root_scaffold"),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                // Tab 1: Contacts (थेट संपर्क)
                NavigationBarItem(
                    selected = activeTab == "contacts" && !(showFormDialog && editingContact == null),
                    onClick = {
                        activeTab = "contacts"
                        if (editingContact == null) {
                            showFormDialog = false
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ContactPhone,
                            contentDescription = if (isMarathi) "थेट संपर्क" else "Contacts"
                        )
                    },
                    label = {
                        Text(
                            text = if (isMarathi) "थेट संपर्क" else "Contacts",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = GBPrimary,
                        indicatorColor = GBPrimary,
                        unselectedIconColor = GBPrimary.copy(alpha = 0.5f),
                        unselectedTextColor = GBGreyText
                    )
                )
                // Tab 2: Add Contact (नवीन संपर्क)
                NavigationBarItem(
                    selected = activeTab == "contacts" && showFormDialog && editingContact == null,
                    onClick = {
                        activeTab = "contacts"
                        editingContact = null
                        showFormDialog = true
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = if (isMarathi) "नवीन संपर्क" else "Add Contact"
                        )
                    },
                    label = {
                        Text(
                            text = if (isMarathi) "नवीन संपर्क" else "Add New",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = GreenMedical,
                        indicatorColor = GreenMedical,
                        unselectedIconColor = GreenMedical.copy(alpha = 0.5f),
                        unselectedTextColor = GBGreyText
                    )
                )
                // Tab 3: AI Chatbot (AI सहाय्यक)
                NavigationBarItem(
                    selected = activeTab == "chatbot",
                    onClick = { activeTab = "chatbot" },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = if (isMarathi) "AI सहाय्यक" else "AI Chatbot"
                        )
                    },
                    label = {
                        Text(
                            text = if (isMarathi) "AI सहाय्यक" else "AI Assistant",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color(0xFF8B5CF6),
                        indicatorColor = Color(0xFF8B5CF6),
                        unselectedIconColor = Color(0xFF8B5CF6).copy(alpha = 0.5f),
                        unselectedTextColor = GBGreyText
                    )
                )
                // Tab 4: Group Chat (चर्चा गट)
                NavigationBarItem(
                    selected = activeTab == "group_chat",
                    onClick = { activeTab = "group_chat" },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = if (isMarathi) "चर्चा गट" else "Group Chat"
                        )
                    },
                    label = {
                        Text(
                            text = if (isMarathi) "चर्चा गट" else "Group Chat",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color(0xFFD97706),
                        indicatorColor = Color(0xFFD97706),
                        unselectedIconColor = Color(0xFFD97706).copy(alpha = 0.5f),
                        unselectedTextColor = GBGreyText
                    )
                )
                // Tab 5: Settings / Guides (मार्गदर्शक)
                NavigationBarItem(
                    selected = activeTab == "settings",
                    onClick = { activeTab = "settings" },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = if (isMarathi) "मार्गदर्शक" else "Settings"
                        )
                    },
                    label = {
                        Text(
                            text = if (isMarathi) "मार्गदर्शक" else "Guides",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = RedEmergency,
                        indicatorColor = RedEmergency,
                        unselectedIconColor = RedEmergency.copy(alpha = 0.5f),
                        unselectedTextColor = GBGreyText
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(GBBg)
        ) {
            if (activeTab == "contacts") {
                // TAB 1: 100% CONTACT SEARCH ENGINE WITH INTEGRATED ADJACENT FORM PANEL
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isWide = maxWidth >= 640.dp
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (!showFormDialog || isWide) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                ContactsListArea(
                                    contacts = contacts,
                                    expandedContactId = expandedContactId,
                                    onContactClick = { id ->
                                        expandedContactId = if (expandedContactId == id) null else id
                                    },
                                    onCallClick = { phone ->
                                        triggerDirectCall(phone)
                                    },
                                    onShareClick = { contact ->
                                        val shareString = buildBilingualShareText(contact, isMarathi)
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "आपत्कालीन संपर्क / Emergency Contact")
                                            putExtra(Intent.EXTRA_TEXT, shareString)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "संपर्क शेअर करा (Share Contact)"))
                                    },
                                    onEditClick = { contact ->
                                        editingContact = contact
                                        showFormDialog = true
                                    },
                                    onDeleteClick = { contact ->
                                        contactViewModel.deleteContact(contact, isAdmin = isAdminUnlocked)
                                    },
                                    isMarathi = isMarathi,
                                    selectedScope = selectedScope,
                                    searchQuery = searchQuery,
                                    onSearchChange = { contactViewModel.updateSearchQuery(it) },
                                    onLanguageToggle = { isMarathi = !isMarathi },
                                    onCallEmergency = { num ->
                                        triggerDirectCall(num)
                                    },
                                    onScopeSelect = { contactViewModel.selectScope(it) },
                                    availableVillages = availableVillages,
                                    selectedVillage = selectedVillage,
                                    onVillageSelect = { contactViewModel.selectVillage(it) },
                                    selectedCategory = selectedCategory,
                                    onCategorySelect = { contactViewModel.selectCategory(it) },
                                    onCloudSyncClick = {
                                        contactViewModel.triggerBackgroundSync()
                                        Toast.makeText(
                                            context,
                                            if (isMarathi) "क्लाऊड डेटा सिंक सुरू होत आहे... 🔄" else "Cloud data sync starting... 🔄",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    talukaName = talukaName,
                                    talukaNameEn = talukaNameEn,
                                    appNameMr = appNameMr,
                                    appNameEn = appNameEn,
                                    favoriteContactIds = favoriteContactIds,
                                    onFavoriteToggle = { contactViewModel.toggleFavorite(it) },
                                    liveAlertMr = liveAlertMr,
                                    liveAlertEn = liveAlertEn,
                                    isLiveAlertDismissed = isLiveAlertDismissed,
                                    onDismissLiveAlert = { contactViewModel.dismissLiveAlert() },
                                    isLiveAlertEnabled = isLiveAlertEnabled,
                                    isAdminMode = isAdminUnlocked,
                                    isOnline = isOnline,
                                    syncStatus = syncStatus,
                                    lastSyncedTime = lastSyncedTime,
                                    onRegisterVolunteerClick = { showVolunteerRegistrationDialog = true },
                                    onVoiceSearchClick = startVoiceSearch
                                )
                            }
                        }

                        if (showFormDialog) {
                            if (isWide) {
                                // Direct adjacent side panel for wide screens (no popups, beautiful side-by-side)
                                Box(
                                    modifier = Modifier
                                        .width(360.dp)
                                        .fillMaxHeight()
                                ) {
                                    ContactFormPanel(
                                        editingContact = editingContact,
                                        isMarathi = isMarathi,
                                        onDismiss = { showFormDialog = false },
                                        allContacts = allContactsList,
                                        onSave = { id, nameMr, nameEn, phone, phoneAlt, category, desMr, desEn, villMr, villEn, isDef, notes ->
                                            contactViewModel.saveContact(
                                                id = id,
                                                nameMr = nameMr,
                                                nameEn = nameEn,
                                                phone = phone,
                                                phoneAlt = phoneAlt,
                                                category = category,
                                                designationMr = desMr,
                                                designationEn = desEn,
                                                villageOrAreaMr = villMr,
                                                villageOrAreaEn = villEn,
                                                isDefault = isDef,
                                                notes = notes,
                                                isAdmin = isAdminUnlocked
                                            )
                                            showFormDialog = false
                                        }
                                    )
                                }
                            } else {
                                // On mobile, show it as full screen so it fills the screen perfectly and beautifully!
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {
                                    ContactFormPanel(
                                        editingContact = editingContact,
                                        isMarathi = isMarathi,
                                        onDismiss = { showFormDialog = false },
                                        allContacts = allContactsList,
                                        onSave = { id, nameMr, nameEn, phone, phoneAlt, category, desMr, desEn, villMr, villEn, isDef, notes ->
                                            contactViewModel.saveContact(
                                                id = id,
                                                nameMr = nameMr,
                                                nameEn = nameEn,
                                                phone = phone,
                                                phoneAlt = phoneAlt,
                                                category = category,
                                                designationMr = desMr,
                                                designationEn = desEn,
                                                villageOrAreaMr = villMr,
                                                villageOrAreaEn = villEn,
                                                isDefault = isDef,
                                                notes = notes,
                                                isAdmin = isAdminUnlocked
                                            )
                                            showFormDialog = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (activeTab == "chatbot") {
                // TAB 2: AI CHATBOT ASSISTANT
                ChatbotScreen(
                    contactViewModel = contactViewModel,
                    isMarathi = isMarathi
                )
            } else if (activeTab == "group_chat") {
                // TAB 2.5: TALUKA GROUP CHAT DISCUSSION BOARD
                GroupChatScreen(
                    contactViewModel = contactViewModel,
                    isMarathi = isMarathi
                )
            } else {
                // TAB 3: SYSTEM GUIDES, IMPORT/EXPORT TOOLS & SYSTEM MAINTENANCE
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. HEADER WITH STATS & LANGUAGE SWITCHER
                    HeaderSection(
                        isMarathi = isMarathi,
                        talukaName = talukaName,
                        talukaNameEn = talukaNameEn,
                        appNameMr = appNameMr,
                        appNameEn = appNameEn,
                        onLanguageToggle = { isMarathi = !isMarathi },
                        onCallEmergency = { num ->
                            triggerDirectCall(num)
                        },
                        onCloudSyncClick = {
                            contactViewModel.triggerBackgroundSync()
                            Toast.makeText(
                                context,
                                if (isMarathi) "क्लाऊड डेटा सिंक सुरू होत आहे... 🔄" else "Cloud data sync starting... 🔄",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )

                    // Auto Background Cloud Sync & Offline Status Indicator Bar
                    SyncStatusBanner(
                        isOnline = isOnline,
                        syncStatus = syncStatus,
                        lastSyncedTime = lastSyncedTime,
                        onForceSync = { contactViewModel.triggerBackgroundSync() },
                        isMarathi = isMarathi
                    )

                    // 2. DISASTER PREPAREDNESS TIPS CORNER (Always visible & public)
                    DisasterTipsSection(
                        isMarathi = isMarathi,
                        isExpanded = currentHelpSectionExpanded,
                        onToggleExpand = { currentHelpSectionExpanded = !currentHelpSectionExpanded }
                    )

                    // Theme Selector Card (Instantly customizes active theme colors!)
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("theme_selector_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, GBBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Theme Palette",
                                    tint = GBPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isMarathi) "🎨 ॲपची रंगसंगती बदला (Theme)" else "🎨 Customize App Theme",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = GBText
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = if (isMarathi) 
                                    "ॲपचा मुख्य रंग बदलण्यासाठी खालीलपैकी कोणताही आकर्षक रंग निवडा:" 
                                    else "Select any vibrant theme below to instantly customize the app experience:",
                                fontSize = 11.sp,
                                color = GBGreyText
                            )
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            val themesList = listOf(
                                Triple("DEFAULT", if (isMarathi) "शाही निळा" else "Royal Blue", Color(0xFF2563EB)),
                                Triple("TEAL", if (isMarathi) "हिरवा-टीळ" else "Ocean Teal", Color(0xFF0D9488)),
                                Triple("RED", if (isMarathi) "लाल इशारा" else "Crimson Red", Color(0xFFFF2D55)),
                                Triple("PURPLE", if (isMarathi) "जांभळा" else "Cosmic Purple", Color(0xFF8B5CF6)),
                                Triple("ORANGE", if (isMarathi) "नारंगी" else "Orange", Color(0xFFFF6B00)),
                                Triple("DARK", if (isMarathi) "कोळसा" else "Slate Gray", Color(0xFF475569))
                            )
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                items(themesList) { (code, themeName, colorValue) ->
                                    val isSelected = selectedThemeCode == code
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable {
                                                contactViewModel.setSelectedThemeCode(code)
                                            }
                                            .padding(2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(colorValue, CircleShape)
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) GBText else colorValue.copy(alpha = 0.3f),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = themeName,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) GBPrimary else GBGreyText,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. ADMIN TOOLS - PASSWORD PROTECTION BARRIER
                    if (!isAdminUnlocked) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, GBBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Lock",
                                        tint = GBGreyText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isMarathi) "⚙️ ॲडमिन सेटिंग्स पासवर्ड सुरक्षित" else "⚙️ Admin Settings Password Secure",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = GBText
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isMarathi) 
                                        "गुगल शीट बदलणे किंवा डेटाबेस रिस्टोर / पुसून टाकण्यासाठी पासवर्ड टाका." 
                                        else "Enter password to sync Google Sheets or perform database maintenance.",
                                    fontSize = 11.sp,
                                    color = GBGreyText,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = adminPinInput,
                                        onValueChange = { 
                                            adminPinInput = it
                                            pinError = false
                                        },
                                        placeholder = { Text(if (isMarathi) "पासवर्ड टाका..." else "Enter password...", fontSize = 12.sp) },
                                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                                        ),
                                        singleLine = true,
                                        maxLines = 1,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(54.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = GBText,
                                            unfocusedTextColor = GBText,
                                            focusedPlaceholderColor = GBGreyText,
                                            unfocusedPlaceholderColor = GBGreyText,
                                            focusedBorderColor = GBPrimary,
                                            unfocusedBorderColor = if (pinError) Color.Red else GBBorder,
                                            unfocusedContainerColor = Color(0xFFFAFAFA),
                                            focusedContainerColor = Color.White
                                        )
                                    )
                                    
                                    Button(
                                        onClick = {
                                            if (adminPinInput == "Rajkumar@2026") {
                                                isAdminUnlocked = true
                                                adminPinInput = ""
                                                pinError = false
                                            } else {
                                                pinError = true
                                                Toast.makeText(context, if (isMarathi) "चुकीचा पासवर्ड!" else "Incorrect Password!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GBPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(54.dp)
                                    ) {
                                        Text(text = if (isMarathi) "अनलॉक" else "Unlock", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        // Banner showing Admin Active State and a Lock button
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            border = BorderStroke(1.dp, Color(0xFFA5D6A7))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = "Unlocked",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isMarathi) "🔓 ॲडमिन मोड सुरू आहे" else "🔓 Admin Mode Active",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                Button(
                                    onClick = { isAdminUnlocked = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text(
                                        text = if (isMarathi) "लॉक करा" else "Lock",
                                        color = Color(0xFFC62828),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Volunteer Verification Section (Admins verify self-registrations)
                        PendingVolunteersSection(
                            isMarathi = isMarathi,
                            pendingContacts = pendingContacts,
                            onApprove = { contactViewModel.verifyContact(it) },
                            onReject = { contactViewModel.deleteContact(it, isAdmin = true) }
                        )

                        // TWO-WAY CLOUD SYNC CONTROL PANEL (Visible only to Admin)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, GBBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = "Two-Way Sync",
                                        tint = GBPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isMarathi) "टू-वे क्लाउड सिंक कंट्रोल पॅनेल 🔄" else "Two-Way Cloud Sync Panel 🔄",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = GBText
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isMarathi) {
                                        "डेटाबेस क्लाउड सिंक सेटिंग्स, स्वयंचलित सिंक्रोनाइझेशन आणि रिअल-टाइम बॅकअप व्यवस्थापन उघडा."
                                    } else {
                                        "Manage cloud sync configurations, configure auto-sync behavior, and run manual database cloud backup."
                                    },
                                    fontSize = 12.sp,
                                    color = GBGreyText
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showSyncDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = GBPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Text(
                                        text = if (isMarathi) "🔄 टू-वे क्लाउड सिंक पॅनेल उघडा" else "🔄 Open Two-Way Sync Panel",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // GOOGLE SHEET SYNC & DATA MANAGEMENT CORNER (Visible only to Admin)
                        GoogleSheetSyncSection(
                            isMarathi = isMarathi,
                            isExpanded = googleSheetsSectionExpanded,
                            onToggleExpand = { googleSheetsSectionExpanded = !googleSheetsSectionExpanded },
                            sheetUrl = googleSheetsUrl,
                            onUrlChange = { contactViewModel.updateGoogleSheetsUrl(it) },
                            onSyncClick = { contactViewModel.triggerBackgroundSync() },
                            onOpenTwoWaySyncPanel = { showSyncDialog = true },
                            syncStatus = syncStatus,
                            onDownloadTemplate = {
                                try {
                                    csvLauncher.launch("taluka_emergency_contacts_template.csv")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "डाउनलोडर सुरू करता आला नाही.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // Edit Taluka Name Card (Visible only to Admin)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, GBBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Taluka",
                                        tint = GBPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isMarathi) "तालुक्याचे नाव बदला" else "Change Taluka Name",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = GBText
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isMarathi) {
                                        "ॲपच्या हेडरमध्ये दिसणारे मराठी व इंग्रजी तालुक्याचे नाव बदलण्यासाठी खालील बटणावर क्लिक करा."
                                    } else {
                                        "Click below to change the Marathi and English Taluka names shown in the header of the app."
                                    },
                                    fontSize = 12.sp,
                                    color = GBGreyText
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showTalukaEditDialogSystem = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = GBPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Text(
                                        text = if (isMarathi) "✏️ नाव बदला" else "✏️ Change Name",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Edit App Name Card (Visible only to Admin)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, GBBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit App Name",
                                        tint = GBPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isMarathi) "अ‍ॅपचे नाव बदला" else "Change App Name",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = GBText
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isMarathi) {
                                        "अ‍ॅपच्या हेडर आणि विविध ठिकाणी दिसणारे अ‍ॅपचे नाव बदलण्यासाठी खालील बटणावर क्लिक करा."
                                    } else {
                                        "Click below to change the App Name shown in the header and other areas of the application."
                                    },
                                    fontSize = 12.sp,
                                    color = GBGreyText
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showAppNameEditDialogSystem = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = GBPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Text(
                                        text = if (isMarathi) "✏️ अ‍ॅपचे नाव बदला" else "✏️ Change App Name",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Edit Live Alert Card (Visible only to Admin)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, GBBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Edit Live Alert",
                                        tint = Color(0xFFDC2626)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isMarathi) "ताजा इशारा (Live Alert) बदला" else "Change Live Alert Bulletin",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = GBText
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isMarathi) {
                                        "ॲपच्या पहिल्या स्क्रीनवर नागरिकांना मार्गदर्शन करण्यासाठी व चालू अलर्ट देण्यासाठी प्रसिद्ध होणारा ताजा इशारा नियंत्रित करा."
                                    } else {
                                        "Change the flashing Live Alert shown prominently inside the main contacts directory."
                                    },
                                    fontSize = 12.sp,
                                    color = GBGreyText
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showLiveAlertEditDialogSystem = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Text(
                                        text = if (isMarathi) "📢 इशारा बदला" else "📢 Edit Bulletin",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Advanced Features Management Card (Visible only to Admin)
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("advanced_features_card"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, GBBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Features",
                                        tint = GBPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isMarathi) "प्रगत सुविधा व्यवस्थापन (A & D)" else "Advanced Features Settings",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = GBText
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isMarathi) {
                                        "ॲपमधील महत्त्वाच्या एआय सहाय्यक (Chatbot) आणि ताजी सूचना (Live Alert) या सुविधा सुरू किंवा बंद करा."
                                    } else {
                                        "Toggle advanced features such as the AI Chatbot and the Live Alert Banner."
                                    },
                                    fontSize = 12.sp,
                                    color = GBGreyText
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Feature A: AI Chatbot Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isMarathi) "A. AI सहाय्यक (Chatbot)" else "A. AI Chatbot Assistant",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = GBText
                                        )
                                        Text(
                                            text = if (isMarathi) {
                                                if (isChatbotEnabled) "सुरू आहे (नागरिकांना दिसेल)" else "बंद आहे (नागरिकांना दिसणार नाही)"
                                            } else {
                                                if (isChatbotEnabled) "Enabled (Visible to public)" else "Disabled (Hidden from public)"
                                            },
                                            fontSize = 11.sp,
                                            color = if (isChatbotEnabled) Color(0xFF0D9488) else Color(0xFFFF2D55)
                                        )
                                    }
                                    Switch(
                                        checked = isChatbotEnabled,
                                        onCheckedChange = { contactViewModel.setChatbotEnabled(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = GBPrimary
                                        ),
                                        modifier = Modifier.testTag("chatbot_toggle_switch")
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = Color(0xFFE2E8F0))
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Feature D: Live Alert Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isMarathi) "D. ताजी सूचना (Live Alert)" else "D. Live Alert Banner",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = GBText
                                        )
                                        Text(
                                            text = if (isMarathi) {
                                                if (isLiveAlertEnabled) "सुरू आहे (हवामान/आपत्ती इशारा दिसेल)" else "बंद आहे (इशारा बॅनर लपवला जाईल)"
                                            } else {
                                                if (isLiveAlertEnabled) "Enabled (Alert banner shown)" else "Disabled (Alert banner hidden)"
                                            },
                                            fontSize = 11.sp,
                                            color = if (isLiveAlertEnabled) Color(0xFF0D9488) else Color(0xFFFF2D55)
                                        )
                                    }
                                    Switch(
                                        checked = isLiveAlertEnabled,
                                        onCheckedChange = { contactViewModel.setLiveAlertEnabled(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = GBPrimary
                                        ),
                                        modifier = Modifier.testTag("live_alert_toggle_switch")
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Database Maintenance Card (Visible only to Admin)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, GBBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "रीसेट",
                                        tint = GBPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isMarathi) "डेटाबेस रीसेट / रिस्टोर" else "Database Maintenance",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = GBText
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isMarathi) {
                                        "चालू असलेला सर्व डेटा काढून मूळ डीफॉल्ट शासकीय संपर्क डेटा री-लोड करण्यासाठी 'रिस्टोर' दाबा. किंवा संपूर्ण डेटाबेस रिकामा करून फक्त स्वतःचा गुगल शीट डेटा ठेवण्यासाठी 'सर्व पुसून टाका' दाबा."
                                    } else {
                                        "Press 'Restore Defaults' to reload core government directory, or 'Clear All' to delete the entire database. Clear All is recommended before syncing custom Google Sheets data so that old hardcoded data doesn't persist."
                                    },
                                    fontSize = 12.sp,
                                    color = GBGreyText
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { contactViewModel.resetToDefaults() },
                                        colors = ButtonDefaults.buttonColors(containerColor = GBPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(42.dp)
                                    ) {
                                        Text(
                                            text = if (isMarathi) "🔄 रिस्टोर करा" else "🔄 Restore Defaults",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Button(
                                        onClick = { contactViewModel.clearAllContacts() },
                                        colors = ButtonDefaults.buttonColors(containerColor = GBSosText),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(42.dp)
                                    ) {
                                        Text(
                                            text = if (isMarathi) "🗑️ सर्व पुसून टाका" else "🗑️ Clear All",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Offline CSV Backup & Restore Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, GBBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Backup,
                                        contentDescription = "Backup",
                                        tint = GBPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isMarathi) "स्थानिक ऑफलाईन बॅकअप" else "Local Offline Backup",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = GBText
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isMarathi) {
                                        "आपत्तीच्या वेळेस इंटरनेट बंद असल्यास ब्लूटूथ किंवा जवळून फाईल शेअर करून संपूर्ण संपर्क डेटाबेस आयात किंवा निर्यात करा. डेटा सुरक्षित आणि 100% ऑफलाईन साठवला जाईल."
                                    } else {
                                        "In disasters where internet/cloud sync is down, export/import your entire contact database locally. Share with nearby rescuers via local Bluetooth or nearby transfer."
                                    },
                                    fontSize = 12.sp,
                                    color = GBGreyText
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            try {
                                                exportCsvLauncher.launch("taluka_emergency_contacts_local.csv")
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "एक्सपोर्टर सुरू करता आला नाही.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GBPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(42.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = "Export"
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isMarathi) "एक्सपोर्ट CSV" else "Export CSV",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            try {
                                                importCsvLauncher.launch("*/*")
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "इम्पोर्टर सुरू करता आला नाही.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GBPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(42.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Upload,
                                                contentDescription = "Import"
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isMarathi) "इम्पोर्ट CSV" else "Import CSV",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }



        if (showVolunteerRegistrationDialog) {
            VolunteerRegistrationDialog(
                isMarathi = isMarathi,
                onDismiss = { showVolunteerRegistrationDialog = false },
                onRegister = { nameMr, nameEn, phone, phoneAlt, desMr, desEn, villMr, villEn, notes, publishDirectly ->
                    contactViewModel.registerVolunteer(
                        nameMr = nameMr,
                        nameEn = nameEn,
                        phone = phone,
                        phoneAlt = phoneAlt,
                        category = "RESCUE",
                        designationMr = desMr,
                        designationEn = desEn,
                        villageOrAreaMr = villMr,
                        villageOrAreaEn = villEn,
                        notes = notes,
                        publishDirectly = publishDirectly
                    )
                    showVolunteerRegistrationDialog = false
                }
            )
        }

        // 7. GOOGLE SHEETS QUICK SYNC & LINK CONFIG DIALOG
        if (showSyncDialog) {
            GoogleSheetsQuickConfigDialog(
                currentUrl = googleSheetsUrl,
                onUrlChange = { contactViewModel.updateGoogleSheetsUrl(it) },
                onSyncClick = { contactViewModel.triggerBackgroundSync() },
                twoWayUrl = twoWaySyncUrl,
                onTwoWayUrlChange = { contactViewModel.updateTwoWaySyncUrl(it) },
                onPushClick = { contactViewModel.pushContactsToGoogleSheet() },
                syncStatus = syncStatus,
                onDismiss = { showSyncDialog = false },
                isMarathi = isMarathi
            )
        }

        if (showTalukaEditDialogSystem) {
            var tempMr by remember { mutableStateOf(talukaName) }
            var tempEn by remember { mutableStateOf(talukaNameEn) }

            AlertDialog(
                onDismissRequest = { showTalukaEditDialogSystem = false },
                title = {
                    Text(
                        text = if (isMarathi) "तालुका नाव बदला" else "Change Taluka Name",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GBText
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            contactViewModel.updateTalukaName(tempMr, tempEn)
                            showTalukaEditDialogSystem = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GBPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_taluka_name_btn")
                    ) {
                        Text(text = if (isMarathi) "जतन करा" else "Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTalukaEditDialogSystem = false }) {
                        Text(text = if (isMarathi) "रद्द करा" else "Cancel", color = GBGreyText)
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = tempMr,
                            onValueChange = { tempMr = it },
                            label = { Text("तालुका (मराठी)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("taluka_mr_input"),
                            textStyle = TextStyle(fontSize = 13.sp)
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        OutlinedTextField(
                            value = tempEn,
                            onValueChange = { tempEn = it },
                            label = { Text("Taluka (English)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("taluka_en_input"),
                            textStyle = TextStyle(fontSize = 13.sp)
                        )
                    }
                }
            )
        }

        if (showAppNameEditDialogSystem) {
            var tempMr by remember { mutableStateOf(appNameMr) }
            var tempEn by remember { mutableStateOf(appNameEn) }

            AlertDialog(
                onDismissRequest = { showAppNameEditDialogSystem = false },
                title = {
                    Text(
                        text = if (isMarathi) "अ‍ॅपचे नाव बदला" else "Change App Name",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GBText
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            contactViewModel.updateAppName(tempMr, tempEn)
                            showAppNameEditDialogSystem = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GBPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_app_name_btn")
                    ) {
                        Text(text = if (isMarathi) "जतन करा" else "Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAppNameEditDialogSystem = false }) {
                        Text(text = if (isMarathi) "रद्द करा" else "Cancel", color = GBGreyText)
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = tempMr,
                            onValueChange = { tempMr = it },
                            label = { Text("अ‍ॅपचे नाव (मराठी)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("app_name_mr_input"),
                            textStyle = TextStyle(fontSize = 13.sp)
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        OutlinedTextField(
                            value = tempEn,
                            onValueChange = { tempEn = it },
                            label = { Text("App Name (English)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("app_name_en_input"),
                            textStyle = TextStyle(fontSize = 13.sp)
                        )
                    }
                }
            )
        }

        if (showLiveAlertEditDialogSystem) {
            var tempMr by remember { mutableStateOf(liveAlertMr) }
            var tempEn by remember { mutableStateOf(liveAlertEn) }
            var presetDropdownExpanded by remember { mutableStateOf(false) }

            val presets = listOf(
                Triple(
                    if (isMarathi) "⛈️ मुसळधार पाऊस (Heavy Rain Warning)" else "⛈️ Heavy Rain Warning",
                    "मुसळधार पावसाचा इशारा! नदीकाठच्या नागरिकांनी सतर्कता बाळगावी व पूर आल्यास सुरक्षित स्थळी स्थलांतर करावे.",
                    "Heavy rainfall warning! Riverbank residents should stay highly alert and move to safer places in case of flooding."
                ),
                Triple(
                    if (isMarathi) "🌊 पूर परिस्थिती (Flood Warning)" else "🌊 Flood Warning",
                    "पूर परिस्थितीचा इशारा! सखल भागातील लोकांनी तात्काळ सुरक्षित स्थळी स्थलांतर करावे. प्रशासकीय सूचनांचे पालन करा.",
                    "Flood Warning Alert! People in low-lying areas should immediately evacuate to safer places. Follow administrative instructions."
                ),
                Triple(
                    if (isMarathi) "🔔 मॉक ड्रिल / सराव (Mock Drill)" else "🔔 Mock Drill",
                    "आपत्ती व्यवस्थापन मॉक ड्रिल: आज तालुक्यात मॉक ड्रिल सुरू आहे. नागरिकांनी घाबरून जाऊ नये.",
                    "Disaster management mock drill: A mock drill is ongoing in the taluka today. Citizens should not panic."
                ),
                Triple(
                    if (isMarathi) "✅ हवामान सामान्य (Normal Weather)" else "✅ Normal Weather",
                    "हवामान सामान्य आहे. कोणत्याही आपत्कालीन मदतीसाठी आमच्या हेल्पलाइन क्रमांकावर किंवा स्थानिक स्वयंसेवकांशी संपर्क साधा.",
                    "Weather is normal. For any emergency help, contact our helpline numbers or local volunteers."
                ),
                Triple(
                    if (isMarathi) "🔕 इशारा बंद करा (Hide Alert Banner)" else "🔕 Hide Alert Banner",
                    "",
                    ""
                )
            )

            var selectedPresetName by remember { mutableStateOf(
                if (isMarathi) "एक टेंपलेट निवडा..." else "Select a template..."
            ) }

            AlertDialog(
                onDismissRequest = { showLiveAlertEditDialogSystem = false },
                title = {
                    Text(
                        text = if (isMarathi) "ताजा इशारा बदला (Live Alert)" else "Configure Live Alert",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GBText
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            contactViewModel.updateLiveAlert(tempMr, tempEn)
                            showLiveAlertEditDialogSystem = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GBPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_live_alert_btn")
                    ) {
                        Text(text = if (isMarathi) "जतन करा" else "Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLiveAlertEditDialogSystem = false }) {
                        Text(text = if (isMarathi) "रद्द करा" else "Cancel", color = GBGreyText)
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isMarathi) {
                                "ॲपच्या पहिल्या स्क्रीनवर सर्वात वर दिसणारा लाल इशारा बदलण्यासाठी खालीलपैकी एक पर्याय निवडा किंवा स्वतः सानुकूल मेसेज टाईप करा."
                            } else {
                                "Select a pre-configured template below or customize the fields manually. Clear both fields to hide the banner."
                            },
                            fontSize = 11.sp,
                            color = GBGreyText
                        )

                        // Presets Dropdown Selector
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedPresetName,
                                onValueChange = {},
                                label = { Text(if (isMarathi) "इशारा पर्याय (Select Alert Preset)" else "Select Alert Preset", fontSize = 11.sp) },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { presetDropdownExpanded = !presetDropdownExpanded }) {
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "निवडा")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { presetDropdownExpanded = !presetDropdownExpanded },
                                textStyle = TextStyle(fontSize = 13.sp)
                            )
                            DropdownMenu(
                                expanded = presetDropdownExpanded,
                                onDismissRequest = { presetDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                presets.forEach { (name, mrText, enText) ->
                                    DropdownMenuItem(
                                        text = { Text(name, fontSize = 13.sp) },
                                        onClick = {
                                            selectedPresetName = name
                                            tempMr = mrText
                                            tempEn = enText
                                            presetDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = tempMr,
                            onValueChange = { tempMr = it },
                            label = { Text("ताजा इशारा (मराठी)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("live_alert_mr_input"),
                            textStyle = TextStyle(fontSize = 13.sp),
                            minLines = 3,
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        OutlinedTextField(
                            value = tempEn,
                            onValueChange = { tempEn = it },
                            label = { Text("Live Alert (English)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("live_alert_en_input"),
                            textStyle = TextStyle(fontSize = 13.sp),
                            minLines = 3,
                            maxLines = 4
                        )
                    }
                }
            )
        }
    }
}

// ------------------------------------
// UI COMPONENTS DEFINED BELOW
// ------------------------------------

@Composable
fun HeaderSection(
    isMarathi: Boolean,
    talukaName: String,
    talukaNameEn: String,
    appNameMr: String,
    appNameEn: String,
    onLanguageToggle: () -> Unit,
    onCallEmergency: (String) -> Unit,
    onCloudSyncClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("header_card")
            .padding(bottom = 2.dp),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                    )
                )
                .padding(top = 8.dp, bottom = 10.dp, start = 14.dp, end = 14.dp)
        ) {
            // Unified compact header row putting "आपत्ती व्यवस्थापन (Disaster Response)" on top with icons on its sides
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Cloud Sync Icon Button
                IconButton(
                    onClick = onCloudSyncClick,
                    modifier = Modifier.size(36.dp).testTag("cloud_sync_header_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center: Shield icon + Title "आपत्ती व्यवस्थापन"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "आपत्ती नियंत्रण",
                        tint = AmberAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMarathi) appNameMr else appNameEn,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                // Right: Compact Language Toggle Button ("E" or "म")
                Button(
                    onClick = onLanguageToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(28.dp).testTag("language_toggle_btn")
                ) {
                    Text(
                        text = if (isMarathi) "E" else "म",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }

            // Sub-header for Taluka Name, centered and highly compact with Offline Ready status badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .testTag("taluka_name_header_row"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isMarathi) "तालुका - $talukaName" else "Taluka - $talukaNameEn",
                    fontSize = 13.sp,
                    color = Color(0xFFFBBF24),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isMarathi) "ऑफलाईन सज्ज 🟢" else "Offline Ready 🟢",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // SOS EMERGENCY ROW (3 COLUMNS - COLORFUL & PREMIUM)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Local helper data class for custom action buttons
                class EmergencyDialItem(
                    val label: String,
                    val numberLabel: String,
                    val dialNumber: String,
                    val icon: ImageVector,
                    val bgColor: Color,
                    val accentColor: Color,
                    val labelColor: Color
                )

                val emergencyDials = listOf(
                    EmergencyDialItem(
                        label = if (isMarathi) "पोलीस" else "Police",
                        numberLabel = "100",
                        dialNumber = "100",
                        icon = Icons.Default.LocalPolice,
                        bgColor = Color(0xFFEFF6FF), // Soft clean blue
                        accentColor = Color(0xFF2563EB), // Active Blue
                        labelColor = Color(0xFF1E3A8A) // Navy slate
                    ),
                    EmergencyDialItem(
                        label = if (isMarathi) "आरोग्य" else "Medical",
                        numberLabel = "108",
                        dialNumber = "108",
                        icon = Icons.Default.MedicalServices,
                        bgColor = Color(0xFFE6F4EA), // Soft clean green
                        accentColor = Color(0xFF0D9488), // Forest Green/Teal
                        labelColor = Color(0xFF065F46) // Deep Teal
                    ),
                    EmergencyDialItem(
                        label = if (isMarathi) "अग्निशमन" else "Fire",
                        numberLabel = "101",
                        dialNumber = "101",
                        icon = Icons.Default.FireTruck,
                        bgColor = Color(0xFFFEE2E2), // Soft clean red
                        accentColor = Color(0xFFEF4444), // Crimson Red
                        labelColor = Color(0xFF991B1B) // Maroon Deep Red
                    )
                )

                emergencyDials.forEach { item ->
                    Card(
                        onClick = { onCallEmergency(item.dialNumber) },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = item.bgColor),
                        border = BorderStroke(1.2.dp, item.accentColor.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 6.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(item.accentColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = item.accentColor,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = item.label,
                                    color = item.labelColor,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.numberLabel,
                                    color = item.accentColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisasterTipsSection(
    isMarathi: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrow_rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("disaster_tips_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = GBGreyContainer
        ),
        border = BorderStroke(
            width = 1.dp,
            color = GBBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "मार्गदर्शक",
                        tint = GBGreyText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isMarathi) "आपत्कालीन काय करावे? (मार्गदर्शक)" else "Disaster Emergency Survival Guide",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GBGreyText
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "विस्तार करा",
                    tint = GBGreyText,
                    modifier = Modifier.rotate(rotationState)
                )
            }
 
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = GBGreyText.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))
 
                    data class DisasterTip(
                        val title: String,
                        val desc: String,
                        val icon: androidx.compose.ui.graphics.vector.ImageVector
                    )
 
                    val tips = if (isMarathi) {
                        listOf(
                            DisasterTip("पूर सुरक्षा (Flood Safe)", "विद्युतचे मुख्य बटन बंद करा. मौल्यवान सामान व कागदपत्रे वरच्या मजल्यावर हलवा. अफवांवर विश्वास ठेवू नका.", Icons.Default.WaterDrop),
                            DisasterTip("भूकंप सुरक्षा (Earthquake)", "घराबाहेर मोकळ्या जागी जा. आत अडकल्यास भक्कम टेबल किंवा खाटेखाली थांबा, डोक्याचे संरक्षण करा.", Icons.Default.HomeWork),
                            DisasterTip("वीज कोसळणे (Lightning)", "अतिवृष्टीत झाडाखाली किंवा धातूच्या खांबाजवळ थांबू नका. घरातच थांबा, इलेक्ट्रॉनिक उपकरणे प्लग काढू ठेवा.", Icons.Default.ElectricBolt),
                            DisasterTip("वन्यजीव / साप बचाव (Wildlife)", "वन विभागाशी तातडीने संपर्क करा. वन्यजीवावर हल्ला करू नका. तोपर्यंत गर्दी करू नका व परिसर शांत ठेवा.", Icons.Default.Pets)
                        )
                    } else {
                        listOf(
                            DisasterTip("Flood Protocol", "Turn off primary electricity switch. Move important documents to upper floors. Follow reliable admin channels.", Icons.Default.WaterDrop),
                            DisasterTip("Earthquake Drill", "Evacuate buildings safely to open fields. If trapped, drop-cover-hold under hard wooden tables.", Icons.Default.HomeWork),
                            DisasterTip("Lightning Alert", "Do not take shelter under trees, metallic poles, or machinery. Unplug power systems inside shelters.", Icons.Default.ElectricBolt),
                            DisasterTip("Snakes & Wild Animals", "Contact Forest division immediately (RFO). Do not harm/corner wild beasts. Keep distance, reduce noise.", Icons.Default.Pets)
                        )
                    }
 
                    tips.forEach { tip ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(GBGreyText.copy(alpha = 0.15f), CircleShape)
                                    .padding(6.dp)
                            ) {
                                Icon(
                                    imageVector = tip.icon,
                                    contentDescription = tip.title,
                                    tint = GBGreyText,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = tip.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GBText
                                )
                                Text(
                                    text = tip.desc,
                                    fontSize = 11.sp,
                                    color = GBGreyText,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchAndFilterSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isMarathi: Boolean,
    contactCount: Int,
    onVoiceSearchClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text(
                    text = if (isMarathi) "नाव, पद, गाव शोधा..." else "Search name, post, village...",
                    fontSize = 13.sp,
                    color = GBGreyText.copy(alpha = 0.7f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "शोध",
                    tint = GBPrimary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    IconButton(
                        onClick = onVoiceSearchClick,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("voice_search_mic_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = if (isMarathi) "आवाज शोध" else "Voice Search",
                            tint = Color(0xFF0D9488), // Beautiful teal for voice
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { onSearchChange("") },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "शोध रद्द करा",
                                tint = GBGreyText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            },
            maxLines = 1,
            singleLine = true,
            textStyle = TextStyle(fontSize = 13.sp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GBPrimary,
                unfocusedBorderColor = GBBorder,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                focusedTextColor = GBText,
                unfocusedTextColor = GBText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("search_input")
        )
    }
}

@Composable
fun CategoryFilterBar(
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    isMarathi: Boolean
) {
    val categories = listOf(
        Pair("ADMIN", if (isMarathi) "🏛️ नियंत्रण कक्ष व प्रशासन" else "Control Room & Admin"),
        Pair("POLICE", if (isMarathi) "🚨 पोलीस व सुरक्षा" else "Police & Security"),
        Pair("FIRE", if (isMarathi) "🚒 अग्निशमन सेवा" else "Fire Brigade"),
        Pair("MEDICAL", if (isMarathi) "🏥 वैद्यकीय व रुग्णवाहिका" else "Medical & Ambulance"),
        Pair("RESCUE", if (isMarathi) "🛟 बचाव दल व पोहणारे" else "Rescue & Swimmers"),
        Pair("MACHINERY", if (isMarathi) "🚜 जेसीबी व यंत्रसामग्री" else "JCB & Machinery"),
        Pair("UTILITY", if (isMarathi) "⚡ वीज, पाणी व रस्ते" else "Electricity & Water"),
        Pair("RELIEF", if (isMarathi) "📦 अन्न, निवारा व मदत" else "Food, Shelter & Relief"),
        Pair("VOLUNTEER", if (isMarathi) "🤝 स्वयंसेवक गट" else "Volunteers"),
        Pair("ALL", if (isMarathi) "🔍 इतर / सर्व विभाग" else "Others / All Categories")
    )

    var dropdownExpanded by remember { mutableStateOf(false) }

    val activePair = categories.find { it.first == selectedCategory } ?: Pair("ALL", if (isMarathi) "सर्व विभाग" else "All Categories")
    val activeLabel = activePair.second

    val activeIcon = when (selectedCategory) {
        "RESCUE" -> Icons.Default.Waves
        "MEDICAL" -> Icons.Default.LocalHospital
        "ADMIN" -> Icons.Default.AccountBalance
        "POLICE" -> Icons.Default.LocalPolice
        "FIRE" -> Icons.Default.FireTruck
        "MACHINERY" -> Icons.Default.LocalShipping
        "UTILITY" -> Icons.Default.FlashOn
        "RELIEF" -> Icons.Default.Home
        "VOLUNTEER" -> Icons.Default.People
        else -> Icons.Default.List
    }

    val activeColor = when (selectedCategory) {
        "RESCUE" -> Color(0xFFD97706) // Amber
        "MEDICAL" -> Color(0xFF0D9488) // Teal
        "ADMIN" -> Color(0xFF8B5CF6) // Purple
        "POLICE" -> Color(0xFF2563EB) // Blue
        "FIRE" -> Color(0xFFEF4444) // Red
        "MACHINERY" -> Color(0xFFE65100) // Dark Orange
        "UTILITY" -> Color(0xFFF57F17) // Dark Yellow
        "RELIEF" -> Color(0xFF64748B) // Slate/Grey
        "VOLUNTEER" -> Color(0xFF10B981) // Green
        else -> GBPrimary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .testTag("category_filter_row")
    ) {
        // Active State Display Card with dropdown trigger
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { dropdownExpanded = true }
                .testTag("category_dropdown_trigger_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedCategory != "ALL") activeColor.copy(alpha = 0.08f) else Color.White
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (selectedCategory != "ALL") activeColor.copy(alpha = 0.5f) else GBBorder
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = activeIcon,
                        contentDescription = null,
                        tint = if (selectedCategory != "ALL") activeColor else GBText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isMarathi) "सक्रिय आपत्कालीन विभाग :-" else "Active Emergency Category :-",
                            fontSize = 11.sp,
                            color = GBGreyText
                        )
                        Text(
                            text = activeLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedCategory != "ALL") activeColor else GBText
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Clear Button
                    if (selectedCategory != "ALL") {
                        IconButton(
                            onClick = { onCategorySelect("ALL") },
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFFFFEBEE), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Selection",
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand",
                        tint = if (selectedCategory != "ALL") activeColor else GBText
                    )
                }
            }
        }

        // Dropdown Menu List
        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color.White)
                .padding(vertical = 4.dp)
                .testTag("category_dropdown_menu")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isMarathi) "आपत्कालीन विभाग निवडा (Select Emergency Category):" else "Select Emergency Category:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GBPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                categories.forEach { (key, label) ->
                    val isCurrent = selectedCategory == key
                    val itemIcon = when (key) {
                        "RESCUE" -> Icons.Default.Waves
                        "MEDICAL" -> Icons.Default.LocalHospital
                        "ADMIN" -> Icons.Default.AccountBalance
                        "POLICE" -> Icons.Default.LocalPolice
                        "FIRE" -> Icons.Default.FireTruck
                        "MACHINERY" -> Icons.Default.LocalShipping
                        "UTILITY" -> Icons.Default.FlashOn
                        "RELIEF" -> Icons.Default.Home
                        "VOLUNTEER" -> Icons.Default.People
                        else -> Icons.Default.List
                    }
                    val itemColor = when (key) {
                        "RESCUE" -> Color(0xFFD97706)
                        "MEDICAL" -> Color(0xFF0D9488)
                        "ADMIN" -> Color(0xFF8B5CF6)
                        "POLICE" -> Color(0xFF2563EB)
                        "FIRE" -> Color(0xFFEF4444)
                        "MACHINERY" -> Color(0xFFE65100)
                        "UTILITY" -> Color(0xFFF57F17)
                        "RELIEF" -> Color(0xFF64748B)
                        "VOLUNTEER" -> Color(0xFF10B981)
                        else -> GBPrimary
                    }

                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = itemIcon,
                                    contentDescription = null,
                                    tint = itemColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) itemColor else GBText
                                )
                            }
                        },
                        onClick = {
                            onCategorySelect(key)
                            dropdownExpanded = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isCurrent) itemColor.copy(alpha = 0.05f) else Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
fun VillageFilterBar(
    availableVillages: List<String>,
    selectedVillage: String,
    onVillageSelect: (String) -> Unit,
    isMarathi: Boolean
) {
    if (availableVillages.size <= 1) return

    var dropdownExpanded by remember { mutableStateOf(false) }
    var villageSearchQuery by remember { mutableStateOf("") }

    // Filter available villages based on search query in the dropdown for instant lookup
    val filteredVillages = remember(availableVillages, villageSearchQuery) {
        if (villageSearchQuery.isBlank()) {
            availableVillages
        } else {
            availableVillages.filter {
                it.contains(villageSearchQuery, ignoreCase = true) ||
                it.equals("ALL", ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .testTag("village_filter_container")
    ) {
        // Active State Display Card with dropdown trigger
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("village_dropdown_trigger_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedVillage != "ALL") Color(0xFFE8F5E9) else Color.White
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (selectedVillage != "ALL") Color(0xFFA5D6A7) else GBBorder
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { dropdownExpanded = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (selectedVillage != "ALL") Icons.Default.Place else Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = if (selectedVillage != "ALL") Color(0xFF2E7D32) else GBText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (selectedVillage == "ALL") {
                                if (isMarathi) "सक्रिय गाव :- सर्व" else "Active Village :- All"
                            } else {
                                if (isMarathi) "सक्रिय गाव :- $selectedVillage" else "Active Village :- $selectedVillage"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedVillage != "ALL") Color(0xFF2E7D32) else GBText
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Reset / Clear button so they don't have to reopen dropdown to reset
                    if (selectedVillage != "ALL") {
                        IconButton(
                            onClick = { onVillageSelect("ALL") },
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFFFFEBEE), CircleShape)
                                .testTag("clear_village_filter_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Selection",
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand",
                        tint = if (selectedVillage != "ALL") Color(0xFF2E7D32) else GBText
                    )
                }
            }
        }

        // Popup Dropdown Menu
        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = {
                dropdownExpanded = false
                villageSearchQuery = ""
            },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color.White)
                .padding(vertical = 4.dp)
                .testTag("village_dropdown_menu")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isMarathi) "गाव निवडा किंवा फिल्टर शोधा:" else "Select or Search Village:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GBPrimary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Inline Search Bar inside to quickly type and narrow down options
                OutlinedTextField(
                    value = villageSearchQuery,
                    onValueChange = { villageSearchQuery = it },
                    label = {
                        Text(
                            text = if (isMarathi) "गावाचे नाव टाईप करा" else "Type village name",
                            fontSize = 13.sp
                        )
                    },
                    placeholder = {
                        Text(
                            text = if (isMarathi) "उदा. मंगळवेढा, जुनोनी..." else "e.g. Mangalwedha, Junoni...",
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("village_dropdown_search_field"),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GBPrimary,
                        unfocusedBorderColor = GBBorder,
                        focusedLabelColor = GBPrimary,
                        unfocusedLabelColor = GBGreyText
                    ),
                    trailingIcon = {
                        if (villageSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { villageSearchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = GBGreyText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
            }

            Divider(color = GBBorder, modifier = Modifier.padding(vertical = 4.dp))

            // Scrollable list container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
            ) {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    if (filteredVillages.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isMarathi) "या नावाचे गाव आढळले नाही" else "No matches found",
                                    fontSize = 12.sp,
                                    color = Color.LightGray
                                )
                            },
                            onClick = {},
                            enabled = false
                        )
                    } else {
                        filteredVillages.forEach { villageKey ->
                            val isSelected = selectedVillage == villageKey
                            val label = if (villageKey == "ALL") {
                                if (isMarathi) "🔄 सर्व गाव दाखवा (Clear Filter)" else "🔄 Show All Villages"
                            } else {
                                villageKey
                            }

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) GBPrimary else GBText
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = GBPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onVillageSelect(villageKey)
                                    dropdownExpanded = false
                                    villageSearchQuery = ""
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("village_menu_item_$villageKey"),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun isTalukaLevelContact(contact: EmergencyContact): Boolean {
    val village = contact.villageOrAreaMr.trim()
    val villageEn = contact.villageOrAreaEn.trim()
    return village.contains("सर्व") || village.contains("तालुका") || 
           villageEn.contains("All") || villageEn.contains("Taluka") || 
           village.isEmpty() || village == "Tehsil Office (HQ)" || village == "सर्व तालुका विभाग"
}

data class AlertStyle(
    val backgroundColor: Color,
    val textColor: Color,
    val titleColor: Color,
    val borderColor: Color,
    val iconColor: Color,
    val iconBgColor: Color
)

private fun getAlertStyle(text: String): AlertStyle {
    val lower = text.lowercase()
    return when {
        lower.contains("सामान्य") || lower.contains("normal") || lower.contains("उत्तम") || lower.contains("clear") || lower.contains("ok") -> {
            AlertStyle(
                backgroundColor = Color(0xFFF0FDF4), // Green 50
                textColor = Color(0xFF166534),       // Green 800
                titleColor = Color(0xFF15803D),      // Green 700
                borderColor = Color(0xFFBBF7D0),     // Green 200
                iconColor = Color(0xFF16A34A),       // Green 600
                iconBgColor = Color(0xFFDCFCE7)      // Green 100
            )
        }
        lower.contains("मॉक") || lower.contains("drill") || lower.contains("सराव") -> {
            AlertStyle(
                backgroundColor = Color(0xFFEFF6FF), // Blue 50
                textColor = Color(0xFF1E40AF),       // Blue 800
                titleColor = Color(0xFF1D4ED8),      // Blue 700
                borderColor = Color(0xFFBFDBFE),     // Blue 200
                iconColor = Color(0xFF2563EB),       // Blue 600
                iconBgColor = Color(0xFFDBEAFE)      // Blue 100
            )
        }
        lower.contains("पाऊस") || lower.contains("rain") || lower.contains("वारा") || lower.contains("इशारा") || lower.contains("warning") || lower.contains("सतर्क") || lower.contains("alert") -> {
            AlertStyle(
                backgroundColor = Color(0xFFFFFBEB), // Amber 50
                textColor = Color(0xFF78350F),       // Amber 900
                titleColor = Color(0xFF92400E),      // Amber 800
                borderColor = Color(0xFFFDE68A),     // Amber 300
                iconColor = Color(0xFFD97706),       // Amber 600
                iconBgColor = Color(0xFFFEF3C7)      // Amber 100
            )
        }
        else -> {
            // High Alert / Flood
            AlertStyle(
                backgroundColor = Color(0xFFFEF2F2), // Red 50
                textColor = Color(0xFF7F1D1D),       // Red 900
                titleColor = Color(0xFF991B1B),      // Red 800
                borderColor = Color(0xFFFCA5A5),     // Red 300
                iconColor = Color(0xFFDC2626),       // Red 600
                iconBgColor = Color(0xFFFEE2E2)      // Red 100
            )
        }
    }
}

@Composable
fun LiveAlertBanner(
    isMarathi: Boolean,
    liveAlertMr: String,
    liveAlertEn: String,
    isDismissed: Boolean,
    onDismissClick: () -> Unit
) {
    if (liveAlertMr.isBlank() && liveAlertEn.isBlank()) return
    if (isDismissed) return

    val text = if (isMarathi) liveAlertMr else liveAlertEn
    val style = remember(text) { getAlertStyle(text) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("live_alert_banner_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = style.backgroundColor
        ),
        border = BorderStroke(1.dp, style.borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(style.iconBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = "Alert",
                    tint = style.iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                     text = if (isMarathi) "📢 ताजा इशारा (Live Alert)" else "📢 Live Alert Bulletin",
                     fontSize = 11.sp,
                     fontWeight = FontWeight.Black,
                     color = style.titleColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                     text = text,
                     fontSize = 12.sp,
                     fontWeight = FontWeight.SemiBold,
                     color = style.textColor,
                     lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    
                    // Damini Button
                    Surface(
                        onClick = { 
                            try {
                                uriHandler.openUri("https://web.umang.gov.in/web_new/department?url=damini%2Fservice%2F1636&dept_id=357&dept_name=Damini%20-%20Lightning%20Alert")
                            } catch (e: Exception) {}
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFF7ED),
                        border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Damini Lightning Alert",
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isMarathi) "दामिनी वीज इशारा ⚡" else "Damini Lightning ⚡",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC2410C)
                            )
                        }
                    }

                    // Sachet Button
                    Surface(
                        onClick = { 
                            try {
                                uriHandler.openUri("https://sachet.ndma.gov.in/")
                            } catch (e: Exception) {}
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Sachet Alert Portal",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isMarathi) "सचेत राष्ट्रीय इशारा 🛡️" else "Sachet Alert 🛡️",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onDismissClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = style.titleColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun ListSectionHeader(
    title: String,
    count: Int,
    isMarathi: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = GBPrimary,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .background(GBPrimary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$count ${if (isMarathi) "संपर्क" else "Contacts"}",
                fontSize = 10.sp,
                color = GBPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ContactsListArea(
    contacts: List<EmergencyContact>,
    expandedContactId: Int?,
    onContactClick: (Int) -> Unit,
    onCallClick: (String) -> Unit,
    onShareClick: (EmergencyContact) -> Unit,
    onEditClick: (EmergencyContact) -> Unit,
    onDeleteClick: (EmergencyContact) -> Unit,
    isMarathi: Boolean,
    selectedScope: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onLanguageToggle: () -> Unit,
    onCallEmergency: (String) -> Unit,
    onScopeSelect: (String) -> Unit,
    availableVillages: List<String>,
    selectedVillage: String,
    onVillageSelect: (String) -> Unit,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    onCloudSyncClick: () -> Unit,
    talukaName: String,
    talukaNameEn: String,
    appNameMr: String,
    appNameEn: String,
    favoriteContactIds: Set<Int> = emptySet(),
    onFavoriteToggle: (Int) -> Unit = {},
    liveAlertMr: String,
    liveAlertEn: String,
    isLiveAlertDismissed: Boolean = false,
    onDismissLiveAlert: () -> Unit = {},
    isLiveAlertEnabled: Boolean = true,
    isAdminMode: Boolean = false,
    isOnline: Boolean,
    syncStatus: String,
    lastSyncedTime: String,
    onRegisterVolunteerClick: () -> Unit,
    onVoiceSearchClick: () -> Unit = {}
) {
    val talukaContacts = contacts.filter { isTalukaLevelContact(it) }
    val villageContacts = contacts.filter { !isTalukaLevelContact(it) }
    val favoriteContacts = contacts.filter { favoriteContactIds.contains(it.id) }
    var showFirstAidDialog by remember { mutableStateOf(false) }

    if (showFirstAidDialog) {
        FirstAidDialog(
            isMarathi = isMarathi,
            onDismiss = { showFirstAidDialog = false }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- FROZEN / FIXED TOP REGION (Does not scroll) ---
        // 1. Header Section
        HeaderSection(
            isMarathi = isMarathi,
            talukaName = talukaName,
            talukaNameEn = talukaNameEn,
            appNameMr = appNameMr,
            appNameEn = appNameEn,
            onLanguageToggle = onLanguageToggle,
            onCallEmergency = onCallEmergency,
            onCloudSyncClick = onCloudSyncClick
        )

        // --- SCROLLING LIST AREA ---
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("contacts_lazy_column"),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Live Alert Banner Section
            item {
                if (isLiveAlertEnabled) {
                    LiveAlertBanner(
                        isMarathi = isMarathi,
                        liveAlertMr = liveAlertMr,
                        liveAlertEn = liveAlertEn,
                        isDismissed = isLiveAlertDismissed,
                        onDismissClick = onDismissLiveAlert
                    )
                }
            }

            // 🚨 EMERGENCY TOOLKIT (SOS & FIRST AID)
            item {
                EmergencyToolkitRow(
                    isMarathi = isMarathi,
                    onOpenFirstAid = { showFirstAidDialog = true }
                )
            }

            // Starred / Favorite Contacts Section
            if (favoriteContacts.isNotEmpty()) {
                item {
                    FavoriteContactsRow(
                        favoriteContacts = favoriteContacts,
                        isMarathi = isMarathi,
                        onCallClick = onCallClick,
                        onContactClick = onContactClick
                    )
                }
            }

            // 2. Search & Filter Section (Moved inside LazyColumn to scroll)
            item {
                SearchAndFilterSection(
                    searchQuery = searchQuery,
                    onSearchChange = onSearchChange,
                    isMarathi = isMarathi,
                    contactCount = contacts.size,
                    onVoiceSearchClick = onVoiceSearchClick
                )
            }

            // 3. Scope Level Selector (Moved inside LazyColumn to scroll)
            item {
                ScopeLevelSelector(
                    selectedScope = selectedScope,
                    onScopeSelect = onScopeSelect,
                    isMarathi = isMarathi
                )
            }

            // 4. Village Filter Bar (Only of scope != TALUKA) (Moved inside LazyColumn to scroll)
            if (selectedScope != "TALUKA") {
                item {
                    VillageFilterBar(
                        availableVillages = availableVillages,
                        selectedVillage = selectedVillage,
                        onVillageSelect = onVillageSelect,
                        isMarathi = isMarathi
                    )
                }
            }

            // 5. Category Filter Bar (Moved inside LazyColumn to scroll)
            item {
                CategoryFilterBar(
                    selectedCategory = selectedCategory,
                    onCategorySelect = onCategorySelect,
                    isMarathi = isMarathi
                )
            }

            if (selectedCategory == "Volunteers") {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("volunteer_registration_banner"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4EA)), // beautiful light green
                        border = BorderStroke(1.5.dp, Color(0xFFA3E635)), // Lime/green border
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Pets, // Volunteer icon
                                    contentDescription = "Volunteer icon",
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isMarathi) "🤝 आपत्ती मदत स्वयंसेवक व्हा!" else "🤝 Become a Disaster Volunteer!",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF065F46)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isMarathi) 
                                    "आपल्या तालुक्यात पूर, भूकंप किंवा कोणत्याही संकटाच्या वेळी मदत करण्यासाठी आपली नोंदणी करा. प्रशासक आपले नाव तपासून यादीत समाविष्ट करतील."
                                else 
                                    "Register to help during floods, earthquakes, or any emergency in your taluka. Admins will verify and add your contact details.",
                                fontSize = 11.sp,
                                color = Color(0xFF047857),
                                lineHeight = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onRegisterVolunteerClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .testTag("volunteer_register_button")
                            ) {
                                Text(
                                    text = if (isMarathi) "स्वयंसेवक नोंदणी फॉर्म भरा" else "Fill Volunteer Registration Form",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Check if list is empty
            if (contacts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = "काहीही सापडले नाही",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isMarathi) "क्षमस्व, शोधलेले संपर्क मिळाले नाहीत." else "No emergency contacts match your search query.",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isMarathi) 
                                    "कृपया स्पेलिंग तपासा किंवा सर्च बॉक्स रिकामी करा." 
                                else 
                                    "Check your keywords or clear your search query.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            } else {
                if (selectedScope == "ALL" || selectedScope == "TALUKA") {
                    if (talukaContacts.isNotEmpty()) {
                        item {
                            ListSectionHeader(
                                title = if (isMarathi) "🏛️ तालुका स्तर नियंत्रण कक्ष व प्रमुख अधिकारी" else "🏛️ Taluka Level Controls & Officers",
                                count = talukaContacts.size,
                                isMarathi = isMarathi
                            )
                        }
                        items(talukaContacts, key = { "taluka_${it.id}" }) { contact ->
                            val isExpanded = expandedContactId == contact.id
                            ContactItem(
                                contact = contact,
                                isExpanded = isExpanded,
                                isFavorite = favoriteContactIds.contains(contact.id),
                                onFavoriteClick = { onFavoriteToggle(contact.id) },
                                onHeaderClick = { onContactClick(contact.id) },
                                onCallClick = onCallClick,
                                onShareClick = { onShareClick(contact) },
                                onEditClick = { onEditClick(contact) },
                                onDeleteClick = { onDeleteClick(contact) },
                                isMarathi = isMarathi,
                                isAdmin = isAdminMode
                            )
                        }
                    }
                }

                if (selectedScope == "ALL" || selectedScope == "VILLAGE") {
                    if (villageContacts.isNotEmpty()) {
                        item {
                            ListSectionHeader(
                                title = if (isMarathi) "🏡 ग्राम स्तर कर्मचारी, मदत समन्वयक व स्वयंसेवक" else "🏡 Village Level Officials & Lifeguards",
                                count = villageContacts.size,
                                isMarathi = isMarathi
                            )
                        }
                        items(villageContacts, key = { "village_${it.id}" }) { contact ->
                            val isExpanded = expandedContactId == contact.id
                            ContactItem(
                                contact = contact,
                                isExpanded = isExpanded,
                                isFavorite = favoriteContactIds.contains(contact.id),
                                onFavoriteClick = { onFavoriteToggle(contact.id) },
                                onHeaderClick = { onContactClick(contact.id) },
                                onCallClick = onCallClick,
                                onShareClick = { onShareClick(contact) },
                                onEditClick = { onEditClick(contact) },
                                onDeleteClick = { onDeleteClick(contact) },
                                isMarathi = isMarathi,
                                isAdmin = isAdminMode
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactItem(
    contact: EmergencyContact,
    isExpanded: Boolean,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onHeaderClick: () -> Unit,
    onCallClick: (String) -> Unit,
    onShareClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isMarathi: Boolean,
    isAdmin: Boolean = false
) {
    // Determine category badge attributes and iconography mapped to unified high-contrast categories
    val (categoryLabel, iconBg, iconTint, categoryIcon) = when (contact.category.uppercase()) {
        "POLICE" -> quadruplet(
            if (isMarathi) "👮 पोलीस व सुरक्षा विभाग" else "👮 Police & Security",
            Color(0xFFEFF6FF), // Soft clean blue
            Color(0xFF2563EB), // Blue
            Icons.Default.LocalPolice
        )
        "MEDICAL" -> quadruplet(
            if (isMarathi) "🏥 वैद्यकीय व रुग्णवाहिका" else "🏥 Medical & Ambulance",
            Color(0xFFE0F2F1), // Soft teal
            Color(0xFF00796B), // Dark green teal
            Icons.Default.LocalHospital
        )
        "FIRE" -> quadruplet(
            if (isMarathi) "🚒 अग्निशमन सेवा" else "🚒 Fire Brigade",
            Color(0xFFFEE2E2), // Soft red
            Color(0xFFDC2626), // Fire red
            Icons.Default.FireTruck
        )
        "RESCUE" -> quadruplet(
            if (isMarathi) "🛟 बचाव दल व पोहणारे" else "🛟 Rescue & Swimmers",
            Color(0xFFFEF3C7), // Soft amber
            Color(0xFFD97706), // Accent amber
            Icons.Default.Waves
        )
        "ADMIN" -> quadruplet(
            if (isMarathi) "🏛️ नियंत्रण कक्ष व प्रशासन" else "🏛️ Control Room & Admin",
            Color(0xFFF3E8FF), // Soft purple
            Color(0xFF7C3AED), // Indigo purple
            Icons.Default.AccountBalance
        )
        "MACHINERY" -> quadruplet(
            if (isMarathi) "🚜 जेसीबी व यंत्रसामग्री" else "🚜 JCB & Machinery",
            Color(0xFFFFF3E0), // Soft orange
            Color(0xFFE65100), // Dark orange
            Icons.Default.LocalShipping
        )
        "UTILITY" -> quadruplet(
            if (isMarathi) "⚡ वीज, पाणी व रस्ते" else "⚡ Electricity & Water",
            Color(0xFFFFFDE7), // Soft yellow
            Color(0xFFF57F17), // Dark yellow
            Icons.Default.FlashOn
        )
        "RELIEF" -> quadruplet(
            if (isMarathi) "📦 अन्न, निवारा व मदत कार्य" else "📦 Food, Shelter & Relief",
            Color(0xFFF1F5F9), // Soft slate
            Color(0xFF475569), // Slate
            Icons.Default.Home
        )
        "VOLUNTEER" -> quadruplet(
            if (isMarathi) "🤝 स्वयंसेवक गट" else "🤝 Volunteer Group",
            Color(0xFFE8F5E9), // Soft green
            Color(0xFF2E7D32), // Green
            Icons.Default.People
        )
        else -> quadruplet(
            if (isMarathi) "🏡 इतर आपत्कालीन संपर्क" else "🏡 Other Emergency Contact",
            Color(0xFFECFDF5), // Soft green
            Color(0xFF059669), // Emerald
            Icons.Default.People
        )
    }
    val context = LocalContext.current

    val isTalukaLevel = isTalukaLevelContact(contact)
    val cardBgColor = if (isTalukaLevel) Color(0xFFFFF1F2) else Color.White // Soft pink/rose background
    val cardBorderColor = if (isTalukaLevel) Color(0xFFFECDD3) else GBBorder // Soft pink border
    val cardElevationDp = if (isTalukaLevel) 2.dp else 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("contact_card_${contact.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor
        ),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevationDp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onHeaderClick() }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT SIDE ICON CONTAINER
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(iconBg, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = categoryLabel,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // MIDDLE METADATA COLUMN (Two lines format: Name on line 1, Designation/village on line 2)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Line 1: Full name and Verification Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isMarathi) contact.nameMr else contact.nameEn,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GBText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (contact.isDefault) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "शासकीय संपर्क",
                                tint = GBPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        if (contact.isPending) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = Color(0xFFFEF3C7), // Light amber/yellow
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, Color(0xFFFCD34D))
                            ) {
                                Text(
                                    text = if (isMarathi) "⏳ पडताळणी प्रलंबित" else "⏳ Pending Verification",
                                    color = Color(0xFFD97706), // Dark amber
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        if (contact.isPendingDelete) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, Color(0xFFFCA5A5))
                            ) {
                                Text(
                                    text = if (isMarathi) "हटवण्यासाठी प्रलंबित" else "Pending Deletion",
                                    color = Color(0xFFDC2626),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    // Line 2: Designation and Village/Area info
                    val des = if (isMarathi) contact.designationMr else contact.designationEn
                    val vil = if (isMarathi) contact.villageOrAreaMr else contact.villageOrAreaEn
                    val detailText = buildString {
                        if (des.isNotBlank()) {
                            append(des)
                        }
                        if (vil.isNotBlank() && vil != "All" && vil != "ALL" && vil != "Taluka") {
                            if (des.isNotBlank()) {
                                append(" ($vil)")
                            } else {
                                append(vil)
                            }
                        }
                    }
                    if (detailText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = detailText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = GBGreyText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Favorite Star Toggle Button
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("star_button_${contact.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFFBBF24) else GBGreyText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // RIGHT ACTION DIAL BUTTON
                if (contact.phone.isNotBlank()) {
                    IconButton(
                        onClick = { onCallClick(contact.phone) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = GBPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("dial_button_${contact.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "कॉल करा",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Box(modifier = Modifier.size(40.dp))
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Divider(color = GBBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (contact.phone.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GBPrimary.copy(alpha = 0.08f),
                                modifier = Modifier.clickable { onCallClick(contact.phone) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "कॉल",
                                        tint = GBPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = contact.phone,
                                        color = GBPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GBGreyText.copy(alpha = 0.08f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhoneCallback,
                                        contentDescription = "बंद",
                                        tint = GBGreyText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isMarathi) "क्रमांक उपलब्ध नाही" else "No phone available",
                                        color = GBGreyText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(iconBg, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = categoryLabel,
                                color = iconTint,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (contact.phone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val cleanPhone = remember(contact.phone) { contact.phone.filter { it.isDigit() } }
                            val formattedPhone = remember(cleanPhone) { if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone }

                            // WhatsApp Message Button
                            Button(
                                onClick = {
                                    try {
                                        val waText = if (isMarathi) {
                                            "नमस्कार, मी सोलापूर तालुका आपत्कालीन डिरेक्टरी ॲपमधून संपर्क करत आहे."
                                        } else {
                                            "Hello, contacting you from Solapur Taluka Emergency Directory App."
                                        }
                                        val waUri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(waText)}")
                                        val waIntent = Intent(Intent.ACTION_VIEW, waUri)
                                        context.startActivity(waIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, if (isMarathi) "व्हाट्सॲप उघडता आले नाही!" else "Could not open WhatsApp!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("whatsapp_btn_${contact.id}"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "WhatsApp",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isMarathi) "व्हाट्सॲप" else "WhatsApp",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Video Call Button
                            Button(
                                onClick = {
                                    try {
                                        val videoIntent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${contact.phone}")
                                            putExtra("android.telecom.extra.START_CALL_WITH_VIDEO_STATE", 3)
                                        }
                                        context.startActivity(videoIntent)
                                    } catch (e: Exception) {
                                        try {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                                            context.startActivity(dialIntent)
                                        } catch (ex: Exception) {
                                            Toast.makeText(context, "कॉल करता आला नाही", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)), // Teal video call
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("video_call_btn_${contact.id}"),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = "Video Call",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isMarathi) "व्हिडिओ कॉल" else "Video Call",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (contact.phoneAlt.isNotBlank() && contact.phoneAlt != contact.phone) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(
                            label = if (isMarathi) "पर्यायी नंबर:" else "Alt Phone:",
                            value = contact.phoneAlt,
                            isPhone = true,
                            onPhoneClick = {
                                onCallClick(contact.phoneAlt)
                            }
                        )
                    }

                    if (contact.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(
                            label = if (isMarathi) "टीप / माहिती:" else "Information:",
                            value = contact.notes,
                            isPhone = false
                        )

                        val mapsUrl = remember(contact.notes) {
                            val regex = """(https?://[^\s,]+)""".toRegex()
                            val match = regex.find(contact.notes)
                            match?.value
                        }

                        if (mapsUrl != null) {
                            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                            val context = androidx.compose.ui.platform.LocalContext.current
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    try {
                                        uriHandler.openUri(mapsUrl)
                                    } catch (e: Exception) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))
                                            context.startActivity(intent)
                                        } catch (ex: Exception) {
                                            // Fallback
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .testTag("contact_maps_button_${contact.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Maps",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isMarathi) "गुगल मॅप लोकेशन (Google Maps Link)" else "View Location on Google Maps",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onShareClick,
                            colors = ButtonDefaults.textButtonColors(contentColor = GBPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "शेअर",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isMarathi) "शेअर करा" else "Share Details",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = onEditClick,
                                modifier = Modifier.testTag("edit_contact_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "सुधारा",
                                    tint = GBPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            var showDeleteConfirmation by remember { mutableStateOf(false) }

                            IconButton(onClick = { showDeleteConfirmation = true }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "काढा",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            if (showDeleteConfirmation) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteConfirmation = false },
                                    title = {
                                        Text(
                                            text = if (isMarathi) {
                                                if (isAdmin) "हा संपर्क डिलीट करायचा?" else "संपर्क डिलीट करण्याची विनंती?"
                                            } else {
                                                if (isAdmin) "Delete this contact?" else "Request Deletion of Contact?"
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = if (isMarathi) {
                                                if (isAdmin) {
                                                    if (contact.isDefault) "हा शासकीय संपर्क आहे! याला काढून टाकण्याऐवजी आपण नवीन सुधारणा करू शकता." else "हा संपर्क माहितीकोषातून कायमचा डिलीट केला जाईल. पुढे जायचे?"
                                                } else {
                                                    "आपली हा संपर्क हटवण्याची विनंती प्रशासकाकडे पाठवली जाईल. प्रशासकीय मंजुरीनंतर तो कायमचा हटवला जाईल. पुढे जायचे?"
                                                }
                                            } else {
                                                if (isAdmin) {
                                                    if (contact.isDefault) "This is an administrative emergency contact! Deleting it is not recommended." else "This contact will be permanently deleted from local cache. Continue?"
                                                } else {
                                                    "Your deletion request will be submitted to the administrator. This contact will be permanently removed once approved. Continue?"
                                                }
                                            }
                                        )
                                    },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                onDeleteClick()
                                                showDeleteConfirmation = false
                                            },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Text(
                                                text = if (isMarathi) {
                                                    if (isAdmin) "होय, डिलीट करा" else "विनंती पाठवा"
                                                } else {
                                                    if (isAdmin) "Yes, Delete" else "Send Request"
                                                }
                                            )
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteConfirmation = false }) {
                                            Text(if (isMarathi) "रद्द करा" else "Cancel")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class Quadruplet<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
fun <A, B, C, D> quadruplet(a: A, b: B, c: C, d: D) = Quadruplet(a, b, c, d)

@Composable
fun FavoriteContactsRow(
    favoriteContacts: List<EmergencyContact>,
    isMarathi: Boolean,
    onCallClick: (String) -> Unit,
    onContactClick: (Int) -> Unit
) {
    if (favoriteContacts.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isMarathi) "⭐ आवडते संपर्क (Quick Dial)" else "⭐ Starred Favorites (Quick Dial)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = GBText
            )
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("favorite_contacts_row"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(favoriteContacts) { contact ->
                Card(
                    onClick = { onContactClick(contact.id) },
                    modifier = Modifier
                        .width(170.dp)
                        .height(86.dp)
                        .testTag("favorite_card_${contact.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = BorderStroke(1.2.dp, Color(0xFF93C5FD))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isMarathi) contact.nameMr else contact.nameEn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GBText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val designation = if (isMarathi) contact.designationMr else contact.designationEn
                            if (designation.isNotBlank()) {
                                Text(
                                    text = designation,
                                    fontSize = 9.sp,
                                    color = GBGreyText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onCallClick(contact.phone) },
                                colors = ButtonDefaults.buttonColors(containerColor = GBPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(26.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call",
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isMarathi) "कॉल" else "Call",
                                        fontSize = 9.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyToolkitRow(
    isMarathi: Boolean,
    onOpenFirstAid: () -> Unit
) {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // SOS BUTTON
        Button(
            onClick = {
                try {
                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                    var bestLocation: android.location.Location? = null
                    try {
                        val providers = locationManager.getProviders(true)
                        for (provider in providers) {
                            val l = locationManager.getLastKnownLocation(provider) ?: continue
                            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                                bestLocation = l
                            }
                        }
                    } catch (e: SecurityException) {
                        // ignore
                    }
                    
                    if (bestLocation != null) {
                        val lat = bestLocation.latitude
                        val lng = bestLocation.longitude
                        val mapLink = "https://maps.google.com/?q=$lat,$lng"
                        val message = if (isMarathi) {
                            "🚨 *आपत्कालीन संदेश!* मला तातडीने मदत हवी आहे. माझे चालू लोकेशन (Google Maps): $mapLink"
                        } else {
                            "🚨 *EMERGENCY!* I need urgent help. My current GPS location: $mapLink"
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, message)
                        }
                        context.startActivity(Intent.createChooser(intent, if (isMarathi) "लोकेशन शेअर करा" else "Share SOS Location"))
                    } else {
                        Toast.makeText(
                            context,
                            if (isMarathi) "लोकेशन मिळाले नाही. कृपया मोबाईलचे GPS (लोकेशन सेवा) चालू ठेवा आणि पुन्हा प्रयत्न करा!" 
                            else "Could not fetch GPS coords. Please turn on Device Location (GPS) and retry!",
                            Toast.LENGTH_LONG
                        ).show()

                        val generalMessage = if (isMarathi) {
                            "🚨 *तातडीची मदत हवी आहे!* कृपया माझ्याशी संपर्क साधा. (लोकेशन काढता आले नाही, कृपया जीपीएस ऑन करा)"
                        } else {
                            "🚨 *EMERGENCY!* Please contact me immediately. (GPS was off, please locate me manually)"
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, generalMessage)
                        }
                        context.startActivity(Intent.createChooser(intent, "SOS"))
                    }
                } catch (e: SecurityException) {
                    Toast.makeText(context, "Location permission issue!", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2D55)),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("sos_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "SOS",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isMarathi) "तातडीने SOS संदेश" else "Send SOS Location",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
        
        // FIRST AID BUTTON
        Button(
            onClick = onOpenFirstAid,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("first_aid_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = "First Aid Guides",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isMarathi) "🏥 प्रथमोपचार" else "🏥 First Aid",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun FirstAidDialog(
    isMarathi: Boolean,
    onDismiss: () -> Unit
) {
    var selectedTopicIndex by remember { mutableStateOf<Int?>(null) }
    var currentStep by remember { mutableStateOf(0) }

    Dialog(
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = {
            if (selectedTopicIndex != null) {
                selectedTopicIndex = null
                currentStep = 0
            } else {
                onDismiss()
            }
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedTopicIndex != null) {
                            IconButton(
                                onClick = {
                                    selectedTopicIndex = null
                                    currentStep = 0
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "मागे (Go Back)",
                                    tint = GBPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = "First Aid Icon",
                                tint = Color(0xFFFF2D55),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        Text(
                            text = if (isMarathi) "सचित्र प्रथमोपचार मार्गदर्शिका" else "Visual First Aid Guide",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = GBText
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = GBGreyText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // If no topic selected, show topic selection dashboard
                if (selectedTopicIndex == null) {
                    Text(
                        text = if (isMarathi) 
                            "⚠️ आपत्कालीन प्रसंगी घाबरून न जाता खालीलपैकी एका विषयावर क्लिक करून सचित्र मार्गदर्शन मिळवा:" 
                        else 
                            "⚠️ Keep calm and select a topic below for visual step-by-step guidance:",
                        fontSize = 11.sp,
                        color = Color(0xFFB45309),
                        fontWeight = FontWeight.Bold,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val topics = listOf(
                        Triple("🫁 CPR", if (isMarathi) "कृत्रिम श्वासोच्छ्वास (CPR)" else "Cardiopulmonary Resuscitation", Color(0xFFFEE2E2)),
                        Triple("🐍 Snake", if (isMarathi) "सर्पदंश प्रथमोपचार" else "Snake Bite Protocol", Color(0xFFFEF3C7)),
                        Triple("🩸 Bleed", if (isMarathi) "अति-रक्तस्त्राव" else "Heavy Bleeding Control", Color(0xFFFCE7F3)),
                        Triple("⚡ Shock", if (isMarathi) "विजेचा झटका" else "Electric Shock Treatment", Color(0xFFE0F2F1)),
                        Triple("🥵 Heat", if (isMarathi) "उष्माघात (उन्हाचा त्रास)" else "Heat Stroke Guide", Color(0xFFEFF6FF))
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        topics.forEachIndexed { index, (emoji, title, bgCol) ->
                            Card(
                                onClick = {
                                    selectedTopicIndex = index
                                    currentStep = 0
                                },
                                modifier = Modifier.fillMaxWidth().testTag("first_aid_topic_$index"),
                                colors = CardDefaults.cardColors(containerColor = bgCol.copy(alpha = 0.85f)),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.2.dp, bgCol)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(Color.White.copy(alpha = 0.9f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = if (isMarathi) "सचित्र स्टेप-बाय-स्टेप पायऱ्या पहा" else "Tap for step-by-step visual cards",
                                            fontSize = 11.sp,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = "पहा",
                                        tint = GBPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = GBPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = if (isMarathi) "ठीक आहे (Close)" else "Close Guide",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                } else {
                    // Step-by-Step Interactive Screen
                    val topicIdx = selectedTopicIndex!!
                    val topicTitle = when (topicIdx) {
                        0 -> if (isMarathi) "कृत्रिम श्वासोच्छ्वास (CPR)" else "CPR Guidance"
                        1 -> if (isMarathi) "सर्पदंश प्रथमोपचार" else "Snake Bite Treatment"
                        2 -> if (isMarathi) "अति-रक्तस्त्राव नियंत्रण" else "Heavy Bleeding Control"
                        3 -> if (isMarathi) "विजेचा झटका उपचार" else "Electric Shock Treatment"
                        else -> if (isMarathi) "उष्माघात उपचार" else "Heat Stroke Treatment"
                    }

                    val stepsData = when (topicIdx) {
                        0 -> listOf( // CPR Steps
                            Pair(
                                if (isMarathi) "पायरी 1: जागा तपासा व रुग्णाला झोपवा" else "Step 1: Check Area & Lay Patient Flat",
                                if (isMarathi) "रुग्णाला एका सपाट, मजबूत जागेवर पाठीवर उताणे झोपवा. तो शुद्धीवर आहे का ते विचारून खात्री करा. (Lay the patient on a firm, flat surface.)" else "Lay the patient flat on a firm, secure surface. Check for responsiveness."
                            ),
                            Pair(
                                if (isMarathi) "पायरी 2: छातीच्या मध्यभागी वेगाने दाब द्या" else "Step 2: Start Rapid Chest Compressions",
                                if (isMarathi) "छातीच्या मध्यभागी दोन्ही हात जोडून ठेवा. वेगाने व जोराने खाली दाबा (1 मिनिटात 100 ते 120 वेळा). (Push hard & fast, 100-120/min)" else "Place hands on the center of the chest. Push hard and fast at 100 to 120 compressions per minute."
                            ),
                            Pair(
                                if (isMarathi) "पायरी 3: तोंडाने कृत्रिम श्वास द्या" else "Step 3: Administer Rescue Breaths",
                                if (isMarathi) "नाक बंद करून प्रत्येक 30 दाबानंतर 2 वेळा तोंडातून खोल श्वास द्या (प्रशिक्षित असल्यास). (Give 2 rescue breaths after every 30 compressions)" else "Pinch the nose and give 2 rescue breaths after every 30 chest compressions, if trained."
                            ),
                            Pair(
                                if (isMarathi) "पायरी 4: रुग्ण शुद्धीवर येईपर्यंत सुरू ठेवा" else "Step 4: Continue until help arrives",
                                if (isMarathi) "रुग्ण हालचाल करेपर्यंत किंवा वैद्यकीय मदत मिळेपर्यंत ही सायकल चालू ठेवा. (Repeat comp/breath cycles continuously)" else "Keep repeating the compression/breath cycles until medical help arrives or the victim revives."
                            )
                        )
                        1 -> listOf( // Snake Bite Steps
                            Pair(
                                if (isMarathi) "पायरी 1: रुग्णाला धीर द्या व हालचाल बंद करा" else "Step 1: Calm the Patient & Immobilize",
                                if (isMarathi) "रुग्णाला शांत ठेवा. घाबरल्यामुळे हृदयाचे ठोके वाढून विष वेगाने शरीरात पसरते. (Keep patient calm; panic spreads venom faster.)" else "Keep the patient absolutely still and quiet. Calm them down, as high heart rate spreads venom."
                            ),
                            Pair(
                                if (isMarathi) "पायरी 2: दंश झालेला भाग हृदयाखाली ठेवा" else "Step 2: Position Limb below Heart",
                                if (isMarathi) "ज्या अवयवावर साप चावला आहे, तो भाग हृदयाच्या पातळीपेक्षा खाली ठेवावा. (Keep bitten area below patient's heart level.)" else "Keep the bitten arm or leg positioned below the level of the patient's heart."
                            ),
                            Pair(
                                if (isMarathi) "पायरी 3: दंशावर जखम करू नका किंवा चोळू नका" else "Step 3: Do NOT Cut, Burn or Suction",
                                if (isMarathi) "दंश झालेल्या जागेवर कोणतीही जखम किंवा काप करू नका, पाणी किंवा मातीने चोळू नका. (Do NOT cut, massage, or apply pressure-tourniquet)" else "Never cut the wound, burn, suction, or massage. Avoid traditional tight bindings."
                            ),
                            Pair(
                                if (isMarathi) "पायरी 4: सरकारी रुग्णालयात न्या (ASV)" else "Step 4: Rush to Govt Hospital for ASV",
                                if (isMarathi) "तातडीने अँटी-स्नेक व्हेनम (ASV) उपलब्ध असलेल्या शासकीय रुग्णालयात दाखल करा. (Rush to nearest Govt Hospital for Anti-Snake Venom)" else "Rush immediately to the nearest Government Hospital that stock Anti-Snake Venom (ASV) injection."
                            )
                        )
                        2 -> listOf( // Heavy Bleeding Steps
                            Pair(
                                if (isMarathi) "पायरी 1: जखमेवर थेट दाब द्या" else "Step 1: Apply Direct Pressure",
                                if (isMarathi) "स्वच्छ कापड किंवा पट्टीच्या साहाय्याने जखमेवर थेट जोराने दाबून ठेवा. (Apply firm, direct pressure on the wound.)" else "Apply firm, direct pressure over the bleeding site with a clean cloth, gauze, or dressing."
                            ),
                            Pair(
                                if (isMarathi) "पायरी 2: जखम हृदयाच्या वर उंचावा" else "Step 2: Elevate Bounding Limb",
                                if (isMarathi) "हात किंवा पाय जिथे जखम आहे, तो भाग हृदयापेक्षा उंच पातळीवर ठेवा जेणेकरून रक्तप्रवाह कमी होईल. (Elevate limb above heart level.)" else "Elevate the bleeding limb/arm above heart level to naturally reduce blood flow speed."
                            ),
                            Pair(
                                if (isMarathi) "पायरी 3: कापड न काढता अतिरिक्त थर लावा" else "Step 3: Add More Layers (Don't Remove)",
                                if (isMarathi) "कापड रक्ताने माखल्यास ते काढू नका, त्यावर दुसरे स्वच्छ कापड ठेवून दाब वाढवा. (Add another clean cloth layer on top.)" else "If blood soaks through, do not peel off the cloth. Just place another clean cloth layer on top and continue pressing."
                            )
                        )
                        3 -> listOf( // Electric Shock Steps
                            Pair(
                                if (isMarathi) "पायरी 1: मुख्य वीज स्विच बंद करा" else "Step 1: Switch Off Main Power",
                                if (isMarathi) "सगळ्यात आधी घरातील मुख्य वीज कनेक्शन (Main Board Switch) तात्काळ बंद करा. (Shut off main breaker circuit immediately.)" else "Immediately flip the main power switch or trip the circuit breaker. Do not touch wires."
                            ),
                            Pair(
                                if (isMarathi) "पायरी 2: सुक्या लाकडाचा वापर करून दूर करा" else "Step 2: Push away with Dry Wood",
                                if (isMarathi) "रुग्णाला थेट स्पर्श करू नका! कोरडी लाकडी काठी, दोरी किंवा सुक्या रबरने त्याला विजेपासून दूर करा. (Use non-conductive wood/plastic)" else "Never touch the victim with bare hands. Push them away using dry wood, dry plastic, or dry rubber."
                            ),
                            Pair(
                                if (isMarathi) "पायरी 3: श्वसन तपासा आणि CPR सुरू करा" else "Step 3: Monitor Breathing & CPR",
                                if (isMarathi) "रुग्णाची श्वास चालू आहे का ते तपासा, प्रतिसाद नसल्यास त्वरित CPR सुरू करा. (Check breathing; begin CPR if required)" else "Check if they are breathing. If they are unresponsive and not breathing, start CPR immediately."
                            )
                        )
                        else -> listOf( // Heat Stroke Steps
                            Pair(
                                if (isMarathi) "पायरी 1: रुग्णाला सावलीत हलवा" else "Step 1: Shift to Shade & Cool Spot",
                                if (isMarathi) "रुग्णाला तात्काळ कडक उन्हातून हलवून थंड सावलीत किंवा पंख्याखाली आणा. (Move victim immediately to a cool, shaded place.)" else "Quickly move the affected person out of direct hot sunlight into a cool, well-ventilated, shaded spot."
                            ),
                            Pair(
                                if (isMarathi) "पायरी 2: गार पाण्याच्या पट्ट्या अंगावर ठेवा" else "Step 2: Cool with Cold Wet Cloth",
                                if (isMarathi) "कपडे सैल करा, ओल्या गार कापडाने अंग पुसा आणि कपाळावर/अंगावर गार पाण्याच्या पट्ट्या ठेवा. (Loosen clothes; apply cold wet sheets)" else "Loosen tight clothes. Sponge their body with cold water or put cold wet packs on their forehead and chest."
                            ),
                            Pair(
                                if (isMarathi) "पायरी 3: ओआरएस (ORS) किंवा लिंबू पाणी द्या" else "Step 3: Give ORS, Lemon Juice or Water",
                                if (isMarathi) "रुग्ण पूर्ण शुद्धीवर असल्यास थंड पाणी, ओआरएस पाणी किंवा लिंबू पाणी प्यायला द्या. (Give cool water or ORS solution if conscious)" else "If the patient is conscious and can swallow, offer cool water, ORS solution, or fresh lemonade."
                            )
                        )
                    }

                    val maxStep = stepsData.size
                    val stepTitle = stepsData[currentStep].first
                    val stepDesc = stepsData[currentStep].second

                    Text(
                        text = topicTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GBPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 🎨 GORGEOUS DYNAMIC CANVAS DRAWING ILLUSTRATION PANEL
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .testTag("first_aid_canvas_card"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val pulseTransition = rememberInfiniteTransition(label = "pulse")
                            val animScale by pulseTransition.animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )
                            val animOffset by pulseTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(2000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "offset"
                            )

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val centerX = canvasWidth / 2
                                val centerY = canvasHeight / 2

                                when (topicIdx) {
                                    0 -> { // CPR Animation
                                        if (currentStep == 1) { // Compressions animation
                                            // Chest target area
                                            drawCircle(
                                                color = Color(0xFFFF2D55).copy(alpha = 0.15f),
                                                radius = 60.dp.toPx() * animScale,
                                                center = Offset(centerX, centerY)
                                            )
                                            drawCircle(
                                                color = Color(0xFFFF2D55),
                                                radius = 24.dp.toPx(),
                                                center = Offset(centerX, centerY),
                                                style = Stroke(width = 4.dp.toPx())
                                            )
                                            // Overlapping hands sketch
                                            drawCircle(
                                                color = Color(0xFF2563EB),
                                                radius = 12.dp.toPx(),
                                                center = Offset(centerX - 10.dp.toPx(), centerY),
                                            )
                                            drawCircle(
                                                color = Color(0xFF2563EB).copy(alpha = 0.8f),
                                                radius = 12.dp.toPx(),
                                                center = Offset(centerX + 10.dp.toPx(), centerY),
                                            )
                                        } else if (currentStep == 2) { // Breaths animation
                                            // Expanding Lungs representation
                                            val lungRadius = 20.dp.toPx() * animScale
                                            drawCircle(
                                                color = Color(0xFF0D9488).copy(alpha = 0.4f),
                                                radius = lungRadius,
                                                center = Offset(centerX - 25.dp.toPx(), centerY)
                                            )
                                            drawCircle(
                                                color = Color(0xFF0D9488).copy(alpha = 0.4f),
                                                radius = lungRadius,
                                                center = Offset(centerX + 25.dp.toPx(), centerY)
                                            )
                                            // Air passage arrow
                                            drawLine(
                                                color = Color(0xFF0D9488),
                                                start = Offset(centerX, centerY - 40.dp.toPx()),
                                                end = Offset(centerX, centerY + 20.dp.toPx()),
                                                strokeWidth = 4.dp.toPx()
                                            )
                                        } else { // Flat / Ambulance
                                            // Draw horizontal flat line and a red cross sign
                                            drawLine(
                                                color = Color(0xFFCBD5E1),
                                                start = Offset(40.dp.toPx(), centerY + 30.dp.toPx()),
                                                end = Offset(canvasWidth - 40.dp.toPx(), centerY + 30.dp.toPx()),
                                                strokeWidth = 6.dp.toPx()
                                            )
                                            drawCircle(
                                                color = Color(0xFFFF2D55),
                                                radius = 25.dp.toPx(),
                                                center = Offset(centerX, centerY - 10.dp.toPx())
                                            )
                                            // White Cross lines
                                            drawLine(
                                                color = Color.White,
                                                start = Offset(centerX - 12.dp.toPx(), centerY - 10.dp.toPx()),
                                                end = Offset(centerX + 12.dp.toPx(), centerY - 10.dp.toPx()),
                                                strokeWidth = 5.dp.toPx()
                                            )
                                            drawLine(
                                                color = Color.White,
                                                start = Offset(centerX, centerY - 22.dp.toPx()),
                                                end = Offset(centerX, centerY + 2.dp.toPx()),
                                                strokeWidth = 5.dp.toPx()
                                            )
                                        }
                                    }
                                    1 -> { // Snake bite Animation
                                        if (currentStep == 0) { // Heartbeat calm pulse
                                            drawCircle(
                                                color = Color(0xFFFF2D55).copy(alpha = 0.2f),
                                                radius = 45.dp.toPx() * animScale,
                                                center = Offset(centerX, centerY)
                                            )
                                            // Simple Heart line
                                            drawCircle(
                                                color = Color(0xFFFF2D55),
                                                radius = 18.dp.toPx(),
                                                center = Offset(centerX, centerY)
                                            )
                                        } else if (currentStep == 2) { // Warning no cut
                                            drawCircle(
                                                color = Color(0xFFFF2D55),
                                                radius = 35.dp.toPx(),
                                                center = Offset(centerX, centerY),
                                                style = Stroke(width = 6.dp.toPx())
                                            )
                                            // Diagonal line
                                            drawLine(
                                                color = Color(0xFFFF2D55),
                                                start = Offset(centerX - 24.dp.toPx(), centerY - 24.dp.toPx()),
                                                end = Offset(centerX + 24.dp.toPx(), centerY + 24.dp.toPx()),
                                                strokeWidth = 6.dp.toPx()
                                            )
                                            // Wound Dots
                                            drawCircle(Color.Black, 4.dp.toPx(), Offset(centerX - 8.dp.toPx(), centerY))
                                            drawCircle(Color.Black, 4.dp.toPx(), Offset(centerX + 8.dp.toPx(), centerY))
                                        } else { // Medical Hospital Shield
                                            drawRect(
                                                color = Color(0xFF059669),
                                                size = Size(60.dp.toPx(), 60.dp.toPx()),
                                                topLeft = Offset(centerX - 30.dp.toPx(), centerY - 30.dp.toPx())
                                            )
                                            // Plus Symbol
                                            drawLine(
                                                color = Color.White,
                                                start = Offset(centerX - 15.dp.toPx(), centerY),
                                                end = Offset(centerX + 15.dp.toPx(), centerY),
                                                strokeWidth = 6.dp.toPx()
                                            )
                                            drawLine(
                                                color = Color.White,
                                                start = Offset(centerX, centerY - 15.dp.toPx()),
                                                end = Offset(centerX, centerY + 15.dp.toPx()),
                                                strokeWidth = 6.dp.toPx()
                                            )
                                        }
                                    }
                                    2 -> { // Bleeding Animation
                                        if (currentStep == 0) { // Direct pressure pulse
                                            drawCircle(
                                                color = Color(0xFFFF2D55).copy(alpha = 0.3f),
                                                radius = 50.dp.toPx() * animScale,
                                                center = Offset(centerX, centerY)
                                            )
                                            drawCircle(
                                                color = Color(0xFFFF2D55),
                                                radius = 20.dp.toPx(),
                                                center = Offset(centerX, centerY)
                                            )
                                        } else if (currentStep == 1) { // Elevate arrow
                                            drawLine(
                                                color = Color(0xFF2563EB),
                                                start = Offset(centerX, centerY + 30.dp.toPx()),
                                                end = Offset(centerX, centerY - 30.dp.toPx()),
                                                strokeWidth = 8.dp.toPx()
                                            )
                                            // Arrowhead pointing UP
                                            drawLine(
                                                color = Color(0xFF2563EB),
                                                start = Offset(centerX - 15.dp.toPx(), centerY - 15.dp.toPx()),
                                                end = Offset(centerX, centerY - 30.dp.toPx()),
                                                strokeWidth = 8.dp.toPx()
                                            )
                                            drawLine(
                                                color = Color(0xFF2563EB),
                                                start = Offset(centerX + 15.dp.toPx(), centerY - 15.dp.toPx()),
                                                end = Offset(centerX, centerY - 30.dp.toPx()),
                                                strokeWidth = 8.dp.toPx()
                                            )
                                        } else { // Multi-layered Bandage stack
                                            drawRect(
                                                color = Color(0xFFCBD5E1),
                                                size = Size(80.dp.toPx(), 14.dp.toPx()),
                                                topLeft = Offset(centerX - 40.dp.toPx(), centerY - 25.dp.toPx())
                                            )
                                            drawRect(
                                                color = Color(0xFF94A3B8),
                                                size = Size(80.dp.toPx(), 14.dp.toPx()),
                                                topLeft = Offset(centerX - 40.dp.toPx(), centerY - 5.dp.toPx())
                                            )
                                            drawRect(
                                                color = Color(0xFF64748B),
                                                size = Size(80.dp.toPx(), 14.dp.toPx()),
                                                topLeft = Offset(centerX - 40.dp.toPx(), centerY + 15.dp.toPx())
                                            )
                                        }
                                    }
                                    3 -> { // Electric Shock
                                        if (currentStep == 0) { // Switch off
                                            // Switch box
                                            drawRect(
                                                color = Color(0xFF64748B),
                                                size = Size(40.dp.toPx(), 70.dp.toPx()),
                                                topLeft = Offset(centerX - 20.dp.toPx(), centerY - 35.dp.toPx())
                                            )
                                            // Switch knob pointing down (OFF)
                                            drawRect(
                                                color = Color(0xFFFF2D55),
                                                size = Size(20.dp.toPx(), 20.dp.toPx()),
                                                topLeft = Offset(centerX - 10.dp.toPx(), centerY + 5.dp.toPx())
                                            )
                                        } else if (currentStep == 1) { // Wooden stick pushing
                                            // Wire
                                            drawLine(Color.Black, Offset(centerX - 50.dp.toPx(), centerY + 20.dp.toPx()), Offset(centerX + 50.dp.toPx(), centerY + 20.dp.toPx()), 4.dp.toPx())
                                            // Wooden pole/stick pushing it
                                            drawLine(
                                                color = Color(0xFF854D0E), // Brown wood
                                                start = Offset(centerX - 40.dp.toPx(), centerY - 30.dp.toPx()),
                                                end = Offset(centerX, centerY + 15.dp.toPx()),
                                                strokeWidth = 10.dp.toPx()
                                            )
                                        } else { // Heart with thunderbolt
                                            drawCircle(Color(0xFFE2E8F0), 45.dp.toPx(), Offset(centerX, centerY))
                                            drawCircle(Color(0xFFFFB000), 25.dp.toPx() * animScale, Offset(centerX, centerY))
                                        }
                                    }
                                    else -> { // Heat stroke
                                        if (currentStep == 0) { // Sun with umbrella shade
                                            drawCircle(Color(0xFFF59E0B), 25.dp.toPx(), Offset(centerX - 20.dp.toPx(), centerY - 20.dp.toPx()))
                                            // Canopy curved line
                                            drawArc(
                                                color = Color(0xFF2563EB),
                                                startAngle = 180f,
                                                sweepAngle = 180f,
                                                useCenter = false,
                                                size = Size(80.dp.toPx(), 80.dp.toPx()),
                                                topLeft = Offset(centerX - 40.dp.toPx(), centerY - 10.dp.toPx()),
                                                style = Stroke(width = 6.dp.toPx())
                                            )
                                        } else if (currentStep == 1) { // Cold wet drops
                                            drawCircle(
                                                color = Color(0xFF60A5FA).copy(alpha = animScale),
                                                radius = 15.dp.toPx(),
                                                center = Offset(centerX - 30.dp.toPx(), centerY - 15.dp.toPx())
                                            )
                                            drawCircle(
                                                color = Color(0xFF60A5FA).copy(alpha = animScale),
                                                radius = 15.dp.toPx(),
                                                center = Offset(centerX + 30.dp.toPx(), centerY + 15.dp.toPx())
                                            )
                                            drawCircle(
                                                color = Color(0xFF3B82F6),
                                                radius = 10.dp.toPx(),
                                                center = Offset(centerX, centerY)
                                            )
                                        } else { // ORS Glass
                                            // Cup body
                                            drawLine(Color(0xFF3B82F6), Offset(centerX - 20.dp.toPx(), centerY - 30.dp.toPx()), Offset(centerX - 12.dp.toPx(), centerY + 30.dp.toPx()), 4.dp.toPx())
                                            drawLine(Color(0xFF3B82F6), Offset(centerX + 20.dp.toPx(), centerY - 30.dp.toPx()), Offset(centerX + 12.dp.toPx(), centerY + 30.dp.toPx()), 4.dp.toPx())
                                            drawLine(Color(0xFF3B82F6), Offset(centerX - 12.dp.toPx(), centerY + 30.dp.toPx()), Offset(centerX + 12.dp.toPx(), centerY + 30.dp.toPx()), 4.dp.toPx())
                                            // Drink liquid level filled (animated)
                                            drawCircle(Color(0xFF60A5FA), 12.dp.toPx() * animScale, Offset(centerX, centerY + 10.dp.toPx()))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Step indicator and title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stepTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = GBText
                        )
                        Text(
                            text = "${currentStep + 1} / $maxStep",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GBPrimary
                        )
                    }

                    // Progress Bar
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color(0xFFE2E8F0), RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((currentStep + 1).toFloat() / maxStep)
                                .fillMaxHeight()
                                .background(GBPrimary, RoundedCornerShape(3.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Step detailed description
                    Text(
                        text = stepDesc,
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stepper Navigation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (currentStep > 0) {
                            OutlinedButton(
                                onClick = { currentStep-- },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, GBBorder)
                            ) {
                                Text(
                                    text = if (isMarathi) "⬅️ मागील पायरी" else "⬅️ Previous",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = GBText
                                )
                            }
                        }

                        if (currentStep < maxStep - 1) {
                            Button(
                                onClick = { currentStep++ },
                                colors = ButtonDefaults.buttonColors(containerColor = GBPrimary),
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isMarathi) "पुढील पायरी ➡️" else "Next Step ➡️",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        } else {
                            // Done / Back to topics list
                            Button(
                                onClick = {
                                    selectedTopicIndex = null
                                    currentStep = 0
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (isMarathi) "पूर्ण झाले (Back to List)" else "Completed (Back)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}




@Composable
fun DetailRow(
    label: String,
    value: String,
    isPhone: Boolean = false,
    onPhoneClick: (() -> Unit)? = null
) {
    Row(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF64748B)
        )
        if (isPhone && onPhoneClick != null) {
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB),
                modifier = androidx.compose.ui.Modifier.clickable { onPhoneClick() }
            )
        } else {
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF0F172A)
            )
        }
    }
}

@Composable
fun ContactFormDialog(
    editingContact: EmergencyContact?,
    isMarathi: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        id: Int,
        nameMr: String, nameEn: String,
        phone: String, phoneAlt: String,
        category: String,
        designationMr: String, designationEn: String,
        villageOrAreaMr: String, villageOrAreaEn: String,
        isDefault: Boolean,
        notes: String
    ) -> Unit
) {
    // Form Input States
    var nameMr by remember { mutableStateOf(editingContact?.nameMr ?: "") }
    var nameEn by remember { mutableStateOf(editingContact?.nameEn ?: "") }
    var phone by remember { mutableStateOf(editingContact?.phone ?: "") }
    var phoneAlt by remember { mutableStateOf(editingContact?.phoneAlt ?: "") }
    var category by remember { mutableStateOf(editingContact?.category ?: "RESCUE") }
    var designationMr by remember { mutableStateOf(editingContact?.designationMr ?: "") }
    var designationEn by remember { mutableStateOf(editingContact?.designationEn ?: "") }
    var villageMr by remember { mutableStateOf(editingContact?.villageOrAreaMr ?: "") }
    var villageEn by remember { mutableStateOf(editingContact?.villageOrAreaEn ?: "") }
    var notes by remember { mutableStateOf(editingContact?.notes ?: "") }

    val categories = listOf("RESCUE", "MEDICAL", "ADMIN", "POLICE", "FIRE", "MACHINERY", "UTILITY", "RELIEF", "VOLUNTEER")
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("contact_form_surface"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editingContact == null) {
                            if (isMarathi) "नवीन आपत्कालीन संपर्क" else "New Emergency Contact"
                        } else {
                            if (isMarathi) "सद्य संपर्क बदला" else "Modify Contact Record"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "बंद करा")
                    }
                }

                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable fields to fit all screen variables
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        // SECTION: Basic details (Bilingual Names)
                        Text(
                            text = if (isMarathi) "1. संपर्क नाव (Contact Name)" else "1. Contact Name Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = nameMr,
                            onValueChange = { nameMr = it },
                            label = { Text("नाव (मराठीत) *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = nameEn,
                            onValueChange = { nameEn = it },
                            label = { Text("Name (in English) *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        // SECTION: Telecom Info
                        Text(
                            text = if (isMarathi) "2. दूरध्वनी संपर्क (Phone Information)" else "2. Phone Contact Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("प्राथमिक फोन नंबर *") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = phoneAlt,
                            onValueChange = { phoneAlt = it },
                            label = { Text("पर्यायी फोन (पर्यायी)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        // SECTION: Classification & Role details (Category & Designation)
                        Text(
                            text = if (isMarathi) "3. पद व कार्यालयीन माहिती (Offices & Designation)" else "3. Offices & Classification",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = designationMr,
                            onValueChange = { designationMr = it },
                            label = { Text("पद / कायदेशीर भूमिका (मराठीत)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = designationEn,
                            onValueChange = { designationEn = it },
                            label = { Text("Designation / Role (in English)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        // SECTION: Geographic info / Category selection
                        Text(
                            text = if (isMarathi) "4. विभाग वर्ग आणि पत्ता (Location & Class)" else "4. Category & Area",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Category Custom drop-style
                        Box {
                            val categoryDisplayValue = when (category) {
                                "RESCUE" -> if (isMarathi) "RESCUE (🛟 बचाव दल व पोहणारे)" else "RESCUE (🛟 Rescue & Swimmers)"
                                "MEDICAL" -> if (isMarathi) "MEDICAL (🏥 वैद्यकीय व रुग्णवाहिका)" else "MEDICAL (🏥 Medical & Ambulance)"
                                "ADMIN" -> if (isMarathi) "ADMIN (🏛️ नियंत्रण कक्ष व प्रशासन)" else "ADMIN (🏛️ Control Room & Admin)"
                                "POLICE" -> if (isMarathi) "POLICE (🚨 पोलीस व सुरक्षा)" else "POLICE (🚨 Police & Security)"
                                "FIRE" -> if (isMarathi) "FIRE (🚒 अग्निशमन सेवा)" else "FIRE (🚒 Fire Brigade)"
                                "MACHINERY" -> if (isMarathi) "MACHINERY (🚜 जेसीबी व यंत्रसामग्री)" else "MACHINERY (🚜 JCB & Machinery)"
                                "UTILITY" -> if (isMarathi) "UTILITY (⚡ वीज, पाणी व रस्ते)" else "UTILITY (⚡ Electricity & Water)"
                                "RELIEF" -> if (isMarathi) "RELIEF (📦 अन्न, निवारा व मदत)" else "RELIEF (📦 Food, Shelter & Relief)"
                                "VOLUNTEER" -> if (isMarathi) "VOLUNTEER (🤝 स्वयंसेवक गट)" else "VOLUNTEER (🤝 Volunteers)"
                                else -> category
                            }

                            OutlinedTextField(
                                value = categoryDisplayValue,
                                onValueChange = {},
                                label = { Text("वर्ग / Category") },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { categoryDropdownExpanded = !categoryDropdownExpanded }) {
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "निवडा")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { categoryDropdownExpanded = !categoryDropdownExpanded }
                            )
                            DropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    val dropdownLabel = when (cat) {
                                        "RESCUE" -> if (isMarathi) "RESCUE (🛟 बचाव दल व पोहणारे)" else "RESCUE (🛟 Rescue & Swimmers)"
                                        "MEDICAL" -> if (isMarathi) "MEDICAL (🏥 वैद्यकीय व रुग्णवाहिका)" else "MEDICAL (🏥 Medical & Ambulance)"
                                        "ADMIN" -> if (isMarathi) "ADMIN (🏛️ नियंत्रण कक्ष व प्रशासन)" else "ADMIN (🏛️ Control Room & Admin)"
                                        "POLICE" -> if (isMarathi) "POLICE (🚨 पोलीस व सुरक्षा)" else "POLICE (🚨 Police & Security)"
                                        "FIRE" -> if (isMarathi) "FIRE (🚒 अग्निशमन सेवा)" else "FIRE (🚒 Fire Brigade)"
                                        "MACHINERY" -> if (isMarathi) "MACHINERY (🚜 जेसीबी व यंत्रसामग्री)" else "MACHINERY (🚜 JCB & Machinery)"
                                        "UTILITY" -> if (isMarathi) "UTILITY (⚡ वीज, पाणी व रस्ते)" else "UTILITY (⚡ Electricity & Water)"
                                        "RELIEF" -> if (isMarathi) "RELIEF (📦 अन्न, निवारा व मदत)" else "RELIEF (📦 Food, Shelter & Relief)"
                                        "VOLUNTEER" -> if (isMarathi) "VOLUNTEER (🤝 स्वयंसेवक गट)" else "VOLUNTEER (🤝 Volunteers)"
                                        else -> cat
                                    }
                                    DropdownMenuItem(
                                        text = { Text(dropdownLabel) },
                                        onClick = {
                                            category = cat
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = villageMr,
                            onValueChange = { villageMr = it },
                            label = { Text("गाव / तालुका परिसर (मराठीत)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = villageEn,
                            onValueChange = { villageEn = it },
                            label = { Text("Village / Headquarters (English)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        // NOTES OR EXTRA COORDINATES
                        Text(
                            text = if (isMarathi) "5. पूरक टीप (Notes / Extra Coordinates)" else "5. Notes & Guidelines",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(if (isMarathi) "आपत्कालीन वेळेची सवय/इतर माहिती" else "Available time support notes") },
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Save or Cancel Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isMarathi) "रद्द करा" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nameMr.isBlank()) {
                                return@Button
                            }
                            onSave(
                                editingContact?.id ?: 0,
                                nameMr, nameEn.ifBlank { nameMr },
                                phone, phoneAlt,
                                category,
                                designationMr.ifBlank { "मदत अधिकारी" },
                                designationEn.ifBlank { "Response Agent" },
                                villageMr.ifBlank { "मुख्यालय" },
                                villageEn.ifBlank { "Headquarters" },
                                editingContact?.isDefault ?: false,
                                notes
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("save_contact_btn")
                    ) {
                        Text(if (isMarathi) "संपर्क जतन करा" else "Save Contact", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ContactFormPanel(
    editingContact: EmergencyContact?,
    isMarathi: Boolean,
    onDismiss: () -> Unit,
    allContacts: List<EmergencyContact> = emptyList(),
    onSave: (
        id: Int,
        nameMr: String, nameEn: String,
        phone: String, phoneAlt: String,
        category: String,
        designationMr: String, designationEn: String,
        villageOrAreaMr: String, villageOrAreaEn: String,
        isDefault: Boolean,
        notes: String
    ) -> Unit
) {
    // Form Input States
    var nameMr by remember(editingContact) { mutableStateOf(editingContact?.nameMr ?: "") }
    var nameEn by remember(editingContact) { mutableStateOf(editingContact?.nameEn ?: "") }
    var phone by remember(editingContact) { mutableStateOf(editingContact?.phone ?: "") }
    var phoneAlt by remember(editingContact) { mutableStateOf(editingContact?.phoneAlt ?: "") }
    var category by remember(editingContact) { mutableStateOf(editingContact?.category ?: "RESCUE") }
    var designationMr by remember(editingContact) { mutableStateOf(editingContact?.designationMr ?: "") }
    var designationEn by remember(editingContact) { mutableStateOf(editingContact?.designationEn ?: "") }
    var villageMr by remember(editingContact) { mutableStateOf(editingContact?.villageOrAreaMr ?: "") }
    var villageEn by remember(editingContact) { mutableStateOf(editingContact?.villageOrAreaEn ?: "") }
    var notes by remember(editingContact) { mutableStateOf(editingContact?.notes ?: "") }

    // Unique lists for autocompletion
    val villageSuggestions = remember(allContacts) {
        allContacts
            .filter { it.villageOrAreaMr.isNotBlank() && it.villageOrAreaEn.isNotBlank() }
            .associate { it.villageOrAreaMr.trim() to it.villageOrAreaEn.trim() }
            .filterKeys { !it.contains("सर्व") && !it.contains("All") && !it.contains("तालुका") }
    }

    val designationSuggestions = remember(allContacts) {
        allContacts
            .filter { it.designationMr.isNotBlank() && it.designationEn.isNotBlank() }
            .associate { it.designationMr.trim() to it.designationEn.trim() }
    }

    val categories = listOf("RESCUE", "MEDICAL", "ADMIN", "POLICE", "FIRE", "MACHINERY", "UTILITY", "RELIEF", "VOLUNTEER")
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var designationDropdownExpanded by remember { mutableStateOf(false) }
    var villageDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri = result.data?.data
            if (contactUri != null) {
                try {
                    val cursor = context.contentResolver.query(
                        contactUri,
                        arrayOf(
                            android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER
                        ),
                        null, null, null
                    )
                    cursor?.use { c ->
                        if (c.moveToFirst()) {
                            val nameIndex = c.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                            val numberIndex = c.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (nameIndex >= 0 && numberIndex >= 0) {
                                val importedName = c.getString(nameIndex) ?: ""
                                val importedPhone = c.getString(numberIndex) ?: ""
                                nameMr = importedName
                                nameEn = importedName
                                phone = importedPhone.replace(" ", "").replace("-", "")
                                Toast.makeText(
                                    context,
                                    if (isMarathi) "संपर्क यशस्वीरित्या आणला: $importedName" else "Contact imported successfully: $importedName",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        if (isMarathi) "संपर्क वाचताना त्रुटी: ${e.localizedMessage}" else "Error reading contact: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    val launchContactPicker = {
        val intent = Intent(Intent.ACTION_PICK, android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        try {
            contactPickerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                if (isMarathi) "संपर्क पुस्तक उघडता आले नाही: ${e.localizedMessage}" else "Could not open contacts: ${e.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("contact_form_panel"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, GBBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Panel Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editingContact == null) {
                        if (isMarathi) "📝 नवीन संपर्क जोडा" else "📝 Add New Contact"
                    } else {
                        if (isMarathi) "✏️ संपर्क बदला" else "✏️ Edit Contact"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GBPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "बंद करा")
                }
            }

            Divider(color = GBPrimary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

            // Scrollable fields
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Button(
                        onClick = launchContactPicker,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)), // Elegant Teal color for importing
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .testTag("import_phone_contact_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = if (isMarathi) "फोन बुकमधून संपर्क निवडा" else "Import from Phone Contacts",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isMarathi) "📞 फोन बुकमधून संपर्क आणा" else "📞 Import from Phone Book",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                    Divider(color = GBPrimary.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                }

                item {
                    Text(
                        text = if (isMarathi) "1. संपर्क नाव (Contact Name)" else "1. Contact Name Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = GBPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = nameMr,
                        onValueChange = { nameMr = it },
                        label = { Text("नाव (मराठीत) *", fontSize = 11.sp) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text("Name (in English) *", fontSize = 11.sp) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        text = if (isMarathi) "2. दूरध्वनी संपर्क (Phone Information)" else "2. Phone Contact Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = GBPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("प्राथमिक फोन नंबर *", fontSize = 11.sp) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = phoneAlt,
                        onValueChange = { phoneAlt = it },
                        label = { Text("पर्यायी फोन (पर्यायी)", fontSize = 11.sp) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        text = if (isMarathi) "3. पद व कार्यालयीन माहिती (Offices & Designation)" else "3. Offices & Classification",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = GBPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val filteredDesignations = remember(designationMr) {
                        if (designationMr.isBlank()) {
                            designationSuggestions.toList().take(8)
                        } else {
                            designationSuggestions.toList().filter {
                                it.first.contains(designationMr, ignoreCase = true) ||
                                it.second.contains(designationMr, ignoreCase = true)
                            }.take(8)
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = designationMr,
                            onValueChange = {
                                designationMr = it
                                designationDropdownExpanded = true
                            },
                            label = { Text("पद / कायदेशीर भूमिका (मराठीत) *", fontSize = 11.sp) },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.sp),
                            trailingIcon = {
                                IconButton(onClick = { designationDropdownExpanded = !designationDropdownExpanded }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "निवडा")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = designationDropdownExpanded && filteredDesignations.isNotEmpty(),
                            onDismissRequest = { designationDropdownExpanded = false },
                            properties = androidx.compose.ui.window.PopupProperties(focusable = false),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            filteredDesignations.forEach { (mr, en) ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(text = mr, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GBText)
                                            Text(text = en, fontSize = 10.sp, color = GBGreyText)
                                        }
                                    },
                                    onClick = {
                                        designationMr = mr
                                        designationEn = en
                                        designationDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = designationEn,
                        onValueChange = { designationEn = it },
                        label = { Text("Designation / Role (in English) *", fontSize = 11.sp) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        text = if (isMarathi) "4. विभाग वर्ग आणि पत्ता (Location & Class)" else "4. Category & Area",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = GBPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Box {
                        val categoryDisplayValue = when (category) {
                            "RESCUE" -> if (isMarathi) "RESCUE (🛟 बचाव दल व पोहणारे)" else "RESCUE (🛟 Rescue & Swimmers)"
                            "MEDICAL" -> if (isMarathi) "MEDICAL (🏥 वैद्यकीय व रुग्णवाहिका)" else "MEDICAL (🏥 Medical & Ambulance)"
                            "ADMIN" -> if (isMarathi) "ADMIN (🏛️ नियंत्रण कक्ष व प्रशासन)" else "ADMIN (🏛️ Control Room & Admin)"
                            "POLICE" -> if (isMarathi) "POLICE (🚨 पोलीस व सुरक्षा)" else "POLICE (🚨 Police & Security)"
                            "FIRE" -> if (isMarathi) "FIRE (🚒 अग्निशमन सेवा)" else "FIRE (🚒 Fire Brigade)"
                            "MACHINERY" -> if (isMarathi) "MACHINERY (🚜 जेसीबी व यंत्रसामग्री)" else "MACHINERY (🚜 JCB & Machinery)"
                            "UTILITY" -> if (isMarathi) "UTILITY (⚡ वीज, पाणी व रस्ते)" else "UTILITY (⚡ Electricity & Water)"
                            "RELIEF" -> if (isMarathi) "RELIEF (📦 अन्न, निवारा व मदत)" else "RELIEF (📦 Food, Shelter & Relief)"
                            "VOLUNTEER" -> if (isMarathi) "VOLUNTEER (🤝 स्वयंसेवक गट)" else "VOLUNTEER (🤝 Volunteers)"
                            else -> category
                        }

                        OutlinedTextField(
                            value = categoryDisplayValue,
                            onValueChange = {},
                            label = { Text("वर्ग / Category", fontSize = 11.sp) },
                            readOnly = true,
                            textStyle = TextStyle(fontSize = 13.sp),
                            trailingIcon = {
                                IconButton(onClick = { categoryDropdownExpanded = !categoryDropdownExpanded }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "निवडा")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { categoryDropdownExpanded = !categoryDropdownExpanded }
                        )
                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                val dropdownLabel = when (cat) {
                                    "RESCUE" -> if (isMarathi) "RESCUE (🛟 बचाव दल व पोहणारे)" else "RESCUE (🛟 Rescue & Swimmers)"
                                    "MEDICAL" -> if (isMarathi) "MEDICAL (🏥 वैद्यकीय व रुग्णवाहिका)" else "MEDICAL (🏥 Medical & Ambulance)"
                                    "ADMIN" -> if (isMarathi) "ADMIN (🏛️ नियंत्रण कक्ष व प्रशासन)" else "ADMIN (🏛️ Control Room & Admin)"
                                    "POLICE" -> if (isMarathi) "POLICE (🚨 पोलीस व सुरक्षा)" else "POLICE (🚨 Police & Security)"
                                    "FIRE" -> if (isMarathi) "FIRE (🚒 अग्निशमन सेवा)" else "FIRE (🚒 Fire Brigade)"
                                    "MACHINERY" -> if (isMarathi) "MACHINERY (🚜 जेसीबी व यंत्रसामग्री)" else "MACHINERY (🚜 JCB & Machinery)"
                                    "UTILITY" -> if (isMarathi) "UTILITY (⚡ वीज, पाणी व रस्ते)" else "UTILITY (⚡ Electricity & Water)"
                                    "RELIEF" -> if (isMarathi) "RELIEF (📦 अन्न, निवारा व मदत)" else "RELIEF (📦 Food, Shelter & Relief)"
                                    "VOLUNTEER" -> if (isMarathi) "VOLUNTEER (🤝 स्वयंसेवक गट)" else "VOLUNTEER (🤝 Volunteers)"
                                    else -> cat
                                }
                                DropdownMenuItem(
                                    text = { Text(dropdownLabel, fontSize = 12.sp) },
                                    onClick = {
                                        category = cat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val filteredVillages = remember(villageMr) {
                        if (villageMr.isBlank()) {
                            villageSuggestions.toList().take(8)
                        } else {
                            villageSuggestions.toList().filter {
                                it.first.contains(villageMr, ignoreCase = true) ||
                                it.second.contains(villageMr, ignoreCase = true)
                            }.take(8)
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = villageMr,
                            onValueChange = {
                                villageMr = it
                                villageDropdownExpanded = true
                            },
                            label = { Text("गाव / तालुका परिसर (मराठीत) *", fontSize = 11.sp) },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.sp),
                            trailingIcon = {
                                IconButton(onClick = { villageDropdownExpanded = !villageDropdownExpanded }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "निवडा")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = villageDropdownExpanded && filteredVillages.isNotEmpty(),
                            onDismissRequest = { villageDropdownExpanded = false },
                            properties = androidx.compose.ui.window.PopupProperties(focusable = false),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            filteredVillages.forEach { (mr, en) ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(text = mr, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GBText)
                                            Text(text = en, fontSize = 10.sp, color = GBGreyText)
                                        }
                                    },
                                    onClick = {
                                        villageMr = mr
                                        villageEn = en
                                        villageDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = villageEn,
                        onValueChange = { villageEn = it },
                        label = { Text("Village / Headquarters (English) *", fontSize = 11.sp) },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 13.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        text = if (isMarathi) "5. पूरक टीप (Notes / Extra Coordinates)" else "5. Notes & Guidelines",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = GBPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(if (isMarathi) "आपत्कालीन वेळेची सवय/इतर माहिती" else "Available time support notes", fontSize = 11.sp) },
                        textStyle = TextStyle(fontSize = 13.sp),
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Save / Cancel Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(if (isMarathi) "रद्द करा" else "Cancel", fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (nameMr.isBlank()) {
                            return@Button
                        }
                        onSave(
                            editingContact?.id ?: 0,
                            nameMr, nameEn.ifBlank { nameMr },
                            phone, phoneAlt,
                            category,
                            designationMr.ifBlank { "मदत अधिकारी" },
                            designationEn.ifBlank { "Response Agent" },
                            villageMr.ifBlank { "मुख्यालय" },
                            villageEn.ifBlank { "Headquarters" },
                            editingContact?.isDefault ?: false,
                            notes
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GBPrimary),
                    modifier = Modifier.testTag("save_contact_panel_btn")
                ) {
                    Text(if (isMarathi) "जतन करा" else "Save", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// Helper: Builds a clean bilingual string for instant messaging and social sharing
fun buildBilingualShareText(contact: EmergencyContact, isMarathi: Boolean): String {
    return if (isMarathi) {
        """
        🚨 *आपत्कालीन मदत संपर्क तपशील* 🚨
        ----------------------------------------------
        नाव: ${contact.nameMr}
        पद: ${contact.designationMr}
        विभाग: ${contact.category}
        पत्ता/गाव: ${contact.villageOrAreaMr}
        ----------------------------------------------
        📞 मुख्य फोन: ${contact.phone}
        ${if (contact.phoneAlt.isNotBlank()) "📱 पर्यायी फोन: " + contact.phoneAlt else ""}
        ${if (contact.notes.isNotBlank()) "📌 टीप: " + contact.notes else ""}
        
        *आपत्ती मदत संपर्क ॲप द्वारे पाठवलेला मेसेज.*
        """.trimIndent()
    } else {
        """
        🚨 *Emergency Disaster Response Contact* 🚨
        ----------------------------------------------
        Name: ${contact.nameEn}
        Post: ${contact.designationEn}
        Category: ${contact.category}
        Location: ${contact.villageOrAreaEn}
        ----------------------------------------------
        📞 Main Phone: ${contact.phone}
        ${if (contact.phoneAlt.isNotBlank()) "📱 Alt Phone: " + contact.phoneAlt else ""}
        ${if (contact.notes.isNotBlank()) "📌 Notes: " + contact.notes else ""}
        
        *Shared via Taluka Emergency Contact Directory.*
        """.trimIndent()
    }
}

@Composable
fun SyncStatusBanner(
    isOnline: Boolean,
    syncStatus: String,
    lastSyncedTime: String,
    onForceSync: () -> Unit,
    isMarathi: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("sync_status_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOnline) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
        ),
        border = BorderStroke(1.dp, if (isOnline) Color(0xFFC8E6C9) else Color(0xFFFFE0B2))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (isOnline) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                        contentDescription = "Sync State Icon",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isOnline) {
                            if (syncStatus == "SYNCING") {
                                if (isMarathi) "गुगल ड्राइव्हवरून डेटा सिंक होत आहे..." else "Syncing with Google Drive..."
                            } else {
                                if (isMarathi) "क्लाउड डेटा सिंक्रोनाइझ (कृतीशील)" else "Google Drive Synced (Active)"
                            }
                        } else {
                            if (isMarathi) "ऑफलाईन मोड (डेटा पूर्णपणे सुरक्षित)" else "Disaster Offline Mode (100% Cached)"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnline) Color(0xFF1B5E20) else Color(0xFFE65100),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isOnline) {
                            if (syncStatus == "SYNCING") {
                                if (isMarathi) "नवीन क्रमांकाचे संदर्भीय विश्लेषण सुरू आहे..." else "Downloading latest official databases..."
                            } else {
                                if (isMarathi) "अंतिम सिंक वेळ: $lastSyncedTime" else "Last dynamic sync: $lastSyncedTime"
                            }
                        } else {
                            if (isMarathi) "नेटवर्क नाही. स्थानिक रूम डेटाबेसमधून सर्व संपर्क उपलब्ध आहेत." else "No internet. Room DB secures immediate offline access!"
                        },
                        fontSize = 9.sp,
                        color = if (isOnline) Color(0xFF2E7D32) else Color(0xFFF57C00),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (isOnline) {
                IconButton(
                    onClick = onForceSync,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("force_sync_button")
                ) {
                    if (syncStatus == "SYNCING") {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF1B5E20),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Force Sync",
                            tint = Color(0xFF1B5E20),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScopeLevelSelector(
    selectedScope: String,
    onScopeSelect: (String) -> Unit,
    isMarathi: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
    ) {
        // Beautiful rounded pill-shaped Segmented Tab Row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFFE0E0E0).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val scopes = listOf(
                    Triple("ALL", if (isMarathi) "सर्व संपर्क" else "All Directory", Icons.Default.Apps),
                    Triple("TALUKA", if (isMarathi) "🏛️ तालुका स्तर" else "🏛️ Taluka Level", Icons.Default.AccountBalance),
                    Triple("VILLAGE", if (isMarathi) "🏡 गावस्तर" else "🏡 Village Level", Icons.Default.Home)
                )

                scopes.forEach { (scopeKey, scopeName, icon) ->
                    val isSelected = selectedScope == scopeKey
                    
                    // Animate the container color and text color for premium visual response
                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) GBPrimary else Color.Transparent,
                        label = "tab_bg"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else GBText,
                        label = "tab_text"
                    )

                    Surface(
                        onClick = { onScopeSelect(scopeKey) },
                        shape = RoundedCornerShape(10.dp),
                        color = containerColor,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp) // Large tactile target area for simple touch in distress
                            .testTag("scope_tab_$scopeKey")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = scopeName,
                                modifier = Modifier.size(14.dp),
                                tint = contentColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = scopeName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleSheetSyncSection(
    isMarathi: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    sheetUrl: String,
    onUrlChange: (String) -> Unit,
    onSyncClick: () -> Unit,
    onOpenTwoWaySyncPanel: () -> Unit,
    syncStatus: String,
    onDownloadTemplate: () -> Unit
) {
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "sheet_arrow_rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("google_sheets_sync_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD) // Soft premium blue
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFBBDEFB)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = "गुगल शीट सिंक",
                        tint = Color(0xFF0D47A1),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isMarathi) "📊 गुगल शीट सिंक व डेटा रचना मार्गदर्शक" else "📊 Google Sheets Sync & Data Structure Guide",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "विस्तार करा",
                    tint = Color(0xFF0D47A1),
                    modifier = Modifier.rotate(rotationState)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = Color(0xFF0D47A1).copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isMarathi) "1. मुख्य डेटाबेस रकामे (Columns) व रचना:" else "1. Core Database Columns & Mapping:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Table of columns
                    val columns = listOf(
                        "nameMr / nameEn" to (if (isMarathi) "कर्मचारी/रूग्णालय/कार्यालयाचे नाव (मराठी/इंग्रजी)" else "Name of Employee/Substation (Marathi/English)"),
                        "phone / phoneAlt" to (if (isMarathi) "मुख्य मोबाईल क्रमांक आणि इतर पर्यायी क्रमांक" else "Primary Mobile and Secondary Contact Number"),
                        "category" to (if (isMarathi) "श्रेणी (आपोआप आपत्कालीन सेवा, प्रशासकीय किंवा स्थानिक गटांत विभागले जाते): RESCUE, MEDICAL, ADMIN, POLICE, FIRE, MACHINERY, UTILITY, RELIEF, VOLUNTEER" else "Category code (Auto maps to Emergency Services, Admin, or Local Reps): RESCUE, MEDICAL, ADMIN, POLICE, FIRE, MACHINERY, UTILITY, RELIEF, VOLUNTEER"),
                        "designationMr / designationEn" to (if (isMarathi) "अधिकारी-कर्मचाऱ्यांचे पद (मराठी/इंग्रजी)" else "Officer's Designation (Marathi/English)"),
                        "villageOrAreaMr / villageOrAreaEn" to (if (isMarathi) "नाव ग्रामीण पातळीसाठी गाव किंवा 'तालुका (सर्व)'" else "Village name, or 'Taluka (All)' for headquarters"),
                        "isDefault" to (if (isMarathi) "प्रशासकीय संपर्क डीफॉल्ट दाखवायचा असल्यास 'TRUE' ठेवा" else "Set 'TRUE' to load as persistent contact"),
                        "notes" to (if (isMarathi) "मदत व्याप्ती, विशेष सूचना किंवा पत्ता माहिती" else "Helpline timing, instructions or office location")
                    )

                    columns.forEach { (colName, colDesc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "• $colName:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0),
                                modifier = Modifier.width(135.dp)
                            )
                            Text(
                                text = colDesc,
                                fontSize = 10.sp,
                                color = Color(0xFF1E88E5),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isMarathi) "2. नवीन तालुक्यासाठी कस्टमायझेशन व स्वयंचलित सिंक कशी कार्य करते?" else "2. How automatic sync & multi-taluka design works:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Text(
                        text = if (isMarathi) {
                            "• गुगल शीटवर नवीन माहिती लिहिताच, पार्श्वभूमीत इंटरनेट मिळताच ॲप आपोआप ती माहिती स्थानिक SQLite Room DB मध्ये सिंक्रोनाइझ करेल.\n" +
                            "• प्रत्येक सिंक नंतर युझरला अचूक वेळ दाखवली जाईल जेणेकरून आपत्ती दरम्यान जुना व चुकीचा डेटा वापरला जाणार नाही.\n" +
                            "• नवीन तालुक्यासाठी फक्त त्या तालुक्याची लिंक टाकून संपूर्ण ॲपची मार्गदर्शिका आणि संपर्कांचे रूपांतर दुसऱ्या तालुक्यात सहज करता येईल."
                        } else {
                            "• Editing data on Google Sheets automatically propagates to the local SQLite DB inside the app via auto network detection.\n" +
                            "• App sync timestamps show exactly when data was updated, essential during live floods/disasters.\n" +
                            "• Simply change the Cloud URL in Settings to reuse this app structure for any other taluka globally."
                        },
                        fontSize = 10.sp,
                        color = Color(0xFF1565C0),
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFF0D47A1).copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isMarathi) "🔗 गुगल शीट वेब पब्लिश लिंक (CSV URL):" else "🔗 Google Sheets Web Publish Link (CSV URL):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = sheetUrl,
                        onValueChange = onUrlChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_sheet_url_input"),
                        placeholder = {
                            Text(
                                text = "https://docs.google.com/spreadsheets/d/.../pub?output=csv",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF1565C0),
                            unfocusedBorderColor = Color(0xFFBBDEFB)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Multi-lingual instructions on how to get CSV Publish link
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEBF5FB)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isMarathi) "💡 लिंक मिळवण्याची सोपी पद्धत:" else "💡 Steps to get CSV link:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B4F72)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isMarathi) {
                                    "1. गुगल शीट उघडा -> File -> Share -> Publish to web निवडा.\n" +
                                    "2. 'Entire Document' आणि 'Comma-separated values (.csv)' निवडा.\n" +
                                    "3. 'Publish' दाबा आणि आलेली लिंक कॉपी करून येथे पेस्ट करा!"
                                } else {
                                    "1. Open Sheet -> File -> Share -> Publish to web.\n" +
                                    "2. Select 'Entire Document' and 'Comma-separated values (.csv)'.\n" +
                                    "3. Click 'Publish', then copy & paste the generated link here."
                                },
                                fontSize = 10.sp,
                                color = Color(0xFF2E86C1),
                                lineHeight = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ⚠️ SYNC REVERSE DIRECTION EXPLANATION CARD (MANDATORY RESOLUTION)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)), // Alert soft yellow
                        border = BorderStroke(1.dp, Color(0xFFFFEBA2)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Sync Info",
                                    tint = Color(0xFF856404),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isMarathi) "⚠️ महत्वाची टीप (वन-वे सिंक):" else "⚠️ Important Note (One-Way Sync):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF856404)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isMarathi) {
                                    "• गुगल शीट सिंक सुरक्षा कारणांमुळे 'वन-वे' (फक्त डाऊनलोड) असते. मोबाईलमधील नवीन संपर्क गुगल शीटमध्ये आपोआप जात नाहीत.\n" +
                                    "• मोबाईलमध्ये भरलेला नवीन डेटा गुगल शीटमध्ये अपडेट करण्यासाठी, खाली असलेल्या 'ऑफलाईन बॅकअप आणि डेटा मेंटेनन्स' विभागातून 'संपर्क ऑफलाइन फाईल निर्यात करा (Export Actual CSV)' बटण दाबून फाईल सेव्ह करा. त्यानंतर ती फाईल तुमच्या गुगल शीटमध्ये कॉपी-पेस्ट करा!"
                                } else {
                                    "• Google Sheets sync is 'one-way' (download only) to secure your Google drive. Direct writes from mobile to Sheet are restricted.\n" +
                                    "• To upload contacts added on mobile to Google Sheets, use the 'Export Actual CSV' option under the 'Local Offline Backup' section below, and paste/import that data into your Sheet."
                                },
                                fontSize = 10.sp,
                                color = Color(0xFF856404),
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onSyncClick,
                            enabled = syncStatus != "SYNCING",
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32), // High contrast premium green
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("import_sync_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (syncStatus == "SYNCING") {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Sync",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (syncStatus == "SYNCING") {
                                        if (isMarathi) "सिंक होत आहे..." else "Syncing..."
                                    } else {
                                        if (isMarathi) "सिंक करा" else "Sync Now"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = onDownloadTemplate,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1565C0),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(44.dp)
                                .testTag("download_sheet_template_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download Template",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isMarathi) "नमुना डाउनलोड" else "Download Template",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onOpenTwoWaySyncPanel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GBPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("open_two_way_sync_panel_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Cloud Sync Panel",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isMarathi) "टू-वे सिंक कंट्रोल पॅनेल उघडा 🔄" else "Open Two-Way Sync Control Panel 🔄",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleSheetsQuickConfigDialog(
    currentUrl: String,
    onUrlChange: (String) -> Unit,
    onSyncClick: () -> Unit,
    twoWayUrl: String,
    onTwoWayUrlChange: (String) -> Unit,
    onPushClick: () -> Unit,
    syncStatus: String,
    onDismiss: () -> Unit,
    isMarathi: Boolean
) {
    var tempUrl by remember(currentUrl) { mutableStateOf(currentUrl) }
    var tempTwoWayUrl by remember(twoWayUrl) { mutableStateOf(twoWayUrl) }
    var showInstructions by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_close_btn")
            ) {
                Text(
                    text = if (isMarathi) "पूर्ण झाले" else "Done",
                    fontWeight = FontWeight.Bold,
                    color = GBPrimary
                )
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = "Cloud Setup",
                    tint = GBPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isMarathi) "गुगल शीट टू-वे सिंक" else "Google Sheet Two-Way Sync",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GBText
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // PART 1: READ / DOWNLOAD
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, GBBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isMarathi) "📥 1. गुगल शीटवरून डेटा घ्या (Import)" else "📥 1. Read from Google Sheet (Import)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GBPrimary
                        )

                        Text(
                            text = if (isMarathi) 
                                "गुगल शीटची नेहमीची शेअर लिंक (Anyone with link can view) येथे पेस्ट करा:" 
                            else 
                                "Paste standard Google Sheet link (Anyone with link can view) below:",
                            fontSize = 11.sp,
                            color = GBText
                        )

                        OutlinedTextField(
                            value = tempUrl,
                            onValueChange = { tempUrl = it },
                            placeholder = {
                                Text(
                                    text = "https://docs.google.com/spreadsheets/d/...",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 11.sp),
                            maxLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GBPrimary,
                                unfocusedBorderColor = GBBorder
                            )
                        )

                        Button(
                            onClick = {
                                onUrlChange(tempUrl)
                                onSyncClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GBPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isMarathi) "शीटवरून माहिती डाऊनलोड करा" else "Download from Sheet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // PART 2: WRITE / UPLOAD (TWO-WAY SYNC)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, GBBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isMarathi) "📤 2. गुगल शीटवर डेटा पाठवा (Export / Upload)" else "📤 2. Push to Google Sheet (Export / Upload)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D9488)
                        )

                        Text(
                            text = if (isMarathi) 
                                "तुमच्या गुगल ॲप्स स्क्रिप्टची (Apps Script Web App) वेब लिंक येथे पेस्ट करा:" 
                            else 
                                "Paste your deployed Google Apps Script Web App URL below:",
                            fontSize = 11.sp,
                            color = GBText
                        )

                        OutlinedTextField(
                            value = tempTwoWayUrl,
                            onValueChange = { tempTwoWayUrl = it },
                            placeholder = {
                                Text(
                                    text = "https://script.google.com/macros/s/.../exec",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 11.sp),
                            maxLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF0D9488),
                                unfocusedBorderColor = GBBorder
                            )
                        )

                        Button(
                            onClick = {
                                onTwoWayUrlChange(tempTwoWayUrl)
                                onPushClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isMarathi) "गूगल शीटवर बदल अपलोड करा" else "Upload Changes to Sheet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // STATUS BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = when (syncStatus) {
                                "SYNCING" -> Color(0xFFFFF3E0)
                                "SUCCESS" -> Color(0xFFE8F5E9)
                                "FAILED" -> Color(0xFFFFEBEE)
                                else -> Color(0xFFF5F5F5)
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (syncStatus) {
                            "SYNCING" -> Icons.Default.CloudSync
                            "SUCCESS" -> Icons.Default.CheckCircle
                            "FAILED" -> Icons.Default.Error
                            else -> Icons.Default.Info
                        },
                        contentDescription = "Status",
                        tint = when (syncStatus) {
                            "SYNCING" -> Color(0xFFEF6C00)
                            "SUCCESS" -> Color(0xFF2E7D32)
                            "FAILED" -> Color(0xFFC62828)
                            else -> GBGreyText
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (syncStatus) {
                            "SYNCING" -> if (isMarathi) "क्रिया सुरू आहे... कृपया थांबा." else "Processing, please wait..."
                            "SUCCESS" -> if (isMarathi) "कृती यशस्वीरित्या पूर्ण झाली!" else "Operation successful!"
                            "FAILED" -> if (isMarathi) "प्रक्रिया अयशस्वी! लिंक आणि कनेक्शन तपासा." else "Operation failed! Check links & network."
                            else -> if (isMarathi) "इच्छित पर्याय निवडून सिंक करा." else "Choose an action to sync."
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (syncStatus) {
                            "SYNCING" -> Color(0xFFEF6C00)
                            "SUCCESS" -> Color(0xFF2E7D32)
                            "FAILED" -> Color(0xFFC62828)
                            else -> GBText
                        }
                    )
                }

                // INSTRUCTIONS CARD
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, GBBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showInstructions = !showInstructions }
                        ) {
                            Icon(
                                imageVector = if (showInstructions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Instructions",
                                tint = GBPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isMarathi) "🛠️ टू-वे सिंक कसा सेट करायचा? (Setup Guide)" else "🛠️ How to setup Two-Way Sync?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GBPrimary
                            )
                        }

                        if (showInstructions) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isMarathi) {
                                    "1. तुमच्या गुगल शीटमध्ये Extensions -> Apps Script वर जा.\n" +
                                    "2. तेथील कोड काढून खालील कोड पेस्ट करा आणि 'Deploy -> New Deployment -> Web App' म्हणून प्रसिद्ध करा.\n" +
                                    "3. 'Who has access' मध्ये 'Anyone' निवडा.\n" +
                                    "4. मिळालेली Web App URL वरील भाग 2 मध्ये पेस्ट करा."
                                } else {
                                    "1. In your Google Sheet, go to Extensions -> Apps Script.\n" +
                                    "2. Erase existing code, paste the script below, and click 'Deploy -> New Deployment -> Web App'.\n" +
                                    "3. Select 'Anyone' under 'Who has access'.\n" +
                                    "4. Paste the generated Web App URL into Part 2 above."
                                },
                                fontSize = 10.sp,
                                color = GBText,
                                lineHeight = 13.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    val appsScriptCode = """
                                        function doPost(e) {
                                          try {
                                            var data = JSON.parse(e.postData.contents);
                                            var sheet = SpreadsheetApp.getActiveSpreadsheet().getSheets()[0];
                                            sheet.clearContents();
                                            
                                            // Write headers
                                            var headers = ["nameMr", "nameEn", "phone", "phoneAlt", "category", "designationMr", "designationEn", "villageOrAreaMr", "villageOrAreaEn", "isDefault", "notes"];
                                            sheet.appendRow(headers);
                                            
                                            // Write data rows
                                            for (var i = 0; i < data.length; i++) {
                                              var item = data[i];
                                              sheet.appendRow([
                                                item.nameMr || "",
                                                item.nameEn || "",
                                                item.phone || "",
                                                item.phoneAlt || "",
                                                item.category || "",
                                                item.designationMr || "",
                                                item.designationEn || "",
                                                item.villageOrAreaMr || "",
                                                item.villageOrAreaEn || "",
                                                item.isDefault ? "true" : "false",
                                                item.notes || ""
                                              ]);
                                            }
                                            return ContentService.createTextOutput(JSON.stringify({status: "success", count: data.length}))
                                              .setMimeType(ContentService.MimeType.JSON);
                                          } catch (err) {
                                            return ContentService.createTextOutput(JSON.stringify({status: "error", message: err.toString()}))
                                              .setMimeType(ContentService.MimeType.JSON);
                                          }
                                        }
                                    """.trimIndent()
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(appsScriptCode))
                                    Toast.makeText(context, "स्क्रिप्ट कोड कॉपी झाला!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GBText),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Code",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isMarathi) "ॲप्स स्क्रिप्ट कोड कॉपी करा" else "Copy Apps Script Code",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun VolunteerRegistrationDialog(
    isMarathi: Boolean,
    onDismiss: () -> Unit,
    onRegister: (
        nameMr: String, nameEn: String,
        phone: String, phoneAlt: String,
        designationMr: String, designationEn: String,
        villageOrAreaMr: String, villageOrAreaEn: String,
        notes: String,
        publishDirectly: Boolean
    ) -> Unit
) {
    var nameMr by remember { mutableStateOf("") }
    var nameEn by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var phoneAlt by remember { mutableStateOf("") }
    var designationMr by remember { mutableStateOf("") }
    var designationEn by remember { mutableStateOf("") }
    var villageMr by remember { mutableStateOf("") }
    var villageEn by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var publishDirectly by remember { mutableStateOf(false) }

    var showErrorMsg by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("volunteer_form_surface"),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = "Volunteer Icon",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isMarathi) "🤝 स्वयंसेवक नोंदणी फॉर्म" else "🤝 Volunteer Registration",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = GBText
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp).testTag("close_volunteer_form")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = GBGreyText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isMarathi) 
                        "आपत्ती काळात जसे की पूर किंवा इतर आपत्कालीन स्थितीत मदत करण्यासाठी आपली माहिती भरा. प्रशासक पडताळणी करून ही माहिती यादीत समाविष्ट करतील." 
                    else 
                        "Enter your details to register as a disaster volunteer. Admins will verify your entries before showing in the public directory.",
                    fontSize = 11.sp,
                    color = GBGreyText,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (showErrorMsg.isNotBlank()) {
                        Text(
                            text = showErrorMsg,
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 1. Name Marathi
                    Text(
                        text = if (isMarathi) "1. आपले संपूर्ण नाव (मराठीत)*" else "1. Full Name (In Marathi)*",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GBText
                    )
                    OutlinedTextField(
                        value = nameMr,
                        onValueChange = { nameMr = it; showErrorMsg = "" },
                        placeholder = { Text(if (isMarathi) "उदा. रामराव शामराव पाटील" else "e.g. Ramrao Shamrao Patil") },
                        modifier = Modifier.fillMaxWidth().testTag("volunteer_name_mr"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GBPrimary,
                            unfocusedBorderColor = GBBorder
                        )
                    )

                    // 2. Name English
                    Text(
                        text = if (isMarathi) "2. आपले संपूर्ण नाव (इंग्रजीत)" else "2. Full Name (In English)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GBText
                    )
                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        placeholder = { Text("e.g. Ramrao Shamrao Patil") },
                        modifier = Modifier.fillMaxWidth().testTag("volunteer_name_en"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GBPrimary,
                            unfocusedBorderColor = GBBorder
                        )
                    )

                    // 3. Mobile
                    Text(
                        text = if (isMarathi) "3. मोबाईल क्रमांक (10 अंकी)*" else "3. Mobile Number (10 digit)*",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GBText
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; showErrorMsg = "" },
                        placeholder = { Text("e.g. 9876543210") },
                        modifier = Modifier.fillMaxWidth().testTag("volunteer_phone"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GBPrimary,
                            unfocusedBorderColor = GBBorder
                        )
                    )

                    // 4. Phone Alt
                    Text(
                        text = if (isMarathi) "4. पर्यायी मोबाईल क्रमांक" else "4. Alternate Mobile Number",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GBText
                    )
                    OutlinedTextField(
                        value = phoneAlt,
                        onValueChange = { phoneAlt = it },
                        placeholder = { Text("e.g. 9876543211") },
                        modifier = Modifier.fillMaxWidth().testTag("volunteer_phone_alt"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GBPrimary,
                            unfocusedBorderColor = GBBorder
                        )
                    )

                    // 5. Designation Marathi
                    Text(
                        text = if (isMarathi) "5. आपले कौशल्य / पद (मराठीत)*" else "5. Special Skill / Title (In Marathi)*",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GBText
                    )
                    OutlinedTextField(
                        value = designationMr,
                        onValueChange = { designationMr = it; showErrorMsg = "" },
                        placeholder = { Text(if (isMarathi) "उदा. उत्तम पोहणारा / डॉक्टर / बचाव पथक" else "e.g. Expert Swimmer / Doctor / Rescuer") },
                        modifier = Modifier.fillMaxWidth().testTag("volunteer_des_mr"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GBPrimary,
                            unfocusedBorderColor = GBBorder
                        )
                    )

                    // 6. Designation English
                    Text(
                        text = if (isMarathi) "6. आपले कौशल्य / पद (इंग्रजीत)" else "6. Special Skill / Title (In English)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GBText
                    )
                    OutlinedTextField(
                        value = designationEn,
                        onValueChange = { designationEn = it },
                        placeholder = { Text("e.g. Expert Swimmer / Medical Officer") },
                        modifier = Modifier.fillMaxWidth().testTag("volunteer_des_en"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GBPrimary,
                            unfocusedBorderColor = GBBorder
                        )
                    )

                    // 7. Village Marathi
                    Text(
                        text = if (isMarathi) "7. गाव किंवा परिसर (मराठीत)*" else "7. Village or Area (In Marathi)*",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GBText
                    )
                    OutlinedTextField(
                        value = villageMr,
                        onValueChange = { villageMr = it; showErrorMsg = "" },
                        placeholder = { Text(if (isMarathi) "उदा. मंगळवेढा / भोसले गल्ली" else "e.g. Mangalwedha") },
                        modifier = Modifier.fillMaxWidth().testTag("volunteer_vill_mr"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GBPrimary,
                            unfocusedBorderColor = GBBorder
                        )
                    )

                    // 8. Village English
                    Text(
                        text = if (isMarathi) "8. गाव किंवा परिसर (इंग्रजीत)" else "8. Village or Area (In English)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GBText
                    )
                    OutlinedTextField(
                        value = villageEn,
                        onValueChange = { villageEn = it },
                        placeholder = { Text("e.g. Mangalwedha") },
                        modifier = Modifier.fillMaxWidth().testTag("volunteer_vill_en"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GBPrimary,
                            unfocusedBorderColor = GBBorder
                        )
                    )

                    // 9. Notes
                    Text(
                        text = if (isMarathi) "9. विशेष नोंद / संपर्क वेळ / पत्ता" else "9. Additional Info / Availability / Location Url",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GBText
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text(if (isMarathi) "उदा. २४ तास उपलब्ध, बोटीची सोय आहे." else "e.g. Available 24/7, has a rubber boat.") },
                        modifier = Modifier.fillMaxWidth().testTag("volunteer_notes"),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GBPrimary,
                            unfocusedBorderColor = GBBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, GBBorder)
                    ) {
                        Text(
                            text = if (isMarathi) "रद्द करा" else "Cancel",
                            fontWeight = FontWeight.Bold,
                            color = GBGreyText
                        )
                    }

                    Button(
                        onClick = {
                            if (nameMr.isBlank() || phone.isBlank() || designationMr.isBlank() || villageMr.isBlank()) {
                                showErrorMsg = if (isMarathi) "कृपया सर्व आवश्यक (*) रकाने भरा!" else "Please fill all required (*) fields!"
                            } else if (phone.length < 8) {
                                showErrorMsg = if (isMarathi) "कृपया योग्य मोबाईल नंबर टाका!" else "Please enter a valid mobile number!"
                            } else {
                                onRegister(
                                    nameMr, nameEn,
                                    phone, phoneAlt,
                                    designationMr, designationEn,
                                    villageMr, villageEn,
                                    notes,
                                    publishDirectly
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isMarathi) "नोंदणी सबमिट करा" else "Submit Registration",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PendingVolunteersSection(
    isMarathi: Boolean,
    pendingContacts: List<EmergencyContact>,
    onApprove: (EmergencyContact) -> Unit,
    onReject: (EmergencyContact) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pending_volunteers_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, if (pendingContacts.isNotEmpty()) Color(0xFFFF2D55) else Color(0xFFCBD5E1))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Pending Approval",
                        tint = if (pendingContacts.isNotEmpty()) Color(0xFFFF2D55) else Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isMarathi) "⏳ प्रलंबित नोंदणी व बदल मंजुरी" else "⏳ Pending Approvals & Verifications",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF0F172A)
                    )
                }
                if (pendingContacts.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF2D55)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${pendingContacts.size}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isMarathi) 
                    "नागरिकांनी नवीन जोडलेले, बदललेले किंवा हटवण्यासाठी विनंती केलेले संपर्क तपासून मुख्य यादीत समाविष्ट किंवा डिलीट करा."
                else 
                    "Review, edit, or delete requests for contacts and volunteers to include or remove them in the public emergency list.",
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (pendingContacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isMarathi) 
                            "👍 सर्व संपर्क व बदल मंजूर आहेत. प्रलंबित विनंत्या नाहीत." 
                        else 
                            "👍 All contacts & changes approved. No pending requests.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D9488)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    pendingContacts.forEach { contact ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9DB)), // Subtle yellow for pending
                            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                // Request type badge
                                val requestTypeLabel = when {
                                    contact.isPendingDelete -> if (isMarathi) "⚠️ हटवण्याची विनंती (Deletion Request)" else "⚠️ Deletion Request"
                                    contact.updateForContactId != null -> if (isMarathi) "✏️ बदलाची विनंती (Update Request)" else "✏️ Update Request"
                                    else -> if (isMarathi) "🏥 नवीन संपर्क/स्वयंसेवक नोंदणी (New Contact/Volunteer)" else "🏥 New Contact/Volunteer"
                                }
                                val requestColor = when {
                                    contact.isPendingDelete -> Color(0xFFFF2D55) // Crimson
                                    contact.updateForContactId != null -> Color(0xFF2563EB) // Electric Blue
                                    else -> Color(0xFF0D9488) // Teal
                                }

                                Surface(
                                    color = requestColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, requestColor.copy(alpha = 0.3f)),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Text(
                                        text = requestTypeLabel,
                                        color = requestColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = contact.nameMr,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0F172A)
                                )
                                if (contact.nameEn != contact.nameMr) {
                                    Text(
                                        text = contact.nameEn,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    Text(
                                        text = (if (isMarathi) "📞 फोन: " else "📞 Phone: ") + contact.phone,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    if (contact.phoneAlt.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "/ " + contact.phoneAlt,
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                                Text(
                                    text = (if (isMarathi) "📍 गाव: " else "📍 Village: ") + contact.villageOrAreaMr + " (" + contact.villageOrAreaEn + ")",
                                    fontSize = 12.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = (if (isMarathi) "🎖️ कौशल्य: " else "🎖️ Skill: ") + contact.designationMr + " (" + contact.designationEn + ")",
                                    fontSize = 12.sp,
                                    color = Color(0xFF0F172A)
                                )
                                if (contact.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = (if (isMarathi) "📝 नोंद: " else "📝 Note: ") + contact.notes,
                                        fontSize = 11.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onReject(contact) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Reject",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isMarathi) "नाकारा" else "Reject",
                                            color = Color(0xFFDC2626),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = { onApprove(contact) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1FAE5)),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1.2f).height(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Approve",
                                            tint = Color(0xFF059669),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isMarathi) "मंजूर करा" else "Approve",
                                            color = Color(0xFF059669),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}