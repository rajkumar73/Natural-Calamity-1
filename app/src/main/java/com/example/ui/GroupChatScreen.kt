package com.example.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*

@Composable
fun GroupChatScreen(
    contactViewModel: ContactViewModel,
    isMarathi: Boolean
) {
    val profileName by contactViewModel.userProfileName.collectAsStateWithLifecycle()
    val profileRole by contactViewModel.userProfileRole.collectAsStateWithLifecycle()
    val messages by contactViewModel.groupMessages.collectAsStateWithLifecycle()

    var tempName by remember { mutableStateOf(profileName) }
    var tempRole by remember { mutableStateOf(profileRole) }
    var isEditingProfile by remember { mutableStateOf(false) }

    val context = LocalContext.current

    if (profileName.isBlank() || isEditingProfile) {
        // PROFILE ONBOARDING / EDIT SCREEN
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GBBg)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .testTag("profile_onboarding_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(GBPrimary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = "Group Forum",
                            tint = GBPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isMarathi) "आपत्कालीन चर्चा गट 💬" else "Emergency Group Forum 💬",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = GBText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isMarathi) 
                            "या चर्चा गटात तालुक्यातील नागरिक, सरपंच, स्वयंसेवक आणि सर्व समन्वयक थेट चर्चा करत आहेत. संवादासाठी आपली माहिती प्रविष्ट करा."
                        else 
                            "In this forum, taluka citizens, sarpanches, rescue volunteers and administrators discuss in real-time. Enter profile to start.",
                        fontSize = 12.sp,
                        color = GBGreyText,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = TextStyle(lineHeight = 16.sp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Name Input
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = {
                            Text(
                                text = if (isMarathi) "तुमचे नाव (Your Name)" else "Your Name",
                                fontSize = 12.sp
                            )
                        },
                        placeholder = {
                            Text(
                                text = if (isMarathi) "उदा. राजेश पाटील" else "e.g., Rajesh Patil",
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Role Input
                    OutlinedTextField(
                        value = tempRole,
                        onValueChange = { tempRole = it },
                        label = {
                            Text(
                                text = if (isMarathi) "भूमिका / पद (Role / Designation)" else "Role / Designation",
                                fontSize = 12.sp
                            )
                        },
                        placeholder = {
                            Text(
                                text = if (isMarathi) "उदा. स्वयंसेवक, नागरिक, सरपंच" else "e.g., Volunteer, Citizen, Sarpanch",
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_role_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Role Suggestion Chips
                    val roleChips = if (isMarathi) {
                        listOf("नागरिक", "आपत्कालीन स्वयंसेवक", "सरपंच", "ग्रामसेवक", "डॉक्टर")
                    } else {
                        listOf("Citizen", "Rescue Volunteer", "Sarpanch", "Gramsevak", "Doctor")
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(roleChips) { role ->
                            SuggestionChip(
                                onClick = { tempRole = role },
                                label = { Text(role, fontSize = 10.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Color(0xFFF1F5F9)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isEditingProfile) {
                            OutlinedButton(
                                onClick = { isEditingProfile = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (isMarathi) "रद्द करा" else "Cancel")
                            }
                        }

                        Button(
                            onClick = {
                                if (tempName.isNotBlank() && tempRole.isNotBlank()) {
                                    contactViewModel.saveUserProfile(tempName.trim(), tempRole.trim())
                                    isEditingProfile = false
                                } else {
                                    Toast.makeText(context, if (isMarathi) "नाव आणि भूमिका प्रविष्ट करणे आवश्यक आहे!" else "Name and Role are required!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_profile_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = GBPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isMarathi) "सामील व्हा 💬" else "Join Discussion 💬",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    } else {
        // ACTIVE DISCUSSION FORUM CHAT ROOM
        var inputText by remember { mutableStateOf("") }
        val listState = rememberLazyListState()

        // Launcher for speech-to-text
        val speechLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                if (spokenText != null) {
                    inputText = spokenText
                }
            }
        }

        val startVoiceInput = {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isMarathi) "mr-IN" else "en-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, if (isMarathi) "तुमचा संदेश बोला..." else "Speak your message...")
            }
            try {
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, if (isMarathi) "स्पीच रिकग्निशन उपलब्ध नाही." else "Speech recognition not available.", Toast.LENGTH_SHORT).show()
            }
        }

        // Auto Scroll to last message
        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GBBg)
        ) {
            // Forum Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(GBPrimary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = "Active Forum",
                                tint = GBPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isMarathi) "आपत्कालीन चर्चा गट" else "Taluka Discussion Group",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = GBText
                            )
                            Text(
                                text = if (isMarathi) "सक्रिय: $profileName ($profileRole)" else "Active: $profileName ($profileRole)",
                                fontSize = 10.sp,
                                color = GBGreyText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = {
                                tempName = profileName
                                tempRole = profileRole
                                isEditingProfile = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = GBPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = { contactViewModel.clearGroupMessages() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Reset Chat",
                                tint = RedEmergency,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Message Board Area
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(messages) { msg ->
                    val isSelf = msg.isSelf
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start
                        ) {
                            // Sender badge with Role color
                            if (!isSelf) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
                                ) {
                                    Text(
                                        text = msg.senderName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GBText
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    
                                    val roleColor = when {
                                        msg.senderRole.contains("Admin") || msg.senderRole.contains("प्रशासन") -> Color(0xFFDC2626)
                                        msg.senderRole.contains("Officer") || msg.senderRole.contains("वैद्यकीय") || msg.senderRole.contains("डॉक्टर") -> GreenMedical
                                        msg.senderRole.contains("Rescue") || msg.senderRole.contains("स्वयंसेवक") -> GBPrimary
                                        msg.senderRole.contains("Sarpanch") || msg.senderRole.contains("सरपंच") -> Color(0xFF7C3AED)
                                        else -> GBGreyText
                                    }
                                    
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = roleColor.copy(alpha = 0.12f),
                                        border = BorderStroke(0.5.dp, roleColor.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = msg.senderRole,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = roleColor,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = if (isMarathi) "तुम्ही ($profileRole)" else "You ($profileRole)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GBPrimary,
                                    modifier = Modifier.padding(end = 6.dp, bottom = 2.dp)
                                )
                            }

                            val bubbleShape = if (isSelf) {
                                RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 1.dp)
                            } else {
                                RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 1.dp, bottomEnd = 14.dp)
                            }

                            Card(
                                modifier = Modifier.widthIn(max = 290.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelf) GBPrimary else Color.White
                                ),
                                shape = bubbleShape,
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                border = if (isSelf) null else BorderStroke(0.8.dp, GBBorder)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = msg.message,
                                        color = if (isSelf) Color.White else GBText,
                                        fontSize = 12.5.sp,
                                        lineHeight = 17.sp
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = msg.timestamp,
                                        color = if (isSelf) Color.White.copy(alpha = 0.65f) else GBGreyText.copy(alpha = 0.8f),
                                        fontSize = 8.sp,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Chat Input Row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = startVoiceInput,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = Color(0xFF0D9488),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = if (isMarathi) "ग्रुपवर चर्चा करण्यासाठी येथे लिहा..." else "Write here to discuss...",
                                fontSize = 12.sp,
                                color = GBGreyText.copy(alpha = 0.7f)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("group_chat_input_field"),
                        maxLines = 3,
                        textStyle = TextStyle(fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = GBText,
                            unfocusedTextColor = GBText
                        )
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                contactViewModel.sendGroupMessage(inputText.trim())
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .background(GBPrimary, CircleShape)
                            .testTag("send_group_message_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
