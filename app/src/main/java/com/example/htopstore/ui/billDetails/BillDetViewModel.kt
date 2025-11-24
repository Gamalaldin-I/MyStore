package com.example.htopstore.ui.billDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BillWithDetails
import com.example.domain.model.SoldProduct
import com.example.domain.useCase.bill.DeleteBillUseCase
import com.example.domain.useCase.billDetails.GetBillDetailsUseCse
import com.example.domain.useCase.billDetails.ReturnProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BillDetViewModel @Inject constructor(

    private val getBillDetails : GetBillDetailsUseCse,
    private val insertReturnProduct: ReturnProductUseCase,
    private val deleteBillUseCase: DeleteBillUseCase
): ViewModel() {

    private val _sellOp = MutableLiveData<BillWithDetails>()
    val sellOp: LiveData<BillWithDetails> = _sellOp

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message


    fun getBill(id: String, confirmDeleteIfEmpty:()-> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val sellOp = getBillDetails(id)
            withContext(Dispatchers.Main) {
                _sellOp.value = sellOp
                if (sellOp.soldProducts.isEmpty()) {
                    //show alert Dialog to delete bill
                    confirmDeleteIfEmpty()
                }
            }
        }
    }


    fun onClick(
        soldProduct: SoldProduct,
        returnRequest: SoldProduct,
        confirmDeleteIfEmpty: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = insertReturnProduct(soldProduct, returnRequest)) {
                is ReturnProductUseCase.ReturnResult.Error -> {
                    _message.postValue("Return failed: ${result.message}")
                }
                is ReturnProductUseCase.ReturnResult.Success -> {
                    _message.postValue(result.message)
                    getBill(soldProduct.billId!!) { confirmDeleteIfEmpty() }
                }
            }
        }
    }

    fun deleteBill(id: String, onFinish: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteBillUseCase(id)
            withContext(Dispatchers.Main) { onFinish() }
        }
    }
}
