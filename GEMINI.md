# GEMINI.md - Local Intelligence & Prompt Guidelines

This file governs any Gemini-based context transformations, language translations, or structured list formatting for the **Taluka Emergency Directory Application**.

## 📞 Multi-lingual Translation Rules (Marathi & English)
Since the app's database supports parallel fields for Marathi (`nameMr`, `designationMr`, `villageOrAreaMr`) and English (`nameEn`, `designationEn`, `villageOrAreaEn`):
1.  Always preserve semantic equivalence instead of literal dictionary word-by-word transliteration unless it is a proper noun (e.g. name of official or unique village).
2.  Proper Nouns:
    *   `मंगळवेढा` -> `Mangalwedha`
    *   `सोलापूर` -> `Solapur`
3.  Standard Designations:
    *   `तहसीलदार` -> `Tehsildar`
    *   `तलाठी` -> `Talathi`
    *   `पोलीस निरीक्षक` -> `Police Inspector`
    *   `वैद्यकीय अधिकारी` -> `Medical Officer`
    *   `सरपंच` -> `Sarpanch`

## 🗂️ Row-by-Row Structuring & Import Parsing
When feeding unstructured raw text from official notifications or local bulletin messages into the local system:
*   Identify Name, Phone, Designation, and Village/Area.
*   Classify contacts strictly into one of these category tags:
    *   `ADMIN` (🏛️ Taluka level administrative officials, desk coordinators, controllers)
    *   `POLICE` (🚨 Police stations, chowkis, beat officers)
    *   `MEDICAL` (🏥 Doctors, Government hospitals, Primary Health Centers (PHC), clinic supervisors)
    *   `FIRE` (🚨 Fire stations, rescue crews)
    *   `RESCUE` (🚨 Disaster relief forces, certified local lifeguards)
    *   `UTILITY` (🏡 Village-level Sarpanch, Gramsevak, Talathi, electricity/water technicians)
