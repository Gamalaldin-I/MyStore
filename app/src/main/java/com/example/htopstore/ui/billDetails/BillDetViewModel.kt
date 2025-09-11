package com.example.htopstore.ui.billDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.htopstore.data.local.model.SoldProduct
import com.example.htopstore.data.local.model.relation.SalesOpsWithDetails
import com.example.htopstore.domain.useCase.billDetails.DeleteBillUseCase
import com.example.htopstore.domain.useCase.billDetails.GetBillDetailsUseCse
import com.example.htopstore.domain.useCase.billDetails.InsertReturnProduct
import com.example.htopstore.domain.useCase.billDetails.UpdateProductQuantityAfterReturn
import com.example.htopstore.domain.useCase.billDetails.UpdateSoldProductQuantityAfterReturn
import com.example.htopstore.domain.useCase.billDetails.UpdateTotalCashOfBillAfterReturn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BillDetViewModel @Inject constructor(
    private val updateBillCashUseCase : UpdateTotalCashOfBillAfterReturn,
    private val updateProductQuantityAfterReturn: UpdateProductQuantityAfterReturn,
    private val getBillDetails : GetBillDetailsUseCse,
    private val insertReturnProduct: InsertReturnProduct,
    private val updateSoldProductQuantity: UpdateSoldProductQuantityAfterReturn,
    private val deleteBillUseCase: DeleteBillUseCase
): ViewModel() {

    private val _sellOp = MutableLiveData<SalesOpsWithDetails>()
    val sellOp: LiveData<SalesOpsWithDetails> = _sellOp

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message


    fun getSellOp(id: String, onEmptyProducts: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val sellOp = getBillDetails(id)
            withContext(Dispatchers.Main) {
                _sellOp.value = sellOp
                if (sellOp.soldProducts.isEmpty()) {
                    deleteSale(id) { onEmptyProducts() }
                }
            }
        }
    }


    fun onClick(
        soldProduct: SoldProduct,
        returnRequest: SoldProduct,
        onEmptyProducts: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updateBillCash = async {updateBillCashUseCase(soldProduct, returnRequest)}
            val insertReturn = async { insertReturnProduct(returnRequest) }
            val updateProductQuantity = async {updateProductQuantityAfterReturn(returnRequest)}
            val updateBillProductQuantity = async {
                updateSoldProductQuantity(soldProduct, returnRequest)
            }

            listOf(updateBillCash, insertReturn, updateProductQuantity).awaitAll()
            _message.postValue(updateBillProductQuantity.await())
            getSellOp(soldProduct.saleId!!) { onEmptyProducts() }
        }
    }

    fun deleteSale(id: String, onFinish: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteBillUseCase(id)
            withContext(Dispatchers.Main) { onFinish() }
        }
    }
}
