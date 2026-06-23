package com.example

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
    val searchQuery by contactViewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by contactViewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedVillage by contactViewModel.selectedVillage.collectAsStateWithLifecycle()
    val availableVillages by contactViewModel.availableVillages.collectAsStateWithLifecycle()
    val selectedScope by contactViewModel.selectedScope.collectAsStateWithLifecycle()
    val isOnline by contactViewModel.isOnline.collectAsStateWithLifecycle()
    val syncStatus by contactViewModel.syncStatus.collectAsStateWithLifecycle()
    val lastSyncedTime by contactViewModel.lastSyncedTime.collectAsStateWithLifecycle()
    val googleSheetsUrl by contactViewModel.googleSheetsUrl.collectAsStateWithLifecycle()
    val talukaName by contactViewModel.talukaName.collectAsStateWithLifecycle()
    val talukaNameEn by contactViewModel.talukaNameEn.collectAsStateWithLifecycle()

    // State parameters for UI flow
    var isMarathi by remember { mutableStateOf(true) }
    var activeTab by remember { mutableStateOf("contacts") }
    var currentHelpSectionExpanded by remember { mutableStateOf(false) }
    var googleSheetsSectionExpanded by remember { mutableStateOf(false) }
    var expandedContactId by remember { mutableStateOf<Int?>(null) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var isAdminUnlocked by remember { mutableStateOf(false) }
    var adminPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showTalukaEditDialogSystem by remember { mutableStateOf(false) }

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
                    val csvHeaders = "nameMr,nameEn,phone,phoneAlt,category,designationMr,designationEn,villageOrAreaMr,villageOrAreaEn,isDefault,notes\n"
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
        floatingActionButton = {
            if (activeTab == "contacts" && isAdminUnlocked) {
                FloatingActionButton(
                    onClick = {
                        editingContact = null
                        showFormDialog = true
                    },
                    containerColor = GBPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp), // geometric balance FAB shape
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .testTag("add_contact_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "नवीन संपर्क"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isMarathi) "नवीन संपर्क +" else "Add Contact +",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = activeTab == "contacts",
                    onClick = { activeTab = "contacts" },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ContactPhone,
                            contentDescription = if (isMarathi) "थेट संपर्क" else "Contacts"
                        )
                    },
                    label = {
                        Text(
                            text = if (isMarathi) "थेट संपर्क" else "Contacts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = GBPrimary,
                        indicatorColor = GBPrimary,
                        unselectedIconColor = GBGreyText,
                        unselectedTextColor = GBGreyText
                    )
                )
                NavigationBarItem(
                    selected = activeTab == "settings",
                    onClick = { activeTab = "settings" },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = if (isMarathi) "मार्गदर्शक व सेटिंग्स" else "Guides & Tools"
                        )
                    },
                    label = {
                        Text(
                            text = if (isMarathi) "मार्गदर्शक व सेटिंग्स" else "Guides & Tools",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = GBPrimary,
                        indicatorColor = GBPrimary,
                        unselectedIconColor = GBGreyText,
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
                // TAB 1: 100% CONTACT SEARCH ENGINE
                ContactsListArea(
                    contacts = contacts,
                    expandedContactId = expandedContactId,
                    onContactClick = { id ->
                        expandedContactId = if (expandedContactId == id) null else id
                    },
                    onCallClick = { phone ->
                        try {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(dialIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "कॉल करता आला नाही", Toast.LENGTH_SHORT).show()
                        }
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
                        contactViewModel.deleteContact(contact)
                    },
                    isMarathi = isMarathi,
                    selectedScope = selectedScope,
                    searchQuery = searchQuery,
                    onSearchChange = { contactViewModel.updateSearchQuery(it) },
                    onLanguageToggle = { isMarathi = !isMarathi },
                    onCallEmergency = { num ->
                        try {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
                            context.startActivity(dialIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "कॉल करता आला नाही", Toast.LENGTH_SHORT).show()
                        }
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
                            if (isMarathi) "डेटा सिंक सुरू होत आहे..." else "Data sync starting...",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    talukaName = talukaName,
                    talukaNameEn = talukaNameEn,
                    isAdminMode = isAdminUnlocked,
                    isOnline = isOnline,
                    syncStatus = syncStatus,
                    lastSyncedTime = lastSyncedTime
                )
            } else {
                // TAB 2: SYSTEM GUIDES, IMPORT/EXPORT TOOLS & SYSTEM MAINTENANCE
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
                        onLanguageToggle = { isMarathi = !isMarathi },
                        onCallEmergency = { num ->
                            try {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
                                context.startActivity(dialIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "कॉल करता आला नाही", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onCloudSyncClick = {
                            contactViewModel.triggerBackgroundSync()
                            Toast.makeText(
                                context,
                                if (isMarathi) "डेटा सिंक सुरू होत आहे..." else "Data sync starting...",
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
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
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
                                        modifier = Modifier.height(42.dp)
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

                        // GOOGLE SHEET SYNC & DATA MANAGEMENT CORNER (Visible only to Admin)
                        GoogleSheetSyncSection(
                            isMarathi = isMarathi,
                            isExpanded = googleSheetsSectionExpanded,
                            onToggleExpand = { googleSheetsSectionExpanded = !googleSheetsSectionExpanded },
                            sheetUrl = googleSheetsUrl,
                            onUrlChange = { contactViewModel.updateGoogleSheetsUrl(it) },
                            onSyncClick = { contactViewModel.triggerBackgroundSync() },
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
                                        "आपत्तीच्या वेळेस इंटरनेट बंद असल्यास ब्लूटूथ किंवा जवळून फाईल शेअर करून संपूर्ण संपर्क डेटाबेस आयात किंवा निर्यात करा. डेटा सुरक्षित आणि १००% ऑफलाईन साठवला जाईल."
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

        // 6. ADD & EDIT CONTACT FORM DIALOG
        if (showFormDialog) {
            ContactFormDialog(
                editingContact = editingContact,
                isMarathi = isMarathi,
                onDismiss = { showFormDialog = false },
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
                        notes = notes
                    )
                    showFormDialog = false
                }
            )
        }

        // 7. GOOGLE SHEETS QUICK SYNC & LINK CONFIG DIALOG
        if (showSyncDialog) {
            GoogleSheetsQuickConfigDialog(
                currentUrl = googleSheetsUrl,
                onUrlChange = { contactViewModel.updateGoogleSheetsUrl(it) },
                onSyncClick = { contactViewModel.triggerBackgroundSync() },
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
            // Action Controls Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cloud Sync Icon Button
                IconButton(
                    onClick = onCloudSyncClick,
                    modifier = Modifier.size(32.dp).testTag("cloud_sync_header_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Compact Language Toggle Button ("E" or "म")
                Button(
                    onClick = onLanguageToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(26.dp).testTag("language_toggle_btn")
                ) {
                    Text(
                        text = if (isMarathi) "E" else "म",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }

            // Centralized Large Title Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Centered Shield Icon (smaller)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "आपत्ती नियंत्रण",
                        tint = AmberAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isMarathi) "आपत्ती व्यवस्थापन" else "Disaster Response",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(1.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .testTag("taluka_name_header_row"),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isMarathi) "तालुका - $talukaName" else "Taluka - $talukaNameEn",
                        fontSize = 14.sp,
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // SOS EMERGENCY ROW (3 COLUMNS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val emergencyDials = listOf(
                    Triple(if (isMarathi) "पोलीस (१००)" else "Police (100)", "100", Icons.Default.LocalPolice),
                    Triple(if (isMarathi) "आरोग्य (१०८)" else "Medical (108)", "108", Icons.Default.MedicalServices),
                    Triple(if (isMarathi) "अग्निशमन (१०१)" else "Fire (101)", "101", Icons.Default.FireTruck)
                )

                emergencyDials.forEach { (label, number, icon) ->
                    Surface(
                        onClick = { onCallEmergency(number) },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
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
    contactCount: Int
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
                if (searchQuery.isNotEmpty()) {
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
                .height(44.dp)
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
        Pair("ALL", if (isMarathi) "सर्व संपर्क" else "All Categories"),
        Pair("EMERG_SERVICES", if (isMarathi) "🚨 आपत्कालीन सेवा" else "Emergency Services"),
        Pair("ADMIN_OFFICERS", if (isMarathi) "🏛️ प्रशासकीय अधिकारी" else "Admin Officers"),
        Pair("LOCAL_REPS", if (isMarathi) "🏡 स्थानिक प्रतिनिधी व स्वयंसेवक" else "Local Representatives")
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("category_filter_row"),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { (key, label) ->
            val isSelected = selectedCategory == key
            val containerColor = if (isSelected) {
                when (key) {
                    "EMERG_SERVICES" -> Color(0xFFEF4444)
                    "ADMIN_OFFICERS" -> Color(0xFF8B5CF6)
                    "LOCAL_REPS" -> Color(0xFF10B981)
                    else -> Color(0xFF2563EB)
                }
            } else {
                Color.White
            }
            val contentColor = if (isSelected) Color.White else GBGreyText
            val borderColor = if (isSelected) Color.Transparent else GBBorder

            Surface(
                onClick = { onCategorySelect(key) },
                shape = RoundedCornerShape(14.dp),
                color = containerColor,
                contentColor = contentColor,
                border = BorderStroke(1.dp, borderColor),
                tonalElevation = if (isSelected) 4.dp else 1.dp,
                modifier = Modifier
                    .height(38.dp)
                    .testTag("category_chip_$key")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when (key) {
                        "EMERG_SERVICES" -> Icons.Default.LocalHospital
                        "ADMIN_OFFICERS" -> Icons.Default.AccountBalance
                        "LOCAL_REPS" -> Icons.Default.People
                        else -> Icons.Default.List
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
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
    isAdminMode: Boolean = false,
    isOnline: Boolean,
    syncStatus: String,
    lastSyncedTime: String
) {
    val talukaContacts = contacts.filter { isTalukaLevelContact(it) }
    val villageContacts = contacts.filter { !isTalukaLevelContact(it) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- FROZEN / FIXED TOP REGION (Does not scroll) ---
        // 1. Header Section
        HeaderSection(
            isMarathi = isMarathi,
            talukaName = talukaName,
            talukaNameEn = talukaNameEn,
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

            // 2. Search & Filter Section (Moved inside LazyColumn to scroll)
            item {
                SearchAndFilterSection(
                    searchQuery = searchQuery,
                    onSearchChange = onSearchChange,
                    isMarathi = isMarathi,
                    contactCount = contacts.size
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
                                onHeaderClick = { onContactClick(contact.id) },
                                onCallClick = { onCallClick(contact.phone) },
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
                                onHeaderClick = { onContactClick(contact.id) },
                                onCallClick = { onCallClick(contact.phone) },
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
    onHeaderClick: () -> Unit,
    onCallClick: () -> Unit,
    onShareClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isMarathi: Boolean,
    isAdmin: Boolean = false
) {
    // Determine category badge attributes and iconography mapped to unified high-stress navigation groups
    val mappedGroup = when (contact.category.uppercase()) {
        "POLICE", "MEDICAL", "FIRE", "RESCUE" -> "EMERG_SERVICES"
        "ADMIN" -> "ADMIN_OFFICERS"
        else -> "LOCAL_REPS"
    }

    val (categoryLabel, iconBg, iconTint, categoryIcon) = when (mappedGroup) {
        "EMERG_SERVICES" -> {
            val typeStr = when (contact.category.uppercase()) {
                "POLICE" -> if (isMarathi) "सुरक्षा" else "Police"
                "MEDICAL" -> if (isMarathi) "आरोग्य" else "Medical"
                "FIRE" -> if (isMarathi) "अग्निशमन" else "Fire"
                else -> if (isMarathi) "बचाव" else "Rescue"
            }
            quadruplet(
                if (isMarathi) "🚨 आपत्कालीन सेवा ($typeStr)" else "🚨 Emergency Service ($typeStr)",
                Color(0xFFFFEBEE), Color(0xFFC62828),
                when (contact.category.uppercase()) {
                    "POLICE" -> Icons.Default.LocalPolice
                    "MEDICAL" -> Icons.Default.MedicalServices
                    "FIRE" -> Icons.Default.FireTruck
                    else -> Icons.Default.Pets
                }
            )
        }
        "ADMIN_OFFICERS" -> quadruplet(
            if (isMarathi) "🏛️ प्रशासकीय अधिकारी" else "🏛️ Admin Officer",
            Color(0xFFE3F2FD), Color(0xFF0D47A1),
            Icons.Default.AccountBalance
        )
        else -> quadruplet(
            if (isMarathi) "🏡 स्थानिक प्रतिनिधी/सेवक" else "🏡 Local Representative",
            Color(0xFFE8F5E9), Color(0xFF1B5E20),
            Icons.Default.People
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("contact_card_${contact.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, GBBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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

                // RIGHT ACTION DIAL BUTTON
                if (contact.phone.isNotBlank()) {
                    IconButton(
                        onClick = onCallClick,
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
                                modifier = Modifier.clickable { onCallClick() }
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

                    if (contact.phoneAlt.isNotBlank() && contact.phoneAlt != contact.phone) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(
                            label = if (isMarathi) "पर्यायी नंबर:" else "Alt Phone:",
                            value = contact.phoneAlt,
                            isPhone = true,
                            onPhoneClick = { onCallClick() }
                        )
                    }

                    if (contact.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(
                            label = if (isMarathi) "टीप / माहिती:" else "Information:",
                            value = contact.notes,
                            isPhone = false
                        )
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

                        if (isAdmin) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = onEditClick) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "सुधारा",
                                        tint = GBGreyText,
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
                                                text = if (isMarathi) "हा संपर्क डिलीट करायचा?" else "Delete this contact?",
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        text = {
                                            Text(
                                                text = if (isMarathi) {
                                                    if (contact.isDefault) "हा शासकीय संपर्क आहे! याला काढून टाकण्याऐवजी आपण नवीन सुधारणा करू शकता." else "हा संपर्क माहितीकोषातून कायमचा डिलीट केला जाईल. पुढे जायचे?"
                                                } else {
                                                    if (contact.isDefault) "This is an administrative emergency contact! Deleting it is not recommended." else "This contact will be permanently deleted from local cache. Continue?"
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
                                                    text = if (isMarathi) "होय, डिलीट करा" else "Yes, Delete",
                                                    fontWeight = FontWeight.Bold
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
}

// Inline helper for quadruplet return type
private data class Quadruplet<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
private fun <A, B, C, D> quadruplet(a: A, b: B, c: C, d: D): Quadruplet<A, B, C, D> = Quadruplet(a, b, c, d)


@Composable
fun DetailRow(
    label: String,
    value: String,
    isPhone: Boolean,
    onPhoneClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GBGreyText,
            modifier = Modifier.width(85.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (isPhone && onPhoneClick != null) {
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GBPrimary,
                modifier = Modifier
                    .clickable { onPhoneClick() }
                    .weight(1f)
            )
        } else {
            Text(
                text = value,
                fontSize = 12.sp,
                color = GBText,
                modifier = Modifier.weight(1f)
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
    var category by remember { mutableStateOf(editingContact?.category ?: "ADMIN") }
    var designationMr by remember { mutableStateOf(editingContact?.designationMr ?: "") }
    var designationEn by remember { mutableStateOf(editingContact?.designationEn ?: "") }
    var villageMr by remember { mutableStateOf(editingContact?.villageOrAreaMr ?: "") }
    var villageEn by remember { mutableStateOf(editingContact?.villageOrAreaEn ?: "") }
    var notes by remember { mutableStateOf(editingContact?.notes ?: "") }

    val categories = listOf("ADMIN", "POLICE", "MEDICAL", "FIRE", "UTILITY", "RESCUE")
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
                            text = if (isMarathi) "१. संपर्क नाव (Contact Name)" else "1. Contact Name Details",
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
                            text = if (isMarathi) "२. दूरध्वनी संपर्क (Phone Information)" else "2. Phone Contact Details",
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
                            text = if (isMarathi) "३. पद व कार्यालयीन माहिती (Offices & Designation)" else "3. Offices & Classification",
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
                            text = if (isMarathi) "४. विभाग वर्ग आणि पत्ता (Location & Class)" else "4. Category & Area",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Category Custom drop-style
                        Box {
                            val categoryDisplayValue = when (category) {
                                "ADMIN" -> if (isMarathi) "ADMIN (🏛️ प्रशासकीय अधिकारी)" else "ADMIN (🏛️ Admin Officer)"
                                "POLICE" -> if (isMarathi) "POLICE (🚨 आपत्कालीन सुरक्षा)" else "POLICE (🚨 Emergency)"
                                "MEDICAL" -> if (isMarathi) "MEDICAL (🚨 आपत्कालीन आरोग्य)" else "MEDICAL (🚨 Emergency)"
                                "FIRE" -> if (isMarathi) "FIRE (🚨 आपत्कालीन सेवा)" else "FIRE (🚨 Emergency)"
                                "RESCUE" -> if (isMarathi) "RESCUE (🚨 बचाव दल)" else "RESCUE (🚨 Emergency)"
                                "UTILITY" -> if (isMarathi) "UTILITY (🏡 स्थानिक सेवक/यंत्रणा)" else "UTILITY (🏡 Local Rep)"
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
                                        "ADMIN" -> if (isMarathi) "ADMIN (🏛️ प्रशासकीय अधिकारी)" else "ADMIN (🏛️ Admin Officer)"
                                        "POLICE" -> if (isMarathi) "POLICE (🚨 आपत्कालीन सुरक्षा)" else "POLICE (🚨 Emergency)"
                                        "MEDICAL" -> if (isMarathi) "MEDICAL (🚨 आपत्कालीन आरोग्य)" else "MEDICAL (🚨 Emergency)"
                                        "FIRE" -> if (isMarathi) "FIRE (🚨 आपत्कालीन सेवा)" else "FIRE (🚨 Emergency)"
                                        "RESCUE" -> if (isMarathi) "RESCUE (🚨 बचाव दल)" else "RESCUE (🚨 Emergency)"
                                        "UTILITY" -> if (isMarathi) "UTILITY (🏡 स्थानिक सेवक/यंत्रणा)" else "UTILITY (🏡 Local Rep)"
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
                            text = if (isMarathi) "५. पूरक टीप (Notes / Extra Coordinates)" else "5. Notes & Guidelines",
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
                        text = if (isMarathi) "१. मुख्य डेटाबेस रकामे (Columns) व रचना:" else "1. Core Database Columns & Mapping:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Table of columns
                    val columns = listOf(
                        "nameMr / nameEn" to (if (isMarathi) "कर्मचारी/रूग्णालय/कार्यालयाचे नाव (मराठी/इंग्रजी)" else "Name of Employee/Substation (Marathi/English)"),
                        "phone / phoneAlt" to (if (isMarathi) "मुख्य मोबाईल क्रमांक आणि इतर पर्यायी क्रमांक" else "Primary Mobile and Secondary Contact Number"),
                        "category" to (if (isMarathi) "श्रेणी (आपोआप आपत्कालीन सेवा, प्रशासकीय किंवा स्थानिक गटांत विभागले जाते): ADMIN, POLICE, MEDICAL, FIRE, UTILITY, RESCUE" else "Category code (Auto maps to Emergency Services, Admin, or Local Reps): ADMIN, POLICE, MEDICAL, FIRE, UTILITY, RESCUE"),
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
                        text = if (isMarathi) "२. नवीन तालुक्यासाठी कस्टमायझेशन व स्वयंचलित सिंक कशी कार्य करते?" else "2. How automatic sync & multi-taluka design works:",
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
                                    "१. गुगल शीट उघडा -> File -> Share -> Publish to web निवडा.\n" +
                                    "२. 'Entire Document' आणि 'Comma-separated values (.csv)' निवडा.\n" +
                                    "३. 'Publish' दाबा आणि आलेली लिंक कॉपी करून येथे पेस्ट करा!"
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
    syncStatus: String,
    onDismiss: () -> Unit,
    isMarathi: Boolean
) {
    var tempUrl by remember(currentUrl) { mutableStateOf(currentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onUrlChange(tempUrl)
                    onSyncClick()
                    // Keep dialog open during sync so that user can see status instantly!
                },
                colors = ButtonDefaults.buttonColors(containerColor = GBPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("dialog_save_sync_btn")
            ) {
                Text(
                    text = if (isMarathi) "सेव्ह आणि सिंक करा 🔄" else "Save & Sync 🔄",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_close_btn")
            ) {
                Text(
                    text = if (isMarathi) "बंद करा" else "Close",
                    fontWeight = FontWeight.Bold,
                    color = GBGreyText
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
                    text = if (isMarathi) "गुगल शीट डेटा सिंक" else "Google Sheet Sync",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = GBText
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isMarathi) 
                        "कृपया तुमच्या गुगल शीटची (Google Sheet) लिंक खाली पेस्ट करा:" 
                    else 
                        "Please paste your Google Sheet sharing/publish link below:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GBText
                )

                OutlinedTextField(
                    value = tempUrl,
                    onValueChange = { tempUrl = it },
                    placeholder = {
                        Text(
                            text = "https://docs.google.com/spreadsheets/d/...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_sheet_url_input"),
                    textStyle = TextStyle(fontSize = 12.sp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = GBPrimary,
                        unfocusedBorderColor = GBBorder
                    )
                )

                // User-friendly smart auto-conversion tip
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEBF5FB)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isMarathi) "💡 गुगल शीट सोपी पद्धत:" else "💡 Dynamic Conversion Auto-enabled:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B4F72)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isMarathi) {
                                "तुम्ही तुमच्या गुगल शीटची नेहमीची शेअर लिंक (Share -> Anyone with link can view) " +
                                "थेट कॉपी करून येथे पेस्ट करू शकता. ॲप ती आपोआप योग्य फॉरमॅटमध्ये रूपांतरित करून डेटा डाऊनलोड करेल!"
                            } else {
                                "You can directly paste any standard view link (Share -> Anyone with link can view). " +
                                "The app automatically translates it into CSV on-the-fly and syncs!"
                            },
                            fontSize = 10.sp,
                            color = Color(0xFF2E86C1),
                            lineHeight = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Current Sync Status Bar
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
                            "SYNCING" -> if (isMarathi) "सिंक होत आहे... कृपया वाट पहा." else "Syncing data, please wait..."
                            "SUCCESS" -> if (isMarathi) "यशस्वीरित्या सिंक झाले! नवीन डेटा समाविष्ट केला आहे." else "Successfully Synced! New data loaded."
                            "FAILED" -> if (isMarathi) "सिंक अयशस्वी! गुगल शीटची लिंक किंवा इंटरनेट तपासा." else "Sync Failed! Check sheets configuration or your connection."
                            else -> if (isMarathi) "लिंक सेट करून वरील सिंक बटणावर क्लिक करा." else "Link sheet and click Sync above to start."
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
            }
        }
    )
}
