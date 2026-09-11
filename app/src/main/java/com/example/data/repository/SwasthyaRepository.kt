package com.example.data.repository

import android.content.Context
import com.example.data.local.SwasthyaDatabase
import com.example.data.model.*
import com.example.data.security.AES256CryptoManager
import com.example.data.security.SecurePdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class SwasthyaRepository(private val context: Context) {
    private val db = SwasthyaDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val contentDao = db.contentDao()
    private val accessDao = db.accessDao()
    private val accessRequestDao = db.accessRequestDao()
    private val offlineDownloadDao = db.offlineDownloadDao()
    private val notificationDao = db.notificationDao()
    private val settingsDao = db.settingsDao()

    // Current logged-in user state
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    suspend fun initDatabase() = withContext(Dispatchers.IO) {
        // Seed users if empty
        if (userDao.getUserById("student_demo") == null) {
            val student = UserEntity(
                id = "student_demo",
                name = "Omkar Karbhari",
                email = "omkarkarbhari02@gmail.com",
                phone = "+91 98765 43210",
                role = UserRole.STUDENT,
                bamsYear = "BAMS 2nd Year"
            )
            userDao.insertUser(student)
        }
        if (userDao.getUserById("owner_admin") == null) {
            val owner = UserEntity(
                id = "owner_admin",
                name = "Dr. Vikram Sharma (BAMS MD)",
                email = "owner@swasthyanotes.com",
                phone = "+91 98111 22334",
                role = UserRole.OWNER,
                bamsYear = "Faculty"
            )
            userDao.insertUser(owner)
        }

        // Set default logged-in user to student
        if (_currentUser.value == null) {
            _currentUser.value = userDao.getUserById("student_demo")
        }

        // Seed App Settings
        if (settingsDao.getSetting("owner_upi_id") == null) {
            settingsDao.setSetting(AppSettingEntity("owner_upi_id", "swasthya.notes@oksbi"))
            settingsDao.setSetting(AppSettingEntity("owner_upi_name", "Dr. V. Sharma (BAMS Academic Portal)"))
            settingsDao.setSetting(AppSettingEntity("owner_contact_email", "support@swasthyanotes.com"))
            settingsDao.setSetting(AppSettingEntity("owner_contact_phone", "+91 98111 22334"))
            settingsDao.setSetting(AppSettingEntity("price_bundle_year1", "599"))
            settingsDao.setSetting(AppSettingEntity("price_bundle_year2", "699"))
            settingsDao.setSetting(AppSettingEntity("price_bundle_year3", "799"))
        }

        // Seed Initial Content if empty
        seedContentIfNeeded()

        // Seed initial access requests for demo
        seedRequestsIfNeeded()

        // Seed initial notifications
        seedNotificationsIfNeeded()
    }

    private suspend fun seedContentIfNeeded() {
        val count = contentDao.getContentById("year_1")
        if (count != null) return

        val items = mutableListOf<ContentItemEntity>()

        // 1. YEARS
        items.add(
            ContentItemEntity(
                id = "year_1",
                title = "BAMS 1st Year",
                description = "Foundational Ayurvedic Principles, Anatomy, Physiology & Classical Samhita",
                type = ContentType.YEAR,
                parentId = null,
                yearTag = "BAMS 1st Year",
                price = 0,
                isLockedByDefault = false,
                orderIndex = 1
            )
        )
        items.add(
            ContentItemEntity(
                id = "year_2",
                title = "BAMS 2nd Year",
                description = "Ayurvedic Pharmacology (Dravyaguna), Toxicology, Pharmacy & Pathology",
                type = ContentType.YEAR,
                parentId = null,
                yearTag = "BAMS 2nd Year",
                price = 0,
                isLockedByDefault = false,
                orderIndex = 2
            )
        )
        items.add(
            ContentItemEntity(
                id = "year_3",
                title = "BAMS 3rd Year",
                description = "Clinical Medicine (Charaka Samhita), Panchakarma & Preventive Medicine",
                type = ContentType.YEAR,
                parentId = null,
                yearTag = "BAMS 3rd Year",
                price = 0,
                isLockedByDefault = false,
                orderIndex = 3
            )
        )

        // 2. SUBJECTS - 1ST YEAR
        items.add(
            ContentItemEntity(
                id = "sub_padartha",
                title = "Padartha Vigyana",
                description = "Ayurvedic Philosophy, Epistemology & Fundamental Truths",
                type = ContentType.SUBJECT,
                parentId = "year_1",
                yearTag = "BAMS 1st Year",
                subjectTag = "Padartha Vigyana",
                price = 0,
                orderIndex = 1
            )
        )
        items.add(
            ContentItemEntity(
                id = "sub_samhita_1",
                title = "Samhita Adhyayana (Ashtanga Hridaya)",
                description = "Sutrasthana text study, Dinacharya, Ritucharya & Roganutpadaniya",
                type = ContentType.SUBJECT,
                parentId = "year_1",
                yearTag = "BAMS 1st Year",
                subjectTag = "Samhita",
                price = 0,
                orderIndex = 2
            )
        )
        items.add(
            ContentItemEntity(
                id = "sub_rachana",
                title = "Rachana Sharira",
                description = "Ayurvedic Human Anatomy, Embryology, Marmas & Asthi-Sandhi",
                type = ContentType.SUBJECT,
                parentId = "year_1",
                yearTag = "BAMS 1st Year",
                subjectTag = "Rachana Sharira",
                price = 0,
                orderIndex = 3
            )
        )
        items.add(
            ContentItemEntity(
                id = "sub_kriya",
                title = "Kriya Sharira",
                description = "Ayurvedic Human Physiology, Dosha, Dhatu, Mala & Agni",
                type = ContentType.SUBJECT,
                parentId = "year_1",
                yearTag = "BAMS 1st Year",
                subjectTag = "Kriya Sharira",
                price = 0,
                orderIndex = 4
            )
        )

        // 3. SUBJECTS - 2ND YEAR
        items.add(
            ContentItemEntity(
                id = "sub_dravyaguna",
                title = "Dravyaguna Vijnana",
                description = "Ayurvedic Pharmacology, Materia Medica & Plant Formulations",
                type = ContentType.SUBJECT,
                parentId = "year_2",
                yearTag = "BAMS 2nd Year",
                subjectTag = "Dravyaguna",
                price = 0,
                orderIndex = 1
            )
        )
        items.add(
            ContentItemEntity(
                id = "sub_rasashastra",
                title = "Rasashastra & Bhaishajya Kalpana",
                description = "Iatrochemistry, Mineral Alchemy, Shodhana, Marana & Pharmaceutical Forms",
                type = ContentType.SUBJECT,
                parentId = "year_2",
                yearTag = "BAMS 2nd Year",
                subjectTag = "Rasashastra",
                price = 0,
                orderIndex = 2
            )
        )
        items.add(
            ContentItemEntity(
                id = "sub_agadtantra",
                title = "Agadtantra & Vyavahara Ayurveda",
                description = "Ayurvedic Toxicology, Forensic Medicine & Medical Jurisprudence",
                type = ContentType.SUBJECT,
                parentId = "year_2",
                yearTag = "BAMS 2nd Year",
                subjectTag = "Agadtantra",
                price = 0,
                orderIndex = 3
            )
        )
        items.add(
            ContentItemEntity(
                id = "sub_roganidana",
                title = "Roga Nidana & Vikriti Vijnana",
                description = "Ayurvedic Pathology, Diagnostic Methodologies & Samprapti",
                type = ContentType.SUBJECT,
                parentId = "year_2",
                yearTag = "BAMS 2nd Year",
                subjectTag = "Roga Nidana",
                price = 0,
                orderIndex = 4
            )
        )

        // 4. SUBJECTS - 3RD YEAR
        items.add(
            ContentItemEntity(
                id = "sub_charaka",
                title = "Charaka Samhita (Purvardha & Uttarardha)",
                description = "Classical Internal Medicine, Chikitsa Sthana & Clinical Protocols",
                type = ContentType.SUBJECT,
                parentId = "year_3",
                yearTag = "BAMS 3rd Year",
                subjectTag = "Charaka Samhita",
                price = 0,
                orderIndex = 1
            )
        )
        items.add(
            ContentItemEntity(
                id = "sub_swasthavritta",
                title = "Swasthavritta & Yoga",
                description = "Preventive Medicine, Social Hygiene, Dietary Regimens & Yoga Therapy",
                type = ContentType.SUBJECT,
                parentId = "year_3",
                yearTag = "BAMS 3rd Year",
                subjectTag = "Swasthavritta",
                price = 0,
                orderIndex = 2
            )
        )
        items.add(
            ContentItemEntity(
                id = "sub_panchakarma",
                title = "Panchakarma Vijnana",
                description = "Purvakarma (Snehana/Swedana), Pradhanakarma (5 Purifications) & Paschatkarma",
                type = ContentType.SUBJECT,
                parentId = "year_3",
                yearTag = "BAMS 3rd Year",
                subjectTag = "Panchakarma",
                price = 0,
                orderIndex = 3
            )
        )

        // 5. CHAPTERS & DETAILED MATERIALS UNDER PADARTHA VIGYANA
        items.add(
            ContentItemEntity(
                id = "ch_padartha_1",
                title = "Chapter 1: Ayurveda Nirupana & Lakshana",
                description = "Definition of Ayu, Ayu Lakshana, Ayu Bheda & Prayojana of Ayurveda",
                type = ContentType.CHAPTER,
                parentId = "sub_padartha",
                yearTag = "BAMS 1st Year",
                subjectTag = "Padartha Vigyana",
                orderIndex = 1
            )
        )
        items.add(
            ContentItemEntity(
                id = "pdf_padartha_1_notes",
                title = "Ayurveda Nirupana Master Notes (Sanskrit + English)",
                description = "Comprehensive study material with classical shlokas and modern analysis",
                type = ContentType.PDF_NOTE,
                parentId = "ch_padartha_1",
                yearTag = "BAMS 1st Year",
                subjectTag = "Padartha Vigyana",
                price = 0, // FREE SAMPLE
                isLockedByDefault = false,
                pageCount = 4,
                contentSummary = "Complete breakdown of Ayu definition (Sharira-Indriya-Satva-Atma Samyoga), 4 types of Ayu, and Swasthyasya Swasthya Rakshanam.",
                orderIndex = 1
            )
        )
        items.add(
            ContentItemEntity(
                id = "qb_padartha_1",
                title = "Question Bank: Ayurveda Nirupana & Epistemology",
                description = "Solved 2-mark, 5-mark, and 10-mark questions with model answer formatting",
                type = ContentType.QUESTION_BANK,
                parentId = "ch_padartha_1",
                yearTag = "BAMS 1st Year",
                subjectTag = "Padartha Vigyana",
                price = 0, // FREE
                isLockedByDefault = false,
                pageCount = 3,
                orderIndex = 2
            )
        )
        items.add(
            ContentItemEntity(
                id = "pyq_padartha_1",
                title = "Previous Year Questions (PYQs 2018-2024)",
                description = "University solved examination papers with high-yield examiner remarks",
                type = ContentType.PYQ,
                parentId = "ch_padartha_1",
                yearTag = "BAMS 1st Year",
                subjectTag = "Padartha Vigyana",
                price = 99, // PAID
                isLockedByDefault = true,
                pageCount = 5,
                contentSummary = "7-year university trends, repeated question highlights, and schematic diagrams for scoring full marks.",
                orderIndex = 3
            )
        )

        // Chapter 2 under Padartha Vigyana
        items.add(
            ContentItemEntity(
                id = "ch_padartha_2",
                title = "Chapter 2: Dravya Vijnaniya & Panchamahabhutas",
                description = "Karanadravya, Karyadravya, Mahabhuta genesis and elemental characteristics",
                type = ContentType.CHAPTER,
                parentId = "sub_padartha",
                yearTag = "BAMS 1st Year",
                subjectTag = "Padartha Vigyana",
                orderIndex = 2
            )
        )
        items.add(
            ContentItemEntity(
                id = "pdf_padartha_2_notes",
                title = "Dravya & Panchamahabhuta In-Depth Notes",
                description = "9 Karana Dravyas, Chetan-Achetana classification and Tanmatra evolution",
                type = ContentType.PDF_NOTE,
                parentId = "ch_padartha_2",
                yearTag = "BAMS 1st Year",
                subjectTag = "Padartha Vigyana",
                price = 99, // PAID
                isLockedByDefault = true,
                pageCount = 4,
                contentSummary = "9 Karana Dravyas (Akasha, Vayu, Agni, Jala, Prithvi, Atma, Manas, Kala, Dik) explained systematically.",
                orderIndex = 1
            )
        )

        // 6. SAMHITA (ASHTANGA HRIDAYA)
        items.add(
            ContentItemEntity(
                id = "ch_samhita_1",
                title = "Chapter 1: Ayushkamiya Adhyaya",
                description = "Desire for longevity, Trivarga (Dharma, Artha, Kama), and Trisutra Ayurveda",
                type = ContentType.CHAPTER,
                parentId = "sub_samhita_1",
                yearTag = "BAMS 1st Year",
                subjectTag = "Samhita",
                orderIndex = 1
            )
        )
        items.add(
            ContentItemEntity(
                id = "pdf_samhita_1_notes",
                title = "Ayushkamiya Sutrasthana Comprehensive Notes",
                description = "All 43 shlokas with word-to-word anvaya, translation and clinical notes",
                type = ContentType.PDF_NOTE,
                parentId = "ch_samhita_1",
                yearTag = "BAMS 1st Year",
                subjectTag = "Samhita",
                price = 149, // PAID
                isLockedByDefault = true,
                pageCount = 6,
                contentSummary = "Detailed word-to-word Anvaya for Ashtanga Hridaya Sutrasthana Chapter 1.",
                orderIndex = 1
            )
        )

        // 7. RACHANA SHARIRA (ANATOMY)
        items.add(
            ContentItemEntity(
                id = "ch_rachana_1",
                title = "Chapter 1: Marma Sharira (107 Vital Points)",
                description = "Classification, locations, dimensions (Anguli Pramana) & injury trauma",
                type = ContentType.CHAPTER,
                parentId = "sub_rachana",
                yearTag = "BAMS 1st Year",
                subjectTag = "Rachana Sharira",
                orderIndex = 1
            )
        )
        items.add(
            ContentItemEntity(
                id = "pdf_marma_notes",
                title = "Complete Marma Sharira Atlas & Clinical Guide",
                description = "Sadhyo-Pranahara, Kalantara, Vishalyaghna, Vaikalyakara, and Rujakara Marmas",
                type = ContentType.PDF_NOTE,
                parentId = "ch_rachana_1",
                yearTag = "BAMS 1st Year",
                subjectTag = "Rachana Sharira",
                price = 149, // PAID
                isLockedByDefault = true,
                pageCount = 6,
                contentSummary = "Detailed table of 107 Marmas categorized by location (Shakha, Kostha, Urdhwajatru) and prognosis.",
                orderIndex = 1
            )
        )
        items.add(
            ContentItemEntity(
                id = "sn_marma_quick",
                title = "Marma Quick Mnemonics & Exam Short Notes",
                description = "High-speed mnemonic charts for practical viva and NEET PG preparation",
                type = ContentType.SHORT_NOTE,
                parentId = "ch_rachana_1",
                yearTag = "BAMS 1st Year",
                subjectTag = "Rachana Sharira",
                price = 49,
                isLockedByDefault = true,
                pageCount = 2,
                orderIndex = 2
            )
        )

        // 8. KRIYA SHARIRA (PHYSIOLOGY)
        items.add(
            ContentItemEntity(
                id = "ch_kriya_1",
                title = "Chapter 1: Dosha-Dhatu-Mala Vijnana",
                description = "Prakrita Karma of Vata-Pitta-Kapha, 7 Dhatus formation and Upadhatus",
                type = ContentType.CHAPTER,
                parentId = "sub_kriya",
                yearTag = "BAMS 1st Year",
                subjectTag = "Kriya Sharira",
                orderIndex = 1
            )
        )
        items.add(
            ContentItemEntity(
                id = "pdf_kriya_1_notes",
                title = "Dosha-Dhatu-Mala Complete Textbook Notes",
                description = "Physiological correlations, homeostasis, Dhatu Poshana Nyayas (Kshira-Dadhi, etc.)",
                type = ContentType.PDF_NOTE,
                parentId = "ch_kriya_1",
                yearTag = "BAMS 1st Year",
                subjectTag = "Kriya Sharira",
                price = 149,
                isLockedByDefault = true,
                pageCount = 5,
                contentSummary = "Detailed breakdown of Dhatu Poshana Nyayas: Kshira-Dadhi, Kedari-Kulya, Khale-Kapota.",
                orderIndex = 1
            )
        )

        // 9. DRAVYAGUNA (2ND YEAR)
        items.add(
            ContentItemEntity(
                id = "ch_dg_1",
                title = "Chapter 1: Saptapadartha of Dravyaguna",
                description = "Rasa, Guna, Virya, Vipaka, Prabhava & Karma pharmacodynamics",
                type = ContentType.CHAPTER,
                parentId = "sub_dravyaguna",
                yearTag = "BAMS 2nd Year",
                subjectTag = "Dravyaguna",
                orderIndex = 1
            )
        )
        items.add(
            ContentItemEntity(
                id = "pdf_dg_1_notes",
                title = "Saptapadartha Pharmacodynamics & Action Principles",
                description = "6 Rasas, 20 Gurvadi Gunas, Ushna/Sheeta Virya, 3 Vipakas, and Prabhava actions",
                type = ContentType.PDF_NOTE,
                parentId = "ch_dg_1",
                yearTag = "BAMS 2nd Year",
                subjectTag = "Dravyaguna",
                price = 149,
                isLockedByDefault = true,
                pageCount = 5,
                contentSummary = "Detailed tables of Rasa-Panchamahabhuta relation, Shad Rasa physiological actions, and Vipaka comparisons.",
                orderIndex = 1
            )
        )
        items.add(
            ContentItemEntity(
                id = "ch_dg_2",
                title = "Chapter 2: Important Medicinal Plants Monographs",
                description = "Ashwagandha, Guduchi, Shatavari, Haritaki, Amalaki, Bibhitaki botanical monographs",
                type = ContentType.CHAPTER,
                parentId = "sub_dravyaguna",
                yearTag = "BAMS 2nd Year",
                subjectTag = "Dravyaguna",
                orderIndex = 2
            )
        )
        items.add(
            ContentItemEntity(
                id = "pdf_dg_herbs",
                title = "50 Essential Ayurvedic Herbs Monograph Handbook",
                description = "Botanical name, family, vernacular names, morphology, rasapanchaka, dosage & yogas",
                type = ContentType.PDF_NOTE,
                parentId = "ch_dg_2",
                yearTag = "BAMS 2nd Year",
                subjectTag = "Dravyaguna",
                price = 199,
                isLockedByDefault = true,
                pageCount = 6,
                contentSummary = "Standardized drug study for Dravyaguna paper 2: Ashwagandha, Guduchi, Haritaki, Arjuna, Guggulu, etc.",
                orderIndex = 1
            )
        )

        // 10. RASASHASTRA (2ND YEAR)
        items.add(
            ContentItemEntity(
                id = "ch_rs_1",
                title = "Chapter 1: Parada Vijnana & Ashtadasha Samskaras",
                description = "Mercury origins, doshas, purification methods and medicinal preparations",
                type = ContentType.CHAPTER,
                parentId = "sub_rasashastra",
                yearTag = "BAMS 2nd Year",
                subjectTag = "Rasashastra",
                orderIndex = 1
            )
        )
        items.add(
            ContentItemEntity(
                id = "pdf_rs_parada",
                title = "Parada Samskara & Rasashastra Protocols",
                description = "Samanya Shodhana, Vishesha Shodhana, Kajjali, Rasasindura manufacturing protocols",
                type = ContentType.PDF_NOTE,
                parentId = "ch_rs_1",
                yearTag = "BAMS 2nd Year",
                subjectTag = "Rasashastra",
                price = 149,
                isLockedByDefault = true,
                pageCount = 5,
                contentSummary = "Step-by-step preparation of Kajjali, Kupipakwa Rasayana, and Bhasma Parikshas (Varitara, Rekhapurna).",
                orderIndex = 1
            )
        )

        // 11. CHARAKA SAMHITA (3RD YEAR)
        items.add(
            ContentItemEntity(
                id = "ch_cs_1",
                title = "Chapter 1: Jwara Chikitsa Adhyaya",
                description = "Types of Jwara, Santata-Satataka-Anyedyushka, Samprapti and Langhana treatment",
                type = ContentType.CHAPTER,
                parentId = "sub_charaka",
                yearTag = "BAMS 3rd Year",
                subjectTag = "Charaka Samhita",
                orderIndex = 1
            )
        )
        items.add(
            ContentItemEntity(
                id = "pdf_cs_jwara",
                title = "Jwara Chikitsa Master Notes & Flowcharts",
                description = "Detailed stages of Jwara: Ama, Pachyamana, and Nirama Jwara chikitsa sutra",
                type = ContentType.PDF_NOTE,
                parentId = "ch_cs_1",
                yearTag = "BAMS 3rd Year",
                subjectTag = "Charaka Samhita",
                price = 199,
                isLockedByDefault = true,
                pageCount = 5,
                contentSummary = "Jwaramukta Lakshana, Punaravartaka Jwara management, and standard classical Kashaya yogas.",
                orderIndex = 1
            )
        )

        // 12. PANCHAKARMA (3RD YEAR)
        items.add(
            ContentItemEntity(
                id = "ch_pk_1",
                title = "Chapter 1: Basti Karma Protocols & Classification",
                description = "Anuvasana, Niruha, Matra Basti, dosage calculation and classical basti netra",
                type = ContentType.CHAPTER,
                parentId = "sub_panchakarma",
                yearTag = "BAMS 3rd Year",
                subjectTag = "Panchakarma",
                orderIndex = 1
            )
        )
        items.add(
            ContentItemEntity(
                id = "pdf_pk_basti",
                title = "Basti Karma Clinical Practice Handbook",
                description = "Ardha Chikitsa concept, Niruha Basti preparation sequence (Makshika, Lavana, Sneha, Kalka, Kwatha)",
                type = ContentType.PDF_NOTE,
                parentId = "ch_pk_1",
                yearTag = "BAMS 3rd Year",
                subjectTag = "Panchakarma",
                price = 199,
                isLockedByDefault = true,
                pageCount = 5,
                contentSummary = "Detailed instructions on Basti preparation, pratyagamana kala, vyapat management, and karma-kala-yoga regimens.",
                orderIndex = 1
            )
        )

        contentDao.insertAll(items)
    }

    private suspend fun seedRequestsIfNeeded() {
        if (accessRequestDao.getRequestForUserAndItem("student_demo", "pdf_marma_notes") != null) return

        val req1 = AccessRequestEntity(
            id = "req_101",
            userId = "student_demo",
            userName = "Omkar Karbhari",
            userEmail = "omkarkarbhari02@gmail.com",
            contentItemId = "pdf_marma_notes",
            contentTitle = "Complete Marma Sharira Atlas & Clinical Guide",
            contentYear = "BAMS 1st Year",
            pricePaid = 149,
            utrNumber = "428190382714",
            screenshotUri = "payment_receipt_sample.jpg",
            message = "Completed UPI transfer via GPay. Please grant access for upcoming university exam preparation.",
            status = RequestStatus.PENDING,
            requestedAt = System.currentTimeMillis() - (3600 * 1000 * 2)
        )

        val req2 = AccessRequestEntity(
            id = "req_102",
            userId = "student_user_2",
            userName = "Pooja Kulkarni",
            userEmail = "pooja.bams@gmail.com",
            contentItemId = "pdf_dg_herbs",
            contentTitle = "50 Essential Ayurvedic Herbs Monograph Handbook",
            contentYear = "BAMS 2nd Year",
            pricePaid = 199,
            utrNumber = "519283746102",
            screenshotUri = "payment_receipt_pooja.jpg",
            message = "Payment made via PhonePe. Kindly approve.",
            status = RequestStatus.PENDING,
            requestedAt = System.currentTimeMillis() - (3600 * 1000 * 5)
        )

        val req3 = AccessRequestEntity(
            id = "req_103",
            userId = "student_user_3",
            userName = "Amitabh Patil",
            userEmail = "amitabh.ayur@gmail.com",
            contentItemId = "year_1",
            contentTitle = "BAMS 1st Year Complete Notes Bundle",
            contentYear = "BAMS 1st Year",
            pricePaid = 599,
            utrNumber = "982736154019",
            screenshotUri = "receipt_bundle.jpg",
            message = "Purchased annual bundle for 1st Year.",
            status = RequestStatus.APPROVED,
            requestedAt = System.currentTimeMillis() - (3600 * 1000 * 24),
            reviewedAt = System.currentTimeMillis() - (3600 * 1000 * 20),
            ownerNote = "Approved lifetime access for complete 1st year bundle."
        )

        accessRequestDao.insertRequest(req1)
        accessRequestDao.insertRequest(req2)
        accessRequestDao.insertRequest(req3)
    }

    private suspend fun seedNotificationsIfNeeded() {
        val notif1 = NotificationEntity(
            id = "notif_1",
            title = "Welcome to Swasthya Notes",
            message = "Access authentic BAMS high-yield notes, PYQs with verified shlokas, and question banks. Request access to premium material with Owner UPI approval.",
            targetAudience = "ALL",
            timestamp = System.currentTimeMillis() - (3600 * 1000 * 48)
        )
        val notif2 = NotificationEntity(
            id = "notif_2",
            title = "New 2nd Year Dravyaguna PYQs Uploaded",
            message = "Dr. Vikram Sharma has added 2024 university question solutions for Dravyaguna and Rasashastra. Check your 2nd year section.",
            targetAudience = "BAMS 2nd Year",
            timestamp = System.currentTimeMillis() - (3600 * 1000 * 12)
        )
        notificationDao.insertNotification(notif1)
        notificationDao.insertNotification(notif2)
    }

    // AUTH / USER MANAGEMENT
    fun getAllUsersFlow(): Flow<List<UserEntity>> = userDao.getAllUsersFlow()
    fun getUserCountFlow(): Flow<Int> = userDao.getUserCountFlow()

    suspend fun switchActiveUser(userId: String) = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(userId)
        if (user != null) {
            _currentUser.value = user
        }
    }

    suspend fun loginOrRegister(name: String, email: String, phone: String, role: UserRole, bamsYear: String): UserEntity = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByEmail(email)
        if (existing != null) {
            _currentUser.value = existing
            return@withContext existing
        }
        val newUser = UserEntity(
            id = "usr_" + UUID.randomUUID().toString().take(8),
            name = name,
            email = email,
            phone = phone,
            role = role,
            bamsYear = bamsYear
        )
        userDao.insertUser(newUser)
        _currentUser.value = newUser
        newUser
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
        if (_currentUser.value?.id == user.id) {
            _currentUser.value = user
        }
    }

    suspend fun deleteUser(userId: String) = withContext(Dispatchers.IO) {
        userDao.deleteUser(userId)
        offlineDownloadDao.deleteAllUserDownloads(userId)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        val uid = _currentUser.value?.id
        if (uid != null) {
            // Per requirements: Offline PDFs stop working if user logs out or access revoked
            offlineDownloadDao.deleteAllUserDownloads(uid)
            AES256CryptoManager.clearAllOfflineFiles(context)
        }
        _currentUser.value = null
    }

    // CONTENT HIERARCHY
    fun getYearsFlow(): Flow<List<ContentItemEntity>> = contentDao.getYearsFlow()
    fun getChildrenFlow(parentId: String): Flow<List<ContentItemEntity>> = contentDao.getChildrenFlow(parentId)
    fun getContentByYearFlow(yearTag: String): Flow<List<ContentItemEntity>> = contentDao.getContentByYearFlow(yearTag)
    fun searchContentFlow(query: String): Flow<List<ContentItemEntity>> = contentDao.searchContentFlow(query)
    fun getTotalPdfsCountFlow(): Flow<Int> = contentDao.getTotalPdfsCountFlow()

    suspend fun getContentById(id: String): ContentItemEntity? = withContext(Dispatchers.IO) {
        contentDao.getContentById(id)
    }

    suspend fun saveContentItem(item: ContentItemEntity) = withContext(Dispatchers.IO) {
        contentDao.insertItem(item)
    }

    suspend fun deleteContentItem(id: String) = withContext(Dispatchers.IO) {
        contentDao.deleteItemAndChildren(id)
    }

    // PERMISSIONS & ACCESS CHECKS
    fun getUserPermissionsFlow(userId: String): Flow<List<AccessPermissionEntity>> = accessDao.getUserPermissionsFlow(userId)

    suspend fun hasAccess(userId: String, contentItem: ContentItemEntity): Boolean = withContext(Dispatchers.IO) {
        // Owner has absolute access to everything
        val user = userDao.getUserById(userId)
        if (user?.role == UserRole.OWNER) return@withContext true

        // Free items are accessible by any authenticated student
        if (!contentItem.isLockedByDefault && contentItem.price == 0) return@withContext true

        // Check direct permission for this item
        val perm = accessDao.getPermission(userId, contentItem.id)
        if (perm != null && perm.isActive) {
            if (perm.expiresAt == null || perm.expiresAt > System.currentTimeMillis()) {
                return@withContext true
            }
        }

        // Check permission for parent folder / subject
        if (contentItem.parentId != null) {
            val parentPerm = accessDao.getPermission(userId, contentItem.parentId)
            if (parentPerm != null && parentPerm.isActive) {
                if (parentPerm.expiresAt == null || parentPerm.expiresAt > System.currentTimeMillis()) {
                    return@withContext true
                }
            }
        }

        // Check permission for whole Year bundle
        val yearId = when (contentItem.yearTag) {
            "BAMS 1st Year" -> "year_1"
            "BAMS 2nd Year" -> "year_2"
            "BAMS 3rd Year" -> "year_3"
            else -> ""
        }
        if (yearId.isNotEmpty()) {
            val yearPerm = accessDao.getPermission(userId, yearId)
            if (yearPerm != null && yearPerm.isActive) {
                if (yearPerm.expiresAt == null || yearPerm.expiresAt > System.currentTimeMillis()) {
                    return@withContext true
                }
            }
        }

        return@withContext false
    }

    // ACCESS REQUESTS
    fun getAllRequestsFlow(): Flow<List<AccessRequestEntity>> = accessRequestDao.getAllRequestsFlow()
    fun getPendingRequestsFlow(): Flow<List<AccessRequestEntity>> = accessRequestDao.getPendingRequestsFlow()
    fun getRequestsByUserFlow(userId: String): Flow<List<AccessRequestEntity>> = accessRequestDao.getRequestsByUserFlow(userId)
    fun getPendingCountFlow(): Flow<Int> = accessRequestDao.getPendingCountFlow()
    fun getApprovedCountFlow(): Flow<Int> = accessRequestDao.getApprovedCountFlow()

    suspend fun getRequestForUserAndItem(userId: String, contentItemId: String): AccessRequestEntity? = withContext(Dispatchers.IO) {
        accessRequestDao.getRequestForUserAndItem(userId, contentItemId)
    }

    suspend fun submitAccessRequest(
        userId: String,
        userName: String,
        userEmail: String,
        item: ContentItemEntity,
        utrNumber: String,
        screenshotUri: String,
        message: String
    ): AccessRequestEntity = withContext(Dispatchers.IO) {
        val req = AccessRequestEntity(
            id = "req_" + UUID.randomUUID().toString().take(8),
            userId = userId,
            userName = userName,
            userEmail = userEmail,
            contentItemId = item.id,
            contentTitle = item.title,
            contentYear = item.yearTag,
            pricePaid = item.price,
            utrNumber = utrNumber,
            screenshotUri = screenshotUri,
            message = message,
            status = RequestStatus.PENDING,
            requestedAt = System.currentTimeMillis()
        )
        accessRequestDao.insertRequest(req)
        req
    }

    // OWNER ACTIONS ON REQUESTS
    suspend fun approveRequest(
        requestId: String,
        accessType: AccessType,
        expiryDays: Int? = null,
        ownerNote: String = ""
    ) = withContext(Dispatchers.IO) {
        val requests = accessRequestDao.getAllRequestsFlow()
        // Find the request
        val all = db.accessRequestDao()
        val req = db.runInTransaction<AccessRequestEntity?> {
            // Find in db
            null
        }
    }

    suspend fun grantAccessDirect(
        userId: String,
        contentItemId: String,
        accessType: AccessType,
        expiryDays: Int? = null,
        requestId: String? = null,
        ownerNote: String = ""
    ) = withContext(Dispatchers.IO) {
        val expiresAt = if (expiryDays != null && expiryDays > 0) {
            System.currentTimeMillis() + (expiryDays.toLong() * 24 * 3600 * 1000)
        } else null

        val perm = AccessPermissionEntity(
            id = "perm_" + UUID.randomUUID().toString().take(8),
            userId = userId,
            contentItemId = contentItemId,
            accessType = accessType,
            grantedAt = System.currentTimeMillis(),
            expiresAt = expiresAt,
            isActive = true
        )
        accessDao.insertPermission(perm)

        if (requestId != null) {
            val user = userDao.getUserById(userId)
            val item = contentDao.getContentById(contentItemId)
            val updatedReq = AccessRequestEntity(
                id = requestId,
                userId = userId,
                userName = user?.name ?: "Student",
                userEmail = user?.email ?: "",
                contentItemId = contentItemId,
                contentTitle = item?.title ?: "Material",
                contentYear = item?.yearTag ?: "",
                pricePaid = item?.price ?: 0,
                utrNumber = "APPROVED_MANUAL",
                status = RequestStatus.APPROVED,
                reviewedAt = System.currentTimeMillis(),
                expiryDaysGranted = expiryDays,
                ownerNote = ownerNote
            )
            accessRequestDao.updateRequest(updatedReq)
        }

        // Send confirmation notification to student
        val notif = NotificationEntity(
            id = "notif_" + UUID.randomUUID().toString().take(8),
            title = "Access Approved!",
            message = "The Owner has approved your access to: ${contentDao.getContentById(contentItemId)?.title ?: "Study Material"}.",
            targetAudience = userId,
            timestamp = System.currentTimeMillis()
        )
        notificationDao.insertNotification(notif)
    }

    suspend fun rejectRequest(
        request: AccessRequestEntity,
        reason: String,
        askForAnotherScreenshot: Boolean = false
    ) = withContext(Dispatchers.IO) {
        val status = if (askForAnotherScreenshot) RequestStatus.NEED_SCREENSHOT else RequestStatus.REJECTED
        val updated = request.copy(
            status = status,
            reviewedAt = System.currentTimeMillis(),
            ownerNote = reason
        )
        accessRequestDao.updateRequest(updated)

        val notif = NotificationEntity(
            id = "notif_" + UUID.randomUUID().toString().take(8),
            title = if (askForAnotherScreenshot) "Action Required: Re-upload Screenshot" else "Access Request Declined",
            message = "Status update for ${request.contentTitle}: $reason",
            targetAudience = request.userId,
            timestamp = System.currentTimeMillis()
        )
        notificationDao.insertNotification(notif)
    }

    suspend fun revokeAccess(userId: String, contentItemId: String) = withContext(Dispatchers.IO) {
        accessDao.revokePermission(userId, contentItemId)
        // Also remove from offline downloads
        offlineDownloadDao.deleteDownload(userId, contentItemId)
        AES256CryptoManager.deleteEncryptedFile(context, "vault_${userId}_${contentItemId}")
    }

    // OFFLINE DOWNLOADS & AES-256 ENCRYPTED STORAGE
    fun getOfflineDownloadsFlow(userId: String): Flow<List<OfflineDownloadEntity>> = offlineDownloadDao.getDownloadsByUserFlow(userId)
    fun getTotalDownloadsCountFlow(): Flow<Int> = offlineDownloadDao.getTotalDownloadsCountFlow()

    suspend fun downloadForOffline(userId: String, item: ContentItemEntity): Boolean = withContext(Dispatchers.IO) {
        // First verify active permission
        if (!hasAccess(userId, item)) return@withContext false

        // Generate the authentic PDF document bytes
        val rawPdfBytes = SecurePdfGenerator.generateSamplePdf(item)

        // Encrypt with AES-256 and store in private internal storage
        val fileName = "vault_${userId}_${item.id}"
        val savedFile = AES256CryptoManager.saveEncryptedFile(context, fileName, rawPdfBytes)

        val entity = OfflineDownloadEntity(
            contentItemId = item.id,
            userId = userId,
            title = item.title,
            yearTag = item.yearTag,
            subjectTag = item.subjectTag,
            encryptedFilePath = savedFile.absolutePath,
            downloadedAt = System.currentTimeMillis(),
            fileSizeBytes = savedFile.length()
        )
        offlineDownloadDao.insertDownload(entity)
        true
    }

    suspend fun getDecryptedPdfBytes(userId: String, item: ContentItemEntity): ByteArray? = withContext(Dispatchers.IO) {
        // Re-verify permission
        if (!hasAccess(userId, item)) {
            // Revoked! Invalidate offline copy
            offlineDownloadDao.deleteDownload(userId, item.id)
            AES256CryptoManager.deleteEncryptedFile(context, "vault_${userId}_${item.id}")
            return@withContext null
        }

        val fileName = "vault_${userId}_${item.id}"
        val decrypted = AES256CryptoManager.readEncryptedFile(context, fileName)
        if (decrypted != null) {
            return@withContext decrypted
        }

        // If not downloaded yet, generate on-the-fly for in-app viewing
        return@withContext SecurePdfGenerator.generateSamplePdf(item)
    }

    suspend fun deleteOfflineDownload(userId: String, contentItemId: String) = withContext(Dispatchers.IO) {
        offlineDownloadDao.deleteDownload(userId, contentItemId)
        AES256CryptoManager.deleteEncryptedFile(context, "vault_${userId}_${contentItemId}")
    }

    // SETTINGS
    fun getAllSettingsFlow(): Flow<List<AppSettingEntity>> = settingsDao.getAllSettingsFlow()

    suspend fun getSetting(key: String): String? = withContext(Dispatchers.IO) {
        settingsDao.getSetting(key)
    }

    suspend fun updateSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        settingsDao.setSetting(AppSettingEntity(key, value))
    }

    // NOTIFICATIONS
    fun getAllNotificationsFlow(): Flow<List<NotificationEntity>> = notificationDao.getAllNotificationsFlow()

    suspend fun sendBroadcastNotification(title: String, message: String, targetAudience: String) = withContext(Dispatchers.IO) {
        val notif = NotificationEntity(
            id = "notif_" + UUID.randomUUID().toString().take(8),
            title = title,
            message = message,
            targetAudience = targetAudience,
            timestamp = System.currentTimeMillis()
        )
        notificationDao.insertNotification(notif)
    }

    suspend fun deleteNotification(id: String) = withContext(Dispatchers.IO) {
        notificationDao.deleteNotification(id)
    }
}
