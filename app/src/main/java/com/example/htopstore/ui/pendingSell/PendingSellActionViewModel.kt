package com.example.htopstore.ui.pendingSell

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.domain.model.PendingSellAction
import com.example.domain.useCase.pendingSellActions.DeleteApprovedSellActionsUseCase
import com.example.domain.useCase.pendingSellActions.DeletePendingActionByIdUseCase
import com.example.domain.useCase.pendingSellActions.GetAllSellActionsUseCase
import com.example.domain.useCase.pendingSellActions.GetSellPendingActionByIdUseCase
import com.example.domain.useCase.sales.SellUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PendingSellActionViewModel @Inject constructor(
    getAllSellActionsUseCase: GetAllSellActionsUseCase,
    private val deleteApprovedSellActionsUseCase: DeleteApprovedSellActionsUseCase,
    private val deletePendingActionByIdUseCase: DeletePendingActionByIdUseCase,
    private val getSellPendingActionByIdUseCase: GetSellPendingActionByIdUseCase,
    private val sellUseCase: SellUseCase
): ViewModel(){

    companion object {
        private const val TAG = "PendingSellViewModel"
    }

    val actions: LiveData<List<PendingSellAction>> = getAllSellActionsUseCase().asLiveData()

    fun deleteAllApprovedActions(){
        viewModelScope.launch(Dispatchers.IO){
            try {
                deleteApprovedSellActionsUseCase()
                Log.d(TAG, "Approved actions deleted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete approved actions", e)
            }
        }
    }

    fun syncAllPendingActions(
        onFinish: () -> Unit,
        onProgress: (Float, Int, Boolean?) -> Unit
    ){
        viewModelScope.launch(Dispatchers.IO){
            val actionsList = actions.value ?: emptyList()

            Log.d(TAG, "Starting sync for ${actionsList.size} pending actions")

            actionsList.forEachIndexed { index, action ->
                var success: Boolean? = null

                try {
                    Log.d(TAG, "Syncing action #${action.id} (${index + 1}/${actionsList.size})")

                    val result = sellUseCase(
                        id = action.id,
                        billId = action.billId,
                        cartList = action.soldProducts,
                        discount = action.discount,
                        billInserted = action.billInserted,
                        soldItemsInserted = action.soldItemsInserted
                    ) { progress ->
                        // Report progress during sync (success = null means in progress)
                        onProgress(progress, index, null)
                    }

                    // Determine if operation was successful
                    success = result.contains("success", ignoreCase = true) ||
                            result.contains("completed", ignoreCase = true)

                    Log.d(TAG, "Action #${action.id} result: $result (success=$success)")

                } catch (e: Exception) {
                    success = false
                    Log.e(TAG, "Failed to sync action #${action.id}", e)
                }

                // Report final status for this item
                onProgress(100f, index, success)
            }

            Log.d(TAG, "Sync completed for all actions")
            onFinish()
        }
    }
    fun syncPendingProcess(pendingSellAction: PendingSellAction,
                           onProgress: (Float) -> Unit,
                           onFinish: (msg:String) -> Unit){
        viewModelScope.launch(Dispatchers.IO){
            try {
                val msg = sellUseCase(
                id = pendingSellAction.id,
                billId = pendingSellAction.billId,
                cartList = pendingSellAction.soldProducts,
                discount = pendingSellAction.discount,
                billInserted = pendingSellAction.billInserted,
                soldItemsInserted = pendingSellAction.soldItemsInserted) { progress ->
                    onProgress(progress)
                }
                withContext(Dispatchers.Main){
                onFinish(msg)
            }
            }catch (_:Exception){
                withContext(Dispatchers.Main){
                onFinish("Failed to sync transaction. Please try again.")
                }
            }
        }
    }
    fun deletePendingActionById(id: Int,onFinish: () -> Unit){
        viewModelScope.launch(Dispatchers.IO){
            deletePendingActionByIdUseCase(id)
            withContext(Dispatchers.Main){
                onFinish()
            }
        }
    }
    fun getPendingActionById(id: Int,onGet: (PendingSellAction) -> Unit) {
        viewModelScope.launch(Dispatchers.IO){
            val penAc = getSellPendingActionByIdUseCase(id)
            withContext(Dispatchers.Main){
                onGet(penAc)
            }
        }
    }
}
