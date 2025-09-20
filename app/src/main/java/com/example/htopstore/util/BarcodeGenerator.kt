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
import kotlin.random.Random

object BarcodeGenerator {

    fun generateProductBarcodesPDF(context: Context, products: List<SelectionQrProduct>, columns: Int = 4) {
        if (products.isEmpty()) {
            Toast.makeText(context, "No products to generate barcodes", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfDoc = PdfDocument()
        val paint = android.graphics.Paint()

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create() // A4
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas

        val barcodeWidth = 100
        val barcodeHeight = 20

        val marginX = 20
        val marginY = 30

        val cellWidth = (pageInfo.pageWidth - (marginX * 2)) / columns
        val cellHeight = 70

        var col = 0
        var rowY = marginY

        for (product in products) {
            repeat(product.count) {
                // ✅ لو الـ id فيه مش 13 رقم، نولّد EAN-13 جديد
                val code = if (product.id.length == 13 && product.id.all { it.isDigit() }) {
                    product.id
                } else {
                    generateEAN13()
                }

                val barcodeBitmap = generateBarcode(code, barcodeWidth, barcodeHeight)
                val x = marginX + col * cellWidth
                val y = rowY

                barcodeBitmap?.let {
                    canvas.drawBitmap(it, x.toFloat(), y.toFloat(), paint)

                    paint.textSize = 10f
                    paint.color = Color.BLACK
                    canvas.drawText(
                        code,
                        x.toFloat(),
                        (y + barcodeHeight + 10).toFloat(),
                        paint
                    )
                    canvas.drawText(
                        "${product.name} (${product.type})",
                        x.toFloat(),
                        (y + barcodeHeight + 20).toFloat(),
                        paint
                    )
                }

                col++
                if (col >= columns) {
                    col = 0
                    rowY += cellHeight
                }

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

        pdfDoc.finishPage(page)

        val file = File(context.getExternalFilesDir(null), "products_barcode.pdf")
        FileOutputStream(file).use {
            pdfDoc.writeTo(it)
        }
        pdfDoc.close()

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

    // ✅ Barcode Generator (EAN-13)
    private fun generateBarcode(text: String, width: Int, height: Int): Bitmap? {
        return try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                text,
                BarcodeFormat.EAN_13, // 🟢 EAN-13 هنا
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

    // ✅ دالة لتوليد EAN-13 Valid
    private fun generateEAN13(): String {
        val baseDigits = (1..12).map { Random.nextInt(0, 10) }
        val sum = baseDigits.mapIndexed { index, digit ->
            if ((index + 1) % 2 == 0) digit * 3 else digit
        }.sum()
        val checksum = (10 - (sum % 10)) % 10
        return baseDigits.joinToString("") + checksum.toString()
    }
}
