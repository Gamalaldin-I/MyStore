package com.example.htopstore.domain.useCase.billDetails

import com.example.htopstore.data.local.model.SoldProduct
import com.example.htopstore.data.local.repo.billDetails.BillDetailsRepoImp
import javax.inject.Inject
import kotlin.math.abs

class UpdateTotalCashOfBillAfterReturn
    @Inject constructor(private val localRpo: BillDetailsRepoImp){
     suspend operator fun invoke(soldProduct: SoldProduct, returnRequest: SoldProduct) {
             val returnValue = returnRequest.sellingPrice * abs(returnRequest.quantity)
             localRpo.updateSaleCashAfterReturn(soldProduct.saleId!!, returnValue)

         }
}