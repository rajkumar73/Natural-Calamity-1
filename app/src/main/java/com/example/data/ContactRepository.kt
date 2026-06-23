package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ContactRepository(private val contactDao: ContactDao) {
    val allContacts: Flow<List<EmergencyContact>> = contactDao.getAllContacts()

    suspend fun insert(contact: EmergencyContact) = contactDao.insertContact(contact)

    suspend fun update(contact: EmergencyContact) = contactDao.updateContact(contact)

    suspend fun delete(contact: EmergencyContact) = contactDao.deleteContact(contact)

    suspend fun prePopulateIfEmpty() {
        val current = contactDao.getAllContacts().first()
        if (current.isEmpty()) {
             val defaultContacts = listOf(
                EmergencyContact(
                    nameMr = "मा. समन्वयक अधिकारी (आपत्ती नियंत्रण कक्ष)",
                    nameEn = "Disaster Control Coordination Officer",
                    phone = "02342-221011",
                    phoneAlt = "9420394011",
                    category = "ADMIN",
                    designationMr = "प्रमुख आपत्ती नियंत्रण व समन्वय",
                    designationEn = "Disaster Coordination Lead",
                    villageOrAreaMr = "तालुका (सर्व)",
                    villageOrAreaEn = "Taluka (All)",
                    isDefault = true,
                    notes = "कोणत्याही नैसर्गिक किंवा मानवनिर्मित आपत्तीमध्ये तात्काळ संपर्क साधा (२४/७ कार्यरत)."
                ),
                EmergencyContact(
                    nameMr = "मा. तहसीलदार तथा तालुका दंडाधिकारी",
                    nameEn = "Hon. Tehsildar & Executive Magistrate",
                    phone = "02342-221025",
                    phoneAlt = "9842104085",
                    category = "ADMIN",
                    designationMr = "तालुका दंडाधिकारी व मुख्य आपत्ती प्रमुख",
                    designationEn = "Tehsildar & Executive Magistrate",
                    villageOrAreaMr = "तालुका (सर्व)",
                    villageOrAreaEn = "Taluka (All)",
                    isDefault = true,
                    notes = "तालुकास्तरीय संपूर्ण आपत्ती समन्वय व मदतवाटप प्रमुख अधिकारी."
                ),
                EmergencyContact(
                    nameMr = "मा. पोलीस निरीक्षक (सुरक्षा व बंदोबस्त)",
                    nameEn = "Hon. Police Inspector (Shirala)",
                    phone = "100",
                    phoneAlt = "02342-221033",
                    category = "POLICE",
                    designationMr = "कायदा, सुव्यवस्था व पोलीस बंदोबस्त प्रमुख",
                    designationEn = "Head of Law, Order & Security",
                    villageOrAreaMr = "तालुका (सर्व)",
                    villageOrAreaEn = "Taluka (All)",
                    isDefault = true,
                    notes = "कायदा व सुव्यवस्था सुरळीत ठेवणे आणि बचावकार्यासाठी पोलीस बंदोबस्त मिळवणे."
                ),
                EmergencyContact(
                    nameMr = "मा. वैद्यकीय अधीक्षक (उपजिल्हा रुग्णालय)",
                    nameEn = "Hon. Medical Superintendent",
                    phone = "102",
                    phoneAlt = "02342-222105",
                    category = "MEDICAL",
                    designationMr = "आपत्कालीन वैद्यकीय उपचार प्रमुख अधिकारी",
                    designationEn = "ER Head Medical Officer",
                    villageOrAreaMr = "तालुका (सर्व)",
                    villageOrAreaEn = "Taluka (All)",
                    isDefault = true,
                    notes = "मोफत रुग्णवाहिका, अपघात व आपत्कालीन वैद्यकीय उपचारांसाठी तात्काळ संपर्क."
                ),
                EmergencyContact(
                    nameMr = "मा. मुख्य अग्निशामक अधिकारी (बचाव कार्य)",
                    nameEn = "Hon. Chief Fire Officer",
                    phone = "101",
                    phoneAlt = "02342-222011",
                    category = "FIRE",
                    designationMr = "आग व जळित आपत्ती बचाव प्रमुख",
                    designationEn = "Fire & Heavy Rescue Lead",
                    villageOrAreaMr = "तालुका (सर्व)",
                    villageOrAreaEn = "Taluka (All)",
                    isDefault = true,
                    notes = "आग लागणे, इमारत कोसळणे किंवा पुरात अडकलेल्या नागरिकांच्या बचावासाठी विशेष मोहीम दल."
                ),
                EmergencyContact(
                    nameMr = "मा. कनिष्ठ अभियंता (महावितरण वीज पुरवठा)",
                    nameEn = "Hon. Junior Engineer (MSEDCL)",
                    phone = "1912",
                    phoneAlt = "18002333435",
                    category = "UTILITY",
                    designationMr = "वीज पुरवठा खंडित व सुरक्षा नियंत्रण",
                    designationEn = "Power Safety & Distribution Engineer",
                    villageOrAreaMr = "तालुका (सर्व)",
                    villageOrAreaEn = "Taluka (All)",
                    isDefault = true,
                    notes = "पूर, सोसाट्याचा वारा किंवा अतिवृष्टीत खांब पडल्यास शॉर्टसर्किट टाळण्यासाठी तात्काळ लाईन बंद करणे."
                ),
                EmergencyContact(
                    nameMr = "मा. पूर विसर्ग समन्वयक अधिकारी (पाटबंधारे)",
                    nameEn = "Hon. Flood Discharge Coordinator",
                    phone = "02342-221566",
                    phoneAlt = "8888421055",
                    category = "UTILITY",
                    designationMr = "धरण पाणी विसर्ग व नदी पातळी नियंत्रण",
                    designationEn = "Dam Release & Water Level Authority",
                    villageOrAreaMr = "तालुका (सर्व)",
                    villageOrAreaEn = "Taluka (All)",
                    isDefault = true,
                    notes = "नदीपातळी मोजणे आणि धरणांमधून सोडण्यात येणाऱ्या पाण्याचा विसर्ग नियंत्रित करणे."
                ),
                EmergencyContact(
                    nameMr = "१०८ रुग्णवाहिका नियंत्रण समन्वयक",
                    nameEn = "108 Ambulance Control Coordinator",
                    phone = "108",
                    phoneAlt = "108",
                    category = "MEDICAL",
                    designationMr = "शासकीय मोफत रुग्णवाहिका मदत सेवा",
                    designationEn = "Govt Free Ambulance Service Desk",
                    villageOrAreaMr = "तालुका (सर्व)",
                    villageOrAreaEn = "Taluka (All)",
                    isDefault = true,
                    notes = "मुलभूत आणि प्रगत जीवन समर्थक वैद्यकीय सुविधांनी सुसज्ज मोफत रुग्णवाहिका."
                ),
                EmergencyContact(
                    nameMr = "मा. वनपरिक्षेत्र अधिकारी (RFO शिराळा)",
                    nameEn = "Hon. Forest Range Officer (RFO Shirala)",
                    phone = "02342-223450",
                    phoneAlt = "9423508001",
                    category = "RESCUE",
                    designationMr = "वनवणवा व वन्यजीव आपत्ती बचाव नियंत्रण",
                    designationEn = "Forest Fire & Wildlife Emergency Desk",
                    villageOrAreaMr = "तालुका (सर्व)",
                    villageOrAreaEn = "Taluka (All)",
                    isDefault = true,
                    notes = "वनवणवा व वन्यजीव बचाव (बिबट्या किंवा साप घरात आल्यास वन्यजीव रक्षकांसाठी संपर्क)."
                ),
                EmergencyContact(
                    nameMr = "शिवराज आपत्ती प्रतिसाद संस्था",
                    nameEn = "Shivraj Rescue Disaster NGO",
                    phone = "9823012345",
                    phoneAlt = "9766123456",
                    category = "RESCUE",
                    designationMr = "नागरी स्वयंसेवक बचाव दल",
                    designationEn = "Voluntary Rescue Squad",
                    villageOrAreaMr = "तालुका (सर्व)",
                    villageOrAreaEn = "Taluka (All)",
                    isDefault = true,
                    notes = "पूर किंवा दरी खोऱ्यात बचावकार्यासाठी प्रशिक्षित पोहणारे व गिर्यारोहक स्वयंसेवक."
                ),
                // --- VILLAGE: SHIRALA ---
                EmergencyContact(
                    nameMr = "रामनारायण शिंदे (आर. एस. शिंदे)",
                    nameEn = "Ramnarayan Shinde (R. S. Shinde)",
                    phone = "9822456781",
                    category = "ADMIN",
                    designationMr = "ग्राम महसूल अधिकारी (तलाठी)",
                    designationEn = "Gram Revenue Officer (Talathi)",
                    villageOrAreaMr = "शिराळा",
                    villageOrAreaEn = "Shirala",
                    isDefault = true,
                    notes = "महसूल दप्तर नियंत्रण, पूर सर्वेक्षण व सरकारी कागदपत्रे पडताळणी प्रमुख अधिकारी."
                ),
                EmergencyContact(
                    nameMr = "विकास देशपांडे",
                    nameEn = "Vikas Deshpande",
                    phone = "9822456782",
                    category = "ADMIN",
                    designationMr = "ग्रामविकास अधिकारी (ग्रामसेवक)",
                    designationEn = "Gram Vikas Officer (Gramsevak)",
                    villageOrAreaMr = "शिराळा",
                    villageOrAreaEn = "Shirala",
                    isDefault = true,
                    notes = "पंचायत व ग्रामपंचायत स्तरावरील संकट नियोजन व निवारण समन्वयक."
                ),
                EmergencyContact(
                    nameMr = "बाळासाहेब पाटील",
                    nameEn = "Balasaheb Patil",
                    phone = "9822456783",
                    category = "POLICE",
                    designationMr = "पोलीस पाटील",
                    designationEn = "Police Patil",
                    villageOrAreaMr = "शिराळा",
                    villageOrAreaEn = "Shirala",
                    isDefault = true,
                    notes = "गावातील कायदा-सुव्यवस्था व पोलिसांचे थेट स्थानिक संपर्क माध्यम."
                ),
                EmergencyContact(
                    nameMr = "सरिताताई मोहिते",
                    nameEn = "Saritatay Mohite",
                    phone = "9822456784",
                    category = "ADMIN",
                    designationMr = "सरपंच (ग्रामपंचायत शिराळा)",
                    designationEn = "Sarpanch (Gram Panchayat Shirala)",
                    villageOrAreaMr = "शिराळा",
                    villageOrAreaEn = "Shirala",
                    isDefault = true,
                    notes = "गावाचे लोकनियुक्त प्रमुख, आपत्ती मदत वितरण समन्वयक."
                ),
                EmergencyContact(
                    nameMr = "अनिल कुलकर्णी",
                    nameEn = "Anil Kulkarni",
                    phone = "9822456785",
                    category = "UTILITY",
                    designationMr = "कृषी सहाय्यक",
                    designationEn = "Agriculture Assistant",
                    villageOrAreaMr = "शिराळा",
                    villageOrAreaEn = "Shirala",
                    isDefault = true,
                    notes = "पिकांचे नुकसान, सरकारी अनुदान पंचनामा व शेतीविषयक आपत्ती पंचनाम्यांचे अधिकारी."
                ),
                EmergencyContact(
                    nameMr = "भास्कर चव्हाण",
                    nameEn = "Bhaskar Chavan",
                    phone = "9822456786",
                    category = "UTILITY",
                    designationMr = "महावितरण वायरमन",
                    designationEn = "MSEDCL Line Wireman",
                    villageOrAreaMr = "शिराळा",
                    villageOrAreaEn = "Shirala",
                    isDefault = true,
                    notes = "गावातील विद्युत पुरवठा सुरळीत करणे, तुटलेल्या तारा व पोल तात्काळ दुरूस्त करणे."
                ),
                EmergencyContact(
                    nameMr = "संजय तांबडे",
                    nameEn = "Sanjay Tambde",
                    phone = "9822456787",
                    category = "POLICE",
                    designationMr = "बीट अंमलदार (स्थानिक पोलीस)",
                    designationEn = "Beat Constable (Local Police)",
                    villageOrAreaMr = "शिराळा",
                    villageOrAreaEn = "Shirala",
                    isDefault = true,
                    notes = "शिराळा बीट अंतर्गत सुरक्षा, बॅरिकेटिंग व स्थानिक मदत नियंत्रण पथक."
                ),
                EmergencyContact(
                    nameMr = "मीनाताई सावंत",
                    nameEn = "Minatai Sawant",
                    phone = "9822456788",
                    category = "MEDICAL",
                    designationMr = "मुख्य अंगणवाडी सेविका",
                    designationEn = "Chief Anganwadi Worker",
                    villageOrAreaMr = "शिराळा",
                    villageOrAreaEn = "Shirala",
                    isDefault = true,
                    notes = "लहान मुले, गरोदर महिला व ज्येष्ठ नागरिकांच्या प्राथमिक औषधोपचारांची व पोषण आहाराची व्यवस्था."
                ),
                EmergencyContact(
                    nameMr = "गणेश कोळी व सहकारी संघ",
                    nameEn = "Ganesh Koli & Local Rescue Team",
                    phone = "9822456789",
                    category = "RESCUE",
                    designationMr = "स्थानिक जीवरक्षक (पोहणारे लोक)",
                    designationEn = "Local Lifeguards (Swimmers)",
                    villageOrAreaMr = "शिराळा",
                    villageOrAreaEn = "Shirala",
                    isDefault = true,
                    notes = "पुरात अडकलेल्या लोकांना वाचवणे, बोट व दोरीच्या साहाय्याने बचावकार्य करणारे तज्ञ."
                ),
                EmergencyContact(
                    nameMr = "सलीम तांबोळी (जेसीबी ऑपरेटर)",
                    nameEn = "Salim Tamboli (JCB Operator)",
                    phone = "9822456790",
                    category = "RESCUE",
                    designationMr = "जेसीबी चालक व ढिगारा समन्वयक",
                    designationEn = "JCB Driver & Excavation Deck",
                    villageOrAreaMr = "शिराळा",
                    villageOrAreaEn = "Shirala",
                    isDefault = true,
                    notes = "भूस्खलन (दगड कोसळणे), रस्ते अडवले जाणे किंवा ढिगारा हटवण्याच्या तातडीच्या कामासाठी यंत्रासह उपलब्ध."
                ),
                // --- VILLAGE: MANGLE ---
                EmergencyContact(
                    nameMr = "तुकाराम घाडगे (तलाठी)",
                    nameEn = "Tukaram Ghadge (Talathi)",
                    phone = "9975234501",
                    category = "ADMIN",
                    designationMr = "ग्राम महसूल अधिकारी (तलाठी)",
                    designationEn = "Gram Revenue Officer (Talathi)",
                    villageOrAreaMr = "मांगले",
                    villageOrAreaEn = "Mangle",
                    isDefault = true,
                    notes = "मांगले महसूल क्षेत्र पूर नियंत्रण व पंचनामा समन्वयक."
                ),
                EmergencyContact(
                    nameMr = "तानाजी खाडे (ग्रामसेवक)",
                    nameEn = "Tanaji Khade (Gramsevak)",
                    phone = "9975234502",
                    category = "ADMIN",
                    designationMr = "ग्रामविकास अधिकारी (ग्रामसेवक)",
                    designationEn = "Gram Vikas Officer (Gramsevak)",
                    villageOrAreaMr = "मांगले",
                    villageOrAreaEn = "Mangle",
                    isDefault = true,
                    notes = "मांगले ग्रामपंचायत समन्वय व ग्राम आपत्कालीन व्यवस्थापन अधिकारी."
                ),
                EmergencyContact(
                    nameMr = "आनंदा चौगुले",
                    nameEn = "Ananda Chougule",
                    phone = "9975234503",
                    category = "POLICE",
                    designationMr = "पोलीस पाटील (मांगले)",
                    designationEn = "Police Patil (Mangle)",
                    villageOrAreaMr = "मांगले",
                    villageOrAreaEn = "Mangle",
                    isDefault = true,
                    notes = "स्थानिक पातळीवरील पोलीस यंत्रणा आणि ग्रामस्थ समन्वय."
                ),
                EmergencyContact(
                    nameMr = "वैशालीताई पाटील (सरपंच)",
                    nameEn = "Vaishalitay Patil (Sarpanch)",
                    phone = "9975234504",
                    category = "ADMIN",
                    designationMr = "सरपंच (मांगले ग्रामसभा)",
                    designationEn = "Sarpanch (Mangle Gram Sabha)",
                    villageOrAreaMr = "मांगले",
                    villageOrAreaEn = "Mangle",
                    isDefault = true,
                    notes = "गाव प्रमुख जनसंपर्क, पुनर्वसन व निवारण छावण्या उभारणी प्रमुख."
                ),
                EmergencyContact(
                    nameMr = "सुहास कांबळे",
                    nameEn = "Suhas Kamble",
                    phone = "9975234505",
                    category = "UTILITY",
                    designationMr = "कृषी सहाय्यक",
                    designationEn = "Agriculture Assistant",
                    villageOrAreaMr = "मांगले",
                    villageOrAreaEn = "Mangle",
                    isDefault = true,
                    notes = "मांगले क्षेत्रातील नुकसानग्रस्त पिकांचे आणि गोठ्यांचे तात्काळ पंचनामे समन्वयक."
                ),
                EmergencyContact(
                    nameMr = "लक्ष्मण सुतार",
                    nameEn = "Lakshman Sutar",
                    phone = "9975234506",
                    category = "UTILITY",
                    designationMr = "महावितरण लाईनमन व वायरमन",
                    designationEn = "MSEDCL Lineman / Wireman",
                    villageOrAreaMr = "मांगले",
                    villageOrAreaEn = "Mangle",
                    isDefault = true,
                    notes = "वीज पुरवठा व आपत्कालीन लाईन वीज खंडित करून दुर्घटना टाळणे."
                ),
                EmergencyContact(
                    nameMr = "सुनील बर्डे (बीट हवालदार)",
                    nameEn = "Sunil Barde (Beat Police Constable)",
                    phone = "9975234507",
                    category = "POLICE",
                    designationMr = "बीट हवालदार (सुरक्षा विभाग)",
                    designationEn = "Beat Police Constable in Charge",
                    villageOrAreaMr = "मांगले",
                    villageOrAreaEn = "Mangle",
                    isDefault = true,
                    notes = "मांगले व आजूबाजूच्या गावांचे सुरक्षा व नाकेबंदी पथक."
                ),
                EmergencyContact(
                    nameMr = "सिंधुताई कदम",
                    nameEn = "Sindhutai Kadam",
                    phone = "9975234508",
                    category = "MEDICAL",
                    designationMr = "अंगणवाडी सेविका प्रमुख",
                    designationEn = "Anganwadi Worker Coordinator",
                    villageOrAreaMr = "मांगले",
                    villageOrAreaEn = "Mangle",
                    isDefault = true,
                    notes = "आरोग्य व बाल संगोपन केंद्र आपत्कालीन कक्ष व्यवस्थापनात सहाय्यक."
                ),
                EmergencyContact(
                    nameMr = "मारुती पोवार व पोहणारा युवक गट",
                    nameEn = "Maruti Powar & Swimmer Rescue Group",
                    phone = "9975234509",
                    category = "RESCUE",
                    designationMr = "स्थानिक बचाव गट (पोहणारे लोक)",
                    designationEn = "Local Swim Rescue Team",
                    villageOrAreaMr = "मांगले",
                    villageOrAreaEn = "Mangle",
                    isDefault = true,
                    notes = "वारणा नदीकाठ आणि पूरग्रस्त सखल भागात काम करणारा स्थानिक साहसी गट."
                ),
                EmergencyContact(
                    nameMr = "बाबूराव माने (जेसीबी सेवा)",
                    nameEn = "Baburao Mane (JCB Earthmover)",
                    phone = "9975234510",
                    category = "RESCUE",
                    designationMr = "जेसीबी चालक",
                    designationEn = "JCB Operator Service",
                    villageOrAreaMr = "मांगले",
                    villageOrAreaEn = "Mangle",
                    isDefault = true,
                    notes = "रस्ता सफाई आणि पूरकाळात माती-दगड उपसण्याचे तात्काळ काम."
                ),
                // --- VILLAGE: KOKRUD ---
                EmergencyContact(
                    nameMr = "शरद जोशी (तलाठी)",
                    nameEn = "Sharod Joshi (Talathi)",
                    phone = "9545012351",
                    category = "ADMIN",
                    designationMr = "ग्राम महसूल अधिकारी (तलाठी)",
                    designationEn = "Gram Revenue Officer (Talathi)",
                    villageOrAreaMr = "कोकरुड",
                    villageOrAreaEn = "Kokrud",
                    isDefault = true,
                    notes = "डोंगरी व पूरप्रवण क्षेत्र नियोजन तलाठी प्रमुख."
                ),
                EmergencyContact(
                    nameMr = "राजू पाटील (ग्रामसेवक)",
                    nameEn = "Raju Patil (Gramsevak)",
                    phone = "9545012352",
                    category = "ADMIN",
                    designationMr = "ग्रामविकास अधिकारी (ग्रामसेवक)",
                    designationEn = "Gram Vikas Officer (Gramsevak)",
                    villageOrAreaMr = "कोकरुड",
                    villageOrAreaEn = "Kokrud",
                    isDefault = true,
                    notes = "कोकरुड ग्रामपंचायत प्रमुख संकट नियोजन अधिकारी."
                ),
                EmergencyContact(
                    nameMr = "शिवाजीराव देसाई",
                    nameEn = "Shivajirao Desai",
                    phone = "9545012353",
                    category = "POLICE",
                    designationMr = "पोलीस पाटील (कोकरुड)",
                    designationEn = "Police Patil (Kokrud)",
                    villageOrAreaMr = "कोकरुड",
                    villageOrAreaEn = "Kokrud",
                    isDefault = true,
                    notes = "कोकरुड परिसरातील कायदा-सुव्यवस्था व पूर्वसूचना यंत्रणा प्रमुख."
                ),
                EmergencyContact(
                    nameMr = "हर्षवर्धन पाटील (सरपंच)",
                    nameEn = "Harshvardhan Patil (Sarpanch)",
                    phone = "9545012354",
                    category = "ADMIN",
                    designationMr = "गाव सरपंच व आपत्ती अध्यक्ष",
                    designationEn = "Sarpanch & Disaster President",
                    villageOrAreaMr = "कोकरुड",
                    villageOrAreaEn = "Kokrud",
                    isDefault = true,
                    notes = "गावातील शासकीय स्वयंसेवक मदत छावणी प्रमुख संघटक."
                ),
                EmergencyContact(
                    nameMr = "किरण मोरे (वायरमन)",
                    nameEn = "Kiran More (MSEDCL Wireman)",
                    phone = "9545012356",
                    category = "UTILITY",
                    designationMr = "क्षेत्रीय वायरमन (महावितरण)",
                    designationEn = "Field Wireman (MSEDCL)",
                    villageOrAreaMr = "कोकरुड",
                    villageOrAreaEn = "Kokrud",
                    isDefault = true,
                    notes = "कोकरुड वीज वाहिन्या सुरक्षित ठेवणारे तंत्रज्ञ."
                ),
                EmergencyContact(
                    nameMr = "दीपक कदम (पोलिस बीट हवालदार)",
                    nameEn = "Deepak Kadam (Beat Police Officer)",
                    phone = "9545012357",
                    category = "POLICE",
                    designationMr = "नाईक पोलीस / बीट अमलदार",
                    designationEn = "Beat Police Officer (NP)",
                    villageOrAreaMr = "कोकरुड",
                    villageOrAreaEn = "Kokrud",
                    isDefault = true,
                    notes = "पोलीस चौकी कोकरुड अंतर्गत गस्त व मदत कार्य."
                ),
                EmergencyContact(
                    nameMr = "आशाताई शिंदे",
                    nameEn = "Ashatai Shinde",
                    phone = "9545012358",
                    category = "MEDICAL",
                    designationMr = "अंगणवाडी सेविका",
                    designationEn = "Anganwadi Worker",
                    villageOrAreaMr = "कोकरुड",
                    villageOrAreaEn = "Kokrud",
                    isDefault = true,
                    notes = "प्राथमिक औषध साठा व सुखरूप मदत छावणी अन्न वितरण."
                ),
                EmergencyContact(
                    nameMr = "पांडुरंग सुतार व स्थानिक जीवरक्षक संघ",
                    nameEn = "Pandurang Sutar & Local Divers",
                    phone = "9545012359",
                    category = "RESCUE",
                    designationMr = "जीवरक्षक बचाव पथक (पोहणारे लोक)",
                    designationEn = "Life Guard Rescue Team (Swimmers)",
                    villageOrAreaMr = "कोकरुड",
                    villageOrAreaEn = "Kokrud",
                    isDefault = true,
                    notes = "नदीकाठच्या व पुराच्या ठिकाणी मदत कार्यात पटाईत पोहणारे लोक."
                ),
                EmergencyContact(
                    nameMr = "अमोल शिंदे (जेसीबी मालक चालक)",
                    nameEn = "Amol Shinde (JCB Operator)",
                    phone = "9545012360",
                    category = "RESCUE",
                    designationMr = "जेसीबी चालक",
                    designationEn = "JCB Operator",
                    villageOrAreaMr = "कोकरुड",
                    villageOrAreaEn = "Kokrud",
                    isDefault = true,
                    notes = "कोकरुड घाट व परिसरातील दरीतील मार्ग मोकळे करणे."
                )
            )
            contactDao.insertContacts(defaultContacts)
        } else {
            // For existing databases, check if default contacts use outdated office names, and cleanly migrate them to the new officer designations
            for (contact in current) {
                if (contact.isDefault) {
                    var updated = false
                    var nameMr = contact.nameMr
                    var nameEn = contact.nameEn
                    var designationMr = contact.designationMr
                    var designationEn = contact.designationEn
                    
                    when (contact.phone) {
                        "02342-221011" -> {
                            if (nameMr == "तालुका आपत्ती नियंत्रण कक्ष") {
                                nameMr = "मा. समन्वयक अधिकारी (आपत्ती नियंत्रण कक्ष)"
                                nameEn = "Disaster Control Coordination Officer"
                                designationMr = "प्रमुख आपत्ती नियंत्रण व समन्वय"
                                designationEn = "Disaster Coordination Lead"
                                updated = true
                            }
                        }
                        "02342-221025" -> {
                            if (nameMr == "तहसीलदार मुख्य कार्यालय") {
                                nameMr = "मा. तहसीलदार तथा तालुका दंडाधिकारी"
                                nameEn = "Hon. Tehsildar & Executive Magistrate"
                                designationMr = "तालुका दंडाधिकारी व मुख्य आपत्ती प्रमुख"
                                designationEn = "Tehsildar & Executive Magistrate"
                                updated = true
                            }
                        }
                        "100", "02342-221033" -> {
                            if (nameMr == "तालुका पोलीस नियंत्रण कक्ष") {
                                nameMr = "मा. पोलीस निरीक्षक (सुरक्षा व बंदोबस्त)"
                                nameEn = "Hon. Police Inspector (Shirala)"
                                designationMr = "कायदा, सुव्यवस्था व पोलीस बंदोबस्त प्रमुख"
                                designationEn = "Head of Law, Order & Security"
                                updated = true
                            }
                        }
                        "102", "02342-222105" -> {
                            if (nameMr == "ग्रामीण उपजिल्हा रुग्णालय") {
                                nameMr = "मा. वैद्यकीय अधीक्षक (उपजिल्हा रुग्णालय)"
                                nameEn = "Hon. Medical Superintendent"
                                designationMr = "आपत्कालीन वैद्यकीय उपचार प्रमुख अधिकारी"
                                designationEn = "ER Head Medical Officer"
                                updated = true
                            }
                        }
                        "101", "02342-222011" -> {
                            if (nameMr == "अग्निशामक व बचाव दल") {
                                nameMr = "मा. मुख्य अग्निशामक अधिकारी (बचाव कार्य)"
                                nameEn = "Hon. Chief Fire Officer"
                                designationMr = "आग व जळित आपत्ती बचाव प्रमुख"
                                designationEn = "Fire & Heavy Rescue Lead"
                                updated = true
                            }
                        }
                        "1912", "18002333435" -> {
                            if (nameMr == "महावितरण वीज ग्राहक सेवा") {
                                nameMr = "मा. कनिष्ठ अभियंता (महावितरण वीज पुरवठा)"
                                nameEn = "Hon. Junior Engineer (MSEDCL)"
                                designationMr = "वीज पुरवठा खंडित व सुरक्षा नियंत्रण"
                                designationEn = "Power Safety & Distribution Engineer"
                                updated = true
                            }
                        }
                        "02342-221566" -> {
                            if (nameMr == "पाटबंधारे व पूर नियंत्रण कक्ष") {
                                nameMr = "मा. पूर विसर्ग समन्वयक अधिकारी (पाटबंधारे)"
                                nameEn = "Hon. Flood Discharge Coordinator"
                                designationMr = "धरण पाणी विसर्ग व नदी पातळी नियंत्रण"
                                designationEn = "Dam Release & Water Level Authority"
                                updated = true
                            }
                        }
                        "108" -> {
                            if (nameMr == "१०८ शासकीय मोफत रुग्णवाहिका") {
                                nameMr = "१०८ रुग्णवाहिका नियंत्रण समन्वयक"
                                nameEn = "108 Ambulance Control Coordinator"
                                designationMr = "शासकीय मोफत रुग्णवाहिका मदत सेवा"
                                designationEn = "Govt Free Ambulance Service Desk"
                                updated = true
                            }
                        }
                        "02342-223450" -> {
                            if (nameMr == "वनपरिक्षेत्र अधिकारी कार्यालय") {
                                nameMr = "मा. वनपरिक्षेत्र अधिकारी (RFO शिराळा)"
                                nameEn = "Hon. Forest Range Officer (RFO Shirala)"
                                designationMr = "वनवणवा व वन्यजीव आपत्ती बचाव नियंत्रण"
                                designationEn = "Forest Fire & Wildlife Emergency Desk"
                                updated = true
                            }
                        }
                    }
                    if (updated) {
                        contactDao.updateContact(
                            contact.copy(
                                nameMr = nameMr,
                                nameEn = nameEn,
                                designationMr = designationMr,
                                designationEn = designationEn
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun resetToDefault() {
        contactDao.deleteAllContacts()
        prePopulateIfEmpty()
    }

    suspend fun clearAllContacts() {
        contactDao.deleteAllContacts()
    }

    suspend fun importContactsFromSheet(contacts: List<EmergencyContact>) {
        contactDao.deleteAllContacts()
        contactDao.insertContacts(contacts)
    }
}
