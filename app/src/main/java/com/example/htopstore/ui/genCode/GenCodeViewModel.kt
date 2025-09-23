package com.example.htopstore.ui.genCode

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.domain.model.Product
import com.example.domain.useCase.product.GetAvailableProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GenCodeViewModel
    @Inject constructor(
        getAvailableProductsUseCase: GetAvailableProductsUseCase
    ): ViewModel(){
        val products : LiveData<List<Product>> = getAvailableProductsUseCase().asLiveData()
}