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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*

@Composable
fun ChatbotScreen(
    contactViewModel: ContactViewModel,
    isMarathi: Boolean
) {
    val messages by contactViewModel.chatMessages.collectAsStateWithLifecycle()
    val isLoading by contactViewModel.isChatLoading.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Launcher for speech-to-text inside the chatbot
    val chatSpeechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (spokenText != null) {
                inputText = spokenText
            }
        }
    }

    val startChatVoiceInput = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isMarathi) "mr-IN" else "en-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, if (isMarathi) "आपला प्रश्न बोला..." else "Speak your question...")
        }
        try {
            chatSpeechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, if (isMarathi) "स्पीच रिकग्निशन उपलब्ध नाही." else "Speech recognition not available.", Toast.LENGTH_SHORT).show()
        }
    }

    // Scroll to the latest message automatically when list size changes
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
        // Chatbot Header Card
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
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(GBPrimary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Bot Icon",
                            tint = GBPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isMarathi) "आपत्कालीन AI सहाय्यक" else "Emergency AI Assistant",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GBText
                        )
                        Text(
                            text = if (isMarathi) "Gemini द्वारे समर्थित" else "Powered by Gemini AI",
                            fontSize = 11.sp,
                            color = GBGreyText
                        )
                    }
                }

                IconButton(
                    onClick = { contactViewModel.clearChat() },
                    modifier = Modifier.testTag("clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = if (isMarathi) "चॅट साफ करा" else "Clear Chat",
                        tint = RedEmergency,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Suggestions / Quick-Chips
        val suggestions = if (isMarathi) {
            listOf(
                "साप चावला तर काय करावे? 🐍",
                "तहसीलदार साहेबांचा नंबर काय आहे? 🏛️",
                "वैद्यकीय अधिकारी PHC चे नंबर द्या 🏥",
                "पोलीस स्टेशन मंगळवेढा संपर्क 🚨"
            )
        } else {
            listOf(
                "What to do for snake bite? 🐍",
                "What is Tehsildar's contact? 🏛️",
                "Give PHC Medical Officer numbers 🏥",
                "Police Station Mangalwedha contact 🚨"
            )
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 12.dp)
        ) {
            items(suggestions) { query ->
                SuggestionChip(
                    onClick = {
                        contactViewModel.sendChatMessage(query)
                    },
                    label = {
                        Text(
                            text = query,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F766E) // Deep elegant teal color for suggestions
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color(0xFFE6F4F1) // Very soft cyan/teal background
                    ),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                )
            }
        }

        // Message list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp)
        ) {
            items(messages) { message ->
                val bubbleShape = if (message.isUser) {
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
                } else {
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!message.isUser) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Bot",
                            tint = GBPrimary.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(24.dp)
                                .padding(top = 4.dp, end = 4.dp)
                                .align(Alignment.Top)
                        )
                    }

                    Card(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .testTag(if (message.isUser) "user_chat_bubble" else "bot_chat_bubble"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (message.isUser) GBPrimary else Color.White
                        ),
                        shape = bubbleShape,
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = message.text,
                                color = if (message.isUser) Color.White else GBText,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            // Render Search Queries (if any)
                            if (!message.isUser && !message.searchQueries.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isMarathi) "🔍 शोधलेले विषय:" else "🔍 Search Topics:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    items(message.searchQueries) { query ->
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFF1F5F9),
                                            border = BorderStroke(0.5.dp, Color(0xFFCBD5E1)),
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = query,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF475569),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Render Grounded Web Sources (if any)
                            if (!message.isUser && !message.sources.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = Color(0xFFE2E8F0))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isMarathi) "🌐 लाईव्ह संदर्भ (क्लिक करा):" else "🌐 Live References (Click):",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GBPrimary
                                )
                                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    message.sources.forEach { source ->
                                        Surface(
                                            onClick = {
                                                try {
                                                    uriHandler.openUri(source.url)
                                                } catch (e: Exception) {}
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFEFF6FF),
                                            border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Language,
                                                    contentDescription = source.title,
                                                    tint = GBPrimary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = source.title,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1E40AF),
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.OpenInNew,
                                                    contentDescription = "Open",
                                                    tint = GBPrimary.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(10.dp)
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

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI thinking",
                            tint = Color(0xFFFBBF24), // Amber tint for thinking
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isMarathi) "उत्तर शोधत आहे..." else "Searching response...",
                            fontSize = 12.sp,
                            color = GBGreyText,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp,
                            color = GBPrimary
                        )
                    }
                }
            }
        }

        // Message Input Row
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speech icon inside chat field
                IconButton(
                    onClick = startChatVoiceInput,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "माईक द्वारे चॅट टाईप करा",
                        tint = Color(0xFF0D9488),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Input Box
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (isMarathi) "आपला प्रश्न येथे लिहा..." else "Ask anything...",
                            fontSize = 13.sp,
                            color = GBGreyText.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_text_field"),
                    maxLines = 3,
                    textStyle = TextStyle(fontSize = 13.sp),
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

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            contactViewModel.sendChatMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(GBPrimary, CircleShape)
                        .testTag("send_chat_message_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = if (isMarathi) "पाठवा" else "Send",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
