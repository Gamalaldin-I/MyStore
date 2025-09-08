package com.example.htopstore.ui.qrGen

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.htopstore.data.local.repo.productRepo.ProductRepoImp
import com.example.htopstore.databinding.ActivityQrcodeGenBinding
import com.example.htopstore.domain.model.SelectionQrProduct
import com.example.htopstore.domain.useCase.GetPdfOFQrCodesUseCase
import com.example.htopstore.util.adapters.QrSelectAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QRCodeGenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQrcodeGenBinding
    private lateinit var getQrsUseCase: GetPdfOFQrCodesUseCase
    private lateinit var  productRepoImp: ProductRepoImp
    private lateinit var adapter: QrSelectAdapter
    private var li = mutableListOf<SelectionQrProduct>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityQrcodeGenBinding.inflate(layoutInflater)
        getAllProductsByNewest()
        setControllers()
        productRepoImp = ProductRepoImp(this)
        adapter = QrSelectAdapter(li,this)
        binding.recyclerView.adapter = adapter
        setContentView(binding.root)
        }
    private fun setControllers(){
        binding.generateQrs.setOnClickListener {// can get QR or barcode
            if (binding.radioGroup.checkedRadioButtonId == binding.barcode.id)
            {getQrsUseCase.getBarcodesPdf(adapter.getSelected())}
            else {getQrsUseCase.getQrsPdf(adapter.getSelected())}
        }
    }

    private fun getAllProductsByNewest(){
        lifecycleScope.launch(Dispatchers.IO){
            val list = productRepoImp.getProductsByNewest()
            getQrsUseCase = GetPdfOFQrCodesUseCase(list,this@QRCodeGenActivity)
            li = getQrsUseCase.getSelectionQrProducts() as MutableList<SelectionQrProduct>
            withContext(Dispatchers.Main) {
                // set the adapter
                adapter.updateData(li)
            }
        }
    }

    }