package com.example.htopstore.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.example.domain.model.SelectionQrProduct

object QrCodeGenerator {

    fun generateProductQRsPDF(context: Context, products: List<SelectionQrProduct>, columns: Int = 5) {
        if (products.isEmpty()) {
            Toast.makeText(context, "No products to generate QR codes", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfDoc = PdfDocument()
        val paint = android.graphics.Paint()

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas

        val qrSize = 100
        val marginX = 30
        val marginY = 50
        val cellWidth = (pageInfo.pageWidth - (marginX * 2)) / columns
        val cellHeight = 150

        var col = 0
        var rowY = marginY

        for (product in products) {
            repeat(product.count) {
                val qrBitmap = generateQRCode(product.id, qrSize, qrSize)
                val x = marginX + col * cellWidth
                val y = rowY

                qrBitmap?.let {
                    canvas.drawBitmap(it, x.toFloat(), y.toFloat(), paint)
                    paint.textSize = 10f
                    paint.color = Color.BLACK
                    canvas.drawText(product.name, x.toFloat(), (y + qrSize + 15).toFloat(), paint)
                    canvas.drawText(product.id, x.toFloat(), (y + qrSize + 30).toFloat(), paint)
                }

                col++
                if (col >= columns) {
                    col = 0
                    rowY += cellHeight
                }

                // لو الصفحة خلصت، نعمل صفحة جديدة
                if (rowY + cellHeight > pageInfo.pageHeight - marginY) {
                    pdfDoc.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDoc.startPage(pageInfo)
                    canvas = page.canvas
                    rowY = marginY
                    col = 0
                }
            }
        }

        // ✅ نقفل آخر صفحة بس لو فيها شغل
        pdfDoc.finishPage(page)

        // نخزن الملف في external files (آمن أكتر مع FileProvider)
        val file = File(context.getExternalFilesDir(null), "products_qr.pdf")
        FileOutputStream(file).use {
            pdfDoc.writeTo(it)
        }
        pdfDoc.close()

        // فتح الـ PDF أوتوماتيك
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "PDF saved at: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    }

    // QR Generator
    private fun generateQRCode(text: String, width: Int, height: Int): Bitmap? {
        return try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                width,
                height
            )
            val bmp = createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
