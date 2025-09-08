package com.example.htopstore.domain.useCase

import android.content.Context
import com.example.htopstore.data.local.model.Product
import com.example.htopstore.domain.model.SelectionQrProduct
import com.example.htopstore.util.BarcodeGenerator
import com.example.htopstore.util.QrCodeGenerator

class GetPdfOFQrCodesUseCase(var list: List<Product>,val context: Context){

    private val selectionQrProducts = mutableListOf<SelectionQrProduct>()

    fun getSelectionQrProducts(): List<SelectionQrProduct> {
        for (product in list) {
            val selectionQrProduct = SelectionQrProduct(
                name = product.name,
                id = product.id,
                type = product.category,
                image = product.productImage,
                count = product.count
            )
            selectionQrProducts.add(selectionQrProduct)
        }
        return selectionQrProducts
    }

    fun getQrsPdf(selected: ArrayList<SelectionQrProduct>){
        QrCodeGenerator.generateProductQRsPDF(context = context, products = selected, columns = 5)
    }
    fun getBarcodesPdf(selected: ArrayList<SelectionQrProduct>){
        BarcodeGenerator.generateProductBarcodesPDF(context = context, products = selected)
    }


}