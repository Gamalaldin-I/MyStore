package com.example.htopstore.ui.archive

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.domain.model.Product
import com.example.domain.useCase.product.GetArchiveProductsUseCase
import com.example.domain.useCase.product.GetArchiveSizeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class ArchiveViewModel
    @Inject constructor(
        getArchiveProductsUseCase: GetArchiveProductsUseCase,
        getArchiveSizeUseCase: GetArchiveSizeUseCase,
        ): ViewModel(){
    val archive: LiveData<List<Product>> = getArchiveProductsUseCase().asLiveData()
    val archiveSize: LiveData<Int> = getArchiveSizeUseCase().asLiveData()


}