package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Emergency Response Custom Theme Colors - Vibrant, energetic, radiant!
val RedEmergency = Color(0xFFFF2D55)   // Vibrant glowing Pink-Crimson
val OrangeSafety = Color(0xFFFF6B00)    // High-visibility neon Orange
val BlueAdmin = Color(0xFF0070FF)       // High-contrast electric Sky Blue
val GreenMedical = Color(0xFF059669)     // Rich vibrant Emerald Green
val TealNavy = Color(0xFF1E1B4B)         // Deep cosmic Indigo-Navy
val LightBackground = Color(0xFFF8FAFC)   // Ice-tint white modern backdrop
val DarkBackground = Color(0xFF030712)
val DarkSurface = Color(0xFF1F2937)
val AmberAccent = Color(0xFFFBBF24)

// Geometric Balance Specific Palette Colors - Electric high-contrast vibrant palette
val GBPrimaryState = mutableStateOf(Color(0xFF2563EB))

val GBPrimary: Color
    get() = GBPrimaryState.value

val GBBg = Color(0xFFF1F5F9)              // Clean, vivid Slate light grey screen background
val GBText = Color(0xFF0F172A)            // Obsidian Slate Deep for maximum readability
val GBBorder = Color(0xFFCBD5E1)          // High-visibility silver boarders
val GBSosContainer = Color(0xFFFFE4E6)    // Soft rich Rose pink for critical buttons
val GBSosText = Color(0xFFE11D48)         // Vibrant high-contrast Rose-Red for critical text
val GBCardIconBg = Color(0xFFEFF6FF)      // Delicate high-clarity Blue Ice Container
val GBCardIconText = Color(0xFF1D4ED8)    // Intense Royal Cobalt font
val GBGreyContainer = Color(0xFFE2E8F0)
val GBGreyText = Color(0xFF475569)        // Legible deep charcoal slate
