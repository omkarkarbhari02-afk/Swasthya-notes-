package com.example.data.security

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import com.example.data.model.ContentItemEntity
import java.io.ByteArrayOutputStream

object SecurePdfGenerator {

    fun generateSamplePdf(item: ContentItemEntity): ByteArray {
        val document = PdfDocument()
        val pageWidth = 595 // A4 standard width in points at 72dpi
        val pageHeight = 842 // A4 standard height

        // Paints
        val headerPaint = Paint().apply {
            color = Color.rgb(0, 108, 81) // Deep Emerald
            style = Paint.Style.FILL
        }
        val titlePaint = Paint().apply {
            color = Color.rgb(20, 20, 20)
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subTitlePaint = Paint().apply {
            color = Color.rgb(100, 100, 100)
            textSize = 12f
            isAntiAlias = true
        }
        val sectionHeadingPaint = Paint().apply {
            color = Color.rgb(0, 108, 81)
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(40, 40, 40)
            textSize = 10.5f
            isAntiAlias = true
        }
        val shlokaBoxPaint = Paint().apply {
            color = Color.rgb(245, 248, 246)
            style = Paint.Style.FILL
        }
        val shlokaBorderPaint = Paint().apply {
            color = Color.rgb(180, 215, 200)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val shlokaTextPaint = Paint().apply {
            color = Color.rgb(132, 84, 0) // Warm Sanskrit amber
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val footerPaint = Paint().apply {
            color = Color.rgb(140, 140, 140)
            textSize = 9f
            isAntiAlias = true
        }

        val totalPages = maxOf(2, item.pageCount)

        for (pageIndex in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Top decorative banner
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 48f, headerPaint)

            val headerTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 13f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText("SWASTHYA NOTES • BAMS SECURE MEDICAL ACADEMY", 30f, 30f, headerTextPaint)

            var yPos = 75f

            if (pageIndex == 1) {
                // Document Title
                canvas.drawText(item.title, 30f, yPos, titlePaint)
                yPos += 18f

                val metaText = "${item.yearTag} • ${item.subjectTag.ifEmpty { "Ayurveda Department" }} • Type: ${item.type.name.replace('_', ' ')}"
                canvas.drawText(metaText, 30f, yPos, subTitlePaint)
                yPos += 28f

                // Shloka Reference Box
                val shlokaRect = Rect(30, yPos.toInt(), pageWidth - 30, (yPos + 75f).toInt())
                canvas.drawRect(shlokaRect, shlokaBoxPaint)
                canvas.drawRect(shlokaRect, shlokaBorderPaint)

                canvas.drawText("Classical Sanskrit Shloka Reference:", 45f, yPos + 22f, shlokaTextPaint)
                val shlokaLine1 = "॥ शरीरमिन्द्रियसत्त्वात्मसंयोगो धारि जीवितम् । नित्यगश्चानुबन्धश्च पर्यायैरायुरुच्यते ॥"
                canvas.drawText(shlokaLine1, 45f, yPos + 42f, bodyPaint)
                canvas.drawText("(Charaka Samhita Sutrasthana 1/42 - Definition and Synonyms of Ayu)", 45f, yPos + 60f, footerPaint)
                yPos += 95f

                // Section 1: Overview & Fundamental Concepts
                canvas.drawText("1. Core Fundamental Principles & Lakshana", 30f, yPos, sectionHeadingPaint)
                yPos += 18f

                val introLines = listOf(
                    "• The term Ayu comprises Sharira (body), Indriya (senses), Satva (mind), and Atma (soul).",
                    "• Equilibrium of Dhatus (Dhatusamya) is the definition of health (Arogya / Prakriti).",
                    "• Tridosha (Vata, Pitta, Kapha) govern all physiological, pathological, and metabolic actions.",
                    "• Agni (Digestive fire) is central to the formation of Ahara Rasa and replenishment of Sapta Dhatus.",
                    "• Ama (undigested toxic metabolite) is the prime initiating factor for systemic pathogenesis (Samprapti)."
                )
                for (line in introLines) {
                    canvas.drawText(line, 35f, yPos, bodyPaint)
                    yPos += 15f
                }
                yPos += 15f

                // Section 2: Clinical Significance
                canvas.drawText("2. Clinical Relevance & Applied Physiology", 30f, yPos, sectionHeadingPaint)
                yPos += 18f

                val clinicalLines = listOf(
                    "• Diagnostic Assessment: In clinical practice, Ashtavidha Pariksha (Nadi, Mutra, Mala, Jihva,",
                    "  Shabda, Sparsha, Drik, Akriti) must be systematically performed before prescribing chikitsa.",
                    "• Nidana Parivarjana: First line of management in all Ayurvedic disorders is strictly avoiding causative factors.",
                    "• Shodhana vs Shamana: When Doshas are excessively aggravated (Bahudosha Avastha), bio-cleansing",
                    "  (Panchakarma) is superior to palliative pacification (Shamana) to prevent recurrence."
                )
                for (line in clinicalLines) {
                    canvas.drawText(line, 35f, yPos, bodyPaint)
                    yPos += 15f
                }
                yPos += 15f

                // Section 3: Summary text
                if (item.contentSummary.isNotEmpty()) {
                    canvas.drawText("3. Key Takeaway Points", 30f, yPos, sectionHeadingPaint)
                    yPos += 18f
                    canvas.drawText("• ${item.contentSummary}", 35f, yPos, bodyPaint)
                    yPos += 20f
                }

            } else {
                // Page 2 or later: Exam PYQs & Revision Notes
                canvas.drawText("Chapter Revision • High-Yield Exam Notes", 30f, yPos, titlePaint)
                yPos += 25f

                canvas.drawText("Important Previous Year Questions (PYQs) & Long Answer Questions:", 30f, yPos, sectionHeadingPaint)
                yPos += 20f

                val pyqLines = listOf(
                    "Q1 [10 Marks]: Explain the Nirukti, Lakshana, and Types of Srotas with clinical significance.",
                    "Q2 [10 Marks]: Describe the Samprapti Ghatakas of Jwara according to Charaka and Madhava Nidana.",
                    "Q3 [5 Marks]: Differentiate between Prakrita and Vaikrita Dosha Vriddhi with modern correlations.",
                    "Q4 [5 Marks]: Write short notes on Samsarjana Krama post-Vamana therapy.",
                    "Q5 [Viva]: Enumerate the 107 Marmas according to structural classification (Sadhyo Pranahara, etc.)."
                )
                for (line in pyqLines) {
                    canvas.drawText(line, 35f, yPos, bodyPaint)
                    yPos += 22f
                }
                yPos += 20f

                // Table / Box of Dosha properties
                canvas.drawText("Comparative Dosha Guna Table (Quick Revision):", 30f, yPos, sectionHeadingPaint)
                yPos += 18f

                val tableRect = Rect(30, yPos.toInt(), pageWidth - 30, (yPos + 90f).toInt())
                canvas.drawRect(tableRect, shlokaBoxPaint)
                canvas.drawRect(tableRect, shlokaBorderPaint)

                canvas.drawText("Dosha      Primary Guna                 Seat (Sthana)          Main Function", 40f, yPos + 22f, shlokaTextPaint)
                canvas.drawText("VATA       Ruksha, Sheeta, Laghu, Chala   Pakvashaya, Kati, Asthi   Utsaha, Breathing, Movement", 40f, yPos + 42f, bodyPaint)
                canvas.drawText("PITTA      Sasneha, Ushna, Tikshna, Drava Nabhi, Amashaya, Rakta    Digestion, Vision, Body Temp", 40f, yPos + 62f, bodyPaint)
                canvas.drawText("KAPHA      Snigdha, Sheeta, Guru, Manda   Uras, Kantha, Shiras     Stability, Lubrication, Bala", 40f, yPos + 80f, bodyPaint)
                yPos += 110f

                canvas.drawText("Authorized Academic Circulation: Swasthya Notes Student Study Material", 30f, yPos, footerPaint)
            }

            // Footer
            val footerY = pageHeight - 25f
            canvas.drawLine(30f, footerY - 10f, (pageWidth - 30).toFloat(), footerY - 10f, footerPaint)
            canvas.drawText("Swasthya Notes • Strictly In-App Read Only • Copy Protected", 30f, footerY, footerPaint)
            canvas.drawText("Page $pageIndex of $totalPages", (pageWidth - 90).toFloat(), footerY, footerPaint)

            document.finishPage(page)
        }

        val outputStream = ByteArrayOutputStream()
        document.writeTo(outputStream)
        document.close()
        return outputStream.toByteArray()
    }
}
