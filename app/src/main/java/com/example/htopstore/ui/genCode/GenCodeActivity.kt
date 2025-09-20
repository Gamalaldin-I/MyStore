package com.example.htopstore.ui.genCode

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.domain.model.SelectionQrProduct
import com.example.htopstore.databinding.ActivityQrcodeGenBinding
import com.example.htopstore.util.adapters.QrSelectAdapter
import com.example.htopstore.util.helper.CodePdfGenerator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GenCodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQrcodeGenBinding
    private lateinit var getQrsUseCase: CodePdfGenerator
    private lateinit var adapter: QrSelectAdapter
    val vm : GenCodeViewModel by viewModels()
    private var li = mutableListOf<SelectionQrProduct>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityQrcodeGenBinding.inflate(layoutInflater)
        getAllProductsByNewest()
        setControllers()
        adapter = QrSelectAdapter(li,this)
        binding.recyclerView.adapter = adapter
        setContentView(binding.root)
        vm.getAvailableProducts()
    }
    private fun setControllers(){
        binding.generateQrs.setOnClickListener {// can get QR or barcode
            if (binding.radioGroup.checkedRadioButtonId == binding.barcode.id)
            {getQrsUseCase.getBarcodesPdf(adapter.getSelected())}
            else {getQrsUseCase.getQrsPdf(adapter.getSelected())}
        }
    }

    private fun getAllProductsByNewest(){
            vm.products.observe(this){
            getQrsUseCase = CodePdfGenerator(it,this)
            li = getQrsUseCase.mapToSelectionQrProducts() as MutableList<SelectionQrProduct>
                adapter.updateData(li)
    }
    }

}