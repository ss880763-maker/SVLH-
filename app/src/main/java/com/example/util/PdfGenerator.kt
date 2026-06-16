package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.model.BillEntity
import com.example.data.model.BillRoom
import com.example.data.local.RoomsConverter
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun generateBillPdf(
        context: Context,
        bill: BillEntity,
        signatureBitmap: Bitmap?,
        showHeader: Boolean = true,
        showCustomerDetails: Boolean = true,
        showBreakdown: Boolean = true,
        showSignatures: Boolean = true
    ): File? {
        val pdfDocument = PdfDocument()
        
        // A4 Specs: 595 x 842 points
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Paints construction
        val titlePaint = Paint().apply {
            color = Color.rgb(46, 26, 71) // Rich deep vibrant dark purple
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(103, 80, 164) // Medium vibrant purple
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(100, 100, 100)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val boldTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val accentBoldPaint = Paint().apply {
            color = Color.rgb(103, 80, 164)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val linePaint = Paint().apply {
            color = Color.rgb(220, 220, 220)
            strokeWidth = 1f
        }

        val thickLinePaint = Paint().apply {
            color = Color.rgb(103, 80, 164)
            strokeWidth = 2f
        }

        var yPosition = 45f

        // 1. Header Logo / Slogan SVE
        if (showHeader) {
            canvas.drawText("Siddhivinayak Electrical Services", 35f, yPosition, titlePaint)
            yPosition += 16f
            canvas.drawText("सिद्धिविनायक इलेक्ट्रिकल सर्व्हिसेस | Gold Standard Electrical Works", 35f, yPosition, subtitlePaint)
            yPosition += 22f

            canvas.drawLine(35f, yPosition, 560f, yPosition, thickLinePaint)
            yPosition += 20f
        } else {
            // Draw a subtle line if header is omitted
            canvas.drawLine(35f, yPosition, 560f, yPosition, linePaint)
            yPosition += 15f
        }

        // 2. Customer & Bill Details Block
        if (showCustomerDetails) {
            canvas.drawText("CUSTOMER & SITE DETAILS (ग्राहक माहिती):", 35f, yPosition, headerPaint)
            canvas.drawText("BILL INFO:", 390f, yPosition, headerPaint)
            yPosition += 16f

            canvas.drawText("Client Name: ${bill.customerName}", 35f, yPosition, textPaint)
            canvas.drawText("Bill No: #SE-${1000 + bill.id}", 390f, yPosition, textPaint)
            yPosition += 14f

            canvas.drawText("Mobile No: +91 ${bill.customerPhone}", 35f, yPosition, textPaint)
            canvas.drawText("Date: ${bill.date}", 390f, yPosition, textPaint)
            yPosition += 14f

            val addressVal = if (bill.siteAddress.isBlank()) "N/A" else bill.siteAddress
            canvas.drawText("Site Address: $addressVal", 35f, yPosition, textPaint)
            yPosition += 22f

            canvas.drawLine(35f, yPosition, 560f, yPosition, linePaint)
            yPosition += 20f
        }

        // Extract and list rooms & items
        val roomsList: List<BillRoom> = try {
            RoomsConverter().fromRoomsString(bill.roomsJson)
        } catch (e: Exception) {
            emptyList()
        }

        // 3. Room and Items breakdown table
        if (showBreakdown) {
            canvas.drawText("SERVICES & POINT FITTING DETAILS (तपशील सूची):", 35f, yPosition, headerPaint)
            yPosition += 18f

            // Table headers
            canvas.drawText("Points Description / Type (पॉइंट्स प्रकार)", 35f, yPosition, boldTextPaint)
            canvas.drawText("Qty (नग)", 335f, yPosition, boldTextPaint)
            canvas.drawText("Rate (दर)", 415f, yPosition, boldTextPaint)
            canvas.drawText("Total (एकूण ₹)", 495f, yPosition, boldTextPaint)
            yPosition += 8f
            canvas.drawLine(35f, yPosition, 560f, yPosition, linePaint)
            yPosition += 16f

            if (roomsList.isEmpty()) {
                canvas.drawText("No electrical fittings added to this quotation.", 35f, yPosition, textPaint)
                yPosition += 20f
            } else {
                for (room in roomsList) {
                    if (yPosition > 600f) break

                    // Draw Room Title Grouping header
                    val roomHeaderPaint = Paint().apply {
                        color = Color.rgb(46, 26, 71)
                        textSize = 10f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                    
                    canvas.drawRect(35f, yPosition - 10f, 560f, yPosition + 4f, Paint().apply {
                        color = Color.rgb(245, 240, 252)
                    })
                    canvas.drawText("ROOM: ${room.name.uppercase()}", 42f, yPosition, roomHeaderPaint)
                    yPosition += 18f

                    for (item in room.items) {
                        if (yPosition > 640f) break
                        canvas.drawText(item.type, 45f, yPosition, textPaint)
                        canvas.drawText(item.quantity.toString(), 340f, yPosition, textPaint)
                        canvas.drawText("₹${String.format("%.0f", item.rate)}", 415f, yPosition, textPaint)
                        canvas.drawText("₹${String.format("%.2f", item.total)}", 495f, yPosition, textPaint)
                        yPosition += 15f
                    }
                    yPosition += 5f
                }
            }
        } else {
            // Draw simplified line item details block if breakdown table is turned off
            canvas.drawText("SERVICES SUMMARY (सेवांचा गोषवारा):", 35f, yPosition, headerPaint)
            yPosition += 16f
            val totalPointsCount = roomsList.flatMap { it.items }.sumOf { it.quantity }
            val roomsCount = roomsList.size
            canvas.drawText("Fittings across $roomsCount Room(s) - Total of $totalPointsCount point(s) recorded in full.", 35f, yPosition, textPaint)
            yPosition += 15f
        }

        // Keep a modest vertical spacer before billing totals
        yPosition += 15f

        canvas.drawLine(35f, yPosition, 560f, yPosition, linePaint)
        yPosition += 18f

        // Totals billing overview
        val totalSubtotal = roomsList.flatMap { it.items }.sumOf { it.total }
        val isGst = bill.totalAmount > totalSubtotal

        canvas.drawText("Points Subtotal (उपएकूण):", 35f, yPosition, textPaint)
        canvas.drawText("₹${String.format("%.2f", totalSubtotal)}", 185f, yPosition, textPaint)

        // Advance paid / Balance due
        canvas.drawText("Advance Paid (अग्रिम रक्कम):", 300f, yPosition, textPaint)
        canvas.drawText("₹${String.format("%.2f", bill.advanceAmount)}", 460f, yPosition, textPaint)
        yPosition += 15f

        if (isGst) {
            val gstVal = totalSubtotal * 0.18
            canvas.drawText("GST 18% inclusive:", 35f, yPosition, textPaint)
            canvas.drawText("₹${String.format("%.2f", gstVal)}", 185f, yPosition, textPaint)
        } else {
            canvas.drawText("GST Mode:", 35f, yPosition, textPaint)
            canvas.drawText("Exempted / Zero GST", 185f, yPosition, textPaint)
        }

        canvas.drawText("Balance Due (उर्वरित देय):", 300f, yPosition, boldTextPaint)
        val balanceDue = bill.totalAmount - bill.advanceAmount
        val balanceColorPaint = Paint().apply {
            color = if (balanceDue > 0) Color.rgb(179, 38, 30) else Color.rgb(46, 125, 50)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("₹${String.format("%.2f", balanceDue)}", 460f, yPosition, balanceColorPaint)
        yPosition += 16f

        canvas.drawText("GRAND TOTAL (एकूण):", 35f, yPosition, boldTextPaint)
        canvas.drawText("₹${String.format("%.2f", bill.totalAmount)}", 185f, yPosition, boldTextPaint)
        yPosition += 35f

        // 4. Digital signature block / Canvas section
        if (showSignatures) {
            canvas.drawLine(35f, yPosition, 560f, yPosition, linePaint)
            yPosition += 15f

            canvas.drawText("Customer Digital Signature (ग्राहकाची सही):", 35f, yPosition, headerPaint)
            canvas.drawText("Authorized Seal & Signature (सिद्धिविनायक इलेक्ट्रिकल):", 300f, yPosition, headerPaint)
            yPosition += 15f

            val finalSignatureBitmap = signatureBitmap ?: try {
                val sigFile = File(context.filesDir, "signature_${bill.id}.png")
                if (sigFile.exists()) {
                    android.graphics.BitmapFactory.decodeFile(sigFile.absolutePath)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }

            if (finalSignatureBitmap != null) {
                // Scale and draw the signature bitmap on the left side
                val destRect = android.graphics.Rect(35, yPosition.toInt(), 185, (yPosition + 50).toInt())
                canvas.drawBitmap(finalSignatureBitmap, null, destRect, Paint(Paint.FILTER_BITMAP_FLAG))
            } else {
                // Draw baseline for hand-signing
                canvas.drawLine(35f, yPosition + 40f, 185f, yPosition + 40f, textPaint)
                canvas.drawText("(Signed Digitally / Offline)", 35f, yPosition + 52f, Paint().apply {
                    color = Color.GRAY
                    textSize = 8f
                })
            }

            // authorized signature line
            canvas.drawLine(300f, yPosition + 40f, 500f, yPosition + 40f, textPaint)
            canvas.drawText("(Siddhivinayak Electrical Services)", 300f, yPosition + 52f, Paint().apply {
                color = Color.GRAY
                textSize = 8f
            })
        }

        pdfDocument.finishPage(page)

        // Save PDF to cache or documents directory for immediate viewing
        val pdfFile = File(context.cacheDir, "Siddhivinayak_Bill_${bill.id}.pdf")
        return try {
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            pdfFile
        } catch (e: Exception) {
            pdfDocument.close()
            null
        }
    }
}
