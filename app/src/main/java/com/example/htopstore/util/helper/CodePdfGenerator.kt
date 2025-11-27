package com.example.htopstore.util.helper

import android.content.Context
import com.example.domain.model.Product
import com.example.htopstore.util.BarcodeGenerator
import com.example.htopstore.util.QrCodeGenerator

class CodePdfGenerator(
    private val products: List<Product>,
    private val context: Context
) {

    fun generateQrCodesPdf() {
        QrCodeGenerator.generateProductQRsPDF(
            context = context,
            products = products,
            columns = 5
        )
    }

    fun generateBarcodesPdf() {
        BarcodeGenerator.generateProductBarcodesPDF(
            context = context,
            products = products
        )
    }
}