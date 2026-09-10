package com.example.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.db.RemedyEntity
import java.io.File
import java.io.FileOutputStream

object DocExporter {
    fun exportAndShareWordDoc(context: Context, remedies: List<RemedyEntity>): Boolean {
        return try {
            val htmlContent = buildWordDocHtml(remedies)
            val fileName = "Homeopathic_Materia_Medica.doc"
            val cacheDir = File(context.cacheDir, "exports")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val file = File(cacheDir, fileName)

            FileOutputStream(file).use { out ->
                // Write UTF-8 BOM
                out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                out.write(htmlContent.toByteArray(Charsets.UTF_8))
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/msword"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Homeopathic Materia Medica Booklet")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Export Materia Medica (.doc)"))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun buildWordDocHtml(remedies: List<RemedyEntity>): String {
        val remedyBlocks = remedies.mapIndexed { index, remedy ->
            """
            <div style="margin-bottom: 24pt; padding: 12pt; border-bottom: 1pt solid #E9EBEA;">
                <h2 style="color: #2E3632; font-size: 16pt; margin-bottom: 6pt;">REMEDY ${index + 1}: ${escapeHtml(remedy.name)}</h2>
                ${if (!remedy.source_material.isNull_or_blank()) "<p style='color: #6B8E80; font-style: italic; font-size: 11pt;'>Source Material: ${escapeHtml(remedy.source_material)}</p>" else ""}
                <div style="font-size: 12pt; color: #2E3632; line-height: 1.6; margin-top: 8pt;">
                    <p><strong>Full Profile:</strong></p>
                    <p>${escapeHtml(remedy.full_profile).replace("\n", "<br/>")}</p>
                    ${if (!remedy.indications.isNull_or_blank()) "<p><strong>Indications:</strong> ${escapeHtml(remedy.indications)}</p>" else ""}
                    ${if (!remedy.reasoning.isNull_or_blank()) "<p><strong>Reasoning:</strong> ${escapeHtml(remedy.reasoning)}</p>" else ""}
                    ${if (!remedy.benefits.isNull_or_blank()) "<p><strong>Expected Benefits:</strong> ${escapeHtml(remedy.benefits)}</p>" else ""}
                    ${if (!remedy.source_reference.isNull_or_blank()) "<p style='color: #6B8E80; font-size: 10pt;'><strong>Reference:</strong> ${escapeHtml(remedy.source_reference)}</p>" else ""}
                </div>
            </div>
            """.trimIndent()
        }.joinToString("\n")

        return """
        <html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
        <head>
            <meta charset='utf-8'/>
            <title>Homeopathic Materia Medica Booklet</title>
            <style>
                body {
                    font-family: 'Manrope', 'Segoe UI', Arial, sans-serif;
                    font-size: 12pt;
                    line-height: 1.6;
                    color: #2E3632;
                    margin: 20pt;
                }
                h1 {
                    color: #6B8E80;
                    font-size: 22pt;
                    text-align: center;
                    margin-bottom: 4pt;
                }
                .subtitle {
                    color: #6B8E80;
                    font-size: 14pt;
                    text-align: center;
                    margin-bottom: 24pt;
                }
            </style>
        </head>
        <body>
            <h1>HOMEOPATHIC MATERIA MEDICA BOOKLET</h1>
            <div class='subtitle'>Authentic Remedy Monographs from Verified Sources</div>
            $remedyBlocks
        </body>
        </html>
        """.trimIndent()
    }

    private fun escapeHtml(text: String?): String {
        if (text.isNullOrEmpty()) return ""
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
}
