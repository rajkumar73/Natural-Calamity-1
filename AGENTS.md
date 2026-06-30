# AGENTS.md - Custom Instructions & Rules for AI Agents

Welcome, AI Coding Agent! This file outlines the strict rules, architectural details, and product configuration values for managing and updating the **Taluka Emergency Directory Application**.

## 🚀 Application Parameters & Identity
*   **App Name**: `Taluka Emergency Directory`
*   **Package Namespace**: `com.example`
*   **Application ID**: `com.aistudio.talukaemergency.pwnzqk`

## 🎨 Design System & Colors (Material 3)
Ensure all UI updates strictly follow the geometric, high-contrast visual palette:
*   `GBPrimary = Color(0xFF2563EB)`: Royal Active Electric Blue
*   `GBBg = Color(0xFFF1F5F9)`: Clean Slate backdrop
*   `GBText = Color(0xFF0F172A)`: Obsidian Slate deep charcoal
*   `GBBorder = Color(0xFFCBD5E1)`: Silver boarder lines
*   `RedEmergency = Color(0xFFFF2D55)`: High stress crimson
*   `GreenMedical = Color(0xFF059669)` / Teal `Color(0xFF0D9488)`: Medical clinics & Doctors tab styling

## ⚙️ Core Architecture & State Flow
1.  **State Management**: `ContactViewModel` using `StateFlow` and persistent variables inside shared preferences.
2.  **Data Persistence**: Room Database (`EmergencyContact` entities) abstracted by `ContactRepository`.
3.  **Local Sync Engine**: Ingests google sheets templates in CSV form, parsing custom headings correctly in both Marathi and English.

## 🚨 Custom Feature Specs
*   **Live Alert Banner**: Featured prominently at the top of the contacts list. Customizable via Admin settings panel.
*   **Google Maps Integration**: Any contact with a Google Maps URL or Location inside their `notes` field automatically renders a gorgeous "गुगल मॅप लोकेशन (Google Maps Link)" green button.
*   **Clinical Grouping**: All `MEDICAL` contacts map to `MEDICAL_SERVICES` as a custom first-class tab "🏥 दवाखाने व डॉक्टर्स".
